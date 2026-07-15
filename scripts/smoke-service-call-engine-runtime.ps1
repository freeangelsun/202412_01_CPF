param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $RunRuntime,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "service-call-engine-runtime-success.sanitized.json"

function Read-Text {
    param([string] $Path)
    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "required file missing: $Path"
    }
    return [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
}

function Test-Contains {
    param([string] $Text, [string] $Needle, [string] $Name)
    if ($Text.IndexOf($Needle, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "required marker missing: $Name"
    }
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    runRuntimeRequested = [bool] $RunRuntime
    validationMode = "source-and-unit-contract"
    checks = @()
}

try {
    $engineText = Read-Text "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallEngine.java"
    $webClientText = Read-Text "pfw/src/main/java/cpf/pfw/common/http/CpfWebClient.java"
    $repositoryText = Read-Text "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceRegistryRepository.java"
    $testText = Read-Text "pfw/src/test/java/cpf/pfw/common/servicecall/CpfServiceCallEngineTest.java"

    Test-Contains $engineText "Function<ServiceCallResolvedTarget, T>" "target-aware runtime adapter"
    Test-Contains $engineText "isCircuitOpen" "circuit open check"
    Test-Contains $engineText "markFailure" "failure state transition"
    Test-Contains $engineText "excludedInstanceIds" "failover excluded instance tracking"
    Test-Contains $webClientText "invokeThroughEngineOrFallback" "CpfWebClient service-call engine path"
    Test-Contains $webClientText "webClient(target)" "selected target HTTP adapter"
    Test-Contains $repositoryText "insertCallHistory" "service call history persistence"
    Test-Contains $repositoryText "recordCircuitFailure" "circuit persistence"
    Test-Contains $testText "invokeRetriesAndFailoversToNextInstance" "retry failover unit test"
    Test-Contains $testText "invokeBlocksRemoteCallWhenCircuitIsOpen" "circuit unit test"

    $result.checks += "service-call-engine-source-contract"
    $result.status = "DONE"
    if ($RunRuntime) {
        $result.runtimeNote = "This script verifies the runtime code path source contract. Real multi-service HTTP runtime must be verified by starting services separately."
        if ($RequireRuntime) {
            throw "real multi-service HTTP runtime was requested as required, but this smoke script only performs source/unit contract verification."
        }
    }
} catch {
    $result.status = "FAILED"
    $result.error = $_.Exception.Message
}

$result.finishedAt = (Get-Date).ToString("o")
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)

if ($result.status -eq "FAILED") {
    throw $result.error
}

Write-Host "service call engine runtime source smoke passed. result=$resultPath"
