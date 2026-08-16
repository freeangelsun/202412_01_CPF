#!/usr/bin/env sh
# Linux/Jenkins용 wrapper. 실제 배포 산출물 규칙은 OS-neutral Python 정본 하나만 사용합니다.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
ENVIRONMENT=${1:-dev}
TOPOLOGY=${2:-single-node}
if [ "$#" -gt 0 ]; then shift; fi
if [ "$#" -gt 0 ]; then shift; fi
exec python3 "$ROOT/deploy/tools/prepare-distribution.py" --root "$ROOT" --env "$ENVIRONMENT" --topology "$TOPOLOGY" "$@"
