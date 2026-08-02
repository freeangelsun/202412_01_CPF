param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$contractPath = Join-Path $Root "cpf-tools/generator/contracts/central-domain-template-contract.json"
if (-not (Test-Path -LiteralPath $contractPath -PathType Leaf)) {
    throw "Generated Domain 중앙 계약이 없습니다: $contractPath"
}
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json
$contractVersion = [string] $contract.contractVersion
$officialVendors = @($contract.supportedVendors | ForEach-Object { ([string] $_).ToLowerInvariant() })
if (($officialVendors -join ",") -ne "mariadb,postgresql,oracle") {
    throw "Generated Domain 공식 Vendor 계약이 올바르지 않습니다: $($officialVendors -join ',')"
}

$capabilities = [System.Collections.Generic.List[object]]::new()
$capabilities.Add([ordered]@{
    id = "GEN-CENTRAL-GOLDEN-TEMPLATE"
    owner = "GEN"
    description = "임의 Domain Metadata를 단일 Golden Template과 공식 DB 3종으로 생성·검증"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-tools/generator/create-domain.ps1",
        "cpf-tools/generator/verify-domain-federation.ps1",
        "cpf-tools/generator/contracts/central-domain-template-contract.json",
        "cpf-tools/scripts/check-generated-domain-parity.ps1",
        "cpf-tools/scripts/check-generator-arbitrary-domain-parity.ps1",
        "cpf-tools/scripts/smoke-generated-domain-lifecycle.ps1"
    )
}) | Out-Null

$vendorTemplatePaths = @(
    foreach ($vendor in $officialVendors) {
        foreach ($template in @($contract.requiredTemplates)) {
            "cpf-tools/db/vendor/$vendor/domain-template/$template"
        }
    }
)
$capabilities.Add([ordered]@{
    id = "GEN-CENTRAL-VENDOR-TEMPLATES"
    owner = "CPF DB Tool"
    description = "Generated Domain이 Module 내부 Vendor SQL 복제 없이 중앙 Template을 사용"
    contractValid = $true
    contractMessage = $null
    paths = $vendorTemplatePaths
}) | Out-Null

$capabilities.Add([ordered]@{
    id = "REF-EXTERNAL-EDU"
    owner = "REF"
    description = "특정 Generated Domain과 분리된 대외 호출 정상·오류·결과 불명 교육 흐름"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSample.java",
        "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorController.java",
        "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSampleTest.java",
        "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorControllerTest.java"
    )
}) | Out-Null

$capabilities.Add([ordered]@{
    id = "CPF-SERVICE-CALL"
    owner = "CPF"
    description = "Local·Remote 호출의 공통 실행·복원력 계약"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java",
        "cpf-core/src/test/java/com/cpf/core/common/servicecall/CpfServiceCallEngineTest.java"
    )
}) | Out-Null

$capabilities.Add([ordered]@{
    id = "CPF-TRANSACTION-SEGMENT"
    owner = "CPF"
    description = "부모·자식 거래 구간과 호출 타임라인"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentService.java",
        "cpf-core/src/test/java/com/cpf/core/common/logging/segment/TransactionSegmentServiceTest.java",
        "cpf-tools/db/vendor/mariadb/migration/flyway/V19__transaction_segment_trace.sql"
    )
}) | Out-Null

