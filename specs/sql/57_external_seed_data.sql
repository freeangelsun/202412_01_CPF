-- EXS 로컬 검증용 기관·endpoint와 CPF 서비스 레지스트리 seed입니다.
-- 운영 환경에서는 기관별 실제 URI와 인증 프로파일을 별도 승인 절차로 등록합니다.

USE exsDB;

INSERT INTO exs_institution (
    institution_code, institution_name, enabled_yn, created_by, updated_by
) VALUES (
    'CPF-REF', 'CPF 참조 대외 시뮬레이터', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    institution_name = VALUES(institution_name),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_channel (
    institution_code, channel_code, direction, enabled_yn, created_by, updated_by
) VALUES (
    'CPF-REF', 'REST', 'BIDIRECTIONAL', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    direction = VALUES(direction),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_endpoint (
    endpoint_code, institution_code, service_id, http_method, endpoint_uri,
    result_query_uri, timeout_ms, retry_count, enabled_yn, created_by, updated_by
) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'CPF-REF', 'REF', 'POST',
    '/api/reference/external-simulator/executions',
    '/api/reference/external-simulator/results/{externalRequestId}',
    3000, 0, 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    http_method = VALUES(http_method),
    endpoint_uri = VALUES(endpoint_uri),
    result_query_uri = VALUES(result_query_uri),
    timeout_ms = VALUES(timeout_ms),
    retry_count = VALUES(retry_count),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_control_policy (
    institution_code, control_type, enabled_yn, reason, created_by, updated_by
) VALUES (
    'CPF-REF', 'SEND', 'Y', '로컬 대외 시뮬레이터 송신 허용', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    enabled_yn = VALUES(enabled_yn),
    reason = VALUES(reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

USE cpfDB;

INSERT INTO cpf_service (
    service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by
) VALUES (
    'REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'EXS 로컬 결과 불명 검증 대상', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    owner_module_code = VALUES(owner_module_code),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP',
    'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_name = VALUES(endpoint_name),
    endpoint_type = VALUES(endpoint_type),
    base_url = VALUES(base_url),
    context_path = VALUES(context_path),
    default_timeout_ms = VALUES(default_timeout_ms),
    default_retry_count = VALUES(default_retry_count),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_instance (
    instance_id, service_id, endpoint_code, instance_name, base_url, host_name,
    port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by
) VALUES (
    'REF-EXS-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스',
    'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_code = VALUES(endpoint_code),
    instance_name = VALUES(instance_name),
    base_url = VALUES(base_url),
    host_name = VALUES(host_name),
    port_no = VALUES(port_no),
    instance_status = VALUES(instance_status),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_routing_policy (
    service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn,
    health_check_required_yn, active_yn, priority, created_by, updated_by
) VALUES (
    'REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    routing_mode = VALUES(routing_mode),
    load_balance_type = VALUES(load_balance_type),
    failover_enabled_yn = VALUES(failover_enabled_yn),
    health_check_required_yn = VALUES(health_check_required_yn),
    active_yn = VALUES(active_yn),
    priority = VALUES(priority),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);
