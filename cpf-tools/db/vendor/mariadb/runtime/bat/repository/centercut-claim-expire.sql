UPDATE BAT_CENTER_CUT_CLAIM
   SET claim_status = 'EXPIRED',
       released_at = UTC_TIMESTAMP(6)
 WHERE center_cut_item_id = ?
   AND claim_status IN ('CLAIMED', 'RUNNING')
   AND lease_until < UTC_TIMESTAMP(6)
