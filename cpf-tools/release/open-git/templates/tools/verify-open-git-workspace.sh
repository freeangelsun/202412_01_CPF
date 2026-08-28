#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${CPF_MAVEN_REPOSITORY_URL:?CPF_MAVEN_REPOSITORY_URL is required}"
: "${CPF_VERSION:?CPF_VERSION is required}"
for required in cpf-member cpf-external cpf-backoffice cpf-backoffice-web cpf-education bin; do
  [[ -e "$ROOT/$required" ]] || { echo "CPF Open Git required path missing: $required" >&2; exit 1; }
done
if find "$ROOT" -type f \( -name '*.jar' -o -name '*.war' \) ! -path "$ROOT/gradle/wrapper/gradle-wrapper.jar" -print -quit | grep -q .; then
  echo 'CPF Open Git Source Workspace must not contain accumulated CPF/application JAR/WAR.' >&2
  exit 1
fi
chmod +x "$ROOT/gradlew"
"$ROOT/gradlew" cpfVerify --no-daemon
if [[ -f "$ROOT/cpf-backoffice-web/frontend/package.json" ]]; then
  command -v npm >/dev/null 2>&1 || { echo 'npm is required for Backoffice Web frontend verification.' >&2; exit 1; }
  (cd "$ROOT/cpf-backoffice-web/frontend" && npm ci --ignore-scripts && npm run verify)
fi
echo '[CPF][OPEN-GIT] WORKSPACE_VERIFY=PASS'
