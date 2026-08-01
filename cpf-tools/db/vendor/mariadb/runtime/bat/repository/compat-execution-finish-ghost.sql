UPDATE bat_execution
SET execution_status = ?,
    end_time = CURRENT_TIMESTAMP(3),
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE execution_id = ?
  AND execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING')
  AND last_heartbeat_at IS NOT NULL
