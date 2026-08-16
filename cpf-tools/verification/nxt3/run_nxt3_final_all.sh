#!/usr/bin/env sh
# 모든 Gate를 끝까지 실행하는 OS-neutral Python 검증 Core를 호출하는 Linux thin launcher입니다.
set +e
ROOT=${1:-.}
shift 2>/dev/null || true
exec python3 "$ROOT/cpf-tools/verification/nxt3/run_nxt3_final_all.py" --root "$ROOT" "$@"
