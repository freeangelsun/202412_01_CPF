-- CPF 초기 데이터베이스 smoke check입니다.
-- 01_create_databases.sql부터 70_test_data.sql까지 실행한 뒤 수행합니다.

SELECT 'pfwDB.pfw_transaction_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log;
SELECT 'pfwDB.pfw_transaction_log_detail' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_log_detail;
SELECT 'pfwDB.pfw_transaction_meta' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_transaction_meta;
SELECT 'pfwDB.pfw_log_policy' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy;
SELECT 'pfwDB.pfw_log_policy_override' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy_override;
SELECT 'pfwDB.pfw_log_policy_audit' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_log_policy_audit;
SELECT 'pfwDB.pfw_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_code;
SELECT 'pfwDB.pfw_message' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_message;
SELECT 'pfwDB.pfw_response_code' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_response_code;
SELECT 'pfwDB.pfw_config' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_config;
SELECT 'pfwDB.pfw_cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_cache_refresh_event;
SELECT 'pfwDB.BATCH_JOB_INSTANCE' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_INSTANCE;
SELECT 'pfwDB.BATCH_JOB_EXECUTION' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION;
SELECT 'pfwDB.BATCH_JOB_EXECUTION_PARAMS' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION_PARAMS;
SELECT 'pfwDB.BATCH_STEP_EXECUTION' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_STEP_EXECUTION;
SELECT 'pfwDB.BATCH_JOB_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_SEQ;
SELECT 'pfwDB.BATCH_JOB_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_JOB_EXECUTION_SEQ;
SELECT 'pfwDB.BATCH_STEP_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM pfwDB.BATCH_STEP_EXECUTION_SEQ;
SELECT 'pfwDB.pfw_batch_job' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_job;
SELECT 'pfwDB.pfw_batch_schedule' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_schedule;
SELECT 'pfwDB.pfw_batch_job_relation' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_job_relation;
SELECT 'pfwDB.pfw_batch_instance' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_instance;
SELECT 'pfwDB.pfw_batch_worker' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_worker;
SELECT 'pfwDB.pfw_batch_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_execution;
SELECT 'pfwDB.pfw_batch_execution_target' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_execution_target;
SELECT 'pfwDB.pfw_batch_step_execution' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_step_execution;
SELECT 'pfwDB.pfw_batch_lock' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_lock;
SELECT 'pfwDB.pfw_batch_operation_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_operation_log;
SELECT 'pfwDB.pfw_batch_ghost_event' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_batch_ghost_event;
SELECT 'pfwDB.pfw_business_day_calendar' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_business_day_calendar;
SELECT 'pfwDB.pfw_notification_rule' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_notification_rule;
SELECT 'pfwDB.pfw_notification_delivery_log' AS check_name, COUNT(*) AS row_count FROM pfwDB.pfw_notification_delivery_log;

SELECT 'cmnDB.cmn_sequence' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence;
SELECT 'cmnDB.cmn_sequence_issue_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence_issue_log;
SELECT 'cmnDB.cmn_notification_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_notification_log;
SELECT 'cmnDB.cmn_business_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_business_log;
SELECT 'cmnDB.cmn_edu_query_item' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_edu_query_item;

SELECT 'admDB.adm_operator' AS check_name, COUNT(*) AS row_count FROM admDB.adm_operator;
SELECT 'admDB.adm_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_menu;
SELECT 'admDB.adm_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_button;
SELECT 'admDB.adm_role' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role;
SELECT 'admDB.adm_role_menu' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_menu;
SELECT 'admDB.adm_role_button' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_button;
SELECT 'admDB.adm_api_permission' AS check_name, COUNT(*) AS row_count FROM admDB.adm_api_permission;
SELECT 'admDB.adm_role_api_permission' AS check_name, COUNT(*) AS row_count FROM admDB.adm_role_api_permission;
SELECT 'admDB.adm_password_policy' AS check_name, COUNT(*) AS row_count FROM admDB.adm_password_policy;
SELECT 'admDB.adm_audit_log' AS check_name, COUNT(*) AS row_count FROM admDB.adm_audit_log;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'mbrDB.mbr_member' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member;
SELECT 'mbrDB.mbr_member_role' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_role;
SELECT 'mbrDB.mbr_member_login_history' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_login_history;
SELECT 'mbrDB.mbr_refresh_token' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_refresh_token;

SELECT 'bizadmDB.bizadm_admin_user' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_admin_user;
SELECT 'bizadmDB.bizadm_login_history' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_login_history;
SELECT 'bizadmDB.bizadm_refresh_token' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_refresh_token;
SELECT 'bizadmDB.bizadm_menu' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_menu;
SELECT 'bizadmDB.bizadm_role' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_role;
SELECT 'bizadmDB.bizadm_permission' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_permission;
SELECT 'bizadmDB.bizadm_customer' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_customer;
SELECT 'bizadmDB.bizadm_product' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_product;
SELECT 'bizadmDB.bizadm_order' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_order;
SELECT 'bizadmDB.bizadm_project_setting' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_project_setting;
SELECT 'bizadmDB.bizadm_masking_audit' AS check_name, COUNT(*) AS row_count FROM bizadmDB.bizadm_masking_audit;

