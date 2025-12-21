## JSONL HTTP Streaming Demo
Spring Boot + Kotlin WebFlux 기반의 NDJSON(JSON Lines) 스트리밍 데모 프로젝트입니다.
`/api/trades`로 체결을 생성하고 `/stream/trades`에서 `application/x-ndjson`으로 수신합니다.

### 프로젝트 구조
- `jsonl-stream-server`: NDJSON 스트리밍 서버
- `jsonl-stream-client`: WebClient로 스트림을 소비하는 클라이언트

## 기술 스택
- **Language**: Kotlin
- **Framework**: Spring Boot 3.5.9 (WebFlux)
- **Streaming**: HTTP chunked + `application/x-ndjson`

### 실행 방법
1) 서버 실행
   `./gradlew :jsonl-stream-server:bootRun`
2) 클라이언트 실행 (다른 터미널)
   `./gradlew :jsonl-stream-client:bootRun --args='--app.server.base-url=http://localhost:8080 --app.symbol=AAPL'`

## 테스트 방법

### 편의 스크립트 사용 (`scripts/` 디렉토리)

1. **체결 생성 (Publish)**
   ```bash
   ./scripts/publish_trades.sh [SYMBOL] [PRICE] [QTY] [BASE_URL]
   # 예시: ./scripts/publish_trades.sh AAPL 190.12 3 http://localhost:8080
   ```

2. **구독 테스트 (NDJSON 스트림)**
   ```bash
   ./scripts/subscribe_trades.sh [SYMBOL] [BASE_URL]
   ```

3. **Raw 모드 (헤더/타이밍 확인)**
   ```bash
   ./scripts/curl_stream_raw.sh [BASE_URL] [SYMBOL]
   ```

4. **끊김 시뮬레이션**
   ```bash
   ./scripts/simulate_disconnect.sh [BASE_URL] [SYMBOL] [SECONDS]
   ```

### Curl 직접 사용
```bash
curl -N -H "Accept: application/x-ndjson" "http://localhost:8080/stream/trades?symbol=AAPL"
```

## 예상 로그

**Server**
```
NDJSON stream requested symbol=AAPL
NDJSON stream started symbol=AAPL
```

**Client**
```
Starting NDJSON subscription url=http://localhost:8080/stream/trades?symbol=AAPL
[trade] symbol=AAPL price=190.12 qty=3 id=t-000001 ts=...
```
