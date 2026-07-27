-- CPF generated lifecycle bundle; vendor=oracle
-- Source of truth: cpf-tools/db/vendor/oracle/source + database-source-plan.json

-- ===== BEGIN 55_cmn_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=55_cmn_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cmnDB
-- CPF_USE_LOGICAL_DATABASE=cmnDB
MERGE INTO cmn_sample_item tgt
USING (
  SELECT 'CMN-SAMPLE-001' AS sample_key, 'CPF CMN 기본 샘플' AS item_name, 'DATABASE' AS category_code, 'ACTIVE' AS status_code, 'connection migration crud search offset slice cursor' AS searchable_text, NULL AS owner_reference, 10 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual
  UNION ALL
  SELECT 'CMN-SAMPLE-002' AS sample_key, 'CPF CMN 비활성 샘플' AS item_name, 'VALIDATION' AS category_code, 'INACTIVE' AS status_code, 'validation duplicate optimistic-lock rollback' AS searchable_text, NULL AS owner_reference, 20 AS sort_order, 0 AS version_no, 'CMN_SAMPLE' AS created_by, 'CMN_SAMPLE' AS updated_by FROM dual
) src
ON (tgt.sample_key = src.sample_key)
WHEN MATCHED THEN UPDATE SET
  tgt.item_name = src.item_name,
  tgt.category_code = src.category_code,
  tgt.status_code = src.status_code,
  tgt.searchable_text = src.searchable_text,
  tgt.owner_reference = src.owner_reference,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);

-- ===== END 55_cmn_seed_data.sql =====

-- ===== BEGIN 58_reference_external_edu_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=58_reference_external_edu_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_service tgt
USING (
  SELECT 'REF' AS service_id, 'CPF 참조 서비스' AS service_name, 'INTERNAL' AS service_type, 'REF' AS owner_module_code, 'REF EDU 대외연계 결과 불명 검증 대상' AS description, 'Y' AS use_yn, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual
) src
ON (tgt.service_id = src.service_id)
WHEN MATCHED THEN UPDATE SET
  tgt.service_name = src.service_name,
  tgt.owner_module_code = src.owner_module_code,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO cpf_service_endpoint tgt
USING (
  SELECT 'REF-EXTERNAL-SIMULATOR' AS endpoint_code, 'REF' AS service_id, 'REF 대외 시뮬레이터' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://127.0.0.1:8099' AS base_url, '' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual
) src
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
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);

MERGE INTO cpf_service_instance tgt
USING (
  SELECT 'REF-EXT-SIM-local-01' AS instance_id, 'REF' AS service_id, 'REF-EXTERNAL-SIMULATOR' AS endpoint_code, 'REF 대외 시뮬레이터 인스턴스' AS instance_name, 'http://127.0.0.1:8099' AS base_url, 'localhost' AS host_name, 8099 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual
) src
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
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);

MERGE INTO cpf_service_routing_policy tgt
USING (
  SELECT 'REF' AS service_id, 'REF-EXTERNAL-SIMULATOR' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'N' AS failover_enabled_yn, 'N' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SEED' AS created_by, 'SEED' AS updated_by FROM dual
) src
ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.priority = src.priority)
WHEN MATCHED THEN UPDATE SET
  tgt.routing_mode = src.routing_mode,
  tgt.load_balance_type = src.load_balance_type,
  tgt.failover_enabled_yn = src.failover_enabled_yn,
  tgt.health_check_required_yn = src.health_check_required_yn,
  tgt.active_yn = src.active_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);

-- ===== END 58_reference_external_edu_seed.sql =====

-- ===== BEGIN 58_reference_runtime_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=58_reference_runtime_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=batDB
-- CPF_USE_LOGICAL_DATABASE=batDB
MERGE INTO bat_instance tgt
USING (
  SELECT 'local-batch-01' AS instance_id, '로컬 배치 인스턴스' AS instance_name, 'localhost' AS host_name, 8099 AS server_port, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'REF EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET
  tgt.instance_name = src.instance_name,
  tgt.host_name = src.host_name,
  tgt.server_port = src.server_port,
  tgt.active_yn = src.active_yn,
  tgt.last_heartbeat_at = src.last_heartbeat_at,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by)
VALUES (src.instance_id, src.instance_name, src.host_name, src.server_port, src.active_yn, src.last_heartbeat_at, src.description, src.created_by, src.updated_by);

