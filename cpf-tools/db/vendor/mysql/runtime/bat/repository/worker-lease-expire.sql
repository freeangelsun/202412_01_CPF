UPDATE bat_execution_lease
   SET lease_status = 'EXPIRED',
       released_at = CURRENT_TIMESTAMP(3)
 WHERE execution_id = ?
   AND lease_token = ?
   AND fencing_token = ?
   AND lease_until < CURRENT_TIMESTAMP(3)
   AND lease_status IN ('CLAIMED', 'RUNNING')
