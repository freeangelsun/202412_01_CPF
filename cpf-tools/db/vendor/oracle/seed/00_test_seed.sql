-- CPF generated lifecycle bundle; vendor=oracle
-- Source plan: cpf-tools/config/database-source-plan.json

-- ===== BEGIN 70_test_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=70_test_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cmnDB
MERGE INTO cmn_sample_item tgt USING (
SELECT 101 sample_item_id, 'CMN-TEST-101' sample_key, '표준 헤더 단건 조회' item_name, 'HEADER' category_code, 'ACTIVE' status_code, 'header single query' searchable_text, 'CPF-TEST-101' owner_reference, 101 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 102 sample_item_id, 'CMN-TEST-102' sample_key, '거래 로그 목록 조회' item_name, 'LOG' category_code, 'ACTIVE' status_code, 'transaction log list' searchable_text, 'CPF-TEST-102' owner_reference, 102 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 103 sample_item_id, 'CMN-TEST-103' sample_key, 'offset 페이징 조회' item_name, 'QUERY' category_code, 'ACTIVE' status_code, 'offset page' searchable_text, 'CPF-TEST-103' owner_reference, 103 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 104 sample_item_id, 'CMN-TEST-104' sample_key, 'keyset 페이징 조회' item_name, 'QUERY' category_code, 'ACTIVE' status_code, 'keyset cursor' searchable_text, 'CPF-TEST-104' owner_reference, 104 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 105 sample_item_id, 'CMN-TEST-105' sample_key, '검색 조건 정규화' item_name, 'QUERY' category_code, 'INACTIVE' status_code, 'search validation' searchable_text, 'CPF-TEST-105' owner_reference, 105 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 106 sample_item_id, 'CMN-TEST-106' sample_key, '정렬 allowlist' item_name, 'QUERY' category_code, 'ACTIVE' status_code, 'stable sort allowlist' searchable_text, 'CPF-TEST-106' owner_reference, 106 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 107 sample_item_id, 'CMN-TEST-107' sample_key, '낙관적 잠금 충돌' item_name, 'LOCK' category_code, 'ACTIVE' status_code, 'optimistic lock version' searchable_text, 'CPF-TEST-107' owner_reference, 107 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
UNION ALL
SELECT 108 sample_item_id, 'CMN-TEST-108' sample_key, 'Transaction rollback' item_name, 'TRANSACTION' category_code, 'ACTIVE' status_code, 'transaction rollback' searchable_text, 'CPF-TEST-108' owner_reference, 108 sort_order, 0 version_no, 'CMN_TEST' created_by, 'CMN_TEST' updated_by FROM dual
) src ON (tgt.sample_item_id = src.sample_item_id)
WHEN MATCHED THEN UPDATE SET tgt.sample_key = src.sample_key, tgt.item_name = src.item_name, tgt.category_code = src.category_code, tgt.status_code = src.status_code, tgt.searchable_text = src.searchable_text, tgt.owner_reference = src.owner_reference, tgt.sort_order = src.sort_order, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (sample_item_id, sample_key, item_name, category_code, status_code, searchable_text, owner_reference, sort_order, version_no, created_by, updated_by) VALUES (src.sample_item_id, src.sample_key, src.item_name, src.category_code, src.status_code, src.searchable_text, src.owner_reference, src.sort_order, src.version_no, src.created_by, src.updated_by);

