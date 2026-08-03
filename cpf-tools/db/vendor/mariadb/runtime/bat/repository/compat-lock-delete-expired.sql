DELETE FROM bat_lock
WHERE lock_key = ?
  AND expire_at < CURRENT_TIMESTAMP(3)
  AND row_version = ?
