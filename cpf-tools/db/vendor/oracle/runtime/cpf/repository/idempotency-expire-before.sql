UPDATE cpf_idempotency_record
SET record_status = 'EXPIRED',
    retry_allowed_yn = 'Y',
    updated_by = 'CPF_IDEMPOTENCY_CLEANUP',
    updated_at = CURRENT_TIMESTAMP
WHERE record_status = 'PROCESSING'
  AND idempotency_seq IN (
      SELECT idempotency_seq
      FROM cpf_idempotency_record
      WHERE record_status = 'PROCESSING'
        AND expires_at IS NOT NULL
        AND expires_at <= ?
      ORDER BY idempotency_seq
      OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
  )
