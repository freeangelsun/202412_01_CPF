UPDATE cpf_idempotency_record
SET record_status = ?,
    stored_response = ?,
    retry_allowed_yn = ?,
    completed_at = CURRENT_TIMESTAMP,
    updated_by = 'CPF_IDEMPOTENCY',
    updated_at = CURRENT_TIMESTAMP
WHERE scope = ?
  AND idempotency_key = ?
