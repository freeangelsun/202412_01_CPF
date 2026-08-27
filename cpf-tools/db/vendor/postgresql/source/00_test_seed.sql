-- CPF generated lifecycle bundle; vendor=postgresql
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 70_test_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=70_test_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_LOGICAL_DATABASE=referenceFixture
INSERT INTO REF_CMN_SAMPLE_ITEM (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (101, 'CMN-TEST-101', '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'header single query', 'CPF-TEST-101', 101, 0, 'CMN_TEST', 'CMN_TEST'),
    (102, 'CMN-TEST-102', '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'transaction log list', 'CPF-TEST-102', 102, 0, 'CMN_TEST', 'CMN_TEST'),
    (103, 'CMN-TEST-103', 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'offset page', 'CPF-TEST-103', 103, 0, 'CMN_TEST', 'CMN_TEST'),
    (104, 'CMN-TEST-104', 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'keyset cursor', 'CPF-TEST-104', 104, 0, 'CMN_TEST', 'CMN_TEST'),
    (105, 'CMN-TEST-105', '검색 조건 정규화', 'QUERY', 'INACTIVE', 'search validation', 'CPF-TEST-105', 105, 0, 'CMN_TEST', 'CMN_TEST'),
    (106, 'CMN-TEST-106', '정렬 allowlist', 'QUERY', 'ACTIVE', 'stable sort allowlist', 'CPF-TEST-106', 106, 0, 'CMN_TEST', 'CMN_TEST'),
    (107, 'CMN-TEST-107', '낙관적 잠금 충돌', 'LOCK', 'ACTIVE', 'optimistic lock version', 'CPF-TEST-107', 107, 0, 'CMN_TEST', 'CMN_TEST'),
    (108, 'CMN-TEST-108', 'Transaction rollback', 'TRANSACTION', 'ACTIVE', 'transaction rollback', 'CPF-TEST-108', 108, 0, 'CMN_TEST', 'CMN_TEST')
ON CONFLICT (sample_item_id) DO UPDATE SET sample_key=EXCLUDED.sample_key, item_name=EXCLUDED.item_name, category_code=EXCLUDED.category_code, status_code=EXCLUDED.status_code, searchable_text=EXCLUDED.searchable_text, owner_reference=EXCLUDED.owner_reference, sort_order=EXCLUDED.sort_order, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);
DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 90001 AND 90008;
DELETE FROM REF_SAMPLE_ITEM WHERE sample_item_id BETWEEN 91000 AND 91999;
INSERT INTO REF_SAMPLE_ITEM (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, deleted_yn, created_by, created_at, updated_by, updated_at)
VALUES (90001, 'REF-MAPPER-90001', '단건 조회 샘플', 'SINGLE', 'ACTIVE', 'single', 'REF-90001', 90001, 0, 'N', 'MAPPER_TEST', '2026-06-01 09:00:00.000', 'MAPPER_TEST', '2026-06-01 09:00:00.000'),
    (90002, 'REF-MAPPER-90002', '목록 조회 샘플', 'LIST', 'ACTIVE', 'list', 'REF-90002', 90002, 0, 'N', 'MAPPER_TEST', '2026-06-02 09:00:00.000', 'MAPPER_TEST', '2026-06-02 09:00:00.000'),
    (90003, 'REF-MAPPER-90003', '검색 조회 샘플', 'SEARCH', 'ACTIVE', 'search', 'REF-90003', 90003, 0, 'N', 'MAPPER_TEST', '2026-06-03 09:00:00.000', 'MAPPER_TEST', '2026-06-03 09:00:00.000'),
    (90004, 'REF-MAPPER-90004', '정렬 조회 샘플', 'SORT', 'ACTIVE', 'sort', 'REF-90004', 90004, 0, 'N', 'MAPPER_TEST', '2026-06-04 09:00:00.000', 'MAPPER_TEST', '2026-06-04 09:00:00.000'),
    (90005, 'REF-MAPPER-90005', '페이지 조회 샘플', 'PAGE', 'ACTIVE', 'page', 'REF-90005', 90005, 0, 'N', 'MAPPER_TEST', '2026-06-05 09:00:00.000', 'MAPPER_TEST', '2026-06-05 09:00:00.000'),
    (90006, 'REF-MAPPER-90006', '비활성 조회 샘플', 'LIST', 'INACTIVE', 'inactive', 'REF-90006', 90006, 0, 'N', 'MAPPER_TEST', '2026-06-06 09:00:00.000', 'MAPPER_TEST', '2026-06-06 09:00:00.000'),
    (90007, 'REF-MAPPER-90007', 'Validation 조회 샘플', 'VALIDATION', 'INACTIVE', 'validation', 'REF-90007', 90007, 0, 'N', 'MAPPER_TEST', '2026-06-07 09:00:00.000', 'MAPPER_TEST', '2026-06-07 09:00:00.000'),
    (90008, 'REF-MAPPER-90008', 'Keyset 조회 샘플', 'KEYSET', 'ACTIVE', 'keyset', 'REF-90008', 90008, 0, 'N', 'MAPPER_TEST', '2026-06-08 09:00:00.000', 'MAPPER_TEST', '2026-06-08 09:00:00.000')
ON CONFLICT (sample_item_id) DO UPDATE SET sample_key=EXCLUDED.sample_key, item_name=EXCLUDED.item_name, category_code=EXCLUDED.category_code, status_code=EXCLUDED.status_code, searchable_text=EXCLUDED.searchable_text, owner_reference=EXCLUDED.owner_reference, sort_order=EXCLUDED.sort_order, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;
DELETE FROM REF_CENTER_CUT_SAMPLE_RESULT WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';
INSERT INTO REF_CENTER_CUT_SAMPLE_TARGET (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by)
VALUES ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (target_id) DO UPDATE SET target_payload=EXCLUDED.target_payload, status_code=EXCLUDED.status_code, retry_count=EXCLUDED.retry_count, transaction_id=EXCLUDED.transaction_id, parent_segment_id=EXCLUDED.parent_segment_id, transaction_segment_id=EXCLUDED.transaction_segment_id, started_at=EXCLUDED.started_at, completed_at=EXCLUDED.completed_at, last_error_message=EXCLUDED.last_error_message, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_SEED_INLINE_VARIABLE sample_transaction_id
-- CPF_SEED_INLINE_VARIABLE sample_start_time
-- CPF_SEED_INLINE_VARIABLE sample_end_time
INSERT INTO CPF_TRANSACTION_LOG (LOG_DATE, TRANSACTION_ID, TRACE_ID, SPAN_ID, SEQUENCE_NO, MODULE_ID, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE, API_VERSION, CLIENT_ID, CLIENT_VERSION, CALLER_CHANNEL, TARGET_CHANNEL, TARGET_OPERATION_ID, CALLER_INSTANCE_ID, CORRELATION_ID, IDEMPOTENCY_KEY, LOCALE, TIMEZONE, REQUEST_TYPE, ORIGINAL_CHANNEL, CURRENT_CHANNEL, MEMBER_NO, CUSTOMER_NO, SCREEN_ID, DEVICE_ID, WAS_ID, INSTANCE_ID, HOST_NAME, HOST_IP, PROCESS_ID, THREAD_NAME, HTTP_METHOD, URI, CONTROLLER, EXECUTION_PACKAGE, EXECUTION_CLASS, EXECUTION_METHOD, EXECUTION_SIGNATURE, PARAMETERS, REQUEST_BODY, RESPONSE, HTTP_STATUS, RESPONSE_CODE, EXEC_USER, CLIENT_IP, USER_AGENT, START_TIME, END_TIME, DURATION_MS, created_by, updated_by)
SELECT
    DATE(('2026-06-15 12:00:00.000')),
    ('20260615120000000MBRlocal010000001'),
    'trace-sample-001',
    'span-sample-001',
    1,
    'EDU',
    'OEDUAA0001',
    'EDU 표준 거래 샘플',
    'SUCCESS',
    'v1',
    'cpf-edu-web',
    '1.0.0',
    'EDU',
    'EDU',
    'educationCrudList',
    'local-dev',
    'corr-sample-001',
    'idem-sample-001',
    'ko-KR',
    'Asia/Seoul',
    'NORMAL',
    'EDU',
    'EDU',
    'M000000001',
    'C000000001',
    'EDU_SAMPLE_LIST',
    'LOCAL_BROWSER',
    'local01',
    'local-dev:sql-seed',
    'local-dev',
    '127.0.0.1',
    'sql-seed',
    'sql-smoke',
    'GET',
    '/edu/api/education/crud',
    'com.cpf.education.web.crud.controller.EducationCrudEducationController',
    'com.cpf.education.web.crud.controller',
    'EducationCrudEducationController',
    'list',
    'EducationCrudEducationController.list()',
    '{}',
    '{"sampleKey":"REF-SAMPLE-001","secret":"masked"}',
    '{"code":"SCPF000000","message":"정상 처리되었습니다."}',
    200,
    'SCPF000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    ('2026-06-15 12:00:00.000'),
    ('2026-06-15 12:00:00.012'),
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
);
-- CPF_SEED_INLINE_VARIABLE sample_log_idx
INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'headers', '{"X-Current-Channel":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'headers'
  );
INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'fixedTelegram', 'S000000001샘플1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'fixedTelegram'
  );
INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'memo'
  );
INSERT INTO ADM_DYNAMIC_LOG_LEVEL_RULE (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by)
VALUES (
    'sample-rule-001',
    NULL,
    'OEDUAA0001',
    'EDU',
    'DEBUG',
    (CURRENT_TIMESTAMP + INTERVAL '30 minute'),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (RULE_ID) DO UPDATE SET BUSINESS_TRANSACTION_ID=EXCLUDED.BUSINESS_TRANSACTION_ID, MODULE_ID=EXCLUDED.MODULE_ID, LOG_LEVEL=EXCLUDED.LOG_LEVEL, EXPIRE_AT=EXCLUDED.EXPIRE_AT, REASON=EXCLUDED.REASON, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
-- CPF_LOGICAL_DATABASE=mbwDB
INSERT INTO MBW_ADMIN_USER (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by)
VALUES (
    'mbw-admin', '업무 관리자 샘플', NULL, 'MBW_MANAGER', 'Y', 'N',
    0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (admin_login_id) DO UPDATE SET admin_name=EXCLUDED.admin_name, role_code=EXCLUDED.role_code, use_yn=EXCLUDED.use_yn, lock_yn=EXCLUDED.lock_yn, login_fail_count=EXCLUDED.login_fail_count, password_change_required_yn=EXCLUDED.password_change_required_yn, password_expire_at=EXCLUDED.password_expire_at, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_LOGIN_HISTORY (admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent, transaction_id, system_code, application_name, instance_id, created_by, updated_by)
SELECT admin_user_id, 'MBW', 'mbw-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000MBWmbwAP010000001', 'MBW', 'mbwAP01', 'MBW-SEED-01', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM MBW_LOGIN_HISTORY
      WHERE admin_login_id = 'mbw-admin'
        AND transaction_id = '20260715120000000MBWmbwAP010000001'
  );
INSERT INTO MBW_MENU (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by)
VALUES ('DASHBOARD', '업무 대시보드', 'MBW', '/backoffice', '/api/v1/backoffice/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('USER', '백오피스 사용자', 'MBW', '/backoffice#users', '/api/v1/backoffice/admin-users', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ORGANIZATION', '조직 관리', 'MBW', '/backoffice#organizations', '/api/v1/backoffice/organizations', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EMPLOYEE', '직원 관리', 'MBW', '/backoffice#employees', '/api/v1/backoffice/employees', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ROLE', '역할 관리', 'MBW', '/backoffice#roles', '/api/v1/backoffice/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MENU', '메뉴 관리', 'MBW', '/backoffice#menus', '/api/v1/backoffice/menus', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', '권한 관리', 'MBW', '/backoffice#permissions', '/api/v1/backoffice/permissions', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', '결재 관리', 'MBW', '/backoffice#approvals', '/api/v1/backoffice/approvals', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SETTING', '업무 설정', 'MBW', '/backoffice#settings', '/api/v1/backoffice/settings', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', '다운로드 감사', 'MBW', '/backoffice#downloads', '/api/v1/backoffice/downloads', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT', '업무 감사', 'MBW', '/backoffice#audits', '/api/v1/backoffice/audits', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', '업무 알림', 'MBW', '/backoffice#notifications', '/api/v1/backoffice/notifications', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ATTACHMENT', '첨부파일', 'MBW', '/backoffice#attachments', '/api/v1/backoffice/attachments', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAVED_SEARCH', '저장 검색', 'MBW', '/backoffice#savedSearches', '/api/v1/backoffice/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (menu_code) DO UPDATE SET menu_name=EXCLUDED.menu_name, api_path=EXCLUDED.api_path, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (
    'MBW_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (role_code) DO UPDATE SET role_name=EXCLUDED.role_name, write_allowed_yn=EXCLUDED.write_allowed_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_USER_ROLE (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by)
SELECT admin_user_id, 'MBW_MANAGER', CURRENT_TIMESTAMP, NULL, 'Y',
       'CPF_TEST_SEED', 'CPF-TEST-MBW-ROLE-MANAGER-0001', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin'
ON CONFLICT (operation_id) DO UPDATE SET valid_to=NULL, primary_yn='Y', grant_reason=EXCLUDED.grant_reason, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_PERMISSION (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by)
VALUES ('MBW_MANAGER', 'DASHBOARD', 'READ', 'API', 'GET', '/api/v1/backoffice/dashboard', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'USER', 'READ', 'API', 'GET', '/api/v1/backoffice/admin-users/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'USER', 'WRITE', 'API', 'POST', '/api/v1/backoffice/admin-users', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ORGANIZATION', 'READ', 'API', 'GET', '/api/v1/backoffice/organizations/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ORGANIZATION', 'WRITE', 'API', 'POST', '/api/v1/backoffice/organizations', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'EMPLOYEE', 'READ', 'API', 'GET', '/api/v1/backoffice/employees/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'EMPLOYEE', 'WRITE', 'API', 'POST', '/api/v1/backoffice/employees', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ROLE', 'READ', 'API', 'GET', '/api/v1/backoffice/roles/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ROLE', 'WRITE', 'API', 'POST', '/api/v1/backoffice/roles', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'MENU', 'READ', 'API', 'GET', '/api/v1/backoffice/menus/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'MENU', 'WRITE', 'API', 'POST', '/api/v1/backoffice/menus', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'PERMISSION', 'READ', 'API', 'GET', '/api/v1/backoffice/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'PERMISSION', 'WRITE', 'API', 'POST', '/api/v1/backoffice/permissions/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'APPROVAL', 'READ', 'API', 'GET', '/api/v1/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'APPROVAL', 'WRITE', 'API', 'POST', '/api/v1/backoffice/approvals/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'SETTING', 'READ', 'API', 'GET', '/api/v1/backoffice/settings/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'DOWNLOAD', 'READ', 'API', 'GET', '/api/v1/backoffice/downloads/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'AUDIT', 'READ', 'API', 'GET', '/api/v1/backoffice/audits/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'NOTIFICATION', 'READ', 'API', 'GET', '/api/v1/backoffice/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'NOTIFICATION', 'WRITE', 'API', 'POST', '/api/v1/backoffice/notifications/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ATTACHMENT', 'READ', 'API', 'GET', '/api/v1/backoffice/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ATTACHMENT', 'WRITE', 'API', 'POST', '/api/v1/backoffice/attachments', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'ATTACHMENT', 'DOWNLOAD', 'API', 'GET', '/api/v1/backoffice/attachments/*/download', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'SAVED_SEARCH', 'READ', 'API', 'GET', '/api/v1/backoffice/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBW_MANAGER', 'SAVED_SEARCH', 'WRITE', 'API', 'POST', '/api/v1/backoffice/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (role_code, menu_code, button_code, permission_type, environment_code) DO UPDATE SET allow_yn=EXCLUDED.allow_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (
    'DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (setting_key) DO UPDATE SET setting_value=EXCLUDED.setting_value, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_ORGANIZATION (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES ('HQ', NULL, '본사', 'COMPANY', 10, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (organization_code) DO UPDATE SET parent_organization_code=EXCLUDED.parent_organization_code, organization_name=EXCLUDED.organization_name, organization_type=EXCLUDED.organization_type, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_POSITION (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES ('P3', '책임', 30, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (position_code) DO UPDATE SET position_name=EXCLUDED.position_name, rank_order=EXCLUDED.rank_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_JOB_TITLE (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES ('OPERATOR', '업무담당자', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (job_title_code) DO UPDATE SET job_title_name=EXCLUDED.job_title_name, manager_yn=EXCLUDED.manager_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_EMPLOYEE (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by)
SELECT 'EMP001', admin_user_id, 'OPS', '업무 담당자', 'P3', 'OPERATOR', 'ACTIVE', CURRENT_DATE,
       'operator@example.com', 'Y', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER WHERE admin_login_id = 'mbw-admin'
ON CONFLICT (admin_user_id) DO UPDATE SET organization_code=EXCLUDED.organization_code, employee_name=EXCLUDED.employee_name, position_code=EXCLUDED.position_code, job_title_code=EXCLUDED.job_title_code, employment_status=EXCLUDED.employment_status, email=EXCLUDED.email, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
VALUES (
    'EMP001', 'OPS', 'P3', 'OPERATOR', 'PRIMARY', 'Y', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (employee_no, assignment_type, primary_yn) DO UPDATE SET organization_code=EXCLUDED.organization_code, position_code=EXCLUDED.position_code, job_title_code=EXCLUDED.job_title_code, effective_to=NULL, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_NOTIFICATION (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by)
SELECT 'mbw-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'MBW-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM MBW_NOTIFICATION
     WHERE recipient_login_id = 'mbw-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'MBW-SAMPLE-001'
);
INSERT INTO MBW_SAVED_SEARCH (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by)
VALUES (
    'mbw-admin', 'APPROVAL', '진행 중 결재', '{"approvalStatus":"IN_REVIEW"}',
    'N', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (owner_login_id, screen_code, search_name) DO UPDATE SET criteria_json=EXCLUDED.criteria_json, shared_yn=EXCLUDED.shared_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;
-- ===== END 70_test_data.sql =====
