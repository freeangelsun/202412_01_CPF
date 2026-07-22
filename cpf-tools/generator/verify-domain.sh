#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
args=()
while (($#)); do
  case "$1" in
    --domain-name) args+=(-DomainName "$2"); shift 2 ;;
    --system-code) args+=(-SystemCode "$2"); shift 2 ;;
    --result-dir) args+=(-ResultDir "$2"); shift 2 ;;
    --root) args+=(-Root "$2"); shift 2 ;;
    --skip-build) args+=(-SkipBuild); shift ;;
    *) printf 'Unsupported argument: %s\n' "$1" >&2; exit 2 ;;
  esac
done

exec pwsh -NoProfile -File "$script_dir/../../scripts/verify-domain.ps1" "${args[@]}"
