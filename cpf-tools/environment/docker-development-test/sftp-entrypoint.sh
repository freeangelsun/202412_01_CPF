#!/bin/sh
set -eu

user_name="${SFTP_USER:-cpf-sftp}"
password_file="/run/secrets/sftp_password"

if [ ! -s "$password_file" ]; then
  echo "SFTP password secret is missing" >&2
  exit 1
fi

if ! echo "$user_name" | grep -Eq '^[a-z_][a-z0-9_-]{0,31}$'; then
  echo "Invalid SFTP user name" >&2
  exit 1
fi

if ! id "$user_name" >/dev/null 2>&1; then
  addgroup -S cpf-sftp
  adduser -D -h "/home/$user_name" -s /bin/sh -G cpf-sftp "$user_name"
fi

printf '%s:%s\n' "$user_name" "$(tr -d '\r\n' < "$password_file")" | chpasswd
chown root:root "/home/$user_name"
chmod 0755 "/home/$user_name"
mkdir -p "/home/$user_name/exchange/inbound" \
         "/home/$user_name/exchange/outbound" \
         "/home/$user_name/exchange/ack" \
         "/home/$user_name/exchange/error" \
         "/home/$user_name/exchange/archive"
chown -R "$user_name:cpf-sftp" "/home/$user_name/exchange"
chmod 0750 "/home/$user_name/exchange"

cat > /etc/ssh/sshd_config <<CONFIG
Port 22
Protocol 2
HostKey /etc/ssh/ssh_host_rsa_key
HostKey /etc/ssh/ssh_host_ecdsa_key
HostKey /etc/ssh/ssh_host_ed25519_key
PasswordAuthentication yes
KbdInteractiveAuthentication no
AuthenticationMethods password
LogLevel VERBOSE
PermitEmptyPasswords no
PermitRootLogin no
Subsystem sftp internal-sftp
AllowUsers $user_name
Match User $user_name
    ChrootDirectory /home/%u
    ForceCommand internal-sftp -d /exchange
    X11Forwarding no
    AllowTcpForwarding no
    PermitTunnel no
CONFIG

/usr/sbin/sshd -t -f /etc/ssh/sshd_config
exec /usr/sbin/sshd -D -e -f /etc/ssh/sshd_config
