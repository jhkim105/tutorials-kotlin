# Spring Routing DataSource Tutorial

Spring Boot 3.x와 Kotlin을 기반으로 Master-Slave (Write/Read Replica) 환경에서 다중 DataSource를 구성하고, 트랜잭션 속성(`readOnly`) 및 부하 분산(Round-Robin)을 지원하는 라우팅 데이터소스 모듈입니다.

---

## 1. 아키텍처 개요

```text
[Client Request]
       │
       ▼
[@Transactional] ── (readOnly = false) ──► Master DataSource (Write DB Pool)
       │
       └── (readOnly = true)  ──► Slave Load Balancer (Round-Robin)
                                        │
                                        ├──► Slave1 DataSource (Read DB Pool 1)
                                        └──► Slave2 DataSource (Read DB Pool 2)
```

### 핵심 구성 요소
1. **`RoutingDataSource` (extends `AbstractRoutingDataSource`)**:
   - `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`를 검사하여 `true`이면 Slave 풀, `false`이면 Master 풀로 라우팅합니다.
2. **`SlaveLoadBalancer` (Round-Robin)**:
   - `AtomicInteger` 기반으로 다중 Slave DataSource 간에 균등하게 부하를 분산합니다.
3. **`LazyConnectionDataSourceProxy`**:
   - Spring의 기본 동작은 트랜잭션 진입 시 커넥션을 먼저 획득하지만, 프록시를 통해 실제 쿼리 실행 직전까지 커넥션 획득을 지연시켜 `readOnly` 속성이 바인딩된 후 정확한 대상(Master/Slave)의 커넥션을 획득하도록 보장합니다.
4. **독립된 HikariCP 커넥션 풀**:
   - Master와 각각의 Slave가 독립적인 커넥션 풀을 가지므로, 한쪽의 풀 고갈이 다른 쪽에 영향을 주지 않도록 격리됩니다.

---

## 2. 모듈 디렉토리 구조

```text
persistence/spring-routing-datasource/
├── build.gradle.kts
├── docker-compose.yml                         # Master(3306), Slave1(3307), Slave2(3308) MySQL 클러스터
├── run-mysql.sh                               # MySQL Docker 환경 실행 (종료 시 컨테이너 자동 정리)
├── run-server.sh                              # Spring Boot 서버 실행 (종료 시 프로세스 자동 정리)
├── README.md
├── docker/
│   └── init.sql                               # Docker 컨테이너 초기 스키마
├── docs/
│   └── slave-scale-out-architecture-review.md # Slave Scale-Out 리서치 문서
├── http/
│   └── api.http                               # HTTP 테스트 요청 모음
└── src
    ├── main
    │   ├── kotlin/jhkim105/tutorials/routing/
    │   │   ├── config/
    │   │   │   ├── DataSourceConfiguration.kt # HikariCP 및 RoutingDataSource, LazyProxy 빈 등록
    │   │   │   ├── DataSourceProperties.kt    # Master/Slave 프로퍼티 바인딩
    │   │   │   └── RoutingDataSource.kt       # Master/Slave 라우팅 & 다중 Slave 라운드로빈 로드밸런서
    │   │   ├── controller/
    │   │   │   └── RoutingDemoController.kt   # Master/Slave 접속 확인 및 CRUD REST API
    │   │   ├── domain/
    │   │   │   ├── Member.kt                  # JPA 엔티티
    │   │   │   └── MemberRepository.kt        # Spring Data JPA 리포지토리
    │   │   ├── service/
    │   │   │   └── MemberService.kt           # Write / Read 트랜잭션 서비스
    │   │   └── SpringRoutingDataSourceApplication.kt
    │   └── resources/
    │       └── application.yml
    └── test
        ├── kotlin/jhkim105/tutorials/routing/
        │   ├── DataSourceRoutingTest.kt       # 단일 트랜잭션별 Master vs Slave 라우팅 검증
        │   ├── DataSourceConcurrencyLoadTest.kt # 동시성/부하 시 Master/Slave 풀 분산 및 반납 검증
        │   └── TestDatabaseInitializer.kt     # 테스트 환경 스키마 초기화
        └── resources/
            └── application-test.yml
```

