#!/bin/bash

BASE_URL=${1:-http://localhost:8080}
SYMBOL=${2:-AAPL}
DURATION=${3:-3}

echo "Streaming for ${DURATION}s then disconnecting..."

if command -v timeout >/dev/null 2>&1; then
  timeout "${DURATION}s" curl -N "$BASE_URL/stream/trades?symbol=$SYMBOL"
elif command -v gtimeout >/dev/null 2>&1; then
  gtimeout "${DURATION}s" curl -N "$BASE_URL/stream/trades?symbol=$SYMBOL"
else
  python - "$BASE_URL" "$SYMBOL" "$DURATION" <<'PY'
import subprocess
import sys

try:
    subprocess.run(
        ["curl", "-N", f"{sys.argv[1]}/stream/trades?symbol={sys.argv[2]}"],
        timeout=float(sys.argv[3]),
    )
except subprocess.TimeoutExpired:
    pass
PY
fi
