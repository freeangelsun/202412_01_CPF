[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [ValidateSet('Database', 'AllGeneratorOwned')][string] $Scope = 'Database',
    [string[]] $DomainNames = @(),
    [string[]] $DatabaseVendors = @(),
    [switch] $Apply,
    [switch] $AllowModifiedGeneratorFiles
)

# Canonical synchronization never copies Vendor SQL or ownership metadata into a Generated
# Project. Source upgrades use transient generation-state; DB resources are rendered to build/.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
if ($AllowModifiedGeneratorFiles) {
    throw '-AllowModifiedGeneratorFiles는 폐기되었습니다. 사용자 변경 파일을 우회하지 않고 canonical upgrade가 fail-closed로 판정합니다.'
}

$catalog = @(Get-CpfGeneratedDomainInventory -Root $Root | Where-Object { [bool]$_.exists })
if ($Scope -eq 'Database') {
    $catalog = @($catalog | Where-Object { [bool]$_.databaseEnabled })
}
if ($DomainNames.Count -gt 0) {
    $requested = @($DomainNames | ForEach-Object { $_.Trim().ToLowerInvariant() } | Sort-Object -Unique)
    $unknown = @($requested | Where-Object { $_ -notin @($catalog.domainName) })
    if ($unknown.Count -gt 0) { throw "알 수 없는 Generated Domain입니다: $($unknown -join ',')" }
    $catalog = @($catalog | Where-Object { [string]$_.domainName -in $requested })
}

$contractPath = Join-Path $Root 'cpf-tools/generator/contracts/central-domain-template-contract.json'
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$supportedVendors = @($contract.supportedVendors | ForEach-Object { ([string]$_).ToLowerInvariant() })
if ($DatabaseVendors.Count -eq 0) {
    $DatabaseVendors = $supportedVendors
} else {
    $DatabaseVendors = @($DatabaseVendors | ForEach-Object { $_.Trim().ToLowerInvariant() } | Sort-Object -Unique)
    $unsupported = @($DatabaseVendors | Where-Object { $_ -notin $supportedVendors })
    if ($unsupported.Count -gt 0) { throw "지원하지 않는 DB Vendor입니다: $($unsupported -join ',')" }
}

$rows = @()
foreach ($item in $catalog | Sort-Object systemCode, domainName) {
    if (@($item.forbiddenPermanentMetadata).Count -gt 0) {
        throw "Generated Project에 금지된 영구 metadata가 있습니다: project=$($item.projectName) paths=$(@($item.forbiddenPermanentMetadata) -join ',')"
    }
    $project = Join-Path $Root ([string]$item.projectPath)
    if ($Scope -eq 'AllGeneratorOwned') {
        if ($Apply) {
            $lifecycle = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
                'domain', 'upgrade', [string]$item.domainName,
                '--file', [string]$item.contractPath,
                '--output', $project
            )
        } else {
            $lifecycle = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
                'domain', 'diff', '--file', [string]$item.contractPath, '--output', $project
            )
        }
        $rows += [ordered]@{
            domainName = [string]$item.domainName
            systemCode = [string]$item.systemCode
            scope = $Scope
            applied = [bool]$Apply
            result = $lifecycle
        }
        continue
    }

    $vendorResults = @()
    foreach ($vendor in $DatabaseVendors) {
        $output = Join-Path $Root "cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/verification/$($item.projectName)/db3/$vendor"
        $render = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
            'db', 'render', '--file', [string]$item.contractPath,
            '--vendor', $vendor, '--output', $output
        )
        $vendorResults += $render
    }
    $rows += [ordered]@{
        domainName = [string]$item.domainName
        systemCode = [string]$item.systemCode
        scope = $Scope
        applied = $false
        transientOnly = $true
        vendors = $vendorResults
    }
}

$resultDir = Join-Path $Root 'cpf-docs/governance/development-harness/evidence/platform/current/generated/domain-generator/reports/generated-domain-sync'
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$resultPath = Join-Path $resultDir 'generated-domain-sync.sanitized.json'
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = 'PASS'
    scope = $Scope
    generatedProjectMetadata = 'ABSENT'
    domainCount = $rows.Count
    domains = $rows
}
[IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine,
    $Utf8NoBom)
Write-Host "Generated Domain sync PASS. scope=$Scope domains=$($rows.Count) result=$resultPath"
