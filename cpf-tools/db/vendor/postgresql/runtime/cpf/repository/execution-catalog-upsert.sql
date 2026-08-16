INSERT INTO cpf_standard_execution (
    standard_execution_id, execution_name, execution_type, owner_domain,
    source_module, source_class, source_method, http_method, endpoint, operation_id,
    description, required_permission, audit_reason_required_yn, visibility,
    direct_allowed_yn, gateway_allowed_yn, source_version,
    registration_status, first_registered_at, last_discovered_at,
    created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTERED', CURRENT_TIMESTAMP, ?, 'CPF_STARTUP', 'CPF_STARTUP')
ON CONFLICT (standard_execution_id) DO UPDATE SET
    execution_name = EXCLUDED.execution_name,
    execution_type = EXCLUDED.execution_type,
    owner_domain = EXCLUDED.owner_domain,
    source_module = EXCLUDED.source_module,
    source_class = EXCLUDED.source_class,
    source_method = EXCLUDED.source_method,
    http_method = EXCLUDED.http_method,
    endpoint = EXCLUDED.endpoint,
    operation_id = EXCLUDED.operation_id,
    description = EXCLUDED.description,
    required_permission = EXCLUDED.required_permission,
    audit_reason_required_yn = EXCLUDED.audit_reason_required_yn,
    visibility = EXCLUDED.visibility,
    direct_allowed_yn = EXCLUDED.direct_allowed_yn,
    gateway_allowed_yn = EXCLUDED.gateway_allowed_yn,
    source_version = EXCLUDED.source_version,
    registration_status = 'REGISTERED',
    last_discovered_at = EXCLUDED.last_discovered_at,
    updated_by = 'CPF_STARTUP',
    updated_at = CURRENT_TIMESTAMP
