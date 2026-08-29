-- CPF generated lifecycle bundle; vendor=mariadb
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 70_test_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=mariadb; source=70_test_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
USE cpfDB;
SET @sample_transaction_id = '20260615120000000MBRlocal010000001';
SET @sample_start_time = '2026-06-15 12:00:00.000';
SET @sample_end_time = '2026-06-15 12:00:00.012';
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
SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
);
INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
), 'headers', '{"X-Current-Channel":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
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
    LIMIT 1
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
    LIMIT 1
), 'fixedTelegram', 'S000000001샘플1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
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
    LIMIT 1
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
    LIMIT 1
), 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    LIMIT 1
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
    LIMIT 1
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
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE BUSINESS_TRANSACTION_ID=VALUES(BUSINESS_TRANSACTION_ID), MODULE_ID=VALUES(MODULE_ID), LOG_LEVEL=VALUES(LOG_LEVEL), EXPIRE_AT=VALUES(EXPIRE_AT), REASON=VALUES(REASON), USE_YN=VALUES(USE_YN), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- CPF_LOGICAL_DATABASE=mbwDB
USE mbwDB;
INSERT INTO MBW_ADMIN_USER (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by)
VALUES (
    'mbw-admin', '업무 관리자 샘플', NULL, 'MBW_MANAGER', 'Y', 'N',
    0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE admin_name=VALUES(admin_name), role_code=VALUES(role_code), use_yn=VALUES(use_yn), lock_yn=VALUES(lock_yn), login_fail_count=VALUES(login_fail_count), password_change_required_yn=VALUES(password_change_required_yn), password_expire_at=VALUES(password_expire_at), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
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
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), api_path=VALUES(api_path), sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_ROLE (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (
    'MBW_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), write_allowed_yn=VALUES(write_allowed_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_USER_ROLE (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by)
SELECT admin_user_id, 'MBW_MANAGER', CURRENT_TIMESTAMP(3), NULL, 'Y',
       'CPF_TEST_SEED', 'CPF-TEST-MBW-ROLE-MANAGER-0001', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin'
ON DUPLICATE KEY UPDATE valid_to=NULL, primary_yn='Y', grant_reason=VALUES(grant_reason), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
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
ON DUPLICATE KEY UPDATE allow_yn=VALUES(allow_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_PROJECT_SETTING (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (
    'DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), description=VALUES(description), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_ORGANIZATION (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by)
VALUES ('HQ', NULL, '본사', 'COMPANY', 10, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, CURRENT_TIMESTAMP(3), NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE parent_organization_code=VALUES(parent_organization_code), organization_name=VALUES(organization_name), organization_type=VALUES(organization_type), sort_order=VALUES(sort_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_POSITION (position_code, position_name, rank_order, use_yn, created_by, updated_by)
VALUES ('P3', '책임', 30, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE position_name=VALUES(position_name), rank_order=VALUES(rank_order), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_JOB_TITLE (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by)
VALUES ('OPERATOR', '업무담당자', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE job_title_name=VALUES(job_title_name), manager_yn=VALUES(manager_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
INSERT INTO MBW_EMPLOYEE (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by)
SELECT 'EMP001', admin_user_id, 'OPS', '업무 담당자', 'P3', 'OPERATOR', 'ACTIVE', CURRENT_DATE,
       'operator@example.com', 'Y', 'SYSTEM', 'SYSTEM'
FROM MBW_ADMIN_USER WHERE admin_login_id = 'mbw-admin'
ON DUPLICATE KEY UPDATE organization_code=VALUES(organization_code), employee_name=VALUES(employee_name), position_code=VALUES(position_code), job_title_code=VALUES(job_title_code), employment_status=VALUES(employment_status), email=VALUES(email), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
INSERT INTO MBW_EMPLOYEE_ASSIGNMENT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
VALUES (
    'EMP001', 'OPS', 'P3', 'OPERATOR', 'PRIMARY', 'Y', CURRENT_TIMESTAMP(3), NULL, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE organization_code=VALUES(organization_code), position_code=VALUES(position_code), job_title_code=VALUES(job_title_code), effective_to=NULL, updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP(3);
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
ON DUPLICATE KEY UPDATE criteria_json=VALUES(criteria_json), shared_yn=VALUES(shared_yn), use_yn=VALUES(use_yn), updated_by=VALUES(updated_by), updated_at=CURRENT_TIMESTAMP;
-- ===== END 70_test_data.sql =====
