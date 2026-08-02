INSERT INTO cpf_batch_execution_control (
  cpf_execution_id, job_id, definition_version, approval_id, operator_id, reason,
  idempotency_scope, idempotency_key, request_hash, plan_checksum,
  fencing_token, control_status, control_version, reconcile_attempts,
  unknown_reason, unknown_detail, reconcile_after, last_error_code, last_error_detail,
  created_at, updated_at
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'RESERVED', 1, 0,
        NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
