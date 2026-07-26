SELECT l.execution_id,
       l.worker_id,
       l.lease_token,
       l.fencing_token,
       l.lease_status,
       e.execution_status
  FROM bat_execution_lease l
  JOIN bat_execution e
    ON e.execution_id = l.execution_id
 WHERE l.lease_status IN ('CLAIMED', 'RUNNING')
   AND l.lease_until < SYSUTCDATETIME()
 ORDER BY l.execution_id
