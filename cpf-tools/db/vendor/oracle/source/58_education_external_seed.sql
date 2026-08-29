-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=58_education_external_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'EDU' AS service_id, 'CPF 참조 서비스' AS service_name, 'INTERNAL' AS service_type, 'EDU' AS owner_module_code, 'EDU 대외연계 결과 불명 검증 대상' AS description, 'Y' AS use_yn, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'EDU-EXTERNAL-SIMULATOR' AS endpoint_code, 'EDU' AS service_id, 'EDU 대외 시뮬레이터' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://127.0.0.1:8099' AS base_url, '' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_INSTANCE tgt
USING (SELECT 'EDU-EXT-SIM-local-01' AS instance_id, 'EDU' AS service_id, 'EDU-EXTERNAL-SIMULATOR' AS endpoint_code, 'EDU 대외 시뮬레이터 인스턴스' AS instance_name, 'http://127.0.0.1:8099' AS base_url, 'localhost' AS host_name, 8099 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, SYSTIMESTAMP AS last_heartbeat_at, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual) src
ON (tgt.instance_id=src.instance_id)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_code=src.endpoint_code, tgt.instance_name=src.instance_name, tgt.base_url=src.base_url, tgt.host_name=src.host_name, tgt.port_no=src.port_no, tgt.instance_status=src.instance_status, tgt.active_yn=src.active_yn, tgt.last_heartbeat_at=src.last_heartbeat_at, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'EDU' AS service_id, 'EDU-EXTERNAL-SIMULATOR' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'N' AS failover_enabled_yn, 'N' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
