SELECT payload_sha256, status_cd, owner_id, lease_until, expires_at, version_no
  FROM BAT_REMOTE_MESSAGE_LEDGER
 WHERE direction_cd = ?
   AND message_id = ?
