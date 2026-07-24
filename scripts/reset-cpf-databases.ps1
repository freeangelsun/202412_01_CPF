param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $AdminUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $AdminPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $Apply,
    [string] $Confirmation = ""
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$SchemaAllowlist = @(
    "cpfDB",
    "cmnDB",
    "admDB",
    "batDB",
    "refDB",
    "exsDB",
    "mbrDB",
    "bzaDB",
    "accDB"
)
$RequiredConfirmation = "DROP_CPF_ALLOWLIST_ONLY"

if ([string]::IsNullOrWhiteSpace($HostName)) { $HostName = "127.0.0.1" }
if ([string]::IsNullOrWhiteSpace($Port)) { $Port = "3306" }
if ([string]::IsNullOrWhiteSpace($AdminUsername)) { $AdminUsername = "root" }
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build\db-reset"
}

function Find-MariaDbClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        $explicit = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $explicit) { return $explicit.Source }
        if (Test-Path -LiteralPath $ClientPath -PathType Leaf) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
    }
    foreach ($name in @("mariadb", "mysql")) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) { return $command.Source }
    }
    return $null
}

function Invoke-AdminSql([string] $SqlText) {
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $script:MariaDbClient
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardInputEncoding = [System.Text.Encoding]::UTF8
    $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $startInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    foreach ($argument in @(
            "--protocol=tcp",
            "--host=$HostName",
            "--port=$Port",
            "--user=$AdminUsername",
            "--ssl=0",
            "--default-character-set=utf8mb4",
            "--batch",
            "--raw",
            "--skip-column-names"
    )) {
        [void] $startInfo.ArgumentList.Add($argument)
    }
    $startInfo.Environment["MYSQL_PWD"] = $AdminPassword
    $startInfo.Environment["MARIADB_PWD"] = $AdminPassword

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    [void] $process.Start()
    $process.StandardInput.Write($SqlText)
    $process.StandardInput.Close()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) {
        throw "MariaDB reset preflight/apply failed. exitCode=$($process.ExitCode) error=$stderr"
    }
    return $stdout
}

if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    throw "CPF_DB_ROOT_PASSWORD or -AdminPassword is required. Passwordless destructive reset is not allowed."
}

$script:MariaDbClient = Find-MariaDbClient
if ([string]::IsNullOrWhiteSpace($script:MariaDbClient)) {
    throw "mariadb/mysql CLI를 찾을 수 없습니다. CPF_MARIADB_CLI를 설정하세요."
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "cpf-db-reset-result.sanitized.json"
$quotedAllowlist = ($SchemaAllowlist | ForEach-Object { "'$_'" }) -join ", "
$inventorySql = @"
SELECT SCHEMA_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ($quotedAllowlist)
ORDER BY SCHEMA_NAME;
SELECT COUNT(*)
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME NOT IN ($quotedAllowlist)
  AND SCHEMA_NAME NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys');
"@
$inventoryLines = @(
    (Invoke-AdminSql $inventorySql) -split "`r?`n" |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
)
$otherSchemaCount = if ($inventoryLines.Count -gt 0) {
    [int] $inventoryLines[-1]
} else {
    0
}
$presentSchemas = if ($inventoryLines.Count -gt 1) {
    @($inventoryLines[0..($inventoryLines.Count - 2)])
} else {
    @()
}

$dropStatements = @(
    $SchemaAllowlist | ForEach-Object { "DROP DATABASE IF EXISTS ``$_``;" }
)
$result = [ordered]@{
    executedAt = (Get-Date).ToString("o")
    mode = if ($Apply) { "apply" } else { "dry-run" }
    status = "완료"
    target = [ordered]@{
        host = $HostName
        port = $Port
        adminUsername = $AdminUsername
    }
    exactAllowlist = $SchemaAllowlist
    presentAllowlistedSchemas = $presentSchemas
    protectedOtherSchemaCount = $otherSchemaCount
    backupWarning = "Apply는 Allowlist Schema의 모든 데이터를 삭제합니다. 필요한 Backup/Restore Point를 먼저 확보하세요."
    statements = $dropStatements
    applied = $false
}

Write-Host "CPF DB reset target: $HostName`:$Port"
Write-Host "Exact allowlist: $($SchemaAllowlist -join ', ')"
Write-Host "Present allowlisted schemas: $($presentSchemas -join ', ')"
Write-Host "Other application schemas protected: $otherSchemaCount"
Write-Warning $result.backupWarning

if ($Apply) {
    if ($Confirmation -cne $RequiredConfirmation) {
        throw "Apply requires -Confirmation $RequiredConfirmation"
    }
    Invoke-AdminSql (($dropStatements -join [Environment]::NewLine) + [Environment]::NewLine) | Out-Null
    $remaining = Invoke-AdminSql @"
SELECT COUNT(*)
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ($quotedAllowlist);
"@
    if ([int] $remaining.Trim() -ne 0) {
        throw "Allowlisted Schema reset verification failed. remaining=$($remaining.Trim())"
    }
    $result.applied = $true
    Write-Host "CPF exact allowlist reset applied and verified."
} else {
    Write-Host "Dry-run only. No Schema or data was changed."
    Write-Host "Apply requires both -Apply and -Confirmation $RequiredConfirmation."
}

[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 10) + [Environment]::NewLine,
    [System.Text.UTF8Encoding]::new($false))
Write-Host "Sanitized reset result: $resultPath"
