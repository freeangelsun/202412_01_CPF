#!/usr/bin/env sh
# canonical entrypoint 는 bin/cpf 하나다. 이 script 는 하위 호환 thin wrapper 이며
# 자체 명령 해석을 하지 않는다. 자체 해석을 넣으면 OS 사이 의미가 갈라진다.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# 예전 이 script 는 자기만의 명령 목록을 갖고 있었고 canonical CLI 와 어긋났다.
# (예: runtime 명령이 없어 bin/cpf-start.sh 가 Linux 에서 항상 실패했다)
exec "$ROOT/cpf" "$@"
