-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=70_test_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cmnDB
-- CPF_USE_LOGICAL_DATABASE=cmnDB
MERGE INTO cmn_sample_item tgt
USING (VALUES
  (101, 'CMN-TEST-101', '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'header single query', 'MBR-TEST-101', 101, 0, 'CMN_TEST', 'CMN_TEST'),
  (102, 'CMN-TEST-102', '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'transaction log list', 'MBR-TEST-102', 102, 0, 'CMN_TEST', 'CMN_TEST'),
  (103, 'CMN-TEST-103', 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'offset page', 'MBR-TEST-103', 103, 0, 'CMN_TEST', 'CMN_TEST'),
  (104, 'CMN-TEST-104', 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'keyset cursor', 'MBR-TEST-104', 104, 0, 'CMN_TEST', 'CMN_TEST'),
  (105, 'CMN-TEST-105', '검색 조건 정규화', 'QUERY', 'INACTIVE', 'search validation', 'MBR-TEST-105', 105, 0, 'CMN_TEST', 'CMN_TEST'),
  (106, 'CMN-TEST-106', '정렬 allowlist', 'QUERY', 'ACTIVE', 'stable sort allowlist', 'MBR-TEST-106', 106, 0, 'CMN_TEST', 'CMN_TEST'),
  (107, 'CMN-TEST-107', '낙관적 잠금 충돌', 'LOCK', 'ACTIVE', 'optimistic lock version', 'MBR-TEST-107', 107, 0, 'CMN_TEST', 'CMN_TEST'),
  (108, 'CMN-TEST-108', 'Transaction rollback', 'TRANSACTION', 'ACTIVE', 'transaction rollback', 'MBR-TEST-108', 108, 0, 'CMN_TEST', 'CMN_TEST')
) AS src(sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
ON (tgt.sample_item_id = src.sample_item_id)
WHEN MATCHED THEN UPDATE SET
  tgt.sample_key = src.sample_key,
  tgt.item_name = src.item_name,
  tgt.category_code = src.category_code,
  tgt.status_code = src.status_code,
  tgt.searchable_text = src.searchable_text,
  tgt.owner_reference = src.owner_reference,
  tgt.sort_order = src.sort_order,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by)
VALUES (src.sample_item_id, src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=refDB
-- CPF_USE_LOGICAL_DATABASE=refDB
DELETE FROM ref_center_cut_sample_result WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';

MERGE INTO ref_center_cut_sample_target tgt
USING (VALUES
  ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFlocal010000001', 'SEG-REF-CENTER-ROOT', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by)
ON (tgt.target_id = src.target_id)
WHEN MATCHED THEN UPDATE SET
  tgt.target_payload = src.target_payload,
  tgt.status_code = src.status_code,
  tgt.retry_count = src.retry_count,
  tgt.transaction_id = src.transaction_id,
  tgt.parent_segment_id = src.parent_segment_id,
  tgt.transaction_segment_id = src.transaction_segment_id,
  tgt.started_at = src.started_at,
  tgt.completed_at = src.completed_at,
  tgt.last_error_message = src.last_error_message,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by)
VALUES (src.target_id, src.center_cut_job_id, src.business_key, src.business_date, src.target_payload, src.status_code, src.retry_count, src.transaction_id, src.parent_segment_id, src.transaction_segment_id, src.started_at, src.completed_at, src.last_error_message, src.use_yn, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=mbrDB
-- CPF_USE_LOGICAL_DATABASE=mbrDB
MERGE INTO mbr_sample_item tgt
USING (VALUES
  ('MBR-SAMPLE-001', 'MBR 표준 거래 샘플 1', 'GENERAL', 'ACTIVE', 'crud search paging duplicate optimistic-lock', 'REF-SAMPLE-001', 10, 0, 'N', '20260615120000000MBRlocal010000001', 'MBR-SEED-IDEMPOTENCY-001', 'MBR_SEED', 'MBR_SEED'),
  ('MBR-SAMPLE-002', 'MBR 표준 거래 샘플 2', 'TRANSFER', 'ACTIVE', 'local remote call rollback', 'ACC-SAMPLE-001', 20, 0, 'N', '20260615120000000MBRlocal010000002', 'MBR-SEED-IDEMPOTENCY-002', 'MBR_SEED', 'MBR_SEED'),
  ('MBR-SAMPLE-003', 'MBR 비활성 거래 샘플', 'GENERAL', 'INACTIVE', 'status filter cursor slice', NULL, 30, 0, 'N', '20260615120000000MBRlocal010000003', 'MBR-SEED-IDEMPOTENCY-003', 'MBR_SEED', 'MBR_SEED')
) AS src(sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, deleted_yn, transaction_id, idempotency_key, created_by, updated_by)
ON (tgt.idempotency_key = src.idempotency_key)
WHEN MATCHED THEN UPDATE SET
  tgt.item_name = src.item_name,
  tgt.category_code = src.category_code,
  tgt.status_code = src.status_code,
  tgt.searchable_text = src.searchable_text,
  tgt.owner_reference = src.owner_reference,
  tgt.sort_order = src.sort_order,
  tgt.transaction_id = src.transaction_id,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, deleted_yn, transaction_id, idempotency_key, created_by, updated_by)
VALUES (src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.deleted_yn, src.transaction_id, src.idempotency_key, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
INSERT INTO cpf_transaction_log (LOG_DATE, TRANSACTION_ID, TRACE_ID, SPAN_ID, SEQUENCE_NO, MODULE_ID, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE, API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CALLER_INSTANCE_ID, CORRELATION_ID, IDEMPOTENCY_KEY, LOCALE, TIMEZONE, REQUEST_TYPE, ORIGINAL_CHANNEL_CODE, CHANNEL_CODE, MEMBER_NO, CUSTOMER_NO, SCREEN_ID, DEVICE_ID, WAS_ID, SERVER_INSTANCE_ID, HOST_NAME, PROCESS_ID, THREAD_NAME, HTTP_METHOD, URI, CONTROLLER, EXECUTION_PACKAGE, EXECUTION_CLASS, EXECUTION_METHOD, EXECUTION_SIGNATURE, PARAMETERS, REQUEST_BODY, RESPONSE, HTTP_STATUS, RESPONSE_CODE, EXEC_USER, CLIENT_IP, USER_AGENT, START_TIME, END_TIME, DURATION_MS, created_by, updated_by)
SELECT
    DATE(('2026-06-15 12:00:00.000')),
    ('20260615120000000MBRlocal010000001'),
    'trace-sample-001',
    'span-sample-001',
    1,
    'MBR',
    'MBR01BSE0001',
    'MBR 회원 목록 샘플',
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
    'MBR_LIST',
    'LOCAL_BROWSER',
    'local01',
    'local-dev:sql-seed',
    'local-dev',
    'sql-seed',
    'sql-smoke',
    'GET',
    '/mbr/list',
    'com.cpf.member.bse.controller.MbrController',
    'com.cpf.member.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"memberNo":"M000000001","password":"masked"}',
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
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
), 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
)
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
), 'fixedTelegram', 'M000000001회원1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
)
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
), 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
)
        AND DETAIL_KEY = 'memo'
  );


