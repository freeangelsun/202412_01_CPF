param(
    [Parameter(Mandatory = $true)]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $DatabaseVendor = "",
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $DatabaseHost = $(if ($env:CPF_DOMAIN_DB_HOST) { $env:CPF_DOMAIN_DB_HOST } else { "localhost" }),
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $DatabaseUsername = $env:CPF_DOMAIN_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_DOMAIN_DB_PASSWORD,
    [string] $AdminUsername = $env:CPF_DOMAIN_DB_ADMIN_USERNAME,
    [string] $AdminPassword = $env:CPF_DOMAIN_DB_ADMIN_PASSWORD,
    [string] $ClientPath = "",
    [string] $TemplateRoot = "",
    [string] $ResultDir = "",
    [ValidateSet("bootstrap", "migration", "verify", "rollback")]
    [string] $Operation = "bootstrap",
    [switch] $Apply,
    [switch] $ConfirmRollback
)

# Vendor 선택은 생성 Java Source를 변경하지 않고 manifest가 가리키는 SQL resource pack만 선택합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

$domain = $DomainName.Trim().ToLowerInvariant()
if ($domain -notmatch '^[a-z][a-z0-9]{1,29}$') {
    throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
}

$projectName = "cpf-$domain"
$projectDir = Join-Path $Root $projectName
$manifestPath = Join-Path $projectDir "manifest/domain-manifest.json"
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "주제영역 manifest가 없습니다: $projectName/manifest/domain-manifest.json"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not [bool]$manifest.databaseEnabled) {
    throw "DB capability를 선택하지 않은 주제영역은 DB 초기화를 수행할 수 없습니다."
}
$manifestCode = ([string]$manifest.systemCode).ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
        $manifestCode -ne $SystemCode.Trim().ToUpperInvariant()) {
    throw "요청 SystemCode와 manifest의 SystemCode가 다릅니다."
}

$vendor = if ([string]::IsNullOrWhiteSpace($DatabaseVendor)) {
    ([string]$manifest.databaseVendor).ToLowerInvariant()
} else {
    $DatabaseVendor.Trim().ToLowerInvariant()
}
$vendorDefaults = @{
    mariadb = [ordered]@{ port = 3306; adminDatabase = ""; clients = @('mariadb', 'mysql') }
    mysql = [ordered]@{ port = 3306; adminDatabase = ""; clients = @('mysql', 'mariadb') }
    postgresql = [ordered]@{ port = 5432; adminDatabase = "postgres"; clients = @('psql') }
    oracle = [ordered]@{ port = 1521; database = 'FREEPDB1'; adminDatabase = 'FREEPDB1'; clients = @('sqlplus') }
    sqlserver = [ordered]@{ port = 1433; adminDatabase = "master"; clients = @('sqlcmd') }
}
if (-not $vendorDefaults.ContainsKey($vendor)) {
    throw "지원하지 않는 DB Vendor입니다: $vendor"
}
$defaults = $vendorDefaults[$vendor]
if ($DatabasePort -le 0) { $DatabasePort = [int]$defaults.port }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $DatabaseName = if ($vendor -eq 'oracle') {
        [string]$defaults.database
    } else {
        [string]$manifest.schemaName
    }
}
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    throw "manifest.schemaName 또는 DatabaseName이 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = "cpf_${domain}_migration" }
if ([string]::IsNullOrWhiteSpace($AdminUsername)) { $AdminUsername = $DatabaseUsername }
if ([string]::IsNullOrWhiteSpace($AdminPassword)) { $AdminPassword = $DatabasePassword }

