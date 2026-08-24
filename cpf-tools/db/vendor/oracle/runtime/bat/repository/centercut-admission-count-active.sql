SELECT COUNT(*)
  FROM BAT_CENTER_CUT_CLAIM c
  JOIN BAT_CENTER_CUT_ITEM i
    ON i.center_cut_item_id = c.center_cut_item_id
 WHERE i.center_cut_execution_id = ?
   AND c.claim_status IN ('CLAIMED', 'RUNNING')
   AND c.lease_until >= CURRENT_TIMESTAMP(6)
