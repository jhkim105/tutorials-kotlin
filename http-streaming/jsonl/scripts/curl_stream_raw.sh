#!/bin/bash

BASE_URL=${1:-http://localhost:8080}
SYMBOL=${2:-AAPL}

echo "Streaming raw NDJSON with headers..."

curl -i -N -H "Accept: application/x-ndjson" "$BASE_URL/stream/trades?symbol=$SYMBOL"
