UPDATE bat_center_cut_execution
SET failure_count = CASE WHEN failure_count - ? < 0 THEN 0 ELSE failure_count - ? END,
    execution_state = 'RUNNING',
    completed_at = NULL,
    updated_at = SYSUTCDATETIME()
WHERE center_cut_execution_id = ?
  AND execution_state IN ('FAILED', 'PAUSED')
