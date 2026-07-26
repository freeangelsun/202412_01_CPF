UPDATE bat_center_cut_item
   SET item_status = ?,
       completed_at = SYSUTCDATETIME(),
       last_error_message = ?,
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_item_id = ?
