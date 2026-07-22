-- CPF 초기 데이터베이스 smoke check입니다.
-- 01_create_databases.sql부터 70_test_data.sql까지 실행한 뒤 수행합니다.

SELECT 'cpfDB.cpf_transaction_log' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_transaction_log;
SELECT 'cpfDB.cpf_transaction_log_detail' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_transaction_log_detail;
SELECT 'cpfDB.cpf_transaction_segment' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_transaction_segment;
SELECT 'cpfDB.cpf_transaction_segment.timeline_columns' AS check_name, COUNT(*) AS column_count
FROM information_schema.columns
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_transaction_segment'
  AND column_name IN (
      'selected_instance_id', 'attempt_no', 'retry_yn', 'failover_yn',
      'circuit_state', 'downstream_http_status', 'result_state', 'unknown_result_id'
  );
SELECT 'cpfDB.cpf_transaction_segment.timeline_indexes' AS check_name, COUNT(DISTINCT index_name) AS index_count
FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_transaction_segment'
  AND index_name IN (
      'ix_cpf_transaction_segment_instance',
      'ix_cpf_transaction_segment_attempt',
      'ix_cpf_transaction_segment_unknown'
  );
SELECT 'cpfDB.cpf_transaction_meta' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_transaction_meta;
SELECT 'cpfDB.cpf_standard_execution' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_standard_execution;
SELECT 'cpfDB.cpf_standard_execution_alias' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_standard_execution_alias;
SELECT 'cpfDB.cpf_channel_policy_version' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_channel_policy_version;
SELECT 'cpfDB.cpf_channel_registry' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_channel_registry;
SELECT 'cpfDB.cpf_channel_execution_policy' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_channel_execution_policy;
SELECT 'cpfDB.cpf_batch_on_demand_request' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_on_demand_request;
SELECT 'cpfDB.cpf_log_policy' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_log_policy;
SELECT 'cpfDB.cpf_log_policy_override' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_log_policy_override;
SELECT 'cpfDB.cpf_log_policy_audit' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_log_policy_audit;
SELECT 'cpfDB.cpf_code' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_code;
SELECT 'cpfDB.cpf_message' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_message;
SELECT 'cpfDB.cpf_response_code' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_response_code;
SELECT 'cpfDB.cpf_config' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_config;
SELECT 'cpfDB.cpf_cache_refresh_event' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_cache_refresh_event;
SELECT 'cpfDB.BATCH_JOB_INSTANCE' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_JOB_INSTANCE;
SELECT 'cpfDB.BATCH_JOB_EXECUTION' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_JOB_EXECUTION;
SELECT 'cpfDB.BATCH_JOB_EXECUTION_PARAMS' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_JOB_EXECUTION_PARAMS;
SELECT 'cpfDB.BATCH_STEP_EXECUTION' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_STEP_EXECUTION;
SELECT 'cpfDB.BATCH_JOB_SEQ' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_JOB_SEQ;
SELECT 'cpfDB.BATCH_JOB_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_JOB_EXECUTION_SEQ;
SELECT 'cpfDB.BATCH_STEP_EXECUTION_SEQ' AS check_name, COUNT(*) AS row_count FROM cpfDB.BATCH_STEP_EXECUTION_SEQ;
SELECT 'cpfDB.cpf_batch_job' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_job;
SELECT 'cpfDB.cpf_batch_schedule' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_schedule;
SELECT 'cpfDB.cpf_batch_job_relation' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_job_relation;
SELECT 'cpfDB.cpf_batch_instance' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_instance;
SELECT 'cpfDB.cpf_batch_worker' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_worker;
SELECT 'cpfDB.cpf_batch_execution' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_execution;
SELECT 'cpfDB.cpf_batch_execution_target' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_execution_target;
SELECT 'cpfDB.cpf_batch_step_execution' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_step_execution;
SELECT 'cpfDB.cpf_batch_lock' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_lock;
SELECT 'cpfDB.cpf_batch_operation_log' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_operation_log;
SELECT 'cpfDB.cpf_batch_ghost_event' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_batch_ghost_event;
SELECT 'cpfDB.bat_center_cut_job' AS check_name, COUNT(*) AS row_count FROM cpfDB.bat_center_cut_job;
SELECT 'cpfDB.bat_center_cut_parameter' AS check_name, COUNT(*) AS row_count FROM cpfDB.bat_center_cut_parameter;
SELECT 'cpfDB.bat_center_cut_item' AS check_name, COUNT(*) AS row_count FROM cpfDB.bat_center_cut_item;
SELECT 'cpfDB.bat_center_cut_result' AS check_name, COUNT(*) AS row_count FROM cpfDB.bat_center_cut_result;
SELECT 'cpfDB.cpf_business_day_calendar' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_business_day_calendar;
SELECT 'cpfDB.cpf_notification_rule' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_notification_rule;
SELECT 'cpfDB.cpf_notification_delivery_log' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_notification_delivery_log;
SELECT 'cpfDB.cpf_broker_outbox.reliability_columns' AS check_name, COUNT(*) AS column_count
FROM information_schema.columns
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_broker_outbox'
  AND column_name IN ('attempt_count', 'max_attempts', 'next_attempt_at', 'lease_until');
