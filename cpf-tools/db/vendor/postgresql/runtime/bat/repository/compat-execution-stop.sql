UPDATE bat_execution
SET stop_requested_yn = 'Y',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP,
    row_version = row_version + 1
WHERE execution_id = ?
  AND execution_status IN ('READY', 'CLAIMING', 'CLAIMED', 'RUNNING')
  AND row_version = ?
