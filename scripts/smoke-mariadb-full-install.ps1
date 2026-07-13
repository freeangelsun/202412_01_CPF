param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $RootUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $RootPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $RequireRun
)

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
$resultPath = Join-Path $ResultDir "mariadb-full-install-result.json"
$logPath = Join-Path $ResultDir "mariadb-full-install.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusNotVerified
    mode = "MariaDB CLI"
    host = $HostName
    port = $Port
    username = $RootUsername
    passwordProvided = -not [string]::IsNullOrWhiteSpace($RootPassword)
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
        return $Message.Replace($RootPassword, "****")
    }
    return $Message
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
        $arguments += "--password=$RootPassword"
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
        [string] $RelativePath
    )

    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        throw "SQL file not found. step=$StepName path=$RelativePath"
    }

    Add-Log "SQL start: $RelativePath"
    $result.sqlFiles += $RelativePath
    $sourcePath = ((Resolve-Path -LiteralPath $path).Path).Replace("\", "/")
    $output = Invoke-MariaDbText -StepName $StepName -SqlText "SOURCE $sourcePath;`n"
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

    Invoke-MariaDbFile -StepName "allInstallAndSmoke" -RelativePath "specs/sql/00_all_install_and_smoke.sql" | Out-Null
    Invoke-MariaDbFile -StepName "smokeCheck" -RelativePath "specs/sql/99_smoke_check.sql" | Out-Null
    Invoke-MariaDbFile -StepName "frameworkSeedRepeat" -RelativePath "specs/sql/50_framework_seed_data.sql" | Out-Null

    $result.checks.batCenterCutTableCount = [int] (Invoke-Scalar -StepName "batCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name IN ('bat_center_cut_job', 'bat_center_cut_parameter', 'bat_center_cut_item', 'bat_center_cut_result');
"@)
    $result.checks.legacyPfwCenterCutTableCount = [int] (Invoke-Scalar -StepName "legacyPfwCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name IN ('pfw_center_cut_job', 'pfw_center_cut_parameter', 'pfw_center_cut_item', 'pfw_center_cut_result');
"@)
    $result.checks.centerCutSeedCount = [int] (Invoke-Scalar -StepName "centerCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM pfwDB.bat_center_cut_job
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.centerCutParameterSeedCount = [int] (Invoke-Scalar -StepName "centerCutParameterSeedCount" -SqlText @"
SELECT COUNT(*)
FROM pfwDB.bat_center_cut_parameter
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.xyzCenterCutTableCount = [int] (Invoke-Scalar -StepName "xyzCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'xyzDB'
  AND table_name IN ('xyz_center_cut_sample_target', 'xyz_center_cut_sample_result');
"@)
    $result.checks.xyzCenterCutSeedCount = [int] (Invoke-Scalar -StepName "xyzCenterCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM xyzDB.xyz_center_cut_sample_target
WHERE center_cut_job_id = 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB';
"@)
    $result.checks.transactionSegmentTableCount = [int] (Invoke-Scalar -StepName "transactionSegmentTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name = 'pfw_transaction_segment';
"@)
    $result.checks.transactionSegmentRequiredColumnCount = [int] (Invoke-Scalar -StepName "transactionSegmentRequiredColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'pfwDB'
  AND table_name = 'pfw_transaction_segment'
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
WHERE table_schema = 'pfwDB'
  AND table_name = 'pfw_transaction_segment'
  AND index_name IN (
      'uk_pfw_transaction_segment_id',
      'ix_pfw_transaction_segment_global',
      'ix_pfw_transaction_segment_parent',
      'ix_pfw_transaction_segment_status',
      'ix_pfw_transaction_segment_external',
      'ix_pfw_transaction_segment_user',
      'ix_pfw_transaction_segment_operator',
      'ix_pfw_transaction_segment_client'
  );
"@)
    $result.checks.exsLedgerRequiredColumnCount = [int] (Invoke-Scalar -StepName "exsLedgerRequiredColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'exsDB'
  AND table_name = 'exs_transaction_log'
  AND column_name IN (
      'transaction_segment_id',
      'api_path',
      'request_header_masked',
      'response_header_masked',
      'request_payload_masked',
      'response_payload_masked',
      'http_status',
      'timeout_ms',
      'retry_count'
  );
"@)
    $result.checks.exsMessageRequiredColumnCount = [int] (Invoke-Scalar -StepName "exsMessageRequiredColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'exsDB'
  AND table_name = 'exs_message_log'
  AND column_name IN (
      'transaction_segment_id',
      'message_code',
      'request_payload_masked',
      'response_payload_masked',
      'status',
      'failure_code',
      'failure_message_masked'
  );
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
    $result.checks.pfwReliabilityTableCount = [int] (Invoke-Scalar -StepName "pfwReliabilityTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name IN (
      'pfw_idempotency_record',
      'pfw_broker_outbox',
      'pfw_broker_inbox',
      'pfw_broker_dlq',
      'pfw_file_transfer_history',
      'pfw_unknown_result'
  );
"@)
    $result.checks.pfwReliabilityCommonColumnCount = [int] (Invoke-Scalar -StepName "pfwReliabilityCommonColumnCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.columns
WHERE table_schema = 'pfwDB'
  AND table_name IN (
      'pfw_idempotency_record',
      'pfw_broker_outbox',
      'pfw_broker_inbox',
      'pfw_broker_dlq',
      'pfw_file_transfer_history',
      'pfw_unknown_result'
  )
  AND column_name IN ('created_by', 'created_at', 'updated_by', 'updated_at');
"@)
    $result.checks.pfwReliabilityIndexCount = [int] (Invoke-Scalar -StepName "pfwReliabilityIndexCount" -SqlText @"
SELECT COUNT(DISTINCT index_name)
FROM information_schema.statistics
WHERE table_schema = 'pfwDB'
  AND table_name IN (
      'pfw_idempotency_record',
      'pfw_broker_outbox',
      'pfw_broker_inbox',
      'pfw_broker_dlq',
      'pfw_file_transfer_history',
      'pfw_unknown_result'
  )
  AND index_name IN (
      'uk_pfw_idempotency_record_key',
      'ix_pfw_idempotency_record_status',
      'uk_pfw_broker_outbox_message',
      'ix_pfw_broker_outbox_status',
      'ix_pfw_broker_outbox_tx',
      'ix_pfw_broker_outbox_topic',
      'uk_pfw_broker_inbox_message',
      'ix_pfw_broker_inbox_idempotency',
      'ix_pfw_broker_inbox_status',
      'uk_pfw_broker_dlq_message',
      'ix_pfw_broker_dlq_status',
      'ix_pfw_broker_dlq_topic',
      'uk_pfw_file_transfer_history_id',
      'ix_pfw_file_transfer_duplicate',
      'ix_pfw_file_transfer_tx',
      'ix_pfw_file_transfer_status',
      'uk_pfw_unknown_result_id',
      'ix_pfw_unknown_result_status',
      'ix_pfw_unknown_result_tx',
      'ix_pfw_unknown_result_external'
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

    if ($result.checks.batCenterCutTableCount -ne 4) {
        throw "bat_center_cut_* table count mismatch. actual=$($result.checks.batCenterCutTableCount)"
    }
    if ($result.checks.legacyPfwCenterCutTableCount -ne 0) {
        throw "legacy pfw_center_cut_* tables remain. actual=$($result.checks.legacyPfwCenterCutTableCount)"
    }
    if ($result.checks.centerCutSeedCount -lt 1) {
        throw "CPF_BAT_CENTER_CUT_JOB seed is missing."
    }
    if ($result.checks.xyzCenterCutTableCount -ne 2) {
        throw "xyz_center_cut_sample_* table count mismatch. actual=$($result.checks.xyzCenterCutTableCount)"
    }
    if ($result.checks.xyzCenterCutSeedCount -lt 4) {
        throw "CPF_XYZ_CENTER_CUT_SAMPLE_JOB target seed is missing."
    }
    if ($result.checks.transactionSegmentTableCount -ne 1) {
        throw "pfw_transaction_segment table is missing."
    }
    if ($result.checks.transactionSegmentRequiredColumnCount -ne 24) {
        throw "pfw_transaction_segment required column count mismatch. actual=$($result.checks.transactionSegmentRequiredColumnCount)"
    }
    if ($result.checks.transactionSegmentIndexCount -ne 8) {
        throw "pfw_transaction_segment required index count mismatch. actual=$($result.checks.transactionSegmentIndexCount)"
    }
    if ($result.checks.exsLedgerRequiredColumnCount -ne 9) {
        throw "exs_transaction_log required column count mismatch. actual=$($result.checks.exsLedgerRequiredColumnCount)"
    }
    if ($result.checks.exsMessageRequiredColumnCount -ne 7) {
        throw "exs_message_log required column count mismatch. actual=$($result.checks.exsMessageRequiredColumnCount)"
    }
    if ($result.checks.cmnFixedLengthTableCount -ne 4) {
        throw "cmn_fixed_length_* table count mismatch. actual=$($result.checks.cmnFixedLengthTableCount)"
    }
    if ($result.checks.cmnFixedLengthSeedCount -lt 1) {
        throw "BANK01_BALANCE_REQ_V1 fixed-length layout seed is missing."
    }
    if ($result.checks.pfwReliabilityTableCount -ne 6) {
        throw "PFW reliability table count mismatch. actual=$($result.checks.pfwReliabilityTableCount)"
    }
    if ($result.checks.pfwReliabilityCommonColumnCount -ne 24) {
        throw "PFW reliability common column count mismatch. actual=$($result.checks.pfwReliabilityCommonColumnCount)"
    }
    if ($result.checks.pfwReliabilityIndexCount -ne 20) {
        throw "PFW reliability index count mismatch. actual=$($result.checks.pfwReliabilityIndexCount)"
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
