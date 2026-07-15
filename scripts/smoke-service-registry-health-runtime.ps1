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
$resultPath = Join-Path $ResultDir "service-registry-health-runtime.sanitized.json"

$repositoryPath = Join-Path $Root "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceRegistryRepository.java"
$checkerPath = Join-Path $Root "pfw/src/main/java/cpf/pfw/common/servicecall/CpfServiceHealthChecker.java"
$repositoryText = [System.IO.File]::ReadAllText($repositoryPath, [System.Text.Encoding]::UTF8)
$checkerText = [System.IO.File]::ReadAllText($checkerPath, [System.Text.Encoding]::UTF8)

$checks = [ordered]@{
    recordHealthStatus = $repositoryText.Contains("recordHealthStatus")
    updateInstanceHealth = $repositoryText.Contains("updateInstanceHealth")
    heartbeatColumn = $repositoryText.Contains("last_heartbeat_at")
    summarize = $checkerText.Contains("summarize")
}

$failed = @($checks.GetEnumerator() | Where-Object { -not $_.Value })
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    runRuntimeRequested = [bool] $RunRuntime
    validationMode = "source-health-contract"
    checks = $checks
    status = $(if ($failed.Count -eq 0) { "DONE" } else { "FAILED" })
    failed = @($failed | ForEach-Object { $_.Key })
    finishedAt = (Get-Date).ToString("o")
}
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
if ($result.status -eq "FAILED") {
    throw "service registry health contract failed: $($result.failed -join ', ')"
}
Write-Host "service registry health source smoke passed. result=$resultPath"
