#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
exec java "$ROOT/bin/CpfGeneratorLauncher.java" --root "$ROOT" domain sync "$@"