-- CPF_LOGICAL_DATABASE=refDB
DELETE FROM ref_center_cut_sample_result WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';
MERGE INTO ref_center_cut_sample_target tgt USING (
SELECT 'REF-CENTER-CUT-001' target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'REF-ORDER-20260702-001' business_key, '2026-07-02' business_date, '{"amount":1000,"forceFail":false}' target_payload, 'READY' status_code, 0 retry_count, '20260702110000000REFlocal010000001' transaction_id, 'SEG-REF-CENTER-ROOT' parent_segment_id, NULL transaction_segment_id, NULL started_at, NULL completed_at, NULL last_error_message, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-002' target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'REF-ORDER-20260702-002' business_key, '2026-07-02' business_date, '{"amount":2000,"forceFail":false}' target_payload, 'READY' status_code, 0 retry_count, '20260702110000000REFlocal010000001' transaction_id, 'SEG-REF-CENTER-ROOT' parent_segment_id, NULL transaction_segment_id, NULL started_at, NULL completed_at, NULL last_error_message, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-003' target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'REF-ORDER-20260702-003' business_key, '2026-07-02' business_date, '{"amount":3000,"forceFail":true}' target_payload, 'READY' status_code, 0 retry_count, '20260702110000000REFlocal010000001' transaction_id, 'SEG-REF-CENTER-ROOT' parent_segment_id, NULL transaction_segment_id, NULL started_at, NULL completed_at, NULL last_error_message, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REF-CENTER-CUT-004' target_id, 'CPF_REF_CENTER_CUT_SAMPLE_JOB' center_cut_job_id, 'REF-ORDER-20260702-004' business_key, '2026-07-02' business_date, '{"amount":4000,"forceFail":false}' target_payload, 'READY' status_code, 0 retry_count, '20260702110000000REFlocal010000001' transaction_id, 'SEG-REF-CENTER-ROOT' parent_segment_id, NULL transaction_segment_id, NULL started_at, NULL completed_at, NULL last_error_message, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.target_id = src.target_id)
WHEN MATCHED THEN UPDATE SET tgt.target_payload = src.target_payload, tgt.status_code = src.status_code, tgt.retry_count = src.retry_count, tgt.transaction_id = src.transaction_id, tgt.parent_segment_id = src.parent_segment_id, tgt.transaction_segment_id = src.transaction_segment_id, tgt.started_at = src.started_at, tgt.completed_at = src.completed_at, tgt.last_error_message = src.last_error_message, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (target_id, center_cut_job_id, business_key, business_date, target_payload, status_code, retry_count, transaction_id, parent_segment_id, transaction_segment_id, started_at, completed_at, last_error_message, use_yn, created_by, updated_by) VALUES (src.target_id, src.center_cut_job_id, src.business_key, src.business_date, src.target_payload, src.status_code, src.retry_count, src.transaction_id, src.parent_segment_id, src.transaction_segment_id, src.started_at, src.completed_at, src.last_error_message, src.use_yn, src.created_by, src.updated_by);

-- CPF_LOGICAL_DATABASE=cpfDB
DEFINE sample_transaction_id = '20260615120000000MBRlocal010000001'
DEFINE sample_start_time = '2026-06-15 12:00:00.000'
DEFINE sample_end_time = '2026-06-15 12:00:00.012'
INSERT INTO cpf_transaction_log (LOG_DATE, TRANSACTION_ID, TRACE_ID, SPAN_ID, SEQUENCE_NO, MODULE_ID, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE, API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CALLER_INSTANCE_ID, CORRELATION_ID, IDEMPOTENCY_KEY, LOCALE, TIMEZONE, REQUEST_TYPE, ORIGINAL_CHANNEL_CODE, CHANNEL_CODE, MEMBER_NO, CUSTOMER_NO, SCREEN_ID, DEVICE_ID, WAS_ID, SERVER_INSTANCE_ID, HOST_NAME, PROCESS_ID, THREAD_NAME, HTTP_METHOD, URI, CONTROLLER, EXECUTION_PACKAGE, EXECUTION_CLASS, EXECUTION_METHOD, EXECUTION_SIGNATURE, PARAMETERS, REQUEST_BODY, RESPONSE, HTTP_STATUS, RESPONSE_CODE, EXEC_USER, CLIENT_IP, USER_AGENT, START_TIME, END_TIME, DURATION_MS, created_by, updated_by) SELECT
    TRUNC(&&sample_start_time),
    &&sample_transaction_id,
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
    &&sample_start_time,
    &&sample_end_time,
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = &&sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'OREFAA0001'
);
COLUMN sample_log_idx NEW_VALUE sample_log_idx NOPRINT
SELECT (
    SELECT LOG_IDX
    FROM cpf_transaction_log
    WHERE TRANSACTION_ID = &&sample_transaction_id
      AND BUSINESS_TRANSACTION_ID = 'OREFAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROWS ONLY) AS sample_log_idx FROM dual;
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT &&sample_log_idx, 'headers', '{"X-Channel-Code":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE &&sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = &&sample_log_idx
        AND DETAIL_KEY = 'headers'
  );
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT &&sample_log_idx, 'fixedTelegram', 'S000000001샘플1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE &&sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = &&sample_log_idx
        AND DETAIL_KEY = 'fixedTelegram'
  );
