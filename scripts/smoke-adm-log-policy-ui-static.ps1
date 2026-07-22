param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "adm-log-policy-ui-static-result.sanitized.json"
$admSourceRoot = Join-Path $Root "cpf-admin/frontend/src"
$result = [ordered]@{
    checkedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    markers = [ordered]@{}
}

try {
    $sourceFiles = @(Get-ChildItem -LiteralPath $admSourceRoot -Recurse -File |
        Where-Object { $_.Extension -in @(".ts", ".vue") } |
        Sort-Object FullName)
    if ($sourceFiles.Count -eq 0) {
        throw "ADM Vue/TypeScript 정본 소스를 찾지 못했습니다: $admSourceRoot"
    }
    $admSource = ($sourceFiles | ForEach-Object {
        [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    }) -join "`n"
    $markers = [ordered]@{
        traceBoostApi = $admSource.Contains("/adm/api/log-policies/trace-boost")
        traceBoostRuntimeState = $admSource.Contains("/adm/api/log-policies/runtime-state")
        traceBoostHistory = $admSource.Contains("/adm/api/log-policies/history")
        traceBoostDisable = $admSource.Contains("/adm/api/log-policies/") -and $admSource.Contains("/disable")
        batchCalendar = $admSource.Contains("/adm/api/batch/calendar") -and $admSource.Contains("businessDate")
        batchSimulation = $admSource.Contains("/simulation") -and $admSource.Contains("simulationDays")
        batchRelations = $admSource.Contains("/adm/api/batch/relations")
        batchTargets = $admSource.Contains("/adm/api/batch/execution-targets")
        batchLocks = $admSource.Contains("/adm/api/batch/locks")
        batchGhost = $admSource.Contains("/adm/api/batch/ghost-candidates")
        legacyExternalApiRemoved = -not $admSource.Contains("/api/exs/") -and -not $admSource.Contains("/api/acc/")
        centerCutLink = $admSource.Contains("/adm/api/center-cut/jobs")
        reasonGuard = $admSource.Contains("requireReason")
        operationConsoleMenus = $admSource.Contains('id: "batch"') -or $admSource.Contains("id: 'batch'")
    }
    foreach ($key in $markers.Keys) {
        $result.markers[$key] = [bool] $markers[$key]
    }
    $missing = @($result.markers.GetEnumerator() | Where-Object { -not $_.Value })
    $result.status = $(if ($missing.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    if ($missing.Count -gt 0) {
        $result.failureClassification = "implementation"
        $result.missingMarkers = @($missing | ForEach-Object { $_.Key })
    }
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.error = $_.Exception.Message
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    throw
}

Write-Host "ADM log policy UI static smoke finished. status=$($result.status) result=$resultPath"
