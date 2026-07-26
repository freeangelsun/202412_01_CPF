SELECT *
FROM bat_operation_log
WHERE (? IS NULL OR job_id = ?)
  AND (? IS NULL OR execution_id = ?)
ORDER BY operation_id DESC
OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
