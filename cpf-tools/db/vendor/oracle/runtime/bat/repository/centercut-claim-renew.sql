UPDATE BAT_CENTER_CUT_CLAIM
   SET lease_until = SYS_EXTRACT_UTC(SYSTIMESTAMP) + NUMTODSINTERVAL(? / 1000000, 'SECOND'),
       last_heartbeat_at = SYS_EXTRACT_UTC(SYSTIMESTAMP),
       claim_status = 'RUNNING'
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND lease_until >= SYS_EXTRACT_UTC(SYSTIMESTAMP)
   AND claim_status IN ('CLAIMED', 'RUNNING')
