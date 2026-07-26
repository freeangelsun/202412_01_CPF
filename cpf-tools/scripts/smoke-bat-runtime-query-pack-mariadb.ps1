param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = "",
    [string] $EvidencePath = ""
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools\config\database-install.default.json"
}
$ProfilePath = (Resolve-Path -LiteralPath $ProfilePath).Path
. (Join-Path $Root "cpf-tools\scripts\database-profile-common.ps1")

$profile = Get-CpfDatabaseProfile -Path $ProfilePath
$batch = $profile.modules.batch
if ($null -eq $batch -or -not [bool]$batch.enabled) {
    throw "DB Profile의 BAT Module이 활성화되어 있지 않습니다."
}
$vendor = ([string]$batch.vendor).Trim().ToLowerInvariant()
if ($vendor -cne "mariadb") {
    throw "이 Runtime Query 실행 Smoke는 MariaDB Profile만 지원합니다. vendor=$vendor"
}

$clientPath = [string]$batch.clientPath
if (-not (Test-Path -LiteralPath $clientPath -PathType Leaf)) {
    throw "MariaDB Client가 없습니다: $clientPath"
}
$runtimeUsername = [string]$batch.runtime.username
$allowDevDefault = ([string]$profile.environment).ToLowerInvariant() -in @(
    "development", "dev", "local"
) -and [bool]$profile.policy.allowInlineDevDefaults
$runtimePassword = Resolve-CpfProfileSecret `
    -SecretSpec $batch.runtime.password `
    -DisplayName "batch.runtime.password" `
    -AllowDevDefault $allowDevDefault

$hostName = [string]$batch.host
$port = [int]$batch.port
$databaseName = [string]$batch.databaseName
$packRoot = Join-Path $Root "cpf-tools\db\vendor\mariadb\runtime\bat\repository"
$contractPath = Join-Path $Root "cpf-tools\db\metadata\bat-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$statements = @($contract.statements | Sort-Object key)
$failures = [System.Collections.Generic.List[object]]::new()
$preparedCount = 0
$startedAt = [DateTimeOffset]::Now

function ConvertTo-MariaStringLiteral {
    param([Parameter(Mandatory = $true)][string] $Value)
    return $Value.Replace("\", "\\").Replace("'", "''")
}

function Test-PreparedStatement {
    param(
        [Parameter(Mandatory = $true)][string] $Key,
        [Parameter(Mandatory = $true)][string] $SqlText
    )

    $escapedSql = ConvertTo-MariaStringLiteral $SqlText
    $inputSql = @"
SET NAMES utf8mb4;
SET @cpf_runtime_sql = '$escapedSql';
PREPARE cpf_runtime_stmt FROM @cpf_runtime_sql;
DEALLOCATE PREPARE cpf_runtime_stmt;
"@
    $processInfo = [Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = $clientPath
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.CreateNoWindow = $true
    foreach ($argument in @(
        "--protocol=TCP",
        "--host=$hostName",
        "--port=$port",
        "--user=$runtimeUsername",
        "--database=$databaseName",
        "--ssl=0",
        "--batch",
        "--raw",
        "--skip-column-names",
        "--default-character-set=utf8mb4",
        "--connect-timeout=5"
    )) {
        [void]$processInfo.ArgumentList.Add($argument)
    }
    $processInfo.Environment["MYSQL_PWD"] = $runtimePassword
    $processInfo.Environment["MARIADB_PWD"] = $runtimePassword
    $process = [Diagnostics.Process]::Start($processInfo)
    $succeeded = $true
    try {
        $process.StandardInput.Write($inputSql)
        $process.StandardInput.Close()
        [void]$process.StandardOutput.ReadToEnd()
        $errorText = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            $succeeded = $false
            $safeError = ($errorText -replace "\r?\n", " ").Trim()
            if ($safeError.Length -gt 500) {
                $safeError = $safeError.Substring(0, 500)
            }
            $failures.Add([ordered]@{
                key = $Key
                error = $safeError
            })
        }
    } finally {
        if (-not $process.HasExited) {
            $process.Kill($true)
        }
        $process.Dispose()
    }
    return $succeeded
}

try {
    if (Test-PreparedStatement -Key "runtime-account-connection" -SqlText "SELECT 1") {
        foreach ($statement in $statements) {
            $key = [string]$statement.key
            $sqlPath = Join-Path $packRoot "$key.sql"
            if (-not (Test-Path -LiteralPath $sqlPath -PathType Leaf)) {
                $failures.Add([ordered]@{
                    key = $key
                    error = "Generated MariaDB Runtime Query SQL이 없습니다."
                })
                continue
            }
            $sqlText = [System.IO.File]::ReadAllText(
                $sqlPath,
                [System.Text.Encoding]::UTF8).Trim()
            if (Test-PreparedStatement -Key $key -SqlText $sqlText) {
                $preparedCount++
            }
        }
    }
} finally {
    $runtimePassword = $null
}

$result = [ordered]@{
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    vendor = "mariadb"
    profile = [string]$profile.profileName
    database = $databaseName
    verification = "SERVER_SIDE_PREPARE"
    contractStatements = $statements.Count
    preparedStatements = $preparedCount
    failures = @($failures)
    startedAt = $startedAt.ToString("o")
    finishedAt = [DateTimeOffset]::Now.ToString("o")
    secretPersisted = $false
}
$json = $result | ConvertTo-Json -Depth 10
if (-not [string]::IsNullOrWhiteSpace($EvidencePath)) {
    $evidenceDirectory = Split-Path -Parent $EvidencePath
    if (-not [string]::IsNullOrWhiteSpace($evidenceDirectory)) {
        [System.IO.Directory]::CreateDirectory($evidenceDirectory) | Out-Null
    }
    [System.IO.File]::WriteAllText(
        $EvidencePath,
        $json + "`n",
        [System.Text.UTF8Encoding]::new($false))
}
$json
if ($failures.Count -gt 0) {
    throw "MariaDB BAT Runtime Query Pack server-side prepare failed: $($failures.Count)"
}