if ($Operation -eq "rollback" -and (-not $Apply -or -not $ConfirmRollback)) {
    throw "Rollback은 -Apply -ConfirmRollback을 함께 지정해야 합니다."
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/domain-db-init/$domain"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "domain-db-init-result.json"
$logPath = Join-Path $ResultDir "domain-db-init.sanitized.log"
if ([string]::IsNullOrWhiteSpace($TemplateRoot)) {
    $TemplateRoot = Join-Path $Root "cpf-tools/db/vendor"
} elseif (-not [System.IO.Path]::IsPathRooted($TemplateRoot)) {
    $TemplateRoot = Join-Path $Root $TemplateRoot
}
$TemplateRoot = [System.IO.Path]::GetFullPath($TemplateRoot)
$resourceRoot = Join-Path $TemplateRoot "$vendor/domain-template"

$phaseFiles = [ordered]@{
    provision = "provision/01_provision.sql.template"
    install = "install/10_empty_install.sql.template"
    seed = "seed/20_product_seed.sql.template"
    migration = "migration/V1____DOMAIN___domain.sql.template"
    verify = "verify/90_verify.sql.template"
    rollback = "rollback/R1__remove___DOMAIN___domain.sql.template"
}
$phaseOrder = switch ($Operation) {
    "bootstrap" { @("provision", "install", "seed", "verify") }
    "migration" { @("migration", "verify") }
    "verify" { @("verify") }
    "rollback" { @("rollback") }
}

$plannedPhases = @()
$renderedRoot = Join-Path $ResultDir "rendered-sql/$vendor"
New-Item -ItemType Directory -Force -Path $renderedRoot | Out-Null
foreach ($phase in $phaseOrder) {
    $relativePath = [string]$phaseFiles[$phase]
    $absolutePath = Join-Path $resourceRoot $relativePath
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        throw "중앙 Generator Vendor SQL template이 없습니다: $absolutePath"
    }
    $renderedText = [System.IO.File]::ReadAllText($absolutePath, [System.Text.Encoding]::UTF8).
            Replace("@CPF_VENDOR@", $vendor).
             Replace("@CPF_DOMAIN@", $domain).
             Replace("@CPF_SYSTEM_CODE@", $manifestCode).
             Replace("@CPF_DISPLAY_NAME@", [string]$manifest.displayName).
             Replace("@CPF_SCHEMA_NAME@", [string]$manifest.schemaName).
             Replace("@CPF_MODULE_NAME@", [string]$manifest.moduleName).
             Replace("@CPF_PACKAGE_NAME@", [string]$manifest.packageName).
             Replace("@CPF_TABLE_PREFIX@", [string]$manifest.tablePrefix).
            Replace("@CPF_MAPPER_NAMESPACE@", "").
            Replace("@CPF_MAPPER_NAME@", "")
    $renderedFileName = ([System.IO.Path]::GetFileName($relativePath)).
            Replace(".template", "").
            Replace("__VENDOR__", $vendor).
            Replace("__DOMAIN__", $domain)
    $renderedPath = Join-Path $renderedRoot "$phase-$renderedFileName"
    [System.IO.File]::WriteAllText($renderedPath, $renderedText, $Utf8NoBom)
    $plannedPhases += [ordered]@{
        phase = $phase
        templatePath = $absolutePath.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
        renderedPath = $renderedPath.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
        templateSha256 = (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
        renderedSha256 = (Get-FileHash -LiteralPath $renderedPath -Algorithm SHA256).Hash.ToLowerInvariant()
        status = "미검증"
    }
}

function Find-DatabaseClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath -PathType Leaf) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $configured = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $configured) { return $configured.Source }
        throw "지정한 DB CLI를 찾을 수 없습니다: $ClientPath"
    }
    foreach ($name in $defaults.clients) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) { return $command.Source }
    }
    return $null
}

function Add-ProcessArgument {
    param(
        [System.Diagnostics.ProcessStartInfo] $ProcessStartInfo,
        [string] $Value
    )
    [void]$ProcessStartInfo.ArgumentList.Add($Value)
}

function Mask-Text {
    param([string] $Value)
    if ($null -eq $Value) { return "" }
    $safe = $Value
    foreach ($secret in @($DatabasePassword, $AdminPassword)) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $safe = $safe.Replace($secret, '****')
        }
    }
    return $safe
}

