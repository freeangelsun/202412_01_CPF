#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
args=()
while (($#)); do
  case "$1" in
    --domain-name) args+=(-DomainName "$2"); shift 2 ;;
    --system-code) args+=(-SystemCode "$2"); shift 2 ;;
    --database-host) args+=(-DatabaseHost "$2"); shift 2 ;;
    --database-port) args+=(-DatabasePort "$2"); shift 2 ;;
    --database-name) args+=(-DatabaseName "$2"); shift 2 ;;
    --database-username) args+=(-DatabaseUsername "$2"); shift 2 ;;
    --client-path) args+=(-ClientPath "$2"); shift 2 ;;
    --result-dir) args+=(-ResultDir "$2"); shift 2 ;;
    --root) args+=(-Root "$2"); shift 2 ;;
    --apply) args+=(-Apply); shift ;;
    *) printf 'Unsupported argument: %s\n' "$1" >&2; exit 2 ;;
  esac
done

if [[ -n "${CPF_DOMAIN_DB_PASSWORD:-}" ]]; then
  args+=(-DatabasePassword "$CPF_DOMAIN_DB_PASSWORD")
fi
exec pwsh -NoProfile -File "$script_dir/../../scripts/initialize-domain-database.ps1" "${args[@]}"
