param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $RootUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $RootPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $MigrationPassword = $env:CPF_DB_MIGRATION_PASSWORD,
    [string] $AppPassword = $env:CPF_DB_APP_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $RequireRun
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($HostName)) {
    $HostName = "localhost"
}
if ([string]::IsNullOrWhiteSpace($Port)) {
    $Port = "3306"
}
if ([string]::IsNullOrWhiteSpace($RootUsername)) {
    $RootUsername = "root"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/sql-smoke"
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "mariadb-full-install-result.sanitized.json"
$logPath = Join-Path $ResultDir "mariadb-full-install.sanitized.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusNotVerified
    mode = "MariaDB CLI"
    host = $HostName
    port = $Port
    username = $RootUsername
    passwordProvided = -not [string]::IsNullOrWhiteSpace($RootPassword)
    servicePasswordsProvided = (-not [string]::IsNullOrWhiteSpace($MigrationPassword)) -and (-not [string]::IsNullOrWhiteSpace($AppPassword))
    client = $null
    sqlFiles = @()
    steps = [ordered]@{}
    checks = [ordered]@{}
    logPath = $logPath
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $json = $result | ConvertTo-Json -Depth 20
    [System.IO.File]::WriteAllText($resultPath, $json, [System.Text.UTF8Encoding]::new($false))
}

function Add-Log {
    param([string] $Message)

    $line = "[$((Get-Date).ToString("o"))] $Message"
    Add-Content -LiteralPath $logPath -Encoding UTF8 -Value $line
}

function ConvertTo-SafeMessage {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) {
        return $Message
    }
    if (-not [string]::IsNullOrWhiteSpace($RootPassword)) {
        $Message = $Message.Replace($RootPassword, "****")
    }
    if (-not [string]::IsNullOrWhiteSpace($MigrationPassword)) {
        $Message = $Message.Replace($MigrationPassword, "****")
    }
    if (-not [string]::IsNullOrWhiteSpace($AppPassword)) {
        $Message = $Message.Replace($AppPassword, "****")
    }
    return $Message
}

function ConvertTo-MariaDbStringLiteral {
    param([string] $Value)

    return "'" + $Value.Replace("'", "''") + "'"
}

function Join-ProcessArguments {
    param([string[]] $Arguments)

    return ($Arguments | ForEach-Object {
        if ($_ -match '[\s"]') {
            '"' + ($_.Replace('\', '\\').Replace('"', '\"')) + '"'
        } else {
            $_
        }
    }) -join " "
}

function Find-MariaDbClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $command = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    foreach ($name in @("mariadb", "mysql")) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    $installRoots = @("C:\Program Files", "C:\Program Files (x86)")
    foreach ($installRoot in $installRoots) {
        if (-not (Test-Path -LiteralPath $installRoot)) {
            continue
        }
        foreach ($productFilter in @("MariaDB*", "MySQL*")) {
            $productDirs = @(Get-ChildItem -LiteralPath $installRoot -Directory -Filter $productFilter -ErrorAction SilentlyContinue | Sort-Object FullName)
            foreach ($productDir in $productDirs) {
                foreach ($clientName in @("mariadb.exe", "mysql.exe")) {
                    $candidate = Join-Path $productDir.FullName "bin\$clientName"
                    if (Test-Path -LiteralPath $candidate) {
                        return (Resolve-Path -LiteralPath $candidate).Path
                    }
                }
            }
        }
    }

    return $null
}

function Invoke-MariaDbText {
    param(
        [string] $StepName,
        [string] $SqlText
    )

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $result.client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    try {
        $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    } catch {
        # Windows PowerShell 5.1에는 StandardInputEncoding이 없으므로 SOURCE 파일 실행으로 우회합니다.
    }
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    $arguments = @(
        "--protocol=tcp",
        "-h",
        $HostName,
        "-P",
        $Port,
        "-u",
        $RootUsername,
        "--ssl=0",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names"
    )
    if (-not [string]::IsNullOrWhiteSpace($RootPassword)) {
        # 비밀번호는 프로세스 목록과 command line 증적에 노출하지 않고 자식 프로세스 환경으로만 전달합니다.
        $psi.EnvironmentVariables["MYSQL_PWD"] = $RootPassword
        $psi.EnvironmentVariables["MARIADB_PWD"] = $RootPassword
    }
    $psi.Arguments = Join-ProcessArguments $arguments

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void] $process.Start()
    $inputError = $null
    try {
        $process.StandardInput.Write($SqlText)
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try {
            $process.StandardInput.Close()
        } catch {
            if ([string]::IsNullOrWhiteSpace($inputError)) {
                $inputError = $_.Exception.Message
            }
        }
    }
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0 -or -not [string]::IsNullOrWhiteSpace($inputError)) {
        throw "MariaDB SQL step failed. step=$StepName exitCode=$($process.ExitCode) inputError=$(ConvertTo-SafeMessage $inputError) error=$(ConvertTo-SafeMessage $stderr)"
    }

    if (-not [string]::IsNullOrWhiteSpace($stderr)) {
        Add-Log "$StepName stderr: $(ConvertTo-SafeMessage $stderr)"
    }
    return $stdout
}

