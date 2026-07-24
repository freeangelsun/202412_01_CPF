UPDATE cpf_idempotency_record
SET record_status = 'PROCESSING',
    stored_response = NULL,
    retry_allowed_yn = 'N',
    completed_at = NULL,
    expires_at = ?,
    updated_by = 'CPF_IDEMPOTENCY',
    updated_at = CURRENT_TIMESTAMP
WHERE scope = ?
  AND idempotency_key = ?
  AND request_hash = ?
  AND payload_hash = ?
  AND (
      (record_status IN ('FAILED', 'UNKNOWN') AND retry_allowed_yn = 'Y')
      OR record_status = 'EXPIRED'
      OR (record_status = 'PROCESSING' AND expires_at <= ?)
  )
