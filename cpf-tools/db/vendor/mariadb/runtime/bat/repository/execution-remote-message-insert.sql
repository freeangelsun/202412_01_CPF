INSERT INTO bat_remote_message_ledger (
  direction_cd, message_id, payload_sha256, status_cd, owner_id,
  lease_until, expires_at, attempt_no, created_at, updated_at, version_no
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
