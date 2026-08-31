-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=53_runtime_service_registry_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'MBW' AS service_id, '업무 백오피스 서비스' AS service_name, 'INTERNAL' AS service_type, 'MBW' AS owner_module_code, 'CPF 업무 운영 백오피스 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'EDU' AS service_id, '온라인 교육 서비스' AS service_name, 'INTERNAL' AS service_type, 'EDU' AS owner_module_code, 'CPF 온라인 교육 및 검증 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'BAT' AS service_id, '배치 Worker 서비스' AS service_name, 'INTERNAL' AS service_type, 'BAT' AS owner_module_code, 'CPF 배치 Worker 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'ADM' AS service_id, '운영 콘솔 서비스' AS service_name, 'INTERNAL' AS service_type, 'ADM' AS owner_module_code, 'CPF 운영 콘솔 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'CEC' AS service_id, '센터컷 실행 서비스' AS service_name, 'INTERNAL' AS service_type, 'CEC' AS owner_module_code, 'CPF 센터컷 Runner 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'MBW_API' AS endpoint_code, 'MBW' AS service_id, 'MBW API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-backoffice' AS base_url, '/api/v1/backoffice' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'EDU_API' AS endpoint_code, 'EDU' AS service_id, 'EDU API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-education' AS base_url, '/education' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'BAT_API' AS endpoint_code, 'BAT' AS service_id, 'BAT API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-batch' AS base_url, '/bat' AS context_path, 5000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'ADM_API' AS endpoint_code, 'ADM' AS service_id, 'ADM API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-admin' AS base_url, '/adm' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'CEC_API' AS endpoint_code, 'CEC' AS service_id, 'CEC API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-batch-center-cut' AS base_url, '/cec' AS context_path, 5000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'MBW' AS service_id, 'MBW_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'EDU' AS service_id, 'EDU_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'BAT' AS service_id, 'BAT_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'ADM' AS service_id, 'ADM_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'CEC' AS service_id, 'CEC_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
