-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=53_runtime_service_registry_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES ('MBW', '업무 백오피스 서비스', 'INTERNAL', 'MBW', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU', '온라인 교육 서비스', 'INTERNAL', 'EDU', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CEC', '센터컷 실행 서비스', 'INTERNAL', 'CEC', 'CPF 센터컷 Runner 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_name=VALUES(service_name), service_type=VALUES(service_type), owner_module_code=VALUES(owner_module_code), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES ('MBW_API', 'MBW', 'MBW API Endpoint', 'HTTP', 'http://cpf-backoffice', '/api/v1/backoffice', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU_API', 'EDU', 'EDU API Endpoint', 'HTTP', 'http://cpf-education', '/education', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://cpf-batch', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://cpf-admin', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CEC_API', 'CEC', 'CEC API Endpoint', 'HTTP', 'http://cpf-batch-center-cut', '/cec', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_id=VALUES(service_id), endpoint_name=VALUES(endpoint_name), endpoint_type=VALUES(endpoint_type), base_url=VALUES(base_url), context_path=VALUES(context_path), default_timeout_ms=VALUES(default_timeout_ms), default_retry_count=VALUES(default_retry_count), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES ('MBW', 'MBW_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('CEC', 'CEC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE routing_mode=VALUES(routing_mode), load_balance_type=VALUES(load_balance_type), failover_enabled_yn=VALUES(failover_enabled_yn), health_check_required_yn=VALUES(health_check_required_yn), active_yn=VALUES(active_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
