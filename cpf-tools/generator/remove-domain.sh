#!/usr/bin/env sh
# Generator-owned 파일만 제거하는 Canonical CLI Adapter다.
set -eu; ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd); exec "$ROOT/cpf-tools/runtime/cli/cpf" domain remove "$@"
