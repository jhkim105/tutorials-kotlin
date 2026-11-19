---

description: "Task list for SSE streaming comparison and benchmark"
---

# Tasks: Kotlin POST SSE 스트리밍 구현 방식별 비교 및 성능 벤치마크

**Input**: Design documents from `specs/20261119-sse-streaming-comparison/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/chat-stream-api.yaml, quickstart.md

**Tests**: FR-021과 헌법(BDD, Testcontainers)에 따라 공통 계약 테스트와 DB 통합 테스트를 포함한다.

**Organization**: 사용자 스토리별로 묶어 독립 구현·검증이 가능하게 한다. 모든 경로는 저장소 루트 기준이다. (`SC` = `sse-compare/`)

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 병렬 실행 가능 (다른 파일, 미완료 작업에 의존하지 않음)
- **[Story]**: US1 계약, US2 순수 스트리밍 성능, US3 DB 포함 성능, US4 안정성, US5 보고서/추천

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 프로젝트 골격과 PoC

- [X] T001 Create Gradle multi-module skeleton in `sse-compare/settings.gradle.kts` and `sse-compare/build.gradle.kts` (modules: common, persistence-jpa, persistence-r2dbc, app-mvc-sseemitter, app-mvc-flow, app-webflux-flux, app-webflux-flow, loadgen), Kotlin 1.9.25, Java 21, Spring Boot 3.4.x, shared version catalog
- [X] T002 Add Gradle wrapper (Gradle 8.x compatible with Spring Boot 3.4 and Java 21; reuse `http-streaming/sse/gradle/wrapper` only if its version qualifies, otherwise generate with `gradle wrapper`) in `sse-compare/gradle/wrapper/` and `sse-compare/gradlew`
- [X] T003 [P] Create module `build.gradle.kts` files with dependencies per plan in `sse-compare/common/`, `sse-compare/persistence-jpa/`, `sse-compare/persistence-r2dbc/`, `sse-compare/loadgen/`
- [X] T004 [P] Create app module `build.gradle.kts` files in `sse-compare/app-mvc-sseemitter/`, `sse-compare/app-mvc-flow/`, `sse-compare/app-webflux-flux/`, `sse-compare/app-webflux-flow/` (web vs webflux starters, actuator, micrometer-prometheus, kotlin-logging, kotest)
- [X] T005 [P] Create `sse-compare/README.md` describing structure, combinations (A, B, C1, C2, D, E) and links to spec and research docs
- [X] T006 PoC: verify Spring MVC returns `Flow<ServerSentEvent>` as `text/event-stream` and cancels the Flow on client disconnect, record findings in `sse-compare/docs/research/poc-mvc-flow.md`
- [X] T007 [P] PoC: verify loadgen HTTP client (Ktor CIO vs JDK `HttpClient`) can read POST SSE chunks and timestamp each event, record choice in `sse-compare/docs/research/poc-loadgen-client.md`

**Checkpoint**: 모듈이 빌드되고 PoC 결과로 plan의 리스크가 해소된다.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 모든 스토리가 의존하는 공통 코드

- [X] T008 [P] Implement `StreamRequest` with validation (prompt 1~4000, events 1~100000, intervalMs 0~60000, persist) in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/StreamRequest.kt`
- [X] T009 [P] Implement `StreamEvent` types (message, done, error) and JSON payloads in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/StreamEvent.kt`
- [X] T010 [P] Implement `ErrorResponse` and common error codes in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/ErrorResponse.kt`
- [X] T011 Implement fixed-size payload generation and event sequence rules (seq, serverTs) in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/EventFactory.kt`
- [X] T012 [P] Implement heartbeat configuration properties (default 15s) in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/SseProperties.kt`
- [X] T013 [P] Implement active connection gauge and events-sent counter helper in `sse-compare/common/src/main/kotlin/jhkim105/ssecompare/common/SseMetrics.kt`
- [X] T014 Create app `Application.kt` entry points and base `application.yml` (actuator prometheus endpoint, ports 8081~8084 per `docker/monitoring/README.md`, pool and thread settings from research.md §7) in each of `sse-compare/app-*/src/main/`
- [X] T015 [P] Create `Combination` and `Scenario` JSON config in `sse-compare/results/combinations.json` and `sse-compare/loadgen/src/main/resources/scenarios.json`

