SELECT c.operation_id, c.operation_name, c.system_code, c.domain_code, c.application_code,
       c.http_method, c.api_path, c.controller_class, c.handler_method, c.openapi_operation_id,
       c.discovery_status, c.first_seen_at, c.last_seen_at, c.last_instance_id, c.metadata_version,
       c.log_policy_key, c.sensitive_yn, c.masking_policy_key,
       p.enabled_yn, p.all_callers_yn, p.channel_policy_required_yn, p.policy_version,
       p.seed_source, p.seed_revision, p.seeded_at, p.change_reason,
       c.created_by, c.created_at, c.updated_by, c.updated_at
FROM OPS_OPERATION_CATALOG c
JOIN OPS_OPERATION_POLICY p ON p.operation_id = c.operation_id
WHERE (? IS NULL OR c.system_code = ?)
  AND (? IS NULL OR p.enabled_yn = ?)
  AND (? IS NULL OR c.operation_id LIKE ?)
ORDER BY c.system_code, c.operation_id
LIMIT ?
