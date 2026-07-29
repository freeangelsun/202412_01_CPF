param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $All,
    [string[]] $DomainName = @(),
    [string[]] $SystemCode = @(),
    [ValidateSet("bootstrap", "migration", "verify", "rollback")]
    [string] $Operation = "bootstrap",
    [switch] $Apply,
    [switch] $ConfirmRollback
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$centralContractPath = Join-Path $Root "cpf-tools/generator/contracts/central-domain-template-contract.json"
$metadataSchemaPath = Join-Path $Root "cpf-tools/generator/contracts/domain-metadata.schema.json"
foreach ($contractPath in @($centralContractPath, $metadataSchemaPath)) {
    if (-not (Test-Path -LiteralPath $contractPath -PathType Leaf)) {
        throw "Generated Domain 중앙 계약 파일이 없습니다: $contractPath"
    }
}
$centralContract = Get-Content -LiteralPath $centralContractPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 50
$metadataSchema = Get-Content -LiteralPath $metadataSchemaPath -Raw -Encoding UTF8 |
        ConvertFrom-Json -Depth 50
$expectedMetadataVersion = [string]$metadataSchema.properties.metadataVersion.const
$expectedDomainType = [string]$metadataSchema.properties.domainType.const
$allowedTemplateContractVersions = @(
    $metadataSchema.properties.templateContractVersion.enum |
        ForEach-Object { [string]$_ } |
        Sort-Object -Unique
)
$currentTemplateContractVersion = [string]$centralContract.contractVersion
$supportedDatabaseVendors = @(
    $centralContract.supportedVendors |
        ForEach-Object { ([string]$_).Trim().ToLowerInvariant() } |
        Sort-Object -Unique
)
if ([string]::IsNullOrWhiteSpace($expectedMetadataVersion) -or
        [string]::IsNullOrWhiteSpace($expectedDomainType) -or
        $allowedTemplateContractVersions.Count -eq 0 -or
        $currentTemplateContractVersion -notin $allowedTemplateContractVersions -or
        $supportedDatabaseVendors.Count -eq 0) {
    throw "Generated Domain 중앙 계약/schema의 버전 또는 Vendor 계약이 유효하지 않습니다."
}
$initializer = Join-Path $Root "cpf-tools/scripts/initialize-domain-database.ps1"
if (-not (Test-Path -LiteralPath $initializer -PathType Leaf)) {
    throw "Generated Domain DB initializer가 없습니다: $initializer"
}

$catalog = @()
Get-ChildItem -LiteralPath $Root -Directory -Filter "cpf-*" | ForEach-Object {
    $manifestPath = Join-Path $_.FullName "manifest/domain-manifest.json"
    $ownershipPath = Join-Path $_.FullName "manifest/generator-ownership.json"
    $profilePath = Join-Path $_.FullName "deploy/database/database-profile.json"
    if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) { return }

    $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $domainTypeProperty = $manifest.PSObject.Properties["domainType"]
    if ($null -eq $domainTypeProperty -or
            [string]$domainTypeProperty.Value -ne $expectedDomainType) {
        return
    }
    $databaseEnabledProperty = $manifest.PSObject.Properties["databaseEnabled"]
    if ($null -eq $databaseEnabledProperty) {
        throw "Generated Domain manifest에 databaseEnabled가 없습니다: $manifestPath"
    }
    if (-not [bool]$databaseEnabledProperty.Value) { return }
    if ([string]$manifest.metadataVersion -ne $expectedMetadataVersion -or
            [string]$manifest.templateContractVersion -notin $allowedTemplateContractVersions -or
            [string]$manifest.projectName -ne $_.Name) {
        throw "Generated Domain manifest identity/contract가 유효하지 않습니다: $manifestPath"
    }
    $databaseVendor = ([string]$manifest.databaseVendor).Trim().ToLowerInvariant()
    if ($databaseVendor -notin $supportedDatabaseVendors) {
        throw "Generated Domain manifest Vendor가 중앙 계약에 없습니다: path=$manifestPath vendor=$databaseVendor"
    }
    if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
        throw "Generated Domain ownership manifest가 없습니다: $ownershipPath"
    }
    $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]$ownership.projectName -ne [string]$manifest.projectName -or
            [string]$ownership.domainName -ne [string]$manifest.domainName -or
            [string]$ownership.systemCode -ne [string]$manifest.systemCode) {
        throw "Generated Domain manifest/ownership identity가 일치하지 않습니다: $($_.FullName)"
    }
    if (-not (Test-Path -LiteralPath $profilePath -PathType Leaf)) {
        throw "Generated Domain DB Profile이 없습니다: $profilePath"
    }

    $catalog += [pscustomobject]@{
        domainName = ([string]$manifest.domainName).ToLowerInvariant()
        systemCode = ([string]$manifest.systemCode).ToUpperInvariant()
        projectName = [string]$manifest.projectName
        databaseVendor = $databaseVendor
        profilePath = $profilePath
    }
}

