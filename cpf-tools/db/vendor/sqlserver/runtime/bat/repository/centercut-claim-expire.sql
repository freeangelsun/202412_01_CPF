UPDATE bat_center_cut_claim
   SET claim_status = 'EXPIRED',
       released_at = SYSUTCDATETIME()
 WHERE center_cut_item_id = ?
   AND claim_status IN ('CLAIMED', 'RUNNING')
   AND lease_until < SYSUTCDATETIME()
