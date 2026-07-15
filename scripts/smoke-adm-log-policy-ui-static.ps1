param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "adm-log-policy-ui-static-result.sanitized.json"
$admJsPath = Join-Path $Root "adm/src/main/resources/static/adm/adm.js"
$admHtmlPath = Join-Path $Root "adm/src/main/resources/static/adm/index.html"
$result = [ordered]@{
    checkedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    markers = [ordered]@{}
}

try {
    $admJs = [System.IO.File]::ReadAllText($admJsPath, [System.Text.Encoding]::UTF8)
    $admHtml = [System.IO.File]::ReadAllText($admHtmlPath, [System.Text.Encoding]::UTF8)
    $markers = [ordered]@{
        traceBoostApi = $admJs.Contains("/adm/api/log-policies/trace-boost")
        traceBoostRuntimeState = $admJs.Contains("/adm/api/log-policies/runtime-state")
        traceBoostHistory = $admJs.Contains("/adm/api/log-policies/history")
        traceBoostDisable = $admJs.Contains("/disable")
        batchCalendar = $admJs.Contains("/adm/api/batch/calendar") -and $admJs.Contains("businessDate")
        batchSimulation = $admJs.Contains("/simulation") -and $admJs.Contains("simulationDays")
        batchRelations = $admJs.Contains("/adm/api/batch/relations")
        batchTargets = $admJs.Contains("/adm/api/batch/execution-targets")
        batchLocks = $admJs.Contains("/adm/api/batch/locks")
        batchGhost = $admJs.Contains("/adm/api/batch/ghost-candidates")
        legacyExternalApiRemoved = -not $admJs.Contains("/api/exs/") -and -not $admJs.Contains("/api/acc/")
        centerCutLink = $admJs.Contains("/adm/api/center-cut/jobs")
        reasonGuard = $admJs.Contains("requireReason")
        operationConsoleMenus = $admJs.Contains("id: `"batch`"") -or $admJs.Contains("id: ""batch""")
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
