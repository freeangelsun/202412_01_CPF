#!/usr/bin/env sh
# 동일 Python lifecycle Gate를 Linux/macOS에서 실행한다.
set -eu; ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd); exec python3 "$ROOT/cpf-tools/verification/nxt3/cpf_nxt3_generator_gate.py" --root "$ROOT"
