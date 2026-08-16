#!/usr/bin/env sh
# Windows와 동일 verify all surface를 호출한다.
set -eu; ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd); exec "$ROOT/cpf-tools/runtime/cli/cpf" verify all
