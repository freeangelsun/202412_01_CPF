UPDATE bat_center_cut_claim
   SET claim_status = 'RELEASED',
       released_at = CURRENT_TIMESTAMP(6)
 WHERE center_cut_item_id = ?
   AND runner_id = ?
   AND claim_token = ?
   AND fencing_token = ?
   AND claim_status = 'CLAIMED'
