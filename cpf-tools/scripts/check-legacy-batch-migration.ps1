[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..").Path "build/quality-gate"),
    [string] $BaselineSha = "e725ed3f1bc203e28ff6f06c62a69583358d3b6a"
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
$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message) | Out-Null
}

function Require-File {
    param([string] $RelativePath)
    if (-not (Test-Path -LiteralPath (Join-Path $Root $RelativePath) -PathType Leaf)) {
        Add-Failure "대체 Source/Test 누락: $RelativePath"
    }
}

$baselineFiles = @(& git -C $Root ls-tree -r --name-only $BaselineSha -- "cpf-batch/src")
if ($LASTEXITCODE -ne 0) {
    Add-Failure "Legacy 기준 SHA를 읽을 수 없습니다: $BaselineSha"
} elseif ($baselineFiles.Count -ne 146) {
    Add-Failure "Legacy 기준 파일 수가 이관 문서와 다릅니다: expected=146 actual=$($baselineFiles.Count)"
}

$legacyRoot = Join-Path $Root "cpf-batch/src"
if (Test-Path -LiteralPath $legacyRoot) {
    $legacyFiles = @(Get-ChildItem -LiteralPath $legacyRoot -Recurse -Force -File)
    Add-Failure "Legacy cpf-batch/src가 남아 있습니다. fileCount=$($legacyFiles.Count)"
}

$requiredFiles = @(
    "cpf-batch/contract/src/main/java/com/cpf/batch/api/RuntimeRegistration.java",
    "cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/RuntimeCommonConfiguration.java",
    "cpf-batch/control-server/src/main/java/com/cpf/batch/control/BatchControlServerApplication.java",
    "cpf-batch/control-server/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java",
    "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchSchedulerApplication.java",
    "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerCoordinator.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchWorkerApplication.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerRuntimeState.java",
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerStepHandler.java",
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRunnerApplication.java",
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/SpringBatchCenterCutRuntimeState.java",
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/SpringBatchCenterCutStepHandler.java",
    "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/BatchHostAgentApplication.java",
    "cpf-batch/testkit/src/main/java/com/cpf/batch/testkit/RuntimeRegistrationFixture.java",
    "cpf-core/src/main/java/com/cpf/core/api/batch/CpfBatchOperationsPort.java",
    "cpf-reference/src/main/java/com/cpf/reference/batch/config/ReferenceBatchEducationConfig.java",
    "cpf-reference/src/main/java/com/cpf/reference/batch/controller/ReferenceBatchEducationController.java",
    "cpf-reference/src/main/java/com/cpf/reference/batch/ReferenceBatchPolicyEducationSample.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoClient.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoRemoteClient.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoRequest.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceServiceEchoResponse.java",
    "cpf-reference/src/main/java/com/cpf/reference/centercut/ReferenceCenterCutHandler.java",
    "cpf-reference/src/main/java/com/cpf/reference/centercut/ReferenceCenterCutTargetRepository.java",
    "cpf-batch/runtime-common/src/test/java/com/cpf/batch/runtime/RuntimeIdentityFactoryTest.java",
    "cpf-batch/scheduler/src/test/java/com/cpf/batch/scheduler/SchedulerCoordinatorFencingTest.java",
    "cpf-batch/worker/src/test/java/com/cpf/batch/worker/SpringBatchWorkerRuntimeStateTest.java",
    "cpf-batch/worker/src/test/java/com/cpf/batch/worker/SpringBatchWorkerStepHandlerTest.java",
    "cpf-batch/center-cut-runner/src/test/java/com/cpf/batch/centercut/runner/SpringBatchCenterCutRuntimeStateTest.java",
    "cpf-batch/center-cut-runner/src/test/java/com/cpf/batch/centercut/runner/SpringBatchCenterCutStepHandlerTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/batch/ReferenceBatchRepositoryConfigTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/batch/ReferenceBatchEducationConfigTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/batch/ReferenceBatchEducationControllerTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/batch/ReferenceBatchPolicyEducationSampleTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/servicecall/ReferenceServiceEchoRemoteClientTest.java",
    "cpf-reference/src/test/java/com/cpf/reference/centercut/ReferenceCenterCutAdapterTest.java",
    "cpf-docs/development/CPF_LEGACY_BATCH_MIGRATION_MAP.md"
)
$requiredFiles | ForEach-Object { Require-File $_ }

foreach ($legacyPrimary in @(
        "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRuntime.java",
        "cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java",
        "cpf-batch/worker/src/main/java/com/cpf/batch/worker/internal/JdbcWorkerExecutionRepository.java",
        "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRuntime.java",
        "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutDispatcher.java"
    )) {
    if (Test-Path -LiteralPath (Join-Path $Root $legacyPrimary)) {
        Add-Failure "Legacy Batch Primary Engine 잔존: $legacyPrimary"
    }
}