function Invoke-MariaDbFile {
    param(
        [string] $StepName,
        [string] $RelativePath,
        [switch] $WithServicePasswords
    )

    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        throw "SQL file not found. step=$StepName path=$RelativePath"
    }

    Add-Log "SQL start: $RelativePath"
    $result.sqlFiles += $RelativePath
    $sourcePath = ((Resolve-Path -LiteralPath $path).Path).Replace("\", "/")
    $prefix = ""
    if ($WithServicePasswords) {
        $prefix = "SET @cpf_migration_password = $(ConvertTo-MariaDbStringLiteral $MigrationPassword);`n" +
                "SET @cpf_app_password = $(ConvertTo-MariaDbStringLiteral $AppPassword);`n"
    }
    $output = Invoke-MariaDbText -StepName $StepName -SqlText ($prefix + "SOURCE $sourcePath;`n")
    $result.steps[$StepName] = [ordered]@{
        status = $StatusDone
        file = $RelativePath
        outputLines = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }).Count
    }
    Add-Log "SQL completed: $RelativePath"
    return $output
}

function Invoke-Scalar {
    param(
        [string] $StepName,
        [string] $SqlText
    )

    $output = Invoke-MariaDbText -StepName $StepName -SqlText $SqlText
    $line = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -First 1)
    if ($line.Count -eq 0) {
        return $null
    }
    return $line[0].Trim()
}

