#!/usr/bin/env sh
# Compatibility wrapper. Official Tooling Interface is `cpf`.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../../.." && pwd)
CLI="$ROOT/cpf-tools/runtime/cli/cpf"
ACTION=${1:-help}; shift || true
case "$ACTION" in
  help) exec "$CLI" help ;;
  build) exec "$CLI" build "$@" ;;
  test) exec "$CLI" test "$@" ;;
  verify-fast) exec "$CLI" verify all "$@" ;;
  verify-targeted) exec "$CLI" dev targeted-test "$@" ;;
  verify-full) exec "$CLI" dev full-validation "$@" ;;
  run-local) exec "$CLI" run "$@" ;;
  run-batch) exec "$CLI" dev run-batch "$@" ;;
  status) exec "$CLI" status "$@" ;;
  stop) exec "$CLI" stop "$@" ;;
  modules) exec "$CLI" dev modules "$@" ;;
  resource) exec "$CLI" dev resource "$@" ;;
  *) echo "CPF_DEV=FAIL unsupported action=$ACTION; use 'cpf help'" >&2; exit 2 ;;
esac
