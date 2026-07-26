SELECT operation_id, job_id, execution_id, operation_type, operator_id, reason,
       result_type, result_message, created_at
FROM bat_operation_log
ORDER BY operation_id DESC
FETCH FIRST 500 ROWS ONLY
