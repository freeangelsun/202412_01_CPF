#!/usr/bin/env sh
set -eu
PROFILE="${1:-dev}"
case "$PROFILE" in dev|test|prod) ;; *) echo "profile must be dev/test/prod" >&2; exit 2;; esac
INSTANCE_ID="${CPF_WAS_ID:-cpf-batch-center-cut-$PROFILE-1}"
PORT="${CPF_PORT:-8183}"
JAVA_PATH="${JAVA_HOME:+$JAVA_HOME/bin/}java"
command -v "$JAVA_PATH" >/dev/null 2>&1 || command -v java >/dev/null 2>&1 || { echo 'java not found' >&2; exit 2; }
if ! command -v "$JAVA_PATH" >/dev/null 2>&1; then JAVA_PATH="$(command -v java)"; fi
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
JAR_PATH="${CPF_BATCH_JAR:-}"
if [ -z "$JAR_PATH" ]; then
  set -- "$ROOT"/cpf-batch/center-cut/build/libs/cpf-batch-center-cut-*.jar
  [ "$#" -eq 1 ] && [ -f "$1" ] || { echo 'expected exactly one executable jar; build :runtime:batch:center-cut:bootJar or set CPF_BATCH_JAR' >&2; exit 2; }
  JAR_PATH="$1"
fi
STATE_ROOT="${CPF_RUNTIME_STATE_DIR:-${TMPDIR:-/tmp}/cpf-batch-runtime}"
STATE_DIR="$STATE_ROOT/center-cut"; mkdir -p "$STATE_DIR"
PID_FILE="$STATE_DIR/$INSTANCE_ID.pid"; OUT="$STATE_DIR/$INSTANCE_ID.out.log"; ERR="$STATE_DIR/$INSTANCE_ID.err.log"
if [ -f "$PID_FILE" ]; then old=$(cat "$PID_FILE"); if kill -0 "$old" 2>/dev/null; then echo "already running pid=$old" >&2; exit 3; else rm -f "$PID_FILE"; fi; fi
SPRING_PROFILES_ACTIVE="$PROFILE" CPF_WAS_ID="$INSTANCE_ID" CPF_PORT="$PORT" nohup "$JAVA_PATH" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "$JAR_PATH" --spring.profiles.active="$PROFILE" --server.port="$PORT" --cpf.framework.was-id="$INSTANCE_ID" >>"$OUT" 2>>"$ERR" &
pid=$!; printf '%s' "$pid" > "$PID_FILE"
READINESS="http://127.0.0.1:$PORT/actuator/health/readiness"
command -v curl >/dev/null 2>&1 || { kill "$pid" 2>/dev/null || true; rm -f "$PID_FILE"; echo 'curl required for readiness' >&2; exit 2; }
i=0; while [ "$i" -lt "${CPF_READINESS_TIMEOUT_SECONDS:-90}" ]; do
  kill -0 "$pid" 2>/dev/null || break
  if curl -fsS --max-time 2 "$READINESS" >/dev/null 2>&1; then echo "CPF_BATCH_CENTER_CUT_READY profile=$PROFILE instance=$INSTANCE_ID port=$PORT pid=$pid readiness=$READINESS UTF-8"; exit 0; fi
  i=$((i+1)); sleep 1
done
kill "$pid" 2>/dev/null || true; rm -f "$PID_FILE"; echo "readiness failed profile=$PROFILE instance=$INSTANCE_ID port=$PORT pid=$pid" >&2; exit 1
