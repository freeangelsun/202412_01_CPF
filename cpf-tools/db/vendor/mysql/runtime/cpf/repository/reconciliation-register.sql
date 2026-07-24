INSERT INTO cpf_unknown_result (
    unknown_id, unknown_type, unknown_status, transaction_global_id, segment_id,
    external_key, failure_code, failure_message, next_action, detected_at, created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CPF_RECONCILIATION', 'CPF_RECONCILIATION')
