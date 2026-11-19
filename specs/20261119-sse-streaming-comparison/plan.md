# Implementation Plan: Kotlin POST SSE 스트리밍 구현 방식별 비교 및 성능 벤치마크

**Branch**: `20261119-sse-streaming-comparison` | **Date**: 2026-11-19 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/20261119-sse-streaming-comparison/spec.md`

## Summary

저장소 루트에 멀티모듈 Gradle 프로젝트 `sse-compare/`를 만들고, 동일한 `POST /chat/stream` SSE 계약을 6개 구성(A, B, C1, C2, D, E)으로 구현한다. 구성은 4개의 실행 모듈과 설정 프로파일로 나눈다. 공통 모듈이 요청/이벤트 모델, 이벤트 생성 규칙, 계약 테스트 베이스를 제공하고, Kotlin 코루틴 부하 클라이언트(`loadgen`)와 Docker Compose(Prometheus/Grafana/MySQL), k6 보조 스크립트로 S1~S6 시나리오를 실행해 결과 보고서를 만든다. 설계 판단 근거는 [research.md](research.md)에 있다.

## Technical Context

**Language/Version**: Kotlin 1.9.25, Java 21 (Virtual Thread 필요)

**Primary Dependencies**:
- Spring Boot 3.4.x (`spring-boot-starter-web`, `spring-boot-starter-webflux`, `spring-boot-starter-actuator`, `spring-boot-starter-validation`)
- Kotlin Coroutines (`kotlinx-coroutines-core`, `kotlinx-coroutines-reactor`, `kotlinx-coroutines-slf4j`)
- Micrometer Prometheus (`micrometer-registry-prometheus`)
- Kotlin Logging (`io.github.oshai:kotlin-logging-jvm`)
- 부하 클라이언트: Ktor Client(CIO 엔진) 또는 JDK `HttpClient`. 선택은 research.md 참고.
- 영속성: `spring-boot-starter-data-jpa` + MySQL Connector/J (블로킹), `spring-boot-starter-data-r2dbc` + `r2dbc-mysql` (논블로킹)

**Monitoring**: Prometheus 2.55 + Grafana 11 (Docker, `docker/monitoring/`)

**Storage**: MySQL 8.4 (Docker Compose, 테스트는 Testcontainers). 테이블 `chat_message` 하나.

**Testing**: Kotest 5.9.x (`BehaviorSpec`), `kotest-extensions-spring`, MockK/SpringMockk, Testcontainers(MySQL). 공통 계약 테스트는 `common`의 `testFixtures`에 두고 모든 앱 모듈이 상속한다.

**Target Platform**: macOS 로컬 Docker (기본), Linux 재실행 가능

**Project Type**: 멀티모듈 Gradle 웹 서비스 + 벤치마크 도구

**Performance Goals**: 스펙 SC-003 기준. 수치 목표는 없고 조합 간 상대 비교와 한계 연결 수 식별이 목적이다.

**Constraints**:
- 모든 앱 컨테이너 동일 제한: `cpus: 2`, `memory: 1g`, `-Xmx512m`, 동일 GC(G1)
- 조합은 한 번에 하나만 실행, 워밍업 후 측정, 3회 반복
- HikariCP/R2DBC 풀 크기 동일(`10`)
- 부하기는 별도 컨테이너/프로세스에서 실행하고 CPU 사용률을 같이 기록

**Scale/Scope**: 앱 모듈 4개(+공통 1, 영속성 2, 부하기 1), 구성 6개, 시나리오 6개, 연결 수 최대 10,000(로컬 한계 확인 후 조정)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Status | Notes |
|:---|:---|:---:|:---|
| **1. Kotlin & Spring Boot Standards** | Kotlin 관용구, Spring Boot 3.x, Gradle Kotlin DSL | **PASS** | Boot 3.4.x, Java 21, `build.gradle.kts` |
| **2. Kotlin-friendly Logging** | `kotlin-logging` 사용 | **PASS** | 모든 모듈에서 `KotlinLogging.logger {}` |
| **3. BDD-Style Testing** | Kotest `BehaviorSpec`, MockK, Testcontainers | **PASS** | 공통 계약 테스트와 DB 통합 테스트를 Kotest로 작성 |
| **4. Modular PoC Structure** | 주제 디렉토리, 자체 완결, `./gradlew bootRun`/`test` 가능 | **PASS** | 루트 `sse-compare/`, 모듈별 실행 가능. 주제 디렉토리가 아닌 루트에 두는 것은 사용자 지시 |
| **5. Spec-Driven Knowledge Archiving** | 목적, 인수 기준, 검증 | **PASS** | spec/plan/research/quickstart + `sse-compare/docs/research/` |
| **Spec Directory Naming** | `specs/<yyyyMMdd>-<short-name>` | **PASS** | `20261119-sse-streaming-comparison` |

**Post-design re-check**: PASS. 새 위반 없음.

## Project Structure

### Documentation (this feature)

```text
specs/20261119-sse-streaming-comparison/
├── spec.md
├── plan.md              # 이 파일
├── research.md          # Phase 0
├── data-model.md        # Phase 1
├── quickstart.md        # Phase 1
├── contracts/
│   └── chat-stream-api.yaml   # Phase 1
├── checklists/
│   └── requirements.md
└── tasks.md             # /speckit-tasks 에서 생성
```

### Source Code Layout

```text
sse-compare/
├── settings.gradle.kts
├── build.gradle.kts                 # 공통 플러그인/버전 관리
├── gradle/wrapper/...
├── README.md
├── common/                          # 공통: StreamRequest, StreamEvent, EventSource(생성 규칙), 계약 테스트(testFixtures)
├── persistence-jpa/                 # 블로킹 저장소 (JPA, MySQL)
├── persistence-r2dbc/               # 논블로킹 저장소 (R2DBC, MySQL)
├── app-mvc-sseemitter/              # A, B  (프로파일 `vt` 로 Virtual Thread 활성화)
├── app-mvc-flow/                    # C1, C2 (프로파일 `vt` 로 VT 디스패처)
├── app-webflux-flux/                # D
├── app-webflux-flow/                # E
├── loadgen/                         # Kotlin 코루틴 부하 클라이언트 (S1~S6)
├── infra/
│   ├── compose.yml                  # MySQL, 앱(프로파일별). 모니터링은 docker/monitoring 사용
│   ├── Dockerfile                   # 앱 공용 이미지 (모듈명 ARG)
│   ├── mysql/init.sql
│   └── k6/                          # 교차 검증용 스크립트
├── scripts/
│   └── run-benchmark.sh             # <combo> <scenario> 실행, 결과 저장
├── results/                         # 측정 결과(CSV/JSON), 보고서 RESULTS.md
└── docs/
    └── research/
        └── mvc-flow-vs-webflux-flow.md