---

## 3. 실행 방법

### ① 터미널 1: MySQL Docker 환경 실행 (`run-mysql.sh`)
기존 충돌 포트(3306, 3307, 3308)를 사용하는 컨테이너를 자동으로 정리하고, Master/Slave MySQL 3대를 실행합니다. 터미널에서 `Ctrl+C`로 종료 시 실행했던 Docker 컨테이너들을 자동으로 `docker compose down`하여 정리합니다.

```bash
cd persistence/spring-routing-datasource
./run-mysql.sh
```

### ② 터미널 2: Spring Boot 서버 실행 (`run-server.sh`)
Spring Boot 서버를 기동합니다. 터미널에서 `Ctrl+C`로 종료 시 실행된 프로세스를 깨끗하게 종료합니다.

```bash
cd persistence/spring-routing-datasource
./run-server.sh
```

---

## 4. HTTP API 및 curl 테스트 예시

[`http/api.http`](http/api.http) 파일을 사용하거나 아래의 `curl` 명령어를 통해 각 엔드포인트의 라우팅 동작을 즉시 검증할 수 있습니다.

### 4.1 Write Routing 검증 (Master 접속 확인)
쓰기 트랜잭션(`@Transactional`)이 Master 데이터베이스(포트 `3306`)로 라우팅되는지 확인합니다.

```bash
curl -X GET http://localhost:8080/api/routing/write-check
```
* **응답 예시**:
```json
{
  "success": true,
  "message": "Write routing verified. Routed to MASTER DataSource.",
  "data": null,
  "connectionInfo": {
    "transactionType": "WRITE (Check)",
    "databaseUrl": "jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": false
  }
}
```

---

### 4.2 Read-Only Routing 검증 (Slave1 ⇄ Slave2 라운드로빈 확인)
읽기 전용 트랜잭션(`@Transactional(readOnly = true)`)을 연속 호출하여 `Slave1(3307)`과 `Slave2(3308)`로 번갈아가며 균등하게 분기되는지 확인합니다.

```bash
# 1차 호출 -> Slave1 (포트 3307)
curl -X GET http://localhost:8080/api/routing/read-check

# 2차 호출 -> Slave2 (포트 3308)
curl -X GET http://localhost:8080/api/routing/read-check

# 3차 호출 -> Slave1 (포트 3307 순환)
curl -X GET http://localhost:8080/api/routing/read-check
```
* **응답 예시 (Slave1)**:
```json
{
  "success": true,
  "message": "Read-only routing verified. Routed to SLAVE DataSource (Round-Robin).",
  "data": null,
  "connectionInfo": {
    "transactionType": "READ-ONLY (Check)",
    "databaseUrl": "jdbc:mysql://localhost:3307/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": true
  }
}
```
* **응답 예시 (Slave2)**:
```json
{
  "success": true,
  "message": "Read-only routing verified. Routed to SLAVE DataSource (Round-Robin).",
  "data": null,
  "connectionInfo": {
    "transactionType": "READ-ONLY (Check)",
    "databaseUrl": "jdbc:mysql://localhost:3308/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": true
  }
}
```

---

### 4.3 회원 등록 (Master DB 쓰기)
`POST /api/members` 호출 시 Master DB(`localhost:3306`)에 엔티티가 INSERT 됩니다.

```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "email": "alice@example.com"}'
```
* **응답 예시**:
```json
{
  "success": true,
  "message": "Member created successfully on MASTER DataSource",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com"
  },
  "connectionInfo": {
    "transactionType": "WRITE (createMember)",
    "databaseUrl": "jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": false
  }
}
```

---

