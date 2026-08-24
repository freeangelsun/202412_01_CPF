UPDATE BAT_CENTER_CUT_ITEM
   SET item_status = ?,
       retry_count = retry_count + CASE WHEN ? = 'RETRY' THEN 1 ELSE 0 END,
       completed_at = CASE WHEN ? = 'RETRY' THEN NULL ELSE CURRENT_TIMESTAMP(3) END,
       last_error_message = ?,
       updated_at = CURRENT_TIMESTAMP
 WHERE center_cut_item_id = ?
   AND item_status = 'RUNNING'
