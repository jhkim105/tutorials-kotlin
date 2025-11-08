# Spring Cache Redis 샘플

로컬 Redis를 캐시 저장소로 사용해 Spring Cache를 구성한 Kotlin/Spring Boot 예제입니다. 단건/리스트 조회 API를 제공하고, 캐시 이름별 TTL·프리픽스를 프로퍼티로 제어하는 방법과 통합 테스트를 포함하고 있습니다.

## 사전 준비

- JDK 17 이상
- `localhost:6379`에서 실행 중인 Redis  
  (예: `brew services start redis` 또는 `docker run -p 6379:6379 redis`)

## 애플리케이션 실행

```bash
./gradlew bootRun
```

기동 후에는 IntelliJ HTTP 클라이언트 파일(`http/sample.http`)이나 curl로 API를 호출할 수 있습니다.

```bash
curl http://localhost:8080/api/samples/42
curl "http://localhost:8080/api/samples?category=general&limit=5"
```

최초 요청은 서비스에서 데이터를 생성하고, 두 번째부터는 Redis 캐시를 통해 더 빠르게 응답합니다.

## 캐시 구성 포인트

- `@EnableCaching`으로 캐싱을 활성화하고, `CacheConfig`에서 Redis CacheManager를 커스터마이징합니다.
- JSON 직렬화를 위해 Jackson(JavaTime + 다형성) 모듈을 사용합니다.
- `cache.redis.*` 프로퍼티로 기본 TTL/프리픽스와 캐시별 오버라이드를 지정할 수 있습니다.

```properties
cache.redis.default-ttl=60s
cache.redis.default-prefix=cache:
cache.redis.caches.sampleSingle.ttl=30s
cache.redis.caches.sampleSingle.prefix=single:
cache.redis.caches.sampleList.ttl=90s
cache.redis.caches.sampleList.prefix=list:
```

- `SampleService#getSample`은 단일 객체를, `getSamples`는 리스트를 반환하며 각각 `sampleSingle`, `sampleList` 캐시에 저장됩니다.

## 통합 테스트

```bash
./gradlew test
```

`SampleCacheIntegrationTest`는 로컬 Redis가 동작할 때 실행되며, 캐시 적중 여부와 캐시별 TTL/프리픽스 적용을 검증합니다. (Redis가 동작하지 않으면 테스트가 자동으로 스킵됩니다.)
