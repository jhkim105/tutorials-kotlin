#!/bin/bash

SYMBOL=${1:-AAPL}
PRICE=${2:-190.12}
QTY=${3:-3}
BASE_URL=${4:-http://localhost:8080}

echo "Publishing trade: Symbol=$SYMBOL, Price=$PRICE, Qty=$QTY"

curl -X POST "$BASE_URL/api/trades" \
  -H "Content-Type: application/json" \
  -d "{\"symbol\":\"$SYMBOL\",\"price\":$PRICE,\"qty\":$QTY}"

echo ""