SELECT 'cpfDB.cpf_broker_outbox.reliability_indexes' AS check_name, COUNT(DISTINCT index_name) AS index_count
FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_broker_outbox'
  AND index_name IN ('ix_cpf_broker_outbox_ready', 'ix_cpf_broker_outbox_lease');

SELECT 'cmnDB.cmn_sequence' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence;
SELECT 'cmnDB.cmn_sequence_issue_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_sequence_issue_log;
SELECT 'cmnDB.cmn_notification_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_notification_log;
SELECT 'cmnDB.cmn_business_log' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_business_log;
SELECT 'cmnDB.cmn_edu_query_item' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_edu_query_item;
SELECT 'cmnDB.cmn_fixed_length_layout' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_fixed_length_layout;
SELECT 'cmnDB.cmn_fixed_length_group' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_fixed_length_group;
SELECT 'cmnDB.cmn_fixed_length_field' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_fixed_length_field;
SELECT 'cmnDB.cmn_fixed_length_masking_policy' AS check_name, COUNT(*) AS row_count FROM cmnDB.cmn_fixed_length_masking_policy;

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

SELECT 'refDB.ref_center_cut_sample_target' AS check_name, COUNT(*) AS row_count FROM refDB.ref_center_cut_sample_target;
SELECT 'refDB.ref_center_cut_sample_result' AS check_name, COUNT(*) AS row_count FROM refDB.ref_center_cut_sample_result;
SELECT 'exsDB.exs_institution' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_institution;
SELECT 'exsDB.exs_endpoint' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_endpoint;
SELECT 'exsDB.exs_control_policy' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_control_policy;
SELECT 'exsDB.exs_execution' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_execution;
SELECT 'exsDB.exs_reconciliation_log' AS check_name, COUNT(*) AS row_count FROM exsDB.exs_reconciliation_log;
SELECT 'mbrDB.mbr_member' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member;
SELECT 'mbrDB.mbr_member_role' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_role;
SELECT 'mbrDB.mbr_member_login_history' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_member_login_history;
SELECT 'mbrDB.mbr_refresh_token' AS check_name, COUNT(*) AS row_count FROM mbrDB.mbr_refresh_token;

SELECT 'accDB.acc_account' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account;
SELECT 'accDB.acc_account_change_log' AS check_name, COUNT(*) AS row_count FROM accDB.acc_account_change_log;

