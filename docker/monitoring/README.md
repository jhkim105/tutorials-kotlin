# Monitoring (Prometheus + Grafana)

Spring Boot Actuator(`/actuator/prometheus`) 지표를 수집/시각화하는 공용 스택. 현재 `sse-compare` 벤치마크가 사용한다.

```bash
docker compose -f docker/monitoring/docker-compose.yml up -d
```

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin), 대시보드 `SSE Compare` 자동 등록

## 앱 포트 규칙 (sse-compare)

| 모듈 | 구성 | 포트 |
|------|------|------|
| app-mvc-sseemitter | A, B | 8081 |
| app-mvc-flow | C1, C2 | 8082 |
| app-webflux-flux | D | 8083 |
| app-webflux-flow | E | 8084 |

앱은 호스트(`bootRun`) 또는 포트 매핑된 컨테이너로 실행하고, Prometheus 는 `host.docker.internal` 로 접근한다.