SELECT 'exsDB.exs_institution' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_institution;
SELECT 'exsDB.exs_channel' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_channel;
SELECT 'exsDB.exs_endpoint' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_endpoint;
SELECT 'exsDB.exs_auth_profile' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_auth_profile;
SELECT 'exsDB.exs_token_store' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_token_store;
SELECT 'exsDB.exs_token_event_history' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_token_event_history;
SELECT 'exsDB.exs_route_rule' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_route_rule;
SELECT 'exsDB.exs_transaction_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_transaction_log;
SELECT 'exsDB.exs_message_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_message_log;
SELECT 'exsDB.exs_control_policy' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_control_policy;
SELECT 'exsDB.exs_retry_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_retry_log;

SELECT TRANSACTION_ID, LOG_DATE, DATE(START_TIME) AS start_date, MODULE_ID, WAS_ID, SERVER_INSTANCE_ID,
       API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CORRELATION_ID, IDEMPOTENCY_KEY
FROM pfwDB.pfw_transaction_log
WHERE TRANSACTION_ID = '20260615120000000MBRlocal010000001'
ORDER BY LOG_IDX
LIMIT 1;

SELECT COUNT(*) AS transaction_log_date_mismatch_count
FROM pfwDB.pfw_transaction_log
WHERE START_TIME IS NOT NULL
  AND LOG_DATE <> DATE(START_TIME);

SELECT DETAIL_KEY, DETAIL_VALUE
FROM pfwDB.pfw_transaction_log_detail
WHERE DETAIL_KEY IN ('headers', 'fixedTelegram')
ORDER BY DETAIL_KEY;

SELECT sequence_key, business_area, business_key, sequence_kind, channel_code, start_value, increment_by, reset_cycle, log_enabled_yn
FROM cmnDB.cmn_sequence
WHERE sequence_key = 'CMN_EDU_ORDER';

SELECT item_id, item_name, category_code, status_code, owner_member_no
FROM cmnDB.cmn_edu_query_item
WHERE use_yn = 'Y'
ORDER BY item_id
LIMIT 5;

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
WHERE BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_ROLE_GRANT', 'BATCH_EXECUTE', 'BATCH_CALENDAR_SAVE', 'BATCH_SIMULATION', 'BATCH_TARGET_READ')
ORDER BY ROLE_ID, BUTTON_ID;

SELECT ROLE_ID, API_PERMISSION_ID, ALLOW_YN
FROM admDB.adm_role_api_permission
WHERE API_PERMISSION_ID IN ('API_PERMISSION_READ', 'API_PERMISSION_WRITE_PUT', 'API_OPERATOR_READ')
ORDER BY ROLE_ID, API_PERMISSION_ID;

SELECT schedule_id, job_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern
FROM pfwDB.pfw_batch_schedule
ORDER BY schedule_id;

SELECT job_id, related_job_id, relation_type, trigger_condition, required_status
FROM pfwDB.pfw_batch_job_relation
ORDER BY job_id, related_job_id;

SELECT job_id, schedule_id, target_instance_id, business_date, dispatch_status
FROM pfwDB.pfw_batch_execution_target
ORDER BY target_id
LIMIT 5;

SELECT worker_id, server_instance_id, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id
FROM pfwDB.pfw_batch_worker
ORDER BY worker_id
LIMIT 5;

SELECT execution_id, job_id, execution_status, spring_batch_execution_id, batch_instance_id, server_instance_id,
       worker_id, transaction_global_id, requested_by
FROM pfwDB.pfw_batch_execution
ORDER BY execution_id DESC
LIMIT 5;

SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status
FROM pfwDB.pfw_batch_step_execution
ORDER BY step_execution_id DESC
LIMIT 5;

SELECT ghost_event_id, execution_id, job_id, worker_id, ghost_status, action_type, lock_released_yn, retryable_yn
FROM pfwDB.pfw_batch_ghost_event
ORDER BY ghost_event_id DESC
LIMIT 5;

SELECT member_no, customer_no, login_id, name, member_status, lock_yn, withdraw_yn
FROM mbrDB.mbr_member
ORDER BY id
LIMIT 5;

SELECT login_domain, member_no, customer_no, login_id, login_result, transaction_global_id, module_id, was_id, server_instance_id
FROM mbrDB.mbr_member_login_history
ORDER BY login_history_id DESC
LIMIT 5;

SELECT admin_login_id, role_code, use_yn, lock_yn, login_fail_count
FROM bizadmDB.bizadm_admin_user
ORDER BY admin_user_id
LIMIT 5;

SELECT login_domain, admin_login_id, login_result, transaction_global_id, module_id, was_id, server_instance_id
FROM bizadmDB.bizadm_login_history
ORDER BY login_history_id DESC
LIMIT 5;

SELECT member_id, service_code, role_code, role_name, use_yn
FROM mbrDB.mbr_member_role
ORDER BY member_role_id
LIMIT 5;

SELECT auth_profile_code, token_key, token_status, masked_token, transaction_global_id, server_instance_id
FROM exsDB.exs_token_store
ORDER BY token_id
LIMIT 5;

SELECT auth_profile_code, token_key, event_type, transaction_global_id, server_instance_id
FROM exsDB.exs_token_event_history
ORDER BY token_event_id DESC
LIMIT 5;

SELECT response_code, message_code, result_type, http_status
FROM pfwDB.pfw_response_code
WHERE response_code IN ('SPFW000000', 'EPFW010004', 'SACC000000', 'EACC010001', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM pfwDB.pfw_message
WHERE message_code IN ('MCMN000001', 'MPFW010004', 'MACC010001', 'MMBR010102', 'MXYZ090001')
ORDER BY message_code, locale;
