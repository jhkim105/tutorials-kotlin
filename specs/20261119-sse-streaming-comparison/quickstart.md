# Quickstart: SSE 스트리밍 비교 검증 가이드

구현 완료 후 end-to-end로 동작을 확인하는 절차다. (`sse-compare/` 기준. 명령은 구현 후 확정되며 이 문서는 검증 시나리오를 정의한다.)

## 사전 준비

- Java 21, Docker (Compose v2)
- 파일 디스크립터 상향: `ulimit -n 65535` (연결 1만 개 이상 시나리오)
- 계약: [chat-stream-api.yaml](contracts/chat-stream-api.yaml), 모델: [data-model.md](data-model.md)

## 1. 빌드와 계약 테스트

```bash
cd sse-compare
./gradlew build            # 전체 컴파일
./gradlew test             # 공통 계약 테스트 (모든 앱 모듈, DB 통합은 Testcontainers)
```

**기대 결과**: 4개 앱 모듈 모두 계약 테스트 통과. (SC-001: A, B, C1, C2, D, E 구성 포함. 프로파일 변형은 테스트 파라미터로 실행)

## 2. 단일 구성 수동 확인

```bash
./gradlew :app-mvc-sseemitter:bootRun --args='--spring.profiles.active=vt'   # 구성 B
curl -N -X POST localhost:8080/chat/stream \
  -H 'Content-Type: application/json' \
  -d '{"prompt":"hi","events":3,"intervalMs":500}'
```

**기대 결과**: `message` 3개가 약 0.5초 간격으로 출력되고 `done` 이벤트 후 종료.
잘못된 요청(`"events":-1`)은 `400`과 `{code, message}` 응답.

## 3. 모니터링 스택

```bash
docker compose -f ../docker/monitoring/docker-compose.yml up -d   # Prometheus + Grafana
docker compose -f infra/compose.yml up -d mysql        # DB 시나리오용
```

- Prometheus: http://localhost:9090, Grafana: http://localhost:3000

**기대 결과**: 앱 기동 시 Prometheus 타깃이 UP, Grafana에서 스레드/힙/활성 연결 수 확인.

## 4. 벤치마크 실행

```bash
./scripts/run-benchmark.sh <combo> <scenario>
# 예: ./scripts/run-benchmark.sh B S1
```

**기대 결과**: `results/<date>/B-S1-run1.json` 생성. 클라이언트 지표(TTFB, 이벤트 간 지연 p50/95/99, 에러율)와 서버 지표 스냅샷 포함. (SC-007)

## 5. 시나리오별 검증 체크

| 시나리오 | 확인 항목 |
|----------|-----------|
| S1 | 단계별 한계 연결 수 식별 (SC-003) |
| S2 | 처리량, 완료율 |
| S3 | 메모리·스레드 수가 지속 증가하지 않음 |
| S4 | DB 저장 건수 = 요청 수, 풀 지표 기록 (SC-005) |
| S5 | 종료 후 활성 연결/스레드 수가 부하 전 수준으로 복귀 (SC-006) |
| S6 | 서버 메모리 무한 증가 없음, 일관된 타임아웃/정리 |

## 6. 보고서

```bash
./scripts/run-benchmark.sh --report     # results/ 집계 → results/RESULTS.md
```

**기대 결과**: 조합 × 시나리오 × 지표 표, 한계 연결 수, 워크로드별 추천이 포함된 `RESULTS.md` (SC-008).
