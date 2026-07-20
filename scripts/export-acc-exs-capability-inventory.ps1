param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260716_02")
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$legacyEvidencePath = Join-Path $Root "specs/evidence/20260716_01/acc-exs-capability-inventory.sanitized.json"
if (-not (Test-Path -LiteralPath $legacyEvidencePath -PathType Leaf)) {
    throw "삭제 전 ACC/EXS 기능 inventory 원본이 없습니다. path=$legacyEvidencePath"
}
$legacy = Get-Content -LiteralPath $legacyEvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json
$requiredTargets = @(
    "acc/src/main/java/cpf/acc/account/controller/AccAccountController.java",
    "acc/src/main/java/cpf/acc/account/controller/AccAccountSharedController.java",
    "acc/src/main/java/cpf/acc/account/facade/AccAccountSummaryFacade.java",
    "mbr/src/main/java/cpf/mbr/integration/account/MbrAccountSummaryRemoteProxy.java",
    "mbr/src/test/java/cpf/mbr/integration/account/MbrAccountSummaryRemoteProxyTest.java",
    "xyz/src/main/java/cpf/xyz/edu/external/XyzExternalIntegrationEducationSample.java",
    "xyz/src/main/java/cpf/xyz/edu/external/XyzNeutralExternalSimulatorController.java",
    "xyz/src/test/java/cpf/xyz/edu/external/XyzExternalIntegrationEducationSampleTest.java",
    "xyz/src/test/java/cpf/xyz/edu/external/XyzNeutralExternalSimulatorControllerTest.java",
    "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallEngine.java",
    "pfw/src/main/java/cpf/pfw/common/logging/segment/TransactionSegmentService.java"
)
$targetChecks = @($requiredTargets | ForEach-Object {
        [ordered]@{
            path = $_
            exists = Test-Path -LiteralPath (Join-Path $Root $_) -PathType Leaf
        }
    })
$missingTargets = @($targetChecks | Where-Object { -not $_.exists })
$currentCommit = (git -C $Root rev-parse HEAD).Trim()
$result = [ordered]@{
    generatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffK")
    status = if ($missingTargets.Count -eq 0) { "DONE" } else { "FAILED" }
    currentCommit = $currentCommit
    legacyInventoryCommit = $legacy.sourceCommit
    legacyFileCount = $legacy.legacyFileCount
    items = $legacy.items
    targetChecks = $targetChecks
    missingTargetCount = $missingTargets.Count
    note = "삭제 전 기능 disposition은 역사 inventory를 유지하고, 현재 대체 source와 test 존재를 이번 commit에서 다시 검증했습니다."
}
$resultPath = Join-Path $ResultDir "acc-exs-capability-inventory.sanitized.json"
[IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $Utf8NoBom)
if ($missingTargets.Count -gt 0) {
    throw "ACC/EXS 대체 기능 inventory 검증이 실패했습니다. missing=$($missingTargets.Count)"
}
Write-Host "ACC/EXS capability inventory revalidation passed. count=$($legacy.legacyFileCount)"
