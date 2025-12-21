#!/bin/bash

# Default values
SYMBOL=${1:-META}

echo "Subscribing to trades for symbol: $SYMBOL"
echo "Press Ctrl+C to stop..."

#curl -N -H "Accept: text/event-stream" "http://localhost:8080/sse/trades?symbol=$SYMBOL"
curl -N "http://localhost:8080/stream/trades?symbol=$SYMBOL"
