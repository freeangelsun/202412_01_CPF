DELETE FROM BAT_LOCK
WHERE lock_key = ?
  AND expire_at < CURRENT_TIMESTAMP(3)
  AND row_version = ?
