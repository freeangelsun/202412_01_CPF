SELECT direction_cd, message_id, payload_sha256, status_cd, owner_id, lease_until,
       expires_at, attempt_no, last_error_cd, version_no, updated_at
  FROM bat_remote_message_ledger
 WHERE direction_cd = ?
   AND message_id = ?
