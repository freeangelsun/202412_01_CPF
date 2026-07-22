#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
args=()
while (($#)); do
  case "$1" in
    --domain-name) args+=(-DomainName "$2"); shift 2 ;;
    --system-code) args+=(-SystemCode "$2"); shift 2 ;;
    --module-name) args+=(-ModuleName "$2"); shift 2 ;;
    --base-package) args+=(-BasePackage "$2"); shift 2 ;;
    --table-prefix) args+=(-TablePrefix "$2"); shift 2 ;;
    --port) args+=(-Port "$2"); shift 2 ;;
    --db-vendor|--database-vendor) args+=(-DatabaseVendor "$2"); shift 2 ;;
    --capabilities) args+=(-Capabilities "$2"); shift 2 ;;
    --root) args+=(-Root "$2"); shift 2 ;;
    --dry-run) args+=(-DryRun); shift ;;
    --apply) args+=(-Apply); shift ;;
    --allow-reserved) args+=(-AllowReserved); shift ;;
    --help|-h) args+=(-Help); shift ;;
    *) printf 'Unsupported argument: %s\n' "$1" >&2; exit 2 ;;
  esac
done

exec pwsh -NoProfile -File "$script_dir/../../scripts/create-domain.ps1" "${args[@]}"
