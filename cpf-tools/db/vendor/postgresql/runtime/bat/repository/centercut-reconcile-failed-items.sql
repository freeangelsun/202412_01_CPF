UPDATE bat_center_cut_item
SET item_status = 'RETRY',
    retry_count = retry_count + 1,
    completed_at = NULL,
    last_error_message = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE center_cut_execution_id = ?
  AND item_status = 'FAILED'
