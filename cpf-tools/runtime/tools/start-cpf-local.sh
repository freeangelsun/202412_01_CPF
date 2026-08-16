#!/usr/bin/env sh
# CPF 개발자 기본 Local Runtime: Generated Domain까지 한 JVM/한 HTTP Port에 조립합니다.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
MODE=${CPF_LOCAL_MODE:-integrated}
RESOURCE_PROFILE=${CPF_RESOURCE_PROFILE:-local}
case "$MODE" in integrated|minimal|standard|full|integration) ;; *) echo "unsupported CPF_LOCAL_MODE=$MODE" >&2; exit 2;; esac
case "$RESOURCE_PROFILE" in local|dev|test|stg|prod) ;; *) echo "unsupported CPF_RESOURCE_PROFILE=$RESOURCE_PROFILE" >&2; exit 2;; esac

COMMON_POLICY=$ROOT/gradle/cpf-runtime/common.properties
ENV_POLICY=$ROOT/gradle/cpf-runtime/$RESOURCE_PROFILE.properties
WEB_MODULE_POLICY=$ROOT/cpf-tools/runtime/cpf-local-runtime/cpf-resource.properties
[ -f "$COMMON_POLICY" ] || { echo "CPF resource policy missing: $COMMON_POLICY" >&2; exit 2; }
[ -f "$ENV_POLICY" ] || { echo "CPF resource profile missing: $ENV_POLICY" >&2; exit 2; }

cpf_prop () {
    key=$1; file=$2
    [ -f "$file" ] || return 0
    awk -F= -v k="$key" '$1==k { sub(/^[^=]*=/, ""); print }' "$file" | tail -n 1
}
cpf_resolve () {
    key=$1; explicit=${2:-}; module_file=${3:-}
    value=$(cpf_prop "$key" "$COMMON_POLICY")
    env_value=$(cpf_prop "$key" "$ENV_POLICY")
    [ -z "$env_value" ] || value=$env_value
    [ -z "$explicit" ] || value=$explicit
    module_value=$(cpf_prop "$key" "$module_file")
    [ -z "$module_value" ] || value=$module_value
    printf '%s' "$value"
}

PORT=${CPF_LOCAL_RUNTIME_PORT:-8080}
BIND_ADDRESS=${CPF_LOCAL_BIND_ADDRESS:-127.0.0.1}
WEB_XMS=$(cpf_resolve runtime.web.xms "${CPF_WEB_XMS:-}" "$WEB_MODULE_POLICY")
WEB_XMX=$(cpf_resolve runtime.web.xmx "${CPF_WEB_XMX:-}" "$WEB_MODULE_POLICY")
MODULE_WEB_XMX=$(cpf_prop runtime.web.xmx "$WEB_MODULE_POLICY")
[ -z "$MODULE_WEB_XMX" ] && MODULE_WEB_XMX=$(cpf_prop runtime.xmx "$WEB_MODULE_POLICY")
if [ -z "$MODULE_WEB_XMX" ] && [ -z "${CPF_WEB_XMX:-}" ]; then
    MODE_XMX=$(cpf_resolve "runtime.web.mode.$MODE.xmx" '' "$WEB_MODULE_POLICY")
    [ -z "$MODE_XMX" ] || WEB_XMX=$MODE_XMX
fi
MAX_METASPACE=$(cpf_resolve runtime.jvm.maxMetaspace '' "$WEB_MODULE_POLICY")
MAX_DIRECT=$(cpf_resolve runtime.jvm.maxDirectMemory '' "$WEB_MODULE_POLICY")
CODE_CACHE=$(cpf_resolve runtime.jvm.reservedCodeCache '' "$WEB_MODULE_POLICY")
THREAD_STACK=$(cpf_resolve runtime.jvm.threadStack '' "$WEB_MODULE_POLICY")
ENFORCE_CEILING=$(cpf_resolve runtime.memory.enforceCeiling '' "$WEB_MODULE_POLICY")
CEILING_MB=$(cpf_resolve runtime.memory.ceiling.mb '' "$WEB_MODULE_POLICY")
HEAP_STEP_MB=$(cpf_resolve heap.step.mb '' "$WEB_MODULE_POLICY")

