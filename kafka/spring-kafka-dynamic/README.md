

## 리스너 동적 제어

### 이미 선언된 @KafkaListener 를 start/stop
자동 시작 안되게 autoStartup = "false"
```kotlin
@KafkaListener(
    id = "orders-listener",
    topics = ["orders"],
    groupId = "order-consumers",
    autoStartup = "false",            // 앱이 켜져도 자동 시작 안 함
    concurrency = "3"
)
fun onMessage(@Payload payload: String) {
    // 처리 로직
}
```

### 리스너를 런타임에 새로 만들기
DynamicContainerManager

#### 

## 사용 예
```shell
# @KafkaListener 컨테이너 제어
curl -X POST http://localhost:8080/api/kafka/listeners/orders-listener/start
curl -X POST http://localhost:8080/api/kafka/listeners/orders-listener/pause
curl -X POST http://localhost:8080/api/kafka/listeners/orders-listener/resume
curl -X POST http://localhost:8080/api/kafka/listeners/orders-listener/concurrency \
     -H "Content-Type: application/json" -d '{"concurrency":3}'
curl -X POST http://localhost:8080/api/kafka/listeners/orders-listener/stop

# 동적 컨테이너 생성/제어
curl -X POST http://localhost:8080/api/kafka/dynamic \
     -H "Content-Type: application/json" \
     -d '{"id":"dyn-A","topics":["topic.A","topic.B"],"concurrency":2}'

curl -X POST http://localhost:8080/api/kafka/dynamic/dyn-A/pause
curl -X POST http://localhost:8080/api/kafka/dynamic/dyn-A/resume
curl -X POST http://localhost:8080/api/kafka/dynamic/dyn-A/concurrency \
     -H "Content-Type: application/json" -d '{"concurrency":4}'
curl -X DELETE http://localhost:8080/api/kafka/dynamic/dyn-A
```