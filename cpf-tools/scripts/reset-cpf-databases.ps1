param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $AdminUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $AdminPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [ValidateSet("", "disabled", "preferred", "required", "verify-full")]
    [string] $SslMode = $env:CPF_DB_SSL_MODE,
    [string] $SslCaPath = $env:CPF_DB_SSL_CA_PATH,
    [string] $ProfilePath = "",
    [string] $ResultDir = "",
    [switch] $Apply,
    [switch] $DropServiceAccounts,
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
$RequiredConfirmation = if ($DropServiceAccounts) {
    "DROP_CPF_SCHEMA_AND_SERVICE_ACCOUNTS"
} else {
    "DROP_CPF_ALLOWLIST_ONLY"
}

if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools\config\database-install.default.json"
}
$profile = $null
$coreProfile = $null
if (Test-Path -LiteralPath $ProfilePath -PathType Leaf) {
    $profile = Get-Content -LiteralPath $ProfilePath -Raw -Encoding UTF8 | ConvertFrom-Json
    $coreProfile = $profile.modules.core
}
if ([string]::IsNullOrWhiteSpace($HostName)) {
    $HostName = if ($null -ne $coreProfile) { [string]$coreProfile.host } else { "127.0.0.1" }
}
if ([string]::IsNullOrWhiteSpace($Port)) {
    $Port = if ($null -ne $coreProfile -and [int]$coreProfile.port -gt 0) {
        [string]$coreProfile.port
    } else {
        "3306"
    }
}
if ([string]::IsNullOrWhiteSpace($AdminUsername)) {
    $AdminUsername = if ($null -ne $coreProfile) { [string]$coreProfile.admin.username } else { "root" }
}
if ([string]::IsNullOrWhiteSpace($ClientPath) -and $null -ne $coreProfile) {
    $ClientPath = [string]$coreProfile.clientPath
}
if ([string]::IsNullOrWhiteSpace($SslMode)) {
    $profileSslMode = if ($null -ne $coreProfile -and $null -ne $coreProfile.PSObject.Properties["sslMode"]) {
        [string]$coreProfile.sslMode
    } else {
        ""
    }
    $SslMode = if ([string]::IsNullOrWhiteSpace($profileSslMode)) { "preferred" } else { $profileSslMode }
}
if ([string]::IsNullOrWhiteSpace($SslCaPath) -and
    $null -ne $coreProfile -and
    $null -ne $coreProfile.PSObject.Properties["sslCaPath"]) {
    $SslCaPath = [string]$coreProfile.sslCaPath
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build\db-reset"
}

