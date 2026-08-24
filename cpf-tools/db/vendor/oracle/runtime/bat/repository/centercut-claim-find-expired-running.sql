SELECT c.center_cut_item_id,
       i.center_cut_execution_id
  FROM BAT_CENTER_CUT_CLAIM c
  JOIN BAT_CENTER_CUT_ITEM i
    ON i.center_cut_item_id = c.center_cut_item_id
 WHERE c.claim_status IN ('CLAIMED', 'RUNNING')
   AND c.lease_until < CURRENT_TIMESTAMP(6)
   AND i.item_status = 'RUNNING'
