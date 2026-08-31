-- CPF V141: register the canonical CEC(Center-Cut Runner) Runtime Service identity.
-- RuntimeIdentityFactory fixes CANONICAL_CENTER_CUT_SERVICE_ID='CEC' and
-- CpfRuntimeControlPlaneRepository.ensureServiceAndEndpoint fails closed unless the
-- service and its endpoint are pre-registered, so an upgraded database needs these rows.

INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES ('CEC', '센터컷 실행 서비스', 'INTERNAL', 'CEC', 'CPF 센터컷 Runner 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (service_id) DO UPDATE SET service_name=EXCLUDED.service_name, service_type=EXCLUDED.service_type, owner_module_code=EXCLUDED.owner_module_code, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES ('CEC_API', 'CEC', 'CEC API Endpoint', 'HTTP', 'http://cpf-batch-center-cut', '/cec', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (endpoint_code) DO UPDATE SET service_id=EXCLUDED.service_id, endpoint_name=EXCLUDED.endpoint_name, endpoint_type=EXCLUDED.endpoint_type, base_url=EXCLUDED.base_url, context_path=EXCLUDED.context_path, default_timeout_ms=EXCLUDED.default_timeout_ms, default_retry_count=EXCLUDED.default_retry_count, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES ('CEC', 'CEC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON CONFLICT (service_id, endpoint_code, priority) DO UPDATE SET routing_mode=EXCLUDED.routing_mode, load_balance_type=EXCLUDED.load_balance_type, failover_enabled_yn=EXCLUDED.failover_enabled_yn, health_check_required_yn=EXCLUDED.health_check_required_yn, active_yn=EXCLUDED.active_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
