UPDATE bat_execution
   SET execution_status = ?,
       end_time = CURRENT_TIMESTAMP(3),
       error_message = ?,
       last_heartbeat_at = CURRENT_TIMESTAMP(3),
       updated_at = CURRENT_TIMESTAMP
 WHERE execution_id = ?
   AND execution_status = 'RUNNING'
   AND EXISTS (
       SELECT 1
         FROM bat_execution_lease l
        WHERE l.execution_id = bat_execution.execution_id
          AND l.worker_id = ?
          AND l.lease_token = ?
          AND l.fencing_token = ?
          AND l.lease_status IN ('CLAIMED', 'RUNNING')
          AND l.lease_until >= CURRENT_TIMESTAMP(3)
   )