if ($catalog.Count -eq 0) {
    if ($DomainName.Count -gt 0 -or $SystemCode.Count -gt 0) {
        throw "선택 조건과 일치하는 DB 활성 GENERATED_DOMAIN을 찾을 수 없습니다."
    }
    $resultDir = Join-Path $Root "build/db-install/generated-domains"
    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $resultPath = Join-Path $resultDir "generated-domain-batch-result.sanitized.json"
    [IO.File]::WriteAllText(
            $resultPath,
            (([ordered]@{
                generatedAt = (Get-Date).ToString("o")
                operation = $Operation
                applied = [bool]$Apply
                domains = @()
                reason = "DB가 활성화되고 generator ownership이 확인된 Generated Domain이 없습니다."
            } | ConvertTo-Json -Depth 10) + [Environment]::NewLine),
            [Text.UTF8Encoding]::new($false))
    Write-Host "Generated Domain DB selected: none (optional). result=$resultPath"
    return
}

$domainDup = @($catalog | Group-Object domainName | Where-Object Count -gt 1)
$codeDup = @($catalog | Group-Object systemCode | Where-Object Count -gt 1)
if ($domainDup.Count -gt 0) { throw "Generated DomainName 중복: $((($domainDup | ForEach-Object Name) -join ', '))" }
if ($codeDup.Count -gt 0) { throw "Generated SystemCode 중복: $((($codeDup | ForEach-Object Name) -join ', '))" }

if ($All -and ($DomainName.Count -gt 0 -or $SystemCode.Count -gt 0)) {
    throw "-All과 DomainName/SystemCode는 동시에 사용할 수 없습니다."
}

if ($All -or ($DomainName.Count -eq 0 -and $SystemCode.Count -eq 0)) {
    $selected = @($catalog | Sort-Object systemCode, domainName)
} else {
    $selectedMap = [System.Collections.Generic.Dictionary[string,object]]::new([System.StringComparer]::OrdinalIgnoreCase)

    foreach ($name in $DomainName) {
        $match = @($catalog | Where-Object { $_.domainName -eq $name.ToLowerInvariant() })
        if ($match.Count -eq 0) { throw "알 수 없는 Generated DomainName입니다: $name" }
        $selectedMap[$match[0].domainName] = $match[0]
    }

    foreach ($code in $SystemCode) {
        $normalized = $code.ToUpperInvariant()
        $match = @($catalog | Where-Object { $_.systemCode -eq $normalized })
        if ($match.Count -eq 0) { throw "알 수 없는 Generated SystemCode입니다: $code" }
        $selectedMap[$match[0].domainName] = $match[0]
    }

    $selected = @($selectedMap.Values | Sort-Object systemCode, domainName)
}

if ($Operation -eq "rollback" -and (-not $Apply -or -not $ConfirmRollback)) {
    throw "Generated Domain rollback은 -Apply -ConfirmRollback이 모두 필요합니다."
}

Write-Host "Generated Domain DB selected: $((@($selected | ForEach-Object { "$($_.domainName)/$($_.systemCode)" })) -join ', ')"

$results = @()
foreach ($item in $selected) {
    Write-Host "[$($item.domainName)] operation=$Operation"
    $args = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", $initializer,
        "-DomainName", $item.domainName,
        "-SystemCode", $item.systemCode,
        "-Root", $Root,
        "-ProfilePath", $item.profilePath,
        "-Operation", $Operation
    )
    if ($Apply) { $args += "-Apply" }
    if ($ConfirmRollback) { $args += "-ConfirmRollback" }

    & pwsh @args
    if ($LASTEXITCODE -ne 0) {
        throw "Generated Domain DB 작업 실패: domain=$($item.domainName), operation=$Operation"
    }
    $results += [pscustomobject]@{
        domainName = $item.domainName
        systemCode = $item.systemCode
        operation = $Operation
        status = if ($Apply) { "완료" } else { "미검증" }
    }
}

$resultDir = Join-Path $Root "build/db-install/generated-domains"
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$resultPath = Join-Path $resultDir "generated-domain-batch-result.sanitized.json"
@{
    generatedAt = (Get-Date).ToString("o")
    operation = $Operation
    applied = [bool]$Apply
    domains = $results
} | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $resultPath -Encoding UTF8

Write-Host "Generated Domain DB result: $resultPath"
