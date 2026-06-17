-- CPF 초기 데이터베이스 smoke check입니다.
-- 01_create_databases.sql부터 70_test_data.sql까지 실행한 뒤 수행합니다.

SELECT 'pfwDB.pfw_transaction_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log;
SELECT 'pfwDB.pfw_transaction_log_detail' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log_detail;
SELECT 'pfwDB.pfw_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_code;
SELECT 'pfwDB.pfw_message' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_message;
SELECT 'pfwDB.pfw_response_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_response_code;
SELECT 'pfwDB.pfw_config' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_config;
SELECT 'pfwDB.pfw_cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_cache_refresh_event;
SELECT 'pfwDB.pfw_batch_job' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_job;
SELECT 'pfwDB.pfw_batch_schedule' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_schedule;
SELECT 'pfwDB.pfw_batch_instance' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_instance;
SELECT 'pfwDB.pfw_batch_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_execution;
SELECT 'pfwDB.pfw_batch_step_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_step_execution;
SELECT 'pfwDB.pfw_business_day_calendar' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_business_day_calendar;

SELECT 'cmnDB.cmn_sequence' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence;
SELECT 'cmnDB.cmn_sequence_issue_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence_issue_log;
SELECT 'cmnDB.cmn_notification_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_notification_log;
SELECT 'cmnDB.cmn_business_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_business_log;

SELECT 'admDB.adm_operator' AS check_name, COUNT(*) AS row_count FROM admDB.adm_operator;
SELECT 'admDB.adm_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_menu;
SELECT 'admDB.adm_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_button;
SELECT 'admDB.adm_role' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role;
SELECT 'admDB.adm_role_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_menu;
SELECT 'admDB.adm_role_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_button;
SELECT 'admDB.adm_password_policy' AS check_name, COUNT(*) AS row_count FROM admDB.adm_password_policy;
SELECT 'admDB.adm_audit_log' AS check_name, COUNT(*) AS row_count FROM admDB.adm_audit_log;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'mbrDB.mbr_member' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member;
SELECT 'mbrDB.mbr_member_role' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_role;
SELECT 'mbrDB.mbr_member_login_history' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_login_history;

SELECT TRANSACTION_ID, API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CORRELATION_ID, IDEMPOTENCY_KEY
FROM pfwDB.pfw_transaction_log
WHERE TRANSACTION_ID = '20260615120000000MBRlocal010000001'
ORDER BY LOG_IDX
LIMIT 1;

SELECT DETAIL_KEY, DETAIL_VALUE
FROM pfwDB.pfw_transaction_log_detail
WHERE DETAIL_KEY IN ('headers', 'fixedTelegram')
ORDER BY DETAIL_KEY;

SELECT sequence_key, business_area, business_key, sequence_kind, channel_code, start_value, increment_by, reset_cycle, log_enabled_yn
FROM cmnDB.cmn_sequence
WHERE sequence_key = 'CMN_EDU_ORDER';

SELECT AUDIT_ID, OPERATOR_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, IMMUTABLE_YN
FROM admDB.adm_audit_log
ORDER BY AUDIT_ID DESC
LIMIT 5;

SELECT ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN
FROM admDB.adm_role_menu
WHERE MENU_ID IN ('MEMBER', 'BATCH', 'PERMISSION')
ORDER BY ROLE_ID, MENU_ID;

SELECT ROLE_ID, BUTTON_ID, ALLOW_YN
FROM admDB.adm_role_button
WHERE BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_ROLE_GRANT', 'BATCH_EXECUTE', 'BATCH_CALENDAR_SAVE')
ORDER BY ROLE_ID, BUTTON_ID;

SELECT member_no, customer_no, login_id, name, member_status, lock_yn, withdraw_yn
FROM mbrDB.mbr_member
ORDER BY id
LIMIT 5;

SELECT member_id, service_code, role_code, role_name, use_yn
FROM mbrDB.mbr_member_role
ORDER BY member_role_id
LIMIT 5;

SELECT response_code, message_code, result_type, http_status
FROM pfwDB.pfw_response_code
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM pfwDB.pfw_message
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;
