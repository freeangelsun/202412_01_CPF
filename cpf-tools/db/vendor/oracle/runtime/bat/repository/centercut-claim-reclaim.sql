UPDATE BAT_CENTER_CUT_CLAIM
   SET runner_id = ?,
       pool_id = ?,
       claim_token = ?,
       claim_status = 'CLAIMED',
       fencing_token = ?,
       lease_until = SYS_EXTRACT_UTC(SYSTIMESTAMP) + NUMTODSINTERVAL(? / 1000000, 'SECOND'),
       last_heartbeat_at = SYS_EXTRACT_UTC(SYSTIMESTAMP),
       attempt_no = attempt_no + 1,
       takeover_count = takeover_count + 1,
       released_at = NULL
 WHERE center_cut_item_id = ?
   AND claim_status IN ('RELEASED', 'EXPIRED')