**Checkpoint**: 공통 모델이 컴파일되고 각 앱이 빈 상태로 기동된다.

---

## Phase 3: User Story 1 - 동일 계약의 POST SSE 엔드포인트 (Priority: P1) 🎯 MVP

**Goal**: 6개 구성이 동일한 외부 계약을 제공한다.

**Independent Test**: 공통 계약 테스트가 A, B, C1, C2, D, E 모두에서 통과한다.

### Tests for User Story 1

- [X] T016 [US1] Implement abstract `ChatStreamContractSpec` (Kotest BehaviorSpec: event order and count, done event, interval tolerance (FR-010), 400 validation incl. out-of-range, `failAt` → `SIMULATED_ERROR` error event, heartbeat with shortened test interval, client disconnect cancellation and single decrement of active connections, GET baseline) in `sse-compare/common/src/testFixtures/kotlin/jhkim105/ssecompare/common/ChatStreamContractSpec.kt`

### Implementation for User Story 1

- [X] T017 [P] [US1] Implement A/B controller (incl. `failAt` support) using `SseEmitter` with executor, heartbeat, cancellation, `GET /stream` and `POST /chat/stream` in `sse-compare/app-mvc-sseemitter/src/main/kotlin/jhkim105/ssecompare/mvcsseemitter/ChatStreamController.kt`
- [X] T018 [P] [US1] Add `vt` profile (`spring.threads.virtual.enabled=true`) in `sse-compare/app-mvc-sseemitter/src/main/resources/application-vt.yml`
- [X] T019 [P] [US1] Implement C1/C2 controller (POST and `GET /stream`, `failAt` support) returning `Flow<ServerSentEvent>` with dispatcher selected by profile in `sse-compare/app-mvc-flow/src/main/kotlin/jhkim105/ssecompare/mvcflow/ChatStreamController.kt` and `DispatcherConfig.kt`
- [X] T020 [P] [US1] Add `vt` profile for the virtual thread dispatcher in `sse-compare/app-mvc-flow/src/main/resources/application-vt.yml`
- [X] T021 [P] [US1] Implement D controller (POST and `GET /stream`, `failAt` support) returning `Flux<ServerSentEvent>` in `sse-compare/app-webflux-flux/src/main/kotlin/jhkim105/ssecompare/webfluxflux/ChatStreamController.kt`
- [X] T022 [P] [US1] Implement E controller (POST and `GET /stream`, `failAt` support) with `suspend`/`Flow<ServerSentEvent>` in `sse-compare/app-webflux-flow/src/main/kotlin/jhkim105/ssecompare/webfluxflow/ChatStreamController.kt`
- [X] T023 [P] [US1] Implement common validation error handling (400 `{code, message}`) in `sse-compare/app-mvc-sseemitter/.../ErrorAdvice.kt`, `app-mvc-flow/.../ErrorAdvice.kt`, `app-webflux-flux/.../ErrorAdvice.kt`, `app-webflux-flow/.../ErrorAdvice.kt`
- [X] T024 [US1] Implement contract test subclasses with `@SpringBootTest(RANDOM_PORT)` for each app and profile variant in `sse-compare/app-*/src/test/kotlin/.../ChatStreamContractTest.kt`
- [X] T025 [US1] Fix contract discrepancies found by T024 across modules until all 6 configurations pass
- [X] T025a [US1] Add edge-case tests (disconnect during completion, out-of-range `events`/`intervalMs` → 400) to `sse-compare/common/src/testFixtures/kotlin/jhkim105/ssecompare/common/ChatStreamContractSpec.kt`

**Checkpoint**: `./gradlew test`에서 6개 구성의 계약 테스트가 통과한다. (SC-001)

---

