#!/usr/bin/env sh
set -eu
INSTANCE_ID="${CPF_WAS_ID:-cpf-batch-agent-dev-1}"
STATE_ROOT="${CPF_RUNTIME_STATE_DIR:-${TMPDIR:-/tmp}/cpf-batch-runtime}"; PID_FILE="$STATE_ROOT/agent/$INSTANCE_ID.pid"
[ -f "$PID_FILE" ] || { echo 'CPF_BATCH_AGENT_STOP already-stopped'; exit 0; }
pid=$(cat "$PID_FILE"); case "$pid" in ''|*[!0-9]*) echo 'invalid pid file' >&2; exit 2;; esac
if kill -0 "$pid" 2>/dev/null; then kill "$pid"; i=0; while kill -0 "$pid" 2>/dev/null && [ "$i" -lt 20 ]; do i=$((i+1)); sleep 1; done; kill -0 "$pid" 2>/dev/null && kill -9 "$pid"; fi
rm -f "$PID_FILE"; echo 'CPF_BATCH_AGENT_STOP=PASS'
