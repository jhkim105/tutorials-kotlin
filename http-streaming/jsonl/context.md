# JSONL HTTP Streaming Demo (WebFlux) — context.md

목표: **Kotlin + Spring Boot 3.5.9 + WebFlux** 로 **JSON Lines(NDJSON) 기반 HTTP Chunked Streaming** 데모 앱을 구현한다.  
서버는 한 요청에 대해 `\n` 로 구분되는 JSON 객체를 지속적으로 흘려보내고, 클라이언트는 이를 실시간으로 수신/로그 출력한다.

---

## 1) 전체 구성

### 언어/런타임
- Kotlin (JVM)
- Spring Boot **3.5.9**
- WebFlux (Reactor Netty)

### 산출물(프로젝트 2개)
- `jsonl-stream-server` : NDJSON 스트리밍 서버
- `jsonl-stream-client` : WebClient로 서버 스트림을 소비하는 클라이언트

### 저장소 구조(예시)
```
jsonl-demo/
  jsonl-stream-server/
  jsonl-stream-client/
  scripts/
```

---

## 2) 프로토콜/포맷 정의

### 2.1 Content-Type
- 서버 스트리밍 응답은 반드시 **`application/x-ndjson`** (또는 `application/x-ndjson;charset=UTF-8`) 로 반환
- 응답은 **chunked transfer** 로 지속 전송 (한 요청에 대해 연결을 유지)

### 2.2 NDJSON(JSON Lines) 규칙
- 각 이벤트는 **JSON 1개 + 개행(\n)** 으로 구성
- 예시
```
{"symbol":"AAPL","price":190.12,"qty":3,"tradeId":"t-000001","ts":1734741600000}
{"symbol":"AAPL","price":190.11,"qty":1,"tradeId":"t-000002","ts":1734741600500}
```

---

## 3) 서버 요구사항 (jsonl-stream-server)

### 3.1 엔드포인트

#### (Subscribe) NDJSON 스트림
- `GET /stream/trades?symbol={SYMBOL}`
  - produces: `application/x-ndjson`
  - queryParam `symbol` 은 필수 (없으면 400)
  - 스트림은 기본적으로 무한(또는 충분히 길게) 발행

#### (Publish) Trade 발행(서버로 입력)
- `POST /api/trades`
  - 요청 JSON 예시
    ```json
    { "symbol": "AAPL", "price": 190.12, "qty": 3 }
    ```
  - 서버는 `tradeId`, `ts`를 생성해 `TradeTick`으로 만든 뒤, 스트림 구독자에게 fan-out 한다.
  - 응답(ack) 예시
    ```json
    { "accepted": true, "tradeId": "t-000001", "ts": 1734741600000 }
    ```

> 참고: 기존 SSE 데모의 “publish/subscribe” 감각을 그대로 가져오기 위해, **JSONL에서도 publish(POST) → subscribe(GET 스트림)로 즉시 반영**되도록 한다.

### 3.2 응답 모델

#### TradeTick
- `symbol: String`
- `price: BigDecimal`
- `qty: Long`
- `tradeId: String` (유니크)
- `ts: Long` (epoch millis)

#### Publish 요청/응답 DTO(예시)
- `PublishTradeRequest(symbol, price, qty)`
- `PublishTradeResponse(accepted, tradeId, ts)`

### 3.3 구현 방식 (WebFlux)

#### 내부 fan-out 메커니즘: **Sinks.Many<TradeTick>**
- 서버는 중앙 채널로 `Sinks.Many<TradeTick>` 를 생성하고,
  - publish(POST)에서 `sink.tryEmitNext(tick)`(또는 emitNext)로 push
  - subscribe(GET)에서 `sink.asFlux()`를 source로 사용한다.
- Sink는 다중 구독자를 지원해야 하며(멀티캐스트), 느린 구독자(backpressure)에 대한 정책을 정한다.
  - 예: `Sinks.many().multicast().onBackpressureBuffer(...)` (정책/버퍼 사이즈는 데모 수준에서 합리적으로)

#### NDJSON 출력 보장
- NDJSON 규칙(“한 줄 JSON + \n”)을 확실하게 지키기 위해,
  - `Flux<TradeTick>` 직렬화 반환도 가능하나,
  - **권장**: `Flux<String>` 로 변환하여 `objectMapper.writeValueAsString(tick) + "\n"` 형태로 반환한다.
- `GET /stream/trades` 응답에서는 `symbol`로 필터링한다.
  - `sink.asFlux().filter { it.symbol == symbol } ...`

#### 핵심 요구사항
- 라인 단위로 즉시 흘러야 함 (buffering 최소화)
- 클라이언트가 연결을 끊으면 해당 구독이 종료되며, 서버에서 **cancel/complete/error**를 로그로 확인 가능해야 함

### 3.4 옵션/품질
- 로그
  - 스트림 연결 시작/종료, symbol, reason(취소/에러/정상완료) 기록
  - publish 요청 수신/emit 성공/실패 기록
- 설정 값(최소)
  - `server.port` (기본 8080)
- 테스트(선택)
  - `WebTestClient`로 content-type 확인
  - 스트림 첫 N개 이벤트 수신 테스트(예: take(5))

### 3.5 예시 컨트롤러 시그니처(참고)
- subscribe
  - `fun streamTrades(symbol: String): Flux<String>`
  - `@GetMapping("/stream/trades", produces = ["application/x-ndjson"])`
