INSERT INTO cpf_transaction_meta (
    transaction_id, transaction_name, module_code, domain_code,
    http_method, api_path, controller_class, handler_method,
    swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key,
    active_yn, first_detected_at, last_detected_at, last_scanned_at,
    created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)
ON CONFLICT (transaction_id) DO UPDATE SET
    transaction_name = EXCLUDED.transaction_name,
    module_code = EXCLUDED.module_code,
    domain_code = EXCLUDED.domain_code,
    http_method = EXCLUDED.http_method,
    api_path = EXCLUDED.api_path,
    controller_class = EXCLUDED.controller_class,
    handler_method = EXCLUDED.handler_method,
    swagger_operation_id = EXCLUDED.swagger_operation_id,
    log_policy_key = COALESCE(EXCLUDED.log_policy_key, cpf_transaction_meta.log_policy_key),
    sensitive_yn = EXCLUDED.sensitive_yn,
    masking_policy_key = COALESCE(EXCLUDED.masking_policy_key, cpf_transaction_meta.masking_policy_key),
    active_yn = 'Y',
    last_detected_at = CURRENT_TIMESTAMP,
    last_scanned_at = CURRENT_TIMESTAMP,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP
