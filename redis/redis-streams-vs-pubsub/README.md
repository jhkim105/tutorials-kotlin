# redis-streams-vs-pubsub

Redis Streams와 Redis Pub/Sub을 **동일한 시나리오**로 직접 비교하는 데모 모듈입니다.

## 개요

Pub/Sub은 간단한 실시간 알림에 적합하지만 다음과 같은 한계를 가집니다.  
이 모듈은 REST API와 통합 테스트를 통해 두 방식의 차이를 체감할 수 있도록 구성되었습니다.

## 4가지 비교 시나리오

| 시나리오 | Pub/Sub | Streams |
|----------|---------|---------||
| **1. Consumer 오프라인** | ❌ 메시지 유실 | ✅ 스트림에 보존, 나중에 수신 |
| **2. 처리 실패 재처리** | ❌ ACK 없음 → 영구 유실 | ✅ PENDING → XCLAIM 재처리 |
| **3. 메시지 재생(Replay)** | ❌ 과거 메시지 수신 불가 | ✅ `ReadOffset.from(id)` 로 재생 |
| **4. 분산 처리** | ❌ 브로드캐스트 (중복 처리) | ✅ Consumer Group (중복 없음) |

## 프로젝트 구조

```
src/
├── main/kotlin/jhkim105/tutorials/redis/
│   ├── StreamsVsPubSubApplication.kt
│   ├── pubsub/
│   │   ├── PubSubConfig.kt         # ChannelTopic + RedisMessageListenerContainer 설정
│   │   ├── PubSubController.kt     # POST /pubsub/publish, GET /pubsub/subscribe (SSE)
│   │   ├── PubSubPublisher.kt      # convertAndSend() 로 메시지 발행
│   │   └── PubSubSubscriber.kt     # MessageListener + SSE Emitter 실시간 전송
│   └── streams/
│       ├── StreamPublisher.kt      # opsForStream().add() 로 스트림 발행
│       └── StreamsController.kt    # POST /streams/produce, GET /streams/consume, POST /streams/ack
└── test/kotlin/jhkim105/tutorials/redis/
    └── StreamsVsPubSubComparisonTest.kt  # 4가지 시나리오 비교 테스트
```

## REST API

### Pub/Sub — 실시간 알림 컨셉

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/pubsub/publish` | 채널에 메시지 발행 (연결된 SSE 구독자에게 즉시 전달) |
| `GET` | `/pubsub/subscribe` | SSE로 실시간 메시지 수신 대기 (`text/event-stream`) |

### Streams — 작업 큐 / 이력 저장 컨셉

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/streams/produce` | 스트림에 메시지 추가 (XADD) |
| `GET` | `/streams/consume` | Consumer Group으로 메시지 읽기 → PENDING 전환 |
| `POST` | `/streams/ack` | 메시지 처리 완료 신호 (XACK) — PENDING 제거 |

#### GET /streams/consume — `fromId` 파라미터

| fromId | 설명 |
|--------|------|
| `>` (기본값) | 아직 어떤 Consumer에도 전달되지 않은 **새 메시지**만 읽기 |
| `0` | 이 Consumer의 **PENDING 메시지** 재조회 (crash 후 재시작 시 활용) |
| `0-0` | 스트림 **처음부터 전체 Replay** |
| `1234567-0` | 특정 ID 이후부터 Replay |

## 아키텍처

```
【Pub/Sub】
POST /pubsub/publish → PubSubPublisher → Redis Channel → PubSubSubscriber → SSE → GET /pubsub/subscribe

【Streams】
POST /streams/produce → StreamPublisher → Redis Stream (vs:stream:channel)
                                               └── Consumer Group (api-group)
GET /streams/consume ─────────────────────────────── api-consumer (PENDING)
POST /streams/ack ─────────────────────────────────── XACK (PENDING 제거)
```

## 실행 방법

```bash
# Redis 서버 실행
cd ../docker/docker-redis && docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

## API 실행 (http/api.http)

IntelliJ에서 `http/api.http` 파일을 열어 단계별로 실행합니다.

```
[Pub/Sub 흐름]
1. GET  /pubsub/subscribe   → SSE 연결 (탭 유지)
2. POST /pubsub/publish     → SSE 탭에서 실시간 수신 확인

[Streams 흐름]
1. POST /streams/produce    → messageId 확인
2. GET  /streams/consume    → PENDING 전환 확인
3. POST /streams/ack        → messageId 입력 → PENDING 0 확인
4. GET  /streams/consume?fromId=0-0  → 처음부터 Replay
```

## 테스트 실행

```bash
# 비교 테스트 전체 실행
./gradlew test

# 특정 시나리오만 실행
./gradlew test --tests "*.StreamsVsPubSubComparisonTest"
```

## 핵심 테스트 케이스

### 시나리오 1 — 영속성 (Persistence)
```
Pub/Sub: 구독자 없을 때 발행 → 수신 0개 (유실)
Streams: 발행 후 ReadOffset.fromStart()으로 읽기 → 전부 수신
```

### 시나리오 2 — 재처리 (Retry)
```
Pub/Sub: 처리 실패 → 재처리 불가, 영구 유실
Streams: ACK 없음 → PENDING → XCLAIM으로 다른 Consumer가 인수 → 재처리
```

### 시나리오 3 — 재생 (Replay)
```
Pub/Sub: 재구독 후 과거 메시지 수신 불가
Streams: ReadOffset.from(messageId) → 특정 시점 이후 재생
         ReadOffset.fromStart()     → 전체 히스토리 재생
```

### 시나리오 4 — 분산 처리 (Consumer Group)
```
Pub/Sub: 구독자 N개 → 모두 동일 메시지 수신 (N배 중복 처리)
Streams: Consumer Group → 메시지당 정확히 하나의 Consumer만 처리
```

## 관련 모듈

- [redis-streams](../redis-streams) — Streams 장점별 단위 테스트
- [spring-data-redis-pubsub](../spring-data-redis-pubsub) — Pub/Sub 기본 구현
- [spring-data-redisson-pubsub](../spring-data-redisson-pubsub) — Redisson Pub/Sub + 중복 방지
- [docker/docker-redis](../docker/docker-redis) — Redis 단일 서버 Docker Compose

## 주요 학습 포인트

- Pub/Sub은 **실시간 알림**, Streams는 **신뢰성 있는 이벤트 처리**에 적합
- `XADD` / `XREADGROUP` / `XACK` / `XCLAIM` / `XPENDING` 명령어의 실제 동작
- Consumer Group을 이용한 수평 확장(Scale-out) 패턴
- SSE(Server-Sent Events)를 통한 실시간 Pub/Sub 메시지 수신
- Event Sourcing에서 과거 이벤트 재생이 필요한 이유
