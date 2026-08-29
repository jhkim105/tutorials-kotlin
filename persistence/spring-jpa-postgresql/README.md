# Spring Data JPA with PostgreSQL Tutorial

이 프로젝트는 Spring Data JPA와 Kotlin 환경에서 **MySQL 대비 PostgreSQL이 갖는 고유한 차별화 기능 및 성능 이점**을 실습하고 검증할 수 있는 튜토리얼입니다.

---

## 1. PostgreSQL vs MySQL 핵심 차별화 특성

| 특성 | PostgreSQL | MySQL (InnoDB) | 본 프로젝트의 검증 포인트 |
| :--- | :--- | :--- | :--- |
| **대량 Batch Insert** | **`SEQUENCE` + `allocationSize` 지원**<br>(Batch Insert 100% 활성화) | `IDENTITY` 전략 사용 시<br>JDBC Batch Insert 비활성화 | `POST /api/products/batch` (1,000건 일괄 삽입) |
| **JSON 데이터 처리** | **`JSONB` 이진 분해 저장 + GIN 인덱싱**<br>(내부 키/값 고속 색인 & 연산자 지원) | 단순 JSON 텍스트 검증 위주<br>(가상 컬럼 생성 후 인덱싱 필요) | `GET /api/products/search/jsonb` (`->>` 연산자) |
| **배열 (Array) 컬럼** | **네이티브 배열(`text[]`, `int[]`) 지원**<br>(조인 테이블 없이 List 매핑) | 별도 1:N 매핑 테이블<br>또는 구분자 문자열 저장 필요 | `GET /api/products/search/tag` (`ANY()` 연산자) |
| **동시성 큐 처리** | **`FOR UPDATE SKIP LOCKED` 기본 지원**<br>(락 대기/블로킹 없이 병렬 워커 선점) | 8.0부터 지원하나 사용 빈도 및 제약 차이 | `POST /api/jobs/claim` (다중 스레드 동시 선점) |

---

## 2. 프로젝트 아키텍처 및 핵심 코드

### ① `Product.kt` (JSONB, Native Array, Sequence)
```kotlin
@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_sequence", allocationSize = 50)
    val id: Long? = null,

    val name: String,
    val price: Long,

    // 1. JSONB 매핑 (Hibernate 6)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var attributes: Map<String, Any> = emptyMap(),

    // 2. Native Array 매핑 (PostgreSQL text[])
    @Column(columnDefinition = "text[]")
    var tags: List<String> = emptyList()
)
```

### ② `JobTask.kt` (`SKIP LOCKED` 기반 고성능 분산 작업 큐)
```kotlin
interface JobTaskRepository : JpaRepository<JobTask, Long> {
    @Query(
        value = """
            SELECT * FROM job_tasks
            WHERE status = 'PENDING'
            ORDER BY id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    fun findNextPendingTaskForUpdateSkipLocked(): JobTask?
}
```

---

## 3. 실행 및 테스트 방법

### 1) 한 번에 실행 (Docker Compose + Spring Boot)
```bash
./run.sh
```

### 2) 수동 실행
```bash
# PostgreSQL 컨테이너 실행
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

### 3) Kotest BDD 전체 테스트 실행
```bash
./gradlew test
```
* `PostgreSqlFeaturesSpec`: JSONB 쿼리, 500건 대량 배치 저장, 5개 워커 코루틴의 `SKIP LOCKED` 동시성 선점 검증
* `PostgreSqlApiControllerSpec`: Spring MVC MockMvc 기반 REST API 통합 검증


---

## 4. REST API 실습 가이드

`src/main/resources/api.http` 파일을 IntelliJ IDEA 또는 VS Code REST Client에서 바로 실행하거나, 터미널에서 `curl`로 테스트할 수 있습니다.

### ① 대량 Batch Insert 성능 측정 (1,000건)
```bash
curl -X POST "http://localhost:8080/api/products/batch?count=1000"
```
**응답 예시:**
```json
{
  "requestedCount": 1000,
  "savedCount": 1000,
  "elapsedTimeMs": 85,
  "throughputPerSecond": 11764.71
}
```

### ② JSONB 내부 필드 검색 (`brand = Apple`)
```bash
curl -X GET "http://localhost:8080/api/products/search/jsonb?key=brand&value=Apple"
```

### ③ Native Array 태그 검색 (`tag = apple`)
```bash
curl -X GET "http://localhost:8080/api/products/search/tag?tag=apple"
```

### ④ `SKIP LOCKED` 동시성 작업 큐 실습
1. **작업 30개 생성 (Seed)**:
   ```bash
   curl -X POST "http://localhost:8080/api/jobs/seed?count=30"
   ```
2. **동시 선점 호출 (Worker-1, Worker-2)**:
   ```bash
   curl -X POST "http://localhost:8080/api/jobs/claim?workerName=Worker-1"
   curl -X POST "http://localhost:8080/api/jobs/claim?workerName=Worker-2"
   ```
   * *서로 락 대기(Wait/Block) 없이 각각 다른 ID의 작업을 즉시 `PROCESSING` 상태로 변경 후 획득.*
3. **진행 상태 조회**:
   ```bash
   curl -X GET "http://localhost:8080/api/jobs/status"
   ```
