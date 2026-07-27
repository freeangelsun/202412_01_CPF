-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=58_reference_external_edu_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_service tgt
USING (VALUES
  ('REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'REF EDU 대외연계 결과 불명 검증 대상', 'Y', 'SEED', 'SEED')
) AS src(service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
ON (tgt.service_id = src.service_id)
WHEN MATCHED THEN UPDATE SET
  tgt.service_name = src.service_name,
  tgt.owner_module_code = src.owner_module_code,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO cpf_service_endpoint tgt
USING (VALUES
  ('REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP', 'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED')
) AS src(endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
ON (tgt.endpoint_code = src.endpoint_code)
WHEN MATCHED THEN UPDATE SET
  tgt.service_id = src.service_id,
  tgt.endpoint_name = src.endpoint_name,
  tgt.endpoint_type = src.endpoint_type,
  tgt.base_url = src.base_url,
  tgt.context_path = src.context_path,
  tgt.default_timeout_ms = src.default_timeout_ms,
  tgt.default_retry_count = src.default_retry_count,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);

MERGE INTO cpf_service_instance tgt
USING (VALUES
  ('REF-EXT-SIM-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스', 'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SEED', 'SEED')
) AS src(instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET
  tgt.service_id = src.service_id,
  tgt.endpoint_code = src.endpoint_code,
  tgt.instance_name = src.instance_name,
  tgt.base_url = src.base_url,
  tgt.host_name = src.host_name,
  tgt.port_no = src.port_no,
  tgt.instance_status = src.instance_status,
  tgt.active_yn = src.active_yn,
  tgt.last_heartbeat_at = src.last_heartbeat_at,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);

MERGE INTO cpf_service_routing_policy tgt
USING (VALUES
  ('REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED')
) AS src(service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.priority = src.priority)
WHEN MATCHED THEN UPDATE SET
  tgt.routing_mode = src.routing_mode,
  tgt.load_balance_type = src.load_balance_type,
  tgt.failover_enabled_yn = src.failover_enabled_yn,
  tgt.health_check_required_yn = src.health_check_required_yn,
  tgt.active_yn = src.active_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
