param(
    [Parameter(Mandatory = $true)]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $DefinitionPath = "",
    [string] $DatabaseVendor = "",
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ProfilePath = "",
    [string] $DatabaseHost = "",
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $SchemaName = "",
    [string] $DatabaseUsername = "",
    [string] $DatabasePassword = "",
    [string] $ClientPath = "",
    [string] $TemplateRoot = "",
    [string] $ResultDir = "",
    [ValidateSet("bootstrap", "migration", "verify", "rollback")]
    [string] $Operation = "bootstrap",
    [switch] $Apply,
    [switch] $ConfirmRollback
)

# Vendor 선택은 Generated Java Source를 변경하지 않고 중앙 Vendor resource pack만 선택합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root "cpf-tools/generator/tools/generated-domain-common.ps1")

$domain = $DomainName.Trim().ToLowerInvariant()
if ($domain -notmatch '^[a-z][a-z0-9]{1,29}$') {
    throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
}

$projectName = "cpf-$domain"
$definition = Get-CpfGeneratedDomainDefinition `
        -Root $Root `
        -DomainName $domain `
        -DefinitionPath $DefinitionPath `
        -IncludeMissing
if ([string]$definition.databaseRole -ne "CUSTOMER_BUSINESS_DB") {
    throw "Generated Domain canonical definition은 database.role=CUSTOMER_BUSINESS_DB여야 합니다."
}

. (Join-Path $Root "cpf-tools/db/tools/database-profile-common.ps1")
$dbProfile = $null
if (-not [string]::IsNullOrWhiteSpace($ProfilePath) -and
        -not [System.IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
if (-not [string]::IsNullOrWhiteSpace($ProfilePath)) {
    if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
        throw "외부 Generated Domain DB Runtime Profile이 없습니다: $ProfilePath"
    }
    $dbProfile = Get-CpfDomainDatabaseProfile $ProfilePath
}
$allowDevDefault = $null -ne $dbProfile -and
        ([string]$dbProfile.environment).ToLowerInvariant() -in @("development", "dev", "local")

if ([string]::IsNullOrWhiteSpace($DatabaseVendor) -and $null -ne $dbProfile) {
    $DatabaseVendor = [string]$dbProfile.database.vendor
}
$vendor = $DatabaseVendor.Trim().ToLowerInvariant()
if ([string]::IsNullOrWhiteSpace($vendor)) {
    throw "DB Vendor는 Generated Domain Source metadata가 아니라 -DatabaseVendor 또는 외부 -ProfilePath로 선택해야 합니다."
}
$vendor = Assert-CpfSupportedDatabaseVendor $vendor
if ($null -ne $dbProfile -and [string]::IsNullOrWhiteSpace($DatabaseHost)) { $DatabaseHost = [string]$dbProfile.database.host }
if ($null -ne $dbProfile -and $DatabasePort -le 0) { $DatabasePort = [int]$dbProfile.database.port }
if ($null -ne $dbProfile -and [string]::IsNullOrWhiteSpace($DatabaseName)) { $DatabaseName = [string]$dbProfile.database.databaseName }
if ($null -ne $dbProfile -and [string]::IsNullOrWhiteSpace($SchemaName)) { $SchemaName = [string]$dbProfile.database.schemaName }
if ($null -ne $dbProfile -and [string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = [string]$dbProfile.database.migration.username }
if ($null -ne $dbProfile -and $Apply -and [string]::IsNullOrWhiteSpace($DatabasePassword)) {
    $DatabasePassword = Resolve-CpfProfileSecret $dbProfile.database.migration.password "$domain.migration.password" $allowDevDefault
}
if ($null -ne $dbProfile -and [string]::IsNullOrWhiteSpace($ClientPath)) { $ClientPath = [string]$dbProfile.database.clientPath }

if (-not [bool]$definition.databaseEnabled) {
    throw "DB capability를 선택하지 않은 주제영역은 Customer Business DB lifecycle을 수행할 수 없습니다."
}
$manifestCode = ([string]$definition.systemCode).ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
        $manifestCode -ne $SystemCode.Trim().ToUpperInvariant()) {
    throw "요청 SystemCode와 canonical definition의 SystemCode가 다릅니다."
}

$vendorDefaults = @{
    mariadb = [ordered]@{ port = 3306; clients = @('mariadb') }
    postgresql = [ordered]@{ port = 5432; clients = @('psql') }
    oracle = [ordered]@{ port = 1521; database = 'FREEPDB1'; clients = @('sqlplus') }
}
if (-not $vendorDefaults.ContainsKey($vendor)) {
    throw "지원하지 않는 DB Vendor입니다: $vendor"
}
$defaults = $vendorDefaults[$vendor]
if ($DatabasePort -le 0) { $DatabasePort = [int]$defaults.port }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $DatabaseName = if ($vendor -eq 'oracle') { [string]$defaults.database } else { "" }
}
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    throw "NXT2는 Domain별 물리 DB를 생성하지 않습니다. 기존 CUSTOMER_BUSINESS_DB의 DatabaseName을 profile/인자로 지정해야 합니다."
}
# SchemaName은 연결 선택값일 뿐 Domain 이름에서 생성하지 않습니다. 빈 값이면 현재 connection default schema를 사용합니다.
if ($null -eq $SchemaName) { $SchemaName = "" }
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = "cpf_${domain}_migration" }

