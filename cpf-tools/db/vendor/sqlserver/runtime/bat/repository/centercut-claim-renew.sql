UPDATE bat_center_cut_claim
   SET lease_until = ?,
       last_heartbeat_at = SYSUTCDATETIME(),
       claim_status = 'RUNNING'
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND lease_until >= SYSUTCDATETIME()
   AND claim_status IN ('CLAIMED', 'RUNNING')
