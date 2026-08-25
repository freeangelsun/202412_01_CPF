#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

showHelp() {
  cat <<'HELP'
CPF 명령
  cpf bootstrap [옵션]                     로컬 개발 환경을 자동 준비합니다.
  cpf stop                                 CPF 로컬 Runtime을 종료합니다.
  cpf reset --confirm                      CPF 로컬 개발 환경을 안전하게 초기화합니다.
  cpf build                                전체 Build를 실행합니다.
  cpf test                                 전체 Test를 실행합니다.
  cpf verify                               Open Git Workspace 계약을 검증합니다.
  cpf domain new <name> <SYSTEM_CODE>      신규 Business Domain을 생성합니다.
  cpf domain sync                          Generated Domain을 정본과 동기화합니다.
  cpf library create <name>                고객사 공통 JAR 작업공간을 생성합니다.
  cpf library attach <name> <domain>       선택한 Domain에만 고객사 공통 JAR를 연결합니다.
  cpf library sync                         고객사 공통 JAR 연결 정보를 재동기화합니다.
  cpf library verify <name>                고객사 공통 JAR 구조와 경계를 검증합니다.
  cpf help                                 이 도움말을 표시합니다.

고객사 공통 Library는 모든 Domain에 자동 주입하지 않습니다. 필요한 Domain에서만 attach 하여
의존성 경계를 명확하게 유지합니다.
HELP
}

cpfBuild() { "$ROOT/bin/cpf-build.sh" "$@"; }
cpfTest() { "$ROOT/bin/cpf-test.sh" "$@"; }
cpfVerify() { "$ROOT/tools/verify-open-git-workspace.sh" "$@"; }

runGenerator() {
  exec java "$ROOT/bin/CpfGeneratorLauncher.java" --root "$ROOT" "$@"
}

command=${1:-help}
[ "$#" -eq 0 ] || shift
case "$command" in
  help|-h|--help) showHelp; exit 0 ;;
  bootstrap) "$ROOT/bin/cpf-bootstrap.sh" "$@" ;;
  stop) "$ROOT/bin/cpf-stop.sh" "$@" ;;
  reset) "$ROOT/bin/cpf-reset.sh" "$@" ;;
  build) cpfBuild "$@" ;;
  test) cpfTest "$@" ;;
  verify) cpfVerify "$@" ;;
  domain)
    sub=${1:-}; [ "$#" -eq 0 ] || shift
    case "$sub" in
      new)
        [ "$#" -ge 2 ] || { echo '사용법: cpf domain new <name> <SYSTEM_CODE>' >&2; exit 2; }
        name=$1; code=$2; shift 2
        runGenerator domain create --name "$name" --system-code "$code" "$@"
        ;;
      sync) runGenerator domain sync "$@" ;;
      *) echo "지원하지 않는 domain 명령입니다: $sub" >&2; showHelp >&2; exit 2 ;;
    esac
    ;;
  library)
    sub=${1:-}; [ "$#" -eq 0 ] || shift
    case "$sub" in
      create)
        [ "$#" -ge 1 ] || { echo '사용법: cpf library create <name>' >&2; exit 2; }
        name=$1; shift; runGenerator library create --name "$name" "$@"
        ;;
      attach)
        [ "$#" -ge 2 ] || { echo '사용법: cpf library attach <name> <domain>' >&2; exit 2; }
        name=$1; domain=$2; shift 2; runGenerator library attach --name "$name" --domain "$domain" "$@"
        ;;
      sync) runGenerator library sync "$@" ;;
      verify)
        [ "$#" -ge 1 ] || { echo '사용법: cpf library verify <name>' >&2; exit 2; }
        name=$1; shift; runGenerator library verify --name "$name" "$@"
        ;;
      *) echo "지원하지 않는 library 명령입니다: $sub" >&2; showHelp >&2; exit 2 ;;
    esac
    ;;
  *) echo "CPF COMMAND FAILED: 지원하지 않는 명령입니다: $command" >&2; showHelp >&2; exit 2 ;;
esac
rc=$?
if [ "$rc" -eq 0 ]; then
  echo "CPF Command Result: PASS"
  [ "$command" = "bootstrap" ] && echo "CPF LOCAL DEVELOPMENT READY"
else
  echo "CPF COMMAND FAILED: exit=$rc" >&2
fi
exit "$rc"
