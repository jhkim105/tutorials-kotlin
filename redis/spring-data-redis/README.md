# spring-data-redis

Spring Data Redis를 활용한 Redis 기본 자료구조 CRUD 예제입니다.

## 개요

`RedisTemplate`과 Spring Data Redis Repository를 사용하여 Redis의 핵심 자료구조를 다루는 방법을 학습합니다.

## 주요 기능

### 자료구조별 예제 (테스트 코드)

| 테스트 클래스 | 내용 |
|---|---|
| `RedisHashTest` | Hash 자료구조 - `ObjectHashMapper`, `Jackson2HashMapper` 비교 |
| `RedisListTest` | List 자료구조 - `leftPush` / `rightPush` 동작 차이 |
| `RedisSetTest` | Set 자료구조 - 집합 연산 |
| `RedisSortedSetTest` | Sorted Set 자료구조 - 스코어 기반 정렬 |

### Spring Data Redis Repository

`@RedisHash` 어노테이션을 사용한 도메인 객체의 Redis 저장:

```kotlin
@RedisHash("User")
class User(
    @Id var id: String? = null,
    val username: String,
    val role: Role,
    val createdAt: Instant = Instant.now(),
    @TimeToLive val ttl: Long  // TTL 자동 설정
)

interface UserRepository : CrudRepository<User, String>
```

### Redis Cluster 연결 설정 (선택)

`RedisConfig`에 Lettuce 클라이언트를 사용한 Redis Cluster 연결 예제가 포함되어 있습니다 (기본 비활성화).

```kotlin
// 클러스터 토폴로지 자동 갱신 설정
val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
    .enablePeriodicRefresh(Duration.ofSeconds(10))
    .enableAllAdaptiveRefreshTriggers()
    .build()
```

## 프로젝트 구조

```
src/
├── main/kotlin/
│   └── ...redis/
│       ├── RedisConfig.kt              # Redis 연결 설정 (Cluster 옵션)
│       ├── RedisRepositoryConfig.kt    # Repository 활성화 설정
│       ├── RedisValueController.kt     # String 타입 REST API
│       └── user/
│           ├── User.kt                 # @RedisHash 도메인 클래스
│           └── UserRepository.kt      # Redis Repository
└── test/kotlin/
    └── ...redis/
        ├── RedisHashTest.kt            # Hash 자료구조 테스트
        ├── RedisListTest.kt            # List 자료구조 테스트
        ├── RedisSetTest.kt             # Set 자료구조 테스트
        ├── RedisSortedSetTest.kt       # Sorted Set 자료구조 테스트
        └── user/UserRepositoryTest.kt  # Repository 통합 테스트
```

## 실행 방법

```bash
# Redis 서버 실행
docker run -d -p 6379:6379 redis:latest

# 테스트 실행
./gradlew test
```

## 주요 학습 포인트

- `RedisTemplate` vs Spring Data Repository 사용 비교
- `ObjectHashMapper` vs `Jackson2HashMapper` 직렬화 방식 차이
- `@RedisHash`, `@TimeToLive` 등 어노테이션 활용
- Redis List의 `leftPush`/`rightPush` 삽입 방향에 따른 순서 차이
