#!/usr/bin/env bash
set -euo pipefail
[[ "${EUID}" -eq 0 ]] || { echo 'root required' >&2; exit 10; }
BASE="$(cd "$(dirname "$0")/.." && pwd)"; CPF_USER="${CPF_USER:-cpf}"
id "$CPF_USER" >/dev/null 2>&1 || useradd --system --home /opt/cpf --shell /usr/sbin/nologin "$CPF_USER"
install -d -m 0750 /etc/cpf /opt/cpf/bin
for service in cpf-batch-control-server cpf-batch-scheduler cpf-batch-worker cpf-center-cut-runner cpf-batch-host-agent; do
  install -d -o "$CPF_USER" -g "$CPF_USER" "/opt/cpf/$service/releases" "/opt/cpf/$service/config" "/opt/cpf/$service/work" "/var/log/cpf/$service"
  install -m 0755 "$BASE/bin/$service.sh" "/opt/cpf/bin/$service.sh"
  install -m 0644 "$BASE/systemd/$service.service" "/etc/systemd/system/$service.service"
  if [[ -f "$BASE/config/$service.properties" && ! -f "/opt/cpf/$service/config/$service.properties" ]]; then
    install -o "$CPF_USER" -g "$CPF_USER" -m 0640 "$BASE/config/$service.properties" "/opt/cpf/$service/config/$service.properties"
  fi
done
install -m 0755 "$BASE/bin/cpf-runtime.sh" /opt/cpf/bin/cpf-runtime.sh
systemctl daemon-reload
systemctl enable cpf-batch-host-agent.service
printf '%s\n' 'BAT managed-service layout installed. Place signed Host Agent artifact, set current.version, provision /etc/cpf/*.env, then start Host Agent.'
