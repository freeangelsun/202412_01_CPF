-- 로컬 및 통합 검증용 테스트 데이터입니다.

USE pfwDB;

INSERT INTO pfw_file_exchange_log (
    EXCHANGE_ID,
    TRANSACTION_ID,
    TRACE_ID,
    BUSINESS_TRANSACTION_ID,
    ACTION_TYPE,
    PROTOCOL,
    DIRECTION,
    EXECUTED_YN,
    SUCCESS_YN,
    HOST,
    SOURCE_PATH,
    TARGET_PATH,
    REQUEST_USER,
    MESSAGE,
    created_by,
    updated_by
) VALUES (
    'FILE-LOCAL-SAMPLE-001',
    'TEST_TRANSACTION',
    'TEST_TRACE',
    'XYZ08EDU0001',
    'LOCAL_WRITE',
    'LOCAL',
    'WRITE',
    'Y',
    'Y',
    'localhost',
    '/tmp/cpf/source.txt',
    '/tmp/cpf/target.txt',
    'SYSTEM',
    '로컬 파일 교환 샘플 이력입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MESSAGE = VALUES(MESSAGE),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE cmnDB;

INSERT INTO cmn_edu_query_item (
    item_id, item_name, category_code, status_code, owner_member_no, use_yn, created_by, updated_by
) VALUES
    (1, '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'M000000001', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'M000000002', 'Y', 'SYSTEM', 'SYSTEM'),
    (3, 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000003', 'Y', 'SYSTEM', 'SYSTEM'),
    (4, 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'M000000004', 'Y', 'SYSTEM', 'SYSTEM'),
    (5, '검색 조건 정규화', 'QUERY', 'INACTIVE', 'M000000005', 'Y', 'SYSTEM', 'SYSTEM'),
    (6, '정렬 whitelist', 'QUERY', 'ACTIVE', 'M000000006', 'Y', 'SYSTEM', 'SYSTEM'),
    (7, '하위 호출 헤더 전파', 'HEADER', 'ACTIVE', 'M000000007', 'Y', 'SYSTEM', 'SYSTEM'),
    (8, 'Swagger 조회 예시', 'DOC', 'ACTIVE', 'M000000008', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    owner_member_no = VALUES(owner_member_no),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE xyzDB;

DELETE FROM xyz_center_cut_sample_result
WHERE center_cut_job_id = 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB';

INSERT INTO xyz_center_cut_sample_target (
    target_id, center_cut_job_id, business_key, business_date, target_payload,
    status_code, retry_count, parent_transaction_global_id, child_transaction_global_id,
    started_at, completed_at, last_error_message, use_yn, created_by, updated_by
) VALUES
    ('XYZ-CENTER-CUT-001', 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB', 'XYZ-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000XYZparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('XYZ-CENTER-CUT-002', 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB', 'XYZ-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000XYZparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('XYZ-CENTER-CUT-003', 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB', 'XYZ-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000XYZparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('XYZ-CENTER-CUT-004', 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB', 'XYZ-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000XYZparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    target_payload = VALUES(target_payload),
    status_code = VALUES(status_code),
    retry_count = VALUES(retry_count),
    parent_transaction_global_id = VALUES(parent_transaction_global_id),
    child_transaction_global_id = VALUES(child_transaction_global_id),
    started_at = VALUES(started_at),
    completed_at = VALUES(completed_at),
    last_error_message = VALUES(last_error_message),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE mbrDB;

INSERT INTO mbr_member (
    id, member_no, customer_no, login_id, name, email, mobile_no,
    password_hash, login_fail_count, password_change_required_yn, password_expire_at,
    member_status, lock_yn, withdraw_yn, channel_code, description, created_by, updated_by
) VALUES
    (1, 'M000000001', 'C000000001', 'mbr001', '회원 1', 'mbr001@example.com', '010-1000-0001', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'WEB', 'MBR 샘플 회원 1', 'SYSTEM', 'SYSTEM'),
    (2, 'M000000002', 'C000000002', 'mbr002', '회원 2', 'mbr002@example.com', '010-1000-0002', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'MOBILE', 'MBR 샘플 회원 2', 'SYSTEM', 'SYSTEM'),
    (3, 'M000000003', 'C000000003', 'mbr003', '회원 3', 'mbr003@example.com', '010-1000-0003', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'DORMANT', 'N', 'N', 'WEB', 'MBR 휴면 회원 샘플', 'SYSTEM', 'SYSTEM'),
    (100, 'M000000100', 'C000000100', 'search.target', '검색 대상', 'search@example.com', '010-9999-0100', 'PBKDF2$SEED$REPLACE_BY_RUNTIME_HASH', 0, 'N', NULL, 'ACTIVE', 'N', 'N', 'WEB', 'MBR 이름 검색 테스트 행', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    customer_no = VALUES(customer_no),
    login_id = VALUES(login_id),
    password_hash = VALUES(password_hash),
    login_fail_count = VALUES(login_fail_count),
    password_change_required_yn = VALUES(password_change_required_yn),
    password_expire_at = VALUES(password_expire_at),
    name = VALUES(name),
    email = VALUES(email),
    mobile_no = VALUES(mobile_no),
    member_status = VALUES(member_status),
    lock_yn = VALUES(lock_yn),
    withdraw_yn = VALUES(withdraw_yn),
    channel_code = VALUES(channel_code),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_role (
    member_id, service_code, role_code, role_name, grade_code, temporary_yn, expire_at,
    granted_by, use_yn, created_by, updated_by
) VALUES
    (1, 'MBR', 'MBR_USER', '일반 회원', 'NORMAL', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM'),
    (2, 'MBR', 'MBR_PREMIUM', '프리미엄 회원', 'PREMIUM', 'N', NULL, 'SYSTEM', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    grade_code = VALUES(grade_code),
    temporary_yn = VALUES(temporary_yn),
    expire_at = VALUES(expire_at),
    granted_by = VALUES(granted_by),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO mbr_member_login_history (
    member_id, login_domain, member_no, customer_no, login_id, login_result, login_ip, user_agent, failure_reason,
    transaction_global_id, module_id, was_id, server_instance_id, created_by, updated_by
)
SELECT 1, 'MBR', 'M000000001', 'C000000001', 'mbr001', 'SUCCESS', '127.0.0.1', 'SQL-SEED', NULL,
       '20260615120000000MBRlocal010000001', 'MBR', 'local01', 'local-mbr:seed', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM mbr_member_login_history
    WHERE member_id = 1
      AND login_id = 'mbr001'
      AND user_agent = 'SQL-SEED'
);

USE pfwDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';
SET @sample_start_time = '2026-06-15 12:00:00.000';
SET @sample_end_time = '2026-06-15 12:00:00.012';

INSERT INTO pfw_transaction_log (
    LOG_DATE,
    TRANSACTION_ID,
    TRACE_ID,
    SPAN_ID,
    SEQUENCE_NO,
    MODULE_ID,
    BUSINESS_TRANSACTION_ID,
    BUSINESS_TRANSACTION_NAME,
    LOG_TYPE,
    API_VERSION,
    CLIENT_APP_ID,
    CLIENT_VERSION,
    CALLER_SERVICE,
    CALLER_INSTANCE_ID,
    CORRELATION_ID,
    IDEMPOTENCY_KEY,
    LOCALE,
    TIMEZONE,
    REQUEST_TYPE,
    ORIGINAL_CHANNEL_CODE,
    CHANNEL_CODE,
    MEMBER_NO,
    CUSTOMER_NO,
    SCREEN_ID,
    DEVICE_ID,
    WAS_ID,
    SERVER_INSTANCE_ID,
    HOST_NAME,
    PROCESS_ID,
    THREAD_NAME,
    HTTP_METHOD,
    URI,
    CONTROLLER,
    EXECUTION_PACKAGE,
    EXECUTION_CLASS,
    EXECUTION_METHOD,
    EXECUTION_SIGNATURE,
    PARAMETERS,
    REQUEST_BODY,
    RESPONSE,
    HTTP_STATUS,
    RESPONSE_CODE,
    EXEC_USER,
    CLIENT_IP,
    USER_AGENT,
    START_TIME,
    END_TIME,
    DURATION_MS,
    created_by,
    updated_by
)
SELECT
    DATE(@sample_start_time),
    @sample_transaction_id,
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
    'xyz-education',
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
    'cpf.mbr.bse.controller.MbrController',
    'cpf.mbr.bse.controller',
    'MbrController',
    'getAllMembers',
    'MbrController.getAllMembers()',
    '{}',
    '{"memberNo":"M000000001","password":"masked"}',
    '{"code":"SPFW000000","message":"정상 처리되었습니다."}',
    200,
    'SPFW000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    @sample_start_time,
    @sample_end_time,
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM pfw_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'fixedTelegram', 'M000000001회원1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO pfw_transaction_log_detail (
    LOG_IDX,
    DETAIL_KEY,
    DETAIL_VALUE,
    created_by,
    updated_by
)
SELECT @sample_log_idx, 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE @sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

USE admDB;

INSERT INTO adm_dynamic_log_level_rule (
    RULE_ID,
    TRANSACTION_ID,
    BUSINESS_TRANSACTION_ID,
    MODULE_ID,
    LOG_LEVEL,
    EXPIRE_AT,
    REASON,
    USE_YN,
    created_by,
    updated_by
) VALUES (
    'sample-rule-001',
    NULL,
    'MBR01BSE0001',
    'MBR',
    'DEBUG',
    DATE_ADD(NOW(), INTERVAL 30 MINUTE),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    BUSINESS_TRANSACTION_ID = VALUES(BUSINESS_TRANSACTION_ID),
    MODULE_ID = VALUES(MODULE_ID),
    LOG_LEVEL = VALUES(LOG_LEVEL),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    REASON = VALUES(REASON),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

USE bzaDB;

INSERT INTO bza_admin_user (
    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
    login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by
) VALUES (
    'bza-admin', '업무 관리자 샘플', NULL, 'BZA_MANAGER', 'Y', 'N',
    0, 'Y', NULL, NULL, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    admin_name = VALUES(admin_name),
    role_code = VALUES(role_code),
    use_yn = VALUES(use_yn),
    lock_yn = VALUES(lock_yn),
    login_fail_count = VALUES(login_fail_count),
    password_change_required_yn = VALUES(password_change_required_yn),
    password_expire_at = VALUES(password_expire_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_login_history (
    admin_user_id, login_domain, admin_login_id, login_result, failure_reason, client_ip, user_agent,
    transaction_global_id, module_id, was_id, server_instance_id, created_by, updated_by
)
SELECT admin_user_id, 'BZA', 'bza-admin', 'SUCCESS', NULL, '127.0.0.1', 'SQL-SEED',
       '20260715120000000BZAbzaAP010000001', 'BZA', 'bzaAP01', 'local-bza:seed', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user
WHERE admin_login_id = 'bza-admin'
  AND NOT EXISTS (
      SELECT 1
      FROM bza_login_history
      WHERE admin_login_id = 'bza-admin'
        AND transaction_global_id = '20260715120000000BZAbzaAP010000001'
  );

INSERT INTO bza_menu (
    menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by
) VALUES
    ('DASHBOARD', '업무 대시보드', 'BZA', '/bza', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('USER', '백오피스 사용자', 'BZA', '/bza#users', '/api/bza/admin-users', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ORGANIZATION', '조직 관리', 'BZA', '/bza#organizations', '/api/bza/backoffice/organizations', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EMPLOYEE', '직원 관리', 'BZA', '/bza#employees', '/api/bza/backoffice/employees', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ROLE', '역할 관리', 'BZA', '/bza#roles', '/api/bza/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MENU', '메뉴 관리', 'BZA', '/bza#menus', '/api/bza/menus', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', '권한 관리', 'BZA', '/bza#permissions', '/api/bza/permissions', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', '결재 관리', 'BZA', '/bza#approvals', '/api/bza/backoffice/approvals', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CUSTOMER', '고객 업무 관리', 'BZA', '/bza#customers', '/api/bza/customers', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PRODUCT', '상품 관리', 'BZA', '/bza#products', '/api/bza/products', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ORDER', '주문 관리', 'BZA', '/bza#orders', '/api/bza/orders', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SETTING', '업무 설정', 'BZA', '/bza#settings', '/api/bza/settings', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', '다운로드 감사', 'BZA', '/bza#downloads', '/api/bza/downloads', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT', '업무 감사', 'BZA', '/bza#audits', '/api/bza/backoffice/audits', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', '업무 알림', 'BZA', '/bza#notifications', '/api/bza/notifications', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ATTACHMENT', '첨부파일', 'BZA', '/bza#attachments', '/api/bza/attachments', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAVED_SEARCH', '저장 검색', 'BZA', '/bza#savedSearches', '/api/bza/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    api_path = VALUES(api_path),
    sort_order = VALUES(sort_order),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_role (
    role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by
) VALUES (
    'BZA_MANAGER', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    write_allowed_yn = VALUES(write_allowed_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_permission (
    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
    data_scope, allow_yn, created_by, updated_by
) VALUES
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
    ('BZA_MANAGER', 'CUSTOMER', 'READ', 'API', 'GET', '/api/bza/customers/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'CUSTOMER', 'UNMASK', 'API', 'POST', '/api/bza/masking/unmask', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'PRODUCT', 'READ', 'API', 'GET', '/api/bza/products/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BZA_MANAGER', 'ORDER', 'READ', 'API', 'GET', '/api/bza/orders/**', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
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
ON DUPLICATE KEY UPDATE
    allow_yn = VALUES(allow_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_customer (
    customer_no, customer_name, email, mobile_no, customer_status, created_by, updated_by
) VALUES (
    'CUST000001', '샘플 고객', 'customer@example.com', '010-0000-0001', 'ACTIVE', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    customer_name = VALUES(customer_name),
    email = VALUES(email),
    mobile_no = VALUES(mobile_no),
    customer_status = VALUES(customer_status),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_product (
    product_code, product_name, use_yn, created_by, updated_by
) VALUES (
    'PRD_SAMPLE', '샘플 상품', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_order (
    order_no, customer_no, product_code, order_amount, order_status, created_by, updated_by
) VALUES (
    'ORD000001', 'CUST000001', 'PRD_SAMPLE', 10000.00, 'REQUESTED', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    customer_no = VALUES(customer_no),
    product_code = VALUES(product_code),
    order_amount = VALUES(order_amount),
    order_status = VALUES(order_status),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_project_setting (
    setting_key, setting_value, description, use_yn, created_by, updated_by
) VALUES (
    'bza.masking.enabled', 'Y', '업무 관리자 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    setting_value = VALUES(setting_value),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_masking_audit (
    target_type, target_id, operator_id, reason, result_type, created_by, updated_by
)
SELECT 'CUSTOMER', 'CUST000001', 'biz-admin', '업무 관리자 샘플 원문보기 감사', 'SUCCESS', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM bza_masking_audit
    WHERE target_type = 'CUSTOMER'
      AND target_id = 'CUST000001'
      AND operator_id = 'biz-admin'
      AND reason = '업무 관리자 샘플 원문보기 감사'
);

INSERT INTO bza_organization (
    organization_code, parent_organization_code, organization_name, organization_type,
    sort_order, use_yn, created_by, updated_by
) VALUES
    ('HQ', NULL, '본사', 'COMPANY', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS', 'HQ', '업무운영팀', 'DEPARTMENT', 20, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_organization_code = VALUES(parent_organization_code),
    organization_name = VALUES(organization_name),
    organization_type = VALUES(organization_type),
    sort_order = VALUES(sort_order), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_employee (
    employee_no, admin_user_id, organization_code, employee_name, position_code,
    job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by
)
SELECT 'EMP001', admin_user_id, 'OPS', '업무 담당자', 'P3', 'OPERATOR', 'ACTIVE', CURRENT_DATE,
       'operator@example.com', 'Y', 'SYSTEM', 'SYSTEM'
FROM bza_admin_user WHERE admin_login_id = 'bza-admin'
ON DUPLICATE KEY UPDATE
    admin_user_id = VALUES(admin_user_id), organization_code = VALUES(organization_code),
    employee_name = VALUES(employee_name), position_code = VALUES(position_code),
    job_title_code = VALUES(job_title_code), employment_status = VALUES(employment_status),
    email = VALUES(email), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;

INSERT INTO bza_notification (
    recipient_login_id, notification_type, title, message_body,
    reference_type, reference_id, read_yn, use_yn, created_by, updated_by
)
SELECT 'bza-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'BZA-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM bza_notification
     WHERE recipient_login_id = 'bza-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'BZA-SAMPLE-001'
);

INSERT INTO bza_saved_search (
    owner_login_id, screen_code, search_name, criteria_json,
    shared_yn, use_yn, created_by, updated_by
) VALUES (
    'bza-admin', 'APPROVAL', '진행 중 결재', '{"approvalStatus":"IN_REVIEW"}',
    'N', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    criteria_json = VALUES(criteria_json), shared_yn = VALUES(shared_yn), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP;
