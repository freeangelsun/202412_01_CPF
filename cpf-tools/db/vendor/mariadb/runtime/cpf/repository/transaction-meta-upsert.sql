INSERT INTO OPS_OPERATION_CATALOG (
    operation_id, operation_name, description, system_code, domain_code, application_code,
    http_method, api_path, controller_class, handler_method, openapi_operation_id,
    source_fingerprint, discovery_status, first_seen_at, last_seen_at, last_instance_id,
    metadata_version, log_policy_key, sensitive_yn, masking_policy_key, created_by, updated_by
) VALUES (?, ?, NULL, ?, ?, NULL, ?, ?, ?, ?, ?, NULL, 'ACTIVE', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), NULL, 1, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    operation_name = VALUES(operation_name),
    system_code = VALUES(system_code),
    domain_code = VALUES(domain_code),
    http_method = VALUES(http_method),
    api_path = VALUES(api_path),
    controller_class = VALUES(controller_class),
    handler_method = VALUES(handler_method),
    openapi_operation_id = VALUES(openapi_operation_id),
    discovery_status = 'ACTIVE',
    last_seen_at = CURRENT_TIMESTAMP(3),
    log_policy_key = COALESCE(VALUES(log_policy_key), log_policy_key),
    sensitive_yn = VALUES(sensitive_yn),
    masking_policy_key = COALESCE(VALUES(masking_policy_key), masking_policy_key),
    metadata_version = metadata_version + 1,
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP
