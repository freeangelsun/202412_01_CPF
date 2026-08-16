INSERT INTO cpf_standard_execution (
    standard_execution_id, execution_name, execution_type, owner_domain,
    source_module, source_class, source_method, http_method, endpoint, operation_id,
    description, required_permission, audit_reason_required_yn, visibility,
    direct_allowed_yn, gateway_allowed_yn, source_version,
    registration_status, first_registered_at, last_discovered_at,
    created_by, updated_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTERED', CURRENT_TIMESTAMP, ?, 'CPF_STARTUP', 'CPF_STARTUP')
ON DUPLICATE KEY UPDATE
    execution_name = VALUES(execution_name),
    execution_type = VALUES(execution_type),
    owner_domain = VALUES(owner_domain),
    source_module = VALUES(source_module),
    source_class = VALUES(source_class),
    source_method = VALUES(source_method),
    http_method = VALUES(http_method),
    endpoint = VALUES(endpoint),
    operation_id = VALUES(operation_id),
    description = VALUES(description),
    required_permission = VALUES(required_permission),
    audit_reason_required_yn = VALUES(audit_reason_required_yn),
    visibility = VALUES(visibility),
    direct_allowed_yn = VALUES(direct_allowed_yn),
    gateway_allowed_yn = VALUES(gateway_allowed_yn),
    source_version = VALUES(source_version),
    registration_status = 'REGISTERED',
    last_discovered_at = VALUES(last_discovered_at),
    updated_by = 'CPF_STARTUP',
    updated_at = CURRENT_TIMESTAMP
