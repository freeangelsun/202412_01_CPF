UPDATE bat_center_cut_item
   SET item_status = 'UNKNOWN_RESULT',
       completed_at = CURRENT_TIMESTAMP(3),
       last_error_message = 'Center-Cut lease expired; reconcile before retry',
       updated_at = CURRENT_TIMESTAMP
 WHERE center_cut_item_id = ?
   AND item_status = 'RUNNING'
