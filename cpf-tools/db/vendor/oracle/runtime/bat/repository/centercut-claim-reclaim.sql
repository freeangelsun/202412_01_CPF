UPDATE bat_center_cut_claim
   SET runner_id = ?,
       pool_id = ?,
       claim_token = ?,
       claim_status = 'CLAIMED',
       fencing_token = ?,
       lease_until = ?,
       last_heartbeat_at = ?,
       attempt_no = attempt_no + 1,
       takeover_count = takeover_count + 1,
       released_at = NULL
 WHERE center_cut_item_id = ?
   AND claim_status IN ('RELEASED', 'EXPIRED')