## Phase 4: User Story 2 - 순수 스트리밍 성능 비교 (Priority: P1)

**Goal**: DB 없이 S1, S2 시나리오로 조합별 성능을 측정한다.

**Independent Test**: 구성 하나에 `run-benchmark.sh <combo> S1`을 실행하면 지표가 담긴 결과 파일이 생성된다.

### Implementation for User Story 2

- [X] T026 [P] [US2] Implement Dockerfile with module ARG and fixed JVM options (`-Xmx512m -XX:+UseG1GC`) in `sse-compare/infra/Dockerfile`
- [X] T027 [P] [US2] Verify Prometheus config in `docker/monitoring/prometheus/prometheus.yml` scrapes app ports (8081~8084) at `/actuator/prometheus` every 5s; set app ports and `management.metrics.tags.application` in each app `application.yml`
- [X] T028 [P] [US2] Verify Grafana provisioning and dashboard `docker/monitoring/grafana/dashboards/sse-compare.json` (threads, heap, GC, CPU, active connections, event rate, Hikari/R2DBC pool panels) renders with real app metrics; adjust metric names if needed
- [X] T029 [US2] Create `compose.yml` with mysql and app services (published ports 8081~8084) using `cpus: 2`, `memory: 1g` in `sse-compare/infra/compose.yml`; monitoring stack runs separately from `docker/monitoring/docker-compose.yml`
- [X] T030 [P] [US2] Implement loadgen request runner (coroutine per connection, POST SSE client per T007, per-event receive timestamps) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/SseClient.kt`
- [X] T031 [P] [US2] Implement metrics recorder (TTFB, event gap histogram p50/p95/p99, error rate, completion rate) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/MetricsRecorder.kt`
- [X] T032 [US2] Implement scenario S1 (stepped concurrent connections 100→1k→5k→10k with hold) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S1RampUp.kt`
- [X] T033 [US2] Implement scenario S2 (fixed arrival rate short streams) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S2ShortStreams.kt`
- [X] T034 [US2] Implement server metrics snapshot via Prometheus API and result JSON writer in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/ResultWriter.kt`
- [X] T035 [US2] Implement limit detection (error rate >1% or event gap p99 >2×intervalMs) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/LimitDetector.kt`
- [X] T036 [US2] Implement loadgen `Main.kt` CLI (combo, scenario, runs, warmup) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/Main.kt`
- [X] T037 [US2] Implement `run-benchmark.sh <combo> <scenario>` (start one app with profiles, warmup, run loadgen 3 times, stop, save to `results/<date>/`) in `sse-compare/scripts/run-benchmark.sh`
- [X] T038 [P] [US2] Write loadgen unit tests (MetricsRecorder percentiles, LimitDetector) in `sse-compare/loadgen/src/test/kotlin/.../`
- [X] T039 [P] [US2] Create k6 cross-check script for S1 in `sse-compare/infra/k6/s1.js`
- [ ] T040 [US2] Run S1 and S2 for A, B, C1, C2, D, E and store results in `sse-compare/results/`

**Checkpoint**: 6개 구성의 S1, S2 결과와 한계 연결 수가 확보된다. (SC-002, SC-003, SC-004, SC-007)

---

## Phase 5: User Story 3 - 블로킹 DB 저장 포함 성능 비교 (Priority: P2)

**Goal**: 시작/종료 시 DB 저장을 포함한 시나리오(S4)를 비교한다.

**Independent Test**: `db-jpa`/`db-r2dbc` 프로파일에서 스트림 요청 수만큼 `chat_message` 행이 저장된다.

### Tests for User Story 3

- [X] T041 [P] [US3] Write DB integration tests with MySQL Testcontainers (USER row at start, ASSISTANT row at end, no ASSISTANT on cancel) in `sse-compare/persistence-jpa/src/test/kotlin/.../ChatMessageJpaTest.kt` and `sse-compare/persistence-r2dbc/src/test/kotlin/.../ChatMessageR2dbcTest.kt`

### Implementation for User Story 3

- [X] T042 [P] [US3] Implement `ChatMessage` JPA entity, repository and blocking `ChatMessageStore` in `sse-compare/persistence-jpa/src/main/kotlin/.../`
- [X] T043 [P] [US3] Implement R2DBC `ChatMessage`, repository and suspend `ChatMessageStore` in `sse-compare/persistence-r2dbc/src/main/kotlin/.../`
- [X] T044 [P] [US3] Add MySQL schema init script (`chat_message`, index on `stream_id`) in `sse-compare/infra/mysql/init.sql` and mount it in the `mysql` service of `sse-compare/infra/compose.yml`
- [X] T045 [US3] Wire `db-jpa` profile into MVC apps (blocking save at start and end) in `sse-compare/app-mvc-sseemitter/` and `sse-compare/app-mvc-flow/`
- [X] T046 [US3] Wire WebFlux `db-jpa` (isolated via `Dispatchers.IO` / `boundedElastic`) and `db-r2dbc` profiles in `sse-compare/app-webflux-flux/` and `sse-compare/app-webflux-flow/`; resolve JPA and R2DBC auto-configuration conflicts per research.md §4
- [X] T047 [US3] Set identical pool sizes (Hikari/R2DBC = 10) in all DB profile configs and expose pool metrics
- [X] T048 [US3] Extend contract tests to cover `persist=true`, ignored `persist` without DB profile, start-save failure → `DB_ERROR`, pool timeout (5s) → `DB_TIMEOUT`, end-save failure not affecting the stream (counter `sse.persist.failures`) for DB profiles in `sse-compare/common/src/testFixtures/.../ChatStreamContractSpec.kt` and app tests
- [X] T049 [US3] Implement scenario S4 (S1/S2 with `persist=true`) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S4WithDb.kt`
- [ ] T050 [US3] Run S4 for all configurations (including WebFlux `db-jpa` vs `db-r2dbc`) and store results in `sse-compare/results/`

