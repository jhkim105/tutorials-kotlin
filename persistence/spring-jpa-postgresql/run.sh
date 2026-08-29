#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=================================================="
echo "🚀 Starting PostgreSQL Container..."
echo "=================================================="
docker compose up -d

echo "=================================================="
echo "⏳ Waiting for PostgreSQL to be ready..."
echo "=================================================="
until docker exec test-postgres pg_isready -U postgres -d testdb > /dev/null 2>&1; do
  sleep 1
done
echo "✅ PostgreSQL is ready!"

echo "=================================================="
echo "🌱 Starting Spring Boot Application..."
echo "=================================================="
./gradlew bootRun
