#!/usr/bin/env bash
set -euo pipefail
repo=${1:?repository root required}
cd "$repo"
for n in CPF_XA_VENDOR1 CPF_XA_URL1 CPF_XA_USER1 CPF_XA_PASSWORD1 CPF_XA_VENDOR2 CPF_XA_URL2 CPF_XA_USER2 CPF_XA_PASSWORD2 CPF_XA_TRANSACTION_ID CPF_XA_INSERT_SQL CPF_XA_COUNT_SQL; do
  [[ -n "${!n:-}" ]] || { echo "Missing environment variable: $n" >&2; exit 2; }
done
props=(
 "-Dcpf.xa.harness.vendor1=$CPF_XA_VENDOR1" "-Dcpf.xa.harness.url1=$CPF_XA_URL1" "-Dcpf.xa.harness.user1=$CPF_XA_USER1" "-Dcpf.xa.harness.password1=$CPF_XA_PASSWORD1"
 "-Dcpf.xa.harness.vendor2=$CPF_XA_VENDOR2" "-Dcpf.xa.harness.url2=$CPF_XA_URL2" "-Dcpf.xa.harness.user2=$CPF_XA_USER2" "-Dcpf.xa.harness.password2=$CPF_XA_PASSWORD2"
 "-Dcpf.xa.harness.transaction-id=$CPF_XA_TRANSACTION_ID" "-Dcpf.xa.harness.insert-sql=$CPF_XA_INSERT_SQL" "-Dcpf.xa.harness.count-sql=$CPF_XA_COUNT_SQL"
)
set +e
./gradlew "${props[@]}" :cpf-starter-data-transaction-jta:runXaCrashHarness -PcpfXaHarnessMode=prepare-kill
code=$?
set -e
[[ $code -eq 73 ]] || { echo "prepare-kill expected 73, got $code" >&2; exit 3; }
./gradlew "${props[@]}" :cpf-starter-data-transaction-jta:runXaCrashHarness -PcpfXaHarnessMode=recover
