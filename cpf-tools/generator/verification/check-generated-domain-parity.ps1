[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string] $ReferenceDomain,
    [Parameter(Mandatory = $true)][string] $CandidateDomain,
    [string] $ReferenceDefinitionPath = '',
    [string] $CandidateDefinitionPath = '',
    [string] $ReferenceOutputDir = '',
    [string] $CandidateOutputDir = '',
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = ''
)

# Parity identity comes from the two canonical cpf-domain.yaml definitions. Generated
# Projects are deliberately metadata-free and are verified by the same canonical Engine first.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')

function Normalize-DomainName([string] $Value) {
    $normalized = $Value.Trim().ToLowerInvariant()
    if ($normalized -notmatch '^[a-z][a-z0-9-]{1,49}$') { throw "잘못된 DomainName: $Value" }
    return $normalized
}
function Add-Normalizer([Collections.Generic.List[object]] $List, [string] $Value, [string] $Token) {
    if ([string]::IsNullOrWhiteSpace($Value)) { return }
    $List.Add([ordered]@{ value = $Value; token = "<$Token>" }) | Out-Null
}
function Get-Normalizers([object] $Metadata) {
    $pairs = [Collections.Generic.List[object]]::new()
    Add-Normalizer $pairs ([string]$Metadata.projectName) 'PROJECT_NAME'
    Add-Normalizer $pairs ([string]$Metadata.packageName) 'PACKAGE_NAME'
    Add-Normalizer $pairs (([string]$Metadata.packageName).Replace('.', '/')) 'PACKAGE_PATH'
    Add-Normalizer $pairs ([string]$Metadata.className) 'CLASS_NAME'
    Add-Normalizer $pairs ([string]$Metadata.domainName) 'DOMAIN_NAME'
    Add-Normalizer $pairs ([string]$Metadata.systemCode) 'SYSTEM_CODE'
    Add-Normalizer $pairs ([string]$Metadata.tablePrefix) 'TABLE_PREFIX'
    Add-Normalizer $pairs ([string]$Metadata.localOnlinePort) 'LOCAL_ONLINE_PORT'
    $index = 0
    foreach ($dependency in @($Metadata.domainDependencies | Sort-Object domainName, systemCode)) {
        $dependencyName = [string]$dependency.domainName
        $dependencyClass = -join ($dependencyName -split '[-_]' | ForEach-Object {
            if ($_.Length -eq 0) { return }
            $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1)
        })
        Add-Normalizer $pairs $dependencyName "DEPENDENCY_${index}_NAME"
        Add-Normalizer $pairs $dependencyClass "DEPENDENCY_${index}_CLASS"
        Add-Normalizer $pairs ([string]$dependency.systemCode) "DEPENDENCY_${index}_CODE"
        $index++
    }
    $index = 0
    foreach ($client in @($Metadata.externalClients | Sort-Object name, id)) {
        Add-Normalizer $pairs ([string]$client.name) "EXTERNAL_${index}_NAME"
        Add-Normalizer $pairs ([string]$client.id) "EXTERNAL_${index}_ID"
        $index++
    }
    return @($pairs | Sort-Object @{ Expression = { $_.value.Length }; Descending = $true })
}
function Normalize-Text([string] $Text, [object[]] $Pairs) {
    $value = $Text.Replace("`r`n", "`n")
    foreach ($pair in $Pairs) {
        $value = $value.Replace([string]$pair.value, [string]$pair.token)
        $lower = ([string]$pair.value).ToLowerInvariant()
        if ($lower -ne [string]$pair.value) { $value = $value.Replace($lower, ([string]$pair.token).ToLowerInvariant()) }
    }
    return $value.TrimEnd()
}
function Get-ProjectContract([object] $Metadata, [string] $OutputDir) {
    $project = if ([string]::IsNullOrWhiteSpace($OutputDir)) {
        Join-Path $Root ([string]$Metadata.projectPath)
    } elseif ([IO.Path]::IsPathRooted($OutputDir)) {
        [IO.Path]::GetFullPath($OutputDir)
    } else {
        [IO.Path]::GetFullPath((Join-Path $Root $OutputDir))
    }
    if (-not (Test-Path -LiteralPath $project -PathType Container)) {
        throw "Generated Domain project가 없습니다: $project"
    }
    if (@($Metadata.forbiddenPermanentMetadata).Count -gt 0) {
        throw "Generated Project 영구 metadata 금지 위반: project=$($Metadata.projectName) paths=$(@($Metadata.forbiddenPermanentMetadata) -join ',')"
    }
    [void](Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'verify', 'domain', '--file', [string]$Metadata.definitionPath, '--output', $project
    ))
    $pairs = Get-Normalizers $Metadata
    $map = [ordered]@{}
    Get-ChildItem -LiteralPath $project -Recurse -File | Where-Object {
        $relative = $_.FullName.Substring($project.Length + 1).Replace('\', '/')
        $relative -notmatch '(^|/)build/' -and $relative -notmatch '(^|/)\.gradle/'
    } | ForEach-Object {
        $relative = $_.FullName.Substring($project.Length + 1).Replace('\', '/')
        $normalizedPath = Normalize-Text $relative $pairs
        $normalizedText = Normalize-Text ([IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8)) $pairs
        $bytes = [Text.Encoding]::UTF8.GetBytes($normalizedText)
        $hash = [Convert]::ToHexString([Security.Cryptography.SHA256]::HashData($bytes)).ToLowerInvariant()
        if ($map.Contains($normalizedPath)) {
            throw "정규화된 Generated Domain path가 충돌합니다: project=$($Metadata.projectName) path=$normalizedPath"
        }
        $map[$normalizedPath] = $hash
    }
    return $map
}

$reference = Normalize-DomainName $ReferenceDomain
$candidate = Normalize-DomainName $CandidateDomain
if ($reference -eq $candidate) { throw '서로 다른 두 Domain을 지정해야 합니다.' }
$refMetadata = Get-CpfGeneratedDomainDefinition `
    -Root $Root -DomainName $reference -DefinitionPath $ReferenceDefinitionPath -IncludeMissing
$canMetadata = Get-CpfGeneratedDomainDefinition `
    -Root $Root -DomainName $candidate -DefinitionPath $CandidateDefinitionPath -IncludeMissing

$capabilityKeys = @(
    'onlineEnabled', 'databaseRole', 'databaseEnabled', 'persistence',
    'sampleTransaction', 'httpClient', 'resilience', 'cache', 'messaging',
    'objectStorage', 'securityProfile', 'dependencyModel', 'generatorVersion'
)
$capabilityDiff = @()
foreach ($key in $capabilityKeys) {
    if ([string]$refMetadata.$key -ne [string]$canMetadata.$key) {
        $capabilityDiff += "$key=$($refMetadata.$key)/$($canMetadata.$key)"
    }
}
if (@($refMetadata.domainDependencies).Count -ne @($canMetadata.domainDependencies).Count) {
    $capabilityDiff += "domainDependencyCount=$(@($refMetadata.domainDependencies).Count)/$(@($canMetadata.domainDependencies).Count)"
}
if (@($refMetadata.externalClients).Count -ne @($canMetadata.externalClients).Count) {
    $capabilityDiff += "externalClientCount=$(@($refMetadata.externalClients).Count)/$(@($canMetadata.externalClients).Count)"
}

$refContract = Get-ProjectContract $refMetadata $ReferenceOutputDir
$canContract = Get-ProjectContract $canMetadata $CandidateOutputDir
$allPaths = @($refContract.Keys + $canContract.Keys | Sort-Object -Unique)
$diffs = [Collections.Generic.List[object]]::new()
foreach ($path in $allPaths) {
    if ($refContract[$path] -ne $canContract[$path]) {
        $diffs.Add([ordered]@{
            path = $path
            referenceSha256 = $refContract[$path]
            candidateSha256 = $canContract[$path]
        }) | Out-Null
    }
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/generated-domain-parity/$reference-vs-$candidate"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = if ($capabilityDiff.Count -eq 0 -and $diffs.Count -eq 0) { '완료' } else { '실패' }
    referenceDomain = $reference
    candidateDomain = $candidate
    metadataSource = 'cpf-domain.yaml'
    generatedProjectMetadata = 'NONE'
    capabilityDifferences = $capabilityDiff
    normalizedReferenceFileCount = $refContract.Count
    normalizedCandidateFileCount = $canContract.Count
    differences = @($diffs)
}
$resultPath = Join-Path $ResultDir 'generated-domain-parity.sanitized.json'
[IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine,
    $Utf8NoBom)
if ($result.status -ne '완료') {
    throw "Generated Domain parity FAILED. result=$resultPath"
}
Write-Host "Generated Domain parity PASS. reference=$reference candidate=$candidate result=$resultPath"
