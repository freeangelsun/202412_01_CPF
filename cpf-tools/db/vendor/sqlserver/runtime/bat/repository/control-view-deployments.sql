SELECT deployment_id, cell_id, to_version, strategy_code, execution_state, failure_stage,
       requested_by, approved_by, started_at, finished_at
FROM bat_deployment_execution
ORDER BY created_at DESC
OFFSET 0 ROWS FETCH NEXT 500 ROWS ONLY
