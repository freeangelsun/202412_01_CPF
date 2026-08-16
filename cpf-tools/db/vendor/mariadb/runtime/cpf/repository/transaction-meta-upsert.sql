INSERT INTO cpf_transaction_meta (
    transaction_id, transaction_name, module_code, domain_code,
    http_method, api_path, controller_class, handler_method,
    swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key,
    active_yn, first_detected_at, last_detected_at, last_scanned_at,
    created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)
ON DUPLICATE KEY UPDATE
    transaction_name = VALUES(transaction_name),
    module_code = VALUES(module_code),
    domain_code = VALUES(domain_code),
    http_method = VALUES(http_method),
    api_path = VALUES(api_path),
    controller_class = VALUES(controller_class),
    handler_method = VALUES(handler_method),
    swagger_operation_id = VALUES(swagger_operation_id),
    log_policy_key = COALESCE(VALUES(log_policy_key), log_policy_key),
    sensitive_yn = VALUES(sensitive_yn),
    masking_policy_key = COALESCE(VALUES(masking_policy_key), masking_policy_key),
    active_yn = 'Y',
    last_detected_at = CURRENT_TIMESTAMP,
    last_scanned_at = CURRENT_TIMESTAMP,
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP
