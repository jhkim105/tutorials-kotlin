# spring-data-redis-reactive

Spring WebFlux + Kotlin Coroutines 환경에서 Reactive Redis를 사용하는 예제입니다.

## 개요

`ReactiveRedisTemplate`을 사용하여 비동기 방식으로 Redis를 다루는 방법을 다룹니다.
Coroutines의 `suspend` 함수와 결합하여 간결한 비동기 코드를 작성하는 방법을 보여줍니다.

## 주요 기능

### Reactive List 조작 (REST API)

```kotlin
@RestController
@RequestMapping("/list")
class RedisListController(private val redisListService: RedisListService) {

    @PostMapping
    suspend fun addToList(@RequestBody items: List<String>): Long =
        redisListService.addToList(items)

    @GetMapping
    suspend fun getList(@RequestParam page: Int, @RequestParam size: Int): List<String> =
        redisListService.getList(page, size)

    @DeleteMapping("/clear")
    suspend fun clearList(): Boolean = redisListService.clearList()
}

@Service
class RedisListService(private val redisTemplate: ReactiveRedisTemplate<String, String>) {

    // LPUSH - 리스트 왼쪽에 삽입
    suspend fun addToList(items: List<String>): Long =
        redisTemplate.opsForList().leftPushAll(key, items).awaitSingle()

    // 페이지네이션 지원 조회
    suspend fun getList(page: Int, size: Int): List<String> {
        val start = ((page - 1) * size).toLong()
        val end = start + size - 1
        return redisTemplate.opsForList().range(key, start, end).collectList().awaitSingle()
    }
}
```

### 비동기 순서 보장 테스트

`ReactiveRedisPushOrderTest`에서 `subscribe()` 호출 시 삽입 순서 보장 여부를 검증합니다.

```kotlin
// subscribe()는 비동기로 실행되므로 순서가 꼬일 수 있음을 확인하는 테스트
messages.forEach { msg ->
    redisTemplate.opsForList()
        .rightPush(key, msg)
        .subscribe()  // 비동기 호출 — 순서 보장 X
}
```

## 프로젝트 구조

```
src/
├── main/kotlin/.../
│   ├── RedisList.kt                     # RedisListController + RedisListService (Coroutines)
│   └── SpringDataRedisReactiveApplication.kt
└── test/kotlin/.../
    ├── RedisListTest.kt                 # List 삽입/조회 기본 테스트
    └── ReactiveRedisPushOrderTest.kt    # subscribe() 순서 보장 검증 테스트
```

## REST API

```http
### 리스트에 데이터 추가
POST http://localhost:8080/list
Content-Type: application/json
["item1", "item2", "item3"]

### 페이지네이션 조회 (1페이지, 10개)
GET http://localhost:8080/list?page=1&size=10

### 전체 삭제
DELETE http://localhost:8080/list/clear
```

## 실행 방법

```bash
# Redis 서버 실행
docker run -d -p 6379:6379 redis:latest

# 애플리케이션 실행 (기본 포트: 8080)
./gradlew bootRun

# 테스트 실행
./gradlew test
```

## 주요 학습 포인트

- `ReactiveRedisTemplate` vs 동기 `RedisTemplate` 비교
- Reactor의 `Mono` / `Flux`를 Coroutines `awaitSingle()` / `collectList()`로 변환
- `subscribe()` 비동기 호출 시 삽입 순서가 보장되지 않는 현상
- WebFlux에서 `suspend` 컨트롤러 메서드 작성 방법
