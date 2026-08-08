param(
    [string] $DomainName = "",
    [string] $SystemCode = "",
    [string] $ModuleCode = "",
    [string] $Root = "",
    [string] $ModuleName = "",
    [string] $DomainIdCode = "",
    [string] $PackageName = "",
    [string] $BasePackage = "",
    [string] $SchemaName = "",
    [string] $TablePrefix = "",
    [ValidateRange(1024, 65535)]
    [int] $Port = 8080,
    [ValidateSet("Y", "N")]
    [string] $Online = "N",
    [ValidateSet("Y", "N")]
    [string] $Database = "N",
    [Alias("DbVendor")]
    [ValidateSet("mariadb", "postgresql", "oracle")]
    [string] $DatabaseVendor = "mariadb",
    [string] $DatabaseHost = "127.0.0.1",
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $DatabaseSchema = "",
    [string] $DatabaseAdminUsername = "",
    [string] $DatabaseMigrationUsername = "",
    [string] $DatabaseRuntimeUsername = "",
    [string] $DatabaseClientPath = "",
    [ValidateSet("root-project", "published-artifact")]
    [string] $DependencyModel = "root-project",
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [string] $CapabilityProfile = "minimal-domain",
    [string] $ProviderBindings = "",
    [string] $Capabilities = "",
    [string] $ApprovedExceptionRegistry = "",
    [string] $TargetEnvironment = "",
    [string] $UpgradeSourceDomainPath = "",
    [ValidateSet("Y", "N")]
    [string] $Batch = "N",
    [ValidateSet("Y", "N")]
    [string] $CenterCut = "N",
    [ValidateSet("Y", "N")]
    [string] $External = "N",
    [ValidateSet("Y", "N")]
    [string] $Messaging = "N",
    [ValidateSet("Y", "N")]
    [string] $File = "N",
    [ValidateSet("Y", "N")]
    [string] $SecurityAudit = "N",
    [ValidateSet("Y", "N")]
    [string] $Ui = "N",
    [ValidateSet("Y", "N")]
    [string] $BzaMenu = "N",
    [ValidateSet("Y", "N")]
    [string] $ProductionProfile = "N",
    [switch] $ProvisionDatabase,
    [string] $OutputDir = "",
    [switch] $DryRun,
    [switch] $GeneratePatch,
    [switch] $Apply,
    [switch] $AllowReserved
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
} else {
    $Root = (Resolve-Path -LiteralPath $Root).Path
}
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$cpfStackPropertiesPath = Join-Path $Root "gradle/cpf-stack.properties"
if (-not (Test-Path -LiteralPath $cpfStackPropertiesPath -PathType Leaf)) {
    throw "CPF Stack 정본이 없습니다: $cpfStackPropertiesPath"
}
$cpfStackProperties = @{}
foreach ($line in Get-Content -LiteralPath $cpfStackPropertiesPath -Encoding UTF8) {
    $trimmed = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
    $index = $trimmed.IndexOf('=')
    if ($index -le 0) { continue }
    $cpfStackProperties[$trimmed.Substring(0, $index).Trim()] = $trimmed.Substring($index + 1).Trim()
}
$springBootVersion = [string]$cpfStackProperties['springBootVersion']
$dependencyManagementVersion = [string]$cpfStackProperties['springDependencyManagementVersion']
if ([string]::IsNullOrWhiteSpace($springBootVersion) -or [string]::IsNullOrWhiteSpace($dependencyManagementVersion)) {
    throw "CPF Stack 정본의 Spring plugin version이 유효하지 않습니다: $cpfStackPropertiesPath"
}
$centralTemplateContractPath = Join-Path $Root "cpf-tools/generator/contracts/central-domain-template-contract.json"
if (-not (Test-Path -LiteralPath $centralTemplateContractPath -PathType Leaf)) {
    throw "Generated Domain 중앙 Template 계약이 없습니다: $centralTemplateContractPath"
}
$centralTemplateContract = Get-Content -LiteralPath $centralTemplateContractPath -Raw -Encoding UTF8 |
        ConvertFrom-Json
$supportedDatabaseVendors = @($centralTemplateContract.supportedVendors |
        ForEach-Object { ([string]$_).Trim().ToLowerInvariant() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        Sort-Object -Unique)
if ($supportedDatabaseVendors.Count -ne 3) {
    throw "Generated Domain 중앙 Template의 지원 Vendor 계약이 유효하지 않습니다."
}
$physicalTableContract = $centralTemplateContract.physicalTableContract
$minimalDomainModel = [string]$centralTemplateContract.verifyContract.model
$minimalTableRole = [string]$centralTemplateContract.verifyContract.tableRole
$minimalTableTemplate = [string]$centralTemplateContract.verifyContract.requiredTable
$minimalLogicalTableTemplate = [string]$centralTemplateContract.verifyContract.logicalTable
$minimalRequiredColumns = @($centralTemplateContract.verifyContract.requiredColumns)
$minimalTransactionIdWidth = [int]$centralTemplateContract.verifyContract.transactionIdWidth
$minimalRequiredKeys = @($centralTemplateContract.verifyContract.requiredKeys)
$minimalRequiredIndexes = @($centralTemplateContract.verifyContract.requiredIndexes)
$minimalRequiredChecks = @($centralTemplateContract.verifyContract.requiredChecks)
$minimalRequiredOperations = @($centralTemplateContract.verifyContract.requiredOperations)
$idempotencyLedgerModel = [string]$centralTemplateContract.idempotencyLedgerContract.model
$idempotencyLedgerTableRole = [string]$centralTemplateContract.idempotencyLedgerContract.tableRole
$idempotencyLedgerTableTemplate = [string]$centralTemplateContract.idempotencyLedgerContract.requiredTable
$idempotencyLedgerLogicalTableTemplate = [string]$centralTemplateContract.idempotencyLedgerContract.logicalTable
$idempotencyLedgerRequiredColumns = @($centralTemplateContract.idempotencyLedgerContract.requiredColumns)
$idempotencyLedgerTransactionIdWidth = [int]$centralTemplateContract.idempotencyLedgerContract.transactionIdWidth
$idempotencyLedgerRequiredKeys = @($centralTemplateContract.idempotencyLedgerContract.requiredKeys)
$idempotencyLedgerRequiredIndexes = @($centralTemplateContract.idempotencyLedgerContract.requiredIndexes)
$idempotencyLedgerRequiredChecks = @($centralTemplateContract.idempotencyLedgerContract.requiredChecks)
$idempotencyLedgerReplayPolicy = [string]$centralTemplateContract.idempotencyLedgerContract.replayPolicy
$idempotencyLedgerLogicalDeleteReplayRequired =
        [bool]$centralTemplateContract.idempotencyLedgerContract.logicalDeleteReplayRequired
$supportedCapabilities = @($centralTemplateContract.capabilityContract.supported |
        ForEach-Object { ([string]$_).Trim().ToLowerInvariant() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
        Sort-Object -Unique)
$buildRuntimeContract = $centralTemplateContract.buildRuntimeContract
$runtimeAgentContract = $centralTemplateContract.runtimeAgentContract
$runtimeAgentRequiredEnvironment = @($runtimeAgentContract.requiredEnvironment)
if ($null -eq $runtimeAgentContract -or
        [string]$runtimeAgentContract.manifestProperty -ne "runtimeAgent" -or
        [string]$runtimeAgentContract.activationProfile -ne "runtime-agent" -or
        [string]$runtimeAgentContract.applicationConfig -ne "src/main/resources/application-runtime-agent.yml" -or
        [string]$runtimeAgentContract.deploymentDescriptor -ne "deploy/runtime/runtime-agent.json" -or
        [string]$runtimeAgentContract.capabilityDiscovery -ne "CpfRuntimeChangeApplier" -or
        $runtimeAgentRequiredEnvironment.Count -ne 6 -or
        -not [bool]$runtimeAgentContract.failClosed -or
        [bool]$runtimeAgentContract.enabledByDefault) {
    throw "Generated Domain Runtime Agent 중앙 계약이 유효하지 않습니다."
}
if ($null -eq $physicalTableContract -or
        [int]$physicalTableContract.totalTables -ne 2 -or
        [int]$physicalTableContract.businessTableCount -ne 1 -or
        [int]$physicalTableContract.supportLedgerCount -ne 1 -or
        [int]$physicalTableContract.totalTables -ne
                ([int]$physicalTableContract.businessTableCount + [int]$physicalTableContract.supportLedgerCount) -or
        [bool]$physicalTableContract.additionalTablesAllowed -or
        [string]::IsNullOrWhiteSpace($minimalDomainModel) -or
        $minimalTableRole -ne "business-sample" -or
        [string]::IsNullOrWhiteSpace($minimalTableTemplate) -or
        [string]::IsNullOrWhiteSpace($minimalLogicalTableTemplate) -or
        $minimalRequiredColumns.Count -ne 14 -or
        $minimalTransactionIdWidth -ne 34 -or
        $minimalRequiredKeys.Count -ne 2 -or
        $minimalRequiredIndexes.Count -ne 3 -or
        $minimalRequiredChecks.Count -ne 2 -or
        $minimalRequiredOperations.Count -ne 22 -or
        [string]::IsNullOrWhiteSpace($idempotencyLedgerModel) -or
        $idempotencyLedgerTableRole -ne "non-business-support-ledger" -or
        [string]::IsNullOrWhiteSpace($idempotencyLedgerTableTemplate) -or
        [string]::IsNullOrWhiteSpace($idempotencyLedgerLogicalTableTemplate) -or
        $idempotencyLedgerRequiredColumns.Count -ne 8 -or
        $idempotencyLedgerTransactionIdWidth -ne 34 -or
        $idempotencyLedgerRequiredKeys.Count -ne 2 -or
        $idempotencyLedgerRequiredIndexes.Count -ne 2 -or
        $idempotencyLedgerRequiredChecks.Count -ne 2 -or
        [string]::IsNullOrWhiteSpace($idempotencyLedgerReplayPolicy) -or
        -not $idempotencyLedgerLogicalDeleteReplayRequired -or
        $supportedCapabilities.Count -ne 11) {
    throw "Generated Domain Minimal Transaction 중앙 계약이 유효하지 않습니다."
}
$jdbcDriverMapLines = [System.Collections.Generic.List[string]]::new()
$flywayDatabaseMapLines = [System.Collections.Generic.List[string]]::new()
foreach ($supportedVendor in $supportedDatabaseVendors) {
    $vendorDependencyProperty = $buildRuntimeContract.vendors.PSObject.Properties[$supportedVendor]
    if ($null -eq $vendorDependencyProperty) {
        throw "Generated Domain Build Runtime Vendor 계약이 없습니다: $supportedVendor"
    }
    $jdbcDriver = [string]$vendorDependencyProperty.Value.jdbcDriver
    $flywayDatabase = [string]$vendorDependencyProperty.Value.flywayDatabase
    if ([string]::IsNullOrWhiteSpace($jdbcDriver) -or
            [string]::IsNullOrWhiteSpace($flywayDatabase)) {
        throw "Generated Domain Build Runtime 의존성 계약이 유효하지 않습니다: $supportedVendor"
    }
    $jdbcDriverMapLines.Add("        '$supportedVendor': '$jdbcDriver'")
    $flywayDatabaseMapLines.Add("        '$supportedVendor': '$flywayDatabase'")
}
$supportedDatabaseVendorsGradle = (
    $supportedDatabaseVendors | ForEach-Object { "'$_'" }
) -join ", "
$jdbcDriverMapGradle = $jdbcDriverMapLines -join ",`n"
$flywayDatabaseMapGradle = $flywayDatabaseMapLines -join ",`n"
$protectedLegacyBatchSource = [IO.Path]::GetFullPath((Join-Path $Root "cpf-batch/src")).
        TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)

function Test-IsProtectedRepositoryPath {
    param([string] $Path)
    $candidate = [IO.Path]::GetFullPath($Path)
    return $candidate.Equals($protectedLegacyBatchSource, [StringComparison]::OrdinalIgnoreCase) -or
            $candidate.StartsWith(
                    $protectedLegacyBatchSource + [IO.Path]::DirectorySeparatorChar,
                    [StringComparison]::OrdinalIgnoreCase)
}

function New-StatusText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}


$capabilityProfileCatalogPath = Join-Path $Root "cpf-tools/generator/contracts/capability-profiles.json"
if (-not (Test-Path -LiteralPath $capabilityProfileCatalogPath -PathType Leaf)) {
    throw "CPF Capability Profile 정본이 없습니다: $capabilityProfileCatalogPath"
}
$capabilityProfileCatalog = Get-Content -LiteralPath $capabilityProfileCatalogPath -Raw -Encoding UTF8 | ConvertFrom-Json
$starterCatalogPath = Join-Path $Root "cpf-tools/generator/contracts/cpf-starter-catalog.json"
if (-not (Test-Path -LiteralPath $starterCatalogPath -PathType Leaf)) {
    throw "CPF Starter Catalog 정본이 없습니다: $starterCatalogPath"
}
$starterCatalog = Get-Content -LiteralPath $starterCatalogPath -Raw -Encoding UTF8 | ConvertFrom-Json
$requiredPublicProfiles = @("minimal-domain", "web-api", "secure-api", "browser-bff", "event-service", "batch-service")
$requiredCapabilityGroups = @("data", "messaging", "integration", "file", "notification", "security", "platform-operations")
$catalogPublicProfiles = @($starterCatalog.publicProfiles | ForEach-Object { [string]$_ })
$catalogCapabilityGroups = @($starterCatalog.capabilityGroups | ForEach-Object { [string]$_.id })
if (($catalogPublicProfiles -join ',') -ne ($requiredPublicProfiles -join ',')) {
    throw "CPF 공개 Profile 정본이 QA39 6개 Profile과 일치하지 않습니다: $($catalogPublicProfiles -join ',')"
}
if (($catalogCapabilityGroups -join ',') -ne ($requiredCapabilityGroups -join ',')) {
    throw "CPF Capability Group 정본이 QA39 7개 Group과 일치하지 않습니다: $($catalogCapabilityGroups -join ',')"
}
$profileSelector = $CapabilityProfile.Trim()
$profileSelectorNormalized = $profileSelector.Replace('_', '-').ToLowerInvariant()
$resolvedCapabilityProfile = @($capabilityProfileCatalog.profiles | Where-Object {
    ([string]$_.profileId).Equals($profileSelector, [StringComparison]::OrdinalIgnoreCase) -or
    ([string]$_.publicName).Equals($profileSelectorNormalized, [StringComparison]::OrdinalIgnoreCase)
})
if ($resolvedCapabilityProfile.Count -ne 1) {
    throw "지원하지 않거나 중복된 CapabilityProfile입니다: $CapabilityProfile. 지원값=$($requiredPublicProfiles -join ',')"
}
$resolvedCapabilityProfile = $resolvedCapabilityProfile[0]
$CapabilityProfile = [string]$resolvedCapabilityProfile.publicName
$resolvedStarters = @($resolvedCapabilityProfile.resolvedStarters | ForEach-Object { [string]$_ })
if ($resolvedStarters.Count -eq 0) {
    throw "CapabilityProfile에 resolvedStarters가 없습니다: $CapabilityProfile"
}
$resolvedProviderBindings = [ordered]@{}
$resolvedProviderProjects = [System.Collections.Generic.List[string]]::new()
$resolvedProviderCoordinates = [System.Collections.Generic.List[string]]::new()
if (-not [string]::IsNullOrWhiteSpace($ProviderBindings)) {
    foreach ($pair in ($ProviderBindings -split ',')) {
        $parts = $pair.Split('=', 2)
        if ($parts.Count -ne 2 -or [string]::IsNullOrWhiteSpace($parts[0]) -or [string]::IsNullOrWhiteSpace($parts[1])) {
            throw "ProviderBindings 형식은 capability=provider[,..] 입니다: $pair"
        }
        $capability = $parts[0].Trim().ToLowerInvariant()
        $provider = $parts[1].Trim().ToLowerInvariant()
        if ($resolvedProviderBindings.Contains($capability)) {
            throw "Provider Binding 중복: $capability"
        }
        $allowedProperty = $resolvedCapabilityProfile.allowedProviderBindings.PSObject.Properties[$capability]
        if ($null -eq $allowedProperty) {
            throw "Profile에서 지원하지 않는 Provider Binding slot입니다: $capability"
        }
        $allowed = @($allowedProperty.Value | ForEach-Object { [string]$_ })
        if ($allowed -notcontains $provider) {
            throw "Profile에서 허용하지 않는 Provider Binding: $capability=$provider"
        }
        $slotProperty = $capabilityProfileCatalog.providerSlots.PSObject.Properties[$capability]
        if ($null -eq $slotProperty) {
            throw "Provider slot 정본이 없습니다: $capability"
        }
        $providerProperty = $slotProperty.Value.PSObject.Properties[$provider]
        if ($null -eq $providerProperty) {
            throw "Provider 정본이 없습니다: $capability=$provider"
        }
        $projectPath = [string]$providerProperty.Value.projectPath
        $coordinate = [string]$providerProperty.Value.coordinate
        if ([string]::IsNullOrWhiteSpace($projectPath) -or [string]::IsNullOrWhiteSpace($coordinate)) {
            throw "Provider dependency 정본이 유효하지 않습니다: $capability=$provider"
        }
        $resolvedProviderBindings[$capability] = $provider
        $resolvedProviderProjects.Add($projectPath)
        $resolvedProviderCoordinates.Add($coordinate)
        $resolvedStarters += $coordinate.Substring($coordinate.LastIndexOf(':') + 1)
    }
}
$requiredProviderBindings = @($resolvedCapabilityProfile.requiredProviderBindings | ForEach-Object { [string]$_ })
$missingProviderBindings = @($requiredProviderBindings | Where-Object { -not $resolvedProviderBindings.Contains($_) })
if ($missingProviderBindings.Count -gt 0) {
    throw "Profile에 필수 Provider Binding이 없습니다: $($missingProviderBindings -join ',')"
}
$resolvedStarters = @($resolvedStarters | Sort-Object -Unique)

$StatusDone = New-StatusText @(0xC644, 0xB8CC)
$StatusFailed = New-StatusText @(0xC2E4, 0xD328)

function Normalize-DomainName {
    param([string] $Value)
    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "DomainName은 필수입니다. 이전 호환 입력인 ModuleCode도 사용할 수 있습니다."
    }
    $normalized = $Value.Trim().ToLowerInvariant()
    if ($normalized -notmatch '^[a-z][a-z0-9]{1,29}$') {
        throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
    }
    return $normalized
}

function ConvertTo-ClassName {
    param([string] $Value)
    $segments = @($Value -split '[-_]' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    return -join ($segments | ForEach-Object {
            $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1).ToLowerInvariant()
        })
}