MERGE INTO bat_worker tgt
USING (
  SELECT 'local-batch-01' AS worker_id, 'local-batch-01' AS server_instance_id, 'localhost' AS host_name, 'seed' AS process_id, 'seed-main' AS thread_name, 'IDLE' AS worker_status, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, NULL AS current_job_id, NULL AS current_execution_id, '로컬 smoke 검증용 배치 worker heartbeat' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.worker_id = src.worker_id)
WHEN MATCHED THEN UPDATE SET
  tgt.server_instance_id = src.server_instance_id,
  tgt.host_name = src.host_name,
  tgt.process_id = src.process_id,
  tgt.thread_name = src.thread_name,
  tgt.worker_status = src.worker_status,
  tgt.active_yn = src.active_yn,
  tgt.last_heartbeat_at = src.last_heartbeat_at,
  tgt.current_job_id = src.current_job_id,
  tgt.current_execution_id = src.current_execution_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (worker_id, server_instance_id, host_name, process_id, thread_name, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by)
VALUES (src.worker_id, src.server_instance_id, src.host_name, src.process_id, src.thread_name, src.worker_status, src.active_yn, src.last_heartbeat_at, src.current_job_id, src.current_execution_id, src.description, src.created_by, src.updated_by);

MERGE INTO bat_job tgt
USING (
  SELECT 'CPF_EDU_TASKLET_JOB' AS job_id, 'CPF 교육 Tasklet Job' AS job_name, 'TASKLET' AS job_type, '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.' AS description, 'Y' AS restartable_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_EDU_CHUNK_JOB' AS job_id, 'CPF 교육 Chunk Job' AS job_name, 'CHUNK' AS job_type, '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.' AS description, 'Y' AS restartable_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_EDU_RETRY_JOB' AS job_id, 'CPF 교육 재처리 Job' AS job_name, 'RETRY' AS job_type, '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.' AS description, 'Y' AS restartable_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET
  tgt.job_name = src.job_name,
  tgt.job_type = src.job_type,
  tgt.description = src.description,
  tgt.restartable_yn = src.restartable_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bat_schedule tgt
USING (
  SELECT 'CPF_EDU_TASKLET_DAILY' AS schedule_id, 'CPF_EDU_TASKLET_JOB' AS job_id, '0 0 2 * * *' AS cron_expression, 'DEFAULT' AS calendar_id, 'Y' AS business_day_only_yn, 'SKIP' AS holiday_policy, '02:00:00' AS available_start_time, '04:00:00' AS available_end_time, 'D+0' AS run_date_pattern, 'Asia/Seoul' AS timezone, 'N' AS enabled_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_EDU_CHUNK_DAILY' AS schedule_id, 'CPF_EDU_CHUNK_JOB' AS job_id, '0 30 2 * * *' AS cron_expression, 'DEFAULT' AS calendar_id, 'Y' AS business_day_only_yn, 'SKIP' AS holiday_policy, '02:30:00' AS available_start_time, '05:30:00' AS available_end_time, 'D+0' AS run_date_pattern, 'Asia/Seoul' AS timezone, 'N' AS enabled_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.schedule_id = src.schedule_id)
WHEN MATCHED THEN UPDATE SET
  tgt.job_id = src.job_id,
  tgt.cron_expression = src.cron_expression,
  tgt.calendar_id = src.calendar_id,
  tgt.business_day_only_yn = src.business_day_only_yn,
  tgt.holiday_policy = src.holiday_policy,
  tgt.available_start_time = src.available_start_time,
  tgt.available_end_time = src.available_end_time,
  tgt.run_date_pattern = src.run_date_pattern,
  tgt.timezone = src.timezone,
  tgt.enabled_yn = src.enabled_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern, timezone, enabled_yn, created_by, updated_by)
VALUES (src.schedule_id, src.job_id, src.cron_expression, src.calendar_id, src.business_day_only_yn, src.holiday_policy, src.available_start_time, src.available_end_time, src.run_date_pattern, src.timezone, src.enabled_yn, src.created_by, src.updated_by);

MERGE INTO bat_job_relation tgt
USING (
  SELECT 'CPF_EDU_CHUNK_JOB' AS job_id, 'CPF_EDU_TASKLET_JOB' AS related_job_id, 'PREDECESSOR' AS relation_type, 'COMPLETED' AS trigger_condition, 'COMPLETED' AS required_status, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_EDU_TASKLET_JOB' AS job_id, 'CPF_EDU_CHUNK_JOB' AS related_job_id, 'TRIGGER' AS relation_type, 'COMPLETED' AS trigger_condition, 'COMPLETED' AS required_status, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.job_id = src.job_id AND tgt.related_job_id = src.related_job_id AND tgt.relation_type = src.relation_type)
WHEN MATCHED THEN UPDATE SET
  tgt.trigger_condition = src.trigger_condition,
  tgt.required_status = src.required_status,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by)
VALUES (src.job_id, src.related_job_id, src.relation_type, src.trigger_condition, src.required_status, src.sort_order, src.use_yn, src.created_by, src.updated_by);

INSERT INTO bat_execution (job_id, schedule_id, job_parameters, execution_status, batch_instance_id, server_instance_id, worker_id, transaction_id, start_time, end_time, read_count, write_count, skip_count, requested_by, created_by, updated_by)
SELECT 'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000REFlocal010000001',
    (CURRENT_TIMESTAMP(3) - INTERVAL '10' MINUTE),
    (CURRENT_TIMESTAMP(3) - INTERVAL '9' MINUTE),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);

