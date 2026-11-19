# SSE Compare: Kotlin POST SSE 스트리밍 구현 방식별 비교 및 성능 벤치마크

Spring Boot 3.4 / Kotlin / Java 21 환경에서 실서비스에 적용 가능한 SSE 스트리밍 조합 6개를 동일한 API 계약(`POST /chat/stream`, `GET /stream`)으로 구현하고 성능을 비교하는 멀티모듈 프로젝트입니다.

## 비교 구성 (Combinations)

| ID | 모듈 | 프로파일 | 서버 | 동시성 모델 | 반환 타입 | 포트 |
|----|------|----------|------|-------------|-----------|------|
| **A** | `app-mvc-sseemitter` | (기본) | Tomcat | 플랫폼 스레드 풀 | `SseEmitter` | 8081 |
| **B** | `app-mvc-sseemitter` | `vt` | Tomcat | Virtual Thread | `SseEmitter` | 8081 |
| **C1** | `app-mvc-flow` | (기본) | Tomcat | `Dispatchers.Default` | `Flow<ServerSentEvent>` | 8082 |
| **C2** | `app-mvc-flow` | `vt` | Tomcat | VT Coroutine Dispatcher | `Flow<ServerSentEvent>` | 8082 |
| **D** | `app-webflux-flux` | (기본) | Netty | Reactor | `Flux<ServerSentEvent>` | 8083 |
| **E** | `app-webflux-flow` | (기본) | Netty | Coroutines (`suspend`) | `Flow<ServerSentEvent>` | 8084 |

- DB 모드 프로파일: `db-jpa` (모든 앱, WebFlux는 `Dispatchers.IO`/`boundedElastic` 격리), `db-r2dbc` (WebFlux 앱 전용)

## 프로젝트 구조

- `common/`: 요청/응답 모델, 이벤트 생성 규칙, 공통 메트릭, 계약 테스트 베이스(`testFixtures`)
- `persistence-jpa/`: 블로킹 JPA 저장소 (MySQL)
- `persistence-r2dbc/`: 논블로킹 R2DBC 저장소 (MySQL)
- `app-*`: 4개 실행 앱 모듈
- `loadgen/`: Kotlin 코루틴 기반 POST SSE 부하 클라이언트 및 결과 집계기
- `infra/`: Dockerfile, `compose.yml` (MySQL + 앱), k6 교차 검증 스크립트
- `scripts/`: 벤치마크 실행 스크립트 (`run-benchmark.sh`)
- `results/`: 측정 결과 JSON/CSV 및 최종 보고서 (`RESULTS.md`)
- `docs/research/`: 사전 조사 및 PoC 문서

## 관련 문서

- 스펙: [spec.md](../specs/20261119-sse-streaming-comparison/spec.md)
- 설계: [plan.md](../specs/20261119-sse-streaming-comparison/plan.md)
- API 계약: [chat-stream-api.yaml](../specs/20261119-sse-streaming-comparison/contracts/chat-stream-api.yaml)
- 조사 문서: [mvc-flow-vs-webflux-flow.md](docs/research/mvc-flow-vs-webflux-flow.md)
- 모니터링 스택: [docker/monitoring](../docker/monitoring/README.md)

## 주의사항

- **리버스 프록시/로드밸런서**: Nginx 등 프록시 환경에서는 `proxy_buffering off`, `X-Accel-Buffering: no`, `proxy_read_timeout` 조정이 필요하며 본 벤치마크 범위에서는 제외합니다.
- **로컬 Docker 결과 해석**: macOS Docker Desktop은 VM 위에서 실행되므로 절대 성능이 아닌 **구성 간 상대 비교용**으로 해석해야 합니다.
