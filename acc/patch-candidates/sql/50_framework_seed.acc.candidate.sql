-- Account PFW 서비스 레지스트리 seed 후보입니다.
INSERT INTO pfw_service (
    service_id, service_name, service_type, owner_module_code, description,
    use_yn, created_by, updated_by
) VALUES (
    'ACC', 'Account 서비스', 'INTERNAL', 'ACC',
    'Account 주제영역 서비스 호출 대상', 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    owner_module_code = VALUES(owner_module_code),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES (
    'ACC_API', 'ACC', 'Account API Endpoint', 'HTTP',
    'http://localhost:8082', '/api/v1/acc', 3000, 0, 'Y', 'create-domain', 'create-domain'
)
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_name = VALUES(endpoint_name),
    base_url = VALUES(base_url),
    context_path = VALUES(context_path),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;