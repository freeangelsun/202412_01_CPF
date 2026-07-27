param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $RootUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $RootPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $RequireRun
)

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($HostName)) { $HostName = "localhost" }
if ([string]::IsNullOrWhiteSpace($Port)) { $Port = "3306" }
if ([string]::IsNullOrWhiteSpace($RootUsername)) { $RootUsername = "root" }
if ([string]::IsNullOrWhiteSpace($ResultDir)) { $ResultDir = Join-Path $Root "build/sql-smoke/data-safety-recovery" }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "data-safety-migration-recovery.sanitized.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "미검증"
    scenario = "V60 fixture -> partial V61 -> full V61 recovery -> exact rollback -> V61 reapply"
    host = $HostName
    port = $Port
    credentialsProvided = -not [string]::IsNullOrWhiteSpace($RootPassword)
    steps = [ordered]@{}
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), [System.Text.UTF8Encoding]::new($false))
}

function Find-Client {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath) { return (Resolve-Path -LiteralPath $ClientPath).Path }
        $cmd = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $cmd) { return $cmd.Source }
    }
    foreach ($name in @('mariadb')) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $cmd) { return $cmd.Source }
    }
    return $null
}

$client = Find-Client
if ($null -eq $client -or [string]::IsNullOrWhiteSpace($RootPassword)) {
    Save-Result
    if ($RequireRun) { throw "MariaDB client/root credential is required for data-safety migration recovery smoke." }
    Write-Host "CPF data-safety migration recovery: 미검증 (MariaDB client/credential unavailable)"
    exit 0
}

$admDb = "cpf_qa_adm_" + ([Guid]::NewGuid().ToString('N').Substring(0,8))
$bzaDb = "cpf_qa_bza_" + ([Guid]::NewGuid().ToString('N').Substring(0,8))
$result.scratchDatabases = @($admDb, $bzaDb)

function Invoke-Sql {
    param([string] $Sql, [switch] $AllowFailure)
    $temp = Join-Path $ResultDir ("sql-" + [Guid]::NewGuid().ToString('N') + ".sql")
    [System.IO.File]::WriteAllText($temp, $Sql, [System.Text.UTF8Encoding]::new($false))
    try {
        $args = @("--host=$HostName", "--port=$Port", "--user=$RootUsername", "--batch", "--skip-column-names")
        $previousMysqlPwd = $env:MYSQL_PWD
        $previousMariaPwd = $env:MARIADB_PWD
        try {
            $env:MYSQL_PWD = $RootPassword
            $env:MARIADB_PWD = $RootPassword
            $output = & $client @args "--execute=source $($temp.Replace('\','/'));" 2>&1
            $exitCode = $LASTEXITCODE
        } finally {
            $env:MYSQL_PWD = $previousMysqlPwd
            $env:MARIADB_PWD = $previousMariaPwd
        }
        if ($exitCode -ne 0 -and -not $AllowFailure) { throw (($output | Out-String).Trim()) }
        return [ordered]@{ exitCode = $exitCode; output = (($output | Out-String) -replace [regex]::Escape($RootPassword), '****').Trim() }
    } finally {
        Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue
    }
}

function Map-MigrationSql {
    param([string] $Path)
    $text = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    $text = $text -replace '(?im)^USE\s+admDB\s*;', ("USE ``" + $admDb + "``;")
    $text = $text -replace '(?im)^USE\s+bzaDB\s*;', ("USE ``" + $bzaDb + "``;")
    return $text
}

$v61Path = Join-Path $Root "cpf-tools/db/vendor/mariadb/source/migration/flyway/V61__admin_data_safety_status.sql"
$rollbackPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/source/migration/rollback/V61__admin_data_safety_status_rollback.sql"

