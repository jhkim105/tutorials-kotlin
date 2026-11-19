# Spring MVC + Flow vs WebFlux + Flow 비교

**작성일**: 2026-11-19  
**관련 스펙**: `specs/20261119-sse-streaming-comparison/spec.md` (조합 C, E)  
**성격**: 사전 조사. 아래 성능 서술은 일반적인 동작 원리에 기반한 **예상**이며, 벤치마크로 검증한다.

두 조합 모두 컨트롤러 코드는 거의 같다. 차이는 그 아래의 서버 런타임과 요청 처리 방식에 있다.

## 1. 구현 방법

```kotlin
// 공통: 컨트롤러 코드는 사실상 동일
@PostMapping("/chat/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun stream(@RequestBody req: StreamRequest): Flow<ServerSentEvent<String>> = flow {
    repeat(req.events) { emit(ServerSentEvent.builder("msg-$it").build()); delay(req.intervalMs) }
}
```

| 항목 | MVC + Flow | WebFlux + Flow |
|------|-----------|----------------|
| 의존성 | `spring-boot-starter-web` + `reactor-core` + `kotlinx-coroutines-reactor` | `spring-boot-starter-webflux` + `kotlinx-coroutines-reactor` |
| 서버 | Tomcat (서블릿) | Netty (기본) |
| 반환 타입 | `Flow<T>` 또는 `suspend` 가능 | 동일 |
| 요청 바디 | `@RequestBody` 동기 역직렬화 | 논블로킹 역직렬화 (`suspend` 또는 `Flow` 입력도 가능) |
| 필터, 보안 | 서블릿 필터, Spring Security(서블릿) | `WebFilter`, Spring Security(리액티브). 설정 체계가 다름 |
| 생태계 | JPA, `ThreadLocal`, MDC가 자연스러움 | 블로킹 라이브러리 사용에 주의 필요 |

## 2. 동작 방식

### MVC + Flow
1. MVC가 `Flow`를 `ReactiveAdapterRegistry`로 `Publisher`로 변환한다. 내부적으로는 `ResponseBodyEmitter`/`DeferredResult` 계열 경로를 탄다.
2. 요청 스레드는 즉시 반환되고 서블릿 비동기 모드(`startAsync`)로 넘어간다.
3. 이벤트마다 **쓰기는 서블릿 컨테이너 스레드에서 블로킹 I/O로 수행**된다. 대기 중에는 스레드를 쓰지 않지만 쓰는 순간에는 스레드가 필요하다.
4. 코루틴은 지정한 디스패처(`Default`, `IO` 등)에서 실행되므로 코루틴 스레드와 서블릿 쓰기 스레드로 컨텍스트가 나뉜다.

### WebFlux + Flow
1. `Flow`를 `Flux`로 변환해 Reactor Netty의 논블로킹 쓰기 경로로 보낸다.
2. 소수의 이벤트 루프 스레드(보통 CPU 코어 수)가 **요청 수신, 코루틴 재개, 쓰기를 모두 논블로킹**으로 처리한다.
3. 백프레셔가 소켓 쓰기 가능 상태까지 끝에서 끝으로 전파된다.
4. `suspend` 컨트롤러는 이벤트 루프에서 시작되므로 **블로킹 호출을 직접 하면 이벤트 루프가 막힌다.** `withContext(Dispatchers.IO)`로 격리해야 한다.

## 3. 성능 관점 (예상, 실측 필요)

| 관점 | MVC + Flow | WebFlux + Flow |
|------|-----------|----------------|
| 유휴 연결의 스레드 | 점유 안 함 | 점유 안 함 |
| 이벤트 전송 시 | 서블릿 스레드가 짧게 블로킹 쓰기 | 논블로킹 쓰기 |
| 동시 연결 한계 | Tomcat `max-connections`, `threads.max`, 쓰기 경합 영향 | 소켓/메모리 한계에 가까움 (더 높음) |
| 메모리 | 연결당 서블릿 요청 객체 + 컨텍스트 전환 오버헤드 | 연결당 비용이 적은 편 |
| 다수 연결에서 이벤트 동시 발생 | 서블릿 스레드 풀이 병목 가능 | 이벤트 루프 소수로 처리 |
| 느린 클라이언트 | 블로킹 쓰기로 서블릿 스레드가 오래 묶일 수 있음 | 백프레셔로 완충 |
| CPU 사용 | 컨텍스트 전환이 더 많음 | 일반적으로 효율적 |
| 지연 꼬리(p99) | 풀이 차면 증가 | 이벤트 루프가 막히지 않는 한 안정적 |

- **순수 스트리밍(대기 위주, 연결 수 많음)**: WebFlux가 대체로 유리하며 연결 수가 커질수록 차이가 벌어진다. 수백 연결 이하에서는 차이가 작다.
- **블로킹 호출 혼합(JPA 등)**: MVC가 단순하고 안전하다. WebFlux는 `Dispatchers.IO`로 격리하면 결국 스레드 풀 구조가 되어 장점이 줄어든다.

## 4. 선택 가이드

| 상황 | 선택 |
|------|------|
| 기존 MVC/JPA 서비스에 스트리밍을 추가하고 코루틴 문법만 쓰고 싶다 | MVC + Flow |
| 신규 서비스이며 I/O 위주이고 연결 수가 많다 | WebFlux + Flow |
| 블로킹 코드를 그대로 쓰면서 확장성도 원한다 | MVC + Virtual Thread (조합 B) |

## 5. 벤치마크에서 확인할 점
- 두 조합의 컨트롤러 코드를 같게 두고 서버 스택만 다르게 해야 순수한 서버 차이가 드러난다.
- 비교 지표: 서블릿/이벤트 루프 스레드 수, 이벤트 간 지연 p99, 메모리 증가율, 느린 클라이언트에서의 동작.
- MVC + Flow의 코루틴 디스패처(`Default`, `IO`, VT 디스패처)에 따라 결과가 달라질 수 있다. (조합 F와 연결)
- Spring Boot 버전에 따라 MVC의 `Flow` 지원 세부 동작이 다를 수 있으므로 plan 단계에서 PoC로 먼저 확인한다.