$expectedBatchProjects = @(
    ":cpf-batch:contract",
    ":cpf-batch:runtime-common",
    ":cpf-batch:control-server",
    ":cpf-batch:scheduler",
    ":cpf-batch:worker",
    ":cpf-batch:center-cut-runner",
    ":cpf-batch:host-agent",
    ":cpf-batch:testkit"
)
$settingsPath = Join-Path $Root "settings.gradle"
if (-not (Test-Path -LiteralPath $settingsPath -PathType Leaf)) {
    Add-Failure "Gradle 설정 누락: settings.gradle"
    $actualBatchProjects = @()
} else {
    $settingsText = [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
    $actualBatchProjects = @(
        [regex]::Matches(
            $settingsText,
            "(?m)^\s*include\s+['""](:cpf-batch:[^'""]+)['""]\s*$") |
            ForEach-Object { $_.Groups[1].Value }
    )
    foreach ($projectPath in $expectedBatchProjects) {
        if ($projectPath -notin $actualBatchProjects) {
            Add-Failure "BAT 독립 Gradle Project 누락: $projectPath"
        }
    }
    foreach ($projectPath in $actualBatchProjects) {
        if ($projectPath -notin $expectedBatchProjects) {
            Add-Failure "알 수 없는 BAT Gradle Project: $projectPath"
        }
    }
    if ($actualBatchProjects.Count -ne $expectedBatchProjects.Count) {
        Add-Failure "BAT Gradle Project 수 불일치: expected=8 actual=$($actualBatchProjects.Count)"
    }
}

$forbiddenPaths = @(
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceMemberSummaryClient.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceMemberSummaryRemoteClient.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceMemberSummaryRequest.java",
    "cpf-reference/src/main/java/com/cpf/reference/servicecall/ReferenceMemberSummaryResponse.java"
)
foreach ($relativePath in $forbiddenPaths) {
    if (Test-Path -LiteralPath (Join-Path $Root $relativePath)) {
        Add-Failure "REF가 특정 Generated Domain EDU 타입을 유지합니다: $relativePath"
    }
}

$sourceFiles = @(
    Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.java" |
        Where-Object {
            $_.FullName -notmatch "[\\/]build[\\/]" -and
            $_.FullName -notmatch "[\\/]bin[\\/]"
        }
)

foreach ($file in $sourceFiles) {
    $relative = $file.FullName.Substring($Root.Length + 1).Replace("\", "/")
    $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    if ($text -match "com\.cpf\.core\.common\.batch") {
        Add-Failure "삭제된 Core Batch 내부 Package 참조: $relative"
    }
    if ($relative -match "^cpf-batch/[^/]+/src/(main|test)/java/" -and
            ($relative -match "/edu/" -or $file.Name -match "EducationSample")) {
        Add-Failure "BAT 독립 Runtime/Library의 EDU Source 잔존: $relative"
    }
    if ($relative -match "^cpf-reference/src/(main|test)/java/com/cpf/reference/(batch|centercut)/" -and
            $text -match "import\s+com\.cpf\.batch\.(runtime|control|scheduler|worker|centercut|agent)") {
        Add-Failure "REF가 BAT Runtime 구현을 직접 import합니다: $relative"
    }
    if ($relative -match "^cpf-reference/src/(main|test)/java/" -and
            $text -match "import\s+com\.cpf\.(member|account|external)\.") {
        Add-Failure "REF가 특정 Generated Domain 구현을 직접 import합니다: $relative"
    }
    if ($relative -match "^cpf-reference/src/(main|test)/java/com/cpf/reference/servicecall/" -and
            ($text -match "ReferenceMemberSummary" -or
             $text -match "(?i)['""]mbr['""]" -or
             $text -match "(?i)/(?:api/[^'""]*/)?mbr(?:/|['""])")) {
        Add-Failure "REF Service Call EDU가 고정 MBR 계약에 결합됩니다: $relative"
    }
}

foreach ($relativeScript in @(
        "cpf-tools/scripts/build-sample-coverage-matrix.ps1",
        "cpf-tools/scripts/check-sample-standard.ps1",
        "cpf-tools/scripts/check-sample-coverage.ps1",
        "cpf-tools/scripts/check-runtime-config-standard.ps1",
        "cpf-tools/scripts/check-spring-event-usage.ps1",
        "cpf-tools/scripts/check-architecture-ownership.ps1"
    )) {
    $path = Join-Path $Root $relativeScript
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        Add-Failure "검증 Script 누락: $relativeScript"
        continue
    }
    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    if ($text.Contains("cpf-batch/src/main/java/com/cpf/batch/edu") -or
            $text.Contains("cpf-batch/src/test/java/com/cpf/batch/edu")) {
        Add-Failure "삭제된 BAT EDU 경로를 정본으로 참조하는 Script: $relativeScript"
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "legacy-batch-migration-result.sanitized.json"
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    baselineSha = $BaselineSha
    baselineFileCount = $baselineFiles.Count
    legacyPathPresent = Test-Path -LiteralPath $legacyRoot
    requiredFileCount = $requiredFiles.Count
    batchProjectCount = $actualBatchProjects.Count
    checkedJavaFileCount = $sourceFiles.Count
    failureCount = $failures.Count
    failures = @($failures)
}
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    [System.Text.UTF8Encoding]::new($false))

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
Write-Host "Legacy BAT migration check passed: $resultPath"
