#!/bin/bash

SYMBOL=${1:-AAPL}
BASE_URL=${2:-http://localhost:8080}

echo "Subscribing to trades for symbol: $SYMBOL"

echo "Press Ctrl+C to stop..."

curl -N -H "Accept: application/x-ndjson" "$BASE_URL/stream/trades?symbol=$SYMBOL"