-- CPF_LOGICAL_DATABASE=admDB
-- CPF_USE_LOGICAL_DATABASE=admDB
MERGE INTO adm_dynamic_log_level_rule tgt
USING (VALUES
  ('sample-rule-001', NULL, 'MBR01BSE0001', 'MBR', 'DEBUG', (CURRENT_TIMESTAMP + INTERVAL '30 minute'), 'ADM 화면 smoke 검증용 동적 로그 규칙입니다.', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by)
ON (tgt.RULE_ID = src.RULE_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.BUSINESS_TRANSACTION_ID = src.BUSINESS_TRANSACTION_ID,
  tgt.MODULE_ID = src.MODULE_ID,
  tgt.LOG_LEVEL = src.LOG_LEVEL,
  tgt.EXPIRE_AT = src.EXPIRE_AT,
  tgt.REASON = src.REASON,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by)
VALUES (src.RULE_ID, src.TRANSACTION_ID, src.BUSINESS_TRANSACTION_ID, src.MODULE_ID, src.LOG_LEVEL, src.EXPIRE_AT, src.REASON, src.USE_YN, src.created_by, src.updated_by);


-- CPF_LOGICAL_DATABASE=bzaDB
-- CPF_USE_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_admin_user tgt
USING (VALUES
  ('bza-admin', '업무 관리자 샘플', NULL, 'BZA_MANAGER', 'Y', 'N', 0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM')
) AS src(admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by)
ON (tgt.admin_login_id = src.admin_login_id)
WHEN MATCHED THEN UPDATE SET
  tgt.admin_name = src.admin_name,
  tgt.role_code = src.role_code,
  tgt.use_yn = src.use_yn,
  tgt.lock_yn = src.lock_yn,
  tgt.login_fail_count = src.login_fail_count,
  tgt.password_change_required_yn = src.password_change_required_yn,
  tgt.password_expire_at = src.password_expire_at,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by)
VALUES (src.admin_login_id, src.admin_name, src.password_hash, src.role_code, src.use_yn, src.lock_yn, src.login_fail_count, src.password_change_required_yn, src.password_expire_at, src.last_login_at, src.created_by, src.updated_by);

INSERT INTO bza_login_history (admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent, transaction_id, module_id, was_id, server_instance_id, created_by, updated_by)
SELECT admin_user_id, 'BZA', 'bza-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000BZAbzaAP010000001', 'BZA', 'bzaAP01', 'local-bza:seed', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user
WHERE admin_login_id = 'bza-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM bza_login_history
      WHERE admin_login_id = 'bza-admin'
        AND transaction_id = '20260715120000000BZAbzaAP010000001'
  );

