# spring-data-redis-pubsub

Spring Data Redis의 Pub/Sub 기능을 활용한 메시징 패턴 예제입니다.

## 개요

Redis의 채널(Channel) 기반 Pub/Sub 모델을 Spring Data Redis로 구현합니다.
메시지를 발행(Publish)하는 Publisher와 수신(Subscribe)하는 Subscriber 구현 방법을 보여줍니다.

## 아키텍처

```
Publisher → Redis Channel (pubsub:queue) → Subscriber
```

## 주요 구성 요소

### RedisConfig (설정)

```kotlin
@Configuration
class RedisConfig {
    @Bean
    fun topic(): ChannelTopic = ChannelTopic("pubsub:queue")  // 채널 이름

    @Bean
    fun messageListener(): MessageListenerAdapter =
        MessageListenerAdapter(RedisMessageSubscriber())

    @Bean
    fun messageListenerContainer(connectionFactory: RedisConnectionFactory) =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(messageListener(), topic())  // 채널에 리스너 등록
        }
}
```

### RedisMessagePublisher

```kotlin
@Service
class RedisMessagePublisher(
    private val redisTemplate: RedisTemplate<String, String>,
    private val topic: ChannelTopic
) {
    fun publish(message: String) {
        redisTemplate.convertAndSend(topic.topic, message)
    }
}
```

### RedisMessageSubscriber

```kotlin
class RedisMessageSubscriber : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        println("Message received: ${String(message.body)}")
    }
}
```

## 프로젝트 구조

```
src/
├── main/kotlin/.../pubsub/
│   ├── RedisConfig.kt              # 채널 및 리스너 컨테이너 설정
│   ├── RedisMessagePublisher.kt    # 메시지 발행자
│   ├── RedisMessageSubscriber.kt   # 메시지 수신자
│   └── MessageController.kt       # 메시지 발행 REST API
└── test/kotlin/...
    └── SpringDataRedisPubsubApplicationTests.kt
```

## REST API

```http
### 메시지 발행
POST http://localhost:8080/messages
Content-Type: text/plain

Hello, Redis Pub/Sub!
```

## 실행 방법

```bash
# Redis 서버 실행
docker run -d -p 6379:6379 redis:latest

# 애플리케이션 실행
./gradlew bootRun
```

## 주요 학습 포인트

- `RedisMessageListenerContainer`를 통한 리스너 등록 방식
- `ChannelTopic` vs `PatternTopic` 차이
- Pub/Sub 메시징의 비용: 메시지는 저장되지 않으며, 구독자가 없으면 유실됨
- Redisson Pub/Sub과 비교하려면 [spring-data-redisson-pubsub](../spring-data-redisson-pubsub) 참고
