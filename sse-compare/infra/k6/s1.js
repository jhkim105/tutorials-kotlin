import http from 'k6/http';
import { check } from 'k6';

// 교차 검증용 k6 스크립트 (POST /chat/stream)
// 실행 예: TARGET_URL=http://localhost:8081/chat/stream k6 run sse-compare/infra/k6/s1.js
// 주의: 기본 http.post 는 스트림 전체를 버퍼링하므로 이벤트 단위 지연은 loadgen(Kotlin 코루틴)으로 측정한다.

export const options = {
  stages: [
    { duration: '10s', target: 50 },
    { duration: '20s', target: 200 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

const targetUrl = __ENV.TARGET_URL || 'http://localhost:8081/chat/stream';
const events = Number(__ENV.EVENTS || 10);
const intervalMs = Number(__ENV.INTERVAL_MS || 200);

export default function () {
  const payload = JSON.stringify({
    prompt: 'k6 cross-check',
    events: events,
    intervalMs: intervalMs,
    persist: false,
  });

  const res = http.post(targetUrl, payload, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '60s',
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'contains done event': (r) => r.body && r.body.includes('event:done'),
  });
}
