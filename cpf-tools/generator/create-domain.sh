#!/usr/bin/env sh
# Linux/macOS Legacy wrapper도 cpf-tools/runtime/cli/cpf의 동일 OS-neutral Engine만 호출한다.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
if [ "$#" -eq 1 ] && [ -f "$1" ]; then exec "$ROOT/cpf-tools/runtime/cli/cpf" domain generate --file "$1"; fi
exec "$ROOT/cpf-tools/runtime/cli/cpf" domain generate "$@"
