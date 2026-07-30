UPDATE bat_execution_attempt
   SET attempt_status = ?,
       result_message = ?,
       executor_type = ?,
       exit_code = ?,
       stdout_text = ?,
       stderr_text = ?,
       output_truncated_yn = ?,
       duration_ms = ?,
       artifact_hash = ?,
       unknown_result_yn = ?,
       finished_at = CURRENT_TIMESTAMP
 WHERE execution_id = ?
   AND attempt_no = ?
   AND worker_id = ?
   AND fencing_token = ?
   AND attempt_status = 'RUNNING'