INSERT INTO cpf_transaction_log_detail (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by) SELECT &&sample_log_idx, 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE &&sample_log_idx IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_transaction_log_detail
      WHERE LOG_IDX = &&sample_log_idx
        AND DETAIL_KEY = 'memo'
  );

-- CPF_LOGICAL_DATABASE=admDB
MERGE INTO adm_dynamic_log_level_rule tgt USING (
SELECT 'sample-rule-001' RULE_ID, NULL TRANSACTION_ID, 'OREFAA0001' BUSINESS_TRANSACTION_ID, 'REF' MODULE_ID, 'DEBUG' LOG_LEVEL, (SYSTIMESTAMP + INTERVAL '30' MINUTE) EXPIRE_AT, 'ADM 화면 smoke 검증용 동적 로그 규칙입니다.' REASON, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.RULE_ID = src.RULE_ID)
WHEN MATCHED THEN UPDATE SET tgt.BUSINESS_TRANSACTION_ID = src.BUSINESS_TRANSACTION_ID, tgt.MODULE_ID = src.MODULE_ID, tgt.LOG_LEVEL = src.LOG_LEVEL, tgt.EXPIRE_AT = src.EXPIRE_AT, tgt.REASON = src.REASON, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by) VALUES (src.RULE_ID, src.TRANSACTION_ID, src.BUSINESS_TRANSACTION_ID, src.MODULE_ID, src.LOG_LEVEL, src.EXPIRE_AT, src.REASON, src.USE_YN, src.created_by, src.updated_by);

