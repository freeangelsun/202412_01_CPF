#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
ACTION="${1:-build}"
if [[ "$ACTION" == "build" || "$ACTION" == "check" || "$ACTION" == "status" || "$ACTION" == "setup" ]]; then
  shift || true
else
  ACTION="build"
fi
if command -v python3 >/dev/null 2>&1; then PYTHON=python3
elif command -v python >/dev/null 2>&1; then PYTHON=python
else echo 'CPF_OPEN_GIT=FAIL Python 3 is required' >&2; exit 127
fi
exec "$PYTHON" "$SCRIPT_DIR/cpf_open_git.py" "$ACTION" --root "$ROOT" "$@"
