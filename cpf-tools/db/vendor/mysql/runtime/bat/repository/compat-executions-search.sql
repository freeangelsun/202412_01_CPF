SELECT *
FROM bat_execution
WHERE (? IS NULL OR job_id = ?)
  AND (? IS NULL OR transaction_id = ?)
  AND (? IS NULL OR spring_batch_job_instance_id = ?)
  AND (? IS NULL OR worker_id = ?)
  AND (? IS NULL OR server_instance_id = ?)
  AND (? IS NULL OR created_at >= ?)
  AND (? IS NULL OR created_at < ?)
ORDER BY execution_id DESC
LIMIT ?
