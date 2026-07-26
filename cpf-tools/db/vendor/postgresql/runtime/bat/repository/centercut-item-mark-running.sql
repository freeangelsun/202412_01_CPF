UPDATE bat_center_cut_item
   SET item_status = 'RUNNING',
       started_at = COALESCE(started_at, CURRENT_TIMESTAMP(3)),
       updated_at = CURRENT_TIMESTAMP
 WHERE center_cut_item_id = ?
   AND item_status IN ('READY', 'RETRY')
