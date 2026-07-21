param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
$SqlRoot = Join-Path $Root "specs\sql"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Read-Utf8([string] $Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8).TrimEnd()
}

function Write-Utf8([string] $Path, [string] $Text) {
    [System.IO.File]::WriteAllText($Path, $Text, $Utf8NoBom)
}

function Section([string] $FileName) {
    $path = Join-Path $SqlRoot $FileName
    return @"

-- ============================================================================
-- specs/sql/$FileName
-- ============================================================================
$(Read-Utf8 $path)
"@
}

$cleanup = @"

-- ============================================================================
-- CPF 현행 테이블 정리
-- ============================================================================
-- 로컬 설치와 smoke 검증을 위해 CPF 현행 표준 테이블을 다시 생성합니다.

DROP TABLE IF EXISTS bzaDB.bza_approval_history;
DROP TABLE IF EXISTS bzaDB.bza_approval_line;
DROP TABLE IF EXISTS bzaDB.bza_approval_document;
DROP TABLE IF EXISTS bzaDB.bza_business_audit;
DROP TABLE IF EXISTS bzaDB.bza_user_role;
DROP TABLE IF EXISTS bzaDB.bza_employee;
DROP TABLE IF EXISTS bzaDB.bza_organization;
DROP TABLE IF EXISTS bzaDB.bza_masking_audit;
DROP TABLE IF EXISTS bzaDB.bza_project_setting;
DROP TABLE IF EXISTS bzaDB.bza_order;
DROP TABLE IF EXISTS bzaDB.bza_product;
DROP TABLE IF EXISTS bzaDB.bza_customer;
DROP TABLE IF EXISTS bzaDB.bza_permission;
DROP TABLE IF EXISTS bzaDB.bza_role;
DROP TABLE IF EXISTS bzaDB.bza_menu;
DROP TABLE IF EXISTS bzaDB.bza_refresh_token;
DROP TABLE IF EXISTS bzaDB.bza_login_history;
DROP TABLE IF EXISTS bzaDB.bza_admin_user;

DROP TABLE IF EXISTS mbrDB.mbr_refresh_token;
DROP TABLE IF EXISTS mbrDB.mbr_member_login_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role;
DROP TABLE IF EXISTS mbrDB.mbr_member;

DROP TABLE IF EXISTS accDB.acc_account_change_log;
DROP TABLE IF EXISTS accDB.acc_account;

DROP TABLE IF EXISTS xyzDB.xyz_center_cut_sample_result;
DROP TABLE IF EXISTS xyzDB.xyz_center_cut_sample_target;

DROP TABLE IF EXISTS admDB.adm_download_audit_log;
DROP TABLE IF EXISTS admDB.adm_notification_delivery_log;
DROP TABLE IF EXISTS admDB.adm_notification_rule;
DROP TABLE IF EXISTS admDB.adm_operator_session;
DROP TABLE IF EXISTS admDB.adm_login_history;
DROP TABLE IF EXISTS admDB.adm_password_history;
DROP TABLE IF EXISTS admDB.adm_password_policy;
DROP TABLE IF EXISTS admDB.adm_mfa_otp_secret;
DROP TABLE IF EXISTS admDB.adm_ip_allowlist;
DROP TABLE IF EXISTS admDB.adm_audit_log;
DROP TABLE IF EXISTS admDB.adm_role_api_permission;
DROP TABLE IF EXISTS admDB.adm_api_permission;
DROP TABLE IF EXISTS admDB.adm_role_button;
DROP TABLE IF EXISTS admDB.adm_role_menu;
DROP TABLE IF EXISTS admDB.adm_button;
DROP TABLE IF EXISTS admDB.adm_menu;
DROP TABLE IF EXISTS admDB.adm_operator_role;
DROP TABLE IF EXISTS admDB.adm_role;
DROP TABLE IF EXISTS admDB.adm_operator;

DROP TABLE IF EXISTS cmnDB.cmn_edu_query_item;
DROP TABLE IF EXISTS cmnDB.cmn_fixed_length_masking_policy;
DROP TABLE IF EXISTS cmnDB.cmn_fixed_length_field;
DROP TABLE IF EXISTS cmnDB.cmn_fixed_length_group;
DROP TABLE IF EXISTS cmnDB.cmn_fixed_length_layout;
DROP TABLE IF EXISTS cmnDB.cmn_business_log;
DROP TABLE IF EXISTS cmnDB.cmn_notification_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence_issue_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence;

