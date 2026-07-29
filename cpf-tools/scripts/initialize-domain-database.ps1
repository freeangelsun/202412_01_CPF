param(
    [Parameter(Mandatory = $true)]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $DatabaseVendor = "",
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = "",
    [string] $DatabaseHost = "",
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $SchemaName = "",
    [string] $DatabaseUsername = "",
    [string] $DatabasePassword = "",
    [string] $RuntimeUsername = "",
    [string] $RuntimePassword = "",
    [string] $AdminUsername = "",
    [string] $AdminPassword = "",
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

. (Join-Path $PSScriptRoot "database-profile-common.ps1")
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $projectDir "deploy/database/database-profile.json"
} elseif (-not [System.IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
    throw "Generated Domain DB Profile이 없습니다: $ProfilePath"
}
$dbProfile = Get-CpfDomainDatabaseProfile $ProfilePath
$allowDevDefault = ([string]$dbProfile.environment).ToLowerInvariant() -in @("development", "dev", "local")

if ([string]::IsNullOrWhiteSpace($DatabaseVendor)) { $DatabaseVendor = [string]$dbProfile.database.vendor }
$vendor = if ([string]::IsNullOrWhiteSpace($DatabaseVendor)) {
    ([string]$manifest.databaseVendor).ToLowerInvariant()
} else {
    $DatabaseVendor.Trim().ToLowerInvariant()
}
$vendor = Assert-CpfSupportedDatabaseVendor $vendor
if ([string]::IsNullOrWhiteSpace($DatabaseHost)) { $DatabaseHost = [string]$dbProfile.database.host }
if ($DatabasePort -le 0) { $DatabasePort = [int]$dbProfile.database.port }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) { $DatabaseName = [string]$dbProfile.database.databaseName }
if ([string]::IsNullOrWhiteSpace($SchemaName)) { $SchemaName = [string]$dbProfile.database.schemaName }
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = [string]$dbProfile.database.migration.username }
if ([string]::IsNullOrWhiteSpace($RuntimeUsername)) { $RuntimeUsername = [string]$dbProfile.database.runtime.username }
if ([string]::IsNullOrWhiteSpace($AdminUsername)) { $AdminUsername = [string]$dbProfile.database.admin.username }
if ([string]::IsNullOrWhiteSpace($DatabasePassword)) {
    $DatabasePassword = Resolve-CpfProfileSecret $dbProfile.database.migration.password "$domain.migration.password" $allowDevDefault
}
if ([string]::IsNullOrWhiteSpace($RuntimePassword)) {
    $RuntimePassword = Resolve-CpfProfileSecret $dbProfile.database.runtime.password "$domain.runtime.password" $allowDevDefault
}
if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    $AdminPassword = Resolve-CpfProfileSecret $dbProfile.database.admin.password "$domain.admin.password" $allowDevDefault
}
if ([string]::IsNullOrWhiteSpace($ClientPath)) { $ClientPath = [string]$dbProfile.database.clientPath }

if (-not [bool]$manifest.databaseEnabled) {
    throw "DB capability를 선택하지 않은 주제영역은 DB 초기화를 수행할 수 없습니다."
}
$manifestCode = ([string]$manifest.systemCode).ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
        $manifestCode -ne $SystemCode.Trim().ToUpperInvariant()) {
    throw "요청 SystemCode와 manifest의 SystemCode가 다릅니다."
}

