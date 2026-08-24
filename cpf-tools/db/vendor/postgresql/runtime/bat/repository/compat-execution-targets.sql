SELECT t.*
FROM BAT_EXECUTION_TARGET t
JOIN BAT_EXECUTION e ON e.execution_id = t.execution_id
WHERE (? IS NULL OR e.job_id = ?)
  AND (? IS NULL OR t.dispatch_status = ?)
ORDER BY t.target_id DESC
LIMIT ?
