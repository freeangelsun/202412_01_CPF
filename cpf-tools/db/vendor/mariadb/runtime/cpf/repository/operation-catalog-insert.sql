INSERT INTO OPS_OPERATION_CATALOG (
    operation_id, operation_name, description, system_code, domain_code,
    application_code, http_method, api_path, controller_class, handler_method,
    openapi_operation_id, source_fingerprint, discovery_status, first_seen_at,
    last_seen_at, last_instance_id, metadata_version, created_by, created_at,
    updated_by, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 'CPF_RUNTIME', ?, 'CPF_RUNTIME', ?)