$vendorDefaults = @{
    mariadb = [ordered]@{ port = 3306; adminDatabase = ""; clients = @('mariadb', 'mysql') }
    postgresql = [ordered]@{ port = 5432; adminDatabase = "postgres"; clients = @('psql') }
    oracle = [ordered]@{ port = 1521; database = 'FREEPDB1'; adminDatabase = 'FREEPDB1'; clients = @('sqlplus') }
}
if (-not $vendorDefaults.ContainsKey($vendor)) {
    throw "지원하지 않는 DB Vendor입니다: $vendor"
}
$defaults = $vendorDefaults[$vendor]
if ($DatabasePort -le 0) { $DatabasePort = [int]$defaults.port }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $DatabaseName = if ($vendor -eq 'oracle') { [string]$defaults.database } else { [string]$manifest.schemaName }
}
if ([string]::IsNullOrWhiteSpace($SchemaName)) { $SchemaName = [string]$manifest.schemaName }
if ([string]::IsNullOrWhiteSpace($DatabaseName) -or [string]::IsNullOrWhiteSpace($SchemaName)) {
    throw "DatabaseName과 SchemaName이 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = "cpf_${domain}_migration" }
if ([string]::IsNullOrWhiteSpace($RuntimeUsername)) { $RuntimeUsername = "cpf_${domain}_app" }
if ([string]::IsNullOrWhiteSpace($AdminUsername)) { $AdminUsername = $DatabaseUsername }
if ([string]::IsNullOrWhiteSpace($AdminPassword)) { $AdminPassword = $DatabasePassword }

Assert-CpfDbIdentifier $DatabaseName "databaseName"
Assert-CpfDbIdentifier $SchemaName "schemaName"
Assert-CpfDbUsername $DatabaseUsername "migration.username"
Assert-CpfDbUsername $RuntimeUsername "runtime.username"
Assert-CpfDbUsername $AdminUsername "admin.username"
if ($DatabaseUsername.Equals($RuntimeUsername, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Generated Domain migration 계정과 runtime 계정은 서로 달라야 합니다."
}
if ($vendor -eq "mariadb" -and
        -not $DatabaseName.Equals($SchemaName, [StringComparison]::OrdinalIgnoreCase)) {
    throw "MariaDB는 DatabaseName과 SchemaName이 동일해야 합니다."
}
if ($vendor -eq "oracle" -and
        -not $SchemaName.Equals($DatabaseUsername, [StringComparison]::OrdinalIgnoreCase)) {
    throw "Oracle Generated Domain의 SchemaName은 migration 계정명과 동일해야 합니다."
}
foreach ($secretEntry in @(
        [ordered]@{ name = "migration.password"; value = $DatabasePassword },
        [ordered]@{ name = "runtime.password"; value = $RuntimePassword },
        [ordered]@{ name = "admin.password"; value = $AdminPassword })) {
    if ([string]::IsNullOrWhiteSpace([string]$secretEntry.value)) {
        throw "$($secretEntry.name) Secret이 비어 있습니다."
    }
    if ([string]$secretEntry.value -match "[`r`n]") {
        throw "$($secretEntry.name) Secret에는 줄바꿈을 사용할 수 없습니다."
    }
}
if ($vendor -eq "oracle" -and
        ($DatabasePassword.Contains('"') -or $RuntimePassword.Contains('"'))) {
    throw "Oracle migration/runtime 비밀번호에는 큰따옴표를 사용할 수 없습니다."
}

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
    principals = "provision/02_principals.sql.template"
    install = "install/10_empty_install.sql.template"
    seed = "seed/20_product_seed.sql.template"
    migration = "migration/V1____DOMAIN___domain.sql.template"
    verify = "verify/90_verify.sql.template"
    rollback = "rollback/R1__remove___DOMAIN___domain.sql.template"
}
$phaseOrder = switch ($Operation) {
    "bootstrap" { @("provision", "principals", "install", "seed", "verify") }
    "migration" { @("migration", "verify") }
    "verify" { @("verify") }
    "rollback" { @("rollback") }
}

function ConvertTo-HexUtf8 {
    param([Parameter(Mandatory = $true)][string] $Value)
    return ([BitConverter]::ToString([Text.Encoding]::UTF8.GetBytes($Value))).Replace("-", "")
}

function Render-DomainTemplate {
    param(
        [Parameter(Mandatory = $true)][string] $TemplateText,
        [switch] $IncludeSecrets
    )

    $rendered = $TemplateText.
            Replace("@CPF_VENDOR@", $vendor).
            Replace("@CPF_DOMAIN@", $domain).
            Replace("@CPF_SYSTEM_CODE@", $manifestCode).
            Replace("@CPF_DISPLAY_NAME@", [string]$manifest.displayName).
            Replace("@CPF_SCHEMA_NAME@", $SchemaName).
            Replace("@CPF_DATABASE_NAME@", $DatabaseName).
            Replace("@CPF_MIGRATION_USERNAME@", $DatabaseUsername).
            Replace("@CPF_RUNTIME_USERNAME@", $RuntimeUsername).
            Replace("@CPF_MODULE_NAME@", [string]$manifest.moduleName).
            Replace("@CPF_PACKAGE_NAME@", [string]$manifest.packageName).
            Replace("@CPF_TABLE_PREFIX@", [string]$manifest.tablePrefix).
            Replace("@CPF_MAPPER_NAMESPACE@", "").
            Replace("@CPF_MAPPER_NAME@", "")

    if ($IncludeSecrets) {
        return $rendered.
                Replace("@CPF_MIGRATION_PASSWORD_HEX@", (ConvertTo-HexUtf8 $DatabasePassword)).
                Replace("@CPF_RUNTIME_PASSWORD_HEX@", (ConvertTo-HexUtf8 $RuntimePassword)).
                Replace("@CPF_MIGRATION_PASSWORD_SQL_LITERAL@", $DatabasePassword.Replace("'", "''")).
                Replace("@CPF_RUNTIME_PASSWORD_SQL_LITERAL@", $RuntimePassword.Replace("'", "''"))
    }

    return $rendered.
            Replace("@CPF_MIGRATION_PASSWORD_HEX@", "__CPF_SECRET_REDACTED__").
            Replace("@CPF_RUNTIME_PASSWORD_HEX@", "__CPF_SECRET_REDACTED__").
            Replace("@CPF_MIGRATION_PASSWORD_SQL_LITERAL@", "__CPF_SECRET_REDACTED__").
            Replace("@CPF_RUNTIME_PASSWORD_SQL_LITERAL@", "__CPF_SECRET_REDACTED__")
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
    $templateText = [System.IO.File]::ReadAllText($absolutePath, [System.Text.Encoding]::UTF8)
    $containsSecretTokens = $templateText -match "@CPF_(?:MIGRATION|RUNTIME)_PASSWORD_(?:HEX|SQL_LITERAL)@"
    $renderedText = Render-DomainTemplate -TemplateText $templateText
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
        secretBearing = $containsSecretTokens
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
    foreach ($secret in @($DatabasePassword, $RuntimePassword, $AdminPassword)) {
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
        "mariadb" {
            $psi.RedirectStandardInput = $true
            # MariaDB client는 MySQL wire protocol 호환 환경변수도 함께 지원합니다.
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
        default { throw "지원하지 않는 DB Vendor 실행 경로입니다: $vendor" }
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void]$process.Start()

    $inputError = $null
    if ($psi.RedirectStandardInput) {
        try {
            $process.StandardInput.Write($inputText)
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
    }

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if (-not [string]::IsNullOrWhiteSpace($inputError)) {
        $stderr = (($stderr + " " + $inputError).Trim())
    }

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
    schemaName = $SchemaName
    manifestSchemaName = [string]$manifest.schemaName
    tablePrefix = [string]$manifest.tablePrefix
    projectName = $projectName
    databaseVendorProperty = "cpf.db.vendor"
    databaseVendor = $vendor
    databaseHost = $DatabaseHost
    databasePort = $DatabasePort
    databaseName = $DatabaseName
    databaseUsername = $DatabaseUsername
    runtimeUsername = $RuntimeUsername
    adminUsername = $AdminUsername
    profilePath = $ProfilePath
    passwordProvided = -not [string]::IsNullOrWhiteSpace($DatabasePassword)
    runtimePasswordProvided = -not [string]::IsNullOrWhiteSpace($RuntimePassword)
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
        $isProvisionDatabase = $phase -eq "provision"
        $isAdminPhase = $phase -in @("provision", "principals")
        $targetDatabase = if ($isProvisionDatabase) {
            [string]$defaults.adminDatabase
        } else {
            $DatabaseName
        }
        $username = if ($isAdminPhase) { $AdminUsername } else { $DatabaseUsername }
        $password = if ($isAdminPhase) { $AdminPassword } else { $DatabasePassword }

        $ephemeralSecretSqlPath = $null
        try {
            if ([bool]$phaseResult.secretBearing) {
                $templatePath = Join-Path $Root ([string]$phaseResult.templatePath)
                $templateText = [System.IO.File]::ReadAllText(
                        $templatePath,
                        [System.Text.Encoding]::UTF8)
                $secretSql = Render-DomainTemplate -TemplateText $templateText -IncludeSecrets
                $ephemeralSecretSqlPath = [System.IO.Path]::GetTempFileName()
                [System.IO.File]::WriteAllText(
                        $ephemeralSecretSqlPath,
                        $secretSql,
                        $Utf8NoBom)
                $sqlPath = $ephemeralSecretSqlPath
            }

            $execution = Invoke-SqlResource `
                -Client $client `
                -SqlPath $sqlPath `
                -TargetDatabase $targetDatabase `
                -Username $username `
                -Password $password
        } finally {
            if (-not [string]::IsNullOrWhiteSpace($ephemeralSecretSqlPath) -and
                    (Test-Path -LiteralPath $ephemeralSecretSqlPath -PathType Leaf)) {
                Remove-Item -LiteralPath $ephemeralSecretSqlPath -Force
            }
        }
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