**Checkpoint**: DB 포함 결과와 DB 없는 결과 대비 저하율을 비교할 수 있다. (SC-005)

---

## Phase 6: User Story 5 - 결과 보고서와 추천 (Priority: P2)

**Goal**: 결과를 한 문서로 정리하고 워크로드별 추천을 제시한다.

**Independent Test**: `RESULTS.md`만 읽고 두 워크로드 유형별 추천을 근거와 함께 답할 수 있다.

- [X] T051 [US5] Implement result aggregator (JSON → CSV, comparison tables, degradation %, per-metric deviation across the 3 runs with warning when p95 latency or throughput deviates beyond ±15% per SC-004) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/report/Aggregator.kt`
- [X] T052 [US5] Add `--report` mode to `sse-compare/scripts/run-benchmark.sh` generating `sse-compare/results/RESULTS.md`
- [X] T053 [US5] Write report analysis: configuration table (pool sizes, thread settings, JVM options per FR-014), comparison tables, charts, limit connections, bottleneck explanations (thread pool, connection pool, event loop) in `sse-compare/results/RESULTS.md`
- [X] T054 [US5] Write workload-based recommendations (existing blocking service vs new I/O-centric service) with trade-offs in `sse-compare/results/RESULTS.md`
- [ ] T055 [P] [US5] Update `sse-compare/docs/research/mvc-flow-vs-webflux-flow.md` with measured results versus predictions

**Checkpoint**: 보고서가 SC-008을 충족한다.

---

## Phase 7: User Story 4 - 안정성·장애 시나리오 (Priority: P3)

**Goal**: 장시간 유지, 중도 이탈, 느린 클라이언트 비교.

**Independent Test**: S3, S5, S6 결과로 리소스 누수 여부와 복귀 상태를 확인한다.

- [X] T056 [P] [US4] Implement scenario S3 (fixed connections, long hold, memory/thread trend sampling) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S3LongHold.kt`
- [X] T057 [P] [US4] Implement scenario S5 (random mid-stream disconnects by `dropRatio`, verify active connections return to baseline) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S5Drop.kt`
- [X] T058 [P] [US4] Implement scenario S6 (slow reader with `readDelayMs`) in `sse-compare/loadgen/src/main/kotlin/jhkim105/ssecompare/loadgen/scenario/S6SlowClient.kt`
- [X] T059 [US4] Add baseline-recovery check (active connections, thread count) to `ResultWriter` for SC-006
- [ ] T060 [US4] Run S3, S5, S6 for all configurations and store results in `sse-compare/results/`
- [ ] T061 [US4] Add stability findings (leaks, backpressure behavior) to `sse-compare/results/RESULTS.md`

**Checkpoint**: 안정성 결과가 보고서에 포함된다. (SC-006)

---

## Phase 8: Polish & Cross-Cutting Concerns

- [X] T062 [P] Document proxy/load balancer SSE buffering caveat and local Docker interpretation limits in `sse-compare/README.md`
- [X] T063 [P] Add `ulimit`, OS tuning and troubleshooting notes in `sse-compare/docs/research/benchmark-environment.md`
- [X] T064 Run quickstart validation end-to-end per `specs/20261119-sse-streaming-comparison/quickstart.md` and fix gaps
- [X] T065 [P] Update root `GEMINI.md` project structure list with `sse-compare/` and `docker/monitoring`; update `docker/README.md` with the monitoring stack
- [ ] T066 Run `/speckit-converge` to verify remaining unbuilt work against spec, plan and tasks

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 즉시 시작. T006, T007 PoC 결과는 US1(T019), US2(T030)에 영향.
- **Foundational (Phase 2)**: Setup 이후. 모든 스토리를 막는다.
- **US1 (Phase 3)**: Foundational 이후. MVP.
- **US2 (Phase 4)**: US1 완료 후 (측정할 앱이 필요).
- **US3 (Phase 5)**: US1 완료 후. US2의 loadgen/infra(T026~T036)를 재사용하므로 US2와 부분 병렬 가능.
- **US5 (Phase 6)**: US2 결과 필요, S4 포함 시 US3 이후.
- **US4 (Phase 7)**: US2의 loadgen 기반. US3와 독립.
- **Polish (Phase 8)**: 원하는 스토리 완료 후.

### Story Dependencies

```text
Setup → Foundational → US1 → US2 → US5
                          ├→ US3 ─┘
                          └→ US4 (US2 loadgen 필요)
