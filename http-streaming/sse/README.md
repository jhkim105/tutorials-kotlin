## Trade Ticks SSE Demo
Spring Boot + Kotlin WebFlux + Coroutines 기반의 Server-Sent Events (SSE) 데모 프로젝트입니다.
체결 데이터(Trade Ticks)를 서버에서 클라이언트로 실시간 스트리밍하는 기능을 구현합니다.

### 프로젝트 구조
- `trade-common`: 공용 DTO만 포함 (Spring Boot plugin 미적용)
- `trade-sse-server`: WebFlux 기반 SSE 서버
- `trade-sse-client`: SSE 소비자(Spring Boot + WebFlux `WebClient`)

## 기술 스택
- **Language**: Kotlin
- **Framework**: Spring Boot 3.5.9 (WebFlux)
- **Concurrency**: Kotlin Coroutines & Flow
- **SSE**: `text/event-stream`
- 
### SSE/Flow 개념
- **SSE (Server-Sent Events)**: 서버 → 클라이언트 단방향 스트림(`text/event-stream`)으로, HTTP 연결을 유지하며 이벤트를 연속 전송합니다.
- **Kotlin Flow**: 서버 내부에서는 `MutableSharedFlow`를 사용하여 이벤트를 전파하고, 컨트롤러에서는 이를 `Flow<ServerSentEvent<TradeTick>>` 형태로 반환하여 SSE 스트림으로 변환합니다.

### 실행 방법
1) 서버 실행  
   `./gradlew :trade-sse-server:bootRun`
2) 클라이언트 실행 (다른 터미널)  
   `./gradlew :trade-sse-client:bootRun`

## 테스트 방법

### 편의 스크립트 사용 (`scripts/` 디렉토리)

프로젝트 루트에서 다음 스크립트들을 사용할 수 있습니다.

1.  **체결 데이터 생성 (Publish)**
    ```bash
    ./scripts/publish_trade.sh [SYMBOL] [PRICE] [QTY]
    # 예시: ./scripts/publish_trade.sh META 350.12 3
    ```

2.  **자동 체결 데이터 생성 (Auto Publish)**
    ```bash
    ./scripts/auto_publish.sh [SYMBOL] [INTERVAL]
    # 예시: 0.5초마다 META 체결 생성
    ./scripts/auto_publish.sh META 0.5
    ```

3.  **구독 테스트 (Subscribe)**
    (클라이언트 앱을 실행하지 않고 별도로 구독만 테스트하고 싶을 때)
    ```bash
    ./scripts/subscribe_trades.sh [SYMBOL]
    ```

### Curl 직접 사용

**Publish (POST)**
```bash
curl -X POST "http://localhost:8080/api/trades" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"META","price":350.12,"qty":3}'
```

**Subscribe (GET)**
```bash
curl -N "http://localhost:8080/sse/trades?symbol=META"
```

## 예상 로그

**Server**
```
Publishing trade: TradeTick(tradeId=..., symbol=META, price=350.12, qty=3, ts=...)
New SSE subscription for symbol: META
SSE stream started for symbol: META
```

**Client**
```
Starting SSE client for symbol: META from http://localhost:8080
Subscribed to SSE stream
[trade] symbol=META price=350.12 qty=3 id=... ts=...
```