Assert-CpfDbIdentifier $DatabaseName "databaseName"
if (-not [string]::IsNullOrWhiteSpace($SchemaName)) { Assert-CpfDbIdentifier $SchemaName "schemaName" }
Assert-CpfDbUsername $DatabaseUsername "migration.username"
if ($Apply -and [string]::IsNullOrWhiteSpace($DatabasePassword)) {
    throw "migration.password Secret이 비어 있습니다."
}
if (-not [string]::IsNullOrWhiteSpace($DatabasePassword) -and $DatabasePassword -match "[`r`n]") {
    throw "migration.password Secret에는 줄바꿈을 사용할 수 없습니다."
}
if ($vendor -eq "oracle" -and $DatabasePassword.Contains('"')) {
    throw "Oracle migration 비밀번호에는 큰따옴표를 사용할 수 없습니다."
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
    $TemplateRoot = Join-Path $Root "cpf-tools/db/generated/domain-template"
} elseif (-not [System.IO.Path]::IsPathRooted($TemplateRoot)) {
    $TemplateRoot = Join-Path $Root $TemplateRoot
}
$TemplateRoot = [System.IO.Path]::GetFullPath($TemplateRoot)
$resourceRoot = Join-Path $TemplateRoot $vendor
if (-not (Test-Path -LiteralPath $resourceRoot -PathType Container)) {
    throw "중앙 Generated Domain Vendor template root가 없습니다: $resourceRoot"
}

