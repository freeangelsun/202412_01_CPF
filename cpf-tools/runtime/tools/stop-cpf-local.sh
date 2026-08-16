#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
PID_FILE="$ROOT/build/cpf-local-runtime/local-web.pid"
if [ ! -f "$PID_FILE" ]; then echo 'CPF local runtime is not running.'; exit 0; fi
PID=$(cat "$PID_FILE")
kill "$PID" 2>/dev/null || true
rm -f "$PID_FILE"
echo "CPF local runtime stopped. pid=$PID"
