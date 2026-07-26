DELETE FROM bat_lock
WHERE lock_key = ?
  AND expire_at < SYSUTCDATETIME()
