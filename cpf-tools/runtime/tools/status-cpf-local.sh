#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
PID_FILE="$ROOT/build/cpf-local-runtime/local-web.pid"
if [ ! -f "$PID_FILE" ]; then echo 'STOPPED'; exit 1; fi
PID=$(cat "$PID_FILE")
if kill -0 "$PID" 2>/dev/null; then echo "RUNNING pid=$PID"; exit 0; fi
echo "STALE pid=$PID"; exit 2
