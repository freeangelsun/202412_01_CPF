UPDATE bat_execution_lease
   SET worker_id = ?,
       lease_token = ?,
       lease_status = 'CLAIMED',
       claimed_at = ?,
       lease_until = ?,
       last_heartbeat_at = ?,
       attempt_no = attempt_no + 1,
       takeover_count = takeover_count + 1,
       fencing_token = ?,
       released_at = NULL,
       failure_message = NULL
 WHERE execution_id = ?
   AND lease_status IN ('RELEASED', 'EXPIRED')
