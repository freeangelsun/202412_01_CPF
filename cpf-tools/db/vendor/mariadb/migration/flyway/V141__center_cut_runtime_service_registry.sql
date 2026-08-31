-- CPF V141: register the canonical CEC(Center-Cut Runner) Runtime Service identity.
-- RuntimeIdentityFactory fixes CANONICAL_CENTER_CUT_SERVICE_ID='CEC' and
-- CpfRuntimeControlPlaneRepository.ensureServiceAndEndpoint fails closed unless the
-- service and its endpoint are pre-registered, so an upgraded database needs these rows.

INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES ('CEC', '센터컷 실행 서비스', 'INTERNAL', 'CEC', 'CPF 센터컷 Runner 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_name=VALUES(service_name), service_type=VALUES(service_type), owner_module_code=VALUES(owner_module_code), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES ('CEC_API', 'CEC', 'CEC API Endpoint', 'HTTP', 'http://cpf-batch-center-cut', '/cec', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE service_id=VALUES(service_id), endpoint_name=VALUES(endpoint_name), endpoint_type=VALUES(endpoint_type), base_url=VALUES(base_url), context_path=VALUES(context_path), default_timeout_ms=VALUES(default_timeout_ms), default_retry_count=VALUES(default_retry_count), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES ('CEC', 'CEC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE routing_mode=VALUES(routing_mode), load_balance_type=VALUES(load_balance_type), failover_enabled_yn=VALUES(failover_enabled_yn), health_check_required_yn=VALUES(health_check_required_yn), active_yn=VALUES(active_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