SELECT 'bzaDB.bza_admin_user' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_admin_user;
SELECT 'bzaDB.bza_login_history' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_login_history;
SELECT 'bzaDB.bza_refresh_token' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_refresh_token;
SELECT 'bzaDB.bza_menu' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_menu;
SELECT 'bzaDB.bza_role' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_role;
SELECT 'bzaDB.bza_permission' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_permission;
SELECT 'bzaDB.bza_customer' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_customer;
SELECT 'bzaDB.bza_product' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_product;
SELECT 'bzaDB.bza_order' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_order;
SELECT 'bzaDB.bza_project_setting' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_project_setting;
SELECT 'bzaDB.bza_masking_audit' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_masking_audit;
SELECT 'bzaDB.bza_organization' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_organization;
SELECT 'bzaDB.bza_employee' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_employee;
SELECT 'bzaDB.bza_user_role' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_user_role;
SELECT 'bzaDB.bza_business_audit' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_business_audit;
SELECT 'bzaDB.bza_notification' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_notification;
SELECT 'bzaDB.bza_attachment' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_attachment;
SELECT 'bzaDB.bza_saved_search' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_saved_search;
SELECT 'bzaDB.bza_download_audit' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_download_audit;
SELECT 'bzaDB.bza_approval_document' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_approval_document;
SELECT 'bzaDB.bza_approval_line' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_approval_line;
SELECT 'bzaDB.bza_approval_history' AS check_name, COUNT(*) AS row_count FROM bzaDB.bza_approval_history;

SELECT TRANSACTION_ID, LOG_DATE, DATE(START_TIME) AS start_date, MODULE_ID, WAS_ID, SERVER_INSTANCE_ID,
       API_VERSION, CLIENT_APP_ID, CLIENT_VERSION, CALLER_SERVICE, CORRELATION_ID, IDEMPOTENCY_KEY
FROM cpfDB.cpf_transaction_log
WHERE TRANSACTION_ID = '20260615120000000MBRlocal010000001'
ORDER BY LOG_IDX
LIMIT 1;

SELECT COUNT(*) AS transaction_log_date_mismatch_count
FROM cpfDB.cpf_transaction_log
WHERE START_TIME IS NOT NULL
  AND LOG_DATE <> DATE(START_TIME);

SELECT DETAIL_KEY, DETAIL_VALUE
FROM cpfDB.cpf_transaction_log_detail
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

SELECT layout_id, institution_code, message_code, direction, version, enabled_yn
FROM cmnDB.cmn_fixed_length_layout
WHERE layout_id = 'BANK01_BALANCE_REQ_V1';

SELECT layout_id, group_id, field_name, display_name, start_position, field_length, field_type, masking_type
FROM cmnDB.cmn_fixed_length_field
WHERE layout_id = 'BANK01_BALANCE_REQ_V1'
ORDER BY start_position;

SELECT target_id, center_cut_job_id, business_key, status_code, parent_transaction_global_id, child_transaction_global_id
FROM refDB.ref_center_cut_sample_target
WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB'
ORDER BY target_id;

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
FROM cpfDB.cpf_batch_schedule
ORDER BY schedule_id;

SELECT job_id, related_job_id, relation_type, trigger_condition, required_status
FROM cpfDB.cpf_batch_job_relation
ORDER BY job_id, related_job_id;

SELECT job_id, schedule_id, target_instance_id, business_date, dispatch_status
FROM cpfDB.cpf_batch_execution_target
ORDER BY target_id
LIMIT 5;

SELECT worker_id, server_instance_id, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id
FROM cpfDB.cpf_batch_worker
ORDER BY worker_id
LIMIT 5;

SELECT execution_id, job_id, execution_status, spring_batch_execution_id, batch_instance_id, server_instance_id,
       worker_id, transaction_global_id, requested_by
FROM cpfDB.cpf_batch_execution
ORDER BY execution_id DESC
LIMIT 5;

SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status
FROM cpfDB.cpf_batch_step_execution
ORDER BY step_execution_id DESC
LIMIT 5;

SELECT ghost_event_id, execution_id, job_id, worker_id, ghost_status, action_type, lock_released_yn, retryable_yn
FROM cpfDB.cpf_batch_ghost_event
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
FROM bzaDB.bza_admin_user
ORDER BY admin_user_id
LIMIT 5;

SELECT login_domain, admin_login_id, login_result, transaction_global_id, module_id, was_id, server_instance_id
FROM bzaDB.bza_login_history
ORDER BY login_history_id DESC
LIMIT 5;

