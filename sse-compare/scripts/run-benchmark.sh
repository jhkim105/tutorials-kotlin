#!/usr/bin/env bash
set -euo pipefail

# SSE Compare 벤치마크 실행 스크립트
# 사용법:
#   ./scripts/run-benchmark.sh <combo> <scenario> [--runs=3] [--stages=100,1000] [--db=jpa|r2dbc] [--docker]
#   ./scripts/run-benchmark.sh --report
#
# 예시:
#   ./scripts/run-benchmark.sh A S1
#   ./scripts/run-benchmark.sh B S1 --runs=3 --stages=100,500
#   ./scripts/run-benchmark.sh E S4 --db=r2dbc
#   ./scripts/run-benchmark.sh --report

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
TODAY="$(date +%Y%m%d)"
RESULTS_DIR="$ROOT_DIR/results/$TODAY"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"

if [[ "${1:-}" == "--report" ]]; then
  cd "$ROOT_DIR"
  ./gradlew :loadgen:run --no-daemon --args="--mode=report --results-dir=$ROOT_DIR/results"
  exit 0
fi

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <A|B|C1|C2|D|E> <S1|S2|S3|S4|S5|S6> [--runs=3] [--stages=100,500] [--db=jpa|r2dbc] [--docker]"
  echo "       $0 --report"
  exit 1
fi

COMBO="$1"
SCENARIO="$2"
shift 2

RUNS=3
STAGES=""
DB_MODE=""
USE_DOCKER=false

for arg in "$@"; do
  case "$arg" in
    --runs=*) RUNS="${arg#*=}" ;;
    --stages=*) STAGES="${arg#*=}" ;;
    --db=*) DB_MODE="${arg#*=}" ;;
    --docker) USE_DOCKER=true ;;
    *) echo "Unknown arg: $arg"; exit 1 ;;
  esac
done

case "$COMBO" in
  A)  MODULE="app-mvc-sseemitter"; PROFILES=""; PORT=8081; JOB="sse-compare-mvc-sseemitter" ;;
  B)  MODULE="app-mvc-sseemitter"; PROFILES="vt"; PORT=8081; JOB="sse-compare-mvc-sseemitter" ;;
  C1) MODULE="app-mvc-flow"; PROFILES=""; PORT=8082; JOB="sse-compare-mvc-flow" ;;
  C2) MODULE="app-mvc-flow"; PROFILES="vt"; PORT=8082; JOB="sse-compare-mvc-flow" ;;
  D)  MODULE="app-webflux-flux"; PROFILES=""; PORT=8083; JOB="sse-compare-webflux-flux" ;;
  E)  MODULE="app-webflux-flow"; PROFILES=""; PORT=8084; JOB="sse-compare-webflux-flow" ;;
  *) echo "Unknown combo: $COMBO (expected A, B, C1, C2, D, E)"; exit 1 ;;
esac

if [[ -n "$DB_MODE" ]]; then
  DB_PROFILE="db-$DB_MODE"
  PROFILES="${PROFILES:+$PROFILES,}$DB_PROFILE"
  COMBO_LABEL="${COMBO}-${DB_MODE}"
else
  COMBO_LABEL="$COMBO"
fi

APP_PID=""
cleanup() {
  if [[ "$USE_DOCKER" == "true" ]]; then
    docker compose -f "$ROOT_DIR/infra/compose.yml" --profile "$MODULE" stop "$MODULE" >/dev/null 2>&1 || true
  elif [[ -n "$APP_PID" ]]; then
    kill "$APP_PID" >/dev/null 2>&1 || true
    wait "$APP_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

cd "$ROOT_DIR"

if [[ "$USE_DOCKER" == "true" ]]; then
  echo "[run-benchmark] Starting $MODULE via Docker Compose (profiles=$PROFILES)..."
  SPRING_PROFILES_ACTIVE="$PROFILES" docker compose -f "$ROOT_DIR/infra/compose.yml" --profile "$MODULE" up -d --build "$MODULE"
else
  echo "[run-benchmark] Building :$MODULE:bootJar..."
  ./gradlew ":$MODULE:bootJar" --no-daemon -x test -q
  JAR_PATH="$(ls "$ROOT_DIR/$MODULE/build/libs/"*.jar | head -n 1)"
  echo "[run-benchmark] Starting $MODULE locally on port $PORT (profiles=$PROFILES)..."
  SPRING_PROFILES_ACTIVE="$PROFILES" java -Xmx512m -Xms512m -XX:+UseG1GC -jar "$JAR_PATH" > "$ROOT_DIR/results/${COMBO_LABEL}.log" 2>&1 &
  APP_PID=$!
fi

echo "[run-benchmark] Waiting for http://localhost:$PORT/actuator/health..."
for i in $(seq 1 60); do
  if curl -sf "http://localhost:$PORT/actuator/health" >/dev/null 2>&1; then
    echo "[run-benchmark] App is UP."
    break
  fi
  if [[ $i -eq 60 ]]; then
    echo "[run-benchmark] Timed out waiting for app."
    exit 1
  fi
  sleep 1
done

mkdir -p "$RESULTS_DIR"
./gradlew :loadgen:installDist --no-daemon -x test -q
LOADGEN_BIN="$ROOT_DIR/loadgen/build/install/loadgen/bin/loadgen"

for run in $(seq 1 "$RUNS"); do
  echo "[run-benchmark] Running $COMBO_LABEL / $SCENARIO (run $run/$RUNS)..."
  ARGS="--mode=run --combo=$COMBO_LABEL --scenario=$SCENARIO --url=http://localhost:$PORT --prometheus=$PROMETHEUS_URL --prometheus-job=$JOB --run=$run --out=$RESULTS_DIR"
  if [[ -n "$STAGES" ]]; then
    ARGS="$ARGS --stages=$STAGES"
  fi
  "$LOADGEN_BIN" $ARGS
done

echo "[run-benchmark] Completed $COMBO_LABEL / $SCENARIO ($RUNS runs). Results in $RESULTS_DIR"
