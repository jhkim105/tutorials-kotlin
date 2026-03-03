# redis-distributed-lock

Redisson 기반의 분산 락(Distributed Lock)을 AOP로 구현한 예제입니다.

## 개요

멀티 인스턴스 환경에서 공유 자원에 대한 동시 접근을 제어하기 위해 Redis 분산 락을 활용합니다.
`@DistributedLock` 커스텀 어노테이션과 AOP를 사용하여 비즈니스 코드와 락 처리 로직을 분리합니다.

## 주요 기능

### 1. `@DistributedLock` — 분산 뮤텍스 락

특정 키에 대해 하나의 스레드만 실행되도록 보장합니다. 동시 요청이 있을 경우 대기(waitTime) 후 실패합니다.

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,        // SpEL 또는 리터럴 키
    val waitTime: Long = 3000,   // 락 획득 대기 시간(ms)
    val leaseTime: Long = 1000   // 락 점유 최대 시간(ms)
)

// 사용 예시
@DistributedLock(key = "#id")
@Transactional
fun readArticle(id: Long) {
    articleRepository.readArticle(id)
}
```

**AOP 구현 포인트**

```kotlin
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1) // @Transactional보다 먼저 실행되어야 함
class DistributedLockAspect(private val redissonClient: RedissonClient) {
    @Around("@annotation(distributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, distributedLock: DistributedLock): Any? {
        val rLock = redissonClient.getLock(key)
        if (!rLock.tryLock(waitTime, leaseTime, MILLISECONDS)) {
            throw IllegalStateException("Could not acquire lock")
        }
        return joinPoint.proceed().also { rLock.unlock() }
    }
}
```

> ⚠️ `@Order(Ordered.LOWEST_PRECEDENCE - 1)`: Lock AOP가 Transaction AOP를 감싸야 합니다.
> 락 해제 전에 트랜잭션이 먼저 커밋되어야 다른 스레드에서 변경 내용을 볼 수 있습니다.

### 2. `@DistributedExecuteOnce` — 중복 실행 방지

Redisson의 `AtomicLong`을 활용하여 분산 환경에서 동일 키로 한 번만 실행되도록 보장합니다.

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedExecuteOnce(
    val key: String,
    val leaseTime: Long = 2000  // 락 TTL(ms)
)

// AOP - CAS 방식으로 0→1 전환에 성공한 경우만 실행
fun around(...): Any? {
    val atomicLong = redissonClient.getAtomicLong(key)
    if (atomicLong.compareAndSet(0, 1)) {
        atomicLong.expire(Duration.ofSeconds(leaseTime))
        return joinPoint.proceed()
    }
    return null  // 이미 실행 중 → 스킵
}
```

## 동시성 테스트

100개 스레드가 동시에 `readArticle()`을 호출할 때 `viewCount`가 정확히 100이 되는지 검증합니다.

```kotlin
"동시에 여러 사용자가 게시글 조회 시 viewCount가 정확히 증가해야 한다" {
    val threadCount = 100
    val latch = CountDownLatch(threadCount)
    val executor = Executors.newFixedThreadPool(threadCount)

    repeat(threadCount) {
        executor.submit {
            articleService.readArticle(saved.id!!)
            latch.countDown()
        }
    }
    latch.await()

    val updated = articleRepository.getById(saved.id!!)
    updated.viewCount shouldBe threadCount
}
```

## 프로젝트 구조

```
src/
├── main/kotlin/.../dlock/
│   ├── aop/
│   │   ├── DistributedLock.kt          # 분산 락 어노테이션 + Aspect
│   │   ├── DistributedExecuteOnce.kt   # 단일 실행 보장 어노테이션 + Aspect
│   │   └── KeyExtractor.kt             # SpEL 기반 락 키 추출
│   ├── config/
│   │   ├── MessageListenerConfig.kt
│   │   └── MessageTopicConfig.kt
│   ├── controller/
│   │   └── MessageController.kt
│   ├── messaging/
│   │   ├── RedisMessagePublisher.kt
│   │   └── RedisMessageSubscriber.kt
│   ├── persistence/
│   │   ├── Article.kt                  # 게시글 엔티티 (viewCount 포함)
│   │   ├── IdGenerator.kt              # Snowflake 기반 ID 생성
│   │   └── MessageEntity.kt
│   └── service/
│       ├── ArticleService.kt           # @DistributedLock 적용 예시
│       └── MessageService.kt
└── test/kotlin/...
    └── ArticleServiceConcurrencyTest.kt  # 100 스레드 동시성 테스트 (Kotest)
```

## 실행 환경

```yaml
# application.yml (예시)
spring:
  data:
    redis:
      host: localhost
      port: 6379
  datasource:
    url: jdbc:mariadb://localhost:3306/testdb
```

```bash
# Redis 실행
docker run -d -p 6379:6379 redis:latest

# MariaDB 실행
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=testdb mariadb:latest

# 테스트 실행
./gradlew test
```

## 주요 학습 포인트

- `RLock.tryLock(waitTime, leaseTime, TimeUnit)` — Redisson 분산 락 동작 원리
- `@Order` 로 AOP 실행 순서 제어 (Lock → Transaction)
- SpEL을 사용한 동적 락 키 추출 (`KeyExtractor`)
- `AtomicLong.compareAndSet()`으로 멱등성(Idempotency) 보장
- `CountDownLatch`를 활용한 동시성 테스트 패턴
