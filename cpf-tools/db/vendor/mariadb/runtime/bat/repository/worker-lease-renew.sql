UPDATE bat_execution_lease
   SET lease_until = ?,
       last_heartbeat_at = CURRENT_TIMESTAMP(3),
       lease_status = 'RUNNING'
 WHERE execution_id = ?
   AND worker_id = ?
   AND lease_token = ?
   AND fencing_token = ?
   AND lease_until >= CURRENT_TIMESTAMP(3)
   AND lease_status IN ('CLAIMED', 'RUNNING')
