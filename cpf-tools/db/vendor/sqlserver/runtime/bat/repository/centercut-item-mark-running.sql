UPDATE bat_center_cut_item
   SET item_status = 'RUNNING',
       started_at = COALESCE(started_at, SYSUTCDATETIME()),
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_item_id = ?
   AND item_status IN ('READY', 'RETRY')
