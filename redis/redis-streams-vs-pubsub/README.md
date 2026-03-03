# redis-streams-vs-pubsub

Redis Streams와 Redis Pub/Sub을 **동일한 시나리오**로 직접 비교하는 데모 모듈입니다.

## 개요

Pub/Sub은 간단한 실시간 알림에 적합하지만, 다음과 같은 한계를 가집니다.  
이 모듈은 각 시나리오에서 두 방식을 나란히 실행하여 Streams의 장점을 체감할 수 있도록 구성되었습니다.

## 4가지 비교 시나리오

| 시나리오 | Pub/Sub | Streams |
|----------|---------|---------|
| **1. Consumer 오프라인** | ❌ 메시지 유실 | ✅ 스트림에 보존, 나중에 수신 |
| **2. 처리 실패 재처리** | ❌ ACK 없음 → 영구 유실 | ✅ PENDING → XCLAIM 으로 재처리 |
| **3. 메시지 재생(Replay)** | ❌ 과거 메시지 수신 불가 | ✅ `ReadOffset.from(id)` 로 재생 |
| **4. 분산 처리** | ❌ 브로드캐스트 (중복 처리) | ✅ Consumer Group (중복 없음) |

## 프로젝트 구조

```
src/
├── main/kotlin/jhkim105/tutorials/redis/
│   ├── StreamsVsPubSubApplication.kt
│   ├── pubsub/
│   │   ├── PubSubConfig.kt         # ChannelTopic + RedisMessageListenerContainer 설정
│   │   ├── PubSubPublisher.kt      # convertAndSend() 로 메시지 발행
│   │   └── PubSubSubscriber.kt     # MessageListener 구현 + 수신 메시지 저장
│   └── streams/
│       └── StreamPublisher.kt      # opsForStream().add() 로 스트림 발행
└── test/kotlin/jhkim105/tutorials/redis/
    └── StreamsVsPubSubComparisonTest.kt  # 4가지 시나리오 비교 테스트
```

## 아키텍처

```
【Pub/Sub 경로】
PubSubPublisher → Redis Channel (vs:pubsub:channel) → PubSubSubscriber

【Streams 경로】
StreamPublisher → Redis Stream (vs:stream:channel) → Consumer Group (vs-group)
                                                         ├── consumer-1
                                                         └── consumer-2
```

## 테스트 실행

```bash
# Redis 서버 실행 (필수)
docker run -d -p 6379:6379 redis:latest

# 비교 테스트 전체 실행
./gradlew test

# 특정 시나리오만 실행
./gradlew test --tests "*.StreamsVsPubSubComparisonTest"
```

## 핵심 테스트 케이스 설명

### 시나리오 1 — 영속성 (Persistence)

```
Pub/Sub: 구독자 없을 때 발행 → 수신 0개 (유실)
Streams: 발행 후 Consumer가 ReadOffset.fromStart()으로 읽기 → 전부 수신
```

### 시나리오 2 — 재처리 (Retry)

```
Pub/Sub: 처리 실패 → 재처리 불가, 영구 유실
Streams: ACK 없음 → PENDING 상태 유지 → XCLAIM으로 다른 Consumer가 인수 → 재처리
```

### 시나리오 3 — 재생 (Replay)

```
Pub/Sub: 재구독 후 과거 메시지 수신 불가
Streams: ReadOffset.from(messageId) → 특정 시점 이후 메시지 재생
         ReadOffset.fromStart()     → 전체 히스토리 재생
```

### 시나리오 4 — 분산 처리 (Consumer Group)

```
Pub/Sub: 구독자 N개 → 모두 동일 메시지 수신 (N배 중복 처리)
Streams: Consumer Group → 메시지당 정확히 하나의 Consumer만 처리
```

## 관련 모듈

- [redis-streams](../redis-streams) — Streams 장점별 단위 테스트 (영속성/재처리/재생/ConsumerGroup)
- [spring-data-redis-pubsub](../spring-data-redis-pubsub) — Spring Data Redis Pub/Sub 기본 구현
- [spring-data-redisson-pubsub](../spring-data-redisson-pubsub) — Redisson Pub/Sub + 중복 처리 방지 패턴

## 주요 학습 포인트

- Pub/Sub은 **실시간 알림**, Streams는 **신뢰성 있는 이벤트 처리**에 적합
- `XACK` / `XCLAIM` / `PENDING` 명령어의 실제 동작 방식
- Consumer Group을 이용한 수평 확장(Scale-out) 패턴
- Event Sourcing에서 과거 이벤트 재생이 필요한 이유