-- CPF_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_admin_user tgt USING (
SELECT 'bza-admin' admin_login_id, '업무 관리자 샘플' admin_name, NULL password_hash, 'BZA_MANAGER' role_code, 'Y' use_yn, 'N' lock_yn, 0 login_fail_count, 'Y' password_change_required_yn, NULL password_expire_at, NULL last_login_at, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.admin_login_id = src.admin_login_id)
WHEN MATCHED THEN UPDATE SET tgt.admin_name = src.admin_name, tgt.role_code = src.role_code, tgt.use_yn = src.use_yn, tgt.lock_yn = src.lock_yn, tgt.login_fail_count = src.login_fail_count, tgt.password_change_required_yn = src.password_change_required_yn, tgt.password_expire_at = src.password_expire_at, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn, login_fail_count, password_change_required_yn, password_expire_at, last_login_at, created_by, updated_by) VALUES (src.admin_login_id, src.admin_name, src.password_hash, src.role_code, src.use_yn, src.lock_yn, src.login_fail_count, src.password_change_required_yn, src.password_expire_at, src.last_login_at, src.created_by, src.updated_by);
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
MERGE INTO bza_menu tgt USING (
SELECT 'DASHBOARD' menu_code, '업무 대시보드' menu_name, 'BZA' module_code, '/bza' route_path, '/api/bza/dashboard' api_path, 10 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'USER' menu_code, '백오피스 사용자' menu_name, 'BZA' module_code, '/bza#users' route_path, '/api/bza/admin-users' api_path, 20 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ORGANIZATION' menu_code, '조직 관리' menu_name, 'BZA' module_code, '/bza#organizations' route_path, '/api/bza/backoffice/organizations' api_path, 30 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EMPLOYEE' menu_code, '직원 관리' menu_name, 'BZA' module_code, '/bza#employees' route_path, '/api/bza/backoffice/employees' api_path, 40 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ROLE' menu_code, '역할 관리' menu_name, 'BZA' module_code, '/bza#roles' route_path, '/api/bza/roles' api_path, 50 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MENU' menu_code, '메뉴 관리' menu_name, 'BZA' module_code, '/bza#menus' route_path, '/api/bza/menus' api_path, 60 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PERMISSION' menu_code, '권한 관리' menu_name, 'BZA' module_code, '/bza#permissions' route_path, '/api/bza/permissions' api_path, 70 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'APPROVAL' menu_code, '결재 관리' menu_name, 'BZA' module_code, '/bza#approvals' route_path, '/api/bza/backoffice/approvals' api_path, 80 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SETTING' menu_code, '업무 설정' menu_name, 'BZA' module_code, '/bza#settings' route_path, '/api/bza/settings' api_path, 120 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DOWNLOAD' menu_code, '다운로드 감사' menu_name, 'BZA' module_code, '/bza#downloads' route_path, '/api/bza/downloads' api_path, 130 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'AUDIT' menu_code, '업무 감사' menu_name, 'BZA' module_code, '/bza#audits' route_path, '/api/bza/backoffice/audits' api_path, 140 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION' menu_code, '업무 알림' menu_name, 'BZA' module_code, '/bza#notifications' route_path, '/api/bza/notifications' api_path, 150 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ATTACHMENT' menu_code, '첨부파일' menu_name, 'BZA' module_code, '/bza#attachments' route_path, '/api/bza/attachments' api_path, 160 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SAVED_SEARCH' menu_code, '저장 검색' menu_name, 'BZA' module_code, '/bza#savedSearches' route_path, '/api/bza/saved-searches' api_path, 170 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name = src.menu_name, tgt.api_path = src.api_path, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, module_code, route_path, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.module_code, src.route_path, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_role tgt USING (
SELECT 'BZA_MANAGER' role_code, '업무 관리자' role_name, 'Y' write_allowed_yn, 'ALL' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name = src.role_name, tgt.write_allowed_yn = src.write_allowed_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_user_role tgt USING (
SELECT admin_user_id admin_user_id, 'BZA_MANAGER' role_code, SYSTIMESTAMP valid_from, NULL valid_to, 'Y' primary_yn, 'CPF_TEST_SEED' grant_reason, 'CPF-TEST-BZA-ROLE-MANAGER-0001' operation_id, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM bza_admin_user
WHERE admin_login_id = 'bza-admin'
) src ON (tgt.operation_id = src.operation_id)
WHEN MATCHED THEN UPDATE SET tgt.valid_to = NULL, tgt.primary_yn = 'Y', tgt.grant_reason = src.grant_reason, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (admin_user_id, role_code, valid_from, valid_to, primary_yn, grant_reason, operation_id, created_by, updated_by) VALUES (src.admin_user_id, src.role_code, src.valid_from, src.valid_to, src.primary_yn, src.grant_reason, src.operation_id, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_MANAGER' role_code, 'DASHBOARD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/dashboard' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'USER' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/admin-users/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'USER' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/admin-users' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ORGANIZATION' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/organizations/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ORGANIZATION' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/organizations' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'EMPLOYEE' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/employees/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'EMPLOYEE' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/employees' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ROLE' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/roles/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ROLE' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/roles' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'MENU' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/menus/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'MENU' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/menus' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'PERMISSION' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/permissions/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'PERMISSION' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/permissions/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'APPROVAL' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/approvals/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'APPROVAL' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/approvals/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'SETTING' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/settings/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'DOWNLOAD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/downloads/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'AUDIT' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/audits/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'NOTIFICATION' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/notifications/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'NOTIFICATION' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/notifications/**' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ATTACHMENT' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/attachments' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ATTACHMENT' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/attachments' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'ATTACHMENT' menu_code, 'DOWNLOAD' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/attachments/*/download' api_pattern, 'ALL' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'SAVED_SEARCH' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/saved-searches/**' api_pattern, 'OWN' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_MANAGER' role_code, 'SAVED_SEARCH' menu_code, 'WRITE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/saved-searches/**' api_pattern, 'OWN' data_scope, 'Y' allow_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn = src.allow_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, data_scope, allow_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.data_scope, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO bza_project_setting tgt USING (
SELECT 'DOWNLOAD.MASKING.ENABLED' setting_key, 'Y' setting_value, '업무 다운로드 마스킹 사용 여부' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value = src.setting_value, tgt.description = src.description, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_organization tgt USING (
SELECT 'HQ' organization_code, NULL parent_organization_code, '본사' organization_name, 'COMPANY' organization_type, 10 sort_order, SYSTIMESTAMP effective_from, NULL effective_to, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPS' organization_code, 'HQ' parent_organization_code, '업무운영팀' organization_name, 'DEPARTMENT' organization_type, 20 sort_order, SYSTIMESTAMP effective_from, NULL effective_to, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.organization_code = src.organization_code)
WHEN MATCHED THEN UPDATE SET tgt.parent_organization_code = src.parent_organization_code, tgt.organization_name = src.organization_name, tgt.organization_type = src.organization_type, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (organization_code, parent_organization_code, organization_name, organization_type, sort_order, effective_from, effective_to, use_yn, created_by, updated_by) VALUES (src.organization_code, src.parent_organization_code, src.organization_name, src.organization_type, src.sort_order, src.effective_from, src.effective_to, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_position tgt USING (
SELECT 'P3' position_code, '책임' position_name, 30 rank_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.position_code = src.position_code)
WHEN MATCHED THEN UPDATE SET tgt.position_name = src.position_name, tgt.rank_order = src.rank_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (position_code, position_name, rank_order, use_yn, created_by, updated_by) VALUES (src.position_code, src.position_name, src.rank_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_job_title tgt USING (
SELECT 'OPERATOR' job_title_code, '업무담당자' job_title_name, 'N' manager_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.job_title_code = src.job_title_code)
WHEN MATCHED THEN UPDATE SET tgt.job_title_name = src.job_title_name, tgt.manager_yn = src.manager_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (job_title_code, job_title_name, manager_yn, use_yn, created_by, updated_by) VALUES (src.job_title_code, src.job_title_name, src.manager_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_employee tgt USING (
SELECT 'EMP001' employee_no, admin_user_id admin_user_id, 'OPS' organization_code, '업무 담당자' employee_name, 'P3' position_code, 'OPERATOR' job_title_code, 'ACTIVE' employment_status, CURRENT_DATE join_date, 'operator&&example.com' email, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM bza_admin_user WHERE admin_login_id = 'bza-admin'
) src ON (tgt.admin_user_id = src.admin_user_id)
WHEN MATCHED THEN UPDATE SET tgt.admin_user_id = src.admin_user_id, tgt.organization_code = src.organization_code, tgt.employee_name = src.employee_name, tgt.position_code = src.position_code, tgt.job_title_code = src.job_title_code, tgt.employment_status = src.employment_status, tgt.email = src.email, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code, employment_status, join_date, email, use_yn, created_by, updated_by) VALUES (src.employee_no, src.admin_user_id, src.organization_code, src.employee_name, src.position_code, src.job_title_code, src.employment_status, src.join_date, src.email, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_employee_assignment tgt USING (
SELECT 'EMP001' employee_no, 'OPS' organization_code, 'P3' position_code, 'OPERATOR' job_title_code, 'PRIMARY' assignment_type, 'Y' primary_yn, SYSTIMESTAMP effective_from, NULL effective_to, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.employee_no = src.employee_no AND tgt.assignment_type = src.assignment_type AND tgt.primary_yn = src.primary_yn)
WHEN MATCHED THEN UPDATE SET tgt.organization_code = src.organization_code, tgt.position_code = src.position_code, tgt.job_title_code = src.job_title_code, tgt.primary_yn = src.primary_yn, tgt.effective_to = NULL, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (employee_no, organization_code, position_code, job_title_code, assignment_type, primary_yn, effective_from, effective_to, created_by, updated_by) VALUES (src.employee_no, src.organization_code, src.position_code, src.job_title_code, src.assignment_type, src.primary_yn, src.effective_from, src.effective_to, src.created_by, src.updated_by);
INSERT INTO bza_notification (recipient_login_id, notification_type, title, message_body, reference_type, reference_id, read_yn, use_yn, created_by, updated_by) SELECT 'bza-admin', 'APPROVAL', '결재 대기 알림', '기준정보 변경 요청 결재를 확인하세요.',
       'APPROVAL', 'BZA-SAMPLE-001', 'N', 'Y', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM bza_notification
     WHERE recipient_login_id = 'bza-admin'
       AND reference_type = 'APPROVAL'
       AND reference_id = 'BZA-SAMPLE-001'
);
MERGE INTO bza_saved_search tgt USING (
SELECT 'bza-admin' owner_login_id, 'APPROVAL' screen_code, '진행 중 결재' search_name, '{"approvalStatus":"IN_REVIEW"}' criteria_json, 'N' shared_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.owner_login_id = src.owner_login_id AND tgt.screen_code = src.screen_code AND tgt.search_name = src.search_name)
WHEN MATCHED THEN UPDATE SET tgt.criteria_json = src.criteria_json, tgt.shared_yn = src.shared_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (owner_login_id, screen_code, search_name, criteria_json, shared_yn, use_yn, created_by, updated_by) VALUES (src.owner_login_id, src.screen_code, src.search_name, src.criteria_json, src.shared_yn, src.use_yn, src.created_by, src.updated_by);

-- ===== END 70_test_data.sql =====
