param(
    [Parameter(Mandatory = $true)] [string] $ReferenceDomain,
    [Parameter(Mandatory = $true)] [string] $CandidateDomain,
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

function Normalize-DomainName([string] $Value) {
    $value = $Value.Trim().ToLowerInvariant()
    if ($value -notmatch '^[a-z][a-z0-9]{1,29}$') { throw "잘못된 DomainName: $Value" }
    return $value
}
function Read-Manifest([string] $Domain) {
    $path = Join-Path $Root "cpf-$Domain/manifest/domain-manifest.json"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Generated Domain manifest가 없습니다: $path" }
    return Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
}
function Get-Normalizers([object] $Manifest) {
    $pairs = [System.Collections.Generic.List[object]]::new()
    $values = [ordered]@{
        projectName = [string]$Manifest.projectName
        packageName = [string]$Manifest.packageName
        basePackage = [string]$Manifest.basePackage
        schemaName = [string]$Manifest.schemaName
        tablePrefix = [string]$Manifest.tablePrefix
        domainName = [string]$Manifest.domainName
        systemCode = [string]$Manifest.systemCode
        moduleCode = [string]$Manifest.moduleCode
        moduleName = [string]$Manifest.moduleName
        domainIdCode = [string]$Manifest.domainIdCode
        port = [string]$Manifest.port
    }
    foreach ($entry in $values.GetEnumerator()) {
        if ([string]::IsNullOrWhiteSpace($entry.Value)) { continue }
        $tokenName = ([string]$entry.Key).ToUpperInvariant()
        $pairs.Add([ordered]@{ value = [string]$entry.Value; token = "<$tokenName>" }) | Out-Null
    }
    return @($pairs | Sort-Object { $_.value.Length } -Descending)
}
function Normalize-Text([string] $Text, [object[]] $Pairs) {
    $value = $Text.Replace("`r`n", "`n")
    foreach ($pair in $Pairs) {
        $escaped = [regex]::Escape([string]$pair.value)
        $value = [regex]::Replace($value, $escaped, [string]$pair.token, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
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
        $map[$normalizedPath] = $hash
    }
    return $map
}

$reference = Normalize-DomainName $ReferenceDomain
$candidate = Normalize-DomainName $CandidateDomain
if ($reference -eq $candidate) { throw "서로 다른 두 Domain을 지정해야 합니다." }
$refManifest = Read-Manifest $reference
$canManifest = Read-Manifest $candidate

$capabilityKeys = @('onlineEnabled','databaseEnabled','databaseVendor','batchEnabled','externalEnabled','messagingEnabled','fileEnabled','securityAuditEnabled','uiEnabled','bzaMenuEnabled','productionProfileEnabled')
$capabilityDiff = @()
foreach ($key in $capabilityKeys) {
    if ([string]$refManifest.$key -ne [string]$canManifest.$key) { $capabilityDiff += "$key=$($refManifest.$key)/$($canManifest.$key)" }
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
    normalizedReferenceFileCount = $refContract.Count
    normalizedCandidateFileCount = $canContract.Count
    differences = @($diffs)
}
$resultPath = Join-Path $ResultDir 'generated-domain-parity.sanitized.json'
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20) + [Environment]::NewLine, $Utf8NoBom)
if ($result.status -ne '완료') {
    Write-Host "Generated Domain parity FAILED. result=$resultPath"
    exit 1
}
Write-Host "Generated Domain parity PASS. reference=$reference candidate=$candidate result=$resultPath"
