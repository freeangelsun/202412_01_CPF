-- CPF generated lifecycle bundle; vendor=oracle
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 55_cmn_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=55_cmn_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB

-- CPF_LOGICAL_DATABASE=referenceFixture
MERGE INTO REF_CMN_SAMPLE_ITEM tgt USING (
SELECT 'CMN-SAMPLE-001' sample_key, 'CPF CMN 기본 샘플' item_name, 'DATABASE' category_code, 'ACTIVE' status_code, 'connection migration crud search offset slice cursor' searchable_text, NULL owner_reference, 10 sort_order, 0 version_no, 'CMN_SAMPLE' created_by, 'CMN_SAMPLE' updated_by FROM dual
UNION ALL
SELECT 'CMN-SAMPLE-002' sample_key, 'CPF CMN 비활성 샘플' item_name, 'VALIDATION' category_code, 'INACTIVE' status_code, 'validation duplicate optimistic-lock rollback' searchable_text, NULL owner_reference, 20 sort_order, 0 version_no, 'CMN_SAMPLE' created_by, 'CMN_SAMPLE' updated_by FROM dual
) src ON (tgt.sample_key = src.sample_key)
WHEN MATCHED THEN UPDATE SET tgt.item_name = src.item_name, tgt.category_code = src.category_code, tgt.status_code = src.status_code, tgt.searchable_text = src.searchable_text, tgt.owner_reference = src.owner_reference, tgt.sort_order = src.sort_order, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);

-- ===== END 55_cmn_seed_data.sql =====

