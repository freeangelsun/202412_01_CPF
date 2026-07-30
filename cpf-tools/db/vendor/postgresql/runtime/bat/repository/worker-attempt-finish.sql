UPDATE bat_execution_attempt
   SET attempt_status = ?,
       result_message = ?,
       finished_at = CURRENT_TIMESTAMP
 WHERE execution_id = ?
   AND attempt_no = ?
   AND worker_id = ?
   AND fencing_token = ?
   AND attempt_status = 'RUNNING'
