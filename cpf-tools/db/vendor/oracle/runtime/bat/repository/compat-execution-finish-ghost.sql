UPDATE bat_execution
SET execution_status = ?,
    end_time = SYSTIMESTAMP,
    updated_by = ?,
    updated_at = SYSTIMESTAMP,
    row_version = row_version + 1
WHERE execution_id = ?
  AND execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING')
  AND last_heartbeat_at IS NOT NULL
  AND row_version = ?
