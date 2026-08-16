SELECT t.*
FROM bat_execution_target t
JOIN bat_execution e ON e.execution_id = t.execution_id
WHERE (? IS NULL OR e.job_id = ?)
  AND (? IS NULL OR t.dispatch_status = ?)
ORDER BY t.target_id DESC
LIMIT ?
