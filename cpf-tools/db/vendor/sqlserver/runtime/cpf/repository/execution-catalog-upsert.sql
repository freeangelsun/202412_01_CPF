MERGE INTO cpf_standard_execution AS target
USING (VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)) AS source (
    standard_execution_id, execution_name, execution_type, owner_domain,
    source_module, source_class, source_method, http_method, endpoint, operation_id,
    description, required_permission, audit_reason_required_yn, visibility,
    direct_allowed_yn, gateway_allowed_yn, source_version, last_discovered_at
)
ON target.standard_execution_id = source.standard_execution_id
WHEN MATCHED THEN UPDATE SET
    execution_name = source.execution_name,
    execution_type = source.execution_type,
    owner_domain = source.owner_domain,
    source_module = source.source_module,
    source_class = source.source_class,
    source_method = source.source_method,
    http_method = source.http_method,
    endpoint = source.endpoint,
    operation_id = source.operation_id,
    description = source.description,
    required_permission = source.required_permission,
    audit_reason_required_yn = source.audit_reason_required_yn,
    visibility = source.visibility,
    direct_allowed_yn = source.direct_allowed_yn,
    gateway_allowed_yn = source.gateway_allowed_yn,
    source_version = source.source_version,
    registration_status = 'REGISTERED',
    last_discovered_at = source.last_discovered_at,
    updated_by = 'CPF_STARTUP',
    updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    standard_execution_id, execution_name, execution_type, owner_domain,
    source_module, source_class, source_method, http_method, endpoint, operation_id,
    description, required_permission, audit_reason_required_yn, visibility,
    direct_allowed_yn, gateway_allowed_yn, source_version, registration_status,
    first_registered_at, last_discovered_at, created_by, updated_by
) VALUES (
    source.standard_execution_id, source.execution_name, source.execution_type, source.owner_domain,
    source.source_module, source.source_class, source.source_method, source.http_method,
    source.endpoint, source.operation_id, source.description, source.required_permission,
    source.audit_reason_required_yn, source.visibility, source.direct_allowed_yn,
    source.gateway_allowed_yn, source.source_version, 'REGISTERED', CURRENT_TIMESTAMP,
    source.last_discovered_at, 'CPF_STARTUP', 'CPF_STARTUP'
);
