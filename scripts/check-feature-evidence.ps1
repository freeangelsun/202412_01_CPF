param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260716_02")
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$ResultDir = [IO.Path]::GetFullPath($ResultDir)
if (-not $ResultDir.StartsWith($Root, [StringComparison]::OrdinalIgnoreCase)) {
    throw "ResultDir는 저장소 내부 경로여야 합니다. path=$ResultDir"
}
$resultDirRelative = $ResultDir.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
$checks = [System.Collections.Generic.List[object]]::new()

function Test-File([string] $RelativePath) {
    Test-Path -LiteralPath (Join-Path $Root $RelativePath) -PathType Leaf
}

function Test-Text([string] $RelativePath, [string] $Pattern) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        return $false
    }
    $text = [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8)
    [Regex]::IsMatch($text, $Pattern, [Text.RegularExpressions.RegexOptions]::CultureInvariant)
}

function Test-Files([string[]] $Paths) {
    @($Paths | Where-Object { -not (Test-File $_) }).Count -eq 0
}

function Add-Check([string] $Id, [bool] $Passed, [string[]] $Evidence) {
    $checks.Add([ordered]@{
            id = $Id
            passed = $Passed
            evidence = $Evidence
        }) | Out-Null
}

$settings = [IO.File]::ReadAllText((Join-Path $Root "settings.gradle"), [Text.Encoding]::UTF8)
Add-Check "MODULE_TOPOLOGY" `
    ($settings -match "include\s+'pfw'.*'cmn'.*'mbr'.*'xyz'.*'adm'.*'bza'.*'bat'" -and
     $settings -match "include\s+'pfw-gateway-runtime'" -and $settings -match "include\s+'acc'") `
    @("settings.gradle")

$accGeneratedFiles = @(
    "acc/src/main/java/cpf/acc/AccApplication.java",
    "acc/src/main/resources/application.yml",
    "acc/manifest/ownership.json",
    "acc/src/main/java/cpf/acc/reference/adapter/local/LocalAccountReferenceQueryAdapter.java",
    "acc/src/main/java/cpf/acc/reference/adapter/remote/RemoteAccountReferenceQueryProxy.java",
    "$resultDirRelative/create-domain-result.sanitized.json",
    "$resultDirRelative/remove-domain-smoke.sanitized.json"
)
Add-Check "ACC_GENERATOR_REFERENCE" (Test-Files $accGeneratedFiles) $accGeneratedFiles

$accCrudFiles = @(
    "acc/src/main/java/cpf/acc/account/controller/AccAccountController.java",
    "acc/src/main/java/cpf/acc/account/service/AccAccountService.java",
    "acc/src/main/java/cpf/acc/account/port/AccAccountRepository.java",
    "acc/src/main/java/cpf/acc/account/adapter/JdbcAccAccountRepository.java",
    "acc/src/test/java/cpf/acc/account/service/AccAccountServiceTest.java",
    "specs/sql/migration/flyway/V33__acc_reference_domain.sql"
)
Add-Check "ACC_REFERENCE_CRUD" `
    ((Test-Files $accCrudFiles) -and
     (Test-Text "acc/src/main/java/cpf/acc/account/controller/AccAccountController.java" "OACCAC000[1-5]") -and
     (Test-Text "specs/sql/40_business_modules_schema.sql" "CREATE TABLE IF NOT EXISTS accDB\.acc_account")) `
    $accCrudFiles

$executionFiles = @(
    "pfw/src/main/java/cpf/pfw/common/execution/CpfStandardExecutionId.java",
    "pfw/src/main/java/cpf/pfw/common/execution/CpfOnlineTransaction.java",
    "pfw/src/main/java/cpf/pfw/common/execution/CpfSharedApi.java",
    "pfw/src/main/java/cpf/pfw/common/execution/CpfBatchJob.java",
    "pfw/src/test/java/cpf/pfw/common/execution/CpfStandardExecutionIdTest.java",
    "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql",
    "specs/sql/52_standard_execution_alias_seed.sql"
)
Add-Check "STANDARD_EXECUTION_OSB" `
    ((Test-Files $executionFiles) -and
     (Test-Text "pfw/src/main/java/cpf/pfw/common/execution/CpfStandardExecutionId.java" "\(\[OSB\]\)") -and
     (Test-Text "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql" "tmp_pfw_standard_execution_id_map") -and
     (Test-Text "specs/sql/52_standard_execution_alias_seed.sql" "ON DUPLICATE KEY UPDATE")) `
    $executionFiles

