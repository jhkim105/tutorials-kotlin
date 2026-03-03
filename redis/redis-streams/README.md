# redis-streams

Redis Streams를 활용한 메시지 큐(Message Queue) 예제입니다.

## 개요

Redis Streams는 Kafka와 유사한 로그 기반 메시지 스트림 자료구조입니다.
Consumer Group을 사용하여 여러 Consumer가 메시지를 분산 처리하고, 처리 확인(ACK)을 통해 메시지 손실을 방지합니다.

## 아키텍처

```
HTTP 요청 → MessageController
                ↓
        RedisMessageProducer  →  Redis Stream (stream:demo)
                                        ↓
                                 Consumer Group (group1)
                                        ↓
                                RedisMessageConsumer
                                        ↓
                                  MessageService  →  MariaDB
```

## 주요 구성 요소

### RedisMessageProducer (메시지 발행)

```kotlin
@Service
class RedisMessageProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    fun sendToStream(streamKey: String, message: String) {
        val map = objectMapper.readValue<Map<String, String>>(message)
        val record = MapRecord.create(streamKey, map)
        redisTemplate.opsForStream<String, String>().add(record)
    }
}
```

### RedisMessageConsumer (메시지 소비)

```kotlin
@Component
class RedisMessageConsumer(
    private val messageService: MessageService,
    private val objectMapper: ObjectMapper
) : StreamListener<String, MapRecord<String, String, String>> {

    override fun onMessage(message: MapRecord<String, String, String>) {
        val jsonString = objectMapper.writeValueAsString(message.value)
        val sampleMessage = objectMapper.readValue(jsonString, SampleMessage::class.java)
        messageService.saveMessage(sampleMessage.id, sampleMessage.createdAt)
    }
}
```

### RedisStreamConfig (Consumer Group 설정)

```kotlin
@Configuration
class RedisStreamConfig {

    @Bean
    fun startConsumer(...): StreamMessageListenerContainer<...> {
        // Consumer Group 생성 (없으면 자동 생성)
        commands.xGroupCreate(streamKey, groupName, ReadOffset.latest(), true)

        // 자동 ACK 수신 설정
        container.receiveAutoAck(
            Consumer.from(groupName, "consumer-1"),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            listener
        )
        container.start()
    }
}
```

## Redis Streams vs Pub/Sub 비교

| 구분 | Redis Streams | Redis Pub/Sub |
|------|--------------|---------------|
| 메시지 저장 | O (Log 구조) | X (발행 즉시 소멸) |
| Consumer Group | O | X |
| 메시지 재처리 | O | X |
| 순서 보장 | O | X |
| 용도 | 신뢰성 있는 이벤트 처리 | 실시간 브로드캐스트 |

## 프로젝트 구조

```
src/
├── main/kotlin/.../
│   ├── MessageController.kt          # GET /messages/publish → 스트림에 메시지 발행
│   ├── MessageService.kt             # 메시지 DB 저장 서비스
│   ├── config/
│   │   └── RedisStreamConfig.kt     # StreamListenerContainer + ConsumerGroup 설정
│   ├── persistence/
│   │   ├── IdGenerator.kt           # TSID 기반 ID 생성
│   │   ├── MessageEntity.kt         # JPA 엔티티
│   │   └── MessageJpaRepository.kt
│   └── streams/
│       ├── RedisMessageProducer.kt  # Stream 발행자
│       └── RedisMessageConsumer.kt  # Stream 소비자
└── test/kotlin/.../
    ├── RedisStreamsApplicationTests.kt
    └── streams/
        ├── StreamPersistenceTest.kt  # 장점 #1: 메시지 영속성 검증
        ├── ConsumerGroupTest.kt      # 장점 #2: Consumer Group 분산 처리 검증
        ├── StreamRetryTest.kt        # 장점 #3: PENDING/XCLAIM 재처리 검증
        └── StreamReplayTest.kt       # 장점 #4: 특정 시점 메시지 재생 검증
```

## REST API

```http
### 메시지 발행 (스트림에 전송)
GET http://localhost:8080/messages/publish
```

## 실행 방법

```bash
# Redis 실행
docker run -d -p 6379:6379 redis:latest

# MariaDB 실행
docker run -d -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=testdb \
  mariadb:latest

# 애플리케이션 실행
./gradlew bootRun
```

## 주요 학습 포인트

- Redis Streams의 `XADD` / `XREAD` / `XACK` 명령어 동작 원리
- `StreamMessageListenerContainer`로 Consumer Group 기반 자동 소비 설정
- `receiveAutoAck()` vs `receive()` — 자동/수동 ACK 차이
- Stream 메시지가 DB에 저장되는 파이프라인 구조
- Pub/Sub과 달리 Consumer가 없어도 메시지가 보존되는 특성