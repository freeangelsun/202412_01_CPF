UPDATE bat_execution_lease
   SET lease_status = 'EXPIRED',
       released_at = SYSUTCDATETIME()
 WHERE execution_id = ?
   AND lease_token = ?
   AND fencing_token = ?
   AND lease_until < SYSUTCDATETIME()
   AND lease_status IN ('CLAIMED', 'RUNNING')