$fixture = @"
DROP DATABASE IF EXISTS ``$admDb``;
DROP DATABASE IF EXISTS ``$bzaDb``;
CREATE DATABASE ``$admDb`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE ``$bzaDb`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ``$admDb``;
CREATE TABLE adm_operator (
 OPERATOR_ID BIGINT NOT NULL AUTO_INCREMENT, OPERATOR_NAME VARCHAR(100) NOT NULL, PASSWORD_HASH VARCHAR(300),
 USE_YN CHAR(1) NOT NULL DEFAULT 'Y', LOCKED_YN CHAR(1) NOT NULL DEFAULT 'N', PRIMARY KEY(OPERATOR_ID)
) ENGINE=InnoDB;
CREATE TABLE adm_operator_profile (
 PROFILE_ID BIGINT NOT NULL AUTO_INCREMENT, OPERATOR_ID BIGINT NOT NULL, EFFECTIVE_TO DATETIME NULL, PRIMARY KEY(PROFILE_ID)
) ENGINE=InnoDB;
INSERT INTO adm_operator(OPERATOR_NAME, PASSWORD_HASH, USE_YN, LOCKED_YN) VALUES ('qa-admin', 'x', 'Y', 'N');
INSERT INTO adm_operator_profile(OPERATOR_ID, EFFECTIVE_TO) VALUES (1, NULL);
USE ``$bzaDb``;
CREATE TABLE bza_admin_user (
 admin_user_id BIGINT NOT NULL AUTO_INCREMENT, admin_login_id VARCHAR(80) NOT NULL, admin_name VARCHAR(100) NOT NULL,
 role_code VARCHAR(50) NOT NULL, use_yn CHAR(1) NOT NULL DEFAULT 'Y', lock_yn CHAR(1) NOT NULL DEFAULT 'N', PRIMARY KEY(admin_user_id)
) ENGINE=InnoDB;
CREATE TABLE bza_employee (
 employee_id BIGINT NOT NULL AUTO_INCREMENT, employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', PRIMARY KEY(employee_id),
 CONSTRAINT ck_bza_employee_status CHECK (employment_status IN ('ACTIVE','EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'))
) ENGINE=InnoDB;
INSERT INTO bza_admin_user(admin_login_id, admin_name, role_code, use_yn, lock_yn) VALUES ('qa-bza', 'QA BZA', 'ADMIN', 'Y', 'N');
INSERT INTO bza_employee(employment_status) VALUES ('ACTIVE');
"@

try {
    $result.steps.fixture = Invoke-Sql $fixture

    # DDL auto-commit 상황을 모사하여 V61 앞부분만 적용한 뒤 의도적으로 중단합니다.
    $partial = @"
USE ``$admDb``;
ALTER TABLE adm_operator ADD COLUMN IF NOT EXISTS ACCOUNT_STATUS VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION';
ALTER TABLE adm_operator ADD COLUMN IF NOT EXISTS VERSION_NO BIGINT NOT NULL DEFAULT 0;
"@
    $result.steps.partialV61 = Invoke-Sql $partial

    $result.steps.recoverFullV61 = Invoke-Sql (Map-MigrationSql $v61Path)

    $verifyV61 = @"
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='$admDb' AND table_name='adm_operator' AND column_name IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='$bzaDb' AND table_name='bza_admin_user' AND column_name IN ('account_status','version_no');
SELECT employment_status FROM ``$bzaDb``.bza_employee WHERE employee_id=1;
"@
    $verifyOutput = Invoke-Sql $verifyV61
    $result.steps.verifyV61 = $verifyOutput
    $lines = @($verifyOutput.output -split "`r?`n" | Where-Object { $_ -ne '' })
    if ($lines.Count -lt 3 -or $lines[0] -ne '3' -or $lines[1] -ne '2' -or $lines[2] -ne 'EMPLOYED') {
        throw "Recovered V61 schema verification failed."
    }

    $result.steps.exactRollback = Invoke-Sql (Map-MigrationSql $rollbackPath)
    $verifyRollback = @"
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='$admDb' AND table_name='adm_operator' AND column_name IN ('ACCOUNT_STATUS','VERSION_NO','CREATE_OPERATION_ID');
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='$bzaDb' AND table_name='bza_admin_user' AND column_name IN ('account_status','version_no');
"@
    $rb = Invoke-Sql $verifyRollback
    $result.steps.verifyRollback = $rb
    $lines = @($rb.output -split "`r?`n" | Where-Object { $_ -ne '' })
    if ($lines.Count -lt 2 -or $lines[0] -ne '0' -or $lines[1] -ne '0') { throw "Exact rollback verification failed." }

    $result.steps.reapplyV61 = Invoke-Sql (Map-MigrationSql $v61Path)
    $result.status = "완료"
    Save-Result
    Write-Host "CPF data-safety migration recovery: 완료"
} catch {
    $result.status = "실패"
    $result.error = ($_.Exception.Message -replace [regex]::Escape($RootPassword), '****')
    Save-Result
    throw
} finally {
    try { Invoke-Sql "DROP DATABASE IF EXISTS ``$admDb``; DROP DATABASE IF EXISTS ``$bzaDb``;" | Out-Null } catch {}
}
