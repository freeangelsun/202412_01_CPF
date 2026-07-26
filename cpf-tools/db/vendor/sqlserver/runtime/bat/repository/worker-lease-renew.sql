UPDATE bat_execution_lease
   SET lease_until = ?,
       last_heartbeat_at = SYSUTCDATETIME(),
       lease_status = 'RUNNING'
 WHERE execution_id = ?
   AND worker_id = ?
   AND lease_token = ?
   AND fencing_token = ?
   AND lease_until >= SYSUTCDATETIME()
   AND lease_status IN ('CLAIMED', 'RUNNING')