SELECT organization_code, parent_organization_code, organization_name, organization_type, use_yn
FROM bzaDB.bza_organization
ORDER BY sort_order, organization_code;

SELECT employee_no, organization_code, employee_name, position_code, employment_status, use_yn
FROM bzaDB.bza_employee
ORDER BY employee_no;

SELECT recipient_login_id, notification_type, title, read_yn, reference_type, reference_id
FROM bzaDB.bza_notification
ORDER BY notification_id DESC
LIMIT 5;

SELECT owner_login_id, screen_code, search_name, shared_yn, use_yn
FROM bzaDB.bza_saved_search
ORDER BY saved_search_id DESC
LIMIT 5;

SELECT actor_id, download_code, result_status, file_name, masking_applied_yn, transaction_global_id
FROM bzaDB.bza_download_audit
ORDER BY download_audit_id DESC
LIMIT 5;

SELECT member_id, service_code, role_code, role_name, use_yn
FROM mbrDB.mbr_member_role
ORDER BY member_role_id
LIMIT 5;

SELECT response_code, message_code, result_type, http_status
FROM cpfDB.cpf_response_code
WHERE response_code IN ('SCPF000000', 'ECPF010004', 'SMBR000000', 'EMBR010002')
ORDER BY response_code;

SELECT message_code, locale, message_format_type, external_message, internal_message
FROM cpfDB.cpf_message
WHERE message_code IN ('MCMN000001', 'MCPF010004', 'MMBR010102', 'MREF090001')
ORDER BY message_code, locale;

-- cpf-core 공식 시스템 코드와 구형 CPF 코드의 활성 상태를 확인합니다.
SELECT code_key, code_value, description, use_yn
FROM cpfDB.cpf_code
WHERE code_key = 'MODULE' AND code_value IN ('CPF', 'CPF')
ORDER BY code_value;

SELECT module_id, use_yn, COUNT(*) AS response_code_count
FROM cpfDB.cpf_response_code
WHERE module_id IN ('CPF', 'CPF')
GROUP BY module_id, use_yn
ORDER BY module_id, use_yn;

SELECT 'cpfDB.cpf_service' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service;
SELECT 'cpfDB.cpf_service_endpoint' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_endpoint;
SELECT 'cpfDB.cpf_service_instance' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_instance;
SELECT 'cpfDB.cpf_service_health_status' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_health_status;
SELECT 'cpfDB.cpf_service_routing_policy' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_routing_policy;
SELECT 'cpfDB.cpf_service_circuit_state' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_circuit_state;
SELECT 'cpfDB.cpf_service_call_history' AS check_name, COUNT(*) AS row_count FROM cpfDB.cpf_service_call_history;

SELECT service_id, service_name, service_type, owner_module_code, use_yn
FROM cpfDB.cpf_service
WHERE service_id IN ('MBR', 'REF', 'BAT', 'ADM', 'BZA', 'ACC')
ORDER BY service_id;

SELECT endpoint_code, service_id, base_url, default_timeout_ms, default_retry_count, use_yn
FROM cpfDB.cpf_service_endpoint
WHERE endpoint_code IN ('MBR_API', 'REF_API', 'REF-EXTERNAL-SIMULATOR', 'BAT_API', 'ADM_API', 'BZA_API', 'ACC_API')
ORDER BY endpoint_code;

SELECT instance_id, service_id, endpoint_code, instance_status, active_yn
FROM cpfDB.cpf_service_instance
WHERE instance_id IN ('MBR-local-01', 'REF-local-01', 'REF-EXS-local-01', 'BAT-local-01', 'ADM-local-01', 'BZA-local-01', 'ACC-local-01')
ORDER BY instance_id;

SELECT institution_code, institution_name, enabled_yn
FROM exsDB.exs_institution
ORDER BY institution_code;

SELECT endpoint_code, institution_code, service_id, endpoint_uri, result_query_uri, timeout_ms, retry_count, enabled_yn
FROM exsDB.exs_endpoint
ORDER BY endpoint_code;

SELECT institution_code, control_type, enabled_yn, reason
FROM exsDB.exs_control_policy
ORDER BY institution_code, control_type;
