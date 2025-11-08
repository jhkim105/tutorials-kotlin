# dual-cache

Kotlin 기반 Spring Boot 애플리케이션으로, Caffeine 로컬 캐시와 Spring Data Redis 글로벌 캐시를 선택적으로 적용할 수 있는 실험용 프로젝트입니다. 동일한 서비스 메서드에 `@LocalCache` 또는 `@GlobalCache`를 선언하면 TTL·사이즈·캐시 이름을 세밀하게 제어하며 호출 비용을 줄일 수 있습니다.

## 프로젝트 구조
- `src/main/kotlin/jhkim105/dualcache` — 애플리케이션 엔트리포인트와 캐시 인프라 (`cache` 패키지) 및 샘플 서비스가 위치합니다.
- `src/main/resources` — `application.properties` 및 프로필별 Redis 설정을 배치합니다.
- `src/test/kotlin` — 메인 패키지를 미러링하는 테스트를 추가합니다.

## 주요 기능
1. `@LocalCache` : Caffeine 기반, TTL·최대 엔트리 수·null 캐시 여부 설정 가능.
2. `@GlobalCache` : Redis 기반 글로벌 캐시, TTL과 null 캐시 설정 가능.
3. 공통 `CacheKeyGenerator`가 동일한 키 규칙을 적용해 로컬/글로벌 캐시를 일관되게 다룹니다.

## 빠른 시작
```bash
docker run -d --name redis -p 6379:6379 redis:7
./gradlew bootRun
```
기본 Redis 호스트와 포트는 `application.properties`에서 조정할 수 있습니다.

## 캐시 애노테이션 사용법
```kotlin
@Service
class SampleService {
    @LocalCache(cacheName = "profile", ttlSeconds = 60, maximumSize = 500)
    fun loadProfile(id: String): Profile = remoteApi.fetch(id)

    @GlobalCache(cacheName = "profile", ttlSeconds = 300, cacheNull = true)
    fun loadProfileGlobally(id: String): Profile = remoteApi.fetch(id)
}
```
동일한 키 규칙을 활용하여 로컬 캐시 적중 후 글로벌 캐시를 백업 전략으로 활용할 수 있습니다.

## 개발 및 테스트
- `./gradlew test` : JUnit 5 기반 단위·통합 테스트 실행
- `./gradlew build` : 컴파일, 테스트, 실행 가능한 JAR 생성
- `./gradlew clean` : 생성물 삭제 후 재현 환경 초기화