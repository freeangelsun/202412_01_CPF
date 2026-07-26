UPDATE bat_execution
SET execution_status = ?,
    end_time = SYSUTCDATETIME()
WHERE execution_id = ?
