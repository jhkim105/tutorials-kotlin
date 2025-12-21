# context.md — Trade Ticks SSE Demo (Spring Boot 3.5.9, Kotlin, Multi-module)

## 목적
“체결 데이터(trade ticks)를 SSE로 구독해 수신하는 데모”를 만든다.

- **Server**: 체결 이벤트를 생성/입력받아(in-memory) SSE로 실시간 푸시
- **Client**: SSE를 구독(consumer)하여 수신 체결을 콘솔 로그로 출력

> 범위 한정: **체결 데이터 수신(전송/구독) 데모**에만 집중한다. (주문/호가/잔고/인증/DB/Kafka 등은 제외)

---

## 기술 스택 (고정)
- Language: Kotlin
- Build: Gradle (Kotlin DSL)
- Spring Boot: **3.5.9**
- Server: Spring WebFlux (SSE 지원)
- Client: Spring Boot + WebFlux `WebClient`
- Streaming model: Kotlin `Flow`, `SharedFlow`
- SSE: `text/event-stream`
- External infra: 없음 (Kafka/Redis/DB 사용 ❌)

---

## 멀티모듈 구성 (필수)
단일 레포 안에 3개 모듈로 구성한다.

- `trade-common` : 공용 DTO (TradeTick, Publish 요청)
- `trade-sse-server` : SSE Server 애플리케이션
- `trade-sse-client` : SSE Client 애플리케이션 (Spring Boot)

디렉토리 예시:
```
.
├─ settings.gradle.kts
├─ build.gradle.kts
├─ trade-common/
│  └─ build.gradle.kts
├─ trade-sse-server/
│  └─ build.gradle.kts
└─ trade-sse-client/
   └─ build.gradle.kts
```

### Gradle 정책 (중요)
- Spring Boot plugin version은 **반드시 3.5.9**
- `trade-common`은 Spring Boot plugin 적용 ❌ (순수 Kotlin/JVM 모듈)
- `trade-sse-server`, `trade-sse-client`만 Spring Boot plugin 적용 ✅
- WebFlux 사용 (spring-boot-starter-webflux)
- Coroutines Reactor 브릿지 사용 (kotlinx-coroutines-reactor)

---

## 공용 DTO (trade-common)
### 1) TradeTick DTO (필수 필드)
- `tradeId: String` (UUID 또는 증가값 문자열; 데모는 UUID 권장)
- `symbol: String` (예: META, AAPL, BTCUSDT)
- `price: BigDecimal`
- `qty: BigDecimal`
- `ts: Instant` (체결 시각)

예시:
```kotlin
data class TradeTick(
  val tradeId: String,
  val symbol: String,
  val price: BigDecimal,
  val qty: BigDecimal,
  val ts: Instant
)
```

### 2) PublishTradeRequest DTO
- `symbol: String`
- `price: BigDecimal`
- `qty: BigDecimal`

서버는 요청을 받으면 `tradeId`, `ts`를 채워 `TradeTick`으로 변환해 publish 한다.

---

## 서버 (trade-sse-server) 요구사항

### 1) Trade EventBus (in-memory)
- `MutableSharedFlow<TradeTick>` 기반
- 설정 권장:
  - `replay = 0` (연결 이후 이벤트만)
  - `extraBufferCapacity = 1000` (데모에서 충분히 크게)
- 구독자는 symbol 파라미터로 필터링:
  - `flow.filter { it.symbol == symbol }`
- (선택) 폭주 대응 데모를 위해 `conflate()` 또는 `sample(Duration.ofMillis(200))` 중 하나를 추가해도 됨.
  - 단, “원본 스트림” vs “다운샘플 스트림”을 엔드포인트로 분리하면 학습 효과가 큼.