-- ===== BEGIN 58_reference_external_edu_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=58_reference_external_edu_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_SERVICE tgt USING (
SELECT 'EDU' service_id, 'CPF 참조 서비스' service_name, 'INTERNAL' service_type, 'EDU' owner_module_code, 'EDU 대외연계 결과 불명 검증 대상' description, 'Y' use_yn, 'SEED' created_by, 'SEED' updated_by FROM dual
) src ON (tgt.service_id = src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name = src.service_name, tgt.owner_module_code = src.owner_module_code, tgt.description = src.description, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt USING (
SELECT 'EDU-EXTERNAL-SIMULATOR' endpoint_code, 'EDU' service_id, 'EDU 대외 시뮬레이터' endpoint_name, 'HTTP' endpoint_type, 'http://127.0.0.1:8099' base_url, '' context_path, 3000 default_timeout_ms, 0 default_retry_count, 'Y' use_yn, 'SEED' created_by, 'SEED' updated_by FROM dual
) src ON (tgt.endpoint_code = src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id = src.service_id, tgt.endpoint_name = src.endpoint_name, tgt.endpoint_type = src.endpoint_type, tgt.base_url = src.base_url, tgt.context_path = src.context_path, tgt.default_timeout_ms = src.default_timeout_ms, tgt.default_retry_count = src.default_retry_count, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_INSTANCE tgt USING (
SELECT 'EDU-EXT-SIM-local-01' instance_id, 'EDU' service_id, 'EDU-EXTERNAL-SIMULATOR' endpoint_code, 'EDU 대외 시뮬레이터 인스턴스' instance_name, 'http://127.0.0.1:8099' base_url, 'localhost' host_name, 8099 port_no, 'UP' instance_status, 100 weight, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'SEED' created_by, 'SEED' updated_by FROM dual
) src ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET tgt.service_id = src.service_id, tgt.endpoint_code = src.endpoint_code, tgt.instance_name = src.instance_name, tgt.base_url = src.base_url, tgt.host_name = src.host_name, tgt.port_no = src.port_no, tgt.instance_status = src.instance_status, tgt.active_yn = src.active_yn, tgt.last_heartbeat_at = src.last_heartbeat_at, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt USING (
SELECT 'EDU' service_id, 'EDU-EXTERNAL-SIMULATOR' endpoint_code, 'PRIMARY' routing_mode, 'WEIGHT' load_balance_type, 'N' failover_enabled_yn, 'N' health_check_required_yn, 'Y' active_yn, 100 priority, 'SEED' created_by, 'SEED' updated_by FROM dual
) src ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.priority = src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode = src.routing_mode, tgt.load_balance_type = src.load_balance_type, tgt.failover_enabled_yn = src.failover_enabled_yn, tgt.health_check_required_yn = src.health_check_required_yn, tgt.active_yn = src.active_yn, tgt.priority = src.priority, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);

-- ===== END 58_reference_external_edu_seed.sql =====

-- ===== BEGIN 58_reference_runtime_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=58_reference_runtime_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO BAT_INSTANCE tgt USING (
SELECT 'local-batch-01' instance_id, '로컬 배치 인스턴스' instance_name, 'localhost' host_name, 8099 server_port, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET tgt.instance_name = src.instance_name, tgt.host_name = src.host_name, tgt.server_port = src.server_port, tgt.active_yn = src.active_yn, tgt.last_heartbeat_at = src.last_heartbeat_at, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by) VALUES (src.instance_id, src.instance_name, src.host_name, src.server_port, src.active_yn, src.last_heartbeat_at, src.description, src.created_by, src.updated_by);
MERGE INTO BAT_WORKER tgt USING (
SELECT 'local-batch-01' worker_id, 'local-batch-01' server_instance_id, 'localhost' host_name, 'seed' process_id, 'seed-main' thread_name, 'IDLE' worker_status, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, NULL current_job_id, NULL current_execution_id, '로컬 smoke 검증용 배치 worker heartbeat' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.worker_id = src.worker_id)
WHEN MATCHED THEN UPDATE SET tgt.server_instance_id = src.server_instance_id, tgt.host_name = src.host_name, tgt.process_id = src.process_id, tgt.thread_name = src.thread_name, tgt.worker_status = src.worker_status, tgt.active_yn = src.active_yn, tgt.last_heartbeat_at = src.last_heartbeat_at, tgt.current_job_id = src.current_job_id, tgt.current_execution_id = src.current_execution_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (worker_id, server_instance_id, host_name, process_id, thread_name, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by) VALUES (src.worker_id, src.server_instance_id, src.host_name, src.process_id, src.thread_name, src.worker_status, src.active_yn, src.last_heartbeat_at, src.current_job_id, src.current_execution_id, src.description, src.created_by, src.updated_by);
MERGE INTO BAT_JOB tgt USING (
SELECT 'CPF_EDU_TASKLET_JOB' job_id, 'CPF 교육 Tasklet Job' job_name, 'TASKLET' job_type, '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.' description, 'Y' restartable_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_EDU_CHUNK_JOB' job_id, 'CPF 교육 Chunk Job' job_name, 'CHUNK' job_type, '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.' description, 'Y' restartable_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_EDU_RETRY_JOB' job_id, 'CPF 교육 재처리 Job' job_name, 'RETRY' job_type, '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.' description, 'Y' restartable_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET tgt.job_name = src.job_name, tgt.job_type = src.job_type, tgt.description = src.description, tgt.restartable_yn = src.restartable_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BAT_SCHEDULE tgt USING (
SELECT 'CPF_EDU_TASKLET_DAILY' schedule_id, 'CPF_EDU_TASKLET_JOB' job_id, '0 0 2 * * *' cron_expression, 'DEFAULT' calendar_id, 'Y' business_day_only_yn, 'SKIP' holiday_policy, '02:00:00' available_start_time, '04:00:00' available_end_time, 'D+0' run_date_pattern, 'Asia/Seoul' timezone, 'N' enabled_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_EDU_CHUNK_DAILY' schedule_id, 'CPF_EDU_CHUNK_JOB' job_id, '0 30 2 * * *' cron_expression, 'DEFAULT' calendar_id, 'Y' business_day_only_yn, 'SKIP' holiday_policy, '02:30:00' available_start_time, '05:30:00' available_end_time, 'D+0' run_date_pattern, 'Asia/Seoul' timezone, 'N' enabled_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.schedule_id = src.schedule_id)
WHEN MATCHED THEN UPDATE SET tgt.job_id = src.job_id, tgt.cron_expression = src.cron_expression, tgt.calendar_id = src.calendar_id, tgt.business_day_only_yn = src.business_day_only_yn, tgt.holiday_policy = src.holiday_policy, tgt.available_start_time = src.available_start_time, tgt.available_end_time = src.available_end_time, tgt.run_date_pattern = src.run_date_pattern, tgt.timezone = src.timezone, tgt.enabled_yn = src.enabled_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern, timezone, enabled_yn, created_by, updated_by) VALUES (src.schedule_id, src.job_id, src.cron_expression, src.calendar_id, src.business_day_only_yn, src.holiday_policy, src.available_start_time, src.available_end_time, src.run_date_pattern, src.timezone, src.enabled_yn, src.created_by, src.updated_by);
MERGE INTO BAT_JOB_RELATION tgt USING (
SELECT 'CPF_EDU_CHUNK_JOB' job_id, 'CPF_EDU_TASKLET_JOB' related_job_id, 'PREDECESSOR' relation_type, 'COMPLETED' trigger_condition, 'COMPLETED' required_status, 10 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_EDU_TASKLET_JOB' job_id, 'CPF_EDU_CHUNK_JOB' related_job_id, 'TRIGGER' relation_type, 'COMPLETED' trigger_condition, 'COMPLETED' required_status, 20 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_id = src.job_id AND tgt.related_job_id = src.related_job_id AND tgt.relation_type = src.relation_type)
WHEN MATCHED THEN UPDATE SET tgt.trigger_condition = src.trigger_condition, tgt.required_status = src.required_status, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by) VALUES (src.job_id, src.related_job_id, src.relation_type, src.trigger_condition, src.required_status, src.sort_order, src.use_yn, src.created_by, src.updated_by);
INSERT INTO BAT_EXECUTION (job_id, schedule_id, job_parameters, execution_status, batch_instance_id, server_instance_id, worker_id, transaction_id, start_time, end_time, read_count, write_count, skip_count, requested_by, created_by, updated_by) SELECT
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000REFlocal010000001',
    (SYSTIMESTAMP - INTERVAL '10' MINUTE),
    (SYSTIMESTAMP - INTERVAL '9' MINUTE),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);
COLUMN cpf_edu_execution_id NEW_VALUE cpf_edu_execution_id NOPRINT
SELECT (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY) AS cpf_edu_execution_id FROM dual;
INSERT INTO BAT_STEP_EXECUTION (execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status, start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by) SELECT &&cpf_edu_execution_id, NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', (SYSTIMESTAMP - INTERVAL '10' MINUTE), (SYSTIMESTAMP - INTERVAL '9' MINUTE), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE &&cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_STEP_EXECUTION
      WHERE execution_id = &&cpf_edu_execution_id
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );
INSERT INTO BAT_EXECUTION_TARGET (execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at, dispatch_status, dispatch_reason, created_by, updated_by) SELECT
    &&cpf_edu_execution_id,
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST((CURRENT_DATE || ' 02:00:00') AS TIMESTAMP),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE &&cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_EXECUTION_TARGET
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );
MERGE INTO CMN_BUSINESS_CALENDAR_DAY tgt USING (
SELECT 'DEFAULT' calendar_id, CURRENT_DATE business_date, 'Y' business_day_yn, 'BUSINESS' day_type, NULL institution_code, '로컬 smoke 검증용 기본 영업일' reason, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DEFAULT' calendar_id, (CURRENT_DATE + INTERVAL '1' DAY) business_date, 'Y' business_day_yn, 'BUSINESS' day_type, NULL institution_code, '로컬 smoke 검증용 다음 영업일' reason, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.calendar_id = src.calendar_id AND tgt.business_date = src.business_date)
WHEN MATCHED THEN UPDATE SET tgt.business_day_yn = src.business_day_yn, tgt.day_type = src.day_type, tgt.institution_code = src.institution_code, tgt.reason = src.reason, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (calendar_id, business_date, business_day_yn, day_type, institution_code, reason, created_by, updated_by) VALUES (src.calendar_id, src.business_date, src.business_day_yn, src.day_type, src.institution_code, src.reason, src.created_by, src.updated_by);
MERGE INTO BAT_JOB tgt USING (
SELECT 'CPF_BAT_CENTER_CUT_JOB' job_id, 'CPF BAT 센터컷 smoke Job' job_name, 'TASKLET' job_type, 'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.' description, 'Y' restartable_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET tgt.job_name = src.job_name, tgt.job_type = src.job_type, tgt.description = src.description, tgt.restartable_yn = src.restartable_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BAT_JOB tgt USING (
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' job_id, 'CPF REF 업무 DB 센터컷 샘플 Job' job_name, 'TASKLET' job_type, 'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.' description, 'Y' restartable_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET tgt.job_name = src.job_name, tgt.job_type = src.job_type, tgt.description = src.description, tgt.restartable_yn = src.restartable_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BAT_CENTER_CUT_JOB tgt USING (
SELECT 'CPF_BAT_CENTER_CUT_JOB' center_cut_job_id, 'CPF_BAT_CENTER_CUT_JOB' batch_job_id, 'CPF BAT 센터컷 smoke Job' center_cut_job_name, 'batCenterCutSampleTargetProvider' provider_key, 'batCenterCutSampleHandler' handler_key, 10 chunk_size, 3 retry_limit, 'Y' use_yn, 'CPF 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.center_cut_job_id = src.center_cut_job_id)
WHEN MATCHED THEN UPDATE SET tgt.batch_job_id = src.batch_job_id, tgt.center_cut_job_name = src.center_cut_job_name, tgt.provider_key = src.provider_key, tgt.handler_key = src.handler_key, tgt.chunk_size = src.chunk_size, tgt.retry_limit = src.retry_limit, tgt.use_yn = src.use_yn, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by) VALUES (src.center_cut_job_id, src.batch_job_id, src.center_cut_job_name, src.provider_key, src.handler_key, src.chunk_size, src.retry_limit, src.use_yn, src.description, src.created_by, src.updated_by);
MERGE INTO BAT_CENTER_CUT_JOB tgt USING (
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' batch_job_id, 'CPF REF 업무 DB 센터컷 샘플 Job' center_cut_job_name, 'refCenterCutTargetProvider' provider_key, 'refCenterCutHandler' handler_key, 10 chunk_size, 3 retry_limit, 'Y' use_yn, 'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.center_cut_job_id = src.center_cut_job_id)
WHEN MATCHED THEN UPDATE SET tgt.batch_job_id = src.batch_job_id, tgt.center_cut_job_name = src.center_cut_job_name, tgt.provider_key = src.provider_key, tgt.handler_key = src.handler_key, tgt.chunk_size = src.chunk_size, tgt.retry_limit = src.retry_limit, tgt.use_yn = src.use_yn, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by) VALUES (src.center_cut_job_id, src.batch_job_id, src.center_cut_job_name, src.provider_key, src.handler_key, src.chunk_size, src.retry_limit, src.use_yn, src.description, src.created_by, src.updated_by);
MERGE INTO BAT_CENTER_CUT_PARAMETER tgt USING (
SELECT 'CPF_BAT_CENTER_CUT_JOB' center_cut_job_id, 'businessDatePattern' parameter_key, 'D+0' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_BAT_CENTER_CUT_JOB' center_cut_job_id, 'defaultLimit' parameter_key, '10' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.center_cut_job_id = src.center_cut_job_id AND tgt.parameter_key = src.parameter_key)
WHEN MATCHED THEN UPDATE SET tgt.parameter_value = src.parameter_value, tgt.encrypted_yn = src.encrypted_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by) VALUES (src.center_cut_job_id, src.parameter_key, src.parameter_value, src.encrypted_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt USING (
SELECT 'BZA' service_id, '업무 백오피스 서비스' service_name, 'INTERNAL' service_type, 'BZA' owner_module_code, 'CPF 업무 운영 백오피스 서비스 호출 대상' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EDU' service_id, '온라인 교육 서비스' service_name, 'INTERNAL' service_type, 'EDU' owner_module_code, 'CPF 온라인 교육 및 검증 서비스 호출 대상' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT' service_id, '배치 Worker 서비스' service_name, 'INTERNAL' service_type, 'BAT' owner_module_code, 'CPF 배치 Worker 서비스 호출 대상' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM' service_id, '운영 콘솔 서비스' service_name, 'INTERNAL' service_type, 'ADM' owner_module_code, 'CPF 운영 콘솔 서비스 호출 대상' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.service_id = src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name = src.service_name, tgt.service_type = src.service_type, tgt.owner_module_code = src.owner_module_code, tgt.description = src.description, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt USING (
SELECT 'BZA_API' endpoint_code, 'BZA' service_id, 'BZA API Endpoint' endpoint_name, 'HTTP' endpoint_type, 'http://localhost:8091' base_url, '/api/bza' context_path, 3000 default_timeout_ms, 0 default_retry_count, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EDU_API' endpoint_code, 'EDU' service_id, 'EDU API Endpoint' endpoint_name, 'HTTP' endpoint_type, 'http://localhost:8099' base_url, '/education' context_path, 3000 default_timeout_ms, 0 default_retry_count, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_API' endpoint_code, 'BAT' service_id, 'BAT API Endpoint' endpoint_name, 'HTTP' endpoint_type, 'http://localhost:8093' base_url, '/bat' context_path, 5000 default_timeout_ms, 0 default_retry_count, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_API' endpoint_code, 'ADM' service_id, 'ADM API Endpoint' endpoint_name, 'HTTP' endpoint_type, 'http://localhost:8090' base_url, '/adm' context_path, 3000 default_timeout_ms, 0 default_retry_count, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.endpoint_code = src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id = src.service_id, tgt.endpoint_name = src.endpoint_name, tgt.endpoint_type = src.endpoint_type, tgt.base_url = src.base_url, tgt.context_path = src.context_path, tgt.default_timeout_ms = src.default_timeout_ms, tgt.default_retry_count = src.default_retry_count, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_INSTANCE tgt USING (
SELECT 'BZA-local-01' instance_id, 'BZA' service_id, 'BZA_API' endpoint_code, 'BZA local instance' instance_name, 'http://localhost:8091' base_url, 'localhost' host_name, 8091 port_no, 'UP' instance_status, 100 weight, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EDU-local-01' instance_id, 'EDU' service_id, 'EDU_API' endpoint_code, 'EDU local instance' instance_name, 'http://localhost:8099' base_url, 'localhost' host_name, 8099 port_no, 'UP' instance_status, 100 weight, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT-local-01' instance_id, 'BAT' service_id, 'BAT_API' endpoint_code, 'BAT local instance' instance_name, 'http://localhost:8093' base_url, 'localhost' host_name, 8093 port_no, 'UP' instance_status, 100 weight, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM-local-01' instance_id, 'ADM' service_id, 'ADM_API' endpoint_code, 'ADM local instance' instance_name, 'http://localhost:8090' base_url, 'localhost' host_name, 8090 port_no, 'UP' instance_status, 100 weight, 'Y' active_yn, SYSTIMESTAMP last_heartbeat_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET tgt.service_id = src.service_id, tgt.endpoint_code = src.endpoint_code, tgt.instance_name = src.instance_name, tgt.base_url = src.base_url, tgt.host_name = src.host_name, tgt.port_no = src.port_no, tgt.instance_status = src.instance_status, tgt.weight = src.weight, tgt.active_yn = src.active_yn, tgt.last_heartbeat_at = src.last_heartbeat_at, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt USING (
SELECT 'BZA' service_id, 'BZA_API' endpoint_code, 'PRIMARY' routing_mode, 'WEIGHT' load_balance_type, 'Y' failover_enabled_yn, 'Y' health_check_required_yn, 'Y' active_yn, 100 priority, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EDU' service_id, 'EDU_API' endpoint_code, 'PRIMARY' routing_mode, 'WEIGHT' load_balance_type, 'Y' failover_enabled_yn, 'Y' health_check_required_yn, 'Y' active_yn, 100 priority, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT' service_id, 'BAT_API' endpoint_code, 'PRIMARY' routing_mode, 'WEIGHT' load_balance_type, 'Y' failover_enabled_yn, 'Y' health_check_required_yn, 'Y' active_yn, 100 priority, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM' service_id, 'ADM_API' endpoint_code, 'PRIMARY' routing_mode, 'WEIGHT' load_balance_type, 'Y' failover_enabled_yn, 'Y' health_check_required_yn, 'Y' active_yn, 100 priority, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.priority = src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode = src.routing_mode, tgt.load_balance_type = src.load_balance_type, tgt.failover_enabled_yn = src.failover_enabled_yn, tgt.health_check_required_yn = src.health_check_required_yn, tgt.active_yn = src.active_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_CIRCUIT_STATE tgt USING (
SELECT 'BZA' service_id, 'BZA_API' endpoint_code, 'BZA-local-01' instance_id, 'CLOSED' circuit_state, 0 failure_count, 0 success_count, SYSTIMESTAMP closed_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EDU' service_id, 'EDU_API' endpoint_code, 'EDU-local-01' instance_id, 'CLOSED' circuit_state, 0 failure_count, 0 success_count, SYSTIMESTAMP closed_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT' service_id, 'BAT_API' endpoint_code, 'BAT-local-01' instance_id, 'CLOSED' circuit_state, 0 failure_count, 0 success_count, SYSTIMESTAMP closed_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM' service_id, 'ADM_API' endpoint_code, 'ADM-local-01' instance_id, 'CLOSED' circuit_state, 0 failure_count, 0 success_count, SYSTIMESTAMP closed_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET tgt.circuit_state = src.circuit_state, tgt.failure_count = src.failure_count, tgt.success_count = src.success_count, tgt.closed_at = src.closed_at, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.instance_id, src.circuit_state, src.failure_count, src.success_count, src.closed_at, src.created_by, src.updated_by);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'BZA', 'BZA_API', 'BZA-local-01', 'UP', 200, 0, NULL, SYSTIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'BZA' AND endpoint_code = 'BZA_API' AND instance_id = 'BZA-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'EDU', 'EDU_API', 'EDU-local-01', 'UP', 200, 0, NULL, SYSTIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'EDU' AND endpoint_code = 'EDU_API' AND instance_id = 'EDU-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, SYSTIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, SYSTIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);
MERGE INTO BAT_CENTER_CUT_PARAMETER tgt USING (
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'businessDatePattern' parameter_key, 'D+0' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'defaultLimit' parameter_key, '10' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'targetTable' parameter_key, 'REF_CENTER_CUT_SAMPLE_TARGET' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'resultTable' parameter_key, 'REF_CENTER_CUT_SAMPLE_RESULT' parameter_value, 'N' encrypted_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.center_cut_job_id = src.center_cut_job_id AND tgt.parameter_key = src.parameter_key)
WHEN MATCHED THEN UPDATE SET tgt.parameter_value = src.parameter_value, tgt.encrypted_yn = src.encrypted_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by) VALUES (src.center_cut_job_id, src.parameter_key, src.parameter_value, src.encrypted_yn, src.use_yn, src.created_by, src.updated_by);

-- CPF_LOGICAL_DATABASE=bzaDB
MERGE INTO BZA_ORGANIZATION tgt USING (
SELECT 'SAMPLE_ROOT' organization_code, NULL parent_organization_code, '샘플 본부' organization_name, 'COMPANY' organization_type, 10 sort_order, SYSTIMESTAMP effective_from, NULL effective_to, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_DEV' organization_code, 'SAMPLE_ROOT' parent_organization_code, '샘플 개발부' organization_name, 'DEPARTMENT' organization_type, 20 sort_order, SYSTIMESTAMP effective_from, NULL effective_to, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.organization_code = src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code = src.parent_organization_code, tgt.organization_name = src.organization_name, tgt.organization_type = src.organization_type, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BZA_POSITION tgt USING (
SELECT 'SAMPLE_P1' position_code, '샘플 일반' position_name, 10 rank_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_P2' position_code, '샘플 책임' position_name, 20 rank_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.position_code = src.position_code)
WHEN MATCHED THEN UPDATE SET tgt.position_name = src.position_name, tgt.rank_order = src.rank_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BZA_JOB_TITLE tgt USING (
SELECT 'SAMPLE_MEMBER' job_title_code, '샘플 구성원' job_title_name, 'N' manager_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SAMPLE_MANAGER' job_title_code, '샘플 부서장' job_title_name, 'Y' manager_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_title_code = src.job_title_code)
WHEN MATCHED THEN UPDATE SET tgt.job_title_name = src.job_title_name, tgt.manager_yn = src.manager_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO BZA_EMPLOYEE tgt USING (
SELECT 'SAMPLE0001' employee_no, NULL admin_user_id, 'SAMPLE_DEV' organization_code, '샘플 결재자' employee_name, 'SAMPLE_P2' position_code, 'SAMPLE_MANAGER' job_title_code, NULL manager_employee_no, 'ACTIVE' employment_status, CURRENT_DATE join_date, NULL leave_date, NULL email, NULL mobile_no, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SAMPLE0002' employee_no, NULL admin_user_id, 'SAMPLE_DEV' organization_code, '샘플 요청자' employee_name, 'SAMPLE_P1' position_code, 'SAMPLE_MEMBER' job_title_code, 'SAMPLE0001' manager_employee_no, 'ACTIVE' employment_status, CURRENT_DATE join_date, NULL leave_date, NULL email, NULL mobile_no, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.admin_user_id = src.admin_user_id)
WHEN MATCHED THEN UPDATE SET tgt.organization_code = src.organization_code, tgt.employee_name = src.employee_name, tgt.position_code = src.position_code, tgt.job_title_code = src.job_title_code, tgt.manager_employee_no = src.manager_employee_no, tgt.employment_status = src.employment_status, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by) VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.manager_employee_no, src.employment_status, src.join_date, src.leave_date, src.email, src.mobile_no, src.use_yn, src.created_by, src.updated_by);
INSERT INTO BZA_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', SYSTIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
FROM (
    SELECT 'SAMPLE0001' employee_no, 'SAMPLE_DEV' organization_code, 'SAMPLE_P2' position_code, 'SAMPLE_MANAGER' job_title_code
    UNION ALL
    SELECT 'SAMPLE0002', 'SAMPLE_DEV', 'SAMPLE_P1', 'SAMPLE_MEMBER'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_EMPLOYEE_ASSIGNMENT a
    WHERE a.employee_no = v.employee_no AND a.organization_code = v.organization_code
      AND a.primary_yn = 'Y' AND a.effective_to IS NULL
);
INSERT INTO BZA_ORGANIZATION_RESPONSIBILITY (organization_code, responsibility_type, employee_no, effective_from, effective_to, priority_no, use_yn, created_by, updated_by) SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', SYSTIMESTAMP, NULL, 1, 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_ORGANIZATION_RESPONSIBILITY
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);
MERGE INTO BZA_APPROVAL_POLICY tgt USING (
SELECT 'SAMPLE_STANDARD_APPROVAL' policy_code, 1 policy_version, '샘플 표준 결재' policy_name, 'SAMPLE' business_domain, 'STANDARD' approval_type, SYSTIMESTAMP effective_from, NULL effective_to, 'Y' enabled_yn, 'N' self_approval_allowed_yn, 'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version)
WHEN MATCHED THEN UPDATE SET tgt.policy_name = src.policy_name, tgt.enabled_yn = src.enabled_yn, tgt.self_approval_allowed_yn = src.self_approval_allowed_yn, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by) VALUES (src.policy_code, src.policy_version, src.policy_name, src.business_domain, src.approval_type, src.effective_from, src.effective_to, src.enabled_yn, src.self_approval_allowed_yn, src.description, src.created_by, src.updated_by);
MERGE INTO BZA_APPROVAL_POLICY_STEP tgt USING (
SELECT 'SAMPLE_STANDARD_APPROVAL' policy_code, 1 policy_version, 1 step_no, 'APPROVAL' step_type, 'ORG_MANAGER' target_type, 'SAMPLE_DEV' target_code, 'ALL' decision_rule, NULL required_count, 'Y' required_yn, 10 sort_order, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version AND tgt.step_no = src.step_no AND tgt.target_type = src.target_type AND tgt.target_code = src.target_code)
WHEN MATCHED THEN UPDATE SET tgt.step_type = src.step_type, tgt.target_type = src.target_type, tgt.target_code = src.target_code, tgt.decision_rule = src.decision_rule, tgt.required_count = src.required_count, tgt.required_yn = src.required_yn, tgt.sort_order = src.sort_order, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by) VALUES (src.policy_code, src.policy_version, src.step_no, src.step_type, src.target_type, src.target_code, src.decision_rule, src.required_count, src.required_yn, src.sort_order, src.created_by, src.updated_by);

-- ===== END 58_reference_runtime_seed.sql =====

-- ===== BEGIN 59_adm_local_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=59_adm_local_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO ADM_IP_ALLOWLIST tgt USING (
SELECT '127.0.0.1' IP_PATTERN, '로컬 개발 PC' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.IP_PATTERN = src.IP_PATTERN)
WHEN MATCHED THEN UPDATE SET tgt.DESCRIPTION = src.DESCRIPTION, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.IP_PATTERN, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
INSERT INTO ADM_AUDIT_LOG (TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, REQUEST_BODY, CLIENT_IP, created_by, updated_by) SELECT
    '20260724120000000ADMadmUI010000001',
    '20260724120000000ADMadmUI010000001',
    'admin',
    'DASHBOARD',
    'SEED',
    'ADM',
    'INITIAL_DATA',
    'ADM 초기 데이터 등록',
    NULL,
    '127.0.0.1',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM ADM_AUDIT_LOG
    WHERE TRANSACTION_ID = '20260724120000000ADMadmUI010000001'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);

-- ===== END 59_adm_local_seed.sql =====
