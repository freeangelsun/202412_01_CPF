UPDATE bat_center_cut_item
   SET item_status = ?,
       completed_at = CURRENT_TIMESTAMP(3),
       last_error_message = ?,
       updated_at = CURRENT_TIMESTAMP
 WHERE center_cut_item_id = ?
