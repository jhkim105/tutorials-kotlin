# Sink 와 Flow 설명 정리 (WebFlux / Kotlin Coroutines)

이 문서는 **Spring WebFlux 기반 JSONL/SSE 스트리밍 데모**에서 사용되는  
**Reactor Sink**와 **Kotlin Flow**의 개념, 차이, 사용 기준을 정리한 문서입니다.

---

## 1. Sink란? (Reactor 세계)

### 한 줄 정의
**Sink는 외부에서 값을 밀어 넣을 수 있는(publish) 리액티브 스트림의 입구**입니다.

Reactor에서 일반적인 데이터 흐름은 다음과 같습니다.

```
Data Source → Flux → Subscriber
```

하지만 HTTP POST, 메시지 수신, 이벤트 등  
**“언제 들어올지 모르는 외부 입력”**을 스트림에 넣어야 할 때가 있습니다.

이때 사용하는 것이 **`Sinks.Many<T>`** 입니다.

---

### Sink의 역할 구조

```
POST /api/trades
        ↓
 Sinks.Many<TradeTick>   ← publish (emit)
        ↓
     Flux<TradeTick>     ← subscribe
        ↓
 HTTP Streaming / SSE / JSONL
```

- publish: `sink.tryEmitNext(value)`
- subscribe: `sink.asFlux()`

즉, **push 기반 pub/sub**를 만드는 도구입니다.

---

### 대표적인 Sink 생성 방식

```kotlin
Sinks.many()
    .multicast()
    .onBackpressureBuffer()
```

의미:
- `many()` : 여러 이벤트
- `multicast()` : 여러 구독자에게 fan-out
- `onBackpressureBuffer()` : 느린 구독자 대비

➡️ **SSE / JSONL HTTP Streaming / WebSocket fan-out에 가장 적합**

---

### Sink의 특징 요약

- WebFlux(Reactor)와 완벽히 호환
- backpressure 제어 가능
- HTTP streaming 과 궁합이 매우 좋음
- hot stream (구독 여부와 무관하게 publish 가능)
- emit 실패 처리 필요 (FAIL_NON_SERIALIZED 등)

---

## 2. Flow란? (Kotlin Coroutines 세계)

### 한 줄 정의
**Flow는 코루틴 기반 비동기 데이터 스트림**입니다.

```kotlin
flow {
    emit(1)
    delay(100)
    emit(2)
}
```

Flow는 다음을 중시합니다.

- 구조화된 동시성
- suspend 기반 제어
- 명확한 생명주기

> “이 스트림은 이 코루틴 스코프 안에서만 살아 있다”

---

### MutableSharedFlow

Flow 세계에서 Sink와 가장 유사한 개념입니다.

```kotlin
val sharedFlow = MutableSharedFlow<TradeTick>()
```

- 여러 producer → 여러 consumer
- 외부에서 값 push 가능

```kotlin
sharedFlow.emit(tick)     // suspend
sharedFlow.tryEmit(tick) // non-suspend
```

---

### SharedFlow 특징

- 코루틴 친화적
- Job cancel 시 자동 전파
- Flow 연산자(filter/map/buffer) 풍부
- WebFlux와 연결 시 Reactor-Flow 브리지 필요
- backpressure 개념이 Reactor와 다름

---

## 3. Sink vs Flow 비교

| 항목 | Sinks.Many | MutableSharedFlow |
|----|-----------|------------------|
| 세계관 | Reactor | Kotlin Coroutines |
| 주 사용처 | WebFlux, SSE, HTTP Streaming | Coroutine 기반 서비스 |
| publish | tryEmitNext | emit / tryEmit |
| subscribe | asFlux | collect |
| backpressure | 명시적 | buffer/overflow |
| HTTP Streaming | 매우 적합 | 보통 |
| 구조화된 취소 | 제한적 | 매우 강력 |
| Fan-out | 기본 지원 | 기본 지원 |

---

## 4. JSONL / SSE 데모에서 Sink가 더 적합한 이유

현재 구조:
- Spring Boot WebFlux
- HTTP Chunked Streaming (NDJSON)
- POST → GET fan-out
- Reactor Netty 기반

### 결론
**`Sinks.Many<TradeTick>`가 가장 자연스럽고 단순한 선택**

이유:
- WebFlux 내부가 Reactor 기반
- Flux 그대로 HTTP 응답에 연결 가능
- backpressure 정책을 HTTP 스트림에 직접 반영 가능
- SSE 데모와 구조적으로 동일

MutableSharedFlow를 사용할 경우:
- `asFlux()` 브리지 필요
- backpressure 의미가 약해짐
- Reactor + Coroutine 혼합 구조 발생

---

## 5. Flow가 더 적합한 경우

Flow는 다음과 같은 경우에 적합합니다.

- 서비스 내부 비동기 처리
- 도메인 이벤트 파이프라인
- Kafka Consumer 후처리
- UI / ViewModel
- Coroutine scope 생명주기가 중요한 경우

권장 패턴:

> **HTTP/WebFlux 경계 → Sink**  
> **도메인/비즈니스 로직 → Flow**

---

## 6. 한 문장 요약

> **Sink는 “HTTP/WebFlux에 이벤트를 밀어 넣는 입구”이고,  
> Flow는 “코루틴 세계에서 안전하게 흐르는 비동기 스트림”이다.**

JSONL/SSE 스트리밍 데모에서는  
👉 **Sinks.Many 중심 설계가 가장 적합하다.**

---
끝.
