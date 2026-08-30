#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
LOG_ROOT="$ROOT/logs"
mkdir -p "$LOG_ROOT"
TS=$(date '+%Y%m%d_%H%M%S')
LOG="$LOG_ROOT/cpf_${TS}_$$.log"
START=$(date '+%Y-%m-%dT%H:%M:%S%z')
print_help() {
cat <<'EOF'
CPF Developer CLI
  cpf bootstrap
  cpf build
  cpf test
  cpf verify
  cpf status
  cpf stop
  cpf reset --confirm
  cpf domain new <name> <SYSTEM_CODE>
  cpf domain sync <name>
EOF
}
run_logged() {
  set +e
  "$@" 2>&1 | tee -a "$LOG"
  code=${PIPESTATUS:-}
  if [ -n "$code" ]; then rc=$(printf '%s' "$code" | awk '{print $1}'); else rc=0; fi
  set -e
  return "$rc"
}
cmd=${1:-help}; [ "$#" -gt 0 ] && shift || true
rc=0
case "$cmd" in
  help|-h|--help) print_help ;;
  bootstrap)
    echo "[1/1] CPF bootstrap" | tee -a "$LOG"
    java "$ROOT/bin/CpfBootstrap.java" bootstrap "$@" 2>&1 | tee -a "$LOG" || rc=$?
    [ "$rc" -eq 0 ] && echo "CPF LOCAL DEVELOPMENT READY" | tee -a "$LOG"
    ;;
  build)
    echo "[1/1] CPF build" | tee -a "$LOG"
    (cd "$ROOT" && ./gradlew cpfBuildAll "$@") 2>&1 | tee -a "$LOG" || rc=$?
    ;;
  test)
    echo "[1/1] CPF test" | tee -a "$LOG"
    (cd "$ROOT" && ./gradlew cpfTestAll "$@") 2>&1 | tee -a "$LOG" || rc=$?
    ;;
  verify)
    echo "[1/1] CPF verify" | tee -a "$LOG"
    (cd "$ROOT" && ./gradlew cpfVerify "$@") 2>&1 | tee -a "$LOG" || rc=$?
    ;;
  status)
    echo "CPF workspace: $ROOT" | tee -a "$LOG"
    if command -v docker >/dev/null 2>&1; then docker compose -f "$ROOT/deploy/local/compose.yaml" ps 2>&1 | tee -a "$LOG" || rc=$?; else echo "Container runtime not available" | tee -a "$LOG"; rc=2; fi
    ;;
  stop)
    echo "[1/1] CPF stop" | tee -a "$LOG"
    if command -v docker >/dev/null 2>&1; then docker compose -f "$ROOT/deploy/local/compose.yaml" down 2>&1 | tee -a "$LOG" || rc=$?; else echo "Container runtime not available" | tee -a "$LOG"; rc=2; fi
    ;;
  reset)
    if [ "${1:-}" != "--confirm" ]; then echo "CPF reset is destructive. Re-run: cpf reset --confirm" | tee -a "$LOG"; rc=2; else
      shift
      echo "[1/1] CPF reset" | tee -a "$LOG"
      if command -v docker >/dev/null 2>&1; then docker compose -f "$ROOT/deploy/local/compose.yaml" down -v 2>&1 | tee -a "$LOG" || rc=$?; else echo "Container runtime not available" | tee -a "$LOG"; rc=2; fi
    fi
    ;;
  domain)
    sub=${1:-}; [ "$#" -gt 0 ] && shift || true
    case "$sub" in
      new)
        name=${1:-}; code=${2:-};
        if [ -z "$name" ] || [ -z "$code" ]; then echo "Usage: cpf domain new <name> <SYSTEM_CODE>" | tee -a "$LOG"; rc=2; else java "$ROOT/bin/CpfGeneratorLauncher.java" new "$name" "$code" "$@" 2>&1 | tee -a "$LOG" || rc=$?; fi
        ;;
      sync)
        name=${1:-}; if [ -z "$name" ]; then echo "Usage: cpf domain sync <name>" | tee -a "$LOG"; rc=2; else java "$ROOT/bin/CpfGeneratorLauncher.java" sync "$name" "$@" 2>&1 | tee -a "$LOG" || rc=$?; fi
        ;;
      *) echo "Unknown domain command: ${sub:-<empty>}" | tee -a "$LOG"; print_help; rc=2 ;;
    esac
    ;;
  *) echo "CPF COMMAND FAILED: unknown command '$cmd'" | tee -a "$LOG"; print_help; rc=2 ;;
esac
END=$(date '+%Y-%m-%dT%H:%M:%S%z')
echo "CPF Command Result" | tee -a "$LOG"
echo "Command   : $cmd" | tee -a "$LOG"
echo "PASS/FAIL : $([ "$rc" -eq 0 ] && echo PASS || echo FAIL)" | tee -a "$LOG"
echo "ExitCode  : $rc" | tee -a "$LOG"
echo "Start Time: $START" | tee -a "$LOG"
echo "End Time  : $END" | tee -a "$LOG"
echo "Log File  : $LOG" | tee -a "$LOG"
if [ "$rc" -ne 0 ]; then echo "CPF COMMAND FAILED" | tee -a "$LOG"; echo "Next      : inspect the log and fix the failed stage, then rerun the same command." | tee -a "$LOG"; fi
exit "$rc"
