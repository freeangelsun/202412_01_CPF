SELECT *
FROM bat_lock
WHERE (? IS NULL OR job_id = ?)
ORDER BY locked_at DESC
