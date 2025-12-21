#!/bin/bash

# Default values
SYMBOL=${1:-META}
PRICE=${2:-350.12}
QTY=${3:-3}

echo "Publishing trade: Symbol=$SYMBOL, Price=$PRICE, Qty=$QTY"

curl -X POST "http://localhost:8080/api/trades" \
  -H "Content-Type: application/json" \
  -d "{\"symbol\":\"$SYMBOL\",\"price\":$PRICE,\"qty\":$QTY}"

echo "" # Newline
