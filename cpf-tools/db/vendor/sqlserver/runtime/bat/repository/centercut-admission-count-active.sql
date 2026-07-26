SELECT COUNT(*)
  FROM bat_center_cut_claim c
  JOIN bat_center_cut_item i
    ON i.center_cut_item_id = c.center_cut_item_id
 WHERE i.center_cut_execution_id = ?
   AND c.claim_status IN ('CLAIMED', 'RUNNING')
   AND c.lease_until >= SYSUTCDATETIME()
