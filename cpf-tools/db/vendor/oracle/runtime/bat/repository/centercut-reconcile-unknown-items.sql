UPDATE BAT_CENTER_CUT_ITEM
SET item_status = 'RETRY',
    retry_count = retry_count + 1,
    completed_at = NULL,
    last_error_message = 'Approved replay after UNKNOWN_RESULT reconciliation',
    updated_at = CURRENT_TIMESTAMP
WHERE center_cut_execution_id = ?
  AND item_status = 'UNKNOWN_RESULT'
