SELECT COUNT(*)
  FROM bat_center_cut_item
 WHERE center_cut_execution_id = ?
   AND item_status IN ('READY', 'RETRY', 'RUNNING')
