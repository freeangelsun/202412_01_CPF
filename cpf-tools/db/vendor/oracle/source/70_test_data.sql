-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=70_test_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

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
MERGE INTO ADM_DYNAMIC_LOG_LEVEL_RULE tgt
USING (SELECT 'sample-rule-001' AS RULE_ID, NULL AS TRANSACTION_ID, 'OEDUAA0001' AS BUSINESS_TRANSACTION_ID, 'EDU' AS MODULE_ID, 'DEBUG' AS LOG_LEVEL, (SYSTIMESTAMP + INTERVAL '30' MINUTE) AS EXPIRE_AT, 'ADM 화면 smoke 검증용 동적 로그 규칙입니다.' AS REASON, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.RULE_ID=src.RULE_ID)
WHEN MATCHED THEN UPDATE SET tgt.BUSINESS_TRANSACTION_ID=src.BUSINESS_TRANSACTION_ID, tgt.MODULE_ID=src.MODULE_ID, tgt.LOG_LEVEL=src.LOG_LEVEL, tgt.EXPIRE_AT=src.EXPIRE_AT, tgt.REASON=src.REASON, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by) VALUES (src.RULE_ID, src.TRANSACTION_ID, src.BUSINESS_TRANSACTION_ID, src.MODULE_ID, src.LOG_LEVEL, src.EXPIRE_AT, src.REASON, src.USE_YN, src.created_by, src.updated_by);
-- CPF_LOGICAL_DATABASE=mbwDB
MERGE INTO MBW_ADMIN_USER tgt
USING (SELECT 'mbw-admin' AS admin_login_id, '업무 관리자 샘플' AS admin_name, NULL AS password_hash, 'MBW_MANAGER' AS role_code, 'Y' AS use_yn, 'N' AS lock_yn, 0 AS login_fail_count, 'Y' AS password_change_required_yn, NULL AS password_expire_at, NULL AS last_login_at, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.admin_login_id=src.admin_login_id)
WHEN MATCHED THEN UPDATE SET tgt.admin_name=src.admin_name, tgt.role_code=src.role_code, tgt.use_yn=src.use_yn, tgt.lock_yn=src.lock_yn, tgt.login_fail_count=src.login_fail_count, tgt.password_change_required_yn=src.password_change_required_yn, tgt.password_expire_at=src.password_expire_at, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by) VALUES (src.admin_login_id, src.admin_name, src.password_hash, src.role_code, src.use_yn, src.lock_yn, src.login_fail_count, src.password_change_required_yn, src.password_expire_at, src.last_login_at, src.created_by, src.updated_by);
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
MERGE INTO MBW_MENU tgt
USING (SELECT 'DASHBOARD' AS menu_code, '업무 대시보드' AS menu_name, 'MBW' AS module_code, '/backoffice' AS route_path, '/api/v1/backoffice/dashboard' AS api_path, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'USER' AS menu_code, '백오피스 사용자' AS menu_name, 'MBW' AS module_code, '/backoffice#users' AS route_path, '/api/v1/backoffice/admin-users' AS api_path, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'ORGANIZATION' AS menu_code, '조직 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#organizations' AS route_path, '/api/v1/backoffice/organizations' AS api_path, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'EMPLOYEE' AS menu_code, '직원 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#employees' AS route_path, '/api/v1/backoffice/employees' AS api_path, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'ROLE' AS menu_code, '역할 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#roles' AS route_path, '/api/v1/backoffice/roles' AS api_path, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MENU' AS menu_code, '메뉴 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#menus' AS route_path, '/api/v1/backoffice/menus' AS api_path, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'PERMISSION' AS menu_code, '권한 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#permissions' AS route_path, '/api/v1/backoffice/permissions' AS api_path, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'APPROVAL' AS menu_code, '결재 관리' AS menu_name, 'MBW' AS module_code, '/backoffice#approvals' AS route_path, '/api/v1/backoffice/approvals' AS api_path, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'SETTING' AS menu_code, '업무 설정' AS menu_name, 'MBW' AS module_code, '/backoffice#settings' AS route_path, '/api/v1/backoffice/settings' AS api_path, 120 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'DOWNLOAD' AS menu_code, '다운로드 감사' AS menu_name, 'MBW' AS module_code, '/backoffice#downloads' AS route_path, '/api/v1/backoffice/downloads' AS api_path, 130 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'AUDIT' AS menu_code, '업무 감사' AS menu_name, 'MBW' AS module_code, '/backoffice#audits' AS route_path, '/api/v1/backoffice/audits' AS api_path, 140 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'NOTIFICATION' AS menu_code, '업무 알림' AS menu_name, 'MBW' AS module_code, '/backoffice#notifications' AS route_path, '/api/v1/backoffice/notifications' AS api_path, 150 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'ATTACHMENT' AS menu_code, '첨부파일' AS menu_name, 'MBW' AS module_code, '/backoffice#attachments' AS route_path, '/api/v1/backoffice/attachments' AS api_path, 160 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'SAVED_SEARCH' AS menu_code, '저장 검색' AS menu_name, 'MBW' AS module_code, '/backoffice#savedSearches' AS route_path, '/api/v1/backoffice/saved-searches' AS api_path, 170 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_MANAGER' AS role_code, '업무 관리자' AS role_name, 'Y' AS write_allowed_yn, 'ALL' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_USER_ROLE tgt
USING (SELECT admin_user_id AS admin_user_id, 'MBW_MANAGER' AS role_code, SYSTIMESTAMP AS valid_from, NULL AS valid_to, 'Y' AS primary_yn, 'CPF_TEST_SEED' AS grant_reason, 'CPF-TEST-MBW-ROLE-MANAGER-0001' AS operation_id, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_ADMIN_USER
WHERE admin_login_id = 'mbw-admin') src
ON (tgt.operation_id=src.operation_id)
WHEN MATCHED THEN UPDATE SET tgt.valid_to=NULL, tgt.primary_yn='Y', tgt.grant_reason=src.grant_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by) VALUES (src.admin_user_id, src.role_code, src.valid_from, src.valid_to, src.primary_yn, src.grant_reason, src.operation_id, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'USER' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/admin-users/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'USER' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/admin-users' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ORGANIZATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/organizations/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ORGANIZATION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/organizations' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'EMPLOYEE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/employees/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'EMPLOYEE' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/employees' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ROLE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/roles/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ROLE' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/roles' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'MENU' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/menus/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'MENU' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/menus' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'PERMISSION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'PERMISSION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/permissions/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'APPROVAL' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'APPROVAL' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'SETTING' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/settings/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'DOWNLOAD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/downloads/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'AUDIT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/audits/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'NOTIFICATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/notifications/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'NOTIFICATION' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/notifications/**' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/attachments' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/attachments' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'ATTACHMENT' AS menu_code, 'DOWNLOAD' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/attachments/*/download' AS api_pattern, 'ALL' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'SAVED_SEARCH' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/saved-searches/**' AS api_pattern, 'OWN' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_MANAGER' AS role_code, 'SAVED_SEARCH' AS menu_code, 'WRITE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/saved-searches/**' AS api_pattern, 'OWN' AS data_scope, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'DOWNLOAD.MASKING.ENABLED' AS setting_key, 'Y' AS setting_value, '업무 다운로드 마스킹 사용 여부' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ORGANIZATION tgt
USING (SELECT 'HQ' AS organization_code, NULL AS parent_organization_code, '본사' AS organization_name, 'COMPANY' AS organization_type, 10 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.organization_code=src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code=src.parent_organization_code, tgt.organization_name=src.organization_name, tgt.organization_type=src.organization_type, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ORGANIZATION tgt
USING (SELECT 'OPS' AS organization_code, 'HQ' AS parent_organization_code, '업무운영팀' AS organization_name, 'DEPARTMENT' AS organization_type, 20 AS sort_order, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.organization_code=src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code=src.parent_organization_code, tgt.organization_name=src.organization_name, tgt.organization_type=src.organization_type, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_POSITION tgt
USING (SELECT 'P3' AS position_code, '책임' AS position_name, 30 AS rank_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.position_code=src.position_code)
WHEN MATCHED THEN UPDATE SET tgt.position_name=src.position_name, tgt.rank_order=src.rank_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_JOB_TITLE tgt
USING (SELECT 'OPERATOR' AS job_title_code, '업무담당자' AS job_title_name, 'N' AS manager_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.job_title_code=src.job_title_code)
WHEN MATCHED THEN UPDATE SET tgt.job_title_name=src.job_title_name, tgt.manager_yn=src.manager_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_EMPLOYEE tgt
USING (SELECT 'EMP001' AS employee_no, admin_user_id AS admin_user_id, 'OPS' AS organization_code, '업무 담당자' AS employee_name, 'P3' AS position_code, 'OPERATOR' AS job_title_code, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, 'operator@example.com' AS email, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_ADMIN_USER WHERE admin_login_id = 'mbw-admin') src
ON (tgt.admin_user_id=src.admin_user_id)
WHEN MATCHED THEN UPDATE SET tgt.organization_code=src.organization_code, tgt.employee_name=src.employee_name, tgt.position_code=src.position_code, tgt.job_title_code=src.job_title_code, tgt.employment_status=src.employment_status, tgt.email=src.email, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by) VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.employment_status, src.join_date, src.email, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_EMPLOYEE_ASSIGNMENT tgt
USING (SELECT 'EMP001' AS employee_no, 'OPS' AS organization_code, 'P3' AS position_code, 'OPERATOR' AS job_title_code, 'PRIMARY' AS assignment_type, 'Y' AS primary_yn, SYSTIMESTAMP AS effective_from, NULL AS effective_to, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.employee_no=src.employee_no AND tgt.assignment_type=src.assignment_type AND tgt.primary_yn=src.primary_yn)
WHEN MATCHED THEN UPDATE SET tgt.organization_code=src.organization_code, tgt.position_code=src.position_code, tgt.job_title_code=src.job_title_code, tgt.effective_to=NULL, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) VALUES (src.employee_no, src.organization_code, src.position_code, src.job_title_code, src.assignment_type, src.primary_yn, src.effective_from, src.effective_to, src.created_by, src.updated_by);
INSERT INTO MBW_NOTIFICATION (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by)
SELECT 'mbw-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'MBW-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM MBW_NOTIFICATION
     WHERE recipient_login_id = 'mbw-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'MBW-SAMPLE-001'
);
MERGE INTO MBW_SAVED_SEARCH tgt
USING (SELECT 'mbw-admin' AS owner_login_id, 'APPROVAL' AS screen_code, '진행 중 결재' AS search_name, '{"approvalStatus":"IN_REVIEW"}' AS criteria_json, 'N' AS shared_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.owner_login_id=src.owner_login_id AND tgt.screen_code=src.screen_code AND tgt.search_name=src.search_name)
WHEN MATCHED THEN UPDATE SET tgt.criteria_json=src.criteria_json, tgt.shared_yn=src.shared_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by) VALUES (src.owner_login_id, src.screen_code, src.search_name, src.criteria_json, src.shared_yn, src.use_yn, src.created_by, src.updated_by);
