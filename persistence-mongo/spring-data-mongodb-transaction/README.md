# Spring Data MongoDB Transaction Demo

이 프로젝트는 Spring Boot와 Kotlin을 사용하여 MongoDB의 **Multi-Document Transaction**을 시연합니다.
가장 일반적인 분산 시스템 패턴 중 하나인 **Outbox Pattern**을 구현하여 트랜잭션의 원자성(Atomicity)을 검증합니다.

## MongoDB Transaction 개요

MongoDB 4.0부터 단일 클러스터 내에서 **Multi-Document ACID Transaction**을 지원합니다.
이는 RDBMS의 트랜잭션과 매우 유사하게 동작하며, 여러 문서나 컬렉션에 대한 변경 사항을 한 번에 커밋하거나 롤백할 수 있습니다.

### 주요 특징
- **ACID 보장**: Atomicity(원자성), Consistency(일관성), Isolation(고립성), Durability(지속성)를 보장합니다.
- **Replica Set 필수**: 트랜잭션을 사용하려면 MongoDB가 **Replica Set**으로 구성되어 있어야 합니다. (Standalone 모드에서는 지원하지 않음)
- **Spring Data 지원**: `MongoTransactionManager`를 통해 `@Transactional` 어노테이션으로 손쉽게 제어할 수 있습니다.

## 프로젝트 구조 (Outbox Pattern)

이 데모는 "주문 생성" 이라는 비즈니스 로직을 시뮬레이션합니다. 하나의 트랜잭션 안에서 다음 두 가지 작업이 수행됩니다.

1.  **Order 저장**: `orders` 컬렉션에 주문 정보 저장
2.  **Outbox 저장**: `outbox` 컬렉션에 이벤트 발행 정보 저장

트랜잭션이 보장되므로, 만약 Outbox 저장 중 예외가 발생하면 주문 정보도 함께 **롤백**되어 데이터 정합성을 유지합니다.

## 사전 요구사항 (Prerequisites)

이 프로젝트를 실행하기 위해 로컬에 MongoDB Replica Set이 실행 중이어야 합니다.
상위 디렉토리의 `docker-mongodb-cluster`를 실행하세요.

```bash
cd ../docker-mongodb-cluster
docker compose up -d
```

## 실행 방법

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### 테스트 실행
```bash
./gradlew clean test
```

## API 사용법

### 1. 주문 생성 (성공 케이스)
정상적으로 주문과 Outbox 이벤트가 생성됩니다.

- **Request**
    ```http
    POST /orders?name=my-order
    ```

### 2. 주문 생성 실패 (롤백 테스트)
`fail=true` 파라미터를 전달하면 Order 저장 후 의도적인 예외가 발생합니다. 트랜잭션 롤백으로 인해 Order 데이터가 남지 않아야 합니다.

- **Request**
    ```http
    POST /orders?name=fail-order&fail=true
    ```

## 테스트 도구
`http/tests.http` 파일을 사용하여 IntelliJ IDEA의 HTTP Client로 쉽게 테스트해볼 수 있습니다.
