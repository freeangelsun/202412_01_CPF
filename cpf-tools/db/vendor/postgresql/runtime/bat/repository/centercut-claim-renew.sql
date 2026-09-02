UPDATE BAT_CENTER_CUT_CLAIM
   SET lease_until = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC') + (? * INTERVAL '1 microsecond'),
       last_heartbeat_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC'),
       claim_status = 'RUNNING'
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND lease_until >= (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
   AND claim_status IN ('CLAIMED', 'RUNNING')
