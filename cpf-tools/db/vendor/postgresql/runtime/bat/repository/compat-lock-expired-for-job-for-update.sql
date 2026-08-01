SELECT *
FROM bat_lock
WHERE job_id = ?
  AND expire_at < CURRENT_TIMESTAMP(3)
FOR UPDATE
