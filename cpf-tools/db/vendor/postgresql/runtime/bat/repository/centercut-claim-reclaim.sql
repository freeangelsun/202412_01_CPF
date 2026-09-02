UPDATE BAT_CENTER_CUT_CLAIM
   SET runner_id = ?,
       pool_id = ?,
       claim_token = ?,
       claim_status = 'CLAIMED',
       fencing_token = ?,
       lease_until = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC') + (? * INTERVAL '1 microsecond'),
       last_heartbeat_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC'),
       attempt_no = attempt_no + 1,
       takeover_count = takeover_count + 1,
       released_at = NULL
 WHERE center_cut_item_id = ?
   AND claim_status IN ('RELEASED', 'EXPIRED')