### 4.4 회원 단건 조회 (Slave DB 읽기)
`GET /api/members/{id}` 호출 시 Slave DB(`localhost:3307` 또는 `3308`)로 SELECT 쿼리가 전달됩니다.

```bash
curl -X GET http://localhost:8080/api/members/1
```
* **응답 예시**:
```json
{
  "success": true,
  "message": "Member retrieved from SLAVE DataSource",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com"
  },
  "connectionInfo": {
    "transactionType": "READ-ONLY (getMember)",
    "databaseUrl": "jdbc:mysql://localhost:3307/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": true
  }
}
```

---

### 4.5 회원 전체 목록 조회 (Slave DB 읽기)

```bash
curl -X GET http://localhost:8080/api/members
```
* **응답 예시**:
```json
{
  "success": true,
  "message": "Members list retrieved from SLAVE DataSource",
  "data": [
    {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com"
    }
  ],
  "connectionInfo": {
    "transactionType": "READ-ONLY (getAllMembers)",
    "databaseUrl": "jdbc:mysql://localhost:3308/testdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    "catalog": "testdb",
    "isReadOnly": true
  }
}
```

---

### 4.6 HikariCP 커넥션 풀 실시간 상태 모니터링
Master, Slave1, Slave2 각각의 독립된 커넥션 풀(활성 커넥션 수, 유휴 커넥션 수, 대기 스레드 수)을 조회합니다.

```bash
curl -X GET http://localhost:8080/api/routing/pool-status
```
* **응답 예시**:
```json
{
  "master": {
    "poolName": "HikariPool-Master",
    "activeConnections": 0,
    "idleConnections": 2,
    "totalConnections": 2,
    "threadsAwaitingConnection": 0
  },
  "slave1": {
    "poolName": "HikariPool-Slave1",
    "activeConnections": 0,
    "idleConnections": 2,
    "totalConnections": 2,
    "threadsAwaitingConnection": 0
  },
  "slave2": {
    "poolName": "HikariPool-Slave2",
    "activeConnections": 0,
    "idleConnections": 2,
    "totalConnections": 2,
    "threadsAwaitingConnection": 0
  }
}
```

---

## 5. 테스트 및 검증 결과

Kotest BDD (`BehaviorSpec`) 스타일을 기반으로 다음 항목들을 검증하였습니다.

* **`DataSourceRoutingTest`**:
  * `@Transactional` (쓰기): `master_db`로 라우팅 확인
  * `@Transactional(readOnly = true)` (읽기): `slave1_db` 및 `slave2_db`로 Round-Robin 교차 라우팅 확인
  * Master에만 쓰기 저장 시 격리된 Slave에서 미조회됨을 통해 DB 분리 검증
* **`DataSourceConcurrencyLoadTest`**:
  * 비동기 코루틴(`Dispatchers.IO`)을 통해 동시 20건의 쓰기 요청과 40건의 읽기 요청 발생
  * 20건의 쓰기 요청은 전부 `HikariPool-Master`에서 처리
  * 40건의 읽기 요청은 `HikariPool-Slave1`(20건)과 `HikariPool-Slave2`(20건)로 균등 분산 처리
  * 부하 종료 후 모든 활성 커넥션이 정상 반납(`activeConnections == 0`)됨을 검증

```bash
# 전체 테스트 실행
./gradlew test
```

---

## 6. 추가 리서치 및 아키텍처 가이드

* **[Slave Scale-Out 아키텍처 및 JDBC Load Balancing 방식 검토 리서치](docs/slave-scale-out-architecture-review.md)**
  * 단일 Slave URL에 여러 DB Host를 묶는 `jdbc:mysql:loadbalance://` 방식의 동작 원리
  * HikariCP 커넥션 풀링 환경에서의 실시간 쿼리 부하 쏠림 문제점
  * AWS Aurora Reader Endpoint / ProxySQL / Dynamic Routing DataSource 패턴 비교 및 Best Practice
