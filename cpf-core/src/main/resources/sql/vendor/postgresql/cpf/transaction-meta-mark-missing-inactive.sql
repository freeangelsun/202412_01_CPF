UPDATE cpf_transaction_meta
SET active_yn = 'N',
    last_scanned_at = CURRENT_TIMESTAMP,
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE active_yn = 'Y'
  AND transaction_id NOT IN (%s)
