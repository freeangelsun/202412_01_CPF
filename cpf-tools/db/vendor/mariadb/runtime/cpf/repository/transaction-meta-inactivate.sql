UPDATE cpf_transaction_meta
SET active_yn = 'N',
    updated_by = ?,
    updated_at = CURRENT_TIMESTAMP
WHERE transaction_id = ?
