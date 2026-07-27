-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=58_reference_runtime_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=batDB
-- CPF_USE_LOGICAL_DATABASE=batDB
MERGE INTO bat_instance tgt
USING (VALUES
  ('local-batch-01', '로컬 배치 인스턴스', 'localhost', 8099, 'Y', CURRENT_TIMESTAMP, 'REF EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스', 'SYSTEM', 'SYSTEM')
) AS src(instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by)
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
USING (VALUES
  ('local-batch-01', 'local-batch-01', 'localhost', 'seed', 'seed-main', 'IDLE', 'Y', CURRENT_TIMESTAMP, NULL, NULL, '로컬 smoke 검증용 배치 worker heartbeat', 'SYSTEM', 'SYSTEM')
) AS src(worker_id, server_instance_id, host_name, process_id, thread_name, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by)
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
USING (VALUES
  ('CPF_EDU_TASKLET_JOB', 'CPF 교육 Tasklet Job', 'TASKLET', '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_EDU_CHUNK_JOB', 'CPF 교육 Chunk Job', 'CHUNK', '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_EDU_RETRY_JOB', 'CPF 교육 재처리 Job', 'RETRY', '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
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
USING (VALUES
  ('CPF_EDU_TASKLET_DAILY', 'CPF_EDU_TASKLET_JOB', '0 0 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:00:00', '04:00:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF_EDU_CHUNK_DAILY', 'CPF_EDU_CHUNK_JOB', '0 30 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:30:00', '05:30:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM')
) AS src(schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern, timezone, enabled_yn, created_by, updated_by)
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
USING (VALUES
  ('CPF_EDU_CHUNK_JOB', 'CPF_EDU_TASKLET_JOB', 'PREDECESSOR', 'COMPLETED', 'COMPLETED', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_EDU_TASKLET_JOB', 'CPF_EDU_CHUNK_JOB', 'TRIGGER', 'COMPLETED', 'COMPLETED', 20, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by)
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
SELECT
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000REFlocal010000001',
    (CURRENT_TIMESTAMP - INTERVAL '10 minute'),
    (CURRENT_TIMESTAMP - INTERVAL '9 minute'),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
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
    LIMIT 1
), NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', (CURRENT_TIMESTAMP - INTERVAL '10 minute'), (CURRENT_TIMESTAMP - INTERVAL '9 minute'), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
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
    LIMIT 1
)
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO bat_execution_target (execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at, dispatch_status, dispatch_reason, created_by, updated_by)
SELECT
    (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
),
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST((CURRENT_DATE || ' 02:00:00') AS TIMESTAMP),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE (
    SELECT execution_id
    FROM bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM bat_execution_target
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

MERGE INTO cmn_business_calendar_day tgt
USING (VALUES
  ('DEFAULT', CURRENT_DATE, 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 기본 영업일', 'SYSTEM', 'SYSTEM'),
  ('DEFAULT', (CURRENT_DATE + INTERVAL '1 day'), 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 다음 영업일', 'SYSTEM', 'SYSTEM')
) AS src(calendar_id, business_date, business_day_yn, day_type, institution_code, reason, created_by, updated_by)
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
USING (VALUES
  ('CPF_BAT_CENTER_CUT_JOB', 'CPF BAT 센터컷 smoke Job', 'TASKLET', 'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
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
USING (VALUES
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF REF 업무 DB 센터컷 샘플 Job', 'TASKLET', 'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
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
USING (VALUES
  ('CPF_BAT_CENTER_CUT_JOB', 'CPF_BAT_CENTER_CUT_JOB', 'CPF BAT 센터컷 smoke Job', 'batCenterCutSampleTargetProvider', 'batCenterCutSampleHandler', 10, 3, 'Y', 'CPF 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.', 'SYSTEM', 'SYSTEM')
) AS src(center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
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
USING (VALUES
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'CPF REF 업무 DB 센터컷 샘플 Job', 'refCenterCutTargetProvider', 'refCenterCutHandler', 10, 3, 'Y', 'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.', 'SYSTEM', 'SYSTEM')
) AS src(center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
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
USING (VALUES
  ('CPF_BAT_CENTER_CUT_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_BAT_CENTER_CUT_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
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
USING (VALUES
  ('BZA', '업무 백오피스 서비스', 'INTERNAL', 'BZA', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
  ('MBR', '회원 서비스', 'INTERNAL', 'MBR', 'CPF 회원 업무 모듈 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
  ('REF', '온라인 교육 서비스', 'INTERNAL', 'REF', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ACC', '계정 Reference 서비스', 'INTERNAL', 'ACC', '생성기 검증과 MBR 연계에 사용하는 계정 reference 서비스', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
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
USING (VALUES
  ('BZA_API', 'BZA', 'BZA API Endpoint', 'HTTP', 'http://localhost:8091', '/api/bza', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
  ('MBR_API', 'MBR', 'MBR API Endpoint', 'HTTP', 'http://localhost:8081', '/mbr', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
  ('REF_API', 'REF', 'REF API Endpoint', 'HTTP', 'http://localhost:8099', '/ref', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://localhost:8093', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
  ('ACC_API', 'ACC', 'ACC API Endpoint', 'HTTP', 'http://localhost:8082', '/internal/api/v1/accounts', 3000, 1, 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://localhost:8090', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM')
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
  ('BZA-local-01', 'BZA', 'BZA_API', 'BZA local instance', 'http://localhost:8091', 'localhost', 8091, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('MBR-local-01', 'MBR', 'MBR_API', 'MBR local instance', 'http://localhost:8081', 'localhost', 8081, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('REF-local-01', 'REF', 'REF_API', 'REF local instance', 'http://localhost:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('BAT-local-01', 'BAT', 'BAT_API', 'BAT local instance', 'http://localhost:8093', 'localhost', 8093, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('ACC-local-01', 'ACC', 'ACC_API', 'ACC local instance', 'http://localhost:8082', 'localhost', 8082, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('ADM-local-01', 'ADM', 'ADM_API', 'ADM local instance', 'http://localhost:8090', 'localhost', 8090, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM')
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
  tgt.weight = src.weight,
  tgt.active_yn = src.active_yn,
  tgt.last_heartbeat_at = src.last_heartbeat_at,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES (src.instance_id, src.service_id, src.endpoint_code, src.instance_name, src.base_url, src.host_name, src.port_no, src.instance_status, src.weight, src.active_yn, src.last_heartbeat_at, src.created_by, src.updated_by);

MERGE INTO cpf_service_routing_policy tgt
USING (VALUES
  ('BZA', 'BZA_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
  ('MBR', 'MBR_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
  ('REF', 'REF_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
  ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
  ('ACC', 'ACC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
  ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
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

MERGE INTO cpf_service_circuit_state tgt
USING (VALUES
  ('BZA', 'BZA_API', 'BZA-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('MBR', 'MBR_API', 'MBR-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('REF', 'REF_API', 'REF-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('BAT', 'BAT_API', 'BAT-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('ACC', 'ACC_API', 'ACC-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
  ('ADM', 'ADM_API', 'ADM-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM')
) AS src(service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by)
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
SELECT 'BZA', 'BZA_API', 'BZA-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BZA' AND endpoint_code = 'BZA_API' AND instance_id = 'BZA-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'MBR', 'MBR_API', 'MBR-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'MBR' AND endpoint_code = 'MBR_API' AND instance_id = 'MBR-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'REF', 'REF_API', 'REF-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'REF' AND endpoint_code = 'REF_API' AND instance_id = 'REF-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'ACC', 'ACC_API', 'ACC-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ACC' AND endpoint_code = 'ACC_API' AND instance_id = 'ACC-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);


-- CPF_LOGICAL_DATABASE=batDB
-- CPF_USE_LOGICAL_DATABASE=batDB
MERGE INTO bat_center_cut_parameter tgt
USING (VALUES
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'ref_center_cut_sample_target', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
  ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'ref_center_cut_sample_result', 'N', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
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
USING (VALUES
  ('SAMPLE_ROOT', NULL, '샘플 본부', 'COMPANY', 10, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SAMPLE_DEV', 'SAMPLE_ROOT', '샘플 개발부', 'DEPARTMENT', 20, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
ON (tgt.organization_code = src.organization_code)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_organization_code = src.parent_organization_code,
  tgt.organization_name = src.organization_name,
  tgt.organization_type = src.organization_type,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_position tgt
USING (VALUES
  ('SAMPLE_P1', '샘플 일반', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SAMPLE_P2', '샘플 책임', 20, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(position_code, position_name, rank_order, use_yn, created_by, updated_by)
ON (tgt.position_code = src.position_code)
WHEN MATCHED THEN UPDATE SET
  tgt.position_name = src.position_name,
  tgt.rank_order = src.rank_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_job_title tgt
USING (VALUES
  ('SAMPLE_MEMBER', '샘플 구성원', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
  ('SAMPLE_MANAGER', '샘플 부서장', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
ON (tgt.job_title_code = src.job_title_code)
WHEN MATCHED THEN UPDATE SET
  tgt.job_title_name = src.job_title_name,
  tgt.manager_yn = src.manager_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_employee tgt
USING (VALUES
  ('SAMPLE0001', NULL, 'SAMPLE_DEV', '샘플 결재자', 'SAMPLE_P2', 'SAMPLE_MANAGER', NULL, 'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SAMPLE0002', NULL, 'SAMPLE_DEV', '샘플 요청자', 'SAMPLE_P1', 'SAMPLE_MEMBER', 'SAMPLE0001', 'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by)
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
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by)
VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.manager_employee_no, src.employment_status, src.join_date, src.leave_date, src.email, src.mobile_no, src.use_yn, src.created_by, src.updated_by);

INSERT INTO bza_employee_assignment (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
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
SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', CURRENT_TIMESTAMP, NULL, 1, 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM bza_organization_responsibility
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);

MERGE INTO bza_approval_policy tgt
USING (VALUES
  ('SAMPLE_STANDARD_APPROVAL', 1, '샘플 표준 결재', 'SAMPLE', 'STANDARD', CURRENT_TIMESTAMP, NULL, 'Y', 'N', 'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책', 'SYSTEM', 'SYSTEM')
) AS src(policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by)
ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version)
WHEN MATCHED THEN UPDATE SET
  tgt.policy_name = src.policy_name,
  tgt.enabled_yn = src.enabled_yn,
  tgt.self_approval_allowed_yn = src.self_approval_allowed_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by)
VALUES (src.policy_code, src.policy_version, src.policy_name, src.business_domain, src.approval_type, src.effective_from, src.effective_to, src.enabled_yn, src.self_approval_allowed_yn, src.description, src.created_by, src.updated_by);

MERGE INTO bza_approval_policy_step tgt
USING (VALUES
  ('SAMPLE_STANDARD_APPROVAL', 1, 1, 'APPROVAL', 'ORG_MANAGER', 'SAMPLE_DEV', 'ALL', NULL, 'Y', 10, 'SYSTEM', 'SYSTEM')
) AS src(policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by)
ON (tgt.policy_code = src.policy_code AND tgt.policy_version = src.policy_version AND tgt.step_no = src.step_no AND tgt.target_type = src.target_type AND tgt.target_code = src.target_code)
WHEN MATCHED THEN UPDATE SET
  tgt.step_type = src.step_type,
  tgt.decision_rule = src.decision_rule,
  tgt.required_count = src.required_count,
  tgt.required_yn = src.required_yn,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by)
VALUES (src.policy_code, src.policy_version, src.step_no, src.step_type, src.target_type, src.target_code, src.decision_rule, src.required_count, src.required_yn, src.sort_order, src.created_by, src.updated_by);
