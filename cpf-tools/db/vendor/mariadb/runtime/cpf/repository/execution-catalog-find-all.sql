SELECT standard_execution_id, execution_name, execution_type, owner_domain,
       source_module, source_class, source_method, http_method, endpoint, operation_id,
       description, required_permission, audit_reason_required_yn, visibility,
       direct_allowed_yn, gateway_allowed_yn, source_version, last_discovered_at
FROM cpf_standard_execution
WHERE registration_status <> 'RETIRED'
ORDER BY standard_execution_id
