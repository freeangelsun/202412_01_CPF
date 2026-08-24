INSERT INTO OPS_RETENTION_CONTROL_AUDIT(
  audit_id, operation_type, target_type, target_id, requested_by, approved_by,
  approval_request_id, reason_text, expected_version, result_state, created_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
