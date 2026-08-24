UPDATE OPS_RETENTION_RUN
SET status = 'RUNNING', runtime_instance_id = ?, actor_id = ?, control_actor_id = ?,
    pause_requested_yn = 'N', completed_at = NULL, error_code = NULL,
    error_summary = NULL, updated_at = CURRENT_TIMESTAMP
WHERE run_id = ?
