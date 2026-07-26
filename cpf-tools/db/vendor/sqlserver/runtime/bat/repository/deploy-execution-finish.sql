UPDATE bat_deployment_execution
SET execution_state = ?,
    failure_stage = ?,
    result_message = ?,
    finished_at = SYSUTCDATETIME()
WHERE deployment_id = ?
