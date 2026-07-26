UPDATE bat_execution
SET stop_requested_yn = 'Y',
    updated_at = CURRENT_TIMESTAMP
WHERE execution_id = ?
  AND execution_status IN ('READY', 'CLAIMING', 'CLAIMED', 'RUNNING')