DROP TABLE IF EXISTS pfwDB.pfw_notification_delivery_log;
DROP TABLE IF EXISTS pfwDB.pfw_notification_rule;
DROP TABLE IF EXISTS pfwDB.pfw_business_day_calendar;
DROP TABLE IF EXISTS pfwDB.bat_center_cut_result;
DROP TABLE IF EXISTS pfwDB.bat_center_cut_item;
DROP TABLE IF EXISTS pfwDB.bat_center_cut_parameter;
DROP TABLE IF EXISTS pfwDB.bat_center_cut_job;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_result;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_item;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_parameter;
DROP TABLE IF EXISTS pfwDB.pfw_center_cut_job;
DROP TABLE IF EXISTS pfwDB.pfw_batch_ghost_event;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution_target;
DROP TABLE IF EXISTS pfwDB.pfw_batch_operation_log;
DROP TABLE IF EXISTS pfwDB.pfw_batch_lock;
DROP TABLE IF EXISTS pfwDB.pfw_batch_step_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution_lease;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_job_relation;
DROP TABLE IF EXISTS pfwDB.pfw_batch_worker;
DROP TABLE IF EXISTS pfwDB.pfw_batch_instance;
DROP TABLE IF EXISTS pfwDB.pfw_batch_schedule;
DROP TABLE IF EXISTS pfwDB.pfw_batch_job;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_INSTANCE;
DROP TABLE IF EXISTS pfwDB.BATCH_STEP_EXECUTION_SEQ;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_EXECUTION_SEQ;
DROP TABLE IF EXISTS pfwDB.BATCH_JOB_SEQ;
DROP TABLE IF EXISTS pfwDB.pfw_cache_refresh_event;
DROP TABLE IF EXISTS pfwDB.pfw_dynamic_log_level_rule;
DROP TABLE IF EXISTS pfwDB.pfw_config;
DROP TABLE IF EXISTS pfwDB.pfw_response_code;
DROP TABLE IF EXISTS pfwDB.pfw_message;
DROP TABLE IF EXISTS pfwDB.pfw_code;
DROP TABLE IF EXISTS pfwDB.pfw_unknown_result;
DROP TABLE IF EXISTS pfwDB.pfw_file_transfer_history;
DROP TABLE IF EXISTS pfwDB.pfw_broker_dlq;
DROP TABLE IF EXISTS pfwDB.pfw_broker_inbox;
DROP TABLE IF EXISTS pfwDB.pfw_broker_outbox;
DROP TABLE IF EXISTS pfwDB.pfw_idempotency_record;
DROP TABLE IF EXISTS pfwDB.pfw_service_call_history;
DROP TABLE IF EXISTS pfwDB.pfw_service_circuit_state;
DROP TABLE IF EXISTS pfwDB.pfw_service_routing_policy;
DROP TABLE IF EXISTS pfwDB.pfw_service_health_status;
DROP TABLE IF EXISTS pfwDB.pfw_service_instance;
DROP TABLE IF EXISTS pfwDB.pfw_service_endpoint;
DROP TABLE IF EXISTS pfwDB.pfw_service;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_segment;
DROP TABLE IF EXISTS pfwDB.pfw_batch_on_demand_request;
DROP TABLE IF EXISTS pfwDB.pfw_channel_execution_policy;
DROP TABLE IF EXISTS pfwDB.pfw_channel_registry;
DROP TABLE IF EXISTS pfwDB.pfw_channel_policy_version;
DROP TABLE IF EXISTS pfwDB.pfw_standard_execution_alias;
DROP TABLE IF EXISTS pfwDB.pfw_standard_execution;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_meta;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log_detail;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log;
"@

$installFiles = @(
    "01_create_databases.sql",
    "02_create_service_users.sql",
    "10_pfw_schema.sql",
    "20_cmn_schema.sql",
    "30_adm_schema.sql",
    "35_bat_schema.sql",
    "40_business_modules_schema.sql",
    "50_framework_seed_data.sql",
    "52_standard_execution_alias_seed.sql",
    "55_cmn_seed_data.sql",
    "60_adm_seed_data.sql",
    "70_test_data.sql"
)

$header = @"
-- CPF 전체 설치 SQL입니다.
-- 이 파일은 SOURCE 명령 없이 모든 SQL 본문을 포함합니다.
-- 분리 SQL을 변경한 뒤 scripts/build-all-install-sql.ps1로 다시 생성합니다.
"@

$installBody = $header + (Section "01_create_databases.sql") + $cleanup
foreach ($file in $installFiles | Where-Object { $_ -ne "01_create_databases.sql" }) {
    $installBody += Section $file
}
$installBody = $installBody.TrimEnd() + [Environment]::NewLine

Write-Utf8 (Join-Path $SqlRoot "00_all_install.sql") $installBody
Write-Utf8 (Join-Path $SqlRoot "migration\flyway\V1__cpf_baseline_install.sql") $installBody

$smokeBody = $installBody + (Section "99_smoke_check.sql").TrimEnd() + [Environment]::NewLine
Write-Utf8 (Join-Path $SqlRoot "00_all_install_and_smoke.sql") $smokeBody

Write-Host "CPF all-install SQL files rebuilt."
