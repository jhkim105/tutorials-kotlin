# Kafka Streams vs Kafka Consumer

이 프로젝트는 Kafka 메시지를 처리하는 두 가지 방식인 Kafka Consumer 와 Kafka Streams DSL 을 비교합니다.

## 구조

- `consumer-app`: KafkaListener 기반 수신 처리
- `streams-app`: Kafka Streams DSL 기반 처리
- `common/`: 공통 모델 정의
- `producer-app`: 체결 메시지 발행

## 실행 방법
#### 카프카 토픽 생성
```text
# stock-prices 토픽 생성
kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic stock-prices --replication-factor 1 --partitions 1
```

#### App 실행
```bash
./gradlew bootRun --project-dir consumer-app
./gradlew bootRun --project-dir streams-app
```

### 메시지 전송
```
kafka-console-producer --topic stock-prices --bootstrap-server localhost:9092
> {"symbol":"AAPL", "price":300.0}
```