- publish
  - `fun publishTrade(@RequestBody req: PublishTradeRequest): Mono<PublishTradeResponse>`
  - `@PostMapping("/api/trades")`

---

## 4) 클라이언트 요구사항 (jsonl-stream-client)

### 4.1 기능
- 서버의 `/stream/trades`를 WebClient로 구독하여 실시간 로그 출력
- 실행 시 파라미터/설정으로 서버 주소와 symbol 지정 가능
  - `app.server.base-url` (예: http://localhost:8080)
  - `app.symbol` (예: AAPL)
- 출력 포맷 예:
  - `[trade] symbol=AAPL price=190.12 qty=3 id=t-000001 ts=...`

### 4.2 구현 방식
- WebClient 사용
- subscribe 시 `Accept: application/x-ndjson`
- 역직렬화 방식은 둘 중 하나로 구현(데모에서 더 안정적인 쪽 선택)
  1) `bodyToFlux(String::class.java)` 로 라인 수신 → ObjectMapper로 파싱
  2) 서버가 `Flux<TradeTick>` 직렬화로 안정 동작한다면 `bodyToFlux(TradeTick::class.java)`
- 종료/예외 로그 포함
  - onSubscribe/onCancel/onError 혹은 Kotlin Flow로 변환하여 onStart/onCompletion 처리 가능

### 4.3 재연결(선택)
- 네트워크 에러/서버 종료 시 `retryWhen`(지수 백오프, 최대 N회 또는 무한) 옵션 구현 가능
- Ctrl+C 종료 시 우아하게 종료

---

## 5) scripts (curl 기반)

> 스크립트는 **curl 명령만 포함**한다. (서버 시작/종료는 사용자가 별도로 수행)

### 5.1 publish/subscribe

#### scripts/publish_trades.sh (체결데이터 publish)
- 역할: `POST /api/trades`를 호출하여 trade tick을 발행한다.
- 구현: curl로 1회 발행 또는 루프 발행(옵션) 모두 가능.
- 예시(1회 발행)
  ```bash
  curl -X POST "http://localhost:8080/api/trades"     -H "Content-Type: application/json"     -d '{"symbol":"AAPL","price":190.12,"qty":3}'
  ```
- 예시(여러 개 발행: 데모 편의 옵션)
  ```bash
  for i in {1..10}; do
    curl -s -X POST "http://localhost:8080/api/trades"       -H "Content-Type: application/json"       -d "{"symbol":"AAPL","price":190.12,"qty":3}"
    echo
    sleep 0.2
  done
  ```

#### scripts/subscribe_trades.sh (체결데이터 subscribe)
- 역할: `GET /stream/trades?symbol=...` NDJSON 스트림을 수신한다.
- 요구사항: `curl -N`로 버퍼링 최소화
- 예시
  ```bash
  curl -N -H "Accept: application/x-ndjson"     "http://localhost:8080/stream/trades?symbol=AAPL"
  ```

### 5.2 raw 모드(헤더/타이밍 확인) — scripts/curl_stream_raw.sh
- 목적: 상태코드/헤더를 포함해 **원시 응답을 확인**한다.
  - `-i` 로 헤더 확인
  - `-N` 유지
  - `Content-Type: application/x-ndjson` 및 `Transfer-Encoding: chunked` 확인
- 예시
  ```bash
  curl -i -N -H "Accept: application/x-ndjson"     "http://localhost:8080/stream/trades?symbol=AAPL"
  ```

### 5.3 끊김 시뮬레이션 — scripts/simulate_disconnect.sh
- 목적: 몇 초만 스트림을 읽고 종료하여 **서버 cancel 전파**(disconnect 로그)를 확인한다.
- 예시
  ```bash
  timeout 3s curl -N -H "Accept: application/x-ndjson"     "http://localhost:8080/stream/trades?symbol=AAPL" || true
  ```

---

## 6) 빌드/실행 요구사항

### 6.1 공통
- Gradle Kotlin DSL 사용 (`build.gradle.kts`)
- Spring Boot **3.5.9** 명시
- Kotlin/JVM 설정 (버전은 최신 안정 범위에서 선택)
- 의존성
  - server: `spring-boot-starter-webflux`, `jackson-module-kotlin`, `kotlin-reflect`
  - client: `spring-boot-starter-webflux`, `jackson-module-kotlin`, `kotlin-reflect`

### 6.2 실행 예
- 서버
  ```bash
  cd jsonl-stream-server
  ./gradlew bootRun
  ```
- 클라이언트
  ```bash
  cd jsonl-stream-client
  ./gradlew bootRun --args='--app.server.base-url=http://localhost:8080 --app.symbol=AAPL'
  ```

---

## 7) 구현 체크리스트 (완료 기준)
- [ ] 서버 `/stream/trades`가 `application/x-ndjson`로 응답한다
- [ ] 응답이 라인 단위로 지속적으로 내려온다(버퍼링 없이 거의 실시간)
- [ ] `POST /api/trades` publish가 스트림에 즉시 반영된다
- [ ] 클라이언트가 WebClient로 스트림을 받아 로그를 찍는다
- [ ] curl 스크립트로도 스트림이 실시간으로 보인다(`curl -N`)
- [ ] 클라이언트 종료/끊김 시 서버가 cancel을 감지하고 중단 로그가 찍힌다
- [ ] README 또는 각 프로젝트 실행 방법이 명확히 작성된다

---

끝.