case "$PORT" in ''|*[!0-9]*) echo "invalid CPF_LOCAL_RUNTIME_PORT=$PORT" >&2; exit 2;; esac
[ "$PORT" -ge 1 ] && [ "$PORT" -le 65535 ] || { echo "invalid CPF_LOCAL_RUNTIME_PORT=$PORT" >&2; exit 2; }

PYTHON=$(command -v python3 || command -v python || true)
[ -n "$PYTHON" ] || { echo 'python3/python is required for CPF tooling' >&2; exit 2; }
if [ "$ENFORCE_CEILING" = true ]; then
  "$PYTHON" - "$WEB_XMS" "$WEB_XMX" "$HEAP_STEP_MB" "$CEILING_MB" <<'PY'
import re,sys

def mb(v):
    m=re.fullmatch(r'(\d+)([mMgG])',v)
    if not m: raise SystemExit(f'invalid CPF local memory value: {v}')
    n=int(m.group(1)); return n*1024 if m.group(2).lower()=='g' else n
step=int(sys.argv[3]); ceiling=int(sys.argv[4])
for name,value in [('Xms',sys.argv[1]),('Xmx',sys.argv[2])]:
    n=mb(value)
    if n < step or n > ceiling or n % step:
        raise SystemExit(f'CPF {name}={value} must use {step}MB increments and stay <={ceiling}MB')
if mb(sys.argv[1]) > mb(sys.argv[2]): raise SystemExit('CPF local Xms must be <= Xmx')
PY
fi

"$PYTHON" - "$PORT" "$BIND_ADDRESS" <<'PY'
import socket,sys
p=int(sys.argv[1]); host=sys.argv[2]
s=socket.socket()
try: s.bind((host,p))
except OSError as e: raise SystemExit(f'Local Runtime port is already in use: {host}:{p} ({e})')
finally: s.close()
PY

cd "$ROOT"
if [ "${CPF_LOCAL_SKIP_BUILD:-false}" != true ]; then
  ./gradlew -PcpfIncludeGeneratedDomains=true -PcpfResourceProfile="$RESOURCE_PROFILE" :cpf-local-runtime:bootJar --no-daemon
fi
JAR=$(find cpf-tools/runtime/cpf-local-runtime/build/libs -maxdepth 1 -type f -name '*-local-web.jar' | sort | tail -1)
[ -n "$JAR" ] || { echo 'cpf-local-runtime bootJar not found' >&2; exit 3; }
mkdir -p build/cpf-local-runtime/logs
PID_FILE=build/cpf-local-runtime/local-web.pid
[ ! -f "$PID_FILE" ] || { echo "already running or stale pid file: $PID_FILE" >&2; exit 4; }
PROFILE="local,local-$MODE"
nohup java ${JAVA_OPTS:-} \
  "-Xms$WEB_XMS" "-Xmx$WEB_XMX" \
  "-XX:MaxMetaspaceSize=$MAX_METASPACE" "-XX:MaxDirectMemorySize=$MAX_DIRECT" \
  "-XX:ReservedCodeCacheSize=$CODE_CACHE" "-Xss$THREAD_STACK" \
  -Dfile.encoding=UTF-8 -jar "$JAR" \
  "--spring.profiles.active=$PROFILE" \
  "--server.address=$BIND_ADDRESS" "--server.port=$PORT" \
  "--cpf.environment=local" "--cpf.local.runtime.enabled=true" \
  "--cpf.local.modules.domains.enabled=true" "--cpf.local.modules.domains.auto-discover=true" \
  > build/cpf-local-runtime/logs/LOCAL_WEB.out.log \
  2> build/cpf-local-runtime/logs/LOCAL_WEB.err.log &
echo $! > "$PID_FILE"
echo "CPF local runtime started. pid=$(cat "$PID_FILE") address=$BIND_ADDRESS port=$PORT mode=$MODE resourceProfile=$RESOURCE_PROFILE Xms=$WEB_XMS Xmx=$WEB_XMX"
