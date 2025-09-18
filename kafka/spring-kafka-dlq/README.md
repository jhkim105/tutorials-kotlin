# Spring Boot + Kotlin + Kafka DLQ (DLT) Example

이 프로젝트는 **Spring Boot 3.5 + Kotlin + Spring for Apache Kafka** 기반의  
간단한 **DLQ(Dead Letter Queue, Dead Letter Topic)** 데모 예제입니다.

Kafka 메시지를 소비하는 과정에서 예외가 발생하면,  
재시도(`@RetryableTopic`) 후에도 실패한 메시지를 자동으로 **DLT** 토픽(`demo.topic-dlt`)에 전송합니다.

---

## Features
- Spring Boot 3.5.x, Kotlin 2.x
- Spring Kafka + `@RetryableTopic` 활용
- 실패 메시지 자동 DLT 전송
- 정상 메시지/실패 메시지 각각 로그로 확인 가능
- REST API로 메시지 프로듀싱

---

## Requirements
- JDK 17+
- Docker (Kafka 실행용)
- Gradle 8+

---

## Usage
### 메시지 전송(정상 처리)
```shell
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"id":"1","payload":"hello"}'

```

### 메시지 전송(실패 -> DLT 이동)
```shell
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"id":"2","payload":"boom"}'

```

## Refs
- Spring Kafka Docs: https://docs.spring.io/spring-kafka/reference/
- @RetryableTopic: https://docs.spring.io/spring-kafka/reference/html/#retry-topic
- Dead Letter Topic: 실패 메시지를 별도 토픽에 저장하여, 이후 재처리/분석 가능


