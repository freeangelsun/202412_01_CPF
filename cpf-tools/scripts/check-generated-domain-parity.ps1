param(
    [Parameter(Mandatory = $true)] [string] $ReferenceDomain,
    [Parameter(Mandatory = $true)] [string] $CandidateDomain,
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$templateContractPath = Join-Path $Root "cpf-tools/generator/contracts/central-domain-template-contract.json"
if (-not (Test-Path -LiteralPath $templateContractPath -PathType Leaf)) {
    throw "Generated Domain 중앙 Template 계약이 없습니다: $templateContractPath"
}
$templateContract = Get-Content -LiteralPath $templateContractPath -Raw -Encoding UTF8 | ConvertFrom-Json
$templateContractVersion = [string]$templateContract.contractVersion
if ([string]::IsNullOrWhiteSpace($templateContractVersion)) {
    throw "Generated Domain 중앙 Template 계약 version이 비어 있습니다: $templateContractPath"
}

function Normalize-DomainName([string] $Value) {
    $value = $Value.Trim().ToLowerInvariant()
    if ($value -notmatch '^[a-z][a-z0-9]{1,29}$') { throw "잘못된 DomainName: $Value" }
    return $value
}
function Read-Manifest([string] $Domain) {
    $path = Join-Path $Root "cpf-$Domain/manifest/domain-manifest.json"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Generated Domain manifest가 없습니다: $path" }
    $manifest = Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]$manifest.metadataVersion -ne "1.0" -or
            [string]$manifest.domainType -ne "GENERATED_DOMAIN" -or
            [string]$manifest.domainName -ne $Domain -or
            [string]$manifest.projectName -ne "cpf-$Domain" -or
            [string]$manifest.templateContractVersion -ne $templateContractVersion) {
        throw "Generated Domain manifest가 canonical metadata 계약과 다릅니다: $path"
    }
    $ownershipPath = Join-Path $Root "cpf-$Domain/manifest/generator-ownership.json"
    if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
        throw "Generated Domain generator ownership이 없습니다: $ownershipPath"
    }
    $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]$ownership.domainName -ne $Domain -or
            [string]$ownership.projectName -ne "cpf-$Domain" -or
            [string]$ownership.systemCode -ne [string]$manifest.systemCode -or
            [string]$ownership.templateContractVersion -ne [string]$manifest.templateContractVersion) {
        throw "Generated Domain manifest/ownership identity가 일치하지 않습니다: cpf-$Domain"
    }
    return [pscustomobject]@{
        manifest = $manifest
        ownership = $ownership
    }
}
function Get-Normalizers([object] $Manifest) {
    $pairs = [System.Collections.Generic.List[object]]::new()
    $definitions = @(
        [ordered]@{ key = "projectName"; value = [string]$Manifest.projectName; kind = "exact" }
        [ordered]@{ key = "packageName"; value = [string]$Manifest.packageName; kind = "exact" }
        [ordered]@{ key = "basePackage"; value = [string]$Manifest.basePackage; kind = "exact" }
        [ordered]@{ key = "dataSourceJndiName"; value = [string]$Manifest.dataSourceJndiName; kind = "exact" }
        [ordered]@{ key = "onlineStandardId"; value = [string]$Manifest.onlineStandardId; kind = "exact" }
        [ordered]@{ key = "batchStandardId"; value = [string]$Manifest.batchStandardId; kind = "exact" }
        [ordered]@{ key = "schemaName"; value = [string]$Manifest.schemaName; kind = "exact" }
        [ordered]@{ key = "moduleName"; value = [string]$Manifest.moduleName; kind = "class" }
        [ordered]@{ key = "domainName"; value = [string]$Manifest.domainName; kind = "lower-token" }
        [ordered]@{ key = "tablePrefix"; value = [string]$Manifest.tablePrefix; kind = "lower-token" }
        [ordered]@{ key = "systemCode"; value = [string]$Manifest.systemCode; kind = "upper-token" }
        [ordered]@{ key = "moduleCode"; value = [string]$Manifest.moduleCode; kind = "upper-token" }
        [ordered]@{ key = "domainIdCode"; value = [string]$Manifest.domainIdCode; kind = "upper-token" }
        [ordered]@{ key = "port"; value = [string]$Manifest.port; kind = "number" }
    )
    foreach ($definition in $definitions) {
        $value = [string]$definition.value
        if ([string]::IsNullOrWhiteSpace($value)) { continue }
        $escaped = [regex]::Escape($value)
        $pattern = switch ([string]$definition.kind) {
            "class" { $escaped + "(?=[A-Z0-9_]|[^A-Za-z0-9]|$)" }
            "lower-token" { "(?<![a-z0-9])" + $escaped + "(?![a-z0-9])" }
            "upper-token" {
                "(?:" +
                    "(?<![A-Z0-9])" + $escaped + "(?![A-Z0-9])" +
                    "|(?<=[OB])" + $escaped + "(?=[A-Z]{2}[0-9]{4})" +
                    "|(?<=[0-9]{17})" + $escaped + "(?=[A-Za-z0-9]{14})" +
                    "|(?<=Cpf)" + $escaped + "(?=(?:Mig|App)#)" +
                ")"
            }
            "number" { "(?<![0-9])" + $escaped + "(?![0-9])" }
            default { $escaped }
        }
        $tokenName = ([string]$definition.key).ToUpperInvariant()
        $pairs.Add([ordered]@{
            value = $value
            pattern = $pattern
            token = "<$tokenName>"
        }) | Out-Null
    }
    return @(
        $pairs |
            Sort-Object `
                @{ Expression = { $_.value.Length }; Descending = $true },
                @{ Expression = { $_.token }; Descending = $false }
    )
}
function Normalize-Text([string] $Text, [object[]] $Pairs) {
    $value = $Text.Replace("`r`n", "`n")
    foreach ($pair in $Pairs) {
        $value = [regex]::Replace(
            $value,
            [string]$pair.pattern,
            [string]$pair.token,
            [System.Text.RegularExpressions.RegexOptions]::None)
    }
    return $value.TrimEnd()
}
function Normalize-Path([string] $Path, [object[]] $Pairs) {
    return Normalize-Text ($Path.Replace('\','/')) $Pairs
}
function Get-Contract([string] $Domain, [object] $Manifest) {
    $project = Join-Path $Root "cpf-$Domain"
    $pairs = Get-Normalizers $Manifest
    $ignore = @(
        'manifest/domain-manifest.json',
        'manifest/generator-ownership.json',
        'build/'
    )
    $map = [ordered]@{}
    Get-ChildItem -LiteralPath $project -Recurse -File | ForEach-Object {
        $relative = $_.FullName.Substring($project.Length + 1).Replace('\','/')
        if ($ignore | Where-Object { $relative.StartsWith($_, [StringComparison]::OrdinalIgnoreCase) }) { return }
        $normalizedPath = Normalize-Path $relative $pairs
        $text = [System.IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8)
        $normalizedText = Normalize-Text $text $pairs
        $bytes = [Text.Encoding]::UTF8.GetBytes($normalizedText)
        $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
        if ($map.Contains($normalizedPath)) {
            throw "정규화된 Generated Domain path가 충돌합니다: domain=$Domain path=$normalizedPath"
        }
        $map[$normalizedPath] = $hash
    }
    return $map
}

$reference = Normalize-DomainName $ReferenceDomain
$candidate = Normalize-DomainName $CandidateDomain
if ($reference -eq $candidate) { throw "서로 다른 두 Domain을 지정해야 합니다." }
$refMetadata = Read-Manifest $reference
$canMetadata = Read-Manifest $candidate
$refManifest = $refMetadata.manifest
$canManifest = $canMetadata.manifest

$capabilityKeys = @(
    'onlineEnabled',
    'databaseEnabled',
    'databaseVendor',
    'batchEnabled',
    'centerCutEnabled',
    'externalEnabled',
    'messagingEnabled',
    'fileEnabled',
    'securityAuditEnabled',
    'uiEnabled',
    'bzaMenuEnabled',
    'productionProfileEnabled',
    'dependencyModel',
    'platformVersion',
    'templateContractVersion'
)
$capabilityDiff = @()
foreach ($key in $capabilityKeys) {
    if ([string]$refManifest.$key -ne [string]$canManifest.$key) { $capabilityDiff += "$key=$($refManifest.$key)/$($canManifest.$key)" }
}
$structuredCapabilityKeys = @(
    'online',
    'database',
    'batch',
    'centerCut',
    'external',
    'messaging',
    'file',
    'securityAudit',
    'ui',
    'bzaMenu',
    'productionProfile'
)
foreach ($key in $structuredCapabilityKeys) {
    if ([string]$refManifest.capabilities.$key -ne
            [string]$canManifest.capabilities.$key) {
        $capabilityDiff +=
                "capabilities.$key=$($refManifest.capabilities.$key)/$($canManifest.capabilities.$key)"
    }
}

$refContract = Get-Contract $reference $refManifest
$canContract = Get-Contract $candidate $canManifest
$allPaths = @($refContract.Keys + $canContract.Keys | Sort-Object -Unique)
$diffs = [System.Collections.Generic.List[object]]::new()
foreach ($path in $allPaths) {
    $left = $refContract[$path]
    $right = $canContract[$path]
    if ($left -ne $right) {
        $diffs.Add([ordered]@{ path=$path; referenceSha256=$left; candidateSha256=$right }) | Out-Null
    }
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) { $ResultDir = Join-Path $Root "build/reports/generated-domain-parity/$reference-vs-$candidate" }
elseif (-not [IO.Path]::IsPathRooted($ResultDir)) { $ResultDir = Join-Path $Root $ResultDir }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = if ($capabilityDiff.Count -eq 0 -and $diffs.Count -eq 0) { '완료' } else { '실패' }
    referenceDomain = $reference
    candidateDomain = $candidate
    capabilityDifferences = $capabilityDiff
    referenceGeneratorVersion = [string]$refMetadata.ownership.generatorVersion
    candidateGeneratorVersion = [string]$canMetadata.ownership.generatorVersion
    normalizedReferenceFileCount = $refContract.Count
    normalizedCandidateFileCount = $canContract.Count
    differences = @($diffs)
}
$resultPath = Join-Path $ResultDir 'generated-domain-parity.sanitized.json'
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20) + [Environment]::NewLine, $Utf8NoBom)
if ([string]$refMetadata.ownership.generatorVersion -ne
        [string]$canMetadata.ownership.generatorVersion) {
    $result.status = '실패'
    $result.capabilityDifferences +=
            "generatorVersion=$($refMetadata.ownership.generatorVersion)/$($canMetadata.ownership.generatorVersion)"
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20) + [Environment]::NewLine, $Utf8NoBom)
}
if ($result.status -ne '완료') {
    Write-Host "Generated Domain parity FAILED. result=$resultPath"
    exit 1
}
Write-Host "Generated Domain parity PASS. reference=$reference candidate=$candidate result=$resultPath"
