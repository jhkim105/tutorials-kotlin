# Phase 0 Research: SSE 스트리밍 비교 설계 결정

모든 Technical Context 항목이 확정되어 `NEEDS CLARIFICATION`은 남지 않았다. 아래는 주요 결정과 근거다. 구현 단계에서 PoC로 검증할 항목은 **[PoC]**로 표시한다.

## 1. Spring MVC에서 Kotlin `Flow` 반환

- **Decision**: C1/C2는 컨트롤러가 `Flow<ServerSentEvent<...>>`를 직접 반환한다.
- **Rationale**: `kotlinx-coroutines-reactor`가 클래스패스에 있으면 `ReactiveAdapterRegistry`가 `Flow`를 `Publisher`로 어댑트하고, MVC가 `text/event-stream`일 때 스트리밍 응답으로 처리한다.
- **Alternatives**: `SseEmitter` 안에서 코루틴을 직접 실행(구성 B 범위와 겹침), `Flux` 반환(구성 D와 혼동).
- **[PoC]** Boot 3.4.x에서 `produces = text/event-stream`일 때 쓰기 스레드와 취소 전파(클라이언트 끊김 시 `Flow` 취소)가 의도대로 동작하는지 확인한다.

## 2. 코루틴 디스패처 변형 (C1 / C2)

- **Decision**: C1은 `Dispatchers.Default`, C2는 `Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()`를 `flowOn`에 적용한다.
- **Rationale**: clarify 결과(디스패처 2종 비교). 블로킹 호출이 섞일 때의 차이를 보기 위함이다.
- **Alternatives**: `Dispatchers.IO` 추가(clarify에서 제외).

## 3. Virtual Thread 활성화 (B)

- **Decision**: `spring.threads.virtual.enabled=true` 프로파일(`vt`). Tomcat 요청 처리와 `SseEmitter` 이벤트 생성 작업이 VT에서 실행되도록 한다.
- **Rationale**: Spring Boot 3.2+ 공식 설정으로 코드 변경이 없다.
- **[PoC]** `synchronized` 사용으로 인한 pinning 여부를 JFR(`jdk.VirtualThreadPinned`)로 확인한다. Java 21에서는 `synchronized` 블록이 캐리어 스레드를 고정할 수 있다.

## 4. WebFlux DB 접근 (D, E)

- **Decision**: (1) JPA를 격리 스케줄러로 호출(`Dispatchers.IO`/`boundedElastic`)하는 `db-jpa`와 (2) R2DBC `db-r2dbc` 두 방식을 모두 제공한다.
- **Rationale**: FR-013. 이벤트 루프에서 블로킹 호출 금지 원칙을 지키면서 두 방식의 성능 차이를 비교한다.
- **Alternatives**: R2DBC만 제공(JPA 격리 비교 불가).
- **리스크**: 한 앱에 JPA와 R2DBC를 모두 두면 자동 설정이 충돌할 수 있다. 프로파일별 `@ConditionalOnProperty` 설정으로 분리하고, 문제가 생기면 R2DBC 앱 모듈을 별도로 분리한다.

## 5. 부하 클라이언트

- **Decision**: Kotlin 코루틴 기반 `loadgen` 모듈을 주 부하기로 하고 k6를 교차 검증에 쓴다. HTTP 클라이언트는 Ktor Client(CIO)를 우선 사용하고, POST SSE 청크 파싱이 불안정하면 JDK `HttpClient`(HTTP/1.1, 스트리밍 본문)로 대체한다.
- **Rationale**: 코루틴 하나가 연결 하나를 맡으므로 수만 동시 연결을 낮은 오버헤드로 만들 수 있고, 이벤트 단위 수신 시각을 직접 기록할 수 있다.
- **Alternatives**: k6 `xk6-sse`(POST 이벤트별 측정이 제한적), Gatling(SSE 지원은 있으나 별도 DSL 학습 비용).
- **[PoC]** 10,000 연결에서 부하기 CPU와 파일 디스크립터 한계를 먼저 확인한다(`ulimit -n`).

## 6. 메트릭 수집

- **Decision**: Micrometer + Actuator `/actuator/prometheus`, Prometheus가 5초 간격 수집, Grafana 대시보드 프로비저닝. Prometheus·Grafana는 Docker로 실행하며 저장소 공용 `docker/monitoring/`에 둔다(앱은 호스트에서 실행하고 `host.docker.internal`로 수집). 커스텀 지표: `sse.active.connections`(Gauge), `sse.events.sent`(Counter). 부하기는 결과를 JSON/CSV 파일로 저장한다.
- **Rationale**: 서버 지표(스레드, 힙, GC, Hikari, Tomcat/Netty)는 기본 지표로 충분하다.
- **Alternatives**: JFR만 사용(실시간 비교 시각화 불가).

## 7. 공정성 조건

- **Decision**: 모든 앱을 같은 베이스 이미지, `cpus: 2`, `memory: 1g`, `-Xmx512m -XX:+UseG1GC`로 실행한다. 풀 크기는 DB(Hikari/R2DBC) 10, Tomcat `max-threads` 200, `max-connections` 20,000으로 명시해 고정한다. 한 번에 한 앱만 기동한다.
- **Rationale**: FR-018. Tomcat 기본값이 결과를 좌우하므로 명시적으로 설정한다.
- **리스크**: Docker Desktop(VM)은 편차가 크므로 결과를 상대 비교로만 해석한다(spec Assumptions).

## 8. 오류·취소·heartbeat 동작 통일

- **Decision**:
  - 오류: 스트림 시작 후 오류 시 `event: error` + `data: {code, message}`를 보내고 정상 종료. 테스트용 오류는 요청 `failAt`으로 유발(`SIMULATED_ERROR`).
  - 검증 오류: 스트림 시작 전 `400` + 공통 오류 본문(`{code, message}`).
  - heartbeat: SSE 주석 행(`: ping`)을 기본 15초 간격(설정 가능).
  - 취소: 연결 종료 시 생성 작업을 중단하고 `sse.active.connections`를 감소.
- **Rationale**: FR-003~006. 구현이 다른 4개 모듈에서 외부 동작을 같게 만들기 위해 공통 계약 테스트의 기준으로 삼는다.

## 9. 공통 계약 테스트 공유 방식

- **Decision**: `common`의 `java-test-fixtures`로 추상 Kotest 스펙(`ChatStreamContractSpec`)을 제공하고, 각 앱 모듈이 `@SpringBootTest(webEnvironment = RANDOM_PORT)`로 상속해 실행한다. 클라이언트는 테스트용 `WebClient`/JDK `HttpClient`를 사용한다.
- **Rationale**: 한 곳에서 계약을 정의해 SC-001을 자동 검증한다.

## 10. 스펙 대비 미세 조정

- `GET /stream` 기준선(FR-007)은 동일 서비스 로직을 호출하고 파라미터만 쿼리로 받는다.
- 이벤트 생성 규칙은 `common`의 순수 함수(순번, 페이로드 크기 고정)로 두어 모든 구성이 같은 바이트 수를 전송한다.
