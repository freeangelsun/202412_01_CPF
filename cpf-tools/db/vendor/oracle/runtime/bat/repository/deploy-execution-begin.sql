INSERT INTO bat_deployment_execution(
    deployment_id, cell_id, idempotency_key, to_version, strategy_code, execution_state,
    requested_by, approved_by, reason_text, started_at, created_at
)
VALUES (?, ?, ?, ?, ?, 'EXECUTING', ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
