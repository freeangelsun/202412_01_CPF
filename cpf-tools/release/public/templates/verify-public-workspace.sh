#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${CPF_MAVEN_REPOSITORY_URL:?CPF_MAVEN_REPOSITORY_URL is required}"
: "${CPF_VERSION:?CPF_VERSION is required}"
if [ ! -f "$ROOT/settings.gradle" ] || [ ! -d "$ROOT/domains" ]; then
  echo '[CPF][PUBLIC] workspace root/domain catalog missing' >&2
  exit 2
fi
"$ROOT/gradlew" cpfVerify --no-daemon
if [ -f "$ROOT/cpf-backoffice-web/frontend/package.json" ]; then
  command -v npm >/dev/null 2>&1 || { echo '[CPF][PUBLIC] npm is required for Backoffice Web frontend' >&2; exit 3; }
  (cd "$ROOT/cpf-backoffice-web/frontend" && npm ci --ignore-scripts && npm run verify)
fi
echo '[CPF][PUBLIC] WORKSPACE_VERIFY=PASS'
