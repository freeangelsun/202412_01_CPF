MERGE INTO OPS_OPERATION_CATALOG t
USING (SELECT ? operation_id, ? operation_name, ? system_code, ? domain_code, ? http_method,
              ? api_path, ? controller_class, ? handler_method, ? openapi_operation_id,
              ? log_policy_key, ? sensitive_yn, ? masking_policy_key, ? created_by, ? updated_by FROM dual) s
ON (t.operation_id = s.operation_id)
WHEN MATCHED THEN UPDATE SET
    t.operation_name=s.operation_name, t.system_code=s.system_code, t.domain_code=s.domain_code,
    t.http_method=s.http_method, t.api_path=s.api_path, t.controller_class=s.controller_class,
    t.handler_method=s.handler_method, t.openapi_operation_id=s.openapi_operation_id,
    t.discovery_status='ACTIVE', t.last_seen_at=SYSTIMESTAMP,
    t.log_policy_key=COALESCE(s.log_policy_key,t.log_policy_key), t.sensitive_yn=s.sensitive_yn,
    t.masking_policy_key=COALESCE(s.masking_policy_key,t.masking_policy_key),
    t.metadata_version=t.metadata_version+1, t.updated_by=s.updated_by, t.updated_at=SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    operation_id, operation_name, description, system_code, domain_code, application_code,
    http_method, api_path, controller_class, handler_method, openapi_operation_id,
    source_fingerprint, discovery_status, first_seen_at, last_seen_at, last_instance_id,
    metadata_version, log_policy_key, sensitive_yn, masking_policy_key, created_by, updated_by
) VALUES (
    s.operation_id, s.operation_name, NULL, s.system_code, s.domain_code, NULL,
    s.http_method, s.api_path, s.controller_class, s.handler_method, s.openapi_operation_id,
    NULL, 'ACTIVE', SYSTIMESTAMP, SYSTIMESTAMP, NULL, 1,
    s.log_policy_key, s.sensitive_yn, s.masking_policy_key, s.created_by, s.updated_by
)
