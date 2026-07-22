param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $RunRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "service-registry-health-runtime.sanitized.json"

$repositoryPath = Join-Path $Root "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceRegistryRepository.java"
$checkerPath = Join-Path $Root "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceHealthChecker.java"
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
