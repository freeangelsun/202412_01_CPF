#!/usr/bin/env sh
# Linux/Jenkins thin wrapper. 실제 설치·기동 계약은 cpf-instance.py 하나가 소유합니다.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
exec python3 "$ROOT/deploy/tools/cpf-instance.py" "$@"
