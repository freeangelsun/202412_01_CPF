-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=58_reference_runtime_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
INSERT INTO BAT_INSTANCE (instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by) VALUES (
    'local-batch-01',
    '로컬 배치 인스턴스',
    'localhost',
    8099,
    'Y',
    CURRENT_TIMESTAMP,
    'EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (instance_id) DO UPDATE SET instance_name = EXCLUDED.instance_name, host_name = EXCLUDED.host_name, server_port = EXCLUDED.server_port, active_yn = EXCLUDED.active_yn, last_heartbeat_at = EXCLUDED.last_heartbeat_at, description = EXCLUDED.description, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_WORKER (worker_id, server_instance_id, host_name, process_id, thread_name, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by) VALUES (
    'local-batch-01',
    'local-batch-01',
    'localhost',
    'seed',
    'seed-main',
    'IDLE',
    'Y',
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    '로컬 smoke 검증용 배치 worker heartbeat',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (worker_id) DO UPDATE SET server_instance_id = EXCLUDED.server_instance_id, host_name = EXCLUDED.host_name, process_id = EXCLUDED.process_id, thread_name = EXCLUDED.thread_name, worker_status = EXCLUDED.worker_status, active_yn = EXCLUDED.active_yn, last_heartbeat_at = EXCLUDED.last_heartbeat_at, current_job_id = EXCLUDED.current_job_id, current_execution_id = EXCLUDED.current_execution_id, description = EXCLUDED.description, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES ('CPF_EDU_TASKLET_JOB', 'CPF 교육 Tasklet Job', 'TASKLET', '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_JOB', 'CPF 교육 Chunk Job', 'CHUNK', '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_RETRY_JOB', 'CPF 교육 재처리 Job', 'RETRY', '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (job_id) DO UPDATE SET job_name = EXCLUDED.job_name, job_type = EXCLUDED.job_type, description = EXCLUDED.description, restartable_yn = EXCLUDED.restartable_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_SCHEDULE (schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern, timezone, enabled_yn, created_by, updated_by) VALUES ('CPF_EDU_TASKLET_DAILY', 'CPF_EDU_TASKLET_JOB', '0 0 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:00:00', '04:00:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_DAILY', 'CPF_EDU_CHUNK_JOB', '0 30 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:30:00', '05:30:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM') ON CONFLICT (schedule_id) DO UPDATE SET job_id = EXCLUDED.job_id, cron_expression = EXCLUDED.cron_expression, calendar_id = EXCLUDED.calendar_id, business_day_only_yn = EXCLUDED.business_day_only_yn, holiday_policy = EXCLUDED.holiday_policy, available_start_time = EXCLUDED.available_start_time, available_end_time = EXCLUDED.available_end_time, run_date_pattern = EXCLUDED.run_date_pattern, timezone = EXCLUDED.timezone, enabled_yn = EXCLUDED.enabled_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_JOB_RELATION (job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by) VALUES ('CPF_EDU_CHUNK_JOB', 'CPF_EDU_TASKLET_JOB', 'PREDECESSOR', 'COMPLETED', 'COMPLETED', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_TASKLET_JOB', 'CPF_EDU_CHUNK_JOB', 'TRIGGER', 'COMPLETED', 'COMPLETED', 20, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (job_id, related_job_id, relation_type) DO UPDATE SET trigger_condition = EXCLUDED.trigger_condition, required_status = EXCLUDED.required_status, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_EXECUTION (job_id, schedule_id, job_parameters, execution_status, batch_instance_id, server_instance_id, worker_id, transaction_id, start_time, end_time, read_count, write_count, skip_count, requested_by, created_by, updated_by) SELECT
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
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);
SELECT (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
) AS cpf_edu_execution_id \\gset
INSERT INTO BAT_STEP_EXECUTION (execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status, start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by) SELECT :cpf_edu_execution_id, NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', (CURRENT_TIMESTAMP - INTERVAL '10 minute'), (CURRENT_TIMESTAMP - INTERVAL '9 minute'), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE :cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_STEP_EXECUTION
      WHERE execution_id = :cpf_edu_execution_id
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );
INSERT INTO BAT_EXECUTION_TARGET (execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at, dispatch_status, dispatch_reason, created_by, updated_by) SELECT
    :cpf_edu_execution_id,
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST((CURRENT_DATE || ' 02:00:00') AS TIMESTAMP),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE :cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_EXECUTION_TARGET
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );
INSERT INTO CMN_BUSINESS_CALENDAR_DAY (calendar_id, business_date, business_day_yn, day_type, institution_code, reason, created_by, updated_by) VALUES ('DEFAULT', CURRENT_DATE, 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 기본 영업일', 'SYSTEM', 'SYSTEM'),
    ('DEFAULT', (CURRENT_DATE + INTERVAL '1 day'), 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 다음 영업일', 'SYSTEM', 'SYSTEM') ON CONFLICT (calendar_id, business_date) DO UPDATE SET business_day_yn = EXCLUDED.business_day_yn, day_type = EXCLUDED.day_type, institution_code = EXCLUDED.institution_code, reason = EXCLUDED.reason, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'TASKLET',
    'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (job_id) DO UPDATE SET job_name = EXCLUDED.job_name, job_type = EXCLUDED.job_type, description = EXCLUDED.description, restartable_yn = EXCLUDED.restartable_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'TASKLET',
    'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (job_id) DO UPDATE SET job_name = EXCLUDED.job_name, job_type = EXCLUDED.job_type, description = EXCLUDED.description, restartable_yn = EXCLUDED.restartable_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_CENTER_CUT_JOB (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'batCenterCutSampleTargetProvider',
    'batCenterCutSampleHandler',
    10,
    3,
    'Y',
    'CPF 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (center_cut_job_id) DO UPDATE SET batch_job_id = EXCLUDED.batch_job_id, center_cut_job_name = EXCLUDED.center_cut_job_name, provider_key = EXCLUDED.provider_key, handler_key = EXCLUDED.handler_key, chunk_size = EXCLUDED.chunk_size, retry_limit = EXCLUDED.retry_limit, use_yn = EXCLUDED.use_yn, description = EXCLUDED.description, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_CENTER_CUT_JOB (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'refCenterCutTargetProvider',
    'refCenterCutHandler',
    10,
    3,
    'Y',
    'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (center_cut_job_id) DO UPDATE SET batch_job_id = EXCLUDED.batch_job_id, center_cut_job_name = EXCLUDED.center_cut_job_name, provider_key = EXCLUDED.provider_key, handler_key = EXCLUDED.handler_key, chunk_size = EXCLUDED.chunk_size, retry_limit = EXCLUDED.retry_limit, use_yn = EXCLUDED.use_yn, description = EXCLUDED.description, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BAT_CENTER_CUT_PARAMETER (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by) VALUES ('CPF_BAT_CENTER_CUT_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_BAT_CENTER_CUT_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (center_cut_job_id, parameter_key) DO UPDATE SET parameter_value = EXCLUDED.parameter_value, encrypted_yn = EXCLUDED.encrypted_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES ('BZA', '업무 백오피스 서비스', 'INTERNAL', 'BZA', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU', '온라인 교육 서비스', 'INTERNAL', 'EDU', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (service_id) DO UPDATE SET service_name = EXCLUDED.service_name, service_type = EXCLUDED.service_type, owner_module_code = EXCLUDED.owner_module_code, description = EXCLUDED.description, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES ('BZA_API', 'BZA', 'BZA API Endpoint', 'HTTP', 'http://localhost:8091', '/api/bza', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU_API', 'EDU', 'EDU API Endpoint', 'HTTP', 'http://localhost:8099', '/education', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://localhost:8093', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://localhost:8090', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (endpoint_code) DO UPDATE SET service_id = EXCLUDED.service_id, endpoint_name = EXCLUDED.endpoint_name, endpoint_type = EXCLUDED.endpoint_type, base_url = EXCLUDED.base_url, context_path = EXCLUDED.context_path, default_timeout_ms = EXCLUDED.default_timeout_ms, default_retry_count = EXCLUDED.default_retry_count, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_INSTANCE (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by) VALUES ('BZA-local-01', 'BZA', 'BZA_API', 'BZA local instance', 'http://localhost:8091', 'localhost', 8091, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('EDU-local-01', 'EDU', 'EDU_API', 'EDU local instance', 'http://localhost:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('BAT-local-01', 'BAT', 'BAT_API', 'BAT local instance', 'http://localhost:8093', 'localhost', 8093, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('ADM-local-01', 'ADM', 'ADM_API', 'ADM local instance', 'http://localhost:8090', 'localhost', 8090, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM') ON CONFLICT (instance_id) DO UPDATE SET service_id = EXCLUDED.service_id, endpoint_code = EXCLUDED.endpoint_code, instance_name = EXCLUDED.instance_name, base_url = EXCLUDED.base_url, host_name = EXCLUDED.host_name, port_no = EXCLUDED.port_no, instance_status = EXCLUDED.instance_status, weight = EXCLUDED.weight, active_yn = EXCLUDED.active_yn, last_heartbeat_at = EXCLUDED.last_heartbeat_at, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES ('BZA', 'BZA_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM') ON CONFLICT (service_id, endpoint_code, priority) DO UPDATE SET routing_mode = EXCLUDED.routing_mode, load_balance_type = EXCLUDED.load_balance_type, failover_enabled_yn = EXCLUDED.failover_enabled_yn, health_check_required_yn = EXCLUDED.health_check_required_yn, active_yn = EXCLUDED.active_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_CIRCUIT_STATE (service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by) VALUES ('BZA', 'BZA_API', 'BZA-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'EDU-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'BAT-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'ADM-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM') ON CONFLICT (service_id, endpoint_code, instance_id) DO UPDATE SET circuit_state = EXCLUDED.circuit_state, failure_count = EXCLUDED.failure_count, success_count = EXCLUDED.success_count, closed_at = EXCLUDED.closed_at, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'BZA', 'BZA_API', 'BZA-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'BZA' AND endpoint_code = 'BZA_API' AND instance_id = 'BZA-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'EDU', 'EDU_API', 'EDU-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'EDU' AND endpoint_code = 'EDU_API' AND instance_id = 'EDU-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by) SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);
INSERT INTO BAT_CENTER_CUT_PARAMETER (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by) VALUES ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'REF_CENTER_CUT_SAMPLE_TARGET', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'REF_CENTER_CUT_SAMPLE_RESULT', 'N', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (center_cut_job_id, parameter_key) DO UPDATE SET parameter_value = EXCLUDED.parameter_value, encrypted_yn = EXCLUDED.encrypted_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;

-- CPF_LOGICAL_DATABASE=bzaDB
INSERT INTO BZA_ORGANIZATION (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES ('SAMPLE_ROOT', NULL, '샘플 본부', 'COMPANY', 10, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_DEV', 'SAMPLE_ROOT', '샘플 개발부', 'DEPARTMENT', 20, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (organization_code) DO UPDATE SET parent_organization_code = EXCLUDED.parent_organization_code, organization_name = EXCLUDED.organization_name, organization_type = EXCLUDED.organization_type, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_POSITION (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES ('SAMPLE_P1', '샘플 일반', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_P2', '샘플 책임', 20, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (position_code) DO UPDATE SET position_name = EXCLUDED.position_name, rank_order = EXCLUDED.rank_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_JOB_TITLE (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES ('SAMPLE_MEMBER', '샘플 구성원', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE_MANAGER', '샘플 부서장', 'Y', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (job_title_code) DO UPDATE SET job_title_name = EXCLUDED.job_title_name, manager_yn = EXCLUDED.manager_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_EMPLOYEE (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, manager_employee_no, employment_status, join_date, leave_date, email, mobile_no, use_yn, created_by, updated_by) VALUES ('SAMPLE0001', NULL, 'SAMPLE_DEV', '샘플 결재자', 'SAMPLE_P2', 'SAMPLE_MANAGER', NULL,
     'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAMPLE0002', NULL, 'SAMPLE_DEV', '샘플 요청자', 'SAMPLE_P1', 'SAMPLE_MEMBER', 'SAMPLE0001',
     'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (admin_user_id) DO UPDATE SET organization_code = EXCLUDED.organization_code, employee_name = EXCLUDED.employee_name, position_code = EXCLUDED.position_code, job_title_code = EXCLUDED.job_title_code, manager_employee_no = EXCLUDED.manager_employee_no, employment_status = EXCLUDED.employment_status, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) SELECT v.employee_no, v.organization_code, v.position_code, v.job_title_code, 'PRIMARY', 'Y', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
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
INSERT INTO BZA_ORGANIZATION_RESPONSIBILITY (organization_code, responsibility_type, employee_no, effective_from, effective_to, priority_no, use_yn, created_by, updated_by) SELECT 'SAMPLE_DEV', 'MANAGER', 'SAMPLE0001', CURRENT_TIMESTAMP, NULL, 1, 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM BZA_ORGANIZATION_RESPONSIBILITY
    WHERE organization_code = 'SAMPLE_DEV' AND responsibility_type = 'MANAGER'
      AND employee_no = 'SAMPLE0001' AND use_yn = 'Y' AND effective_to IS NULL
);
INSERT INTO BZA_APPROVAL_POLICY (policy_code, policy_version, policy_name, business_domain, approval_type, effective_from, effective_to, enabled_yn, self_approval_allowed_yn, description, created_by, updated_by) VALUES (
    'SAMPLE_STANDARD_APPROVAL', 1, '샘플 표준 결재', 'SAMPLE', 'STANDARD',
    CURRENT_TIMESTAMP, NULL, 'Y', 'N',
    'Generator/업무관리자 결재 연동을 검증하기 위한 Optional Sample 정책', 'SYSTEM', 'SYSTEM'
) ON CONFLICT (policy_code, policy_version) DO UPDATE SET policy_name = EXCLUDED.policy_name, enabled_yn = EXCLUDED.enabled_yn, self_approval_allowed_yn = EXCLUDED.self_approval_allowed_yn, description = EXCLUDED.description, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO BZA_APPROVAL_POLICY_STEP (policy_code, policy_version, step_no, step_type, target_type, target_code, decision_rule, required_count, required_yn, sort_order, created_by, updated_by) VALUES (
    'SAMPLE_STANDARD_APPROVAL', 1, 1, 'APPROVAL', 'ORG_MANAGER', 'SAMPLE_DEV',
    'ALL', NULL, 'Y', 10, 'SYSTEM', 'SYSTEM'
) ON CONFLICT (policy_code, policy_version, step_no, target_type, target_code) DO UPDATE SET step_type = EXCLUDED.step_type, target_type = EXCLUDED.target_type, target_code = EXCLUDED.target_code, decision_rule = EXCLUDED.decision_rule, required_count = EXCLUDED.required_count, required_yn = EXCLUDED.required_yn, sort_order = EXCLUDED.sort_order, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
