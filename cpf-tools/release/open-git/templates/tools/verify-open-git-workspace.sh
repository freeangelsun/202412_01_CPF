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
# Fresh Consumer 는 checkout 만으로 Gradle 을 실행할 수 있어야 한다. 경로 존재만 확인하면
# gradlew 가 fail-closed 로 요구하는 resource policy 누락 같은 실행 결함을 놓친다.
for resource in gradle/cpf-runtime/common.properties gradle/cpf-runtime/local.properties; do
  [ -f "$ROOT/$resource" ] || { echo "[CPF][OPEN-GIT][FAIL] required gradle resource policy missing: $resource" >&2; exit 1; }
done
ISOLATED_GRADLE_HOME=$(mktemp -d)
trap 'rm -rf "$ISOLATED_GRADLE_HOME"' EXIT
GRADLE_USER_HOME="$ISOLATED_GRADLE_HOME" "$ROOT/gradlew" --no-daemon --console=plain -q projects   || { echo "[CPF][OPEN-GIT][FAIL] isolated Gradle consumer run failed" >&2; exit 1; }
echo "CPF_OPEN_GIT_ISOLATED_GRADLE=PASS"
echo "CPF_OPEN_GIT_WORKSPACE=PASS DOMAIN_COUNT=$count DOMAIN_STATE=$([[ $count -eq 0 ]] && echo NOT_SELECTED || echo SELECTED)"
