INSERT INTO bat_runtime_command(
    command_id, idempotency_key, command_type, target_type, target_snapshot, target_snapshot_hash,
    expected_version, requested_by, reason_text, approval_policy_version, approval_request_id,
    approved_by, command_state, execution_attempt, requested_at, expires_at, result_text,
    failure_stage, before_state, after_state, transaction_id, evidence_ref
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
