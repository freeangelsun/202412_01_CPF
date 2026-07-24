-- 로컬 및 통합 검증용 테스트 데이터입니다.

USE cmnDB;

INSERT INTO cmn_sample_item (
    sample_item_id, sample_key, item_name, category_code, status_code,
    searchable_text, owner_reference, sort_order, version_no, created_by, updated_by
) VALUES
    (101, 'CMN-TEST-101', '표준 헤더 단건 조회', 'HEADER', 'ACTIVE', 'header single query', 'MBR-TEST-101', 101, 0, 'CMN_TEST', 'CMN_TEST'),
    (102, 'CMN-TEST-102', '거래 로그 목록 조회', 'LOG', 'ACTIVE', 'transaction log list', 'MBR-TEST-102', 102, 0, 'CMN_TEST', 'CMN_TEST'),
    (103, 'CMN-TEST-103', 'offset 페이징 조회', 'QUERY', 'ACTIVE', 'offset page', 'MBR-TEST-103', 103, 0, 'CMN_TEST', 'CMN_TEST'),
    (104, 'CMN-TEST-104', 'keyset 페이징 조회', 'QUERY', 'ACTIVE', 'keyset cursor', 'MBR-TEST-104', 104, 0, 'CMN_TEST', 'CMN_TEST'),
    (105, 'CMN-TEST-105', '검색 조건 정규화', 'QUERY', 'INACTIVE', 'search validation', 'MBR-TEST-105', 105, 0, 'CMN_TEST', 'CMN_TEST'),
    (106, 'CMN-TEST-106', '정렬 allowlist', 'QUERY', 'ACTIVE', 'stable sort allowlist', 'MBR-TEST-106', 106, 0, 'CMN_TEST', 'CMN_TEST'),
    (107, 'CMN-TEST-107', '낙관적 잠금 충돌', 'LOCK', 'ACTIVE', 'optimistic lock version', 'MBR-TEST-107', 107, 0, 'CMN_TEST', 'CMN_TEST'),
    (108, 'CMN-TEST-108', 'Transaction rollback', 'TRANSACTION', 'ACTIVE', 'transaction rollback', 'MBR-TEST-108', 108, 0, 'CMN_TEST', 'CMN_TEST')
ON DUPLICATE KEY UPDATE
    sample_key = VALUES(sample_key),
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    searchable_text = VALUES(searchable_text),
    owner_reference = VALUES(owner_reference),
    sort_order = VALUES(sort_order),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

USE refDB;

DELETE FROM ref_center_cut_sample_result
WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';

INSERT INTO ref_center_cut_sample_target (
    target_id, center_cut_job_id, business_key, business_date, target_payload,
    status_code, retry_count, parent_transaction_global_id, child_transaction_global_id,
    started_at, completed_at, last_error_message, use_yn, created_by, updated_by
) VALUES
    ('REF-CENTER-CUT-001', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-001', '2026-07-02', '{"amount":1000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-002', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-002', '2026-07-02', '{"amount":2000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-003', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-003', '2026-07-02', '{"amount":3000,"forceFail":true}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF-CENTER-CUT-004', 'CPF_REF_CENTER_CUT_SAMPLE_JOB', 'REF-ORDER-20260702-004', '2026-07-02', '{"amount":4000,"forceFail":false}', 'READY', 0, '20260702110000000REFparent0000001', NULL, NULL, NULL, NULL, 'Y', 'SYSTEM', 'SYSTEM')
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

INSERT INTO mbr_sample_item (
    sample_key, item_name, category_code, status_code,
    searchable_text, owner_reference, sort_order, version_no, deleted_yn,
    transaction_global_id, idempotency_key, created_by, updated_by
) VALUES
    ('MBR-SAMPLE-001', 'MBR 표준 거래 샘플 1', 'GENERAL', 'ACTIVE',
     'crud search paging duplicate optimistic-lock', 'REF-SAMPLE-001', 10, 0, 'N',
     '20260615120000000MBRlocal010000001', 'MBR-SEED-IDEMPOTENCY-001', 'MBR_SEED', 'MBR_SEED'),
    ('MBR-SAMPLE-002', 'MBR 표준 거래 샘플 2', 'TRANSFER', 'ACTIVE',
     'local remote call rollback', 'ACC-SAMPLE-001', 20, 0, 'N',
     '20260615120000000MBRlocal010000002', 'MBR-SEED-IDEMPOTENCY-002', 'MBR_SEED', 'MBR_SEED'),
    ('MBR-SAMPLE-003', 'MBR 비활성 거래 샘플', 'GENERAL', 'INACTIVE',
     'status filter cursor slice', NULL, 30, 0, 'N',
     '20260615120000000MBRlocal010000003', 'MBR-SEED-IDEMPOTENCY-003', 'MBR_SEED', 'MBR_SEED')
ON DUPLICATE KEY UPDATE
    item_name = VALUES(item_name),
    category_code = VALUES(category_code),
    status_code = VALUES(status_code),
    searchable_text = VALUES(searchable_text),
    owner_reference = VALUES(owner_reference),
    sort_order = VALUES(sort_order),
    transaction_global_id = VALUES(transaction_global_id),
    idempotency_key = VALUES(idempotency_key),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

USE cpfDB;

SET @sample_transaction_id = '20260615120000000MBRlocal010000001';
SET @sample_start_time = '2026-06-15 12:00:00.000';
SET @sample_end_time = '2026-06-15 12:00:00.012';

INSERT INTO cpf_transaction_log (
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
    @sample_start_time,
    @sample_end_time,
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
);

SET @sample_log_idx = (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = @sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'MBR01BSE0001'
    ORDER BY LOG_IDX
    LIMIT 1
);

INSERT INTO cpf_transaction_log_detail (
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
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO cpf_transaction_log_detail (
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
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = @sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO cpf_transaction_log_detail (
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
      FROM cpf_transaction_log_detail
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
    ('SETTING', '업무 설정', 'BZA', '/bza#settings', '/api/bza/settings', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', '다운로드 감사', 'BZA', '/bza#downloads', '/api/bza/downloads', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT', '업무 감사', 'BZA', '/bza#audits', '/api/bza/backoffice/audits', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', '업무 알림', 'BZA', '/bza#notifications', '/api/bza/notifications', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ATTACHMENT', '첨부파일', 'BZA', '/bza#attachments', '/api/bza/attachments', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SAVED_SEARCH', '저장 검색', 'BZA', '/bza#savedSearches', '/api/bza/saved-searches', 170, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ACC_ROOT', 'ACC Reference', 'ACC', '/bza/domain/acc', '/api/v1/accounts', 900, 'Y', 'SYSTEM', 'SYSTEM')
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

INSERT INTO bza_project_setting (
    setting_key, setting_value, description, use_yn, created_by, updated_by
) VALUES (
    'DOWNLOAD.MASKING.ENABLED', 'Y', '업무 다운로드 마스킹 사용 여부', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    setting_value = VALUES(setting_value),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

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
