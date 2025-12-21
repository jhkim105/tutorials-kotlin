#!/bin/bash

# Default values
SYMBOL=${1:-AAPL}
INTERVAL=${2:-1} # Seconds

echo "Auto-publishing trades for $SYMBOL every $INTERVAL seconds..."
echo "Press Ctrl+C to stop."

while true; do
  # Random price between 300 and 400
  PRICE=$(awk -v min=300 -v max=400 'BEGIN{srand(); print min+rand()*(max-min)}')
  # Random qty between 1 and 10
  QTY=$(awk -v min=1 -v max=10 'BEGIN{srand(); print int(min+rand()*(max-min))}')
  
  ./publish_trades.sh "$SYMBOL" "$PRICE" "$QTY"
  sleep "$INTERVAL"
done
