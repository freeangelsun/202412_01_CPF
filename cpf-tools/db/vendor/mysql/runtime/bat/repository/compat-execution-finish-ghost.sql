UPDATE bat_execution
SET execution_status = ?,
    end_time = CURRENT_TIMESTAMP(3)
WHERE execution_id = ?
