#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
for required in cpf-education bin; do
  [[ -e "$ROOT/$required" ]] || { echo "[CPF][OPEN-GIT][FAIL] required path missing: $required" >&2; exit 1; }
done
count=0
for contract in "$ROOT"/cpf-*/gradle.properties; do
  [[ -f "$contract" ]] || continue
  grep -q '^cpf\.domain\.contractVersion=1$' "$contract" || continue
  project="$(dirname "$contract")"
  name="$(sed -n 's/^cpf\.domain\.name=//p' "$contract" | head -1)"
  [[ "$(basename "$project")" == "cpf-$name" ]] || { echo "[CPF][OPEN-GIT][FAIL] domain root/name mismatch: $project" >&2; exit 1; }
  [[ ! -e "$project/cpf-domain.yaml" && ! -e "$project/cpf-generator.lock.json" ]] || { echo "[CPF][OPEN-GIT][FAIL] generator metadata leaked: $project" >&2; exit 1; }
  count=$((count+1))
done
if [[ -f "$ROOT/cpf-backoffice-web/frontend/package.json" ]]; then
  command -v npm >/dev/null 2>&1 || { echo '[CPF][OPEN-GIT][FAIL] npm missing for selected Backoffice frontend' >&2; exit 69; }
  (cd "$ROOT/cpf-backoffice-web/frontend" && npm ci --ignore-scripts && npm run verify)
fi
echo "CPF_OPEN_GIT_WORKSPACE=PASS DOMAIN_COUNT=$count DOMAIN_STATE=$([[ $count -eq 0 ]] && echo NOT_SELECTED || echo SELECTED)"
