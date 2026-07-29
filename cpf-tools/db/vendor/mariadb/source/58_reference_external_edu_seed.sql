-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=58_reference_external_edu_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
INSERT INTO cpf_service (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (
    'REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'REF EDU 대외연계 결과 불명 검증 대상', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE service_name = VALUES(service_name), owner_module_code = VALUES(owner_module_code), description = VALUES(description), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO cpf_service_endpoint (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP',
    'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE service_id = VALUES(service_id), endpoint_name = VALUES(endpoint_name), endpoint_type = VALUES(endpoint_type), base_url = VALUES(base_url), context_path = VALUES(context_path), default_timeout_ms = VALUES(default_timeout_ms), default_retry_count = VALUES(default_retry_count), use_yn = VALUES(use_yn), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO cpf_service_instance (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES (
    'REF-EXT-SIM-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스',
    'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE service_id = VALUES(service_id), endpoint_code = VALUES(endpoint_code), instance_name = VALUES(instance_name), base_url = VALUES(base_url), host_name = VALUES(host_name), port_no = VALUES(port_no), instance_status = VALUES(instance_status), active_yn = VALUES(active_yn), last_heartbeat_at = VALUES(last_heartbeat_at), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
INSERT INTO cpf_service_routing_policy (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (
    'REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE routing_mode = VALUES(routing_mode), load_balance_type = VALUES(load_balance_type), failover_enabled_yn = VALUES(failover_enabled_yn), health_check_required_yn = VALUES(health_check_required_yn), active_yn = VALUES(active_yn), priority = VALUES(priority), updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP(3);
