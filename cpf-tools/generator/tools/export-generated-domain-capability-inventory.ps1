param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root "cpf-tools/generator/tools/generated-domain-common.ps1")
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
        "cpf-tools/generator/verification/check-generated-domain-parity.ps1",
        "cpf-tools/generator/verification/check-generator-arbitrary-domain-parity.ps1",
        "cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1"
    )
}) | Out-Null

$vendorTemplatePaths = @(
    foreach ($vendor in $officialVendors) {
        foreach ($template in @($contract.requiredTemplates)) {
            "cpf-tools/db/generated/domain-template/$vendor/$template"
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
    id = "EDU-EXTERNAL-EDU"
    owner = "EDU"
    description = "특정 Generated Domain과 분리된 대외 호출 정상·오류·결과 불명 교육 흐름"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-education/src/main/java/com/cpf/education/integration/external/EducationExternalIntegrationEducationSample.java",
        "cpf-education/src/main/java/com/cpf/education/integration/external/EducationNeutralExternalSimulatorController.java",
        "cpf-education/src/test/java/com/cpf/education/integration/external/EducationExternalIntegrationEducationSampleTest.java",
        "cpf-education/src/test/java/com/cpf/education/integration/external/EducationNeutralExternalSimulatorControllerTest.java"
    )
}) | Out-Null

$capabilities.Add([ordered]@{
    id = "CPF-SERVICE-CALL"
    owner = "CPF"
    description = "Local·Remote 호출의 공통 실행·복원력 계약"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/servicecall/CpfServiceCallEngine.java",
        "cpf-starters/integration/http/build.gradle"
    )
}) | Out-Null

$capabilities.Add([ordered]@{
    id = "CPF-TRANSACTION-SEGMENT"
    owner = "CPF"
    description = "부모·자식 거래 구간과 호출 타임라인"
    contractValid = $true
    contractMessage = $null
    paths = @(
        "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/segment/TransactionSegmentService.java",
        "cpf-starters/platform-operations/observability/build.gradle",
        "cpf-tools/db/vendor/mariadb/migration/flyway/V19__transaction_segment_trace.sql"
    )
}) | Out-Null

$generatedDomains = [System.Collections.Generic.List[object]]::new()
foreach ($metadata in @(Get-CpfGeneratedDomainInventory -Root $Root)) {
    $generatedDomains.Add($metadata) | Out-Null
    $contractValid =
        [bool]$metadata.exists -and
        [string]$metadata.databaseRole -eq 'CUSTOMER_BUSINESS_DB' -and
        [string]$metadata.generatedProjectMetadata -eq 'NONE' -and
        @($metadata.forbiddenPermanentMetadata).Count -eq 0
    $capabilities.Add([ordered]@{
        id = "GENERATED-DOMAIN-$([string]$metadata.systemCode)"
        owner = [string] $metadata.systemCode
        description = "Canonical definition 기반 Generated Domain $([string]$metadata.domainName)"
        contractValid = $contractValid
        contractMessage = if ($contractValid) {
            $null
        } else {
            "canonical definition/project/permanent-metadata contract mismatch"
        }
        paths = @(
            "$($metadata.projectName)/build.gradle",
            [string]$metadata.definitionPath
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
    contractMessage = if ($goldenReferenceFound) { $null } else { "cpf-member canonical definition/output missing" }
    paths = @(
        "cpf-member/cpf-domain.yaml",
        "cpf-member/build.gradle"
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
    note = "고정 Domain 지원 목록 없이 Framework canonical cpf-domain.yaml 정의와 중앙 Template 계약에서 직접 생성했습니다."
}
$resultPath = Join-Path $ResultDir "generated-domain-capability-inventory.sanitized.json"
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)
if ($failureCount -gt 0) {
    throw "Generated Domain 기능 inventory 검증 실패. missing=$($missingTargets.Count) contract=$($contractFailures.Count)"
}
Write-Host "Generated Domain capability inventory PASS. domains=$($generatedDomains.Count) capabilities=$($items.Count)"
