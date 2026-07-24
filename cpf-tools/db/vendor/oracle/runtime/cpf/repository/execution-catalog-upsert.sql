MERGE INTO cpf_standard_execution target
USING (
    SELECT ? standard_execution_id, ? execution_name, ? execution_type, ? owner_domain,
           ? source_module, ? source_class, ? source_method, ? http_method,
           ? endpoint, ? operation_id, ? description, ? required_permission,
           ? audit_reason_required_yn, ? visibility, ? direct_allowed_yn,
           ? gateway_allowed_yn, ? source_version, ? last_discovered_at
    FROM dual
) source
ON (target.standard_execution_id = source.standard_execution_id)
WHEN MATCHED THEN UPDATE SET
    target.execution_name = source.execution_name,
    target.execution_type = source.execution_type,
    target.owner_domain = source.owner_domain,
    target.source_module = source.source_module,
    target.source_class = source.source_class,
    target.source_method = source.source_method,
    target.http_method = source.http_method,
    target.endpoint = source.endpoint,
    target.operation_id = source.operation_id,
    target.description = source.description,
    target.required_permission = source.required_permission,
    target.audit_reason_required_yn = source.audit_reason_required_yn,
    target.visibility = source.visibility,
    target.direct_allowed_yn = source.direct_allowed_yn,
    target.gateway_allowed_yn = source.gateway_allowed_yn,
    target.source_version = source.source_version,
    target.registration_status = 'REGISTERED',
    target.last_discovered_at = source.last_discovered_at,
    target.updated_by = 'CPF_STARTUP',
    target.updated_at = CURRENT_TIMESTAMP
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
)