function Invoke-SqlResource {
    param(
        [string] $Client,
        [string] $SqlPath,
        [string] $TargetDatabase,
        [string] $Username,
        [string] $Password
    )
    if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
        throw "DB 실행 계정과 비밀번호가 필요합니다."
    }
    if ($Username -match '[\r\n]' -or $Password -match '[\r\n]') {
        throw "DB 계정과 비밀번호에는 줄바꿈을 사용할 수 없습니다."
    }

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $Client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    $inputText = $null

    switch ($vendor) {
        { $_ -in @("mariadb", "mysql") } {
            $psi.RedirectStandardInput = $true
            $psi.EnvironmentVariables['MYSQL_PWD'] = $Password
            $psi.EnvironmentVariables['MARIADB_PWD'] = $Password
            foreach ($argument in @(
                    '--protocol=tcp', '-h', $DatabaseHost, '-P', [string]$DatabasePort,
                    '-u', $Username, '--default-character-set=utf8mb4', '--batch', '--raw')) {
                Add-ProcessArgument $psi $argument
            }
            if (-not [string]::IsNullOrWhiteSpace($TargetDatabase)) {
                Add-ProcessArgument $psi "--database=$TargetDatabase"
            }
            $inputText = [System.IO.File]::ReadAllText($SqlPath, [System.Text.Encoding]::UTF8)
        }
        "postgresql" {
            $psi.EnvironmentVariables['PGPASSWORD'] = $Password
            foreach ($argument in @(
                    '-X', '-v', 'ON_ERROR_STOP=1', '-h', $DatabaseHost, '-p', [string]$DatabasePort,
                    '-U', $Username, '-d', $TargetDatabase, '-f', $SqlPath)) {
                Add-ProcessArgument $psi $argument
            }
        }
        "oracle" {
            if ($Username -match '["/@]' -or $Password -match '"') {
                throw "Oracle 계정과 비밀번호에는 큰따옴표, 슬래시, @를 사용할 수 없습니다."
            }
            $psi.RedirectStandardInput = $true
            Add-ProcessArgument $psi '-S'
            Add-ProcessArgument $psi '/nolog'
            $inputText = "WHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT $Username/`"$Password`"@$DatabaseHost`:$DatabasePort/$TargetDatabase`n" +
                    [System.IO.File]::ReadAllText($SqlPath, [System.Text.Encoding]::UTF8) +
                    "`nEXIT SUCCESS`n"
        }
        "sqlserver" {
            $psi.EnvironmentVariables['SQLCMDPASSWORD'] = $Password
            foreach ($argument in @(
                    '-S', "$DatabaseHost,$DatabasePort", '-U', $Username, '-d', $TargetDatabase,
                    '-i', $SqlPath, '-b', '-C', '-f', '65001')) {
                Add-ProcessArgument $psi $argument
            }
        }
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void]$process.Start()
    if ($psi.RedirectStandardInput) {
        $process.StandardInput.Write($inputText)
        $process.StandardInput.Close()
    }
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    return [ordered]@{
        exitCode = $process.ExitCode
        stdout = Mask-Text $stdout
        stderr = Mask-Text $stderr
    }
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    status = "미검증"
    applied = [bool]$Apply
    operation = $Operation
    domainName = $domain
    systemCode = $manifestCode
    moduleName = [string]$manifest.moduleName
    packageName = [string]$manifest.packageName
    schemaName = [string]$manifest.schemaName
    tablePrefix = [string]$manifest.tablePrefix
    projectName = $projectName
    databaseVendorProperty = "cpf.db.vendor"
    databaseVendor = $vendor
    databaseHost = $DatabaseHost
    databasePort = $DatabasePort
    databaseName = $DatabaseName
    databaseUsername = $DatabaseUsername
    adminUsername = $AdminUsername
    passwordProvided = -not [string]::IsNullOrWhiteSpace($DatabasePassword)
    adminPasswordProvided = -not [string]::IsNullOrWhiteSpace($AdminPassword)
    phases = $plannedPhases
    centralDomainTemplate = "cpf-tools/db/vendor/$vendor/domain-template"
    runtimeResourceTemplate = "cpf-tools/db/vendor/$vendor/domain-template/runtime"
    client = $null
    logPath = $logPath
}

try {
    if (-not $Apply) {
        [System.IO.File]::WriteAllText(
            $logPath,
            "DB $Operation 계획 검증 완료: 실제 SQL은 실행하지 않았습니다.`n",
            $Utf8NoBom)
        Write-Host "domain DB plan ready. project=$projectName vendor=$vendor operation=$Operation result=$resultPath"
        return
    }

    $client = Find-DatabaseClient
    if ([string]::IsNullOrWhiteSpace($client)) {
        throw "DB Vendor용 CLI를 찾지 못했습니다. vendor=$vendor"
    }
    $result.client = [System.IO.Path]::GetFileName($client)
    $safeLog = New-Object System.Text.StringBuilder

    foreach ($phaseResult in $result.phases) {
        $phase = [string]$phaseResult.phase
        $sqlPath = Join-Path $Root ([string]$phaseResult.renderedPath)
        $isProvision = $phase -eq "provision"
        $targetDatabase = if ($isProvision) { [string]$defaults.adminDatabase } else { $DatabaseName }
        $username = if ($isProvision) { $AdminUsername } else { $DatabaseUsername }
        $password = if ($isProvision) { $AdminPassword } else { $DatabasePassword }

        $execution = Invoke-SqlResource `
            -Client $client `
            -SqlPath $sqlPath `
            -TargetDatabase $targetDatabase `
            -Username $username `
            -Password $password
        $phaseResult.exitCode = $execution.exitCode
        [void]$safeLog.AppendLine("[$phase] STDOUT")
        [void]$safeLog.AppendLine($execution.stdout)
        [void]$safeLog.AppendLine("[$phase] STDERR")
        [void]$safeLog.AppendLine($execution.stderr)
        if ($execution.exitCode -ne 0) {
            $phaseResult.status = "실패"
            throw "DB SQL 실행이 실패했습니다. phase=$phase, vendor=$vendor, exitCode=$($execution.exitCode)"
        }
        if ($phase -eq "verify" -and $execution.stdout -match '(?i)\bFAILED\b') {
            $phaseResult.status = "실패"
            throw "DB Verify SQL이 실패 상태를 반환했습니다. vendor=$vendor"
        }
        $phaseResult.status = "완료"
    }
    [System.IO.File]::WriteAllText($logPath, $safeLog.ToString(), $Utf8NoBom)
    $result.status = "완료"
} catch {
    $result.status = "실패"
    $result.failure = Mask-Text $_.Exception.Message
    throw
} finally {
    $result.finishedAt = (Get-Date).ToString('o')
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

Write-Host "domain DB operation completed. project=$projectName vendor=$vendor operation=$Operation result=$resultPath"