$gatewayFiles = @(
    "pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalog.java",
    "pfw/src/test/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalogTest.java",
    "pfw-gateway-runtime/src/main/java/cpf/pfw/gateway/controller/PfwGatewayController.java",
    "pfw-gateway-runtime/src/main/java/cpf/pfw/gateway/service/PfwGatewayProxyService.java",
    "pfw-gateway-runtime/src/test/java/cpf/pfw/gateway/controller/PfwGatewayControllerTest.java",
    "pfw-gateway-runtime/build.gradle"
)
Add-Check "PFW_GATEWAY_RUNTIME" `
    ((Test-Files $gatewayFiles) -and
     (Test-Text "pfw-gateway-runtime/src/main/java/cpf/pfw/gateway/controller/PfwGatewayController.java" "CpfHeaderNames\.STANDARD_EXECUTION_ID") -and
     (Test-Text "pfw/src/main/java/cpf/pfw/common/gateway/CpfGatewayRouteCatalog.java" "executionType\(\).*ONLINE")) `
    $gatewayFiles

$batchFiles = @(
    "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandController.java",
    "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandService.java",
    "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandJobConfig.java",
    "bat/src/test/java/cpf/bat/edu/ondemand/BatOnDemandServiceTest.java",
    "specs/sql/migration/flyway/V34__batch_on_demand_request.sql"
)
Add-Check "BAT_ON_DEMAND_EDU" `
    ((Test-Files $batchFiles) -and
     (Test-Text "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandController.java" "HttpStatus\.ACCEPTED") -and
     (Test-Text "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandController.java" "/restart") -and
     (Test-Text "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandController.java" "/rerun") -and
     (Test-Text "bat/src/main/java/cpf/bat/edu/ondemand/BatOnDemandJobConfig.java" "BBATOD0001")) `
    $batchFiles

$facadeFiles = @(
    "cmn/src/main/java/cpf/cmn/api/account/AccountSummaryFacade.java",
    "acc/src/main/java/cpf/acc/account/facade/AccAccountSummaryFacade.java",
    "acc/src/main/java/cpf/acc/account/controller/AccAccountSharedController.java",
    "mbr/src/main/java/cpf/mbr/integration/account/MbrAccountSummaryRemoteProxy.java",
    "mbr/src/test/java/cpf/mbr/integration/account/MbrAccountSummaryRemoteProxyTest.java"
)
Add-Check "MBR_ACC_FACADE_CONTRACT" `
    ((Test-Files $facadeFiles) -and
     (Test-Text "acc/src/main/java/cpf/acc/account/controller/AccAccountSharedController.java" "SACCAC0001") -and
     (Test-Text "mbr/src/main/java/cpf/mbr/integration/account/MbrAccountSummaryRemoteProxy.java" "CpfWebClient")) `
    $facadeFiles

$externalFiles = @(
    "xyz/src/main/java/cpf/xyz/external/XyzNeutralExternalSimulatorController.java",
    "xyz/src/main/java/cpf/xyz/external/XyzExternalIntegrationEducationSample.java",
    "xyz/src/test/java/cpf/xyz/external/XyzNeutralExternalSimulatorControllerTest.java",
    "xyz/src/test/java/cpf/xyz/external/XyzExternalIntegrationEducationSampleTest.java",
    "$resultDirRelative/acc-exs-capability-inventory.sanitized.json"
)
Add-Check "ACC_EXS_CAPABILITY_RESTORE" (Test-Files $externalFiles) $externalFiles

$generatorFiles = @(
    "scripts/create-domain.ps1",
    "scripts/remove-domain.ps1",
    "scripts/smoke-create-domain.ps1",
    "scripts/smoke-remove-domain.ps1",
    "$resultDirRelative/create-domain-result.sanitized.json",
    "$resultDirRelative/remove-domain-smoke.sanitized.json"
)
Add-Check "DOMAIN_GENERATOR_SMOKE" `
    ((Test-Files $generatorFiles) -and
     (Test-Text "scripts/create-domain.ps1" "bootWar") -and
     (Test-Text "scripts/create-domain.ps1" "SpringBootServletInitializer") -and
     (Test-Text "scripts/create-domain.ps1" "BaseController")) `
    $generatorFiles

$sqlFiles = @(
    "specs/sql/00_all_install.sql",
    "specs/sql/00_all_install_and_smoke.sql",
    "specs/sql/99_smoke_check.sql",
    "scripts/build-all-install-sql.ps1"
)
Add-Check "INSTALL_SQL_SOURCE_SET" (Test-Files $sqlFiles) $sqlFiles

$failed = @($checks | Where-Object { -not $_.passed })
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    status = if ($failed.Count -eq 0) { "DONE" } else { "FAILED" }
    checkCount = $checks.Count
    failureCount = $failed.Count
    checks = $checks
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "feature-evidence-result.sanitized.json"
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10), [Text.UTF8Encoding]::new($false))

if ($failed.Count -gt 0) {
    $failedIds = ($failed | ForEach-Object id) -join ", "
    throw "CPF 마일스톤 기능 증적 검사가 실패했습니다: $failedIds"
}
Write-Host "CPF 마일스톤 기능 증적 검사 통과: $($checks.Count)건"
