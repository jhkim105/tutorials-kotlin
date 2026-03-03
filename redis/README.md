# Redis Tutorials (Kotlin)

Spring Boot + Kotlin 기반의 Redis 활용 예제 모음입니다.

## 모듈 구성

| 모듈 | 설명 |
|------|------|
| [spring-data-redis](./spring-data-redis) | Spring Data Redis 기본 자료구조 활용 (String, Hash, List, Set, Sorted Set) |
| [spring-data-redis-pubsub](./spring-data-redis-pubsub) | Spring Data Redis Pub/Sub 메시징 패턴 |
| [spring-data-redis-reactive](./spring-data-redis-reactive) | Reactive Redis (WebFlux + Coroutines) |
| [spring-data-redisson-pubsub](./spring-data-redisson-pubsub) | Redisson을 이용한 Topic/Sharded Topic Pub/Sub |
| [redis-distributed-lock](./redis-distributed-lock) | Redisson 기반 분산 락 AOP 구현 |
| [redis-streams](./redis-streams) | Redis Streams를 이용한 메시지 큐 |
| [redis-streams-vs-pubsub](./redis-streams-vs-pubsub) | Redis Streams vs Pub/Sub 직접 비교 (영속성/재처리/재생/분산처리) |
| [docker](./docker) | Redis Cluster 도커 환경 설정 |

## 공통 기술 스택

- **Language**: Kotlin 1.9
- **Framework**: Spring Boot 3.4
- **Java**: 21
- **Redis Client**: Lettuce (기본), Redisson
- **Test**: JUnit 5, Kotest

## 사전 요구사항

- JDK 21
- Docker (Redis 실행용)
- Redis 서버 (기본 포트: 6379)

## 빠른 시작

```bash
# Redis 단일 서버 실행
docker run -d -p 6379:6379 redis:latest

# Redis Cluster 실행
cd docker
./docker-redis-cluster-predixy/run.sh
```

## 참고

- [redis-command.md](./redis-command.md) - 자주 사용하는 Redis CLI 명령어 정리
