UPDATE BAT_CENTER_CUT_CLAIM
   SET claim_status = 'RELEASED',
       released_at = (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND claim_status = 'CLAIMED'
