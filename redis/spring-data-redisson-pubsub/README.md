# spring-data-redisson-pubsub

Redisson 클라이언트를 사용한 Redis Pub/Sub 예제입니다.
`Topic`과 `ShardedTopic`의 차이 및 중복 메시지 처리 방지 패턴을 보여줍니다.

## 개요

Redisson의 `RShardedTopic`을 사용하면 Redis Cluster 환경에서 샤딩된 채널로 Pub/Sub을 처리할 수 있습니다.
동일 메시지가 여러 리스너에 전달될 때 `ReentrantLock` + Caffeine 캐시로 중복 처리를 방지하는 패턴을 구현합니다.

## 아키텍처

```
Publisher → Redis Topic / ShardedTopic → Subscriber (중복 처리 방지 포함)
```

## 주요 구성 요소

### EventListenerConfig (구독 등록)

애플리케이션 시작 시 Topic과 ShardedTopic에 리스너를 등록합니다.

```kotlin
@Configuration
class EventListenerConfig(
    private val redissonClient: RedissonClient,
    private val subscriber: Subscriber
) {
    @EventListener
    fun onApplicationReadyEvent(applicationReadyEvent: ApplicationReadyEvent) {
        redissonClient.getTopic("LOCAL:TOPIC")
            .addListener(String::class.java, subscriber)

        redissonClient.getShardedTopic("LOCAL:STOPIC")
            .addListener(String::class.java, subscriber)
    }
}
```

### Subscriber (중복 처리 방지)

메시지 기반 키로 `ReentrantLock`을 만들고, Caffeine 캐시로 락을 관리하여 동일 메시지의 중복 처리를 방지합니다.

```kotlin
@Component
class Subscriber : MessageListener<String> {
    override fun onMessage(channel: CharSequence, message: String) {
        val key = "lock-${message.hashCode()}"
        val lock = lockCache.get(key) { ReentrantLock() }

        if (lock.tryLock()) {
            try {
                // 메시지 처리 로직
            } finally {
                lock.unlock()
            }
        } else {
            // 이미 처리 중 → 스킵
        }
    }

    companion object {
        // TTL 기반 락 캐시: 10분 미사용 시 자동 제거, 최대 10,000개
        private val lockCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build<String, ReentrantLock>()
    }
}
```

## Topic vs ShardedTopic 비교

| 구분 | `RTopic` | `RShardedTopic` |
|------|----------|----------------|
| 지원 환경 | Standalone, Cluster | Cluster 전용 |
| 메시지 전달 | 모든 클러스터 노드 | 특정 샤드 노드만 |
| 네트워크 효율 | 낮음 (브로드캐스트) | 높음 (샤드 한정) |
| 순서 보장 | X | 동일 샤드 내 O |

## 프로젝트 구조

```
src/
├── main/kotlin/.../
│   ├── EventListenerConfig.kt      # ApplicationReadyEvent 시 구독 등록
│   ├── Subscriber.kt               # 메시지 수신자 (중복 처리 방지 포함)
│   └── SpringRedissonPubsubApplication.kt
└── test/kotlin/...
    └── SpringRedissonPubsubApplicationTests.kt
```

## 실행 방법

```bash
# Redis 서버 실행
docker run -d -p 6379:6379 redis:latest

# 애플리케이션 실행
./gradlew bootRun
```

## 주요 학습 포인트

- Spring Data Redis Pub/Sub vs Redisson Pub/Sub 차이
- `RTopic` vs `RShardedTopic` — Cluster 환경에서의 선택 기준
- Caffeine 캐시 + `ReentrantLock`을 통한 분산 환경 중복 메시지 처리 방지
- `@EventListener(ApplicationReadyEvent)` 패턴으로 안전한 초기화 시점 선택
