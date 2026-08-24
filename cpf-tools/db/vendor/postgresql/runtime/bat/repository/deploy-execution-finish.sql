UPDATE BAT_DEPLOYMENT_EXECUTION
SET execution_state = ?,
    failure_stage = ?,
    result_message = ?,
    finished_at = CURRENT_TIMESTAMP(6)
WHERE deployment_id = ?