```

### Within Each Story

- 테스트 → 모델/공통 → 앱 구현 → 통합 → 측정 실행 순서.
- 한 번에 한 구성만 벤치마크를 실행한다 (간섭 방지).

### Parallel Opportunities

- Phase 1: T003, T004, T005, T007
- Phase 2: T008, T009, T010, T012, T013, T015
- US1: T017~T023은 모듈이 달라 병렬 가능
- US2: T026~T028, T030, T031, T038, T039
- US3: T041~T044
- US4: T056~T058

### Parallel Example: User Story 1

```text
T017 app-mvc-sseemitter 컨트롤러
T019 app-mvc-flow 컨트롤러
T021 app-webflux-flux 컨트롤러
T022 app-webflux-flow 컨트롤러
```

---

## Implementation Strategy

### MVP First (US1)

1. Phase 1~2 완료 (PoC 포함)
2. Phase 3: 6개 구성 계약 테스트 통과
3. **검증 후 중단 가능**: 구현 비교 데모 자체가 완성된다.

### Incremental Delivery

1. US1 → 동작하는 6개 구성 (데모 가능)
2. US2 → 순수 스트리밍 성능 비교 (핵심 가치)
3. US3 → DB 포함 비교
4. US5 → 보고서와 추천
5. US4 → 안정성 시나리오

### Notes

- 실제 측정은 로컬 Docker에서 상대 비교용으로 해석한다.
- 각 측정 실행 후 결과 파일을 커밋하기 전에 SC-004(±15% 재현성)를 확인한다.
- PoC에서 MVC `Flow` 지원이 기대와 다르면 T019와 plan의 C1/C2 설계를 먼저 갱신한다.