### 2) 체결 생성/입력 API (Publish)
- Endpoint: `POST /api/trades`
- Request JSON:
```json
{ "symbol": "META", "price": 350.12, "qty": 3 }
```
- Response: 202 또는 200
- 동작:
  - `PublishTradeRequest` → `TradeTick(tradeId, ts 포함)` 생성
  - EventBus에 emit (suspend)

### 3) SSE 구독 API (Subscribe)
- Endpoint: `GET /sse/trades?symbol=META`
- Response Content-Type: `text/event-stream`
- `symbol` 없으면 400
- 반환 타입 권장(학습용으로 SSE 포맷을 드러내기):
```kotlin
Flow<ServerSentEvent<TradeTick>>
```
- SSE 필드 권장:
  - `id`: tradeId
  - `event`: "trade"
  - `data`: TradeTick

연결 시 `onStart`로 “connected” 이벤트를 1회 보내도 됨(선택).
단, connected 이벤트도 TradeTick 형태로 만들기 애매하면, `event="system"` + data에 간단한 문자열을 쓰는 대신,
학습 단순화를 위해 connected 이벤트는 생략해도 무방.

### 4) 로깅 (필수)
- publish 시: symbol, price, qty, tradeId 로그
- subscribe 시: 접속/종료 로그 (가능하면 `onCompletion` 등 활용)

---

## 클라이언트 (trade-sse-client) 요구사항

### 1) 기본 동작
- 애플리케이션 시작 시 SSE 구독 시작
- 기본 구독 URL:
  - `http://localhost:8080/sse/trades?symbol=META`
- 수신한 TradeTick을 콘솔 로그로 출력
- 종료 시 구독 정리 로그

### 2) 설정(application.yml)
```yaml
tradeSse:
  serverBaseUrl: http://localhost:8080
  symbol: META
```

### 3) 구현 방식 (권장)
- WebFlux `WebClient` 사용
- `ServerSentEvent<TradeTick>`로 수신하여 SSE 개념이 보이도록 한다.
  - `data`가 null이 아닌 경우만 처리
- 수신 예시 로그(형태):
  - `[trade] symbol=META price=350.12 qty=3 id=... ts=...`

(선택) 연결이 끊기면 간단히 재연결:
- `retryWhen` + backoff 또는 재시도 루프를 아주 단순하게 구현
- 복잡해지면 README에 “확장 포인트”로만 제시해도 됨.

---

## 실행/테스트 시나리오 (필수)

### 1) 서버 실행
```bash
./gradlew :trade-sse-server:bootRun
```

### 2) 클라이언트 실행
```bash
./gradlew :trade-sse-client:bootRun
```

### 3) 체결 publish 테스트 (curl)
```bash
curl -X POST "http://localhost:8080/api/trades"   -H "Content-Type: application/json"   -d '{"symbol":"META","price":350.12,"qty":3}'
```

여러 번 호출하면 클라이언트가 실시간으로 연속 수신해야 한다.

---

## README.md에 반드시 포함할 내용
- 멀티모듈 구조 설명
- Spring Boot **3.5.9** 고정 사용 명시
- SSE가 무엇인지(서버→클라 단방향 스트림) + Flow는 서버 내부 스트림 모델이라는 점
- 실행 방법(서버/클라)
- curl 테스트 방법
- 예상 로그 예시(서버 publish / 클라 consume)

---

## (선택) 데모 품질을 높이는 확장 아이디어
- 엔드포인트 2개 제공:
  - `/sse/trades` : 원본 스트림
  - `/sse/trades-sampled` : `sample(200ms)` 적용(차트용)
- symbol을 2개 이상(META, AAPL) publish해 필터 동작 확인
- 서버에서 초당 N개 자동 생성 API (예: `POST /api/trades/burst?symbol=META&n=1000`)로 폭주 테스트(선택)

---

## 최종 기대 동작
1) Client가 특정 symbol로 SSE 연결
2) Server에 체결 publish 요청이 들어오면
3) Client 콘솔에 체결 데이터가 실시간으로 출력된다.
