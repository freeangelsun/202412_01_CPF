UPDATE bat_center_cut_item
   SET item_status = 'UNKNOWN_RESULT',
       completed_at = SYSUTCDATETIME(),
       last_error_message = 'Center-Cut lease expired; reconcile before retry',
       updated_at = SYSUTCDATETIME()
 WHERE center_cut_item_id = ?
   AND item_status = 'RUNNING'
