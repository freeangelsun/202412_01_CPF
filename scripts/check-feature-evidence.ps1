param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$checks = [System.Collections.Generic.List[object]]::new()

function Test-PathSet([string[]] $Paths) {
    return @($Paths | Where-Object {
        -not (Test-Path -LiteralPath (Join-Path $Root $_))
    }).Count -eq 0
}

function Add-Check([string] $Id, [bool] $Passed, [string[]] $Paths, [string] $Meaning) {
    $checks.Add([ordered]@{
        id = $Id
        passed = $Passed
        paths = $Paths
        meaning = $Meaning
        runtimeEvidence = $false
    }) | Out-Null
}

$settingsPath = Join-Path $Root "settings.gradle"
$settings = if (Test-Path $settingsPath) {
    [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
} else { "" }
$modules = @(
    "cpf-core", "cpf-common", "cpf-admin", "cpf-biz-admin", "cpf-batch",
    "cpf-gateway", "cpf-member", "cpf-account", "cpf-reference", "cpf-external"
)
$moduleOk = Test-Path $settingsPath
foreach ($module in $modules) {
    if ($settings -notmatch ("(?m)include[^\r\n]*'" + [regex]::Escape($module) + "'")) {
        $moduleOk = $false
    }
}
Add-Check "MODULE_TOPOLOGY" $moduleOk @("settings.gradle") `
    "공식/현재 Reference Module이 Gradle topology에 존재하는지만 확인합니다."

$serviceCall = @(
    "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java",
    "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceRegistry.java",
    "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfHealthAwareInstanceSelector.java"
)
Add-Check "PROTECTED_SERVICE_CALL_BASELINE" (Test-PathSet $serviceCall) $serviceCall `
    "Service Call Protected Baseline의 Source 존재 회귀만 확인하며 Runtime 성공을 의미하지 않습니다."

$fixed = @(
    "cpf-core/src/main/java/com/cpf/core/api/fixedlength",
    "cpf-core/src/main/java/com/cpf/core/spi/fixedlength",
    "cpf-core/src/main/java/com/cpf/core/common/fixedlength"
)
Add-Check "CORE_FIXED_LENGTH_BOUNDARY" (Test-PathSet $fixed) $fixed `
    "Fixed-Length Public API/SPI/Internal owner 경계가 남아 있는지만 확인합니다."

$centralPack = @(
    "cpf-tools/db/vendor/mariadb/pack.json",
    "cpf-tools/db/vendor/mysql/pack.json",
    "cpf-tools/db/vendor/postgresql/pack.json",
    "cpf-tools/db/vendor/oracle/pack.json",
    "cpf-tools/db/vendor/sqlserver/pack.json",
    "cpf-core/src/main/java/com/cpf/core/common/database/CpfVendorResourceRoot.java",
    "cpf-core/src/main/java/com/cpf/core/common/database/CpfSqlResourceResolver.java",
    "cpf-core/src/main/java/com/cpf/core/common/database/CpfVendorSqlCatalog.java"
)
Add-Check "CENTRAL_VENDOR_PACK_BOUNDARY" (Test-PathSet $centralPack) $centralPack `
    "중앙 Vendor Pack 선택 경계의 구조만 확인합니다. Vendor별 실제 Runtime 검증은 별도입니다."

$generator = @(
    "scripts/create-domain.ps1",
    "scripts/initialize-domain-database.ps1",
    "scripts/verify-domain.ps1",
    "scripts/remove-domain.ps1",
    "cpf-tools/db/domain-metadata.schema.json",
    "cpf-tools/db/central-domain-template-contract.json"
)
Add-Check "DOMAIN_GENERATOR_METADATA_BASELINE" (Test-PathSet $generator) $generator `
    "Generator가 Metadata/Template 기반으로 발전할 Source 계약이 존재하는지만 확인합니다."

$approval = @(
    "cpf-admin/src/main/java/com/cpf/admin/approval/api/AdmApprovalDecisionRule.java",
    "cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java",
    "cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/api/BzaApprovalDecisionRule.java",
    "cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/spi/BzaApprovalDirectoryPort.java",
    "specs/sql/30_adm_schema.sql",
    "specs/sql/40_business_modules_schema.sql"
)
Add-Check "APPROVAL_ARCHITECTURE_BASELINE" (Test-PathSet $approval) $approval `
    "ADM/BZA Approval의 Owner별 DDL/API/SPI baseline만 확인합니다. Engine/API/UI 완료가 아닙니다."

$continuity = @(
    "CPF_FINAL_TARGET_REQUIREMENTS.md",
    "CPF_CURRENT_WORK_REQUEST.md",
    "cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md",
    "cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md",
    "cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md"
)
Add-Check "REQUIREMENT_CONTINUITY_BASELINE" (Test-PathSet $continuity) $continuity `
    "Cross-PC/Cross-Account 영속 인수인계 정본 존재 여부를 확인합니다."

$failed = @($checks | Where-Object { -not $_.passed })
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failed.Count -eq 0) { "완료" } else { "실패" }
    scope = "STATIC_STRUCTURE_ONLY"
    completionClaim = $false
    checkCount = $checks.Count
    failureCount = $failed.Count
    checks = @($checks)
}
$resultPath = Join-Path $ResultDir "feature-evidence-result.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 10),
    [System.Text.UTF8Encoding]::new($false))

if ($failed.Count -gt 0) {
    throw "CPF 구조 Baseline 검사가 실패했습니다: $(($failed | ForEach-Object id) -join ', ')"
}
Write-Host "CPF static feature baseline check passed. Runtime completion is not claimed."