MERGE INTO bza_menu tgt
USING (VALUES
  ('DASHBOARD', '업무 대시보드', 'BZA', '/bza', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
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
  ('SAVED_SEARCH', '저장 검색', 'BZA', '/bza#savedSearches', '/api/bza/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM'),
  ('ACC_ROOT', 'ACC Reference', 'ACC', '/bza/domain/acc', '/api/v1/accounts', 900, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by)
ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET
  tgt.menu_name = src.menu_name,
  tgt.api_path = src.api_path,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by)
VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_role tgt
USING (VALUES
  ('BZA_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET
  tgt.role_name = src.role_name,
  tgt.write_allowed_yn = src.write_allowed_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_user_role tgt
USING (
  SELECT admin_user_id AS admin_user_id, 'BZA_MANAGER' AS role_code, CURRENT_TIMESTAMP AS valid_from, NULL AS valid_to, 'Y' AS primary_yn, 'CPF_TEST_SEED' AS grant_reason, 'CPF-TEST-BZA-ROLE-MANAGER-0001' AS operation_id, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM bza_admin_user
  WHERE admin_login_id = 'bza-admin'
) src
ON (tgt.operation_id = src.operation_id)
WHEN MATCHED THEN UPDATE SET
  tgt.valid_to = NULL,
  tgt.primary_yn = 'Y',
  tgt.grant_reason = src.grant_reason,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by)
VALUES (src.admin_user_id, src.role_code, src.valid_from, src.valid_to, src.primary_yn, src.grant_reason, src.operation_id, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (VALUES
  ('BZA_MANAGER', 'DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
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
  ('BZA_MANAGER', 'SAVED_SEARCH', 'WRITE', 'API', 'POST', '/api/bza/saved-searches/**', 'OWN', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.allow_yn = src.allow_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);

MERGE INTO bza_project_setting tgt
USING (VALUES
  ('DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(setting_key, setting_value, description, use_yn, created_by, updated_by)
ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET
  tgt.setting_value = src.setting_value,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_organization tgt
USING (VALUES
  ('HQ', NULL, '본사', 'COMPANY', 10, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
  ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, CURRENT_TIMESTAMP, NULL, 'Y', 'SYSTEM', 'SYSTEM')
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
  ('P3', '책임', 30, 'Y', 'SYSTEM', 'SYSTEM')
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
  ('OPERATOR', '업무담당자', 'N', 'Y', 'SYSTEM', 'SYSTEM')
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
USING (
  SELECT 'EMP001' AS employee_no, admin_user_id AS admin_user_id, 'OPS' AS organization_code, '업무 담당자' AS employee_name, 'P3' AS position_code, 'OPERATOR' AS job_title_code, 'ACTIVE' AS employment_status, CURRENT_DATE AS join_date, 'operator@example.com' AS email, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM bza_admin_user WHERE admin_login_id = 'bza-admin'
) src
ON (tgt.admin_user_id = src.admin_user_id)
WHEN MATCHED THEN UPDATE SET
  tgt.organization_code = src.organization_code,
  tgt.employee_name = src.employee_name,
  tgt.position_code = src.position_code,
  tgt.job_title_code = src.job_title_code,
  tgt.employment_status = src.employment_status,
  tgt.email = src.email,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by)
VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.employment_status, src.join_date, src.email, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_employee_assignment tgt
USING (VALUES
  ('EMP001', 'OPS', 'P3', 'OPERATOR', 'PRIMARY', 'Y', CURRENT_TIMESTAMP, NULL, 'SYSTEM', 'SYSTEM')
) AS src(employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
ON (tgt.employee_no = src.employee_no AND tgt.assignment_type = src.assignment_type AND tgt.primary_yn = src.primary_yn)
WHEN MATCHED THEN UPDATE SET
  tgt.organization_code = src.organization_code,
  tgt.position_code = src.position_code,
  tgt.job_title_code = src.job_title_code,
  tgt.effective_to = NULL,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by)
VALUES (src.employee_no, src.organization_code, src.position_code, src.job_title_code, src.assignment_type, src.primary_yn, src.effective_from, src.effective_to, src.created_by, src.updated_by);

INSERT INTO bza_notification (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by)
SELECT 'bza-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'BZA-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM bza_notification
     WHERE recipient_login_id = 'bza-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'BZA-SAMPLE-001'
);

MERGE INTO bza_saved_search tgt
USING (VALUES
  ('bza-admin', 'APPROVAL', '진행 중 결재', '{"approvalStatus":"IN_REVIEW"}', 'N', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by)
ON (tgt.owner_login_id = src.owner_login_id AND tgt.screen_code = src.screen_code AND tgt.search_name = src.search_name)
WHEN MATCHED THEN UPDATE SET
  tgt.criteria_json = src.criteria_json,
  tgt.shared_yn = src.shared_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by)
VALUES (src.owner_login_id, src.screen_code, src.search_name, src.criteria_json, src.shared_yn, src.use_yn, src.created_by, src.updated_by);
