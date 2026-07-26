UPDATE bat_execution_lease
   SET lease_status = 'RELEASED',
       released_at = SYSUTCDATETIME(),
       failure_message = ?
 WHERE execution_id = ?
   AND worker_id = ?
   AND lease_token = ?
   AND fencing_token = ?
   AND lease_status IN ('CLAIMED', 'RUNNING')
