SELECT *
FROM bat_operation_log
WHERE (? IS NULL OR job_id = ?)
  AND (? IS NULL OR execution_id = ?)
ORDER BY operation_id DESC
FETCH FIRST ? ROWS ONLY
