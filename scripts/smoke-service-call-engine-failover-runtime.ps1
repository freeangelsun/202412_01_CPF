param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $RunRuntime
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "service-call-engine-failover.sanitized.json"

$engineText = [System.IO.File]::ReadAllText((Join-Path $Root "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceCallEngine.java"), [System.Text.Encoding]::UTF8)
$selectorText = [System.IO.File]::ReadAllText((Join-Path $Root "pfw/src/main/java/cpf/pfw/common/servicecall/CpfHealthAwareInstanceSelector.java"), [System.Text.Encoding]::UTF8)
$testText = [System.IO.File]::ReadAllText((Join-Path $Root "pfw/src/test/java/cpf/pfw/common/servicecall/CpfServiceCallEngineTest.java"), [System.Text.Encoding]::UTF8)

$checks = [ordered]@{
    excludedInstanceTracking = $engineText.Contains("excludedInstanceIds")
    failoverFlag = $engineText.Contains("failoverEnabled")
    selectorExclude = $selectorText.Contains("excludedInstanceIds")
    failoverTest = $testText.Contains("invokeRetriesAndFailoversToNextInstance")
}
$failed = @($checks.GetEnumerator() | Where-Object { -not $_.Value })
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    runRuntimeRequested = [bool] $RunRuntime
    validationMode = "source-failover-contract"
    checks = $checks
    status = $(if ($failed.Count -eq 0) { "DONE" } else { "FAILED" })
    failed = @($failed | ForEach-Object { $_.Key })
    finishedAt = (Get-Date).ToString("o")
}
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
if ($result.status -eq "FAILED") {
    throw "service call failover contract failed: $($result.failed -join ', ')"
}
Write-Host "service call failover source smoke passed. result=$resultPath"