try {
    Set-Content -LiteralPath $logPath -Encoding UTF8 -Value "CPF MariaDB full install smoke"
    $result.client = Find-MariaDbClient
    if ([string]::IsNullOrWhiteSpace($result.client)) {
        $result.status = $StatusNotVerified
        $result.reason = "mysql or mariadb CLI was not found. Configure PATH or CPF_MARIADB_CLI."
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "MariaDB full install smoke skipped: CLI not found. result=$resultPath"
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($RootPassword)) {
        $result.status = $StatusNotVerified
        $result.reason = "CPF_DB_ROOT_PASSWORD or -RootPassword was not provided, so DB connection was not attempted."
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "MariaDB full install smoke skipped: password not provided. result=$resultPath"
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($MigrationPassword) -or [string]::IsNullOrWhiteSpace($AppPassword)) {
        $result.status = $StatusNotVerified
        $result.reason = "CPF_DB_MIGRATION_PASSWORD and CPF_DB_APP_PASSWORD are required for service account provisioning."
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "MariaDB full install smoke skipped: service account passwords not provided. result=$resultPath"
        exit 0
    }

    Invoke-MariaDbFile -StepName "allInstallAndSmoke" -RelativePath "specs/sql/00_all_install_and_smoke.sql" -WithServicePasswords | Out-Null
    Invoke-MariaDbFile -StepName "smokeCheck" -RelativePath "specs/sql/99_smoke_check.sql" | Out-Null
    Invoke-MariaDbFile -StepName "frameworkSeedRepeat" -RelativePath "specs/sql/50_framework_seed_data.sql" | Out-Null
    Invoke-MariaDbFile -StepName "executionAliasSeedRepeat" -RelativePath "specs/sql/52_standard_execution_alias_seed.sql" | Out-Null
    Invoke-MariaDbFile -StepName "cmnSeedRepeat" -RelativePath "specs/sql/55_cmn_seed_data.sql" | Out-Null
    Invoke-MariaDbFile -StepName "admSeedRepeat" -RelativePath "specs/sql/60_adm_seed_data.sql" | Out-Null
    Invoke-MariaDbFile -StepName "testDataRepeat" -RelativePath "specs/sql/70_test_data.sql" | Out-Null

    $result.checks.cpfCoreModuleCodeCount = [int] (Invoke-Scalar -StepName "cpfCoreModuleCodeCount" -SqlText @"
SELECT COUNT(*)
FROM cpfDB.cpf_code
WHERE code_key = 'MODULE' AND code_value = 'CPF' AND use_yn = 'Y';
"@)
    $result.checks.cpfCoreMessageCount = [int] (Invoke-Scalar -StepName "cpfCoreMessageCount" -SqlText @"
SELECT COUNT(*)
FROM cpfDB.cpf_message
WHERE message_code LIKE 'MCPF%' AND locale = 'ko' AND use_yn = 'Y';
"@)
    $result.checks.cpfCoreResponseCodeCount = [int] (Invoke-Scalar -StepName "cpfCoreResponseCodeCount" -SqlText @"
SELECT COUNT(*)
FROM cpfDB.cpf_response_code
WHERE module_id = 'CPF' AND response_code REGEXP '^[SE]CPF[0-9]{6}$' AND use_yn = 'Y';
"@)
    $result.checks.legacyCpfActiveSystemCodeCount = [int] (Invoke-Scalar -StepName "legacyCpfActiveSystemCodeCount" -SqlText @"
SELECT
    (SELECT COUNT(*) FROM cpfDB.cpf_code
      WHERE code_key = 'MODULE' AND code_value = 'CPF' AND use_yn = 'Y')
  + (SELECT COUNT(*) FROM cpfDB.cpf_message
      WHERE message_code LIKE 'MCPF%' AND use_yn = 'Y')
  + (SELECT COUNT(*) FROM cpfDB.cpf_response_code
      WHERE module_id = 'CPF' AND use_yn = 'Y');
"@)

    $result.checks.batCenterCutTableCount = [int] (Invoke-Scalar -StepName "batCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'batDB'
  AND table_name IN ('bat_center_cut_job', 'bat_center_cut_parameter', 'bat_center_cut_item', 'bat_center_cut_result');
"@)
    $result.checks.legacyCpfCenterCutTableCount = [int] (Invoke-Scalar -StepName "legacyCpfCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND table_name IN ('cpf_center_cut_job', 'cpf_center_cut_parameter', 'cpf_center_cut_item', 'cpf_center_cut_result');
"@)
    $result.checks.centerCutSeedCount = [int] (Invoke-Scalar -StepName "centerCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM batDB.bat_center_cut_job
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.centerCutParameterSeedCount = [int] (Invoke-Scalar -StepName "centerCutParameterSeedCount" -SqlText @"
SELECT COUNT(*)
FROM batDB.bat_center_cut_parameter
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.refCenterCutTableCount = [int] (Invoke-Scalar -StepName "refCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'refDB'
  AND table_name IN ('ref_center_cut_sample_target', 'ref_center_cut_sample_result');
"@)
    $result.checks.refCenterCutSeedCount = [int] (Invoke-Scalar -StepName "refCenterCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM refDB.ref_center_cut_sample_target
WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';
"@)
    $result.checks.transactionSegmentTableCount = [int] (Invoke-Scalar -StepName "transactionSegmentTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_transaction_segment';
"@)
    $result.checks.transactionSegmentRequiredColumnCount = [int] (Invoke-Scalar -StepName "transactionSegmentRequiredColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_transaction_segment'
  AND column_name IN (
      'transaction_segment_id',
      'transaction_global_id',
      'parent_segment_id',
      'transaction_role',
      'module_code',
      'source_module_code',
      'target_module_code',
      'direction',
      'started_at',
      'ended_at',
      'duration_ms',
      'status',
      'failure_yn',
      'failure_code',
      'failure_message_masked',
      'request_header_snapshot_masked',
      'response_header_snapshot_masked',
      'extension_header_snapshot_masked',
      'user_id_masked',
      'operator_id_masked',
      'client_app_id',
      'caller_service',
      'external_institution_code',
      'external_transaction_id'
  );
"@)
    $result.checks.transactionSegmentIndexCount = [int] (Invoke-Scalar -StepName "transactionSegmentIndexCount" -SqlText @"
SELECT COUNT(DISTINCT index_name)
FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_transaction_segment'
  AND index_name IN (
      'uk_cpf_transaction_segment_id',
      'ix_cpf_transaction_segment_global',
      'ix_cpf_transaction_segment_parent',
      'ix_cpf_transaction_segment_status',
      'ix_cpf_transaction_segment_external',
      'ix_cpf_transaction_segment_user',
      'ix_cpf_transaction_segment_operator',
      'ix_cpf_transaction_segment_client'
  );
"@)
    $result.checks.standardExecutionTableCount = [int] (Invoke-Scalar -StepName "standardExecutionTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_standard_execution';
"@)
    $result.checks.standardExecutionIndexCount = [int] (Invoke-Scalar -StepName "standardExecutionIndexCount" -SqlText @"
SELECT COUNT(DISTINCT index_name)
FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND table_name = 'cpf_standard_execution'
  AND index_name IN (
      'ix_cpf_standard_execution_type',
      'ix_cpf_standard_execution_owner',
      'ix_cpf_standard_execution_source'
  );
"@)
    $result.checks.standardExecutionAliasCount = [int] (Invoke-Scalar -StepName "standardExecutionAliasCount" -SqlText @"
SELECT COUNT(*) FROM cpfDB.cpf_standard_execution_alias;
"@)
    $result.checks.batchOnDemandTableCount = [int] (Invoke-Scalar -StepName "batchOnDemandTableCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'batDB' AND table_name = 'bat_on_demand_request';
"@)
    $result.checks.batRuntimeTableCount = [int] (Invoke-Scalar -StepName "batRuntimeTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'batDB'
  AND table_type = 'BASE TABLE'
  AND table_name IN (
      'BATCH_JOB_INSTANCE',
      'BATCH_JOB_EXECUTION',
      'BATCH_JOB_EXECUTION_PARAMS',
      'BATCH_STEP_EXECUTION',
      'BATCH_STEP_EXECUTION_CONTEXT',
      'BATCH_JOB_EXECUTION_CONTEXT',
      'bat_on_demand_request',
      'bat_job',
      'bat_schedule',
      'bat_job_relation',
      'bat_instance',
      'bat_worker',
      'bat_execution',
      'bat_execution_lease',
      'bat_execution_target',
      'bat_step_execution',
      'bat_lock',
      'bat_operation_log',
      'bat_ghost_event',
      'bat_business_day_calendar',
      'bat_center_cut_job',
      'bat_center_cut_parameter',
      'bat_center_cut_item',
      'bat_center_cut_result'
  );
"@)
    $result.checks.legacyCpfBatchTableCount = [int] (Invoke-Scalar -StepName "legacyCpfBatchTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND (
      table_name LIKE 'BATCH\_%'
      OR table_name LIKE 'cpf\_batch\_%'
      OR table_name LIKE 'bat\_center\_cut\_%'
      OR table_name = 'cpf_business_day_calendar'
  );
"@)
    $result.checks.batSequenceCount = [int] (Invoke-Scalar -StepName "batSequenceCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'batDB'
  AND table_name IN ('BATCH_STEP_EXECUTION_SEQ', 'BATCH_JOB_EXECUTION_SEQ', 'BATCH_JOB_SEQ')
  AND table_type = 'SEQUENCE';
"@)
    $result.checks.batAppDmlGrantCount = [int] (Invoke-Scalar -StepName "batAppDmlGrantCount" -SqlText @"
SELECT (Select_priv = 'Y') + (Insert_priv = 'Y') + (Update_priv = 'Y') + (Delete_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_bat_app' AND Host = 'localhost' AND LOWER(Db) = LOWER('batDB');
"@)
    $result.checks.batAppDdlGrantCount = [int] (Invoke-Scalar -StepName "batAppDdlGrantCount" -SqlText @"
SELECT (Create_priv = 'Y') + (Alter_priv = 'Y') + (Drop_priv = 'Y') + (Index_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_bat_app' AND Host = 'localhost' AND LOWER(Db) = LOWER('batDB');
"@)
    $result.checks.batMigrationDdlGrantCount = [int] (Invoke-Scalar -StepName "batMigrationDdlGrantCount" -SqlText @"
SELECT (Create_priv = 'Y') + (Alter_priv = 'Y') + (Drop_priv = 'Y') + (Index_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_bat_migration' AND Host = 'localhost' AND LOWER(Db) = LOWER('batDB');
"@)
    $result.checks.channelPolicyTableCount = [int] (Invoke-Scalar -StepName "channelPolicyTableCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND table_name IN ('cpf_channel_policy_version', 'cpf_channel_registry', 'cpf_channel_execution_policy');
"@)
    $result.checks.channelRegistrySeedCount = [int] (Invoke-Scalar -StepName "channelRegistrySeedCount" -SqlText @"
SELECT COUNT(*) FROM cpfDB.cpf_channel_registry
WHERE channel_code IN ('ANY', 'WEB', 'MOBILE', 'ADM', 'BATCH') AND active_yn = 'Y';
"@)
    $result.checks.channelPolicySeedCount = [int] (Invoke-Scalar -StepName "channelPolicySeedCount" -SqlText @"
SELECT COUNT(*) FROM cpfDB.cpf_channel_execution_policy
WHERE policy_key = 'CPF.DEFAULT' AND active_yn = 'Y';
"@)
    $result.checks.channelPolicyIndexCount = [int] (Invoke-Scalar -StepName "channelPolicyIndexCount" -SqlText @"
SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND index_name IN (
      'ix_cpf_channel_policy_version_target',
      'ix_cpf_channel_registry_active',
      'ix_cpf_channel_execution_policy_lookup',
      'ix_cpf_channel_execution_policy_effective'
  );
"@)
    $result.checks.accTableCount = [int] (Invoke-Scalar -StepName "accTableCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'accDB' AND table_name IN ('acc_account', 'acc_account_change_log');
"@)
    $result.checks.accCommonColumnCount = [int] (Invoke-Scalar -StepName "accCommonColumnCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.columns
WHERE table_schema = 'accDB'
  AND table_name IN ('acc_account', 'acc_account_change_log')
  AND column_name IN ('created_by', 'created_at', 'updated_by', 'updated_at');
"@)
    $result.checks.accForeignKeyCount = [int] (Invoke-Scalar -StepName "accForeignKeyCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.referential_constraints
WHERE constraint_schema = 'accDB' AND constraint_name = 'fk_acc_account_change_target';
"@)
    $result.checks.accServiceSeedCount = [int] (Invoke-Scalar -StepName "accServiceSeedCount" -SqlText @"
SELECT COUNT(*) FROM cpfDB.cpf_service
WHERE service_id = 'ACC' AND use_yn = 'Y';
"@)
    $result.checks.accAppDmlGrantCount = [int] (Invoke-Scalar -StepName "accAppDmlGrantCount" -SqlText @"
SELECT (Select_priv = 'Y') + (Insert_priv = 'Y') + (Update_priv = 'Y') + (Delete_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_acc_app' AND Host = 'localhost' AND LOWER(Db) = LOWER('accDB');
"@)
    $result.checks.accAppDdlGrantCount = [int] (Invoke-Scalar -StepName "accAppDdlGrantCount" -SqlText @"
SELECT (Create_priv = 'Y') + (Alter_priv = 'Y') + (Drop_priv = 'Y') + (Index_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_acc_app' AND Host = 'localhost' AND LOWER(Db) = LOWER('accDB');
"@)
    $result.checks.accMigrationDdlGrantCount = [int] (Invoke-Scalar -StepName "accMigrationDdlGrantCount" -SqlText @"
SELECT (Create_priv = 'Y') + (Alter_priv = 'Y') + (Drop_priv = 'Y') + (Index_priv = 'Y')
FROM mysql.db WHERE User = 'cpf_acc_migration' AND Host = 'localhost' AND LOWER(Db) = LOWER('accDB');
"@)
    $result.checks.bzaTableCount = [int] (Invoke-Scalar -StepName "bzaTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'bzaDB'
  AND table_name IN (
      'bza_admin_user', 'bza_login_history', 'bza_refresh_token', 'bza_menu',
      'bza_role', 'bza_permission', 'bza_organization', 'bza_employee',
      'bza_business_audit', 'bza_approval_document',
      'bza_approval_line', 'bza_approval_history', 'bza_project_setting'
  );
"@)
    $result.checks.bzaCommonColumnCount = [int] (Invoke-Scalar -StepName "bzaCommonColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'bzaDB'
  AND table_name LIKE 'bza\_%'
  AND column_name IN ('created_by', 'created_at', 'updated_by', 'updated_at');
"@)
    $result.checks.bzaAllTableCount = [int] (Invoke-Scalar -StepName "bzaAllTableCount" -SqlText @"
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'bzaDB' AND table_name LIKE 'bza\_%';
"@)
    $result.checks.cmnFixedLengthTableCount = [int] (Invoke-Scalar -StepName "cmnFixedLengthTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cmnDB'
  AND table_name IN (
      'cmn_fixed_length_layout',
      'cmn_fixed_length_group',
      'cmn_fixed_length_field',
      'cmn_fixed_length_masking_policy'
  );
"@)
    $result.checks.cmnFixedLengthSeedCount = [int] (Invoke-Scalar -StepName "cmnFixedLengthSeedCount" -SqlText @"
SELECT COUNT(*)
FROM cmnDB.cmn_fixed_length_layout
WHERE layout_id = 'BANK01_BALANCE_REQ_V1';
"@)
    $result.checks.cpfReliabilityTableCount = [int] (Invoke-Scalar -StepName "cpfReliabilityTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cpfDB'
  AND table_name IN (
      'cpf_idempotency_record',
      'cpf_broker_outbox',
      'cpf_broker_inbox',
      'cpf_broker_dlq',
      'cpf_file_transfer_history',
      'cpf_unknown_result'
  );
"@)
    $result.checks.cpfReliabilityCommonColumnCount = [int] (Invoke-Scalar -StepName "cpfReliabilityCommonColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'cpfDB'
  AND table_name IN (
      'cpf_idempotency_record',
      'cpf_broker_outbox',
      'cpf_broker_inbox',
      'cpf_broker_dlq',
      'cpf_file_transfer_history',
      'cpf_unknown_result'
  )
  AND column_name IN ('created_by', 'created_at', 'updated_by', 'updated_at');
"@)
    $result.checks.cpfReliabilityIndexCount = [int] (Invoke-Scalar -StepName "cpfReliabilityIndexCount" -SqlText @"
SELECT COUNT(DISTINCT index_name)
FROM information_schema.statistics
WHERE table_schema = 'cpfDB'
  AND table_name IN (
      'cpf_idempotency_record',
      'cpf_broker_outbox',
      'cpf_broker_inbox',
      'cpf_broker_dlq',
      'cpf_file_transfer_history',
      'cpf_unknown_result'
  )
  AND index_name IN (
      'uk_cpf_idempotency_record_key',
      'ix_cpf_idempotency_record_status',
      'uk_cpf_broker_outbox_message',
      'ix_cpf_broker_outbox_status',
      'ix_cpf_broker_outbox_tx',
      'ix_cpf_broker_outbox_topic',
      'uk_cpf_broker_inbox_message',
      'ix_cpf_broker_inbox_idempotency',
      'ix_cpf_broker_inbox_status',
      'uk_cpf_broker_dlq_message',
      'ix_cpf_broker_dlq_status',
      'ix_cpf_broker_dlq_topic',
      'uk_cpf_file_transfer_history_id',
      'ix_cpf_file_transfer_duplicate',
      'ix_cpf_file_transfer_tx',
      'ix_cpf_file_transfer_status',
      'uk_cpf_unknown_result_id',
      'ix_cpf_unknown_result_status',
      'ix_cpf_unknown_result_tx',
      'ix_cpf_unknown_result_external'
  );
"@)
    $result.checks.admReliabilityMenuCount = [int] (Invoke-Scalar -StepName "admReliabilityMenuCount" -SqlText @"
SELECT COUNT(*)
FROM admDB.adm_menu
WHERE menu_id = 'RELIABILITY'
  AND use_yn = 'Y';
"@)
    $result.checks.admReliabilityButtonCount = [int] (Invoke-Scalar -StepName "admReliabilityButtonCount" -SqlText @"
SELECT COUNT(*)
FROM admDB.adm_button
WHERE button_id IN ('RELIABILITY_READ', 'RELIABILITY_REPLAY', 'RELIABILITY_RESOLVE')
  AND use_yn = 'Y';
"@)
    $result.checks.admReliabilityRoleApiCount = [int] (Invoke-Scalar -StepName "admReliabilityRoleApiCount" -SqlText @"
SELECT COUNT(*)
FROM admDB.adm_role_api_permission rap
JOIN admDB.adm_api_permission ap ON ap.api_permission_id = rap.api_permission_id
WHERE ap.button_id IN ('RELIABILITY_READ', 'RELIABILITY_REPLAY', 'RELIABILITY_RESOLVE')
  AND rap.role_id IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR', 'ADM_BIZ_OPERATOR', 'ADM_VIEWER');
"@)
    $result.checks.admChannelPolicyMenuCount = [int] (Invoke-Scalar -StepName "admChannelPolicyMenuCount" -SqlText @"
SELECT COUNT(*) FROM admDB.adm_menu
WHERE menu_id = 'CHANNEL_POLICY' AND use_yn = 'Y';
"@)
    $result.checks.admChannelPolicyButtonCount = [int] (Invoke-Scalar -StepName "admChannelPolicyButtonCount" -SqlText @"
SELECT COUNT(*) FROM admDB.adm_button
WHERE button_id IN ('CHANNEL_POLICY_READ', 'CHANNEL_POLICY_WRITE', 'CHANNEL_POLICY_REFRESH', 'CHANNEL_POLICY_IMPORT')
  AND use_yn = 'Y';
"@)
    $result.checks.admChannelPolicyRoleApiCount = [int] (Invoke-Scalar -StepName "admChannelPolicyRoleApiCount" -SqlText @"
SELECT COUNT(*) FROM admDB.adm_role_api_permission rap
JOIN admDB.adm_api_permission ap ON ap.api_permission_id = rap.api_permission_id
WHERE ap.button_id IN ('CHANNEL_POLICY_READ', 'CHANNEL_POLICY_WRITE', 'CHANNEL_POLICY_REFRESH', 'CHANNEL_POLICY_IMPORT')
  AND rap.role_id IN ('ADM_ADMIN', 'ADM_DEV_OPERATOR', 'ADM_OPERATOR', 'ADM_BIZ_OPERATOR', 'ADM_VIEWER');
"@)

    if ($result.checks.cpfCoreModuleCodeCount -ne 1) {
        throw "cpf-core CPF module code seed mismatch. actual=$($result.checks.cpfCoreModuleCodeCount)"
    }
    if ($result.checks.cpfCoreMessageCount -ne 16 -or $result.checks.cpfCoreResponseCodeCount -ne 16) {
        throw "cpf-core CPF message/response seed mismatch. messages=$($result.checks.cpfCoreMessageCount) responses=$($result.checks.cpfCoreResponseCodeCount)"
    }
    if ($result.checks.legacyCpfActiveSystemCodeCount -ne 0) {
        throw "legacy CPF system code remains active. actual=$($result.checks.legacyCpfActiveSystemCodeCount)"
    }
    if ($result.checks.batCenterCutTableCount -ne 4) {
        throw "bat_center_cut_* table count mismatch. actual=$($result.checks.batCenterCutTableCount)"
    }
    if ($result.checks.legacyCpfCenterCutTableCount -ne 0) {
        throw "legacy cpf_center_cut_* tables remain. actual=$($result.checks.legacyCpfCenterCutTableCount)"
    }
    if ($result.checks.centerCutSeedCount -lt 1) {
        throw "CPF_BAT_CENTER_CUT_JOB seed is missing."
    }
    if ($result.checks.refCenterCutTableCount -ne 2) {
        throw "ref_center_cut_sample_* table count mismatch. actual=$($result.checks.refCenterCutTableCount)"
    }
    if ($result.checks.refCenterCutSeedCount -lt 4) {
        throw "CPF_REF_CENTER_CUT_SAMPLE_JOB target seed is missing."
    }
    if ($result.checks.transactionSegmentTableCount -ne 1) {
        throw "cpf_transaction_segment table is missing."
    }
    if ($result.checks.transactionSegmentRequiredColumnCount -ne 24) {
        throw "cpf_transaction_segment required column count mismatch. actual=$($result.checks.transactionSegmentRequiredColumnCount)"
    }
    if ($result.checks.transactionSegmentIndexCount -ne 8) {
        throw "cpf_transaction_segment required index count mismatch. actual=$($result.checks.transactionSegmentIndexCount)"
    }
    if ($result.checks.standardExecutionTableCount -ne 1) {
        throw "cpf_standard_execution table is missing."
    }
    if ($result.checks.standardExecutionIndexCount -ne 3) {
        throw "cpf_standard_execution required index count mismatch. actual=$($result.checks.standardExecutionIndexCount)"
    }
    if ($result.checks.standardExecutionAliasCount -ne 327) {
        throw "standard execution alias seed count mismatch. actual=$($result.checks.standardExecutionAliasCount)"
    }
    if ($result.checks.batchOnDemandTableCount -ne 1) {
        throw "bat_on_demand_request table is missing from batDB."
    }
    if ($result.checks.batRuntimeTableCount -ne 24) {
        throw "BAT runtime table count mismatch. actual=$($result.checks.batRuntimeTableCount)"
    }
    if ($result.checks.legacyCpfBatchTableCount -ne 0) {
        throw "BAT-owned runtime tables remain in cpfDB. actual=$($result.checks.legacyCpfBatchTableCount)"
    }
    if ($result.checks.batSequenceCount -ne 3) {
        throw "Spring Batch MariaDB sequence contract mismatch. actual=$($result.checks.batSequenceCount)"
    }
    if ($result.checks.batAppDmlGrantCount -ne 4 -or $result.checks.batAppDdlGrantCount -ne 0) {
        throw "BAT app account privilege separation failed."
    }
    if ($result.checks.batMigrationDdlGrantCount -ne 4) {
        throw "BAT migration account DDL privileges are incomplete."
    }
    if ($result.checks.channelPolicyTableCount -ne 3) {
        throw "CPF channel policy table count mismatch. actual=$($result.checks.channelPolicyTableCount)"
    }
    if ($result.checks.channelRegistrySeedCount -ne 5 -or $result.checks.channelPolicySeedCount -ne 1) {
        throw "CPF channel policy seed mismatch. channels=$($result.checks.channelRegistrySeedCount) policies=$($result.checks.channelPolicySeedCount)"
    }
    if ($result.checks.channelPolicyIndexCount -ne 4) {
        throw "CPF channel policy index count mismatch. actual=$($result.checks.channelPolicyIndexCount)"
    }
    if ($result.checks.accTableCount -ne 2 -or $result.checks.accCommonColumnCount -ne 8) {
        throw "ACC reference table/common column mismatch. tables=$($result.checks.accTableCount) columns=$($result.checks.accCommonColumnCount)"
    }
    if ($result.checks.accForeignKeyCount -ne 1 -or $result.checks.accServiceSeedCount -ne 1) {
        throw "ACC FK or service registry seed is missing."
    }
    if ($result.checks.accAppDmlGrantCount -ne 4 -or $result.checks.accAppDdlGrantCount -ne 0) {
        throw "ACC app account privilege separation failed."
    }
    if ($result.checks.accMigrationDdlGrantCount -ne 4) {
        throw "ACC migration account DDL privileges are incomplete."
    }
    if ($result.checks.bzaTableCount -ne 17) {
        throw "BZA baseline table count mismatch. actual=$($result.checks.bzaTableCount)"
    }
    if ($result.checks.bzaCommonColumnCount -ne ($result.checks.bzaAllTableCount * 4)) {
        throw "BZA common audit column count mismatch. tables=$($result.checks.bzaAllTableCount) columns=$($result.checks.bzaCommonColumnCount)"
    }
    if ($result.checks.cmnFixedLengthTableCount -ne 4) {
        throw "cmn_fixed_length_* table count mismatch. actual=$($result.checks.cmnFixedLengthTableCount)"
    }
    if ($result.checks.cmnFixedLengthSeedCount -lt 1) {
        throw "BANK01_BALANCE_REQ_V1 fixed-length layout seed is missing."
    }
    if ($result.checks.cpfReliabilityTableCount -ne 6) {
        throw "CPF reliability table count mismatch. actual=$($result.checks.cpfReliabilityTableCount)"
    }
    if ($result.checks.cpfReliabilityCommonColumnCount -ne 24) {
        throw "CPF reliability common column count mismatch. actual=$($result.checks.cpfReliabilityCommonColumnCount)"
    }
    if ($result.checks.cpfReliabilityIndexCount -ne 20) {
        throw "CPF reliability index count mismatch. actual=$($result.checks.cpfReliabilityIndexCount)"
    }
    if ($result.checks.admReliabilityMenuCount -ne 1) {
        throw "ADM reliability menu count mismatch. actual=$($result.checks.admReliabilityMenuCount)"
    }
    if ($result.checks.admReliabilityButtonCount -ne 3) {
        throw "ADM reliability button count mismatch. actual=$($result.checks.admReliabilityButtonCount)"
    }
    if ($result.checks.admReliabilityRoleApiCount -ne 15) {
        throw "ADM reliability role API count mismatch. actual=$($result.checks.admReliabilityRoleApiCount)"
    }
    if ($result.checks.admChannelPolicyMenuCount -ne 1 -or $result.checks.admChannelPolicyButtonCount -ne 4) {
        throw "ADM channel policy menu/button seed mismatch. menu=$($result.checks.admChannelPolicyMenuCount) buttons=$($result.checks.admChannelPolicyButtonCount)"
    }
    if ($result.checks.admChannelPolicyRoleApiCount -ne 20) {
        throw "ADM channel policy role API count mismatch. actual=$($result.checks.admChannelPolicyRoleApiCount)"
    }

    $result.status = $StatusDone
    Save-SmokeResult
    Write-Host "MariaDB full install smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = ConvertTo-SafeMessage $_.Exception.Message
    Save-SmokeResult
    Write-Error $result.error
    exit 1
}