$generatedDomains = [System.Collections.Generic.List[object]]::new()
foreach ($moduleDirectory in Get-ChildItem -LiteralPath $Root -Directory -Filter "cpf-*") {
    $relativeManifest = "$($moduleDirectory.Name)/manifest/domain-manifest.json"
    $manifestPath = Join-Path $Root $relativeManifest
    if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
        continue
    }
    $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string] $manifest.domainType -ne "GENERATED_DOMAIN") {
        continue
    }
    $generatedDomains.Add($manifest) | Out-Null
    $supportedVendors = @($manifest.supportedDatabaseVendors |
        ForEach-Object { ([string] $_).ToLowerInvariant() } |
        Sort-Object)
    $contractValid =
        [string] $manifest.templateContractVersion -eq $contractVersion -and
        [string] $manifest.projectName -eq $moduleDirectory.Name -and
        [int] $manifest.physicalTableContract.totalTables -eq 2 -and
        [int] $manifest.physicalTableContract.businessTableCount -eq 1 -and
        [int] $manifest.physicalTableContract.supportLedgerCount -eq 1 -and
        ($supportedVendors -join ",") -eq (($officialVendors | Sort-Object) -join ",")
    $capabilities.Add([ordered]@{
        id = "GENERATED-DOMAIN-$([string]$manifest.systemCode)"
        owner = [string] $manifest.systemCode
        description = "Metadata 기반 Generated Domain $([string]$manifest.domainName)"
        contractValid = $contractValid
        contractMessage = if ($contractValid) {
            $null
        } else {
            "manifest identity/template/table/vendor contract mismatch"
        }
        paths = @(
            "$($moduleDirectory.Name)/build.gradle",
            $relativeManifest,
            "$($moduleDirectory.Name)/manifest/generator-ownership.json"
        )
    }) | Out-Null
}

$goldenReferenceFound = @($generatedDomains | Where-Object {
    [string] $_.domainName -eq "member" -and [string] $_.projectName -eq "cpf-member"
}).Count -eq 1
$capabilities.Add([ordered]@{
    id = "GEN-GOLDEN-REFERENCE"
    owner = "GEN"
    description = "cpf-member가 다른 임의 Domain과 동일 Generator 산출물인 Golden Reference"
    contractValid = $goldenReferenceFound
    contractMessage = if ($goldenReferenceFound) { $null } else { "cpf-member Golden Reference manifest missing" }
    paths = @(
        "cpf-member/manifest/domain-manifest.json",
        "cpf-member/manifest/generator-ownership.json"
    )
}) | Out-Null

$items = @($capabilities | ForEach-Object {
    $capability = $_
    $checks = @($capability.paths | ForEach-Object {
        $relativePath = [string] $_
        $absolutePath = Join-Path $Root $relativePath
        $exists = Test-Path -LiteralPath $absolutePath -PathType Leaf
        [ordered]@{
            path = $relativePath
            exists = $exists
            sha256 = if ($exists) {
                (Get-FileHash -LiteralPath $absolutePath -Algorithm SHA256).Hash.ToLowerInvariant()
            } else {
                $null
            }
        }
    })
    $pathsValid = @($checks | Where-Object { -not $_.exists }).Count -eq 0
    [ordered]@{
        id = $capability.id
        owner = $capability.owner
        description = $capability.description
        status = if ($pathsValid -and $capability.contractValid) { "DONE" } else { "FAILED" }
        contractValid = [bool] $capability.contractValid
        contractMessage = $capability.contractMessage
        artifacts = $checks
    }
})

$missingTargets = @($items | ForEach-Object { $_.artifacts } | Where-Object { -not $_.exists })
$contractFailures = @($items | Where-Object { -not $_.contractValid })
$failureCount = $missingTargets.Count + $contractFailures.Count
$currentCommit = (git -C $Root rev-parse HEAD).Trim()
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = if ($failureCount -eq 0) { "DONE" } else { "FAILED" }
    currentCommit = $currentCommit
    inventorySource = "current-source"
    templateContractVersion = $contractVersion
    generatedDomainCount = $generatedDomains.Count
    capabilityCount = $items.Count
    items = $items
    missingTargetCount = $missingTargets.Count
    contractFailureCount = $contractFailures.Count
    note = "고정 Domain 지원 목록 없이 현재 manifest와 중앙 Template 계약에서 직접 생성했습니다."
}
$resultPath = Join-Path $ResultDir "generated-domain-capability-inventory.sanitized.json"
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)
if ($failureCount -gt 0) {
    throw "Generated Domain 기능 inventory 검증 실패. missing=$($missingTargets.Count) contract=$($contractFailures.Count)"
}
Write-Host "Generated Domain capability inventory PASS. domains=$($generatedDomains.Count) capabilities=$($items.Count)"