function Write-Utf8 {
    param(
        [string] $Path,
        [string] $Content
    )
    $parent = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    [System.IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Get-Sha256Hex {
    param([byte[]] $Bytes)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($sha.ComputeHash($Bytes))).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Get-ApprovedExceptionConfigHash {
    param(
        [pscustomobject] $Row,
        [string] $RegistryRoot
    )
    $hashFields = @(
        'exception_id','module','capability','artifact','version','owner','reason',
        'standard_path_gap','environments','security_impact','license_review',
        'supply_chain_review','operations_responsibility','approved_by','approved_at',
        'expires_at','rollback','return_plan','rule_ids','config_files','evidence_path','status'
    )
    $buffer = [IO.MemoryStream]::new()
    try {
        $canonical = @($hashFields | ForEach-Object {
            ([string]$Row.$_).Trim().Replace("`r`n", "`n").Replace("`r", "`n")
        }) -join [char]31
        $metadata = $Utf8NoBom.GetBytes($canonical)
        $buffer.Write($metadata, 0, $metadata.Length)
        $buffer.WriteByte(0)
        foreach ($relativeValue in @(([string]$Row.config_files) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ } | Sort-Object -Unique)) {
            if ([IO.Path]::IsPathRooted($relativeValue) -or $relativeValue -match '(^|[\\/])\.\.([\\/]|$)') {
                throw "승인 예외 Config 경로는 Registry 상대경로여야 합니다: $relativeValue"
            }
            $source = [IO.Path]::GetFullPath((Join-Path $RegistryRoot $relativeValue))
            $registryFull = [IO.Path]::GetFullPath($RegistryRoot).TrimEnd('\\','/') + [IO.Path]::DirectorySeparatorChar
            if (-not $source.StartsWith($registryFull, [StringComparison]::OrdinalIgnoreCase) -or
                    -not (Test-Path -LiteralPath $source -PathType Leaf)) {
                throw "승인 예외 Config가 Registry 범위 안에 존재하지 않습니다: $relativeValue"
            }
            $pathBytes = $Utf8NoBom.GetBytes($relativeValue.Replace('\\','/'))
            $contentBytes = [IO.File]::ReadAllBytes($source)
            $buffer.Write($pathBytes, 0, $pathBytes.Length)
            $buffer.WriteByte(0)
            $buffer.Write($contentBytes, 0, $contentBytes.Length)
            $buffer.WriteByte(0)
        }
        return Get-Sha256Hex -Bytes $buffer.ToArray()
    } finally {
        $buffer.Dispose()
    }
}

function ConvertTo-CsvCell {
    param([string] $Value)
    if ($null -eq $Value) { return '' }
    $escaped = $Value.Replace('"', '""')
    if ($escaped.Contains(',') -or $escaped.Contains('"') -or $escaped.Contains("`n") -or $escaped.Contains("`r")) {
        return '"' + $escaped + '"'
    }
    return $escaped
}

function Test-TextExists {
    param(
        [string] $Path,
        [string] $Text
    )
    if (-not (Test-Path -LiteralPath $Path)) {
        return $false
    }
    $content = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    return $content.IndexOf($Text, [System.StringComparison]::OrdinalIgnoreCase) -ge 0
}

$requestedDomainName = if ([string]::IsNullOrWhiteSpace($DomainName)) { $ModuleCode } else { $DomainName }
$module = Normalize-DomainName $requestedDomainName
$projectName = "cpf-$module"

$approvedExceptionFields = @(
    'exception_id','module','capability','artifact','version','owner','reason',
    'standard_path_gap','environments','security_impact','license_review',
    'supply_chain_review','operations_responsibility','approved_by','approved_at',
    'expires_at','rollback','return_plan','rule_ids','config_files','evidence_path','status','config_hash'
)
$approvedExceptionRows = @()
$approvedExceptionEvidenceFiles = [ordered]@{}
$approvedExceptionConfigFiles = [ordered]@{}
$approvedExceptionCsv = ($approvedExceptionFields -join ',') + "`n"
if (-not [string]::IsNullOrWhiteSpace($ApprovedExceptionRegistry)) {
    $approvedExceptionRegistryPath = if ([IO.Path]::IsPathRooted($ApprovedExceptionRegistry)) {
        [IO.Path]::GetFullPath($ApprovedExceptionRegistry)
    } else {
        [IO.Path]::GetFullPath((Join-Path $Root $ApprovedExceptionRegistry))
    }
    if (-not (Test-Path -LiteralPath $approvedExceptionRegistryPath -PathType Leaf)) {
        throw "승인 예외 Registry가 없습니다: $approvedExceptionRegistryPath"
    }
    $registryHeader = @((Get-Content -LiteralPath $approvedExceptionRegistryPath -Encoding UTF8 -TotalCount 1).TrimStart([char]0xFEFF).Split(','))
    if (($registryHeader -join ',') -ne ($approvedExceptionFields -join ',')) {
        throw "승인 예외 Registry Header가 CPF 정본과 일치하지 않습니다."
    }
    $registryRoot = Split-Path -Parent $approvedExceptionRegistryPath
    $seenExceptionIds = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($row in @(Import-Csv -LiteralPath $approvedExceptionRegistryPath -Encoding UTF8)) {
        foreach ($field in $approvedExceptionFields) {
            if ([string]::IsNullOrWhiteSpace([string]$row.$field)) {
                throw "승인 예외 필드가 비어 있습니다: field=$field, exception=$($row.exception_id)"
            }
        }
        if (-not $seenExceptionIds.Add([string]$row.exception_id)) {
            throw "승인 예외 ID가 중복되었습니다: $($row.exception_id)"
        }
        if ([string]$row.module -notin @($projectName, $module)) {
            throw "승인 예외 module이 생성 Domain과 일치하지 않습니다: $($row.exception_id), module=$($row.module)"
        }
        if ([string]$row.capability -notin @('data','messaging','integration','file','notification','security','platform-operations')) {
            throw "승인 예외 Capability가 유효하지 않습니다: $($row.exception_id), capability=$($row.capability)"
        }
        if ([string]$row.artifact -notmatch '^[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+$') {
            throw "승인 예외 Artifact는 group:name 정확한 좌표여야 합니다: $($row.exception_id)"
        }
        if ([string]$row.status -cne 'APPROVED') {
            throw "승인 예외 상태가 APPROVED가 아닙니다: $($row.exception_id)"
        }
        $approvedAt = [DateTimeOffset]::Parse([string]$row.approved_at)
        $expiresAt = [DateTimeOffset]::Parse([string]$row.expires_at)
        if ($approvedAt -gt [DateTimeOffset]::UtcNow -or $expiresAt -le [DateTimeOffset]::UtcNow) {
            throw "승인 예외 승인시각 또는 만료일이 유효하지 않습니다: $($row.exception_id)"
        }
        $ruleIds = @(([string]$row.rule_ids) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        $configFiles = @(([string]$row.config_files) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        if ($ruleIds.Count -eq 0 -or $configFiles.Count -eq 0) {
            throw "승인 예외 rule_ids/config_files가 비어 있습니다: $($row.exception_id)"
        }
        foreach ($configRelative in $configFiles) {
            $normalizedConfigRelative = $configRelative.Replace('\\','/')
            if (-not $normalizedConfigRelative.StartsWith('src/main/resources/', [StringComparison]::Ordinal) -or
                    [IO.Path]::IsPathRooted($configRelative) -or $configRelative -match '(^|[\\/])\.\.([\\/]|$)') {
                throw "승인 예외 Config는 이용 Domain의 src/main/resources 아래 상대경로여야 합니다: $configRelative"
            }
            $configSource = [IO.Path]::GetFullPath((Join-Path $registryRoot $configRelative))
            $registryFull = [IO.Path]::GetFullPath($registryRoot).TrimEnd('\\','/') + [IO.Path]::DirectorySeparatorChar
            if (-not $configSource.StartsWith($registryFull, [StringComparison]::OrdinalIgnoreCase) -or
                    -not (Test-Path -LiteralPath $configSource -PathType Leaf)) {
                throw "승인 예외 Config가 Registry 범위 안에 존재하지 않습니다: $configRelative"
            }
            $approvedExceptionConfigFiles[$configRelative.Replace('\\','/')] = $configSource
        }
        $calculatedHash = Get-ApprovedExceptionConfigHash -Row $row -RegistryRoot $registryRoot
        if ($calculatedHash -cne ([string]$row.config_hash).ToLowerInvariant()) {
            throw "승인 예외 Config Hash가 일치하지 않습니다: $($row.exception_id)"
        }
        $evidenceSource = [IO.Path]::GetFullPath((Join-Path $registryRoot ([string]$row.evidence_path)))
        if (-not $evidenceSource.StartsWith([IO.Path]::GetFullPath($registryRoot), [StringComparison]::OrdinalIgnoreCase) -or
                -not (Test-Path -LiteralPath $evidenceSource -PathType Leaf)) {
            throw "승인 예외 Evidence가 Registry 범위 안에 존재하지 않습니다: $($row.exception_id)"
        }
        $approvedExceptionEvidenceFiles[([string]$row.evidence_path).Replace('\\','/')] = Get-Content -LiteralPath $evidenceSource -Raw -Encoding UTF8
        $approvedExceptionRows += $row
    }
    $csvLines = [System.Collections.Generic.List[string]]::new()
    [void]$csvLines.Add(($approvedExceptionFields -join ','))
    foreach ($row in $approvedExceptionRows) {
        [void]$csvLines.Add((@($approvedExceptionFields | ForEach-Object { ConvertTo-CsvCell ([string]$row.$_) }) -join ','))
    }
    $approvedExceptionCsv = ($csvLines -join "`n") + "`n"
}
$TargetEnvironment = $TargetEnvironment.Trim()
if ($approvedExceptionRows.Count -gt 0) {
    if ([string]::IsNullOrWhiteSpace($TargetEnvironment) -or
            $TargetEnvironment -notmatch '^[A-Za-z0-9][A-Za-z0-9_.-]{0,63}$') {
        throw "승인 예외가 있으면 TargetEnvironment를 명시해야 합니다."
    }
    foreach ($exception in $approvedExceptionRows) {
        $allowedEnvironments = @(([string]$exception.environments) -split ';' |
                ForEach-Object { $_.Trim() } | Where-Object { $_ })
        if ($TargetEnvironment -notin $allowedEnvironments) {
            throw "TargetEnvironment가 승인 예외 범위를 벗어났습니다: exception=$($exception.exception_id), target=$TargetEnvironment"
        }
    }
} elseif (-not [string]::IsNullOrWhiteSpace($TargetEnvironment)) {
    throw "TargetEnvironment는 승인 예외 Registry가 있을 때만 지정합니다."
}
$Dollar = '$'
if ([string]::IsNullOrWhiteSpace($SystemCode)) {
    $SystemCode = $DomainIdCode
}
if ([string]::IsNullOrWhiteSpace($SystemCode)) {
    $SystemCode = if ($module.Length -ge 3) {
        $module.Substring(0, 3).ToUpperInvariant()
    } else {
        $module.ToUpperInvariant().PadRight(3, 'X')
    }
}
$SystemCode = $SystemCode.Trim().ToUpperInvariant()
if ($SystemCode -notmatch '^[A-Z][A-Z0-9]{2}$') {
    throw "SystemCode는 영문자로 시작하는 정확히 3자리 영문 대문자·숫자여야 합니다."
}
$DomainIdCode = $SystemCode
$ModuleUpper = $SystemCode

if ([string]::IsNullOrWhiteSpace($ModuleName)) {
    $ModuleName = ConvertTo-ClassName $module
}
if ($ModuleName -notmatch '^[A-Z][A-Za-z0-9]{1,49}$') {
    throw "ModuleName은 영문 대문자로 시작하는 2~50자리 Java class 이름이어야 합니다."
}
if (-not [string]::IsNullOrWhiteSpace($PackageName) -and
        -not [string]::IsNullOrWhiteSpace($BasePackage) -and
        $PackageName.Trim() -ne $BasePackage.Trim()) {
    throw "PackageName과 이전 호환 입력 BasePackage가 서로 다릅니다."
}
if ([string]::IsNullOrWhiteSpace($PackageName)) {
    $PackageName = $BasePackage
}
if ([string]::IsNullOrWhiteSpace($PackageName)) {
    $PackageName = "com.cpf.$module"
}
$PackageName = $PackageName.Trim()
if ($PackageName -notmatch '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$') {
    throw "PackageName은 com.cpf 하위의 유효한 Java package여야 합니다."
}
# BasePackage는 이전 호출자와 생성 source template을 위한 호환 변수입니다.
$BasePackage = $PackageName

if ([string]::IsNullOrWhiteSpace($TablePrefix)) {
    $TablePrefix = $SystemCode.ToLowerInvariant()
}
$TablePrefix = $TablePrefix.Trim().ToLowerInvariant()
if ($TablePrefix -notmatch '^[a-z][a-z0-9_]{1,19}$') {
    throw "TablePrefix는 영문 소문자로 시작하는 2~20자리 영문 소문자·숫자·밑줄이어야 합니다."
}
if ([string]::IsNullOrWhiteSpace($SchemaName)) {
    $SchemaName = "${TablePrefix}DB"
}
$SchemaName = $SchemaName.Trim()
if ($SchemaName -notmatch '^[A-Za-z][A-Za-z0-9_]{1,29}$') {
    throw "SchemaName은 영문자로 시작하는 2~30자리 영문·숫자·밑줄이어야 합니다."
}
$minimalTableName = $minimalTableTemplate.Replace("@CPF_TABLE_PREFIX@", $TablePrefix)
$minimalLogicalTable = $minimalLogicalTableTemplate.
        Replace("@CPF_SCHEMA_NAME@", $SchemaName).
        Replace("@CPF_TABLE_PREFIX@", $TablePrefix)
$idempotencyLedgerTableName =
        $idempotencyLedgerTableTemplate.Replace("@CPF_TABLE_PREFIX@", $TablePrefix)
$idempotencyLedgerLogicalTable = $idempotencyLedgerLogicalTableTemplate.
        Replace("@CPF_SCHEMA_NAME@", $SchemaName).
        Replace("@CPF_TABLE_PREFIX@", $TablePrefix)
foreach ($renderedContractValue in @(
        $minimalTableName,
        $minimalLogicalTable,
        $idempotencyLedgerTableName,
        $idempotencyLedgerLogicalTable)) {
    if ($renderedContractValue -match '@CPF_[A-Z_]+@') {
        throw "Generated Domain 중앙 Template 계약 token이 완전히 치환되지 않았습니다: $renderedContractValue"
    }
}
$supportedDatabaseVendorsJson = $supportedDatabaseVendors | ConvertTo-Json -Compress
$minimalRequiredColumnsJson = $minimalRequiredColumns | ConvertTo-Json -Compress
$minimalRequiredKeysJson = $minimalRequiredKeys | ConvertTo-Json -Compress
$minimalRequiredIndexesJson = $minimalRequiredIndexes | ConvertTo-Json -Compress
$minimalRequiredChecksJson = $minimalRequiredChecks | ConvertTo-Json -Compress
$minimalRequiredOperationsJson = $minimalRequiredOperations | ConvertTo-Json -Compress
$idempotencyLedgerRequiredColumnsJson =
        $idempotencyLedgerRequiredColumns | ConvertTo-Json -Compress
$idempotencyLedgerRequiredKeysJson =
        $idempotencyLedgerRequiredKeys | ConvertTo-Json -Compress
$idempotencyLedgerRequiredIndexesJson =
        $idempotencyLedgerRequiredIndexes | ConvertTo-Json -Compress
$idempotencyLedgerRequiredChecksJson =
        $idempotencyLedgerRequiredChecks | ConvertTo-Json -Compress
$idempotencyLedgerLogicalDeleteReplayRequiredJson =
        $idempotencyLedgerLogicalDeleteReplayRequired.ToString().ToLowerInvariant()
$OnlineEnabled = $Online -eq "Y"
$DatabaseEnabled = $Database -eq "Y"
$DatabaseVendor = $DatabaseVendor.ToLowerInvariant()
if ($DatabaseVendor -notin $supportedDatabaseVendors) {
    throw "중앙 Template 계약이 지원하지 않는 DatabaseVendor입니다: $DatabaseVendor"
}
$vendorPortDefaults = @{ mariadb = 3306; postgresql = 5432; oracle = 1521 }
if ($DatabasePort -le 0) { $DatabasePort = [int]$vendorPortDefaults[$DatabaseVendor] }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) {
    $DatabaseName = if ($DatabaseVendor -eq "oracle") { "FREEPDB1" } else { $SchemaName }
}
if ([string]::IsNullOrWhiteSpace($DatabaseSchema)) { $DatabaseSchema = $SchemaName }
if ([string]::IsNullOrWhiteSpace($DatabaseAdminUsername)) {
    $DatabaseAdminUsername = if ($DatabaseVendor -eq "postgresql") { "postgres" } elseif ($DatabaseVendor -eq "oracle") { "SYSTEM" } else { "root" }
}
if ([string]::IsNullOrWhiteSpace($DatabaseMigrationUsername)) {
    $DatabaseMigrationUsername = if ($DatabaseVendor -eq "oracle") {
        $SchemaName.ToUpperInvariant()
    } else {
        "cpf_${module}_migration"
    }
}
if ([string]::IsNullOrWhiteSpace($DatabaseRuntimeUsername)) { $DatabaseRuntimeUsername = "cpf_${module}_app" }
$BatchEnabled = $Batch -eq "Y"
$CenterCutEnabled = $CenterCut -eq "Y"
$ExternalEnabled = $External -eq "Y"
$MessagingEnabled = $Messaging -eq "Y"
$FileEnabled = $File -eq "Y"
$SecurityAuditEnabled = $SecurityAudit -eq "Y"
$UiEnabled = $Ui -eq "Y"
$BzaMenuEnabled = $BzaMenu -eq "Y"
$ProductionProfileEnabled = $ProductionProfile -eq "Y"
$PlatformVersion = $PlatformVersion.Trim()
if ([string]::IsNullOrWhiteSpace($PlatformVersion) -or
        $PlatformVersion -notmatch '^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$') {
    throw "PlatformVersion은 비어 있지 않은 유효한 artifact version이어야 합니다."
}

if ($ProvisionDatabase -and -not $Apply) {
    throw "DB 실제 생성은 Repository에 생성 결과를 반영하는 -Apply와 함께 사용해야 합니다."
}
# ProvisionDatabase의 Data 활성 여부 검증은 Profile·Capability·legacy flag를 모두 해석한 뒤 수행합니다.

# QA39 공개 생성 입력은 6개 Profile과 7개 Capability Group뿐입니다.
# Capabilities 미지정 시 Profile 필수 기능만 조립하며 Provider Leaf는 자동 선택하지 않습니다.
$publicCapabilityGroups = @("data", "messaging", "integration", "file", "notification", "security", "platform-operations")
$providerSlotToGroup = @{
    'data' = 'data'; 'cache' = 'data'; 'messaging' = 'messaging';
    'integration-transport' = 'integration'; 'integration-codec' = 'integration';
    'file' = 'file'; 'object-storage' = 'file'; 'notification' = 'notification'; 'observability' = 'platform-operations';
    'locking' = 'data'; 'graphql' = 'integration'; 'realtime' = 'integration'; 'health-registry' = 'platform-operations'; 'security-mode' = 'security'
}
$requestedCapabilityGroups = @()
if (-not [string]::IsNullOrWhiteSpace($Capabilities)) {
    $requestedCapabilityGroups = @($Capabilities -split '[,; ]+' |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            ForEach-Object { $_.Trim().ToLowerInvariant() } |
            Sort-Object -Unique)
    $unknownCapabilityGroups = @($requestedCapabilityGroups | Where-Object { $_ -notin $publicCapabilityGroups })
    if ($unknownCapabilityGroups.Count -gt 0) {
        throw "지원하지 않는 공개 Capability Group입니다: $($unknownCapabilityGroups -join ', '). 지원값=$($publicCapabilityGroups -join ',')"
    }
}
# 이전 Generator 입력은 호환성 경로일 뿐 별도 구현 경로가 아닙니다.
# legacy flag도 동일한 공개 Capability 정본으로 변환하여 Provider·Lock·Gate 정책을 반드시 공유합니다.
$legacyCapabilityGroups = @()
if ($DatabaseEnabled) { $legacyCapabilityGroups += 'data' }
if ($MessagingEnabled) { $legacyCapabilityGroups += 'messaging' }
if ($ExternalEnabled) { $legacyCapabilityGroups += 'integration' }
if ($FileEnabled) { $legacyCapabilityGroups += 'file' }
if ($SecurityAuditEnabled) { $legacyCapabilityGroups += 'security' }
$requestedCapabilityGroups = @($requestedCapabilityGroups + $legacyCapabilityGroups | Sort-Object -Unique)
$mandatoryCapabilityGroups = @($resolvedCapabilityProfile.mandatoryCapabilityGroups | ForEach-Object { [string]$_ })
$allowedCapabilityGroups = @($resolvedCapabilityProfile.allowedCapabilityGroups | ForEach-Object { [string]$_ })
$disallowedCapabilityGroups = @($requestedCapabilityGroups | Where-Object { $_ -notin $allowedCapabilityGroups })
if ($disallowedCapabilityGroups.Count -gt 0) {
    throw "Profile에서 허용하지 않는 Capability Group입니다: $($disallowedCapabilityGroups -join ',')"
}
$resolvedCapabilityGroups = @($mandatoryCapabilityGroups + $requestedCapabilityGroups | Sort-Object -Unique)

# Capability 간 선행 의존성은 동일 Canonical Catalog에서 계산합니다.
# 예: Messaging/Notification/Platform Operations의 영속 원장은 Data Capability를 요구합니다.
do {
    $capabilityExpanded = $false
    foreach ($group in @($resolvedCapabilityGroups)) {
        $compositionProperty = $capabilityProfileCatalog.capabilityComposition.PSObject.Properties[$group]
        if ($null -eq $compositionProperty) {
            throw "Capability Composition 정본이 없습니다: $group"
        }
        foreach ($requiredCapability in @($compositionProperty.Value.requiresCapabilities | ForEach-Object { [string]$_ })) {
            if ($requiredCapability -notin $resolvedCapabilityGroups) {
                $resolvedCapabilityGroups = @($resolvedCapabilityGroups + $requiredCapability | Sort-Object -Unique)
                $capabilityExpanded = $true
            }
        }
    }
} while ($capabilityExpanded)

# Profile이 내장하는 상호 배타 Provider는 lock에 기록하되 사용자가 다시 선택하지 않게 기본 binding으로 해석합니다.
foreach ($defaultBinding in @($resolvedCapabilityProfile.defaultProviderBindings.PSObject.Properties)) {
    $slot = [string]$defaultBinding.Name
    $provider = [string]$defaultBinding.Value
    $requiredGroup = [string]$providerSlotToGroup[$slot]
    if ($requiredGroup -notin $resolvedCapabilityGroups -or $resolvedProviderBindings.Contains($slot)) { continue }
    $slotProperty = $capabilityProfileCatalog.providerSlots.PSObject.Properties[$slot]
    $providerProperty = if ($null -eq $slotProperty) { $null } else { $slotProperty.Value.PSObject.Properties[$provider] }
    if ($null -eq $providerProperty) {
        throw "Profile 기본 Provider 정본이 없습니다: $CapabilityProfile/$slot=$provider"
    }
    $resolvedProviderBindings[$slot] = $provider
    $projectPath = [string]$providerProperty.Value.projectPath
    $coordinate = [string]$providerProperty.Value.coordinate
    if (-not $resolvedProviderProjects.Contains($projectPath)) { $resolvedProviderProjects.Add($projectPath) }
    if (-not $resolvedProviderCoordinates.Contains($coordinate)) { $resolvedProviderCoordinates.Add($coordinate) }
    $resolvedStarters += $coordinate.Substring($coordinate.LastIndexOf(':') + 1)
}

# 선택된 Capability의 공통 Runtime만 조립합니다. 미선택 Capability의 Runtime/Provider는 절대 추가하지 않습니다.
foreach ($group in @($resolvedCapabilityGroups)) {
    $composition = $capabilityProfileCatalog.capabilityComposition.PSObject.Properties[$group].Value
    foreach ($runtimeProject in @($composition.runtimeProjects | ForEach-Object { [string]$_ })) {
        if (-not $resolvedProviderProjects.Contains($runtimeProject)) {
            $resolvedProviderProjects.Add($runtimeProject)
        }
    }
    foreach ($runtimeCoordinate in @($composition.runtimeCoordinates | ForEach-Object { [string]$_ })) {
        if (-not $resolvedProviderCoordinates.Contains($runtimeCoordinate)) {
            $resolvedProviderCoordinates.Add($runtimeCoordinate)
        }
        $resolvedStarters += $runtimeCoordinate.Substring($runtimeCoordinate.LastIndexOf(':') + 1)
    }
    foreach ($defaultBinding in @($composition.defaultProviderBindings.PSObject.Properties)) {
        $slot = [string]$defaultBinding.Name
        $provider = [string]$defaultBinding.Value
        if ($resolvedProviderBindings.Contains($slot)) { continue }
        $slotProperty = $capabilityProfileCatalog.providerSlots.PSObject.Properties[$slot]
        $providerProperty = if ($null -eq $slotProperty) { $null } else { $slotProperty.Value.PSObject.Properties[$provider] }
        if ($null -eq $providerProperty) {
            throw "Capability 기본 Provider 정본이 없습니다: $group/$slot=$provider"
        }
        $projectPath = [string]$providerProperty.Value.projectPath
        $coordinate = [string]$providerProperty.Value.coordinate
        $resolvedProviderBindings[$slot] = $provider
        if (-not $resolvedProviderProjects.Contains($projectPath)) { $resolvedProviderProjects.Add($projectPath) }
        if (-not $resolvedProviderCoordinates.Contains($coordinate)) { $resolvedProviderCoordinates.Add($coordinate) }
        $resolvedStarters += $coordinate.Substring($coordinate.LastIndexOf(':') + 1)
    }
    foreach ($requiredSlot in @($composition.requiredProviderSlots | ForEach-Object { [string]$_ })) {
        if (-not $resolvedProviderBindings.Contains($requiredSlot)) {
            throw "선택한 Capability에 필수 Provider Binding이 없습니다: capability=$group, slot=$requiredSlot"
        }
    }
}

$DatabaseEnabled = $DatabaseEnabled -or ('data' -in $resolvedCapabilityGroups)
$BatchEnabled = $BatchEnabled -or $CapabilityProfile -eq 'batch-service'
$ExternalEnabled = $ExternalEnabled -or ('integration' -in $resolvedCapabilityGroups)
$MessagingEnabled = $MessagingEnabled -or ('messaging' -in $resolvedCapabilityGroups)
$FileEnabled = $FileEnabled -or ('file' -in $resolvedCapabilityGroups)
$SecurityAuditEnabled = $SecurityAuditEnabled -or ('security' -in $resolvedCapabilityGroups)
$OnlineEnabled = $OnlineEnabled -or $CapabilityProfile -in @('web-api','secure-api','browser-bff')

if ($ProvisionDatabase -and -not $DatabaseEnabled) {
    throw "ProvisionDatabase는 Data Capability가 활성화된 Domain에서만 사용할 수 있습니다."
}
if ($OnlineEnabled -and $CapabilityProfile -notin @('web-api','secure-api','browser-bff')) {
    throw "Online API 생성은 web-api, secure-api, browser-bff Profile 중 하나를 사용해야 합니다. profile=$CapabilityProfile"
}
if (($BatchEnabled -or $CenterCutEnabled) -and $CapabilityProfile -ne 'batch-service') {
    throw "Batch/Center-Cut 생성은 batch-service Profile을 사용해야 합니다. profile=$CapabilityProfile"
}
if ($UiEnabled -and -not $OnlineEnabled) {
    throw "UI 생성은 Online API Profile이 필요합니다."
}

$resolvedStarters = @($resolvedStarters | Sort-Object -Unique)

# Provider 선택은 해당 Capability Group이 활성화된 경우에만 허용합니다.
foreach ($slot in @($resolvedProviderBindings.Keys)) {
    $requiredGroup = [string]$providerSlotToGroup[$slot]
    if ([string]::IsNullOrWhiteSpace($requiredGroup) -or $requiredGroup -notin $resolvedCapabilityGroups) {
        throw "활성화되지 않은 Capability Group의 Provider를 선택했습니다: $slot=$($resolvedProviderBindings[$slot])"
    }
}
$DataSourceJndiName = "java:comp/env/jdbc/cpf${ModuleName}DataSource"
$ModuleClassName = $ModuleName
$FeaturePackage = "$BasePackage.sampleitem"
$FeatureClassPrefix = "${ModuleName}"
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = if ($Apply) {
        Join-Path $Root $projectName
    } else {
        Join-Path $Root "build/domain-generator/$projectName"
    }
} elseif ([IO.Path]::IsPathRooted($OutputDir)) {
    $OutputDir = [IO.Path]::GetFullPath($OutputDir)
} else {
    # 명시 OutputDir도 현재 shell working directory가 아니라 선택한 CPF Root 기준입니다.
    # 그래야 격리 Root/CI 호출에서 생성 파일과 ownership checksum의 상대경로가 결정적입니다.
    $OutputDir = [IO.Path]::GetFullPath((Join-Path $Root $OutputDir))
}

$targetModuleDir = Join-Path $Root $projectName
$upgradeMode = -not [string]::IsNullOrWhiteSpace($UpgradeSourceDomainPath)
$upgradeSourcePath = $null
if ($upgradeMode) {
    $upgradeSourcePath = if ([IO.Path]::IsPathRooted($UpgradeSourceDomainPath)) {
        [IO.Path]::GetFullPath($UpgradeSourceDomainPath)
    } else {
        [IO.Path]::GetFullPath((Join-Path $Root $UpgradeSourceDomainPath))
    }
    if (-not (Test-Path -LiteralPath (Join-Path $upgradeSourcePath 'manifest/domain-manifest.json') -PathType Leaf)) {
        throw "Upgrade source Generated Domain manifest가 없습니다: $upgradeSourcePath"
    }
}
$settingsPath = Join-Path $Root "settings.gradle"
$packagePath = $BasePackage.Replace('.', '/')
$featurePackagePath = $FeaturePackage.Replace('.', '/')
$conflicts = New-Object System.Collections.Generic.List[string]

if (-not $upgradeMode -and (Test-Path -LiteralPath $targetModuleDir)) {
    $conflicts.Add("module directory already exists: $module")
}
if (-not $upgradeMode -and (Test-TextExists -Path $settingsPath -Text "include '$projectName'")) {
    $conflicts.Add("settings.gradle에 같은 모듈이 이미 등록되어 있습니다: $projectName")
}
# Platform/Generated Domain table-prefix collision은 특정 Vendor SQL 경로를 직접 읽지 않는다.
# generated database-schema-manifest.json이 현재 구현된 Platform schema의 vendor-neutral collision gate다.
$databaseSchemaManifestPath = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
if ($DatabaseEnabled -and (Test-Path -LiteralPath $databaseSchemaManifestPath -PathType Leaf)) {
    try {
        $databaseSchemaManifest = Get-Content -LiteralPath $databaseSchemaManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
        $prefixCollision = @($databaseSchemaManifest.tables | Where-Object {
            ([string]$_.tableName).ToLowerInvariant().StartsWith($TablePrefix + "_")
        })
        if ($prefixCollision.Count -gt 0) {
            $conflicts.Add("table prefix already appears in canonical schema manifest: $TablePrefix")
        }
    } catch {
        $conflicts.Add("database-schema-manifest.json을 해석할 수 없습니다: $databaseSchemaManifestPath")
    }
}
if (-not $upgradeMode -and (Test-Path -LiteralPath (Join-Path $Root "$projectName/src/main/java/$packagePath"))) {
    $conflicts.Add("base package already exists: $BasePackage")
}

# Platform Module의 SystemCode는 고정 배열로 관리하지 않습니다. 현재 source 설정에서
# 동적으로 발견하여 Generated Domain metadata와 충돌하는 경우만 차단합니다.
# cpf-batch/src는 별도 정리 대상인 legacy aggregate source이므로 읽지 않고, 독립 하위 Module만 검사합니다.
$platformSourceRoots = [System.Collections.Generic.List[string]]::new()
foreach ($platformProject in @(Get-ChildItem -LiteralPath $Root -Directory -Filter 'cpf-*' -ErrorAction SilentlyContinue)) {
    if ($upgradeMode -and [IO.Path]::GetFullPath($platformProject.FullName).Equals($upgradeSourcePath, [StringComparison]::OrdinalIgnoreCase)) {
        continue
    }
    $sourceCandidates = [System.Collections.Generic.List[string]]::new()
    [void]$sourceCandidates.Add((Join-Path $platformProject.FullName 'src'))
    foreach ($childProject in @(Get-ChildItem -LiteralPath $platformProject.FullName -Directory -ErrorAction SilentlyContinue)) {
        [void]$sourceCandidates.Add((Join-Path $childProject.FullName 'src'))
    }
    foreach ($sourceCandidate in $sourceCandidates) {
        if (-not (Test-IsProtectedRepositoryPath $sourceCandidate) -and
                (Test-Path -LiteralPath $sourceCandidate -PathType Container) -and
                -not $platformSourceRoots.Contains([IO.Path]::GetFullPath($sourceCandidate))) {
            [void]$platformSourceRoots.Add([IO.Path]::GetFullPath($sourceCandidate))
        }
    }
}
if (-not $AllowReserved) {
    $platformConfigurationFiles = @($platformSourceRoots |
            ForEach-Object {
                $resourceDirectory = Join-Path $_ 'main/resources'
                if (Test-Path -LiteralPath $resourceDirectory -PathType Container) {
                    Get-ChildItem -LiteralPath $resourceDirectory -Recurse -File `
                            -Include '*.yml', '*.yaml' -ErrorAction SilentlyContinue
                }
            })
    foreach ($configurationFile in $platformConfigurationFiles) {
        $configurationText = [System.IO.File]::ReadAllText(
                $configurationFile.FullName,
                [System.Text.Encoding]::UTF8)
        $matches = [regex]::Matches(
                $configurationText,
                '(?im)^\s*module-id\s*:\s*(?:\$\{[^:}\r\n]+:)?([A-Z][A-Z0-9]{2})(?:\})?\s*$')
        if (@($matches | Where-Object { $_.Groups[1].Value -eq $SystemCode }).Count -gt 0) {
            $conflicts.Add(
                    "SystemCode가 현재 Platform Module 설정과 중복됩니다: $SystemCode ($($configurationFile.FullName))")
            break
        }
    }
}

$manifestFiles = @(Get-ChildItem -LiteralPath $Root -Filter 'domain-manifest.json' -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notmatch '\\build\\|\\.git\\' -and
                    -not (Test-IsProtectedRepositoryPath $_.FullName) -and
                    (-not $upgradeMode -or -not [IO.Path]::GetFullPath($_.FullName).StartsWith($upgradeSourcePath, [StringComparison]::OrdinalIgnoreCase))
        })
foreach ($manifestFile in $manifestFiles) {
    try {
        $manifest = Get-Content -LiteralPath $manifestFile.FullName -Raw -Encoding UTF8 | ConvertFrom-Json
        if ([string]$manifest.systemCode -eq $SystemCode -or [string]$manifest.moduleCode -eq $SystemCode) {
            $conflicts.Add("SystemCode가 기존 manifest와 중복됩니다: $SystemCode ($($manifestFile.FullName))")
        }
        if ([string]$manifest.domainName -eq $module -or [string]$manifest.projectName -eq $projectName) {
            $conflicts.Add("DomainName 또는 projectName이 기존 manifest와 중복됩니다: $module")
        }
        $existingPackage = if (-not [string]::IsNullOrWhiteSpace([string]$manifest.packageName)) {
            [string]$manifest.packageName
        } else {
            [string]$manifest.basePackage
        }
        if (-not [string]::IsNullOrWhiteSpace($existingPackage) -and
                $existingPackage -eq $PackageName) {
            $conflicts.Add("PackageName이 기존 manifest와 중복됩니다: $PackageName ($($manifestFile.FullName))")
        }
        if ($DatabaseEnabled -and
                -not [string]::IsNullOrWhiteSpace([string]$manifest.schemaName) -and
                [string]$manifest.schemaName -eq $SchemaName) {
            $conflicts.Add("SchemaName이 기존 manifest와 중복됩니다: $SchemaName ($($manifestFile.FullName))")
        }
        if ($DatabaseEnabled -and
                -not [string]::IsNullOrWhiteSpace([string]$manifest.tablePrefix) -and
                [string]$manifest.tablePrefix -eq $TablePrefix) {
            $conflicts.Add("TablePrefix가 기존 manifest와 중복됩니다: $TablePrefix ($($manifestFile.FullName))")
        }
        if ($OnlineEnabled -and [int]$manifest.port -gt 0 -and [int]$manifest.port -eq $Port) {
            $conflicts.Add("Port가 기존 manifest와 중복됩니다: $Port ($($manifestFile.FullName))")
        }
    } catch {
        $conflicts.Add("기존 domain manifest를 해석할 수 없습니다: $($manifestFile.FullName)")
    }
}

foreach ($sourceRoot in $platformSourceRoots) {
    $candidateFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Include *.java,*.yml,*.yaml,*.xml -ErrorAction SilentlyContinue)
    foreach ($candidateFile in $candidateFiles) {
        $candidateText = [System.IO.File]::ReadAllText($candidateFile.FullName, [System.Text.Encoding]::UTF8)
        if ($candidateText.Contains("/api/v1/$module") -or $candidateText.Contains("package $BasePackage")) {
            $conflicts.Add("API route 또는 package가 기존 source와 충돌합니다: $($candidateFile.FullName)")
            break
        }
    }
}

$plan = [ordered]@{
    status = $StatusDone
    dryRun = [bool] $DryRun
    moduleCode = $ModuleUpper
    domainName = $module
    projectName = $projectName
    systemCode = $SystemCode
    moduleName = $ModuleName
    domainIdCode = $DomainIdCode
    packageName = $PackageName
    basePackage = $BasePackage
    schemaName = $SchemaName
    tablePrefix = $TablePrefix
    port = $Port
    online = $OnlineEnabled
    database = $DatabaseEnabled
    databaseVendor = $DatabaseVendor
    dependencyModel = $DependencyModel
    platformVersion = $PlatformVersion
    capabilityProfile = $CapabilityProfile
    capabilityProfileVersion = [string]$resolvedCapabilityProfile.profileVersion
    resolvedStarters = @($resolvedStarters)
    providerBindings = $resolvedProviderBindings
    approvedExternalExceptions = @($approvedExceptionRows | ForEach-Object { [string]$_.exception_id })
    templateContractVersion = [string]$centralTemplateContract.contractVersion
    batch = $BatchEnabled
    external = $ExternalEnabled
    messaging = $MessagingEnabled
    file = $FileEnabled
    securityAudit = $SecurityAuditEnabled
    ui = $UiEnabled
    bzaMenu = $BzaMenuEnabled
    productionProfile = $ProductionProfileEnabled
    # Generated metadata에는 개발 PC 절대경로를 저장하지 않습니다.
    outputDir = $projectName
    generatePatch = $false
    applyMode = [bool] $Apply
    upgradeSourceDomainPath = $(if ($upgradeMode) { $upgradeSourcePath.Substring($Root.Length).TrimStart('\', '/') } else { $null })
    conflicts = @($conflicts)
    generatedFiles = @()
    patchFiles = @()
    patchCandidates = @()
}

if ($conflicts.Count -gt 0) {
    $plan.status = $StatusFailed
}

if ($DryRun -or $conflicts.Count -gt 0) {
    $plan | ConvertTo-Json -Depth 20
    if ($conflicts.Count -gt 0) {
        exit 1
    }
    exit 0
}

$moduleBaseController = @"
package $BasePackage.common.base;

import com.cpf.core.api.base.CpfBaseController;

/**
 * $ModuleUpper Controller가 공유하는 주제영역 확장점입니다.
 *
 * <p>route와 Spring stereotype을 두지 않으며 주제영역 공통 정책만 제한적으로 추가합니다.</p>
 */
public abstract class ${ModuleClassName}BaseController extends CpfBaseController {
}
"@

$moduleBaseService = @"
package $BasePackage.common.base;

import com.cpf.core.api.base.CpfBaseService;

/**
 * $ModuleUpper Service가 공유하는 주제영역 확장점입니다.
 */
public abstract class ${ModuleClassName}BaseService extends CpfBaseService {
}
"@

$moduleFacadeContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfApplicationFacade;

/** $ModuleUpper Application Facade의 주제영역 계약입니다. */
public interface ${ModuleClassName}ApplicationFacade extends CpfApplicationFacade {
}
"@

$moduleRepositoryContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfRepositoryPort;

/**
 * $ModuleUpper Repository Port가 공통으로 확장하는 주제영역 계약입니다.
 *
 * @param <T> model 형식
 * @param <ID> 식별자 형식
 */
public interface ${ModuleClassName}RepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
}
"@

$moduleRequestContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfRequest;

/** $ModuleUpper 요청 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Request extends CpfRequest {
}
"@

$moduleResponseContract = @"
package $BasePackage.common.contract;

import com.cpf.core.api.base.CpfResponse;

/** $ModuleUpper 응답 DTO의 주제영역 계약입니다. */
public interface ${ModuleClassName}Response extends CpfResponse {
}
"@

$controller = @"
package $FeaturePackage.controller;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import $FeaturePackage.dto.*;
import $FeaturePackage.facade.${FeatureClassPrefix}Facade;
import $FeaturePackage.validation.${FeatureClassPrefix}SearchValidator;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfSlice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/** Generated Domain의 Typed Sample Item API입니다. */
@RestController
@RequestMapping("/api/v1/$module/sample-items")
@Tag(name = "$ModuleUpper Sample Item", description = "Generated Domain Typed CRUD/Search/Paging API")
public class ${FeatureClassPrefix}Controller extends ${ModuleClassName}BaseController {
    private final ${FeatureClassPrefix}Facade facade;
    private final ${FeatureClassPrefix}SearchValidator validator;

    public ${FeatureClassPrefix}Controller(${FeatureClassPrefix}Facade facade, ${FeatureClassPrefix}SearchValidator validator) {
        this.facade = Objects.requireNonNull(facade, "facade는 필수입니다.");
        this.validator = Objects.requireNonNull(validator, "validator는 필수입니다.");
    }

    @GetMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0001", name = "${ModuleName}Search", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "search${ModuleName}SampleItems", summary = "Sample Item 목록 조회")
    public ResponseEntity<${FeatureClassPrefix}SearchResult> search(${FeatureClassPrefix}SearchRequest request) {
        validator.validate(request);
        return ok(facade.search(request));
    }

    @PostMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}IN0001", name = "${ModuleName}Create", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "create${ModuleName}SampleItem", summary = "Sample Item 등록")
    public ResponseEntity<${FeatureClassPrefix}SampleItem> create(@RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.create(command));
    }

    @GetMapping("/{sampleKey}")
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0002", name = "${ModuleName}Find", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "find${ModuleName}SampleItem", summary = "Sample Item 단건 조회")
    public ResponseEntity<${FeatureClassPrefix}SampleItem> findBySampleKey(@PathVariable String sampleKey) {
        return ok(facade.findBySampleKey(sampleKey)
                .orElseThrow(() -> new CpfValidationException("Sample Item을 찾을 수 없습니다.")));
    }

    @PostMapping("/{sampleItemId}/update")
    @CpfOnlineTransaction(id = "O${DomainIdCode}UP0001", name = "${ModuleName}Update", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "update${ModuleName}SampleItem", summary = "낙관적 잠금 Sample Item 수정")
    public ResponseEntity<${FeatureClassPrefix}SampleItem> update(
            @PathVariable long sampleItemId, @RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.update(sampleItemId, command));
    }

    @PostMapping("/{sampleItemId}/delete")
    @CpfOnlineTransaction(id = "O${DomainIdCode}DL0001", name = "${ModuleName}Delete", ownerDomain = "$DomainIdCode")
    @Operation(operationId = "delete${ModuleName}SampleItem", summary = "낙관적 잠금 Sample Item 논리 삭제")
    public ResponseEntity<${FeatureClassPrefix}DeleteResult> delete(
            @PathVariable long sampleItemId, @RequestBody ${FeatureClassPrefix}DeleteCommand command) {
        return ok(facade.delete(sampleItemId, command));
    }

    @GetMapping("/cursor")
    @CpfOnlineTransaction(id = "O${DomainIdCode}QY0003", name = "${ModuleName}Cursor", ownerDomain = "$DomainIdCode")
    public ResponseEntity<CpfSlice<${FeatureClassPrefix}SampleItem>> cursor(
            @RequestParam(required = false) Long afterId, @RequestParam(defaultValue = "20") int size) {
        return ok(facade.cursor(afterId, size));
    }

    @PostMapping("/rollback-verify")
    @CpfOnlineTransaction(id = "O${DomainIdCode}TX0001", name = "${ModuleName}Rollback", ownerDomain = "$DomainIdCode")
    public ResponseEntity<Boolean> verifyRollback(@RequestBody ${FeatureClassPrefix}SampleCommand command) {
        return ok(facade.verifyRollback(command));
    }
}
"@

$facade = @"
package $FeaturePackage.facade;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'ApplicationFacade;'))
import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.service.${FeatureClassPrefix}Service;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/** Controller와 업무 서비스를 분리하는 Generated Domain 진입 Facade입니다. */
@Component
public class ${FeatureClassPrefix}Facade implements ${ModuleClassName}ApplicationFacade {
    private final ${FeatureClassPrefix}Service service;
    public ${FeatureClassPrefix}Facade(${FeatureClassPrefix}Service service) { this.service = Objects.requireNonNull(service); }
    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request) { return service.search(request); }
    public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand command) { return service.create(command); }
    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String sampleKey) { return service.findBySampleKey(sampleKey); }
    public ${FeatureClassPrefix}SampleItem update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command) { return service.update(sampleItemId, command); }
    public ${FeatureClassPrefix}DeleteResult delete(long sampleItemId, ${FeatureClassPrefix}DeleteCommand command) { return service.delete(sampleItemId, command); }
    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId, int size) { return service.cursor(afterId, size); }
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command) { return service.verifyRollback(command); }
}
"@

$queryPortSource = @"
package $FeaturePackage.port;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'RepositoryPort;'))
import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import java.util.Optional;

/** 조회 책임만 소유하는 Generated Domain Query Port입니다. */
public interface ${FeatureClassPrefix}QueryPort extends ${ModuleClassName}RepositoryPort<${FeatureClassPrefix}SampleItem, String> {
    ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request);
    Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String sampleKey);
    CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId, int size);
}
"@

$commandPortSource = @"
package $FeaturePackage.port;

import $FeaturePackage.dto.*;

/** 변경·멱등성·낙관적 잠금 책임을 소유하는 Generated Domain Command Port입니다. */
public interface ${FeatureClassPrefix}CommandPort {
    ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    ${FeatureClassPrefix}SampleItem update(long sampleItemId, ${FeatureClassPrefix}SampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    ${FeatureClassPrefix}DeleteResult delete(long sampleItemId, long expectedVersion,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
    boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,
            String transactionId, String idempotencyKey, long transactionSequence, String actor);
}
"@

$localAdapter = @"
package $FeaturePackage.adapter.local;

import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.*;
import $FeaturePackage.repository.${FeatureClassPrefix}Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/** 같은 주제영역 DB를 사용하는 기본 Local Adapter입니다. */
@Component
@ConditionalOnProperty(name = "cpf.$module.sample-item.mode", havingValue = "local", matchIfMissing = true)
public class Local${FeatureClassPrefix}Adapter implements ${FeatureClassPrefix}QueryPort, ${FeatureClassPrefix}CommandPort {
    private final ${FeatureClassPrefix}Repository repository;
    public Local${FeatureClassPrefix}Adapter(${FeatureClassPrefix}Repository repository) { this.repository = Objects.requireNonNull(repository); }
    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request) { return repository.search(request); }
    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String sampleKey) { return repository.findBySampleKey(sampleKey); }
    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId, int size) { return repository.cursor(afterId, size); }
    public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){return repository.create(command,tx,key,seq,actor);}
    public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){return repository.update(id,command,tx,key,seq,actor);}
    public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String tx,String key,long seq,String actor){return repository.delete(id,version,tx,key,seq,actor);}
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){return repository.verifyRollback(command,tx,key,seq,actor);}
}
"@

$remoteMatchIfMissing = if (-not $DatabaseEnabled) { ", matchIfMissing = true" } else { "" }
$remoteProxy = @"
package $FeaturePackage.adapter.remote;

import $FeaturePackage.dto.*;
import com.cpf.core.api.http.CpfHttpClient;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.cpf.core.api.http.CpfHttpPath;
import com.cpf.core.api.http.CpfTypeRef;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/** CPF 표준 Service Call 경계를 사용하는 분리 WAS Remote Adapter입니다. */
@Component
@ConditionalOnProperty(name = "cpf.$module.sample-item.mode", havingValue = "remote"$remoteMatchIfMissing)
public class Remote${FeatureClassPrefix}Adapter implements ${FeatureClassPrefix}QueryPort, ${FeatureClassPrefix}CommandPort {
    private final CpfHttpClient client;
    public Remote${FeatureClassPrefix}Adapter(CpfHttpClient client){this.client=Objects.requireNonNull(client);}
    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request){return client.get("$ModuleUpper", CpfHttpPath.of("/api/v1/$module/sample-items").queryParam("keyword",request.keyword()).queryParam("page",request.page()).queryParam("size",request.size()).queryParam("sortBy",request.sortBy()).queryParam("sortDirection",request.sortDirection()).build(), ${FeatureClassPrefix}SearchResult.class);}
    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){return Optional.ofNullable(client.get("$ModuleUpper","/api/v1/$module/sample-items/"+CpfHttpPath.segment(key),${FeatureClassPrefix}SampleItem.class));}
    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId,int size){return client.get("$ModuleUpper",CpfHttpPath.of("/api/v1/$module/sample-items/cursor").queryParam("afterId",afterId==null?0:afterId).queryParam("size",size).build(),new CpfTypeRef<CpfSlice<${FeatureClassPrefix}SampleItem>>(){});}
    public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){return client.post("$ModuleUpper","/api/v1/$module/sample-items",command,${FeatureClassPrefix}SampleItem.class);}
    public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){return client.post("$ModuleUpper","/api/v1/$module/sample-items/"+id+"/update",command,${FeatureClassPrefix}SampleItem.class);}
    public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String tx,String key,long seq,String actor){return client.post("$ModuleUpper","/api/v1/$module/sample-items/"+id+"/delete",new ${FeatureClassPrefix}DeleteCommand(version),${FeatureClassPrefix}DeleteResult.class);}
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand command,String tx,String key,long seq,String actor){Boolean value=client.post("$ModuleUpper","/api/v1/$module/sample-items/rollback-verify",command,Boolean.class);return Boolean.TRUE.equals(value);}
}
"@

$inMemoryAdapter = @"
package $FeaturePackage.adapter.memory;

import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import $FeaturePackage.port.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.dao.OptimisticLockingFailureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local/Test/EDU 전용 실제 CRUD Adapter입니다.
 * 운영 Runtime에서는 Bean을 생성하지 않으며 멱등 요청 Hash와 낙관적 잠금을 동일하게 검증합니다.
 */
@Component
@Profile("!prod & !production & !stage & !staging & (local | test | edu)")
public class InMemory${FeatureClassPrefix}Adapter implements ${FeatureClassPrefix}QueryPort, ${FeatureClassPrefix}CommandPort {
    private final ConcurrentHashMap<Long,${FeatureClassPrefix}SampleItem> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,Long> keyIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String,IdempotencyRecord> idempotencyIndex = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request) {
        var n = request.normalized();
        var items = store.values().stream()
                .filter(v -> n.keyword() == null || v.sampleKey().contains(n.keyword()) || v.itemName().contains(n.keyword()))
                .sorted(Comparator.comparingLong(${FeatureClassPrefix}SampleItem::sampleItemId))
                .skip((long) n.page() * n.size()).limit(n.size()).toList();
        return new ${FeatureClassPrefix}SearchResult(items, n, store.size());
    }

    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key) {
        Long id = keyIndex.get(key);
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }

    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId, int size) {
        int safe = Math.max(1, Math.min(size, 200));
        var items = store.values().stream()
                .filter(v -> v.sampleItemId() > (afterId == null ? 0 : afterId))
                .sorted(Comparator.comparingLong(${FeatureClassPrefix}SampleItem::sampleItemId))
                .limit(safe + 1L).toList();
        boolean next = items.size() > safe;
        return new CpfSlice<>(next ? items.subList(0, safe) : items, 0, safe, next);
    }

    public synchronized ${FeatureClassPrefix}SampleItem create(
            ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey,
            long transactionSequence, String actor) {
        String requestHash = requestHash("CREATE", 0L, command.sampleKey(), command.itemName(), command.statusCode(), command.expectedVersion());
        IdempotencyRecord replay = replay(idempotencyKey, "CREATE", requestHash, 0L);
        if (replay != null) return requiredItem(replay.sampleItemId());
        if (keyIndex.containsKey(command.sampleKey())) throw new IllegalStateException("sampleKey가 이미 존재합니다.");
        long id = sequence.incrementAndGet();
        Instant now = Instant.now();
        var item = new ${FeatureClassPrefix}SampleItem(id, command.sampleKey(), command.itemName(), command.statusCode(), 0,
                idempotencyKey, transactionId, transactionSequence, now, actor, now, actor, now);
        store.put(id, item); keyIndex.put(item.sampleKey(), id);
        idempotencyIndex.put(idempotencyKey, new IdempotencyRecord("CREATE", requestHash, id, item.versionNo(), false));
        return item;
    }

    public synchronized ${FeatureClassPrefix}SampleItem update(
            long id, ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey,
            long transactionSequence, String actor) {
        String requestHash = requestHash("UPDATE", id, command.sampleKey(), command.itemName(), command.statusCode(), command.expectedVersion());
        IdempotencyRecord replay = replay(idempotencyKey, "UPDATE", requestHash, id);
        if (replay != null) return requiredItem(id);
        var old = requiredItem(id);
        if (old.versionNo() != command.expectedVersion()) throw new OptimisticLockingFailureException("Sample Item version 충돌");
        Long keyOwner = keyIndex.get(command.sampleKey());
        if (keyOwner != null && keyOwner != id) throw new IllegalStateException("sampleKey가 이미 존재합니다.");
        Instant now = Instant.now();
        var updated = new ${FeatureClassPrefix}SampleItem(id, command.sampleKey(), command.itemName(), command.statusCode(), old.versionNo() + 1,
                idempotencyKey, transactionId, transactionSequence, now, old.createdBy(), old.createdAt(), actor, now);
        store.put(id, updated);
        if (!old.sampleKey().equals(updated.sampleKey())) keyIndex.remove(old.sampleKey(), id);
        keyIndex.put(updated.sampleKey(), id);
        idempotencyIndex.put(idempotencyKey, new IdempotencyRecord("UPDATE", requestHash, id, updated.versionNo(), false));
        return updated;
    }

    public synchronized ${FeatureClassPrefix}DeleteResult delete(
            long id, long expectedVersion, String transactionId, String idempotencyKey,
            long transactionSequence, String actor) {
        String requestHash = requestHash("DELETE", id, "", "", "", expectedVersion);
        IdempotencyRecord replay = replay(idempotencyKey, "DELETE", requestHash, id);
        if (replay != null) return new ${FeatureClassPrefix}DeleteResult(true, id, replay.resultVersion());
        var old = requiredItem(id);
        if (old.versionNo() != expectedVersion) throw new OptimisticLockingFailureException("Sample Item version 충돌");
        if (!store.remove(id, old)) throw new OptimisticLockingFailureException("Sample Item 동시 변경");
        keyIndex.remove(old.sampleKey(), id);
        long deletedVersion = old.versionNo() + 1;
        idempotencyIndex.put(idempotencyKey, new IdempotencyRecord("DELETE", requestHash, id, deletedVersion, true));
        return new ${FeatureClassPrefix}DeleteResult(true, id, deletedVersion);
    }

    public synchronized boolean verifyRollback(
            ${FeatureClassPrefix}SampleCommand command, String transactionId, String idempotencyKey,
            long transactionSequence, String actor) {
        Map<Long,${FeatureClassPrefix}SampleItem> storeSnapshot = Map.copyOf(store);
        Map<String,Long> keySnapshot = Map.copyOf(keyIndex);
        Map<String,IdempotencyRecord> idempotencySnapshot = Map.copyOf(idempotencyIndex);
        long sequenceSnapshot = sequence.get();
        try { create(command, transactionId, idempotencyKey, transactionSequence, actor); }
        finally {
            store.clear(); store.putAll(storeSnapshot);
            keyIndex.clear(); keyIndex.putAll(keySnapshot);
            idempotencyIndex.clear(); idempotencyIndex.putAll(idempotencySnapshot);
            sequence.set(sequenceSnapshot);
        }
        return store.equals(storeSnapshot) && keyIndex.equals(keySnapshot)
                && idempotencyIndex.equals(idempotencySnapshot) && sequence.get() == sequenceSnapshot;
    }

    private IdempotencyRecord replay(String key, String operation, String requestHash, long expectedItemId) {
        IdempotencyRecord value = idempotencyIndex.get(key);
        if (value == null) return null;
        if (!value.operationCode().equals(operation) || !value.requestHash().equals(requestHash)
                || (expectedItemId > 0 && value.sampleItemId() != expectedItemId)) {
            throw new IllegalStateException("동일 idempotencyKey에 다른 요청을 사용할 수 없습니다.");
        }
        return value;
    }

    private ${FeatureClassPrefix}SampleItem requiredItem(long id) {
        var item = store.get(id);
        if (item == null) throw new IllegalArgumentException("Sample Item을 찾을 수 없습니다: " + id);
        return item;
    }

    private String requestHash(String operation, long id, String sampleKey, String itemName, String statusCode, long version) {
        String canonical = operation + '|' + id + '|' + sampleKey + '|' + itemName + '|' + statusCode + '|' + version;
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("멱등 요청 Hash 생성 실패", ex); }
    }

    private record IdempotencyRecord(String operationCode, String requestHash, long sampleItemId,
                                     long resultVersion, boolean deleted) {}
}
"@

$service = @"
package $FeaturePackage.service;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseService;'))
import $FeaturePackage.dto.*;
import $FeaturePackage.port.*;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.page.CpfSlice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Optional;

/** Generated Domain Typed Sample Item 업무 서비스입니다. */
@Service
public class ${FeatureClassPrefix}Service extends ${ModuleClassName}BaseService {
    private final ${FeatureClassPrefix}QueryPort queryPort; private final ${FeatureClassPrefix}CommandPort commandPort;
    public ${FeatureClassPrefix}Service(${FeatureClassPrefix}QueryPort queryPort,${FeatureClassPrefix}CommandPort commandPort){this.queryPort=Objects.requireNonNull(queryPort);this.commandPort=Objects.requireNonNull(commandPort);}
    @Transactional(readOnly=true) public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest r){return queryPort.search(r.normalized());}
    @Transactional public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand c){var x=context();return commandPort.create(c,x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional(readOnly=true) public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){if(key==null||key.isBlank())throw new CpfValidationException("sampleKey는 필수입니다.");return queryPort.findBySampleKey(key.trim());}
    @Transactional public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand c){if(id<1)throw new CpfValidationException("sampleItemId는 1 이상이어야 합니다.");var x=context();return commandPort.update(id,c,x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional public ${FeatureClassPrefix}DeleteResult delete(long id,${FeatureClassPrefix}DeleteCommand c){if(id<1||c==null)throw new CpfValidationException("삭제 입력이 올바르지 않습니다.");var x=context();return commandPort.delete(id,c.expectedVersion(),x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional(readOnly=true) public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId,int size){return queryPort.cursor(afterId,size);}
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand c){var x=context();return commandPort.verifyRollback(c,x.tx(),x.idem(),x.seq(),x.actor());}
    private MutationContext context(){String idem=CpfTransactionContext.idempotencyKey();if(idem==null||idem.isBlank())throw new CpfValidationException("변경 거래에는 idempotencyKey가 필수입니다.");String actor=first(CpfTransactionContext.operatorId(),CpfTransactionContext.userId(),"$ModuleUpper");return new MutationContext(CpfTransactionContext.transactionId(),idem.trim(),CpfTransactionContext.nextSequence(),actor);}
    private String first(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.trim();return "$ModuleUpper";}
    private record MutationContext(String tx,String idem,long seq,String actor){}
}
"@

$myBatisRepository = @"
package $FeaturePackage.repository;

import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import com.cpf.core.api.database.CpfDataOperations;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 중앙 Vendor Pack statement를 Typed DTO로 반환하는 DB-neutral 저장소입니다. */
@Repository
public class ${FeatureClassPrefix}Repository {
    private final CpfDataOperations sql;
    public ${FeatureClassPrefix}Repository(CpfDataOperations sql){this.sql=Objects.requireNonNull(sql);}
    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request){List<${FeatureClassPrefix}SampleItem> items=sql.selectList(statement("search"),request);Long total=sql.selectOne(statement("count"),request);return new ${FeatureClassPrefix}SearchResult(items,request,total==null?0:total);}
    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){return Optional.ofNullable(sql.selectOne(statement("findBySampleKey"),key));}
    public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());
        var replay=idempotency(idem,"CREATE",hash,0); if(replay!=null)return requiredItem(replay.sampleItemId());
        var p=parameters(c,txId,idem,sequence,actor);sql.insert(statement("insert"),p);
        var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",hash,item.sampleItemId(),item.versionNo(),false,txId);return item;
    }
    public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("UPDATE",id,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());
        var replay=idempotency(idem,"UPDATE",hash,id); if(replay!=null)return requiredItem(id);
        var p=parameters(c,txId,idem,sequence,actor);p.put("sampleItemId",id);
        if(sql.update(statement("updateWithVersion"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");
        var item=requiredItem(id);insertIdempotency(idem,"UPDATE",hash,id,item.versionNo(),false,txId);return item;
    }
    public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String txId,String idem,long sequence,String actor){
        String hash=requestHash("DELETE",id,"","","",version);var replay=idempotency(idem,"DELETE",hash,id);
        if(replay!=null)return new ${FeatureClassPrefix}DeleteResult(true,id,replay.resultVersion());
        var p=new HashMap<String,Object>();p.put("sampleItemId",id);p.put("versionNo",version);p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("updatedBy",actor);
        if(sql.update(statement("logicalDeleteWithVersion"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");
        long deletedVersion=version+1;insertIdempotency(idem,"DELETE",hash,id,deletedVersion,true,txId);return new ${FeatureClassPrefix}DeleteResult(true,id,deletedVersion);
    }
    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId,int size){int safe=Math.max(1,Math.min(size,200));List<${FeatureClassPrefix}SampleItem> rows=sql.selectList(statement("cursorSlice"),Map.of("cursor",afterId==null?0:afterId,"size",safe+1));boolean next=rows.size()>safe;return new CpfSlice<>(next?rows.subList(0,safe):rows,0,safe,next);}
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){boolean before=findBySampleKey(c.sampleKey()).isPresent();var prior=sql.selectOne(statement("findIdempotency"),idem);sql.inRollbackOnlyTransaction(ignored->{var p=parameters(c,txId,idem,sequence,actor);sql.insert(statement("insert"),p);var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion()),item.sampleItemId(),item.versionNo(),false,txId);});return before==findBySampleKey(c.sampleKey()).isPresent()&&Objects.equals(prior,sql.selectOne(statement("findIdempotency"),idem));}
    private ${FeatureClassPrefix}IdempotencyEntry idempotency(String key,String operation,String hash,long expectedItemId){${FeatureClassPrefix}IdempotencyEntry value=sql.selectOne(statement("findIdempotency"),key);if(value==null)return null;if(!value.operationCode().equals(operation)||!value.requestHash().equals(hash)||(expectedItemId>0&&value.sampleItemId()!=expectedItemId))throw new IllegalStateException("동일 idempotencyKey에 다른 요청을 사용할 수 없습니다.");return value;}
    private void insertIdempotency(String key,String operation,String hash,long itemId,long resultVersion,boolean deleted,String txId){var p=new HashMap<String,Object>();p.put("idempotencyKey",key);p.put("operationCode",operation);p.put("requestHash",hash);p.put("sampleItemId",itemId);p.put("resultVersion",resultVersion);p.put("deletedYn",deleted?"Y":"N");p.put("transactionId",txId);sql.insert(statement("insertIdempotency"),p);}
    private ${FeatureClassPrefix}SampleItem requiredItem(long id){${FeatureClassPrefix}SampleItem value=sql.selectOne(statement("findById"),id);if(value==null)throw new IllegalArgumentException("Sample Item을 찾을 수 없습니다: "+id);return value;}
    private String requestHash(String operation,long id,String sampleKey,String itemName,String statusCode,long version){String canonical=operation+'|'+id+'|'+sampleKey+'|'+itemName+'|'+statusCode+'|'+version;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException("멱등 요청 Hash 생성 실패",ex);}}
    private Map<String,Object> parameters(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){var p=new HashMap<String,Object>();p.put("sampleKey",c.sampleKey());p.put("itemName",c.itemName());p.put("statusCode",c.statusCode());p.put("versionNo",c.expectedVersion());p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("createdBy",actor);p.put("updatedBy",actor);return p;}
    private String statement(String id){return "${FeaturePackage}.mapper.${FeatureClassPrefix}Mapper."+id;}
}
"@


$jdbcRepository = @"
package $FeaturePackage.repository;

import $FeaturePackage.dto.*;
import com.cpf.core.api.database.CpfJdbcOperations;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.page.CpfSlice;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** CPF JDBC API와 공식 3-Vendor SQL Catalog를 사용하는 Generated Domain 저장소입니다. */
@Repository
public class ${FeatureClassPrefix}Repository {
    private final CpfJdbcOperations sql;
    private final CpfVendorSqlCatalog catalog;
    public ${FeatureClassPrefix}Repository(CpfJdbcOperations sql, CpfVendorSqlCatalogProvider catalogs) {
        this.sql=Objects.requireNonNull(sql); this.catalog=Objects.requireNonNull(catalogs).forModule("$module");
    }
    public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request){var n=request.normalized();var p=searchParameters(n);List<${FeatureClassPrefix}SampleItem> items=sql.queryList(catalog.required("sample-search"),p,${FeatureClassPrefix}SampleItem.class);Long total=sql.queryOne(catalog.required("sample-count"),p,Long.class);return new ${FeatureClassPrefix}SearchResult(items,n,total==null?0:total);}
    public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){return Optional.ofNullable(sql.queryOne(catalog.required("sample-find-by-key"),Map.of("value",key),${FeatureClassPrefix}SampleItem.class));}
    public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){String hash=requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());var replay=idempotency(idem,"CREATE",hash,0);if(replay!=null)return requiredItem(replay.sampleItemId());var p=parameters(c,txId,idem,sequence,actor);sql.update(catalog.required("sample-insert"),p);var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",hash,item.sampleItemId(),item.versionNo(),false,txId);return item;}
    public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){String hash=requestHash("UPDATE",id,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion());var replay=idempotency(idem,"UPDATE",hash,id);if(replay!=null)return requiredItem(id);var p=parameters(c,txId,idem,sequence,actor);p.put("sampleItemId",id);if(sql.update(catalog.required("sample-update-version"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");var item=requiredItem(id);insertIdempotency(idem,"UPDATE",hash,id,item.versionNo(),false,txId);return item;}
    public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String txId,String idem,long sequence,String actor){String hash=requestHash("DELETE",id,"","","",version);var replay=idempotency(idem,"DELETE",hash,id);if(replay!=null)return new ${FeatureClassPrefix}DeleteResult(true,id,replay.resultVersion());var p=new HashMap<String,Object>();p.put("sampleItemId",id);p.put("versionNo",version);p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("updatedBy",actor);if(sql.update(catalog.required("sample-delete-version"),p)!=1)throw new OptimisticLockingFailureException("Sample Item version 충돌");long deletedVersion=version+1;insertIdempotency(idem,"DELETE",hash,id,deletedVersion,true,txId);return new ${FeatureClassPrefix}DeleteResult(true,id,deletedVersion);}
    public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId,int size){int safe=Math.max(1,Math.min(size,200));List<${FeatureClassPrefix}SampleItem> rows=sql.queryList(catalog.required("sample-cursor"),Map.of("cursor",afterId==null?0:afterId,"size",safe+1),${FeatureClassPrefix}SampleItem.class);boolean next=rows.size()>safe;return new CpfSlice<>(next?rows.subList(0,safe):rows,0,safe,next);}
    public boolean verifyRollback(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){boolean before=findBySampleKey(c.sampleKey()).isPresent();var prior=sql.queryOne(catalog.required("sample-idempotency-find"),Map.of("value",idem),${FeatureClassPrefix}IdempotencyEntry.class);sql.inRollbackOnlyTransaction(ignored->{var p=parameters(c,txId,idem,sequence,actor);sql.update(catalog.required("sample-insert"),p);var item=findBySampleKey(c.sampleKey()).orElseThrow();insertIdempotency(idem,"CREATE",requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion()),item.sampleItemId(),item.versionNo(),false,txId);});return before==findBySampleKey(c.sampleKey()).isPresent()&&Objects.equals(prior,sql.queryOne(catalog.required("sample-idempotency-find"),Map.of("value",idem),${FeatureClassPrefix}IdempotencyEntry.class));}
    private ${FeatureClassPrefix}IdempotencyEntry idempotency(String key,String operation,String hash,long expectedItemId){${FeatureClassPrefix}IdempotencyEntry value=sql.queryOne(catalog.required("sample-idempotency-find"),Map.of("value",key),${FeatureClassPrefix}IdempotencyEntry.class);if(value==null)return null;if(!value.operationCode().equals(operation)||!value.requestHash().equals(hash)||(expectedItemId>0&&value.sampleItemId()!=expectedItemId))throw new IllegalStateException("동일 idempotencyKey에 다른 요청을 사용할 수 없습니다.");return value;}
    private void insertIdempotency(String key,String operation,String hash,long itemId,long resultVersion,boolean deleted,String txId){var p=new HashMap<String,Object>();p.put("idempotencyKey",key);p.put("operationCode",operation);p.put("requestHash",hash);p.put("sampleItemId",itemId);p.put("resultVersion",resultVersion);p.put("deletedYn",deleted?"Y":"N");p.put("transactionId",txId);sql.update(catalog.required("sample-idempotency-insert"),p);}
    private ${FeatureClassPrefix}SampleItem requiredItem(long id){${FeatureClassPrefix}SampleItem value=sql.queryOne(catalog.required("sample-find-by-id"),Map.of("value",id),${FeatureClassPrefix}SampleItem.class);if(value==null)throw new IllegalArgumentException("Sample Item을 찾을 수 없습니다: "+id);return value;}
    private String requestHash(String operation,long id,String sampleKey,String itemName,String statusCode,long version){String canonical=operation+'|'+id+'|'+sampleKey+'|'+itemName+'|'+statusCode+'|'+version;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException("멱등 요청 Hash 생성 실패",ex);}}
    private Map<String,Object> searchParameters(${FeatureClassPrefix}SearchRequest n){var p=new HashMap<String,Object>();p.put("keyword",n.keyword());p.put("offset",n.offset());p.put("size",n.size());p.put("sortBy",n.sortBy());p.put("sortDirection",n.sortDirection());return p;}
    private Map<String,Object> parameters(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){var p=new HashMap<String,Object>();p.put("sampleKey",c.sampleKey());p.put("itemName",c.itemName());p.put("statusCode",c.statusCode());p.put("versionNo",c.expectedVersion());p.put("idempotencyKey",idem);p.put("transactionId",txId);p.put("transactionSequence",sequence);p.put("createdBy",actor);p.put("updatedBy",actor);return p;}
}
"@

$jpaRepository = @"
package $FeaturePackage.repository;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'RepositoryPort;'))
import $FeaturePackage.dto.*;
import com.cpf.core.api.page.CpfSlice;
import com.cpf.core.api.error.CpfPersistenceException;
import com.cpf.core.api.error.CpfPersistenceFailureType;
import com.cpf.starter.data.persistence.jpa.CpfJpaNativeAccess;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * CPF Optional JPA Provider를 사용하는 Generated Domain 저장소입니다.
 * <p>JDBC/MyBatis와 같은 API/idempotency/optimistic-lock/logical-delete 의미를 유지합니다.
 * 복잡 Query는 {@link #entityManager()} escape hatch를 사용할 수 있습니다.</p>
 */
@Repository
public class ${FeatureClassPrefix}Repository implements ${FeatureClassPrefix}QueryPort, ${FeatureClassPrefix}CommandPort, CpfJpaNativeAccess {
    private static final Map<String,String> SORTS = Map.of(
            "created_at","createdAt", "updated_at","updatedAt", "sample_item_id","sampleItemId", "item_name","itemName");
    private final EntityManager em;
    private final TransactionTemplate tx;

    public ${FeatureClassPrefix}Repository(EntityManager em, PlatformTransactionManager manager) {
        this.em = Objects.requireNonNull(em,"em");
        this.tx = new TransactionTemplate(Objects.requireNonNull(manager,"manager"));
    }
    @Override public EntityManager entityManager(){ return em; }

    @Override public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest request) {
        var n=request.normalized(); String sort=SORTS.get(n.sortBy());
        if(sort==null) throw new IllegalArgumentException("허용되지 않은 정렬 field: "+n.sortBy());
        String order="ASC".equals(n.sortDirection())?"ASC":"DESC";
        String where="e.deletedYn='N' and (:keyword='' or lower(e.sampleKey) like :kw or lower(e.itemName) like :kw)";
        var q=em.createQuery("select e from ${FeatureClassPrefix}SampleItemEntity e where "+where+" order by e."+sort+" "+order,${FeatureClassPrefix}SampleItemEntity.class);
        String keyword=n.keyword()==null?"":n.keyword().trim().toLowerCase(Locale.ROOT); q.setParameter("keyword",keyword);q.setParameter("kw","%"+keyword+"%");
        q.setFirstResult(n.offset());q.setMaxResults(n.size());
        var cq=em.createQuery("select count(e) from ${FeatureClassPrefix}SampleItemEntity e where "+where,Long.class);cq.setParameter("keyword",keyword);cq.setParameter("kw","%"+keyword+"%");
        return new ${FeatureClassPrefix}SearchResult(q.getResultStream().map(${FeatureClassPrefix}Repository::dto).toList(),n,cq.getSingleResult());
    }
    @Override public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){
        return em.createQuery("select e from ${FeatureClassPrefix}SampleItemEntity e where e.sampleKey=:key and e.deletedYn='N'",${FeatureClassPrefix}SampleItemEntity.class)
                .setParameter("key",key).setMaxResults(1).getResultStream().findFirst().map(${FeatureClassPrefix}Repository::dto);
    }
    @Override public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("CREATE",0,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion()); var replay=replay(idem,"CREATE",hash,0); if(replay!=null)return requiredItem(replay.sampleItemId);
        if(findBySampleKey(c.sampleKey()).isPresent())throw new CpfPersistenceException(CpfPersistenceFailureType.CONSTRAINT,"create","sampleKey duplicate",null);
        Instant now=Instant.now(); var e=new ${FeatureClassPrefix}SampleItemEntity();e.sampleKey=c.sampleKey();e.itemName=c.itemName();e.statusCode=c.statusCode();e.versionNo=0;e.idempotencyKey=idem;e.transactionId=txId;e.transactionSequence=sequence;e.transactionAt=now;e.deletedYn="N";e.createdBy=actor;e.createdAt=now;e.updatedBy=actor;e.updatedAt=now;em.persist(e);em.flush();
        persistReplay(idem,"CREATE",hash,e.sampleItemId,e.versionNo,false,txId); return dto(e);
    }
    @Override public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){
        String hash=requestHash("UPDATE",id,c.sampleKey(),c.itemName(),c.statusCode(),c.expectedVersion()); var replay=replay(idem,"UPDATE",hash,id);if(replay!=null)return requiredItem(id);
        var e=requiredEntity(id);if(e.versionNo!=c.expectedVersion())throw new CpfPersistenceException(CpfPersistenceFailureType.OPTIMISTIC_LOCK,"update","version conflict",null);
        e.sampleKey=c.sampleKey();e.itemName=c.itemName();e.statusCode=c.statusCode();e.idempotencyKey=idem;e.transactionId=txId;e.transactionSequence=sequence;e.transactionAt=Instant.now();e.updatedBy=actor;e.updatedAt=Instant.now();em.flush();
        persistReplay(idem,"UPDATE",hash,id,e.versionNo,false,txId);return dto(e);
    }
    @Override public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String txId,String idem,long sequence,String actor){
        String hash=requestHash("DELETE",id,"","","",version);var replay=replay(idem,"DELETE",hash,id);if(replay!=null)return new ${FeatureClassPrefix}DeleteResult(true,id,replay.resultVersion);
        var e=requiredEntity(id);if(e.versionNo!=version)throw new CpfPersistenceException(CpfPersistenceFailureType.OPTIMISTIC_LOCK,"delete","version conflict",null);
        e.deletedYn="Y";e.idempotencyKey=idem;e.transactionId=txId;e.transactionSequence=sequence;e.transactionAt=Instant.now();e.updatedBy=actor;e.updatedAt=Instant.now();em.flush();
        persistReplay(idem,"DELETE",hash,id,e.versionNo,true,txId);return new ${FeatureClassPrefix}DeleteResult(true,id,e.versionNo);
    }
    @Override public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long afterId,int size){
        int safe=Math.max(1,Math.min(size,200));var rows=em.createQuery("select e from ${FeatureClassPrefix}SampleItemEntity e where e.sampleItemId>:id and e.deletedYn='N' order by e.sampleItemId asc",${FeatureClassPrefix}SampleItemEntity.class).setParameter("id",afterId==null?0L:afterId).setMaxResults(safe+1).getResultList();boolean next=rows.size()>safe;var values=(next?rows.subList(0,safe):rows).stream().map(${FeatureClassPrefix}Repository::dto).toList();return new CpfSlice<>(values,0,safe,next);
    }
    @Override public boolean verifyRollback(${FeatureClassPrefix}SampleCommand c,String txId,String idem,long sequence,String actor){
        boolean before=findBySampleKey(c.sampleKey()).isPresent();boolean[] seen={false};tx.executeWithoutResult(status->{create(c,txId,idem,sequence,actor);seen[0]=findBySampleKey(c.sampleKey()).isPresent();status.setRollbackOnly();});em.clear();return seen[0]&&before==findBySampleKey(c.sampleKey()).isPresent()&&em.find(${FeatureClassPrefix}IdempotencyEntity.class,idem)==null;
    }
    private ${FeatureClassPrefix}SampleItem requiredItem(long id){return dto(requiredEntity(id));}
    private ${FeatureClassPrefix}SampleItemEntity requiredEntity(long id){var e=em.find(${FeatureClassPrefix}SampleItemEntity.class,id,LockModeType.OPTIMISTIC);if(e==null||"Y".equals(e.deletedYn))throw new CpfPersistenceException(CpfPersistenceFailureType.NOT_FOUND,"findById","Sample Item not found: "+id,null);return e;}
    private ${FeatureClassPrefix}IdempotencyEntity replay(String key,String op,String hash,long expected){var x=em.find(${FeatureClassPrefix}IdempotencyEntity.class,key);if(x==null)return null;if(!op.equals(x.operationCode)||!hash.equals(x.requestHash)||(expected>0&&x.sampleItemId!=expected))throw new IllegalStateException("동일 idempotencyKey에 다른 요청을 사용할 수 없습니다.");return x;}
    private void persistReplay(String key,String op,String hash,long id,long version,boolean deleted,String txId){var x=new ${FeatureClassPrefix}IdempotencyEntity();x.idempotencyKey=key;x.operationCode=op;x.requestHash=hash;x.sampleItemId=id;x.resultVersion=version;x.deletedYn=deleted?"Y":"N";x.transactionId=txId;x.createdAt=Instant.now();em.persist(x);}
    private String requestHash(String operation,long id,String sampleKey,String itemName,String statusCode,long version){String canonical=operation+'|'+id+'|'+sampleKey+'|'+itemName+'|'+statusCode+'|'+version;try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException("멱등 요청 Hash 생성 실패",ex);}}
    private static ${FeatureClassPrefix}SampleItem dto(${FeatureClassPrefix}SampleItemEntity e){return new ${FeatureClassPrefix}SampleItem(e.sampleItemId,e.sampleKey,e.itemName,e.statusCode,e.versionNo,e.idempotencyKey,e.transactionId,e.transactionSequence,e.transactionAt,e.createdBy,e.createdAt,e.updatedBy,e.updatedAt);}
}

@Entity(name="${FeatureClassPrefix}SampleItemEntity")
@Table(name="$($TablePrefix)_sample_item", schema="$SchemaName")
class ${FeatureClassPrefix}SampleItemEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="sample_item_id") long sampleItemId;
    @Column(name="sample_key",nullable=false,length=100,unique=true) String sampleKey;
    @Column(name="item_name",nullable=false,length=200) String itemName;
    @Column(name="status_code",nullable=false,length=30) String statusCode;
    @Version @Column(name="version_no",nullable=false) long versionNo;
    @Column(name="idempotency_key",nullable=false,length=180) String idempotencyKey;
    @Column(name="transaction_id",nullable=false,length=34) String transactionId;
    @Column(name="transaction_sequence",nullable=false) long transactionSequence;
    @Column(name="transaction_at",nullable=false) Instant transactionAt;
    @Column(name="deleted_yn",nullable=false,length=1) String deletedYn;
    @Column(name="created_by",nullable=false,length=100) String createdBy;
    @Column(name="created_at",nullable=false) Instant createdAt;
    @Column(name="updated_by",nullable=false,length=100) String updatedBy;
    @Column(name="updated_at",nullable=false) Instant updatedAt;
    protected ${FeatureClassPrefix}SampleItemEntity(){}
}

@Entity(name="${FeatureClassPrefix}IdempotencyEntity")
@Table(name="$($TablePrefix)_sample_item_idem", schema="$SchemaName")
class ${FeatureClassPrefix}IdempotencyEntity {
    @Id @Column(name="idempotency_key",length=180) String idempotencyKey;
    @Column(name="operation_code",nullable=false,length=20) String operationCode;
    @Column(name="request_hash",nullable=false,length=64) String requestHash;
    @Column(name="sample_item_id",nullable=false) long sampleItemId;
    @Column(name="result_version",nullable=false) long resultVersion;
    @Column(name="deleted_yn",nullable=false,length=1) String deletedYn;
    @Column(name="transaction_id",nullable=false,length=34) String transactionId;
    @Column(name="created_at",nullable=false) Instant createdAt;
    protected ${FeatureClassPrefix}IdempotencyEntity(){}
}
"@

$dataProvider = if ($DatabaseEnabled) { [string]$resolvedProviderBindings['data'] } else { '' }
$repository = if ($dataProvider -eq 'jdbc') { $jdbcRepository } elseif ($dataProvider -eq 'mybatis') { $myBatisRepository } elseif ($dataProvider -eq 'jpa') { $jpaRepository } else { throw "지원하지 않는 Data Provider입니다: $dataProvider" }

$searchResult = @"
package $FeaturePackage.dto;
import java.util.List;
/** Typed 검색 결과와 정규화된 조회 조건입니다. */
public record ${FeatureClassPrefix}SearchResult(List<${FeatureClassPrefix}SampleItem> items,${FeatureClassPrefix}SearchRequest criteria,long totalCount){public ${FeatureClassPrefix}SearchResult{items=items==null?List.of():List.copyOf(items);}}
"@

$deleteCommand = @"
package $FeaturePackage.dto;
import jakarta.validation.constraints.PositiveOrZero;
/** 낙관적 잠금 삭제 입력입니다. */
public record ${FeatureClassPrefix}DeleteCommand(@PositiveOrZero long expectedVersion){}
"@

$deleteResult = @"
package $FeaturePackage.dto;
/** 논리 삭제 결과입니다. */
public record ${FeatureClassPrefix}DeleteResult(boolean deleted,long sampleItemId,long deletedVersion){}
"@

$idempotencyEntry = @"
package $FeaturePackage.dto;

import java.time.Instant;

/** 변경 요청 Hash와 결과를 보존하는 Generated Domain 멱등 원장 DTO입니다. */
public record ${FeatureClassPrefix}IdempotencyEntry(
        String idempotencyKey,
        String operationCode,
        String requestHash,
        long sampleItemId,
        long resultVersion,
        String deletedYn,
        String transactionId,
        Instant createdAt) {
    public boolean deleted(){ return "Y".equalsIgnoreCase(deletedYn); }
}
"@

$batchRepositoryConfig = @"
package $BasePackage.config;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ${ModuleName} 배치 원천 메타를 CPF DB의 Spring Batch 표준 저장소에 기록합니다.
 *
 * <p>업무 데이터 트랜잭션과 배치 메타 트랜잭션을 분리해 업무 DB에
 * Spring Batch 내부 테이블이 생성되거나 조회되는 것을 방지합니다.</p>
 */
@Configuration
public class ${ModuleName}BatchRepositoryConfig extends DefaultBatchConfiguration {
    private final DataSource cpfDataSource;
    private final PlatformTransactionManager cpfTransactionManager;

    public ${ModuleName}BatchRepositoryConfig(
            @Qualifier("cpfDataSource") DataSource cpfDataSource,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager cpfTransactionManager) {
        this.cpfDataSource = cpfDataSource;
        this.cpfTransactionManager = cpfTransactionManager;
    }

    @Override
    protected DataSource getDataSource() {
        return cpfDataSource;
    }

    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return cpfTransactionManager;
    }

}
"@

$dto = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Request;'))
import com.cpf.core.api.base.CpfQuery;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSort;
import com.cpf.core.api.page.CpfSortDirection;
import java.util.Set;

/**
 * ${ModuleName} 조회 조건입니다.
 *
 * <p>정렬 컬럼은 whitelist로 제한해 SQL Injection을 차단합니다.</p>
 */
public record ${FeatureClassPrefix}SearchRequest(
        String keyword,
        String sortBy,
        String sortDirection,
        Integer page,
        Integer size) implements ${ModuleClassName}Request, CpfQuery {
    private static final Set<String> SORT_COLUMNS = Set.of("created_at", "updated_at", "sample_item_id", "item_name");

    public ${FeatureClassPrefix}SearchRequest normalized() {
        String normalizedSortBy = sortBy != null && SORT_COLUMNS.contains(sortBy) ? sortBy : "created_at";
        String normalizedDirection = "ASC".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        int normalizedPage = page == null || page < 0 ? 0 : page;
        int normalizedSize = size == null || size < 1 ? 20 : Math.min(size, 200);
        return new ${FeatureClassPrefix}SearchRequest(
                keyword, normalizedSortBy, normalizedDirection, normalizedPage, normalizedSize);
    }

    /** CPF 표준 Page 요청으로 변환합니다. Repository/EDU가 별도 Paging DTO를 만들지 않습니다. */
    public CpfPageRequest pageRequest() {
        ${FeatureClassPrefix}SearchRequest n = normalized();
        return new CpfPageRequest(n.page(), n.size());
    }

    /** 정규화된 allow-list field와 방향을 CPF 공개 정렬 계약으로 변환합니다. */
    public CpfSort sort() {
        ${FeatureClassPrefix}SearchRequest n = normalized();
        return new CpfSort(n.sortBy(), CpfSortDirection.from(n.sortDirection()));
    }

    public int offset() {
        return Math.toIntExact(pageRequest().offset());
    }
}
"@

$sampleCommand = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Request;'))
import com.cpf.core.api.security.CpfMasking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Generated Domain Minimal CRUD의 업무 입력 계약입니다.
 * transactionId/idempotencyKey/actor/sequence는 Body에 중복하지 않고 cpf-core TransactionContext에서 공급합니다.
 */
public record ${FeatureClassPrefix}SampleCommand(
        @NotBlank @Size(max = 100) String sampleKey,
        @NotBlank @Size(max = 200) String itemName,
        @Pattern(regexp = "ACTIVE|INACTIVE") String statusCode,
        @PositiveOrZero long expectedVersion) implements ${ModuleClassName}Request {
    public ${FeatureClassPrefix}SampleCommand {
        sampleKey = requireText(sampleKey,"sampleKey"); itemName=requireText(itemName,"itemName");
        statusCode=defaultText(statusCode,"ACTIVE").toUpperCase(Locale.ROOT);
        if(!statusCode.equals("ACTIVE")&&!statusCode.equals("INACTIVE")) throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        if(expectedVersion<0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
    }
    public String maskedAuditKey(){ return CpfMasking.mask(sampleKey); }
    private static String requireText(String value,String field){ if(value==null||value.isBlank()) throw new IllegalArgumentException(field+"는 필수입니다."); return value.trim(); }
    private static String defaultText(String value,String fallback){ return value==null||value.isBlank()?fallback:value.trim(); }
}
"@

$sampleItem = @"
package $FeaturePackage.dto;

$([string]::Concat('import ', $BasePackage, '.common.contract.', $ModuleClassName, 'Response;'))
import java.time.Instant;

/** Vendor와 무관한 Generated Domain Sample Item 논리 모델입니다. */
public record ${FeatureClassPrefix}SampleItem(
        long sampleItemId,
        String sampleKey,
        String itemName,
        String statusCode,
        long versionNo,
        String idempotencyKey,
        String transactionId,
        long transactionSequence,
        Instant transactionAt,
        String createdBy,
        Instant createdAt,
        String updatedBy,
        Instant updatedAt) implements ${ModuleClassName}Response {
}
"@

$validator = @"
package $FeaturePackage.validation;

import $FeaturePackage.dto.${FeatureClassPrefix}SearchRequest;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.stereotype.Component;

/**
 * ${ModuleName} 조회 API 입력값을 검증합니다.
 */
@Component
public class ${FeatureClassPrefix}SearchValidator {
    public void validate(${FeatureClassPrefix}SearchRequest request) {
        if (request == null) {
            throw new CpfValidationException("${ModuleName} 조회 조건은 필수입니다.");
        }
        if (request.size() != null && request.size() > 200) {
            throw new CpfValidationException("페이지 크기는 200 이하여야 합니다.");
        }
    }
}
"@

$batchDependency = ""

$platformDependencies = if ($DependencyModel -eq "published-artifact") {
@"
    implementation platform('com.cpf:cpf-platform-bom:$PlatformVersion')
"@
} else {
    ""
}


$profileProjectName = [string]$resolvedCapabilityProfile.aggregateProject
$profileArtifact = [string]$resolvedCapabilityProfile.aggregateArtifact
$profileDependency = if ($DependencyModel -eq "published-artifact") {
    "    implementation '$profileArtifact`:$PlatformVersion'"
} else {
    "    implementation project(':$profileProjectName')"
}

$embeddedProviderProjects = @($resolvedCapabilityProfile.embeddedRuntimeProjects | ForEach-Object { [string]$_ })
$embeddedProviderCoordinates = @($resolvedCapabilityProfile.embeddedRuntimeCoordinates | ForEach-Object { [string]$_ })
$providerDependencyProjects = @($resolvedProviderProjects | Where-Object { $_ -notin $embeddedProviderProjects })
$providerDependencyCoordinates = @($resolvedProviderCoordinates | Where-Object { $_ -notin $embeddedProviderCoordinates })

$providerDependencies = if ($DependencyModel -eq "published-artifact") {
    @($providerDependencyCoordinates | ForEach-Object { "    implementation '$_`:$PlatformVersion'" }) -join "`n"
} else {
    @($providerDependencyProjects | ForEach-Object { "    implementation project('$_')" }) -join "`n"
}

# 이용 Domain 승인 예외는 Registry에 고정된 정확한 Artifact Version만 조립합니다.
# Convention Plugin이 rule_id, Artifact, Version, Config Hash, 만료를 다시 검증합니다.
$approvedExceptionDependencies = @($approvedExceptionRows | ForEach-Object {
    "    implementation '$([string]$_.artifact):$([string]$_.version)' // $([string]$_.exception_id)"
}) -join "`n"

$batchContractDependency = if (($BatchEnabled -or $CenterCutEnabled) -and
        $DependencyModel -eq "published-artifact") {
    "    implementation 'com.cpf.batch:cpf-batch-contract:$PlatformVersion'"
} else {
    ""
}

$springBootPlugin = if ($DependencyModel -eq "published-artifact") {
    "    id 'org.springframework.boot' version '$springBootVersion'"
} else {
    "    id 'org.springframework.boot'"
}
$dependencyManagementPlugin = if ($DependencyModel -eq "published-artifact") {
    "    id 'io.spring.dependency-management' version '$dependencyManagementVersion'"
} else {
    "    id 'io.spring.dependency-management'"
}
$cpfConventionPlugin = if ($DependencyModel -eq "published-artifact") {
    "    id 'com.cpf.platform-conventions' version '$PlatformVersion'"
} else {
    "    id 'com.cpf.platform-conventions'"
}

$databaseDependencies = ""
$webDependencies = ""

$databaseVendorSelection = if ($DatabaseEnabled) {
@"
def cpfDomainMetadataFile = file('manifest/domain-manifest.json')
if (!cpfDomainMetadataFile.isFile()) {
    throw new GradleException("CPF Generated Domain metadata is missing: `${cpfDomainMetadataFile}")
}
def cpfDomainMetadata = new groovy.json.JsonSlurper().parse(cpfDomainMetadataFile)
def cpfDbVendor = (findProperty('cpfDbVendor')
        ?: System.getenv('${ModuleUpper}_DATABASE_VENDOR')
        ?: cpfDomainMetadata.databaseVendor)
        .toString()
        .toLowerCase(Locale.ROOT)
def cpfSupportedDbVendors = [$supportedDatabaseVendorsGradle] as Set
if (!cpfSupportedDbVendors.contains(cpfDbVendor)) {
    throw new GradleException("Unsupported cpfDbVendor: `${cpfDbVendor}")
}
def cpfJdbcDriverByVendor = [
$jdbcDriverMapGradle
]
def cpfFlywayDatabaseByVendor = [
$flywayDatabaseMapGradle
]
"@
} else {
    ""
}


$databaseProviderResourceAssembly = if ($dataProvider -eq 'jdbc') {
@"
    from(new File(cpfCentralDbPackRoot, "`${cpfDbVendor}/pack.json")) { into '.' }
    from(new File(cpfSelectedDomainTemplate, 'runtime/repository')) {
        include '**/*.template'; filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName -> fileName.replace('.template', '').replace('__DOMAIN__', '$module') }
        into "runtime/$module/repository"; includeEmptyDirs = false
    }
"@
} elseif ($dataProvider -eq 'mybatis') {
@"
    from(new File(cpfSelectedDomainTemplate, 'runtime/mybatis')) {
        include '**/*.template'; filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName -> fileName.replace('.template', '').replace('__DOMAIN__', '$module').replace('__MAPPER__', '${FeatureClassPrefix}Mapper') }
        into "mybatis/vendor/`${cpfDbVendor}/mapper/$module/sampleitem"; includeEmptyDirs = false
    }
"@
} else {
@"
    // JPA는 Entity/JPQL을 사용하고 vendor-specific runtime mapper를 생성하지 않습니다.
    // DDL/migration/verify/rollback은 동일 canonical 3-Vendor template를 그대로 조립합니다.
"@
}
$databaseProviderTemplateCheck = if ($dataProvider -eq 'jdbc') { 'runtime/repository' } elseif ($dataProvider -eq 'mybatis') { 'runtime/mybatis' } else { 'migration' }

$databaseResourceAssembly = if ($DatabaseEnabled) {
@"
def cpfCentralDbPackRoot = file(
        findProperty('cpfCentralDbPackRoot')
                ?: "${Dollar}{rootProject.projectDir}/$(
                    if ($DependencyModel -eq 'published-artifact') { 'cpf-db/vendor' }
                    else { 'cpf-tools/db/vendor' })")
def cpfSelectedDomainTemplate = new File(cpfCentralDbPackRoot, "`${cpfDbVendor}/domain-template")
def cpfGeneratedVendorResources = layout.buildDirectory.dir('generated-resources/cpf-vendor')

tasks.register('prepareCpfVendorResources', Sync) {
    def tokenValues = [
            CPF_VENDOR: cpfDbVendor,
            CPF_DOMAIN: '$module',
            CPF_SYSTEM_CODE: '$ModuleUpper',
            CPF_DISPLAY_NAME: '$ModuleName',
            CPF_SCHEMA_NAME: '$SchemaName',
            CPF_DATABASE_NAME: '$DatabaseName',
            CPF_MIGRATION_USERNAME: '$DatabaseMigrationUsername',
            CPF_RUNTIME_USERNAME: '$DatabaseRuntimeUsername',
            CPF_MODULE_NAME: '$ModuleName',
            CPF_PACKAGE_NAME: '$PackageName',
            CPF_TABLE_PREFIX: '$TablePrefix',
            CPF_MAPPER_NAMESPACE: '$FeaturePackage.mapper.${FeatureClassPrefix}Mapper',
            CPF_MAPPER_NAME: '${FeatureClassPrefix}Mapper',
            CPF_RESULT_TYPE: '$FeaturePackage.dto.${FeatureClassPrefix}SampleItem',
            CPF_IDEMPOTENCY_RESULT_TYPE: '$FeaturePackage.dto.${FeatureClassPrefix}IdempotencyEntry'
    ]
    into cpfGeneratedVendorResources
    from(cpfSelectedDomainTemplate) {
        // Provision/principal SQL contains secret injection points and belongs to
        // the external DB bootstrap tool, never to an application artifact.
        include 'install/**', 'seed/**', 'migration/**', 'verify/**', 'rollback/**'
        filter(org.apache.tools.ant.filters.ReplaceTokens, tokens: tokenValues)
        rename { fileName ->
            fileName
                    .replace('.template', '')
                    .replace('__DOMAIN__', '$module')
        }
        into "db/vendor/`${cpfDbVendor}"
        includeEmptyDirs = false
    }
$databaseProviderResourceAssembly
    doFirst {
        if (!cpfSelectedDomainTemplate.isDirectory()) {
            throw new GradleException(
                    "CPF central domain template is missing: `${cpfSelectedDomainTemplate}")
        }
        if (!new File(cpfSelectedDomainTemplate, '$databaseProviderTemplateCheck').isDirectory()) {
            throw new GradleException(
                    "CPF central domain provider template is missing: `${cpfSelectedDomainTemplate}/$databaseProviderTemplateCheck")
        }
    }
}

sourceSets.main.resources.srcDir(cpfGeneratedVendorResources)
tasks.named('processResources') {
    dependsOn tasks.named('prepareCpfVendorResources')
}
"@
} else {
    ""
}

$batchTransactionImport = if ($DatabaseEnabled) { "" } else {
    "import org.springframework.batch.support.transaction.ResourcelessTransactionManager;"
}
$batchTransactionBean = if ($DatabaseEnabled) { "" } else {
@"
    /** DB capability가 없는 Tasklet의 chunk 경계를 위한 비영속 트랜잭션 관리자입니다. */
    @Bean(name = "${module}TransactionManager")
    public PlatformTransactionManager ${module}TransactionManager() {
        return new ResourcelessTransactionManager();
    }

"@
}
$batchConfig = @"
package $FeaturePackage.batch;

import com.cpf.core.api.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
$batchTransactionImport

/**
 * ${ModuleName} 주제영역의 표준 Tasklet 배치 골격입니다.
 */
@Configuration
public class ${FeatureClassPrefix}BatchConfig {

$batchTransactionBean
    @Bean
    @CpfBatchJob(id = "B${DomainIdCode}TS0001", name = "${ModuleName}표준배치", ownerDomain = "$DomainIdCode")
    public Job ${module}StandardJob(JobRepository jobRepository, Step ${module}StandardStep) {
        return new JobBuilder("${ModuleUpper}_STANDARD_JOB", jobRepository)
                .start(${module}StandardStep)
                .build();
    }

    @Bean
    public Step ${module}StandardStep(
            JobRepository jobRepository,
            @Qualifier("${module}TransactionManager") PlatformTransactionManager transactionManager) {
        return new StepBuilder("${ModuleUpper}_STANDARD_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실제 업무 로직은 재시작 가능성과 멱등성을 보장하는 서비스에 위임합니다.
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
"@

$packagedArtifactIds = [System.Collections.Generic.List[string]]::new()
foreach ($artifactId in @('cpf-core', 'cpf-common', 'cpf-starter-foundation-base')) {
    if (-not $packagedArtifactIds.Contains($artifactId)) { $packagedArtifactIds.Add($artifactId) }
}
$profileArtifactId = ([string]$profileArtifact -split ':')[-1]
if (-not [string]::IsNullOrWhiteSpace($profileArtifactId) -and
        -not $packagedArtifactIds.Contains($profileArtifactId)) {
    $packagedArtifactIds.Add($profileArtifactId)
}
foreach ($coordinate in @($resolvedProviderCoordinates)) {
    $artifactId = ([string]$coordinate -split ':')[-1]
    if (-not [string]::IsNullOrWhiteSpace($artifactId) -and
            -not $packagedArtifactIds.Contains($artifactId)) {
        $packagedArtifactIds.Add($artifactId)
    }
}
if (($BatchEnabled -or $CenterCutEnabled) -and
        -not $packagedArtifactIds.Contains('cpf-batch-contract')) {
    $packagedArtifactIds.Add('cpf-batch-contract')
}
$packagedDependencyPrefixesGradle = @($packagedArtifactIds | Sort-Object | ForEach-Object {
    "'$($_)-'"
}) -join ", "

$buildGradle = @"
plugins {
    id 'java'
    id 'war'
$springBootPlugin
$dependencyManagementPlugin
$cpfConventionPlugin
}

group = '$BasePackage'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

$databaseVendorSelection

dependencies {
$platformDependencies
$profileDependency
$providerDependencies
$approvedExceptionDependencies
$batchContractDependency
$webDependencies
$databaseDependencies
$batchDependency
}

tasks.named('test') {
    useJUnitPlatform()
}

dependencyLocking {
    lockAllConfigurations()
}

$databaseResourceAssembly

tasks.withType(AbstractArchiveTask).configureEach {
    archiveBaseName = "$projectName"
    preserveFileTimestamps = false
    reproducibleFileOrder = true
}

tasks.named('bootJar') {
    enabled = true
}

tasks.named('bootWar') {
    enabled = $($OnlineEnabled.ToString().ToLowerInvariant())
}

def cpfRequiredPackagedDependencyPrefixes = [$packagedDependencyPrefixesGradle]

def cpfAssertPackagedDependencies = { File archiveFile, String libraryRoot ->
    if (!archiveFile.isFile()) {
        throw new GradleException("CPF packaged artifact가 없습니다: `${archiveFile}")
    }
    def entries = []
    new java.util.zip.ZipFile(archiveFile).withCloseable { zip ->
        entries = zip.entries().collect { it.name }
    }
    def missing = cpfRequiredPackagedDependencyPrefixes.findAll { prefix ->
        !entries.any { entry -> entry.startsWith("`${libraryRoot}/`${prefix}") && entry.endsWith('.jar') }
    }
    if (!missing.isEmpty()) {
        throw new GradleException("CPF public dependency가 실행 Artifact에 포함되지 않았습니다. artifact=`${archiveFile.name}, missing=`${missing}")
    }
}

tasks.register('verifyCpfPackagedDependencies') {
    group = 'verification'
    description = 'bootJar/bootWar에 동일 CPF public dependency가 실제 포함되는지 검증합니다.'
    dependsOn tasks.named('bootJar'), tasks.named('bootWar')
    doLast {
        cpfAssertPackagedDependencies(tasks.named('bootJar').get().archiveFile.get().asFile, 'BOOT-INF/lib')
        if (tasks.named('bootWar').get().enabled) {
            cpfAssertPackagedDependencies(tasks.named('bootWar').get().archiveFile.get().asFile, 'WEB-INF/lib')
        }
    }
}

tasks.named('check') {
    dependsOn tasks.named('verifyCpfPackagedDependencies')
}
"@

$applicationJava = if ($OnlineEnabled) {
@"
package $BasePackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ${ModuleClassName}Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(${ModuleClassName}Application.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(${ModuleClassName}Application.class);
    }
}
"@
} else {
@"
package $BasePackage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${ModuleClassName}Application {
    public static void main(String[] args) {
        SpringApplication.run(${ModuleClassName}Application.class, args);
    }
}
"@
}

# Unselected capability policy: NO_DEPENDENCY_NO_BEAN_NO_CONFIG_NO_SQL
# DataSource/MyBatis infrastructure is owned by CPF persistence Starters, never generated per domain.

$databaseSpringYml = if ($DatabaseEnabled) {
@"
  flyway:
    # DDL migration은 app 계정이 아니라 별도 migration 절차에서 실행합니다.
    enabled: ${Dollar}{$($ModuleUpper)_FLYWAY_ENABLED:false}
    locations: classpath:db/vendor/${Dollar}{$($ModuleUpper)_DATABASE_VENDOR:$DatabaseVendor}/migration
"@
} else { "" }
$applicationYml = @"
spring:
  application:
    name: $projectName
$databaseSpringYml
  batch:
    # 배치 Job은 운영 실행 요청으로만 시작하고 애플리케이션 기동 시 자동 실행하지 않습니다.
    job:
      enabled: false
    # Spring Batch 메타 스키마는 cpfDB 설치 SQL이 관리합니다.
    jdbc:
      initialize-schema: never
  config:
    import:
      - optional:classpath:application-cpf.yml
      - optional:classpath:application-cpf-${Dollar}{spring.profiles.active:local}.yml
      - optional:classpath:application-$module.yml
      - optional:classpath:application-$module-${Dollar}{spring.profiles.active:local}.yml

server:
  # productionProfile=false인 Golden/개발 Domain도 Manifest port 계약을 그대로 소비합니다.
  port: ${Dollar}{$($ModuleUpper)_SERVER_PORT:$Port}

cpf:
  common:
    # product에서는 CMN DB/Calendar 등 공식 cpf-common 계약을 fail-closed로 사용합니다.
    runtime-mode: ${Dollar}{CPF_COMMON_RUNTIME_MODE:product}
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  logging:
    file:
      base-path: ${Dollar}{CPF_LOG_ROOT:}
      timezone: ${Dollar}{CPF_LOG_TIMEZONE:Asia/Seoul}
      max-history-days: ${Dollar}{CPF_LOG_MAX_HISTORY_DAYS:30}
      archive-compress-enabled: ${Dollar}{CPF_LOG_ARCHIVE_COMPRESS_ENABLED:true}
      file-pattern: "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log"
logging:
  config: classpath:log/cpf-logback-spring.xml
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
"@
$defaultSampleItemMode = if ($DatabaseEnabled) { 'local' } elseif ($ExternalEnabled) { 'remote' } else { 'memory' }
$moduleDataSourceYml = if ($DatabaseEnabled) {
@"
    datasource:
      mode: ${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}
      jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:$DataSourceJndiName}
      database-name: ${Dollar}{$($ModuleUpper)_DATABASE_NAME:$SchemaName}
      url: '${Dollar}{$($ModuleUpper)_DATASOURCE_URL:}'
      driver-class-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_DRIVER_CLASS_NAME:}
      username: ${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}
      password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD:}
"@
} else { "" }

$selectedProviderRuntimeYml = {
    $lines = [System.Collections.Generic.List[string]]::new()
    if ($resolvedProviderBindings.Contains('messaging')) {
        $provider = [string]$resolvedProviderBindings['messaging']
        [void]$lines.Add('  messaging:')
        switch ($provider) {
            'kafka' {
                [void]$lines.Add('    kafka:')
                [void]$lines.Add('      binding-name: kafka')
                [void]$lines.Add('      default-binding: true')
                [void]$lines.Add('      require-idempotence: true')
                [void]$lines.Add('      acknowledgement-timeout: ${CPF_KAFKA_ACK_TIMEOUT:10s}')
                [void]$lines.Add('      maximum-payload-bytes: ${CPF_KAFKA_MAX_PAYLOAD_BYTES:1048576}')
            }
            'rabbitmq' {
                [void]$lines.Add('    rabbitmq:')
                [void]$lines.Add('      enabled: true')
                [void]$lines.Add('      binding-name: rabbitmq')
                [void]$lines.Add('      default-binding: true')
                [void]$lines.Add('      exchange: ${CPF_RABBITMQ_EXCHANGE:' + $module + '.events}')
                [void]$lines.Add('      queue: ${CPF_RABBITMQ_QUEUE:' + $module + '.events}')
                [void]$lines.Add('      routing-key: ${CPF_RABBITMQ_ROUTING_KEY:' + $module + '.#}')
                [void]$lines.Add('      exchange-type: ${CPF_RABBITMQ_EXCHANGE_TYPE:topic}')
            }
            'jms' {
                [void]$lines.Add('    jms:')
                [void]$lines.Add('      enabled: true')
                [void]$lines.Add('      binding-name: jms')
                [void]$lines.Add('      default-binding: true')
                [void]$lines.Add('      destination: ${CPF_JMS_DESTINATION:' + $module + '.events}')
            }
            'ibm-mq' {
                [void]$lines.Add('    ibm-mq:')
                [void]$lines.Add('      enabled: true')
                [void]$lines.Add('      binding-name: ibm-mq')
                [void]$lines.Add('      default-binding: true')
                [void]$lines.Add('      destination: ${CPF_IBM_MQ_DESTINATION:}')
                [void]$lines.Add('      queue-manager: ${CPF_IBM_MQ_QUEUE_MANAGER:}')
                [void]$lines.Add('      channel: ${CPF_IBM_MQ_CHANNEL:}')
                [void]$lines.Add('      connection-name: ${CPF_IBM_MQ_CONNECTION_NAME:}')
                [void]$lines.Add('      ccdt-url: ${CPF_IBM_MQ_CCDT_URL:}')
                [void]$lines.Add('      tls-required: ${CPF_IBM_MQ_TLS_REQUIRED:true}')
            }
            default { throw "지원하지 않는 Messaging Provider입니다: $provider" }
        }
        [void]$lines.Add('    reliability:')
        [void]$lines.Add('      enabled: true')
        [void]$lines.Add('      schema-required: true')
    }
    if ($resolvedProviderBindings.Contains('file')) {
        $provider = [string]$resolvedProviderBindings['file']
        if ($provider -ne 'sftp') { throw "지원하지 않는 File Provider입니다: $provider" }
        [void]$lines.Add('  integration:')
        [void]$lines.Add('    sftp:')
        [void]$lines.Add('      enabled: true')
        [void]$lines.Add('      host: ${CPF_SFTP_HOST:}')
        [void]$lines.Add('      port: ${CPF_SFTP_PORT:22}')
        [void]$lines.Add('      username: ${CPF_SFTP_USERNAME:}')
        [void]$lines.Add('      password-secret: ${CPF_SFTP_PASSWORD_SECRET:}')
        [void]$lines.Add('      local-root: ${CPF_SFTP_LOCAL_ROOT:.}')
        [void]$lines.Add('      remote-root: ${CPF_SFTP_REMOTE_ROOT:/}')
        [void]$lines.Add('      ledger-required: true')
    }
    if ($resolvedProviderBindings.Contains('notification')) {
        $provider = [string]$resolvedProviderBindings['notification']
        [void]$lines.Add('  notification:')
        [void]$lines.Add('    dispatch:')
        [void]$lines.Add('      enabled: true')
        switch ($provider) {
            'email' {
                [void]$lines.Add('    email:')
                [void]$lines.Add('      enabled: true')
                [void]$lines.Add('      from: ${CPF_NOTIFICATION_EMAIL_FROM:}')
            }
            'sms-spi' {
                [void]$lines.Add('    sms:')
                [void]$lines.Add('      enabled: true')
                [void]$lines.Add('      provider: ${CPF_NOTIFICATION_SMS_PROVIDER:}')
            }
            default { throw "지원하지 않는 Notification Provider입니다: $provider" }
        }
    }
    if ($lines.Count -eq 0) { return '' }
    return ($lines -join "`n") + "`n"
}.Invoke()

$approvedExceptionRuntimeYml = if ($approvedExceptionRows.Count -gt 0) {
    $lines = [System.Collections.Generic.List[string]]::new()
    [void]$lines.Add('  generated-domain:')
    [void]$lines.Add('    # 승인 예외가 있으면 배포 환경을 명시해야 하며 Registry의 environments 범위를 벗어나면 시작이 차단됩니다.')
    [void]$lines.Add('    environment: ${CPF_TARGET_ENVIRONMENT:' + $TargetEnvironment + '}')
    [void]$lines.Add('    approved-exceptions:')
    foreach ($exception in $approvedExceptionRows) {
        $exceptionId = [string]$exception.exception_id
        $environmentKey = ($exceptionId.ToUpperInvariant() -replace '[^A-Z0-9]', '_')
        [void]$lines.Add("      `"$exceptionId`":")
        [void]$lines.Add("        artifact-version: `${CPF_APPROVED_EXCEPTION_${environmentKey}_ARTIFACT_VERSION:}")
        [void]$lines.Add("        config-hash: `${CPF_APPROVED_EXCEPTION_${environmentKey}_CONFIG_HASH:}")
    }
    ($lines -join "`n") + "`n"
} else { '' }

$applicationModuleYml = @"
# ${ModuleName} 주제영역 공통 설정입니다.
cpf:
  domain:
    persistence:
      enabled: $($DatabaseEnabled.ToString().ToLowerInvariant())
      required: $($DatabaseEnabled.ToString().ToLowerInvariant())
      provider: $(if ($DatabaseEnabled) { $dataProvider } else { 'none' })
      data-source-prefix: cpf.$module.datasource
$selectedProviderRuntimeYml$approvedExceptionRuntimeYml  db:
    # Vendor 선택은 Java 업무 Source가 아니라 SQL/Mapper resource 경로만 변경합니다.
    vendor: ${Dollar}{$($ModuleUpper)_DATABASE_VENDOR:$DatabaseVendor}
    # Local/CI는 Generator 산출 Pack을 사용하고, 배포 환경은 외부 Pack mount 경로를 명시적으로 Override합니다.
    resource-root: '${Dollar}{$($ModuleUpper)_CPF_DB_RESOURCE_ROOT:${Dollar}{CPF_DB_RESOURCE_ROOT:${Dollar}{user.dir}/build/generated-resources/cpf-vendor}}'
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
  ${module}:
$moduleDataSourceYml
    sample-item:
      mode: ${Dollar}{$($ModuleUpper)_SAMPLE_ITEM_MODE:$defaultSampleItemMode}
  logging:
    file:
      file-pattern: "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log"
"@
$runtimeAgentApplicationYml = @"
# Runtime Control Agent는 명시적으로 runtime-agent profile을 활성화한 배포에서만 동작합니다.
spring:
  config:
    activate:
      on-profile: runtime-agent

cpf:
  runtime:
    instance-id: ${Dollar}{CPF_RUNTIME_INSTANCE_ID}
    service-id: ${Dollar}{CPF_RUNTIME_SERVICE_ID:$ModuleUpper}
    endpoint-code: ${Dollar}{CPF_RUNTIME_ENDPOINT_CODE:$ModuleUpper}
    base-url: ${Dollar}{CPF_RUNTIME_BASE_URL}
    environment: ${Dollar}{CPF_RUNTIME_ENVIRONMENT:${Dollar}{spring.profiles.active:default}}
    zone: ${Dollar}{CPF_RUNTIME_ZONE:}
    cell: ${Dollar}{CPF_RUNTIME_CELL:}
    role: ${Dollar}{CPF_RUNTIME_ROLE:APPLICATION}
    control:
      base-url: ${Dollar}{CPF_RUNTIME_CONTROL_BASE_URL}
      agent-token: ${Dollar}{CPF_RUNTIME_CONTROL_AGENT_TOKEN}
      agent:
        enabled: true
        inbox-path: ${Dollar}{CPF_RUNTIME_AGENT_INBOX:${Dollar}{java.io.tmpdir}/cpf-runtime-inbox}
"@

$runtimeAgentDescriptor = @"
{
  "contractVersion": "1.0",
  "module": "$ModuleUpper",
  "serviceId": "$ModuleUpper",
  "activationProfile": "runtime-agent",
  "applicationConfig": "src/main/resources/application-runtime-agent.yml",
  "capabilityDiscovery": "CpfRuntimeChangeApplier",
  "enabledByDefault": false,
  "failClosed": true,
  "requiredEnvironment": [
    "CPF_RUNTIME_INSTANCE_ID",
    "CPF_RUNTIME_SERVICE_ID",
    "CPF_RUNTIME_ENDPOINT_CODE",
    "CPF_RUNTIME_BASE_URL",
    "CPF_RUNTIME_CONTROL_BASE_URL",
    "CPF_RUNTIME_CONTROL_AGENT_TOKEN"
  ]
}
"@

$readme = @"
# ${ModuleName} 주제영역 골격

이 디렉터리는 `cpf-tools/generator/create-domain.ps1`로 생성한 신규 업무 모듈 후보입니다.

- 실제 반영 전 `settings.gradle`, `cpf-tools/db/generated/database-schema-manifest.json`, 선택 Vendor Pack, ADM/BZA 연동과 OpenAPI 문서를 함께 검토합니다.
- Controller, Facade, Service, Repository, DTO, Mapper XML, SQL의 모듈 코드와 테이블 prefix를 일치시킵니다.
- 운영 로그는 `${Dollar}{CPF_LOG_ROOT}/{environment}/{moduleCode}/{instanceId}/{category}/cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log` 규칙을 사용합니다.
"@

$uiComponent = @"
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { search${FeatureClassPrefix}, type ${FeatureClassPrefix}SampleItemView } from './${FeatureClassPrefix}Api'

const loading = ref(false)
const rows = ref<${FeatureClassPrefix}SampleItemView[]>([])

async function load(): Promise<void> {
  loading.value = true
  try {
    rows.value = await search${FeatureClassPrefix}()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section aria-labelledby="${module}-sample-item-title">
    <h1 id="${module}-sample-item-title">${ModuleName} 참조 조회</h1>
    <button type="button" :disabled="loading" @click="load">조회</button>
    <p v-if="loading" role="status">조회 중</p>
    <table v-else>
      <caption>${ModuleName} 참조 결과</caption>
      <tbody>
        <tr v-for="(row, index) in rows" :key="index">
          <td>{{ row }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
"@

$uiApi = @"
export interface ${FeatureClassPrefix}SampleItemView {
  sampleItemId: number
  sampleKey: string
  itemName: string
  statusCode: string
  versionNo: number
  transactionId: string
  transactionSequence: number
  createdAt: string
  updatedAt: string
}

interface ${FeatureClassPrefix}SearchResponse {
  items: ${FeatureClassPrefix}SampleItemView[]
  totalCount: number
}

/** ${ModuleName} 참조 API를 호출하고 Typed items만 화면에 전달합니다. */
export async function search${FeatureClassPrefix}(): Promise<${FeatureClassPrefix}SampleItemView[]> {
  const response = await fetch('/api/v1/$module/sample-items?page=0&size=20&sortBy=created_at&sortDirection=DESC', {
    headers: { Accept: 'application/json' },
    credentials: 'same-origin',
  })
  if (!response.ok) {
    throw new Error('${ModuleName} 참조 조회에 실패했습니다.')
  }
  const body = (await response.json()) as ${FeatureClassPrefix}SearchResponse
  return Array.isArray(body.items) ? body.items : []
}
"@

$serviceTest = @"
package $FeaturePackage.service;

import $FeaturePackage.dto.*;
import $FeaturePackage.port.*;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.page.CpfSlice;
import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class ${FeatureClassPrefix}ServiceTest {
    private final StubPort port=new StubPort();
    private final ${FeatureClassPrefix}Service service=new ${FeatureClassPrefix}Service(port,port);
    @BeforeEach void context(){CpfTransactionContext.initializeForTest("20260724123456789${DomainIdCode}00000010000001","${ModuleUpper}_IDEMPOTENCY_001","generator-test");}
    @AfterEach void clear(){CpfTransactionContext.clear();}
    @Test void queryAndCommandPortsRemainSeparatedAndTyped(){var command=new ${FeatureClassPrefix}SampleCommand("${ModuleUpper}_001","Sample","ACTIVE",0);var created=service.create(command);assertThat(created.sampleKey()).isEqualTo("${ModuleUpper}_001");assertThat(service.findBySampleKey(created.sampleKey())).contains(created);}
    @Test void sameIdempotencyKeyAndSameRequestReplaysResult(){var command=new ${FeatureClassPrefix}SampleCommand("${ModuleUpper}_REPLAY","Replay","ACTIVE",0);var first=service.create(command);var second=service.create(command);assertThat(second).isEqualTo(first);}
    @Test void sameIdempotencyKeyAndDifferentRequestIsRejected(){service.create(new ${FeatureClassPrefix}SampleCommand("${ModuleUpper}_A","A","ACTIVE",0));assertThatThrownBy(()->service.create(new ${FeatureClassPrefix}SampleCommand("${ModuleUpper}_B","B","ACTIVE",0))).isInstanceOf(IllegalStateException.class);}
    @Test void rollbackVerificationRestoresTheOriginalState(){var command=new ${FeatureClassPrefix}SampleCommand("${ModuleUpper}_ROLLBACK","Rollback","ACTIVE",0);assertThat(service.verifyRollback(command)).isTrue();assertThat(service.findBySampleKey(command.sampleKey())).isEmpty();}
    private final class StubPort implements ${FeatureClassPrefix}QueryPort,${FeatureClassPrefix}CommandPort {
        private ${FeatureClassPrefix}SampleItem item; private String idem; private String request;
        public ${FeatureClassPrefix}SearchResult search(${FeatureClassPrefix}SearchRequest r){return new ${FeatureClassPrefix}SearchResult(item==null?List.of():List.of(item),r,item==null?0:1);}
        public Optional<${FeatureClassPrefix}SampleItem> findBySampleKey(String key){return Optional.ofNullable(item).filter(v->v.sampleKey().equals(key));}
        public CpfSlice<${FeatureClassPrefix}SampleItem> cursor(Long after,int size){return new CpfSlice<>(item==null?List.of():List.of(item),0,size,false);}
        public ${FeatureClassPrefix}SampleItem create(${FeatureClassPrefix}SampleCommand c,String tx,String key,long seq,String actor){String canonical=c.sampleKey()+"|"+c.itemName()+"|"+c.statusCode()+"|"+c.expectedVersion();if(Objects.equals(idem,key)){if(!Objects.equals(request,canonical))throw new IllegalStateException("idempotency conflict");return item;}if(item!=null&&item.sampleKey().equals(c.sampleKey()))throw new IllegalStateException("sampleKey duplicate");Instant now=Instant.now();item=new ${FeatureClassPrefix}SampleItem(1,c.sampleKey(),c.itemName(),c.statusCode(),0,key,tx,seq,now,actor,now,actor,now);idem=key;request=canonical;return item;}
        public ${FeatureClassPrefix}SampleItem update(long id,${FeatureClassPrefix}SampleCommand c,String tx,String key,long seq,String actor){if(item==null||item.sampleItemId()!=id)throw new IllegalArgumentException("not found");if(item.versionNo()!=c.expectedVersion())throw new IllegalStateException("version conflict");Instant now=Instant.now();item=new ${FeatureClassPrefix}SampleItem(id,c.sampleKey(),c.itemName(),c.statusCode(),item.versionNo()+1,key,tx,seq,now,item.createdBy(),item.createdAt(),actor,now);return item;}
        public ${FeatureClassPrefix}DeleteResult delete(long id,long version,String tx,String key,long seq,String actor){if(item==null||item.sampleItemId()!=id||item.versionNo()!=version)throw new IllegalStateException("version conflict");item=null;idem=key;return new ${FeatureClassPrefix}DeleteResult(true,id,version+1);}
        public boolean verifyRollback(${FeatureClassPrefix}SampleCommand c,String tx,String key,long seq,String actor){var before=item;var beforeIdem=idem;var beforeRequest=request;try{create(c,tx,key,seq,actor);}finally{item=before;idem=beforeIdem;request=beforeRequest;}return Objects.equals(before,item)&&Objects.equals(beforeIdem,idem)&&Objects.equals(beforeRequest,request);}
    }
}
"@

$messagingPublisher = @"
package $FeaturePackage.messaging;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * ${ModuleName} 업무 이벤트를 CPF 공개 transactional-outbox API로 등록합니다.
 * 업무 코드는 broker 종류와 Outbox 내부 저장소를 직접 참조하지 않습니다.
 */
@Service
public class ${ModuleName}EventPublisher {
    private final CpfBrokerClient brokerClient;
    public ${ModuleName}EventPublisher(CpfBrokerClient brokerClient) {
        this.brokerClient = Objects.requireNonNull(brokerClient, "brokerClient는 필수입니다.");
    }
    public CpfBrokerPublishResult publish(String messageId,String topic,String transactionId,String payload) {
        return brokerClient.enqueue(new CpfBrokerPublishRequest(
                messageId, topic, messageId,
                payload == null ? new byte[0] : payload.getBytes(StandardCharsets.UTF_8),
                "application/json", transactionId, null, "$SystemCode", null, messageId,
                Map.of("cpf-source-system", "$SystemCode"), Map.of("domainName", "$module")));
    }
}
"@

$messagingController = @"
package $FeaturePackage.messaging;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** ${ModuleName} 이벤트 outbox 적재 API입니다. */
@RestController
@RequestMapping("/api/v1/$module/events")
public class ${ModuleName}MessagingController extends ${ModuleClassName}BaseController {
    private final ${ModuleName}EventPublisher publisher;
    public ${ModuleName}MessagingController(${ModuleName}EventPublisher publisher) { this.publisher = publisher; }

    @PostMapping
    @CpfOnlineTransaction(id = "O${DomainIdCode}EV0001", name = "${ModuleName}EventPublish", ownerDomain = "$DomainIdCode", requiredPermission = "$SystemCode:EVENT:PUBLISH")
    @Operation(operationId = "publish${ModuleName}Event", summary = "${ModuleName} 업무 이벤트 등록")
    public ResponseEntity<CpfBrokerPublishResult> publish(@RequestBody Map<String,String> request) {
        return ok(publisher.publish(request.get("messageId"),request.get("topic"),request.get("transactionId"),request.get("payload")));
    }
}
"@

$messagingTest = @"
package $FeaturePackage.messaging;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ${ModuleName}EventPublisherTest {
    @Test void savesTraceableRequestThroughPublicBrokerBoundary() {
        AtomicReference<CpfBrokerPublishRequest> captured=new AtomicReference<>();
        CpfBrokerClient client=request->{ captured.set(request); return new CpfBrokerPublishResult("ACCEPTED",request.messageId(),"outbox",request.key(),Instant.now(),null); };
        CpfBrokerPublishResult result=new ${ModuleName}EventPublisher(client).publish("MSG-1","${module}.changed","TX-1","{\"id\":1}");
        assertThat(result.status()).isEqualTo("ACCEPTED");
        assertThat(captured.get().producerModule()).isEqualTo("$SystemCode");
        assertThat(captured.get().transactionId()).isEqualTo("TX-1");
    }
    @Test void rejectsMissingMessageIdBeforeRuntimeAccess() {
        CpfBrokerClient unused=request->{ throw new AssertionError(); };
        assertThatThrownBy(()->new ${ModuleName}EventPublisher(unused).publish(" ","topic","TX-1","{}"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
"@

$fileTransferService = @"
package $FeaturePackage.file;

import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

/** ${ModuleName} 파일 송수신 요청을 CPF 공개 파일전송 경계에 위임합니다. */
@Service
public class ${ModuleName}FileTransferService {
    private final CpfFileTransferClient client;
    public ${ModuleName}FileTransferService(CpfFileTransferClient client) { this.client=Objects.requireNonNull(client,"client는 필수입니다."); }
    public CpfFileResult execute(CpfFileEndpoint endpoint,CpfFileRequest request) {
        return client.execute(Objects.requireNonNull(endpoint,"endpoint는 필수입니다."),Objects.requireNonNull(request,"request는 필수입니다."));
    }
}
"@

$fileTransferTest = @"
package $FeaturePackage.file;

import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ${ModuleName}FileTransferServiceTest {
    @Test void delegatesThroughPublicFileTransferBoundary() {
        AtomicBoolean called=new AtomicBoolean();
        CpfFileTransferClient client=(endpoint,request)->{ called.set(true); return new CpfFileResult("SUCCESS",endpoint.endpointCode(),request.localPath(),request.remotePath(),request.checksum(),request.fileSize(),Instant.now(),null); };
        CpfFileEndpoint endpoint=new CpfFileEndpoint("SFTP-1","SFTP","localhost",22,"/upload",null,Duration.ofSeconds(3),Map.of());
        CpfFileRequest request=new CpfFileRequest("TX-1","SEG-1","SFTP-1","UPLOAD","local.dat","remote.dat","sha256",10L,Map.of());
        CpfFileResult actual=new ${ModuleName}FileTransferService(client).execute(endpoint,request);
        assertThat(actual.status()).isEqualTo("SUCCESS");
        assertThat(called).isTrue();
    }
}
"@

$securityAuditGuard = @"
package $FeaturePackage.security;

import org.springframework.stereotype.Component;

/** 운영 변경 API가 감사 사유를 빠뜨리지 않도록 공통 검증합니다. */
@Component
public class ${ModuleName}OperationGuard {
    public String requireAuditReason(String auditReason) {
        if (auditReason == null || auditReason.isBlank()) {
            throw new IllegalArgumentException("감사 사유는 필수입니다.");
        }
        String normalized = auditReason.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("감사 사유는 500자 이하여야 합니다.");
        }
        return normalized;
    }
}
"@

$securityAuditController = @"
package $FeaturePackage.security;

$([string]::Concat('import ', $BasePackage, '.common.base.', $ModuleClassName, 'BaseController;'))
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 권한과 감사 사유가 필요한 운영 변경 API의 참조 구현입니다. */
@RestController
@RequestMapping("/api/v1/$module/operations")
public class ${ModuleName}OperationController extends ${ModuleClassName}BaseController {
    private final ${ModuleName}OperationGuard guard;

    public ${ModuleName}OperationController(${ModuleName}OperationGuard guard) {
        this.guard = guard;
    }

    @PostMapping("/validate")
    @CpfOnlineTransaction(
            id = "O${DomainIdCode}OP0001",
            name = "${ModuleName}OperationValidate",
            ownerDomain = "$DomainIdCode",
            requiredPermission = "$SystemCode:OPERATION:EXECUTE",
            auditReasonRequired = true)
    @Operation(operationId = "validate${ModuleName}Operation", summary = "운영 변경 감사 사유 검증")
    public ResponseEntity<Map<String, String>> validate(@RequestBody Map<String, String> request) {
        return ok(Map.of("auditReason", guard.requireAuditReason(request.get("auditReason"))));
    }
}
"@

$securityAuditTest = @"
package $FeaturePackage.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ${ModuleName}OperationGuardTest {
    private final ${ModuleName}OperationGuard guard = new ${ModuleName}OperationGuard();

    @Test
    void normalizesRequiredAuditReason() {
        assertThat(guard.requireAuditReason("  승인된 운영 변경  ")).isEqualTo("승인된 운영 변경");
    }

    @Test
    void rejectsMissingAuditReason() {
        assertThatThrownBy(() -> guard.requireAuditReason(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
"@

$smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:$Port",
    [int] `$TimeoutSec = 20
)

`$ErrorActionPreference = "Stop"
`$uri = "`$BaseUrl/api/v1/$module/sample-items?keyword=sample&page=0&size=20&sortBy=created_at&sortDirection=DESC"
`$response = Invoke-WebRequest -Method Get -Uri `$uri -TimeoutSec `$TimeoutSec -UseBasicParsing
if ([int] `$response.StatusCode -lt 200 -or [int] `$response.StatusCode -ge 300) {
    throw "${ModuleName} smoke failed. status=`$(`$response.StatusCode)"
}
Write-Host "${ModuleName} smoke passed. uri=`$uri"
"@

if (-not $OnlineEnabled) {
    $smokeScript = @"
param(
    [string] `$BaseUrl = "http://localhost:$Port",
    [int] `$TimeoutSec = 20
)

`$ErrorActionPreference = "Stop"
`$uri = "`$BaseUrl/actuator/health"
`$response = Invoke-WebRequest -Method Get -Uri `$uri -TimeoutSec `$TimeoutSec -UseBasicParsing
if ([int] `$response.StatusCode -ne 200) {
    throw "${ModuleName} health smoke failed. status=`$(`$response.StatusCode)"
}
Write-Host "${ModuleName} health smoke passed. uri=`$uri"
"@
}

$onlineJson = $OnlineEnabled.ToString().ToLowerInvariant()
$databaseJson = $DatabaseEnabled.ToString().ToLowerInvariant()
$batchJson = $BatchEnabled.ToString().ToLowerInvariant()
$centerCutJson = $CenterCutEnabled.ToString().ToLowerInvariant()
$externalJson = $ExternalEnabled.ToString().ToLowerInvariant()
$uiJson = $UiEnabled.ToString().ToLowerInvariant()
$bzaMenuJson = $BzaMenuEnabled.ToString().ToLowerInvariant()
$DatabaseTemplatePack = if ($DependencyModel -eq "published-artifact") {
    "cpf-db/vendor/{vendor}/domain-template"
} else {
    "cpf-tools/db/vendor/{vendor}/domain-template"
}
$DatabaseBootstrapScript = if ($DependencyModel -eq "published-artifact") {
    "cpf-db/initialize-domain-database.ps1"
} else {
    "cpf-tools/scripts/initialize-domain-database.ps1"
}
$resolvedStartersJson = $resolvedStarters | ConvertTo-Json -Compress
$resolvedStarterVersions = [ordered]@{}
foreach ($resolvedStarter in $resolvedStarters) { $resolvedStarterVersions[$resolvedStarter] = $PlatformVersion }
$resolvedStarterVersionsJson = $resolvedStarterVersions | ConvertTo-Json -Compress
$resolvedCapabilityGroupsJson = @($resolvedCapabilityGroups) | ConvertTo-Json -Compress
$resolvedProviderBindingsJson = $resolvedProviderBindings | ConvertTo-Json -Compress
$resolvedProviderProjectsJson = @($resolvedProviderProjects) | ConvertTo-Json -Compress
$resolvedProviderCoordinatesJson = @($resolvedProviderCoordinates) | ConvertTo-Json -Compress
$approvedExceptionsForLock = @($approvedExceptionRows | ForEach-Object {
    [ordered]@{
        exceptionId = [string]$_.exception_id
        capability = [string]$_.capability
        artifact = [string]$_.artifact
        version = [string]$_.version
        owner = [string]$_.owner
        approvedBy = [string]$_.approved_by
        approvedAt = [string]$_.approved_at
        expiresAt = [string]$_.expires_at
        environments = @(([string]$_.environments) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        ruleIds = @(([string]$_.rule_ids) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        configFiles = @(([string]$_.config_files) -split ';' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        evidencePath = [string]$_.evidence_path
        configHash = ([string]$_.config_hash).ToLowerInvariant()
        rollback = [string]$_.rollback
        returnPlan = [string]$_.return_plan
        status = [string]$_.status
    }
})
$approvedExceptionsJson = if ($approvedExceptionsForLock.Count -eq 0) { '[]' } else {
    $approvedExceptionsForLock | ConvertTo-Json -Depth 10 -Compress
}
$approvedExceptionIds = @($approvedExceptionRows | ForEach-Object { [string]$_.exception_id })
$approvedExceptionIdsCsv = $approvedExceptionIds -join ','
$exceptionRegistrySha256 = Get-Sha256Hex -Bytes $Utf8NoBom.GetBytes($approvedExceptionCsv)
$resolvedCapabilityGroupsCsv = @($resolvedCapabilityGroups) -join ','
$requiredStandardsCsv = 'standard-error,header-context,transaction-id,security-boundary,audit,masking,observability,config,dependency-version,architecture-gate'
$generatedDomainPolicyProperties = @"
policyVersion=1.0
module=$projectName
profile=$CapabilityProfile
platformVersion=$PlatformVersion
capabilities=$resolvedCapabilityGroupsCsv
requiredStandards=$requiredStandardsCsv
approvedExceptionIds=$approvedExceptionIdsCsv
exceptionRegistrySha256=$exceptionRegistrySha256
failClosed=true
"@
$resolvedStarterLock = @"
{
  "lockVersion": "1.0",
  "profile": "$CapabilityProfile",
  "platformVersion": "$PlatformVersion",
  "resolvedStarters": $resolvedStartersJson,
  "resolvedStarterVersions": $resolvedStarterVersionsJson,
  "capabilityGroups": $resolvedCapabilityGroupsJson,
  "providerBindings": $resolvedProviderBindingsJson,
  "providerProjects": $resolvedProviderProjectsJson,
  "providerCoordinates": $resolvedProviderCoordinatesJson,
  "targetEnvironment": "$TargetEnvironment",
  "exceptionRegistrySha256": "$exceptionRegistrySha256",
  "approvedExceptions": $approvedExceptionsJson
}
"@
$domainManifest = @"
{
  "metadataVersion": "1.0",
  "domainType": "GENERATED_DOMAIN",
  "moduleCode": "$ModuleUpper",
  "systemCode": "$SystemCode",
  "domainName": "$module",
  "projectName": "$projectName",
  "moduleName": "$ModuleName",
  "displayName": "$ModuleName",
  "domainIdCode": "$DomainIdCode",
  "packageName": "$PackageName",
  "basePackage": "$BasePackage",
  "schemaName": "$SchemaName",
  "port": $Port,
  "tablePrefix": "$TablePrefix",
  "sampleTable": "$minimalTableName",
  "idempotencyLedgerTable": "$idempotencyLedgerTableName",
  "onlineEnabled": $onlineJson,
  "databaseEnabled": $databaseJson,
  "databaseVendor": "$DatabaseVendor",
  "databaseVendorProperty": "cpf.db.vendor",
  "databaseProfilePath": "deploy/database/database-profile.json",
  "databaseConnectionDefaults": {
    "vendor": "$DatabaseVendor",
    "host": "$DatabaseHost",
    "port": $DatabasePort,
    "databaseName": "$DatabaseName",
    "schemaName": "$DatabaseSchema",
    "adminUsername": "$DatabaseAdminUsername",
    "migrationUsername": "$DatabaseMigrationUsername",
    "runtimeUsername": "$DatabaseRuntimeUsername"
  },
  "supportedDatabaseVendors": $supportedDatabaseVendorsJson,
  "databaseTemplatePack": "$DatabaseTemplatePack",
  "databaseTemplateSelector": {
    "vendor": "$DatabaseVendor",
    "metadata": "manifest/domain-manifest.json",
    "sourceTreeMutation": false,
    "selectedVendorOnly": true
  },
  "databaseResourceAssembly": "prepareCpfVendorResources",
  "databaseLifecycle": {
    "sourceOfTruth": "$DatabaseTemplatePack",
    "bootstrapScript": "$DatabaseBootstrapScript",
    "defaultSampleTable": "$minimalTableName",
    "idempotencyLedgerTable": "$idempotencyLedgerTableName",
    "transactionIdentity": "transactionId"
  },
  "generatedResourceRoot": "build/generated-resources/cpf-vendor",
  "dependencyModel": "$DependencyModel",
  "platformVersion": "$PlatformVersion",

  "capabilityProfile": "$CapabilityProfile",
  "publicCapabilityGroups": ["data","messaging","integration","file","notification","security","platform-operations"],
  "resolvedCapabilityGroups": $resolvedCapabilityGroupsJson,
  "capabilityProfileVersion": "$([string]$resolvedCapabilityProfile.profileVersion)",
  "resolvedStarters": $resolvedStartersJson,
  "resolvedStarterVersions": $resolvedStarterVersionsJson,
  "providerBindings": $resolvedProviderBindingsJson,
  "standardInheritancePolicyVersion": "1.0",
  "approvedExceptionIds": $(if ($approvedExceptionIds.Count -eq 0) { '[]' } else { $approvedExceptionIds | ConvertTo-Json -Compress }),
  "targetEnvironment": "$TargetEnvironment",
  "exceptionRegistrySha256": "$exceptionRegistrySha256",
  "templateContractVersion": "$($centralTemplateContract.contractVersion)",
  "dataSourceJndiName": "$DataSourceJndiName",
  "batchEnabled": $batchJson,
  "centerCutEnabled": $centerCutJson,
  "externalEnabled": $externalJson,
  "messagingEnabled": $($MessagingEnabled.ToString().ToLowerInvariant()),
  "fileEnabled": $($FileEnabled.ToString().ToLowerInvariant()),
  "securityAuditEnabled": $($SecurityAuditEnabled.ToString().ToLowerInvariant()),
  "uiEnabled": $uiJson,
  "bzaMenuEnabled": $bzaMenuJson,
  "productionProfileEnabled": $($ProductionProfileEnabled.ToString().ToLowerInvariant()),
  "capabilities": {
    "online": $onlineJson,
    "database": $databaseJson,
    "batch": $batchJson,
    "centerCut": $centerCutJson,
    "external": $externalJson,
    "messaging": $($MessagingEnabled.ToString().ToLowerInvariant()),
    "file": $($FileEnabled.ToString().ToLowerInvariant()),
    "securityAudit": $($SecurityAuditEnabled.ToString().ToLowerInvariant()),
    "ui": $uiJson,
    "bzaMenu": $bzaMenuJson,
    "productionProfile": $($ProductionProfileEnabled.ToString().ToLowerInvariant())
  },
  "physicalTableContract": {
    "totalTables": $([int]$physicalTableContract.totalTables),
    "businessTableCount": $([int]$physicalTableContract.businessTableCount),
    "supportLedgerCount": $([int]$physicalTableContract.supportLedgerCount),
    "additionalTablesAllowed": false
  },
  "runtimeAgent": {
    "enabledByDefault": false,
    "activationProfile": "runtime-agent",
    "applicationConfig": "src/main/resources/application-runtime-agent.yml",
    "deploymentDescriptor": "deploy/runtime/runtime-agent.json",
    "capabilityDiscovery": "CpfRuntimeChangeApplier",
    "requiredEnvironment": [
      "CPF_RUNTIME_INSTANCE_ID",
      "CPF_RUNTIME_SERVICE_ID",
      "CPF_RUNTIME_ENDPOINT_CODE",
      "CPF_RUNTIME_BASE_URL",
      "CPF_RUNTIME_CONTROL_BASE_URL",
      "CPF_RUNTIME_CONTROL_AGENT_TOKEN"
    ],
    "failClosed": true
  },
  "serviceRegistration": {
    "candidate": true,
    "serviceId": "$ModuleUpper",
    "ownerModule": "$projectName",
    "environmentBinding": "runtime-profile",
    "directAllowedByDefault": true,
    "gatewayAllowedByDefault": false,
    "endpoints": [
      {
        "enabled": $onlineJson,
        "endpointCode": "$($ModuleUpper)_ONLINE",
        "protocol": "HTTP",
        "basePath": "/api/$module",
        "healthMethod": "HTTP",
        "healthPath": "/actuator/health/readiness",
        "contractVersion": "1.0"
      }
    ]
  },
  "parameterSchemas": {
    "contract": "com.cpf.core.api.parameter.CpfParameterSchema",
    "schemaVersion": "1.0",
    "runtimeOverrideDefault": false,
    "secretReferenceOnly": true
  },
  "gatewayBindingDefaults": {
    "externalExposure": "DENY",
    "approvalRequired": true,
    "connectionTestRequired": true,
    "healthValidationRequired": true
  },
  "serviceId": "$ModuleUpper",
  "onlineStandardId": "O${DomainIdCode}QY0001",
  "batchStandardId": "B${DomainIdCode}TS0001",
  "minimalTransactionContract": {
    "model": "$minimalDomainModel",
    "tableRole": "$minimalTableRole",
    "logicalTable": "$minimalLogicalTable",
    "requiredColumns": $minimalRequiredColumnsJson,
    "transactionIdWidth": $minimalTransactionIdWidth,
    "requiredKeys": $minimalRequiredKeysJson,
    "requiredIndexes": $minimalRequiredIndexesJson,
    "requiredChecks": $minimalRequiredChecksJson,
    "operations": $minimalRequiredOperationsJson
  },
  "idempotencyLedgerContract": {
    "model": "$idempotencyLedgerModel",
    "tableRole": "$idempotencyLedgerTableRole",
    "logicalTable": "$idempotencyLedgerLogicalTable",
    "requiredColumns": $idempotencyLedgerRequiredColumnsJson,
    "transactionIdWidth": $idempotencyLedgerTransactionIdWidth,
    "requiredKeys": $idempotencyLedgerRequiredKeysJson,
    "requiredIndexes": $idempotencyLedgerRequiredIndexesJson,
    "requiredChecks": $idempotencyLedgerRequiredChecksJson,
    "replayPolicy": "$idempotencyLedgerReplayPolicy",
    "logicalDeleteReplayRequired": $idempotencyLedgerLogicalDeleteReplayRequiredJson
  }
}
"@

$executionCatalogManifest = @"
[
  {
    "standardExecutionId": "O${DomainIdCode}QY0001",
    "executionType": "ONLINE",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$FeaturePackage.controller.${FeatureClassPrefix}Controller",
    "enabled": $onlineJson
  },
  {
    "standardExecutionId": "B${DomainIdCode}TS0001",
    "executionType": "BATCH",
    "ownerDomain": "$DomainIdCode",
    "sourceModule": "$ModuleUpper",
    "sourceClass": "$FeaturePackage.batch.${FeatureClassPrefix}BatchConfig",
    "enabled": $batchJson
  }
]
"@

$ownershipManifest = @"
{
  "moduleCode": "$ModuleUpper",
  "ownerDomain": "$DomainIdCode",
  "ownedPackages": ["$BasePackage"],
  "ownedSchemas": ["$SchemaName"],
  "ownedTablePrefixes": ["${TablePrefix}_"],
  "forbiddenDependencies": ["other-domain-repository", "other-domain-mapper"],
  "crossDomainContract": "CPF Service Call Engine 또는 CMN Facade Contract"
}
"@

$profileApplicationFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $profileWasId = "${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001"
    $profileDataSourceMode = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:jndi}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_MODE:url}"
    }
    $profileDataSourceUrl = "${Dollar}{$($ModuleUpper)_DATASOURCE_URL:}"
    $profileDataSourceUsername = if ($profileName -eq "prod") {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME}"
    } else {
        "${Dollar}{$($ModuleUpper)_DATASOURCE_USERNAME:cpf_${module}_app}"
    }
    $profileDataSourceYml = if ($DatabaseEnabled) {
@"
    datasource:
      mode: $profileDataSourceMode
      database-name: ${Dollar}{$($ModuleUpper)_DATABASE_NAME:$SchemaName}
      url: $profileDataSourceUrl
      username: $profileDataSourceUsername
      password: ${Dollar}{$($ModuleUpper)_DATASOURCE_PASSWORD}
      jndi-name: ${Dollar}{$($ModuleUpper)_DATASOURCE_JNDI_NAME:$DataSourceJndiName}
"@
    } else { "" }
    $profileApplicationFiles["src/main/resources/application-${module}-${profileName}.yml"] = @"
# ${ModuleName} ${profileName} profile 설정입니다.
spring:
  config:
    activate:
      on-profile: $profileName

server:
  port: ${Dollar}{$($ModuleUpper)_SERVER_PORT:$Port}

cpf:
  framework:
    module-id: ${Dollar}{$($ModuleUpper)_MODULE_ID:$ModuleUpper}
    instance-id: ${Dollar}{$($ModuleUpper)_INSTANCE_ID:${ModuleUpper}01}
    was-id: ${Dollar}{$($ModuleUpper)_WAS_ID:$profileWasId}
  ${module}:
$profileDataSourceYml
"@
}

$deployEnvFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $deployDataSourceMode = if ($profileName -eq "prod") { "jndi" } else { "url" }
    $deployDataSourceEnv = if ($DatabaseEnabled) {
@"
${ModuleUpper}_DATASOURCE_MODE=$deployDataSourceMode
${ModuleUpper}_DATABASE_VENDOR=$DatabaseVendor
${ModuleUpper}_DATABASE_NAME=$SchemaName
${ModuleUpper}_DATASOURCE_URL=
${ModuleUpper}_DATASOURCE_USERNAME=cpf_${module}_app
${ModuleUpper}_DATASOURCE_PASSWORD=__SET_BY_SECRET_PROVIDER__
${ModuleUpper}_DATASOURCE_JNDI_NAME=$DataSourceJndiName
"@
    } else { "" }
    $deployEnvFiles["deploy/env/${profileName}-${module}.env"] = @"
SPRING_PROFILES_ACTIVE=$profileName
${ModuleUpper}_MODULE_ID=$ModuleUpper
${ModuleUpper}_INSTANCE_ID=${ModuleUpper}-${profileName}-01
${ModuleUpper}_WAS_ID=${DomainIdCode}$($profileName.Substring(0, 1).ToUpperInvariant())001
${ModuleUpper}_SERVER_PORT=$Port
CPF_LOG_ROOT=C:/cpf/runtime/logs
# Runtime Agent 사용 시 SPRING_PROFILES_INCLUDE=runtime-agent와 아래 값을 Secret/배포 도구에서 설정합니다.
# CPF_RUNTIME_INSTANCE_ID=${ModuleUpper}-${profileName}-01
# CPF_RUNTIME_SERVICE_ID=$ModuleUpper
# CPF_RUNTIME_ENDPOINT_CODE=$ModuleUpper
# CPF_RUNTIME_BASE_URL=http://127.0.0.1:$Port
# CPF_RUNTIME_CONTROL_BASE_URL=
# CPF_RUNTIME_CONTROL_AGENT_TOKEN=__SET_BY_SECRET_PROVIDER__
$deployDataSourceEnv
"@
}

$deployInventoryFiles = [ordered]@{}
foreach ($profileName in @("local", "dev", "stg", "prod")) {
    $inventoryFileName = if ($profileName -eq "prod") { "prod-services.template.json" } else { "$profileName-services.json" }
    $deployInventoryFiles["deploy/inventory/${inventoryFileName}.${module}.candidate.json"] = @"
{
  "profile": "$profileName",
  "services": [
    {
      "module": "$ModuleUpper",
      "hostAlias": "${module}-${profileName}",
      "sshHostEnvKey": "${ModuleUpper}_SSH_HOST",
      "sshUserEnvKey": "${ModuleUpper}_SSH_USER",
      "deployBase": "/opt/cpf/$projectName",
      "healthUrl": "http://localhost:$Port/actuator/health",
      "serviceName": "$projectName",
      "portEnvKey": "${ModuleUpper}_SERVER_PORT",
      "profile": "$profileName",
      "runtimeMode": "embedded-bootjar",
      "approvalRequired": true,
      "rollbackEnabled": true,
      "runtimeAgent": {
        "activationProfile": "runtime-agent",
        "descriptor": "deploy/runtime/runtime-agent.json",
        "required": true
      }
    }
  ]
}
"@
}

$domainDatabaseProfile = @"
{
  "profileVersion": 1,
  "profileName": "$module-local-dev",
  "environment": "development",
  "domainName": "$module",
  "systemCode": "$SystemCode",
  "database": {
    "vendor": "$DatabaseVendor",
    "host": "$DatabaseHost",
    "port": $DatabasePort,
    "databaseName": "$DatabaseName",
    "schemaName": "$DatabaseSchema",
    "clientPath": "$DatabaseClientPath",
    "admin": {
      "username": "$DatabaseAdminUsername",
      "password": {
        "env": "${ModuleUpper}_DB_ADMIN_PASSWORD",
        "fallbackEnv": "CPF_DB_ROOT_PASSWORD"
      }
    },
    "migration": {
      "username": "$DatabaseMigrationUsername",
      "password": {
        "env": "${ModuleUpper}_DB_MIGRATION_PASSWORD",
        "fallbackEnv": "CPF_DB_MIGRATION_PASSWORD"
      }
    },
    "runtime": {
      "username": "$DatabaseRuntimeUsername",
      "password": {
        "env": "${ModuleUpper}_DB_RUNTIME_PASSWORD",
        "fallbackEnv": "CPF_DB_APP_PASSWORD"
      }
    }
  },
  "seed": {
    "product": true,
    "optionalSample": false,
    "test": false
  }
}
"@


$centerCutHandlerSource = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.spi.centercut.CenterCutHandler;
import org.springframework.stereotype.Component;

/**
 * BAT Center-Cut Runner가 local/remote 양쪽에서 호출할 수 있는 $ModuleName 업무 Handler 예제입니다.
 * 실제 업무에서는 businessKey/idempotency를 기준으로 동일 item 중복 실행을 안전하게 처리하십시오.
 */
@Component
public class ${ModuleName}CenterCutHandler implements CenterCutHandler {
    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        if (target == null) throw new IllegalArgumentException("target은 필수입니다.");
        return CpfCenterCutResult.success(target, "$ModuleName center-cut handled", target.payload());
    }
}
"@

$centerCutControllerSource = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 분리 WAS BAT가 Gateway 재경유 없이 호출하는 내부 Center-Cut endpoint입니다. */
@RestController
@RequestMapping("/internal/$module/center-cut")
public class ${ModuleName}CenterCutController {
    private final ${ModuleName}CenterCutHandler handler;
    public ${ModuleName}CenterCutController(${ModuleName}CenterCutHandler handler) { this.handler = handler; }

    @PostMapping("/{jobId}/items")
    @CpfOnlineTransaction(
        id = "S${DomainIdCode}CC0001",
        name = "${ModuleName}CenterCutItem",
        ownerDomain = "$DomainIdCode",
        visibility = "INTERNAL",
        gatewayAllowed = false,
        directAllowed = true)
    public ResponseEntity<CpfCenterCutResult> execute(@PathVariable String jobId, @RequestBody CpfCenterCutTarget target) {
        if (!jobId.equals(target.centerCutJobId())) throw new IllegalArgumentException("jobId와 target.centerCutJobId가 일치해야 합니다.");
        return ResponseEntity.ok(handler.handle(target));
    }
}
"@

$centerCutHandlerTest = @"
package $FeaturePackage.centercut;

import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutTarget;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class ${ModuleName}CenterCutHandlerTest {
    @Test
    void handlesTargetThroughPublicCenterCutContract() {
        CpfCenterCutTarget target = new CpfCenterCutTarget(
            "T-1", "$ModuleUpper-CC-1", "BUSINESS-1", LocalDate.of(2026,7,25), "{}",
            "TX-1", "SEG-P", "SEG-C", 0, CpfCenterCutStatus.RUNNING);
        assertThat(new ${ModuleName}CenterCutHandler().handle(target).status()).isEqualTo(CpfCenterCutStatus.SUCCESS);
    }
}
"@

$generatedDomainGradleProperties = @"
# Generator-managed CPF platform and approved-exception attestation.
cpfPlatformVersion=$PlatformVersion
$(if ($approvedExceptionRows.Count -gt 0) { "cpfTargetEnvironment=$TargetEnvironment" } else { "" })
"@

$files = [ordered]@{
    "build.gradle" = $buildGradle
    "README.md" = $readme
    "manifest/domain-manifest.json" = $domainManifest
    "manifest/resolved-starter-lock.json" = $resolvedStarterLock
    "config/cpf-approved-exceptions.csv" = $approvedExceptionCsv
    "src/main/resources/META-INF/cpf/cpf-approved-exceptions.csv" = $approvedExceptionCsv
    "src/main/resources/META-INF/cpf/generated-domain-policy.properties" = $generatedDomainPolicyProperties
    "deploy/database/database-profile.json" = $domainDatabaseProfile
    "manifest/ownership.json" = $ownershipManifest
    "manifest/standard-execution-catalog.json" = $executionCatalogManifest
    "src/main/resources/application.yml" = $applicationYml
    "src/main/resources/application-runtime-agent.yml" = $runtimeAgentApplicationYml
    "deploy/runtime/runtime-agent.json" = $runtimeAgentDescriptor
    "src/main/resources/application-${module}.yml" = $applicationModuleYml
    "src/main/java/$packagePath/${ModuleClassName}Application.java" = $applicationJava
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseController.java" = $moduleBaseController
    "src/main/java/$packagePath/common/base/${ModuleClassName}BaseService.java" = $moduleBaseService
    "src/main/java/$packagePath/common/contract/${ModuleClassName}ApplicationFacade.java" = $moduleFacadeContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}RepositoryPort.java" = $moduleRepositoryContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Request.java" = $moduleRequestContract
    "src/main/java/$packagePath/common/contract/${ModuleClassName}Response.java" = $moduleResponseContract
    "src/main/java/$featurePackagePath/facade/${FeatureClassPrefix}Facade.java" = $facade
    "src/main/java/$featurePackagePath/port/${FeatureClassPrefix}QueryPort.java" = $queryPortSource
    "src/main/java/$featurePackagePath/port/${FeatureClassPrefix}CommandPort.java" = $commandPortSource
    "src/main/java/$featurePackagePath/service/${FeatureClassPrefix}Service.java" = $service
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SearchRequest.java" = $dto
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SampleCommand.java" = $sampleCommand
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SampleItem.java" = $sampleItem
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}SearchResult.java" = $searchResult
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}DeleteCommand.java" = $deleteCommand
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}DeleteResult.java" = $deleteResult
    "src/main/java/$featurePackagePath/dto/${FeatureClassPrefix}IdempotencyEntry.java" = $idempotencyEntry
    "src/main/java/$featurePackagePath/validation/${FeatureClassPrefix}SearchValidator.java" = $validator
    "src/test/java/$featurePackagePath/service/${FeatureClassPrefix}ServiceTest.java" = $serviceTest
        "smoke/smoke-${module}.ps1" = $smokeScript
}
$files["gradle.properties"] = $generatedDomainGradleProperties
foreach ($evidenceEntry in $approvedExceptionEvidenceFiles.GetEnumerator()) {
    $files[$evidenceEntry.Key] = $evidenceEntry.Value
}
foreach ($configEntry in $approvedExceptionConfigFiles.GetEnumerator()) {
    if ($files.Contains($configEntry.Key)) {
        throw "승인 예외 Config가 Generator 표준 파일과 충돌합니다: $($configEntry.Key)"
    }
}

if ($DatabaseEnabled) {
    $files["src/main/java/$featurePackagePath/adapter/local/Local${FeatureClassPrefix}Adapter.java"] = $localAdapter
    $files["src/main/java/$featurePackagePath/repository/${FeatureClassPrefix}Repository.java"] = $repository
}
if ($ExternalEnabled) {
    $files["src/main/java/$featurePackagePath/adapter/remote/Remote${FeatureClassPrefix}Adapter.java"] = $remoteProxy
}
if (-not $DatabaseEnabled -and -not $ExternalEnabled) {
    $files["src/main/java/$featurePackagePath/adapter/memory/InMemory${FeatureClassPrefix}Adapter.java"] = $inMemoryAdapter
}
if ($UiEnabled) {
    $files["ui/src/features/sample-items/${FeatureClassPrefix}Page.vue"] = $uiComponent
    $files["ui/src/features/sample-items/${FeatureClassPrefix}Api.ts"] = $uiApi
}

if ($MessagingEnabled) {
    $files["src/main/java/$featurePackagePath/messaging/${ModuleName}EventPublisher.java"] = $messagingPublisher
    $files["src/test/java/$featurePackagePath/messaging/${ModuleName}EventPublisherTest.java"] = $messagingTest
    if ($OnlineEnabled) {
        $files["src/main/java/$featurePackagePath/messaging/${ModuleName}MessagingController.java"] = $messagingController
    }
}
if ($FileEnabled) {
    $files["src/main/java/$featurePackagePath/file/${ModuleName}FileTransferService.java"] = $fileTransferService
    $files["src/test/java/$featurePackagePath/file/${ModuleName}FileTransferServiceTest.java"] = $fileTransferTest
}
if ($CenterCutEnabled) {
    $files["src/main/java/$featurePackagePath/centercut/${ModuleName}CenterCutHandler.java"] = $centerCutHandlerSource
    $files["src/main/java/$featurePackagePath/centercut/${ModuleName}CenterCutController.java"] = $centerCutControllerSource
    $files["src/test/java/$featurePackagePath/centercut/${ModuleName}CenterCutHandlerTest.java"] = $centerCutHandlerTest
}

if ($SecurityAuditEnabled) {
    $files["src/main/java/$featurePackagePath/security/${ModuleName}OperationGuard.java"] = $securityAuditGuard
    $files["src/test/java/$featurePackagePath/security/${ModuleName}OperationGuardTest.java"] = $securityAuditTest
    if ($OnlineEnabled) {
        $files["src/main/java/$featurePackagePath/security/${ModuleName}OperationController.java"] = $securityAuditController
    }
}

if ($OnlineEnabled) {
    $files["src/main/java/$featurePackagePath/controller/${FeatureClassPrefix}Controller.java"] = $controller
}
if ($BatchEnabled) {
    $files["src/main/java/$featurePackagePath/batch/${FeatureClassPrefix}BatchConfig.java"] = $batchConfig
    $files["src/main/java/$packagePath/config/${ModuleName}BatchRepositoryConfig.java"] = $batchRepositoryConfig
}

if ($ProductionProfileEnabled) {
    foreach ($entry in $profileApplicationFiles.GetEnumerator()) {
        $files[$entry.Key] = $entry.Value
    }
}

foreach ($entry in $files.GetEnumerator()) {
    $path = Join-Path $OutputDir $entry.Key
    if (Test-Path -LiteralPath $path) {
        throw "Generated file already exists. path=$path"
    }
    Write-Utf8 -Path $path -Content $entry.Value
    $plan.generatedFiles += $path.Substring($Root.Length).TrimStart('\', '/')
}
foreach ($configEntry in $approvedExceptionConfigFiles.GetEnumerator()) {
    $target = Join-Path $OutputDir $configEntry.Key
    if (Test-Path -LiteralPath $target) {
        throw "Generated approved exception Config already exists. path=$target"
    }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
    Copy-Item -LiteralPath $configEntry.Value -Destination $target
    $plan.generatedFiles += $target.Substring($Root.Length).TrimStart('\', '/')
}

if ($GeneratePatch) {
    Write-Warning "GeneratePatch는 1.0.0부터 중간 후보 파일을 생성하지 않습니다. SQL과 배포 설정은 승인된 별도 절차로 반영하세요."
}

if ($Apply) {
    # Apply 모드는 생성 모듈을 저장소에 유지하고 settings 연결까지 원자적으로 완료합니다.
    # 정본 SQL 합본은 version 충돌 검토가 필요하므로 생성된 patch candidate를 별도 동기화 단계에서 반영합니다.
    $settingsText = [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
    if ($settingsText -notmatch "(?m)^include '$([regex]::Escape($projectName))'$" -and
            $settingsText -notmatch "(?m)^include .*'$([regex]::Escape($projectName))'") {
        $settingsText = $settingsText.TrimEnd() + "`r`n`r`ninclude '$projectName'`r`nproject(':$projectName').projectDir = file('$projectName')`r`n"
        [System.IO.File]::WriteAllText($settingsPath, $settingsText, $Utf8NoBom)
        $plan.patchFiles += 'settings.gradle'
    }
}

# 제거 도구는 생성 당시 checksum과 현재 파일을 비교해 사용자 변경을 보호합니다.
$ownedFiles = @()
foreach ($relativePath in @($plan.generatedFiles)) {
    $absolutePath = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        continue
    }
    $moduleRelativePath = $absolutePath.Substring($OutputDir.Length).TrimStart('\', '/')
    $ownedFiles += [ordered]@{
        path = $moduleRelativePath.Replace('\', '/')
        sha256 = (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
$generatorOwnership = [ordered]@{
    generatorVersion = "3.1"
    templateContractVersion = [string]$centralTemplateContract.contractVersion
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    moduleCode = $ModuleUpper
    systemCode = $SystemCode
    domainName = $module
    projectName = $projectName
    moduleName = $ModuleName
    packageName = $PackageName
    schemaName = $SchemaName
    tablePrefix = $TablePrefix
    moduleDirectory = $projectName
    outputDirectory = $projectName
    dependencyModel = $DependencyModel
    platformVersion = $PlatformVersion
    capabilities = [ordered]@{
        online = $OnlineEnabled
        database = $DatabaseEnabled
        databaseVendor = $DatabaseVendor
        batch = $BatchEnabled
        centerCut = $CenterCutEnabled
        external = $ExternalEnabled
        messaging = $MessagingEnabled
        file = $FileEnabled
        securityAudit = $SecurityAuditEnabled
        ui = $UiEnabled
        bzaMenu = $BzaMenuEnabled
        productionProfile = $ProductionProfileEnabled
        capabilityProfile = $CapabilityProfile
        capabilityGroups = $resolvedCapabilityGroups
        providerBindings = $resolvedProviderBindings
        resolvedStarters = $resolvedStarters
        approvedExceptionIds = $approvedExceptionIds
        exceptionRegistrySha256 = $exceptionRegistrySha256
        standardInheritancePolicyVersion = "1.0"
    }
    createdFiles = $ownedFiles
    modifiedGlobalFiles = @(
        [ordered]@{
            path = "settings.gradle"
            managedLines = @("include '$projectName'", "project(':$projectName').projectDir = file('$projectName')")
        }
    )
    databaseRemovalPolicy = "운영 DB 객체는 자동 삭제하지 않으며 별도 승인 migration으로 처리합니다."
}
$ownershipPath = Join-Path $OutputDir "manifest/generator-ownership.json"
Write-Utf8 -Path $ownershipPath -Content ($generatorOwnership | ConvertTo-Json -Depth 20)
$plan.generatedFiles += $ownershipPath.Substring($Root.Length).TrimStart('\', '/')

if ($ProvisionDatabase) {
    $dbInitializer = Join-Path $Root "cpf-tools/scripts/initialize-domain-database.ps1"
    if (-not (Test-Path -LiteralPath $dbInitializer -PathType Leaf)) {
        throw "공식 Domain DB 초기화 Script가 없습니다: $dbInitializer"
    }

    $generatedProfilePath = Join-Path $Root "$projectName/deploy/database/database-profile.json"
    if (-not (Test-Path -LiteralPath $generatedProfilePath -PathType Leaf)) {
        throw "생성된 Domain DB Profile이 없습니다: $generatedProfilePath"
    }

    # DB 접속/계정/Vendor는 생성된 Git-tracked Domain Profile을 단일 정본으로 사용합니다.
    # Secret은 Profile의 env/fallback Secret 정책으로 initialize-domain-database.ps1가 해석합니다.
    & pwsh -NoProfile -File $dbInitializer `
        -DomainName $module `
        -SystemCode $SystemCode `
        -Root $Root `
        -ProfilePath $generatedProfilePath `
        -Operation bootstrap `
        -Apply
    if ($LASTEXITCODE -ne 0) {
        throw "Domain Source 생성은 완료됐지만 DB bootstrap이 실패했습니다. project=$projectName"
    }

    $plan.databaseProvisioned = $true
    $plan.databaseProvisioningScript = 'cpf-tools/scripts/initialize-domain-database.ps1'
    $plan.databaseProfile = "$projectName/deploy/database/database-profile.json"
}

# Generator 실행 원장은 Apply/DryRun 여부와 무관하게 제품 Module 밖의
# 중앙 build report에만 둡니다. 생성된 Domain은 언제나 깨끗한 제품 Source입니다.
$resultPath = Join-Path $Root "build/reports/create-domain/$module/create-domain-result.json"
Write-Utf8 -Path $resultPath -Content ($plan | ConvertTo-Json -Depth 20)
$plan | ConvertTo-Json -Depth 20
