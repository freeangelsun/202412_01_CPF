UPDATE BAT_CENTER_CUT_CLAIM
   SET lease_until = TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6)),
       last_heartbeat_at = UTC_TIMESTAMP(6),
       claim_status = 'RUNNING'
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND lease_until >= UTC_TIMESTAMP(6)
   AND claim_status IN ('CLAIMED', 'RUNNING')
