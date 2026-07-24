MERGE INTO cpf_transaction_meta AS target
USING (VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)) AS source (
    transaction_id, transaction_name, module_code, domain_code,
    http_method, api_path, controller_class, handler_method,
    swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key,
    created_by, updated_by
)
ON target.transaction_id = source.transaction_id
WHEN MATCHED THEN UPDATE SET
    transaction_name = source.transaction_name,
    module_code = source.module_code,
    domain_code = source.domain_code,
    http_method = source.http_method,
    api_path = source.api_path,
    controller_class = source.controller_class,
    handler_method = source.handler_method,
    swagger_operation_id = source.swagger_operation_id,
    log_policy_key = COALESCE(source.log_policy_key, target.log_policy_key),
    sensitive_yn = source.sensitive_yn,
    masking_policy_key = COALESCE(source.masking_policy_key, target.masking_policy_key),
    active_yn = 'Y',
    last_detected_at = CURRENT_TIMESTAMP,
    last_scanned_at = CURRENT_TIMESTAMP,
    updated_by = source.updated_by,
    updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    transaction_id, transaction_name, module_code, domain_code,
    http_method, api_path, controller_class, handler_method,
    swagger_operation_id, log_policy_key, sensitive_yn, masking_policy_key,
    active_yn, first_detected_at, last_detected_at, last_scanned_at,
    created_by, updated_by
) VALUES (
    source.transaction_id, source.transaction_name, source.module_code, source.domain_code,
    source.http_method, source.api_path, source.controller_class, source.handler_method,
    source.swagger_operation_id, source.log_policy_key, source.sensitive_yn,
    source.masking_policy_key, 'Y', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    source.created_by, source.updated_by
);
