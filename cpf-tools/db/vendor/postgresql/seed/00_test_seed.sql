-- CPF generated lifecycle bundle; vendor=postgresql
-- Source plan: cpf-tools/config/database-source-plan.json

-- ===== BEGIN 70_test_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=70_test_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cmnDB
INSERT INTO cmn_sample_item (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (101, 'CMN-TEST-101', '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'header single query', 'CPF-TEST-101', 101, 0, 'CMN_TEST', 'CMN_TEST'),
    (102, 'CMN-TEST-102', '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'transaction log list', 'CPF-TEST-102', 102, 0, 'CMN_TEST', 'CMN_TEST'),
    (103, 'CMN-TEST-103', 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'offset page', 'CPF-TEST-103', 103, 0, 'CMN_TEST', 'CMN_TEST'),
    (104, 'CMN-TEST-104', 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'keyset cursor', 'CPF-TEST-104', 104, 0, 'CMN_TEST', 'CMN_TEST'),
    (105, 'CMN-TEST-105', '검색 조건 정규화', 'QUERY', 'INACTIVE', 'search validation', 'CPF-TEST-105', 105, 0, 'CMN_TEST', 'CMN_TEST'),
    (106, 'CMN-TEST-106', '정렬 allowlist', 'QUERY', 'ACTIVE', 'stable sort allowlist', 'CPF-TEST-106', 106, 0, 'CMN_TEST', 'CMN_TEST'),
    (107, 'CMN-TEST-107', '낙관적 잠금 충돌', 'LOCK', 'ACTIVE', 'optimistic lock version', 'CPF-TEST-107', 107, 0, 'CMN_TEST', 'CMN_TEST'),
    (108, 'CMN-TEST-108', 'Transaction rollback', 'TRANSACTION', 'ACTIVE', 'transaction rollback', 'CPF-TEST-108', 108, 0, 'CMN_TEST', 'CMN_TEST') ON CONFLICT (sample_item_id) DO UPDATE SET sample_key = EXCLUDED.sample_key, item_name = EXCLUDED.item_name, category_code = EXCLUDED.category_code, status_code = EXCLUDED.status_code, searchable_text = EXCLUDED.searchable_text, owner_reference = EXCLUDED.owner_reference, sort_order = EXCLUDED.sort_order, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;

-- CPF_LOGICAL_DATABASE=refDB
DELETE FROM ref_center_cut_sample_result WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';
INSERT INTO ref_center_cut_sample_target (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by) VALUES ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (target_id) DO UPDATE SET target_payload = EXCLUDED.target_payload, status_code = EXCLUDED.status_code, retry_count = EXCLUDED.retry_count, transaction_id = EXCLUDED.transaction_id, parent_segment_id = EXCLUDED.parent_segment_id, transaction_segment_id = EXCLUDED.transaction_segment_id, started_at = EXCLUDED.started_at, completed_at = EXCLUDED.completed_at, last_error_message = EXCLUDED.last_error_message, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;

-- CPF_LOGICAL_DATABASE=cpfDB
\\set sample_transaction_id '20260615120000000MBRlocal010000001'
\\set sample_start_time '2026-06-15 12:00:00.000'
\\set sample_end_time '2026-06-15 12:00:00.012'
INSERT INTO cpf_transaction_log (LOG_DATE, TRANSACTION_ID, TRACE_ID, SPAN_ID, SEQUENCE_NO, MODULE_ID, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE, API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CALLER_INSTANCE_ID, CORRELATION_ID, IDEMPOTENCY_KEY, LOCALE, TIMEZONE, REQUEST_TYPE, ORIGINAL_CHANNEL_CODE, CHANNEL_CODE, MEMBER_NO, CUSTOMER_NO, SCREEN_ID, DEVICE_ID, WAS_ID, SERVER_INSTANCE_ID, HOST_NAME, PROCESS_ID, THREAD_NAME, HTTP_METHOD, URI, CONTROLLER, EXECUTION_PACKAGE, EXECUTION_CLASS, EXECUTION_METHOD, EXECUTION_SIGNATURE, PARAMETERS, REQUEST_BODY, RESPONSE, HTTP_STATUS, RESPONSE_CODE, EXEC_USER, CLIENT_IP, USER_AGENT, START_TIME, END_TIME, DURATION_MS, created_by, updated_by) SELECT
    DATE(:sample_start_time),
    :sample_transaction_id,
    'trace-sample-001',
    'span-sample-001',
    1,
    'REF',
    'OREFAA0001',
    'REF 표준 거래 샘플',
    'SUCCESS',
    'v1',
    'cpf-edu-web',
    '1.0.0',
    'ref-education',
    'local-dev',
    'corr-sample-001',
    'idem-sample-001',
    'ko-KR',
    'Asia/Seoul',
    'NORMAL',
    'WEB',
    'WEB',
    'M000000001',
    'C000000001',
    'REF_SAMPLE_LIST',
    'LOCAL_BROWSER',
    'local01',
    'local-dev:sql-seed',
    'local-dev',
    'sql-seed',
    'sql-smoke',
    'GET',
    '/ref/api/education/crud',
    'com.cpf.reference.crud.controller.ReferenceCrudEducationController',
    'com.cpf.reference.crud.controller',
    'ReferenceCrudEducationController',
    'list',
    'ReferenceCrudEducationController.list()',
    '{}',
    '{"sampleKey":"REF-SAMPLE-001","secret":"masked"}',
    '{"code":"SCPF000000","message":"정상 처리되었습니다."}',
    200,
    'SCPF000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    :sample_start_time,
    :sample_end_time,
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = :sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'OREFAA0001'
);
SELECT (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = :sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'OREFAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
) AS sample_log_idx \\gset
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT :sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE :sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = :sample_log_idx
        AND DETAIL_KEY = 'headers'
  );
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT :sample_log_idx, 'fixedTelegram', 'S000000001샘플1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE :sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = :sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT :sample_log_idx, 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE :sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = :sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

-- CPF_LOGICAL_DATABASE=admDB
INSERT INTO adm_dynamic_log_level_rule (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by) VALUES (
    'sample-rule-001',
    NULL,
    'OREFAA0001',
    'REF',
    'DEBUG',
    (CURRENT_TIMESTAMP + INTERVAL '30 minute'),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (RULE_ID) DO UPDATE SET BUSINESS_TRANSACTION_ID = EXCLUDED.BUSINESS_TRANSACTION_ID, MODULE_ID = EXCLUDED.MODULE_ID, LOG_LEVEL = EXCLUDED.LOG_LEVEL, EXPIRE_AT = EXCLUDED.EXPIRE_AT, REASON = EXCLUDED.REASON, USE_YN = EXCLUDED.USE_YN, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;

-- CPF_LOGICAL_DATABASE=bzaDB
INSERT INTO bza_admin_user (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by) VALUES (
    'bza-admin', '업무 관리자 샘플', NULL, 'BZA_MANAGER', 'Y', 'N',
    0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM'
) ON CONFLICT (admin_login_id) DO UPDATE SET admin_name = EXCLUDED.admin_name, role_code = EXCLUDED.role_code, use_yn = EXCLUDED.use_yn, lock_yn = EXCLUDED.lock_yn, login_fail_count = EXCLUDED.login_fail_count, password_change_required_yn = EXCLUDED.password_change_required_yn, password_expire_at = EXCLUDED.password_expire_at, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_login_history (admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent, transaction_id, module_id, was_id, server_instance_id, created_by, updated_by) SELECT admin_user_id, 'BZA', 'bza-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000BZAbzaAP010000001', 'BZA', 'bzaAP01', 'local-bza:seed', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user
WHERE admin_login_id = 'bza-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM bza_login_history
      WHERE admin_login_id = 'bza-admin'
        AND transaction_id = '20260715120000000BZAbzaAP010000001'
  );
INSERT INTO bza_menu (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES ('DASHBOARD', '업무 대시보드', 'BZA', '/bza', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('USER', '백오피스 사용자', 'BZA', '/bza#users', '/api/bza/admin-users', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ORGANIZATION', '조직 관리', 'BZA', '/bza#organizations', '/api/bza/backoffice/organizations', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EMPLOYEE', '직원 관리', 'BZA', '/bza#employees', '/api/bza/backoffice/employees', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ROLE', '역할 관리', 'BZA', '/bza#roles', '/api/bza/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MENU', '메뉴 관리', 'BZA', '/bza#menus', '/api/bza/menus', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', '권한 관리', 'BZA', '/bza#permissions', '/api/bza/permissions', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', '결재 관리', 'BZA', '/bza#approvals', '/api/bza/backoffice/approvals', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SETTING', '업무 설정', 'BZA', '/bza#settings', '/api/bza/settings', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', '다운로드 감사', 'BZA', '/bza#downloads', '/api/bza/downloads', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT', '업무 감사', 'BZA', '/bza#audits', '/api/bza/backoffice/audits', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', '업무 알림', 'BZA', '/bza#notifications', '/api/bza/notifications', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ATTACHMENT', '첨부파일', 'BZA', '/bza#attachments', '/api/bza/attachments', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAVED_SEARCH', '저장 검색', 'BZA', '/bza#savedSearches', '/api/bza/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (menu_code) DO UPDATE SET menu_name = EXCLUDED.menu_name, api_path = EXCLUDED.api_path, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_role (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (
    'BZA_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'
) ON CONFLICT (role_code) DO UPDATE SET role_name = EXCLUDED.role_name, write_allowed_yn = EXCLUDED.write_allowed_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_user_role (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by) SELECT admin_user_id, 'BZA_MANAGER', CURRENT_TIMESTAMP, NULL, 'Y',
       'CPF_TEST_SEED', 'CPF-TEST-BZA-ROLE-MANAGER-0001', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user
WHERE admin_login_id = 'bza-admin' ON CONFLICT (operation_id) DO UPDATE SET valid_to = NULL, primary_yn = 'Y', grant_reason = EXCLUDED.grant_reason, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_permission (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES ('BZA_MANAGER', 'DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'USER', 'READ', 'API', 'GET', '/api/bza/admin-users/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'USER', 'WRITE', 'API', 'POST', '/api/bza/admin-users', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/backoffice/organizations/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ORGANIZATION', 'WRITE', 'API', 'POST', '/api/bza/backoffice/organizations', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/backoffice/employees/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'EMPLOYEE', 'WRITE', 'API', 'POST', '/api/bza/backoffice/employees', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ROLE', 'READ', 'API', 'GET', '/api/bza/roles/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ROLE', 'WRITE', 'API', 'POST', '/api/bza/roles', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'MENU', 'READ', 'API', 'GET', '/api/bza/menus/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'MENU', 'WRITE', 'API', 'POST', '/api/bza/menus', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'PERMISSION', 'READ', 'API', 'GET', '/api/bza/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'PERMISSION', 'WRITE', 'API', 'POST', '/api/bza/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'APPROVAL', 'READ', 'API', 'GET', '/api/bza/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'APPROVAL', 'WRITE', 'API', 'POST', '/api/bza/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SETTING', 'READ', 'API', 'GET', '/api/bza/settings/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'DOWNLOAD', 'READ', 'API', 'GET', '/api/bza/downloads/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'AUDIT', 'READ', 'API', 'GET', '/api/bza/backoffice/audits/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'NOTIFICATION', 'READ', 'API', 'GET', '/api/bza/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'NOTIFICATION', 'WRITE', 'API', 'POST', '/api/bza/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'READ', 'API', 'GET', '/api/bza/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'WRITE', 'API', 'POST', '/api/bza/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ATTACHMENT', 'DOWNLOAD', 'API', 'GET', '/api/bza/attachments/*/download', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SAVED_SEARCH', 'READ', 'API', 'GET', '/api/bza/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'SAVED_SEARCH', 'WRITE', 'API', 'POST', '/api/bza/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET allow_yn = EXCLUDED.allow_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_project_setting (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (
    'DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
) ON CONFLICT (setting_key) DO UPDATE SET setting_value = EXCLUDED.setting_value, description = EXCLUDED.description, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_organization (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES ('HQ', NULL, '본사', 'COMPANY', 10, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (organization_code) DO UPDATE SET parent_organization_code = EXCLUDED.parent_organization_code, organization_name = EXCLUDED.organization_name, organization_type = EXCLUDED.organization_type, sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_position (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES ('P3', '책임', 30, 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (position_code) DO UPDATE SET position_name = EXCLUDED.position_name, rank_order = EXCLUDED.rank_order, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_job_title (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES ('OPERATOR', '업무담당자', 'N', 'Y', 'SYSTEM', 'SYSTEM') ON CONFLICT (job_title_code) DO UPDATE SET job_title_name = EXCLUDED.job_title_name, manager_yn = EXCLUDED.manager_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_employee (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by) SELECT 'EMP001', admin_user_id, 'OPS', '업무 담당자', 'P3', 'OPERATOR', 'ACTIVE', CURRENT_DATE,
       'operator:example.com', 'Y', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user WHERE admin_login_id = 'bza-admin' ON CONFLICT (admin_user_id) DO UPDATE SET admin_user_id = EXCLUDED.admin_user_id, organization_code = EXCLUDED.organization_code, employee_name = EXCLUDED.employee_name, position_code = EXCLUDED.position_code, job_title_code = EXCLUDED.job_title_code, employment_status = EXCLUDED.employment_status, email = EXCLUDED.email, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_employee_assignment (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) VALUES (
    'EMP001', 'OPS', 'P3', 'OPERATOR', 'PRIMARY', 'Y', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
) ON CONFLICT (employee_no, assignment_type, primary_yn) DO UPDATE SET organization_code = EXCLUDED.organization_code, position_code = EXCLUDED.position_code, job_title_code = EXCLUDED.job_title_code, primary_yn = EXCLUDED.primary_yn, effective_to = NULL, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;
INSERT INTO bza_notification (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by) SELECT 'bza-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'BZA-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM bza_notification
     WHERE recipient_login_id = 'bza-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'BZA-SAMPLE-001'
);
INSERT INTO bza_saved_search (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by) VALUES (
    'bza-admin', 'APPROVAL', '진행 중 결재', '{"approvalStatus":"IN_REVIEW"}',
    'N', 'Y', 'SYSTEM', 'SYSTEM'
) ON CONFLICT (owner_login_id, screen_code, search_name) DO UPDATE SET criteria_json = EXCLUDED.criteria_json, shared_yn = EXCLUDED.shared_yn, use_yn = EXCLUDED.use_yn, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP;

-- ===== END 70_test_data.sql =====
