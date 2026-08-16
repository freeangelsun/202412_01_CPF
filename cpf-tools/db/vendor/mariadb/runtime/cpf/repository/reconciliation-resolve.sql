UPDATE cpf_unknown_result
SET unknown_status = ?,
    resolved_at = CURRENT_TIMESTAMP,
    resolved_by = ?,
    audit_reason = ?,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE unknown_id = ?
