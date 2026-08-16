#!/usr/bin/env sh
# Upgrade도 동일 OS-neutral Engine의 regenerate 경로를 호출한다.
set -eu; ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd); exec "$ROOT/cpf-tools/runtime/cli/cpf" domain upgrade "$@"
