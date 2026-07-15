-- 최초 배포 모듈 기준으로 서비스 레지스트리를 정리합니다.
-- 제거된 업무 모듈 데이터는 감사와 rollback을 위해 삭제하지 않고 비활성화합니다.

USE pfwDB;

UPDATE pfw_service
SET use_yn = 'N', updated_by = 'FLYWAY', updated_at = CURRENT_TIMESTAMP
WHERE service_id IN ('ACC', 'EXS');

UPDATE pfw_service_endpoint
SET use_yn = 'N', updated_by = 'FLYWAY', updated_at = CURRENT_TIMESTAMP
WHERE service_id IN ('ACC', 'EXS');

UPDATE pfw_service_instance
SET active_yn = 'N', instance_status = 'DOWN', updated_by = 'FLYWAY', updated_at = CURRENT_TIMESTAMP
WHERE service_id IN ('ACC', 'EXS');

INSERT INTO pfw_service (
    service_id, service_name, service_type, owner_module_code, description,
    use_yn, created_by, updated_by
) VALUES
    ('BZA', '업무 백오피스 서비스', 'INTERNAL', 'BZA', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'FLYWAY', 'FLYWAY'),
    ('XYZ', '온라인 교육 서비스', 'INTERNAL', 'XYZ', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name), owner_module_code = VALUES(owner_module_code),
    description = VALUES(description), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES
    ('BZA_API', 'BZA', 'BZA API Endpoint', 'HTTP', 'http://localhost:8091', '/api/bza', 3000, 0, 'Y', 'FLYWAY', 'FLYWAY'),
    ('XYZ_API', 'XYZ', 'XYZ API Endpoint', 'HTTP', 'http://localhost:8099', '/xyz', 3000, 0, 'Y', 'FLYWAY', 'FLYWAY')
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id), endpoint_name = VALUES(endpoint_name),
    endpoint_type = VALUES(endpoint_type), base_url = VALUES(base_url),
    context_path = VALUES(context_path), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
