SELECT COUNT(*)
  FROM BAT_CENTER_CUT_ITEM
 WHERE center_cut_execution_id = ?
   AND item_status IN ('READY', 'RETRY', 'RUNNING')
