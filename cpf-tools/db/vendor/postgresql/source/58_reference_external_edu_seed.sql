-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=58_reference_external_edu_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
INSERT INTO cpf_service (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (
    'REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'REF EDU 대외연계 결과 불명 검증 대상', 'Y', 'SEED', 'SEED'
) ON CONFLICT (service_id) DO UPDATE SET service_name = EXCLUDED.service_name, owner_module_code = EXCLUDED.owner_module_code, description = EXCLUDED.description, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO cpf_service_endpoint (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP',
    'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED'
) ON CONFLICT (endpoint_code) DO UPDATE SET service_id = EXCLUDED.service_id, endpoint_name = EXCLUDED.endpoint_name, endpoint_type = EXCLUDED.endpoint_type, base_url = EXCLUDED.base_url, context_path = EXCLUDED.context_path, default_timeout_ms = EXCLUDED.default_timeout_ms, default_retry_count = EXCLUDED.default_retry_count, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO cpf_service_instance (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES (
    'REF-EXT-SIM-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스',
    'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SEED', 'SEED'
) ON CONFLICT (instance_id) DO UPDATE SET service_id = EXCLUDED.service_id, endpoint_code = EXCLUDED.endpoint_code, instance_name = EXCLUDED.instance_name, base_url = EXCLUDED.base_url, host_name = EXCLUDED.host_name, port_no = EXCLUDED.port_no, instance_status = EXCLUDED.instance_status, active_yn = EXCLUDED.active_yn, last_heartbeat_at = EXCLUDED.last_heartbeat_at, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO cpf_service_routing_policy (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (
    'REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED'
) ON CONFLICT (service_id, endpoint_code, priority) DO UPDATE SET routing_mode = EXCLUDED.routing_mode, load_balance_type = EXCLUDED.load_balance_type, failover_enabled_yn = EXCLUDED.failover_enabled_yn, health_check_required_yn = EXCLUDED.health_check_required_yn, active_yn = EXCLUDED.active_yn, priority = EXCLUDED.priority, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