$ServiceAccountAllowlist = [System.Collections.Generic.List[object]]::new()
if ($DropServiceAccounts) {
    if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
        throw "Service Account 정리에 필요한 DB Profile이 없습니다: $ProfilePath"
    }
    foreach ($moduleProperty in $profile.modules.PSObject.Properties) {
        $module = $moduleProperty.Value
        foreach ($identityName in @("migration", "runtime")) {
            $identity = $module.$identityName
            if ($null -eq $identity -or [string]::IsNullOrWhiteSpace([string] $identity.username)) {
                continue
            }
            foreach ($userHost in @([string] $identity.userHost, "localhost") |
                    Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
                    Sort-Object -Unique) {
                $ServiceAccountAllowlist.Add([pscustomobject]@{
                    username = [string] $identity.username
                    userHost = $userHost
                })
            }
        }
    }
    # EXS는 더 이상 고정 제품 Module이 아니지만 과거 설치가 남긴 정확한 계정은 Reset에서만 정리합니다.
    foreach ($legacyUser in @("cpf_exs_migration", "cpf_exs_app")) {
        foreach ($userHost in @("%", "localhost")) {
            $ServiceAccountAllowlist.Add([pscustomobject]@{
                username = $legacyUser
                userHost = $userHost
            })
        }
    }
    $ServiceAccountAllowlist = @(
        $ServiceAccountAllowlist |
            Sort-Object username, userHost -Unique
    )
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
            "--default-character-set=utf8mb4",
            "--batch",
            "--raw",
            "--skip-column-names"
    )) {
        [void] $startInfo.ArgumentList.Add($argument)
    }
    switch ($SslMode) {
        "disabled" {
            [void]$startInfo.ArgumentList.Add("--ssl=0")
        }
        "required" {
            [void]$startInfo.ArgumentList.Add("--ssl=1")
        }
        "verify-full" {
            [void]$startInfo.ArgumentList.Add("--ssl=1")
            [void]$startInfo.ArgumentList.Add("--ssl-verify-server-cert")
            if (-not [string]::IsNullOrWhiteSpace($SslCaPath)) {
                if (-not (Test-Path -LiteralPath $SslCaPath -PathType Leaf)) {
                    throw "MariaDB TLS CA 파일이 없습니다: $SslCaPath"
                }
                [void]$startInfo.ArgumentList.Add("--ssl-ca=$SslCaPath")
            }
        }
        "preferred" {
            # MariaDB Client 기본 TLS negotiation.
        }
        default {
            throw "지원하지 않는 MariaDB sslMode입니다: $SslMode"
        }
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
$presentServiceAccounts = @()
$dropServiceAccountStatements = @()
$accountPredicate = ""
if ($DropServiceAccounts) {
    $accountPredicate = @(
        $ServiceAccountAllowlist | ForEach-Object {
            $username = $_.username.Replace("'", "''")
            $userHost = $_.userHost.Replace("'", "''")
            "(User='$username' AND Host='$userHost')"
        }
    ) -join " OR "
    $presentServiceAccounts = @(
        (Invoke-AdminSql @"
SELECT CONCAT(User, '@', Host)
FROM mysql.user
WHERE $accountPredicate
ORDER BY User, Host;
"@) -split "`r?`n" |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )
    $dropServiceAccountStatements = @(
        $ServiceAccountAllowlist | ForEach-Object {
            $username = $_.username.Replace("'", "''")
            $userHost = $_.userHost.Replace("'", "''")
            "DROP USER IF EXISTS '$username'@'$userHost';"
        }
    )
}
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
    dropServiceAccounts = [bool] $DropServiceAccounts
    exactServiceAccountAllowlist = @(
        $ServiceAccountAllowlist | ForEach-Object { "$($_.username)@$($_.userHost)" }
    )
    presentServiceAccounts = $presentServiceAccounts
    backupWarning = if ($DropServiceAccounts) {
        "Apply는 Allowlist Schema의 모든 데이터와 정확히 나열된 CPF Service Account를 삭제합니다. 필요한 Backup/Restore Point를 먼저 확보하세요."
    } else {
        "Apply는 Allowlist Schema의 모든 데이터를 삭제합니다. 필요한 Backup/Restore Point를 먼저 확보하세요."
    }
    statements = $dropStatements
    serviceAccountStatements = $dropServiceAccountStatements
    applied = $false
}

Write-Host "CPF DB reset target: $HostName`:$Port"
Write-Host "Exact allowlist: $($SchemaAllowlist -join ', ')"
Write-Host "Present allowlisted schemas: $($presentSchemas -join ', ')"
Write-Host "Other application schemas protected: $otherSchemaCount"
if ($DropServiceAccounts) {
    Write-Host "Present exact CPF service accounts: $($presentServiceAccounts -join ', ')"
}
Write-Warning $result.backupWarning

if ($Apply) {
    if ($Confirmation -cne $RequiredConfirmation) {
        throw "Apply requires -Confirmation $RequiredConfirmation"
    }
    Invoke-AdminSql (($dropStatements -join [Environment]::NewLine) + [Environment]::NewLine) | Out-Null
    if ($DropServiceAccounts) {
        Invoke-AdminSql (($dropServiceAccountStatements -join [Environment]::NewLine) + [Environment]::NewLine) | Out-Null
    }
    $remaining = Invoke-AdminSql @"
SELECT COUNT(*)
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ($quotedAllowlist);
"@
    if ([int] $remaining.Trim() -ne 0) {
        throw "Allowlisted Schema reset verification failed. remaining=$($remaining.Trim())"
    }
    if ($DropServiceAccounts) {
        $remainingAccounts = Invoke-AdminSql @"
SELECT COUNT(*)
FROM mysql.user
WHERE $accountPredicate;
"@
        if ([int] $remainingAccounts.Trim() -ne 0) {
            throw "Allowlisted CPF Service Account reset verification failed. remaining=$($remainingAccounts.Trim())"
        }
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