docker/monitoring/                   # 저장소 공용 모니터링 스택 (Prometheus + Grafana, Docker)
├── docker-compose.yml
├── prometheus/prometheus.yml
├── grafana/provisioning/{datasources,dashboards}/
├── grafana/dashboards/sse-compare.json
└── README.md
```

**Structure Decision**: 구성 6개를 모듈 6개로 늘리지 않고, 서버 스택과 반환 타입이 같은 구성은 한 모듈에서 프로파일로 변형한다(A/B, C1/C2). 코드 중복을 줄이고 "한 변수만 바꾼" 비교를 보장한다. DB 접근 방식(JPA, R2DBC)은 별도 모듈로 분리해 앱이 프로파일로 선택한다.

### 구성 매핑

| 구성 | 모듈 | 프로파일 / 설정 |
|------|------|-----------------|
| A | `app-mvc-sseemitter` | 기본. Tomcat 플랫폼 스레드 풀 |
| B | `app-mvc-sseemitter` | `vt` (`spring.threads.virtual.enabled=true`) |
| C1 | `app-mvc-flow` | 기본. `Dispatchers.Default` |
| C2 | `app-mvc-flow` | `vt`. VT 기반 디스패처(`Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()`) |
| D | `app-webflux-flux` | 기본 |
| E | `app-webflux-flow` | 기본 |

DB 모드: 프로파일 `db-jpa`, `db-r2dbc`(WebFlux 앱만), 미지정 시 DB 없음. WebFlux의 JPA 방식은 `Dispatchers.IO`(D는 `boundedElastic`)로 격리한다.

## Complexity Tracking

| 항목 | 필요한 이유 | 더 단순한 대안을 거부한 이유 |
|------|-------------|------------------------------|
| 앱 모듈 4개 + 지원 모듈 4개 | Tomcat/Netty 스택은 같은 앱에서 동시에 쓸 수 없고, 공통 계약 보장이 필요함 | 단일 모듈은 스타터 충돌과 클래스패스 오염으로 성능 비교가 오염됨 |
| Docker Compose(`docker/monitoring`) + Prometheus + Grafana | FR-017 서버 지표 수집/시각화 | 로그/JFR만으로는 비교 시각화가 어려움 |
| 커스텀 Kotlin 부하기 | FR-016 이벤트별 지연 측정 (clarify 확정) | k6는 POST SSE 이벤트 단위 측정이 제한됨 |
