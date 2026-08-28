#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
PYTHON=$(command -v python3 || command -v python || true)
[ -n "$PYTHON" ] || { echo 'CPF_LOCAL_RUNTIME=FAIL Python 3 is required' >&2; exit 69; }
exec "$PYTHON" "$ROOT/cpf-tools/runtime/tools/cpf_local_runtime.py" stop --root "$ROOT" "$@"
