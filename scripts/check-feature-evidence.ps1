param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

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
    (@(@('cpf-core','cpf-gateway','cpf-common','cpf-admin','cpf-biz-admin','cpf-batch','cpf-member','cpf-account','cpf-reference','cpf-external') |
        Where-Object { $settings -notmatch ("(?m)include[^\r\n]*'" + [regex]::Escape($_) + "'") }).Count -eq 0) `
    @("settings.gradle")

$accGeneratedFiles = @(
    "cpf-account/src/main/java/com/cpf/account/AccApplication.java",
    "cpf-account/src/main/resources/application.yml",
    "cpf-account/manifest/ownership.json",
    "cpf-account/src/main/java/com/cpf/account/reference/adapter/local/LocalAccountReferenceQueryAdapter.java",
    "cpf-account/src/main/java/com/cpf/account/reference/adapter/remote/RemoteAccountReferenceQueryProxy.java",
    "scripts/create-domain.ps1",
    "scripts/initialize-domain-database.ps1",
    "scripts/verify-domain.ps1",
    "scripts/remove-domain.ps1"
)
Add-Check "ACC_GENERATOR_REFERENCE" (Test-Files $accGeneratedFiles) $accGeneratedFiles

$accCrudFiles = @(
    "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountController.java",
    "cpf-account/src/main/java/com/cpf/account/account/service/AccAccountService.java",
    "cpf-account/src/main/java/com/cpf/account/account/port/AccAccountRepository.java",
    "cpf-account/src/main/java/com/cpf/account/account/adapter/JdbcAccAccountRepository.java",
    "cpf-account/src/test/java/com/cpf/account/account/service/AccAccountServiceTest.java",
    "specs/sql/migration/flyway/V33__acc_reference_domain.sql"
)
Add-Check "ACC_REFERENCE_CRUD" `
    ((Test-Files $accCrudFiles) -and
     (Test-Text "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountController.java" "OACCAC000[1-5]") -and
     (Test-Text "specs/sql/40_business_modules_schema.sql" "CREATE TABLE IF NOT EXISTS accDB\.acc_account")) `
    $accCrudFiles

$executionFiles = @(
    "cpf-core/src/main/java/com/cpf/core/common/execution/CpfStandardExecutionId.java",
    "cpf-core/src/main/java/com/cpf/core/common/execution/CpfOnlineTransaction.java",
    "cpf-core/src/main/java/com/cpf/core/common/execution/CpfSharedApi.java",
    "cpf-core/src/main/java/com/cpf/core/common/execution/CpfBatchJob.java",
    "cpf-core/src/test/java/com/cpf/core/common/execution/CpfStandardExecutionIdTest.java",
    "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql",
    "specs/sql/52_standard_execution_alias_seed.sql"
)
Add-Check "STANDARD_EXECUTION_OSB" `
    ((Test-Files $executionFiles) -and
     (Test-Text "cpf-core/src/main/java/com/cpf/core/common/execution/CpfStandardExecutionId.java" "\(\[OSB\]\)") -and
     (Test-Text "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql" "tmp_cpf_standard_execution_id_map") -and
     (Test-Text "specs/sql/52_standard_execution_alias_seed.sql" "ON DUPLICATE KEY UPDATE")) `
    $executionFiles

$gatewayFiles = @(
    "cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java",
    "cpf-core/src/test/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalogTest.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayController.java",
    "cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java",
    "cpf-gateway/src/test/java/com/cpf/gateway/controller/CpfGatewayControllerTest.java",
    "cpf-gateway/build.gradle"
)
Add-Check "CPF_GATEWAY_RUNTIME" `
    ((Test-Files $gatewayFiles) -and
     (Test-Text "cpf-gateway/src/main/java/com/cpf/gateway/controller/CpfGatewayController.java" "CpfHeaderNames\.STANDARD_EXECUTION_ID") -and
     (Test-Text "cpf-core/src/main/java/com/cpf/core/common/gateway/CpfGatewayRouteCatalog.java" "executionType\(\).*ONLINE")) `
    $gatewayFiles

$batchFiles = @(
    "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandController.java",
    "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandService.java",
    "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandJobConfig.java",
    "cpf-batch/src/test/java/com/cpf/batch/edu/ondemand/BatOnDemandServiceTest.java",
    "specs/sql/migration/flyway/V34__batch_on_demand_request.sql"
)
Add-Check "BAT_ON_DEMAND_EDU" `
    ((Test-Files $batchFiles) -and
     (Test-Text "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandController.java" "HttpStatus\.ACCEPTED") -and
     (Test-Text "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandController.java" "/restart") -and
     (Test-Text "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandController.java" "/rerun") -and
     (Test-Text "cpf-batch/src/main/java/com/cpf/batch/edu/ondemand/BatOnDemandJobConfig.java" "BBATOD0001")) `
    $batchFiles

$facadeFiles = @(
    "cpf-common/src/main/java/com/cpf/common/api/account/AccountSummaryFacade.java",
    "cpf-account/src/main/java/com/cpf/account/account/facade/AccAccountSummaryFacade.java",
    "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountSharedController.java",
    "cpf-member/src/main/java/com/cpf/member/integration/account/MbrAccountSummaryRemoteProxy.java",
    "cpf-member/src/test/java/com/cpf/member/integration/account/MbrAccountSummaryRemoteProxyTest.java"
)
Add-Check "MBR_ACC_FACADE_CONTRACT" `
    ((Test-Files $facadeFiles) -and
     (Test-Text "cpf-account/src/main/java/com/cpf/account/account/controller/AccAccountSharedController.java" "SACCAC0001") -and
     (Test-Text "cpf-member/src/main/java/com/cpf/member/integration/account/MbrAccountSummaryRemoteProxy.java" "CpfWebClient")) `
    $facadeFiles

$externalFiles = @(
    "cpf-external/src/main/java/com/cpf/external/execution/api/ExternalExecutionController.java",
    "cpf-external/src/main/java/com/cpf/external/execution/application/ExternalExecutionService.java",
    "cpf-external/src/test/java/com/cpf/external/execution/application/ExternalExecutionServiceTest.java",
    "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorController.java",
    "cpf-reference/src/main/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSample.java",
    "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceNeutralExternalSimulatorControllerTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/external/ReferenceExternalIntegrationEducationSampleTest.java"
)
Add-Check "ACC_EXS_CAPABILITY_RESTORE" (Test-Files $externalFiles) $externalFiles

$generatorFiles = @(
    "scripts/create-domain.ps1",
    "scripts/initialize-domain-database.ps1",
    "scripts/verify-domain.ps1",
    "scripts/remove-domain.ps1",
    "scripts/smoke-create-domain.ps1",
    "scripts/smoke-remove-domain.ps1",
    "scripts/smoke-domain-capability-matrix.ps1",
    "scripts/smoke-acc-lifecycle.ps1",
    "cpf-tools/generator/create-domain.ps1",
    "cpf-tools/generator/initialize-domain-database.ps1",
    "cpf-tools/generator/verify-domain.ps1",
    "cpf-tools/generator/remove-domain.ps1",
    "cpf-tools/generator/create-domain.sh",
    "cpf-tools/generator/initialize-domain-database.sh",
    "cpf-tools/generator/verify-domain.sh",
    "cpf-tools/generator/remove-domain.sh"
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
