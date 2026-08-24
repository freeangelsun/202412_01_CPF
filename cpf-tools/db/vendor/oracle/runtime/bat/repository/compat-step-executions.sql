SELECT s.*
FROM BAT_STEP_EXECUTION s
JOIN BAT_EXECUTION e ON e.execution_id = s.execution_id
WHERE (? IS NULL OR s.execution_id = ?)
  AND (? IS NULL OR e.job_id = ?)
ORDER BY s.step_execution_id DESC
FETCH FIRST ? ROWS ONLY
