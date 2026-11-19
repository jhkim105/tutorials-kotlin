# PoC: Loadgen HTTP 클라이언트 선택 (Ktor CIO vs JDK HttpClient)

**검증일**: 2026-11-19

## 비교 및 결정

| 항목 | Ktor Client (CIO) | JDK 21 `HttpClient` |
|------|-------------------|---------------------|
| 동시성 모델 | 논블로킹 NIO + Kotlin 코루틴 네이티브 | `BodyHandlers.ofInputStream()`은 스레드 블로킹(VT 필요), `Flow.Subscriber`는 복잡 |
| POST SSE 청크 읽기 | `preparePost().execute { bodyAsChannel().readUTF8Line() }`로 청크 도착 즉시 타임스탬프 기록 | `BufferedReader.readLine()` 가능 |
| 10,000+ 연결 확장성 | `maxConnectionsCount` 설정만으로 소수 스레드에서 처리 | 연결당 스레드/버퍼 오버헤드 관리 필요 |
| 중도 이탈(S5) / 느린 읽기(S6) | `execute` 블록 조기 `return`으로 즉시 연결 종료, `delay()`로 백프레셔 시뮬레이션 | 스트림 close / sleep |

- **결정**: 주 부하기(`loadgen`)는 **Ktor Client CIO**(`io.ktor:ktor-client-cio:2.3.12`)를 채택한다.
- **보조**: 공통 계약 테스트(`ChatStreamContractSpec`)는 테스트 모듈 의존성을 최소화하기 위해 JDK 21 `HttpClient`를 사용한다.
- **교차 검증**: `infra/k6/s1.js`를 보조 스크립트로 유지한다.
