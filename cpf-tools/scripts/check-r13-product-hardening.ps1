param([string] $Root = "")
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) { $Root = (Resolve-Path "$PSScriptRoot\..\..").Path }

function Require-Text([string]$Path, [string]$Text) {
    $full = Join-Path $Root $Path
    if (-not (Test-Path $full -PathType Leaf)) { throw "Required file missing: $Path" }
    $content = Get-Content $full -Raw -Encoding UTF8
    if (-not $content.Contains($Text)) { throw "Required R13 marker missing: $Path :: $Text" }
}
function Reject-Text([string]$Path, [string]$Text) {
    $full = Join-Path $Root $Path
    if (-not (Test-Path $full -PathType Leaf)) { return }
    $content = Get-Content $full -Raw -Encoding UTF8
    if ($content.Contains($Text)) { throw "Forbidden R13 marker remains: $Path :: $Text" }
}

Require-Text "build.gradle" "cpf-tools/generator/create-domain.ps1"
Require-Text "build.gradle" "cpf.release-manifest"
Require-Text "build.gradle" "cpf.sbom"
Require-Text "build.gradle" "cpf.provenance"
Require-Text "cpf-tools/release/schema/cpf-release-manifest.schema.json" '"$id": "cpf.release-manifest"'
Require-Text "cpf-tools/release/schema/cpf-sbom.schema.json" '"$id": "cpf.sbom"'
Require-Text "cpf-tools/release/schema/cpf-provenance.schema.json" '"$id": "cpf.provenance"'
Require-Text "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmHealthController.java" "SERVICE_UNAVAILABLE"
Require-Text "cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/RuntimeIdentityFactory.java" "/actuator/health/readiness"
Require-Text "cpf-tools/scripts/runtime-common.ps1" 'healthPath = "/adm/api/health/readiness"'
Require-Text "cpf-tools/scripts/runtime-common.ps1" 'healthPath = "/actuator/health/readiness"'
Require-Text "cpf-tools/scripts/runtime-common.ps1" "Get-CpfGeneratedRuntimeModuleMap"
Require-Text "cpf-tools/scripts/runtime-common.ps1" "manifest/generator-ownership.json"
Reject-Text "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogQueryService.java" 'response.put("details", details)'
Reject-Text "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogQueryService.java" '"SELECT * FROM cpf_transaction_log'
Require-Text "cpf-common/src/main/java/com/cpf/common/cde/service/CodeCacheService.java" 'cache.put(ALL_KEY, snapshot)'
Require-Text "cpf-common/src/main/java/com/cpf/common/msg/service/ResponseCodeCacheService.java" 'cache.put(ALL_KEY, latest)'
Require-Text "cpf-common/src/main/java/com/cpf/common/msg/service/ResponseCodeCacheService.java" 'CODE_PREFIX + CpfStrings.normalizeCode(code)'
Reject-Text "cpf-common/src/main/java/com/cpf/common/msg/service/ResponseCodeCacheService.java" 'clearCache();'
Require-Text "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventStore.java" 'Propagation.REQUIRES_NEW'
Require-Text "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventPublisher.java" 'publishRequired'
Require-Text "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventPublisher.java" 'memoryRetryQueue'
Require-Text "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventListener.java" 'lastSuccessfulPollAt'
Require-Text "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCacheController.java" 'com.cpf.core.api.execution.CpfOnlineTransaction'
Reject-Text "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCacheController.java" 'com.cpf.core.common.'
Require-Text "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCsvSanitizer.java" "neutralizeFormula"
Require-Text "cpf-core/src/main/java/com/cpf/core/api/observability/CpfTelemetry.java" "interface CpfTelemetry"
Require-Text "cpf-core/src/main/java/com/cpf/core/spi/feature/CpfFeatureFlagProvider.java" "interface CpfFeatureFlagProvider"
Require-Text "cpf-core/src/main/java/com/cpf/core/api/reliability/CpfFaultInjector.java" "interface CpfFaultInjector"
Require-Text "cpf-tools/scripts/check-contract-compatibility.ps1" "Test-CpfContractCompatibility"
Require-Text "cpf-tools/scripts/check-admin-data-safety.ps1" "ADM/BZA Data Safety"
Require-Text "cpf-tools/scripts/check-data-safety-schema-contract.ps1" "Canonical/lifecycle drift"
Require-Text "cpf-tools/scripts/check-generator-arbitrary-domain-parity.ps1" "임의 Generated Domain"

$pwsh = (Get-Process -Id $PID).Path
& $pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root "cpf-tools/scripts/check-contract-compatibility.ps1") -SelfTest
if ($LASTEXITCODE -ne 0) { throw "Contract compatibility self-test failed." }
& $pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root "cpf-tools/scripts/check-generator-golden-path.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { throw "Generator Golden Path gate failed." }
Write-Host "CPF R13 product hardening static gate passed."
