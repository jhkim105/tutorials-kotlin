# PoC: Spring MVC + Kotlin Coroutine Flow SSE 스트리밍 검증

**검증일**: 2026-11-19  
**환경**: Spring Boot 3.4.4, Kotlin 1.9.25, Java 21, Tomcat (Embedded)

## 검증 항목 및 결과

1. **`Flow<ServerSentEvent<Any>>` 반환 및 스트리밍**:
   - `spring-boot-starter-web` + `io.projectreactor:reactor-core` + `kotlinx-coroutines-reactor` 조합에서 컨트롤러가 `Flow<ServerSentEvent<Any>>`를 직접 반환하면 `ReactiveAdapterRegistry`가 이를 Reactive `Publisher`로 변환해 `text/event-stream` 스트리밍 응답을 정상 전송한다.
   - `ServerSentEvent.builder<Any>().comment("ping").build()`로 SSE 주석 heartbeat(`: ping`)도 정상 전송됨을 확인했다.
2. **클라이언트 중도 연결 종료 시 취소 전파**:
   - 클라이언트가 스트림 도중 소켓/InputStream을 닫으면, 다음 이벤트 쓰기 시 서블릿 비동기 쓰기가 예외를 감지해 Reactive `Subscription.cancel()`을 호출하고 Kotlin `Flow`에 `CancellationException`이 전파된다.
   - `try { ... } finally { handle.close() }` 블록이 정상 실행되어 `sse.active.connections` 지표가 0으로 복귀함을 계약 테스트(`MvcFlowDefaultContractTest`, `MvcFlowVirtualThreadContractTest`)로 검증했다.
3. **디스패처 변형 (C1 vs C2) 및 주의점**:
   - C1(`Dispatchers.Default`)과 C2(`Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()`) 모두 정상 동작한다.
   - 주의: `CoroutineDispatcher`를 Spring `@Bean`으로 등록할 때 Spring이 종료 시 `Closeable.close()`를 자동 호출해 `Dispatchers.Default cannot be closed` 경고가 발생한다. `@Bean(destroyMethod = "")`로 비활성화하고 VT executor만 `@PreDestroy`에서 명시 종료해야 한다.
