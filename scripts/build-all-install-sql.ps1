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
-- CPF table cleanup
-- ============================================================================
-- Recreate the current CPF standard tables for local install and smoke check.

DROP TABLE IF EXISTS exsDB.exs_retry_log;
DROP TABLE IF EXISTS exsDB.exs_control_policy;
DROP TABLE IF EXISTS exsDB.exs_message_log;
DROP TABLE IF EXISTS exsDB.exs_transaction_log;
DROP TABLE IF EXISTS exsDB.exs_route_rule;
DROP TABLE IF EXISTS exsDB.exs_token_event_history;
DROP TABLE IF EXISTS exsDB.exs_token_store;
DROP TABLE IF EXISTS exsDB.exs_auth_profile;
DROP TABLE IF EXISTS exsDB.exs_endpoint;
DROP TABLE IF EXISTS exsDB.exs_channel;
DROP TABLE IF EXISTS exsDB.exs_institution;

DROP TABLE IF EXISTS bizadmDB.bizadm_masking_audit;
DROP TABLE IF EXISTS bizadmDB.bizadm_project_setting;
DROP TABLE IF EXISTS bizadmDB.bizadm_order;
DROP TABLE IF EXISTS bizadmDB.bizadm_product;
DROP TABLE IF EXISTS bizadmDB.bizadm_customer;
DROP TABLE IF EXISTS bizadmDB.bizadm_permission;
DROP TABLE IF EXISTS bizadmDB.bizadm_role;
DROP TABLE IF EXISTS bizadmDB.bizadm_menu;
DROP TABLE IF EXISTS bizadmDB.bizadm_refresh_token;
DROP TABLE IF EXISTS bizadmDB.bizadm_login_history;
DROP TABLE IF EXISTS bizadmDB.bizadm_admin_user;

DROP TABLE IF EXISTS mbrDB.mbr_refresh_token;
DROP TABLE IF EXISTS mbrDB.mbr_member_login_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role_history;
DROP TABLE IF EXISTS mbrDB.mbr_member_role;
DROP TABLE IF EXISTS mbrDB.mbr_member;

DROP TABLE IF EXISTS accDB.acc_account;

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
DROP TABLE IF EXISTS admDB.adm_role_button;
DROP TABLE IF EXISTS admDB.adm_role_menu;
DROP TABLE IF EXISTS admDB.adm_button;
DROP TABLE IF EXISTS admDB.adm_menu;
DROP TABLE IF EXISTS admDB.adm_operator_role;
DROP TABLE IF EXISTS admDB.adm_role;
DROP TABLE IF EXISTS admDB.adm_operator;

DROP TABLE IF EXISTS cmnDB.cmn_business_log;
DROP TABLE IF EXISTS cmnDB.cmn_notification_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence_issue_log;
DROP TABLE IF EXISTS cmnDB.cmn_sequence;

DROP TABLE IF EXISTS pfwDB.pfw_notification_delivery_log;
DROP TABLE IF EXISTS pfwDB.pfw_notification_rule;
DROP TABLE IF EXISTS pfwDB.pfw_business_day_calendar;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution_target;
DROP TABLE IF EXISTS pfwDB.pfw_batch_operation_log;
DROP TABLE IF EXISTS pfwDB.pfw_batch_lock;
DROP TABLE IF EXISTS pfwDB.pfw_batch_step_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_execution;
DROP TABLE IF EXISTS pfwDB.pfw_batch_job_relation;
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
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log_detail;
DROP TABLE IF EXISTS pfwDB.pfw_transaction_log;
"@

$installFiles = @(
    "01_create_databases.sql",
    "02_create_service_users.sql",
    "10_pfw_schema.sql",
    "20_cmn_schema.sql",
    "30_adm_schema.sql",
    "40_business_modules_schema.sql",
    "50_framework_seed_data.sql",
    "55_cmn_seed_data.sql",
    "60_adm_seed_data.sql",
    "70_test_data.sql"
)

$header = @"
-- CPF all install SQL.
-- This file contains the full SQL body and does not use SOURCE commands.
-- Rebuild this file from split SQL files with scripts/build-all-install-sql.ps1.
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