$phaseFiles = [ordered]@{
    # NXT2: Generated Domain lifecycle never creates a physical DB or service principal.
    install = "install/10_empty_install.sql.template"
    seed = "seed/20_product_seed.sql.template"
    migration = "migration/V1____DOMAIN___domain.sql.template"
    verify = "verify/90_verify.sql.template"
    rollback = "rollback/R1__remove___DOMAIN___domain.sql.template"
}
$expectedTemplatePaths = @($phaseFiles.Values | ForEach-Object { [string]$_ } | Sort-Object -Unique)
$templateManifestPath = Join-Path $resourceRoot "manifest.json"
if (-not (Test-Path -LiteralPath $templateManifestPath -PathType Leaf)) {
    throw "중앙 Generated Domain Vendor template manifest가 없습니다: $templateManifestPath"
}
$templateManifest = Get-Content -LiteralPath $templateManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
if ([int]$templateManifest.schemaVersion -ne 1 -or
        [string]$templateManifest.vendor -ne $vendor -or
        -not [bool]$templateManifest.generated -or
        [string]$templateManifest.canonicalSource -ne "cpf-tools/db/canonical/generated-domain-schema.json" -or
        [string]$templateManifest.businessDatabaseRole -ne "CUSTOMER_BUSINESS_DB") {
    throw "중앙 Generated Domain Vendor template manifest identity가 올바르지 않습니다: $templateManifestPath"
}
$manifestTemplatePaths = @($templateManifest.artifacts.PSObject.Properties.Name | Sort-Object -Unique)
if (@(Compare-Object $expectedTemplatePaths $manifestTemplatePaths).Count -ne 0) {
    throw "중앙 Generated Domain Vendor template manifest는 canonical 5개 artifact만 가져야 합니다: vendor=$vendor expected=$($expectedTemplatePaths -join ',') actual=$($manifestTemplatePaths -join ',')"
}
$actualTemplatePaths = @(
    Get-ChildItem -LiteralPath $resourceRoot -Recurse -File -Filter "*.template" |
        ForEach-Object { $_.FullName.Substring($resourceRoot.Length + 1).Replace('\', '/') } |
        Sort-Object -Unique
)
if (@(Compare-Object $expectedTemplatePaths $actualTemplatePaths).Count -ne 0) {
    throw "중앙 Generated Domain Vendor template directory는 canonical 5개 artifact만 가져야 합니다: vendor=$vendor expected=$($expectedTemplatePaths -join ',') actual=$($actualTemplatePaths -join ',')"
}
foreach ($relativePath in $expectedTemplatePaths) {
    $absolutePath = Join-Path $resourceRoot ($relativePath -replace '/', '\')
    $recordedHash = [string]($templateManifest.artifacts.PSObject.Properties |
            Where-Object Name -eq $relativePath |
            Select-Object -ExpandProperty Value)
    $actualHash = (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($recordedHash -notmatch '^[0-9a-f]{64}$' -or $actualHash -ne $recordedHash) {
        throw "중앙 Generated Domain Vendor template hash가 manifest와 다릅니다: vendor=$vendor artifact=$relativePath expected=$recordedHash actual=$actualHash"
    }
    $templateText = [IO.File]::ReadAllText($absolutePath, [Text.Encoding]::UTF8)
    if ($templateText -match '@CPF_(?:MIGRATION|RUNTIME|ADMIN)_(?:USERNAME|PASSWORD)' -or
            $templateText -match '(?i)\b(?:CREATE|ALTER|DROP)\s+(?:USER|DATABASE|SCHEMA)\b') {
        throw "Generated Domain template에 외부 Provision/Principal 책임이 포함되어 있습니다: vendor=$vendor artifact=$relativePath"
    }
}
$templateManifestSha256 = (Get-FileHash -LiteralPath $templateManifestPath -Algorithm SHA256).Hash.ToLowerInvariant()
$phaseOrder = switch ($Operation) {
    "bootstrap" { @("install", "seed", "verify") }
    "migration" { @("migration", "verify") }
    "verify" { @("verify") }
    "rollback" { @("rollback") }
}

function Render-DomainTemplate {
    param([Parameter(Mandatory = $true)][string] $TemplateText)

    $rendered = $TemplateText.
            Replace("@CPF_VENDOR@", $vendor).
            Replace("@CPF_DOMAIN@", $domain).
            Replace("@CPF_SYSTEM_CODE@", $manifestCode).
            Replace("@CPF_DISPLAY_NAME@", [string]$definition.className).
            Replace("@CPF_MODULE_NAME@", [string]$definition.moduleName).
            Replace("@CPF_PACKAGE_NAME@", [string]$definition.packageName).
            Replace("@CPF_TABLE_PREFIX@", [string]$definition.tablePrefix).
            Replace("@CPF_MAPPER_NAMESPACE@", "").
            Replace("@CPF_MAPPER_NAME@", "")
    $unresolved = @([regex]::Matches($rendered, '@[A-Z][A-Z0-9_]*@') | ForEach-Object Value | Sort-Object -Unique)
    if ($unresolved.Count -gt 0) {
        throw "Generated Domain SQL template에 해석되지 않은 token이 있습니다: $($unresolved -join ',')"
    }
    return $rendered
}

function Get-RecordedPath([string] $AbsolutePath) {
    $absolute = [IO.Path]::GetFullPath($AbsolutePath)
    $rootPrefix = $Root.TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if ($absolute.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        return $absolute.Substring($rootPrefix.Length).Replace('\', '/')
    }
    return $absolute
}

function Resolve-RecordedPath([string] $RecordedPath) {
    if ([IO.Path]::IsPathRooted($RecordedPath)) { return [IO.Path]::GetFullPath($RecordedPath) }
    return [IO.Path]::GetFullPath((Join-Path $Root $RecordedPath))
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
    $renderedText = Render-DomainTemplate -TemplateText $templateText
    $renderedFileName = ([System.IO.Path]::GetFileName($relativePath)).
            Replace(".template", "").
            Replace("__VENDOR__", $vendor).
            Replace("__DOMAIN__", $domain)
    $renderedPath = Join-Path $renderedRoot "$phase-$renderedFileName"
    [System.IO.File]::WriteAllText($renderedPath, $renderedText, $Utf8NoBom)
    $plannedPhases += [ordered]@{
        phase = $phase
        templatePath = Get-RecordedPath $absolutePath
        renderedPath = Get-RecordedPath $renderedPath
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
    foreach ($secret in @($DatabasePassword)) {
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
    moduleName = [string]$definition.moduleName
    packageName = [string]$definition.packageName
    businessDatabaseRole = "CUSTOMER_BUSINESS_DB"
    schemaName = $SchemaName
    physicalDatabaseCreated = $false
    tablePrefix = [string]$definition.tablePrefix
    projectName = $projectName
    databaseVendorProperty = "cpf.db.vendor"
    databaseVendor = $vendor
    databaseHost = $DatabaseHost
    databasePort = $DatabasePort
    databaseName = $DatabaseName
    databaseUsername = $DatabaseUsername
    profilePath = $ProfilePath
    definitionPath = [string]$definition.definitionPath
    definitionSha256 = [string]$definition.definitionSha256
    generatedProjectMetadata = "NONE"
    passwordProvided = -not [string]::IsNullOrWhiteSpace($DatabasePassword)
    phases = $plannedPhases
    centralDomainTemplate = Get-RecordedPath $resourceRoot
    templateManifestPath = Get-RecordedPath $templateManifestPath
    templateManifestSha256 = $templateManifestSha256
    templateArtifactCount = $expectedTemplatePaths.Count
    runtimeResourceTemplate = "cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect/$vendor"
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
        $sqlPath = Resolve-RecordedPath ([string]$phaseResult.renderedPath)
        $execution = Invoke-SqlResource `
            -Client $client `
            -SqlPath $sqlPath `
            -TargetDatabase $DatabaseName `
            -Username $DatabaseUsername `
            -Password $DatabasePassword
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