INSERT INTO bat_step_execution (execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status, start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by)
SELECT (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY
), NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', (CURRENT_TIMESTAMP(3) - INTERVAL '10' MINUTE), (CURRENT_TIMESTAMP(3) - INTERVAL '9' MINUTE), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM' FROM dual WHERE (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bat_step_execution
      WHERE execution_id = (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY
)
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO bat_execution_target (execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at, dispatch_status, dispatch_reason, created_by, updated_by)
SELECT (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY
),
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST((CURRENT_DATE || ' 02:00:00') AS TIMESTAMP),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM' FROM dual WHERE (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROWS ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bat_execution_target
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

MERGE INTO cmn_business_calendar_day tgt
USING (
  SELECT 'DEFAULT' AS calendar_id, CURRENT_DATE AS business_date, 'Y' AS business_day_yn, 'BUSINESS' AS day_type, NULL AS institution_code, '로컬 smoke 검증용 기본 영업일' AS reason, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'DEFAULT' AS calendar_id, (CURRENT_DATE + INTERVAL '1' DAY) AS business_date, 'Y' AS business_day_yn, 'BUSINESS' AS day_type, NULL AS institution_code, '로컬 smoke 검증용 다음 영업일' AS reason, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.calendar_id = src.calendar_id AND tgt.business_date = src.business_date)
WHEN MATCHED THEN UPDATE SET
  tgt.business_day_yn = src.business_day_yn,
  tgt.day_type = src.day_type,
  tgt.institution_code = src.institution_code,
  tgt.reason = src.reason,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (calendar_id, business_date, business_day_yn, day_type, institution_code, reason, created_by, updated_by)
VALUES (src.calendar_id, src.business_date, src.business_day_yn, src.day_type, src.institution_code, src.reason, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=batDB
-- CPF_USE_LOGICAL_DATABASE=batDB
MERGE INTO bat_job tgt
USING (
  SELECT 'CPF_BAT_CENTER_CUT_JOB' AS job_id, 'CPF BAT 센터컷 smoke Job' AS job_name, 'TASKLET' AS job_type, 'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.' AS description, 'Y' AS restartable_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET
  tgt.job_name = src.job_name,
  tgt.job_type = src.job_type,
  tgt.description = src.description,
  tgt.restartable_yn = src.restartable_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bat_job tgt
USING (
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS job_id, 'CPF REF 업무 DB 센터컷 샘플 Job' AS job_name, 'TASKLET' AS job_type, 'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.' AS description, 'Y' AS restartable_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.job_id = src.job_id)
WHEN MATCHED THEN UPDATE SET
  tgt.job_name = src.job_name,
  tgt.job_type = src.job_type,
  tgt.description = src.description,
  tgt.restartable_yn = src.restartable_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES (src.job_id, src.job_name, src.job_type, src.description, src.restartable_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bat_center_cut_job tgt
USING (
  SELECT 'CPF_BAT_CENTER_CUT_JOB' AS center_cut_job_id, 'CPF_BAT_CENTER_CUT_JOB' AS batch_job_id, 'CPF BAT 센터컷 smoke Job' AS center_cut_job_name, 'batCenterCutSampleTargetProvider' AS provider_key, 'batCenterCutSampleHandler' AS handler_key, 10 AS chunk_size, 3 AS retry_limit, 'Y' AS use_yn, 'CPF 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.center_cut_job_id = src.center_cut_job_id)
WHEN MATCHED THEN UPDATE SET
  tgt.batch_job_id = src.batch_job_id,
  tgt.center_cut_job_name = src.center_cut_job_name,
  tgt.provider_key = src.provider_key,
  tgt.handler_key = src.handler_key,
  tgt.chunk_size = src.chunk_size,
  tgt.retry_limit = src.retry_limit,
  tgt.use_yn = src.use_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
VALUES (src.center_cut_job_id, src.batch_job_id, src.center_cut_job_name, src.provider_key, src.handler_key, src.chunk_size, src.retry_limit, src.use_yn, src.description, src.created_by, src.updated_by);

MERGE INTO bat_center_cut_job tgt
USING (
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS batch_job_id, 'CPF REF 업무 DB 센터컷 샘플 Job' AS center_cut_job_name, 'refCenterCutTargetProvider' AS provider_key, 'refCenterCutHandler' AS handler_key, 10 AS chunk_size, 3 AS retry_limit, 'Y' AS use_yn, 'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.center_cut_job_id = src.center_cut_job_id)
WHEN MATCHED THEN UPDATE SET
  tgt.batch_job_id = src.batch_job_id,
  tgt.center_cut_job_name = src.center_cut_job_name,
  tgt.provider_key = src.provider_key,
  tgt.handler_key = src.handler_key,
  tgt.chunk_size = src.chunk_size,
  tgt.retry_limit = src.retry_limit,
  tgt.use_yn = src.use_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
VALUES (src.center_cut_job_id, src.batch_job_id, src.center_cut_job_name, src.provider_key, src.handler_key, src.chunk_size, src.retry_limit, src.use_yn, src.description, src.created_by, src.updated_by);

MERGE INTO bat_center_cut_parameter tgt
USING (
  SELECT 'CPF_BAT_CENTER_CUT_JOB' AS center_cut_job_id, 'businessDatePattern' AS parameter_key, 'D+0' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_BAT_CENTER_CUT_JOB' AS center_cut_job_id, 'defaultLimit' AS parameter_key, '10' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.center_cut_job_id = src.center_cut_job_id AND tgt.parameter_key = src.parameter_key)
WHEN MATCHED THEN UPDATE SET
  tgt.parameter_value = src.parameter_value,
  tgt.encrypted_yn = src.encrypted_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
VALUES (src.center_cut_job_id, src.parameter_key, src.parameter_value, src.encrypted_yn, src.use_yn, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_service tgt
USING (
  SELECT 'BZA' AS service_id, '업무 백오피스 서비스' AS service_name, 'INTERNAL' AS service_type, 'BZA' AS owner_module_code, 'CPF 업무 운영 백오피스 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBR' AS service_id, '회원 서비스' AS service_name, 'INTERNAL' AS service_type, 'MBR' AS owner_module_code, 'CPF 회원 업무 모듈 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'REF' AS service_id, '온라인 교육 서비스' AS service_name, 'INTERNAL' AS service_type, 'REF' AS owner_module_code, 'CPF 온라인 교육 및 검증 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BAT' AS service_id, '배치 Worker 서비스' AS service_name, 'INTERNAL' AS service_type, 'BAT' AS owner_module_code, 'CPF 배치 Worker 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ACC' AS service_id, '계정 Reference 서비스' AS service_name, 'INTERNAL' AS service_type, 'ACC' AS owner_module_code, '생성기 검증과 MBR 연계에 사용하는 계정 reference 서비스' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM' AS service_id, '운영 콘솔 서비스' AS service_name, 'INTERNAL' AS service_type, 'ADM' AS owner_module_code, 'CPF 운영 콘솔 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.service_id = src.service_id)
WHEN MATCHED THEN UPDATE SET
  tgt.service_name = src.service_name,
  tgt.service_type = src.service_type,
  tgt.owner_module_code = src.owner_module_code,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO cpf_service_endpoint tgt
USING (
  SELECT 'BZA_API' AS endpoint_code, 'BZA' AS service_id, 'BZA API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8091' AS base_url, '/api/bza' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBR_API' AS endpoint_code, 'MBR' AS service_id, 'MBR API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8081' AS base_url, '/mbr' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'REF_API' AS endpoint_code, 'REF' AS service_id, 'REF API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8099' AS base_url, '/ref' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BAT_API' AS endpoint_code, 'BAT' AS service_id, 'BAT API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8093' AS base_url, '/bat' AS context_path, 5000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ACC_API' AS endpoint_code, 'ACC' AS service_id, 'ACC API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8082' AS base_url, '/internal/api/v1/accounts' AS context_path, 3000 AS default_timeout_ms, 1 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM_API' AS endpoint_code, 'ADM' AS service_id, 'ADM API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://localhost:8090' AS base_url, '/adm' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'BZA-local-01' AS instance_id, 'BZA' AS service_id, 'BZA_API' AS endpoint_code, 'BZA local instance' AS instance_name, 'http://localhost:8091' AS base_url, 'localhost' AS host_name, 8091 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBR-local-01' AS instance_id, 'MBR' AS service_id, 'MBR_API' AS endpoint_code, 'MBR local instance' AS instance_name, 'http://localhost:8081' AS base_url, 'localhost' AS host_name, 8081 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'REF-local-01' AS instance_id, 'REF' AS service_id, 'REF_API' AS endpoint_code, 'REF local instance' AS instance_name, 'http://localhost:8099' AS base_url, 'localhost' AS host_name, 8099 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BAT-local-01' AS instance_id, 'BAT' AS service_id, 'BAT_API' AS endpoint_code, 'BAT local instance' AS instance_name, 'http://localhost:8093' AS base_url, 'localhost' AS host_name, 8093 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ACC-local-01' AS instance_id, 'ACC' AS service_id, 'ACC_API' AS endpoint_code, 'ACC local instance' AS instance_name, 'http://localhost:8082' AS base_url, 'localhost' AS host_name, 8082 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM-local-01' AS instance_id, 'ADM' AS service_id, 'ADM_API' AS endpoint_code, 'ADM local instance' AS instance_name, 'http://localhost:8090' AS base_url, 'localhost' AS host_name, 8090 AS port_no, 'UP' AS instance_status, 100 AS weight, 'Y' AS active_yn, CURRENT_TIMESTAMP(3) AS last_heartbeat_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET
  tgt.service_id = src.service_id,
  tgt.endpoint_code = src.endpoint_code,
  tgt.instance_name = src.instance_name,
  tgt.base_url = src.base_url,
  tgt.host_name = src.host_name,
  tgt.port_no = src.port_no,
  tgt.instance_status = src.instance_status,
  tgt.weight = src.weight,
  tgt.active_yn = src.active_yn,
  tgt.last_heartbeat_at = src.last_heartbeat_at,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);

MERGE INTO cpf_service_routing_policy tgt
USING (
  SELECT 'BZA' AS service_id, 'BZA_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBR' AS service_id, 'MBR_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'REF' AS service_id, 'REF_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BAT' AS service_id, 'BAT_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ACC' AS service_id, 'ACC_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM' AS service_id, 'ADM_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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

MERGE INTO cpf_service_circuit_state tgt
USING (
  SELECT 'BZA' AS service_id, 'BZA_API' AS endpoint_code, 'BZA-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBR' AS service_id, 'MBR_API' AS endpoint_code, 'MBR-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'REF' AS service_id, 'REF_API' AS endpoint_code, 'REF-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BAT' AS service_id, 'BAT_API' AS endpoint_code, 'BAT-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ACC' AS service_id, 'ACC_API' AS endpoint_code, 'ACC-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM' AS service_id, 'ADM_API' AS endpoint_code, 'ADM-local-01' AS instance_id, 'CLOSED' AS circuit_state, 0 AS failure_count, 0 AS success_count, CURRENT_TIMESTAMP(3) AS closed_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.service_id = src.service_id AND tgt.endpoint_code = src.endpoint_code AND tgt.instance_id = src.instance_id)
WHEN MATCHED THEN UPDATE SET
  tgt.circuit_state = src.circuit_state,
  tgt.failure_count = src.failure_count,
  tgt.success_count = src.success_count,
  tgt.closed_at = src.closed_at,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by)
VALUES (src.service_id, src.endpoint_code, src.instance_id, src.circuit_state, src.failure_count, src.success_count, src.closed_at, src.created_by, src.updated_by);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'BZA', 'BZA_API', 'BZA-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BZA' AND endpoint_code = 'BZA_API' AND instance_id = 'BZA-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'MBR', 'MBR_API', 'MBR-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'MBR' AND endpoint_code = 'MBR_API' AND instance_id = 'MBR-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'REF', 'REF_API', 'REF-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'REF' AND endpoint_code = 'REF_API' AND instance_id = 'REF-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'ACC', 'ACC_API', 'ACC-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ACC' AND endpoint_code = 'ACC_API' AND instance_id = 'ACC-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);


-- CPF_LOGICAL_DATABASE=batDB
-- CPF_USE_LOGICAL_DATABASE=batDB
MERGE INTO bat_center_cut_parameter tgt
USING (
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'businessDatePattern' AS parameter_key, 'D+0' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'defaultLimit' AS parameter_key, '10' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'targetTable' AS parameter_key, 'ref_center_cut_sample_target' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF_REF_CENTER_CUT_SAMPLE_JOB' AS center_cut_job_id, 'resultTable' AS parameter_key, 'ref_center_cut_sample_result' AS parameter_value, 'N' AS encrypted_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.center_cut_job_id = src.center_cut_job_id AND tgt.parameter_key = src.parameter_key)
WHEN MATCHED THEN UPDATE SET
  tgt.parameter_value = src.parameter_value,
  tgt.encrypted_yn = src.encrypted_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
VALUES (src.center_cut_job_id, src.parameter_key, src.parameter_value, src.encrypted_yn, src.use_yn, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=bzaDB
-- CPF_USE_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_organization tgt
USING (
  SELECT 'SAMPLE_ROOT' AS organization_code, NULL AS parent_organization_code, '샘플 본부' AS organization_name, 'COMPANY' AS organization_type, 10 AS sort_order, CURRENT_TIMESTAMP(3) AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SAMPLE_DEV' AS organization_code, 'SAMPLE_ROOT' AS parent_organization_code, '샘플 개발부' AS organization_name, 'DEPARTMENT' AS organization_type, 20 AS sort_order, CURRENT_TIMESTAMP(3) AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.organization_code = src.organization_code)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_organization_code = src.parent_organization_code,
  tgt.organization_name = src.organization_name,
  tgt.organization_type = src.organization_type,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_position tgt
USING (
  SELECT 'SAMPLE_P1' AS position_code, '샘플 일반' AS position_name, 10 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SAMPLE_P2' AS position_code, '샘플 책임' AS position_name, 20 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.position_code = src.position_code)
WHEN MATCHED THEN UPDATE SET
  tgt.position_name = src.position_name,
  tgt.rank_order = src.rank_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_job_title tgt
USING (
  SELECT 'SAMPLE_MEMBER' AS job_title_code, '샘플 구성원' AS job_title_name, 'N' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SAMPLE_MANAGER' AS job_title_code, '샘플 부서장' AS job_title_name, 'Y' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.job_title_code = src.job_title_code)
WHEN MATCHED THEN UPDATE SET
  tgt.job_title_name = src.job_title_name,
  tgt.manager_yn = src.manager_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_employee tgt
USING (
  SELECT 'SAMPLE0001' AS employee_no, NULL AS admin_user_id, 'SAMPLE_DEV' AS organization_code, '샘플 결재자' AS employee_name, 'SAMPLE_P2' AS position_code, 'SAMPLE_MANAGER' AS job_title_code, NULL AS manager_employee_no, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, NULL AS leave_date, NULL AS email, NULL AS mobile_no, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SAMPLE0002' AS employee_no, NULL AS admin_user_id, 'SAMPLE_DEV' AS organization_code, '샘플 요청자' AS employee_name, 'SAMPLE_P1' AS position_code, 'SAMPLE_MEMBER' AS job_title_code, 'SAMPLE0001' AS manager_employee_no, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, NULL AS leave_date, NULL AS email, NULL AS mobile_no, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.admin_user_id = src.admin_user_id)
WHEN MATCHED THEN UPDATE SET
  tgt.organization_code = src.organization_code,
  tgt.employee_name = src.employee_name,
  tgt.position_code = src.position_code,
  tgt.job_title_code = src.job_title_code,
  tgt.manager_employee_no = src.manager_employee_no,
  tgt.employment_status = src.employment_status,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by)
VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.manager_employee_no, src.employment_status, src.join_date, src.leave_date, src.email, src.mobile_no, src.use_yn, src.created_by, src.updated_by);

INSERT INTO bza_employee_assignment (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', CURRENT_TIMESTAMP(3), NULL, 'SYSTEM', 'SYSTEM'
FROM (
    SELECT 'SAMPLE0001' employee_no, 'SAMPLE_DEV' organization_code, 'SAMPLE_P2' position_code, 'SAMPLE_MANAGER' job_title_code
    UNION ALL
    SELECT 'SAMPLE0002', 'SAMPLE_DEV', 'SAMPLE_P1', 'SAMPLE_MEMBER'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM bza_employee_assignment a
    WHERE a.employee_no = v.employee_no AND a.organization_code = v.organization_code
      AND a.primary_yn = 'Y' AND a.effective_to IS NULL
);

INSERT INTO bza_organization_responsibility (organization_code, responsibility_type, employee_no, effective_from, effective_to, priority_no, use_yn, created_by, updated_by)
SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', CURRENT_TIMESTAMP(3), NULL, 1, 'Y', 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1 FROM bza_organization_responsibility
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);

MERGE INTO bza_approval_policy tgt
USING (
  SELECT 'SAMPLE_STANDARD_APPROVAL' AS policy_code, 1 AS policy_version, '샘플 표준 결재' AS policy_name, 'SAMPLE' AS business_domain, 'STANDARD' AS approval_type, CURRENT_TIMESTAMP(3) AS effective_from, NULL AS effective_to, 'Y' AS enabled_yn, 'N' AS self_approval_allowed_yn, 'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version)
WHEN MATCHED THEN UPDATE SET
  tgt.policy_name = src.policy_name,
  tgt.enabled_yn = src.enabled_yn,
  tgt.self_approval_allowed_yn = src.self_approval_allowed_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by)
VALUES (src.policy_code, src.policy_version, src.policy_name, src.business_domain, src.approval_type, src.effective_from, src.effective_to, src.enabled_yn, src.self_approval_allowed_yn, src.description, src.created_by, src.updated_by);

MERGE INTO bza_approval_policy_step tgt
USING (
  SELECT 'SAMPLE_STANDARD_APPROVAL' AS policy_code, 1 AS policy_version, 1 AS step_no, 'APPROVAL' AS step_type, 'ORG_MANAGER' AS target_type, 'SAMPLE_DEV' AS target_code, 'ALL' AS decision_rule, NULL AS required_count, 'Y' AS required_yn, 10 AS sort_order, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version AND tgt.step_no = src.step_no AND tgt.target_type = src.target_type AND tgt.target_code = src.target_code)
WHEN MATCHED THEN UPDATE SET
  tgt.step_type = src.step_type,
  tgt.decision_rule = src.decision_rule,
  tgt.required_count = src.required_count,
  tgt.required_yn = src.required_yn,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by)
VALUES (src.policy_code, src.policy_version, src.step_no, src.step_type, src.target_type, src.target_code, src.decision_rule, src.required_count, src.required_yn, src.sort_order, src.created_by, src.updated_by);

-- ===== END 58_reference_runtime_seed.sql =====

-- ===== BEGIN 59_adm_local_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=59_adm_local_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=admDB
-- CPF_USE_LOGICAL_DATABASE=admDB
MERGE INTO adm_ip_allowlist tgt
USING (
  SELECT '127.0.0.1' AS IP_PATTERN, '로컬 개발 PC' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.IP_PATTERN = src.IP_PATTERN)
WHEN MATCHED THEN UPDATE SET
  tgt.DESCRIPTION = src.DESCRIPTION,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES (src.IP_PATTERN, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);

INSERT INTO adm_audit_log (TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, REQUEST_BODY, CLIENT_IP, created_by, updated_by)
SELECT '20260724120000000ADMadmUI010000001',
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
    'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1
    FROM adm_audit_log
    WHERE TRANSACTION_ID = '20260724120000000ADMadmUI010000001'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);

-- ===== END 59_adm_local_seed.sql =====
