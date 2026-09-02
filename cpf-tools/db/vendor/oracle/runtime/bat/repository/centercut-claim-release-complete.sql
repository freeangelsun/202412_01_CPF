UPDATE BAT_CENTER_CUT_CLAIM
   SET claim_status = 'RELEASED',
       released_at = SYS_EXTRACT_UTC(SYSTIMESTAMP)
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND claim_status IN ('CLAIMED', 'RUNNING')
   AND lease_until >= SYS_EXTRACT_UTC(SYSTIMESTAMP)
