# Phase 1 Data Model

## StreamRequest (요청)

| 필드 | 타입 | 규칙 |
|------|------|------|
| `prompt` | String | 필수, 1~4,000자 |
| `events` | Int | 필수, 1~100,000 |
| `intervalMs` | Long | 필수, 0~60,000 |
| `persist` | Boolean | 선택, 기본 `false`. 요청 단위 DB 저장 on/off. 앱이 `db-*` 프로파일일 때만 유효하며 아니면 무시 |
| `failAt` | Int | 선택. 지정하면 해당 순번(1-based) 이벤트 직전에 오류를 유발한다. 1~`events` 범위만 허용. 테스트·벤치마크 전용 |

검증 실패는 스트림 시작 전에 `400`(`INVALID_REQUEST`)으로 거부한다. 범위 밖 값(`events` 0 또는 100,000 초과 등)도 동일하다. (`events`/`intervalMs` 범위는 안전 한도이며 plan 단계의 가정이다.)

## StreamEvent (응답 이벤트)

| 이벤트 이름 | data | 설명 |
|-------------|------|------|
| `message` | `{seq, content, serverTs}` | `seq`는 1부터 `events`까지. `content`는 고정 크기 페이로드(기본 64바이트) |
| `done` | `{total, serverTs}` | 마지막 이벤트. 이후 스트림 종료 |
| `error` | `{code, message}` | 처리 중 오류. 이후 스트림 종료. 코드: `SIMULATED_ERROR`(`failAt`), `DB_ERROR`, `DB_TIMEOUT`, `INTERNAL_ERROR` |
| (주석) | `: ping` | heartbeat. 이벤트가 아닌 SSE 주석 행 |

`serverTs`는 서버 전송 시각(epoch ms)이다. 부하기가 수신 시각과 비교해 전달 지연을 계산한다(같은 호스트 시계 기준).

### 상태 전이

```text
Requested → (validation ok) → Streaming → Done
                │                  ├────→ Error  (error 이벤트 후 종료)
                │                  └────→ Cancelled (클라이언트 끊김, 작업 중단)
                └→ Rejected (400, 스트림 시작 전)
```

## ChatMessage (DB: `chat_message`)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `stream_id` | VARCHAR(36) | 스트림 식별자(UUID). 요청/응답 행을 묶음 |
| `role` | VARCHAR(10) | `USER` 또는 `ASSISTANT` |
| `content` | TEXT | 요청 prompt 또는 응답 전체(이벤트 content 연결) |
| `created_at` | DATETIME(6) | 저장 시각 |

저장 시점: 스트림 시작 시 `USER` 1건, 정상 종료(`done` 전송 후) `ASSISTANT` 1건. 중도 취소/오류 시 `ASSISTANT` 행은 저장하지 않는다. 시작 저장 실패는 `DB_ERROR`, 풀 획득 타임아웃(5초)은 `DB_TIMEOUT` 오류 이벤트로 종료한다. 종료 시 저장 실패는 스트림 결과에 영향 없이 로그와 `sse.persist.failures` 카운터로만 기록한다. 인덱스: `stream_id`.

## Combination (비교 구성, 메타데이터)

| 필드 | 값 예 |
|------|-------|
| `id` | `A`, `B`, `C1`, `C2`, `D`, `E` |
| `module` | `app-mvc-sseemitter` 등 |
| `profiles` | `vt`, `db-jpa`, `db-r2dbc` |
| `stack` | `TOMCAT` / `NETTY` |
| `concurrency` | `PLATFORM` / `VIRTUAL` / `COROUTINE` / `REACTOR` |
| `returnType` | `SseEmitter` / `Flow` / `Flux` |

실행 스크립트와 보고서 생성기가 공유하는 설정 파일(`results/combinations.json`)로 관리한다.

## Scenario (부하 시나리오)

| 필드 | 설명 |
|------|------|
| `id` | `S1`~`S6` |
| `connections` / `arrivalRate` | 동시 연결 수(단계 목록) 또는 초당 도착률 |
| `events`, `intervalMs` | 요청 파라미터 |
| `holdSeconds` | 단계별 유지 시간 |
| `persist` | DB 저장 여부 |
| `dropRatio` | S5 중도 이탈 비율 |
| `readDelayMs` | S6 읽기 지연 |

## BenchmarkResult (결과)

| 필드 | 설명 |
|------|------|
| `combination`, `scenario`, `run`, `stage` | 식별자 |
| 클라이언트 | `ttfbMs{p50,p95,p99}`, `eventGapMs{p50,p95,p99}`, `errorRate`, `throughputEventsPerSec`, `completionRate` |
| 서버 (Prometheus 스냅샷) | `threads`, `cpuPct`, `heapMb`, `rssMb`, `gcPauseMs`, `poolActive/Pending` |
| 판정 | `limitReached`(에러율 1% 초과 또는 이벤트 간 지연 p99 > 2×`intervalMs`) |

저장 형식: `results/<date>/<combination>-<scenario>-run<N>.json`, 집계는 CSV.
