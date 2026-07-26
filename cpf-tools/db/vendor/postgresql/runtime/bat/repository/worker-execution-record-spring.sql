UPDATE bat_execution
   SET spring_batch_execution_id = ?,
       spring_batch_job_instance_id = ?,
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
