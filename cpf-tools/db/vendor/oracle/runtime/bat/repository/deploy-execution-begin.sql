INSERT INTO bat_deployment_execution(
    deployment_id, cell_id, idempotency_scope, idempotency_key, request_hash,
    to_version, strategy_code, execution_state, expected_version, approval_request_id,
    requested_by, approved_by, reason_text, started_at, created_at
)
VALUES (?, ?, ?, ?, ?, ?, ?, 'EXECUTING', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
