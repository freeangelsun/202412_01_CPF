#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
echo '[CPF][PUBLIC] Member Online/Batch build and tests'
"$ROOT/gradlew" -p "$ROOT/cpf-member" clean build --no-daemon
if [ -f "$ROOT/cpf-biz-channel/build.gradle" ]; then
  echo '[CPF][PUBLIC] BZA Channel build and tests'
  "$ROOT/gradlew" -p "$ROOT/cpf-biz-channel" clean test build --no-daemon
fi
if [ -f "$ROOT/cpf-biz-frontend/package.json" ]; then
  echo '[CPF][PUBLIC] BZA Frontend clean install/verify'
  (cd "$ROOT/cpf-biz-frontend" && npm ci --ignore-scripts && npm run verify)
fi
echo '[CPF][PUBLIC] WORKSPACE_VERIFY=PASS'
