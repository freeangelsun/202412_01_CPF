UPDATE BAT_CENTER_CUT_EXECUTION
SET unknown_count = CASE WHEN unknown_count - ? < 0 THEN 0 ELSE unknown_count - ? END,
    execution_state = 'RUNNING',
    completed_at = NULL,
    last_error_message = NULL,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE center_cut_execution_id = ?
  AND execution_state = 'UNKNOWN_RESULT'
