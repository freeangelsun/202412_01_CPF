#!/usr/bin/env sh
set -eu

ACTION=${1:-help}
PROFILE=${CPF_RESOURCE_PROFILE:-local}
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../../.." && pwd)
cd "$ROOT"

GRADLE="$ROOT/gradlew"
VALIDATION="$ROOT/cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"
RUNTIME_TOOLS="$ROOT/cpf-tools/runtime/tools"

show_help() {
  cat <<EOF
============================================================
 CPF 개발 명령
============================================================
 build         전체 Build + 정적 품질 Gate
 test          전체 Java Test
 verify-fast   빠른 정적 검증
 verify-full   최대 로컬 검증 (PowerShell 7 필요)
 run-local     권장 Local 통합 Runtime
 run-batch     Batch 사용 시에만 별도 Runtime
 status        Local Runtime 상태
 stop          Local Runtime 종료
 modules       Public Starter 보기
 resource      현재 자원/메모리 정책 보기
============================================================
 Profile       $PROFILE
 Projects      apps / runtime / framework / starters / internal
============================================================
EOF
}

case "$ACTION" in
  help) show_help ;;
  build)
    java -version 2>&1 | grep -Eq 'version \"25(\\.|\\")' || { echo 'CPF_DEV=FAIL Java 25 is required' >&2; false; }
    "$GRADLE" -PcpfResourceProfile="$PROFILE" --no-daemon --no-parallel --continue cpfBuild ;;
  test)
    java -version 2>&1 | grep -Eq 'version \"25(\\.|\\")' || { echo 'CPF_DEV=FAIL Java 25 is required' >&2; false; }
    "$GRADLE" -PcpfResourceProfile="$PROFILE" --no-daemon --no-parallel --continue cpfTest ;;
  verify-fast)
    java -version 2>&1 | grep -Eq 'version \"25(\\.|\\")' || { echo 'CPF_DEV=FAIL Java 25 is required' >&2; false; }
    "$GRADLE" -PcpfResourceProfile="$PROFILE" --no-daemon --no-parallel --continue cpfVerifyFast ;;
  verify-full)
    command -v pwsh >/dev/null 2>&1 || { echo 'CPF_DEV=FAIL pwsh 7.x is required for verify-full' >&2; false; }
    pwsh -NoProfile -File "$VALIDATION" -ResourceProfile "$PROFILE" -OutputRoot "${HOME}/Downloads" -FullLocal
    ;;
  run-local)
    command -v pwsh >/dev/null 2>&1 || { echo 'CPF_DEV=FAIL pwsh 7.x is required for run-local' >&2; false; }
    pwsh -NoProfile -File "$RUNTIME_TOOLS/start-cpf-local.ps1" -ResourceProfile "$PROFILE" -Mode integrated
    ;;
  run-batch)
    command -v pwsh >/dev/null 2>&1 || { echo 'CPF_DEV=FAIL pwsh 7.x is required for run-batch' >&2; false; }
    pwsh -NoProfile -File "$RUNTIME_TOOLS/start-cpf-local.ps1" -ResourceProfile "$PROFILE" -Mode standard -BatchOnly
    ;;
  status) pwsh -NoProfile -File "$RUNTIME_TOOLS/status-cpf-local.ps1" ;;
  stop) pwsh -NoProfile -File "$RUNTIME_TOOLS/stop-cpf-local.ps1" ;;
  modules)
    java -version 2>&1 | grep -Eq 'version \"25(\\.|\\")' || { echo 'CPF_DEV=FAIL Java 25 is required' >&2; false; }
    "$GRADLE" -PcpfResourceProfile="$PROFILE" --no-daemon --no-parallel cpfModules ;;
  resource)
    java -version 2>&1 | grep -Eq 'version \"25(\\.|\\")' || { echo 'CPF_DEV=FAIL Java 25 is required' >&2; false; }
    "$GRADLE" -PcpfResourceProfile="$PROFILE" --no-daemon --no-parallel cpfResourcePolicy ;;
  *) echo "CPF_DEV=FAIL unsupported action: $ACTION" >&2; show_help; false ;;
esac
