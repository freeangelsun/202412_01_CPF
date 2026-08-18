INSERT INTO OPS_OPERATION_CATALOG (
    operation_id, operation_name, description, system_code, domain_code, application_code,
    http_method, api_path, controller_class, handler_method, openapi_operation_id,
    source_fingerprint, discovery_status, first_seen_at, last_seen_at, last_instance_id,
    metadata_version, log_policy_key, sensitive_yn, masking_policy_key, created_by, updated_by
) VALUES (?, ?, NULL, ?, ?, NULL, ?, ?, ?, ?, ?, NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 1, ?, ?, ?, ?, ?)
ON CONFLICT (operation_id) DO UPDATE SET
    operation_name = EXCLUDED.operation_name,
    system_code = EXCLUDED.system_code,
    domain_code = EXCLUDED.domain_code,
    http_method = EXCLUDED.http_method,
    api_path = EXCLUDED.api_path,
    controller_class = EXCLUDED.controller_class,
    handler_method = EXCLUDED.handler_method,
    openapi_operation_id = EXCLUDED.openapi_operation_id,
    discovery_status = 'ACTIVE',
    last_seen_at = CURRENT_TIMESTAMP,
    log_policy_key = COALESCE(EXCLUDED.log_policy_key, OPS_OPERATION_CATALOG.log_policy_key),
    sensitive_yn = EXCLUDED.sensitive_yn,
    masking_policy_key = COALESCE(EXCLUDED.masking_policy_key, OPS_OPERATION_CATALOG.masking_policy_key),
    metadata_version = OPS_OPERATION_CATALOG.metadata_version + 1,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP
