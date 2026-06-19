param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

$indexPath = Join-Path $Root "adm/src/main/resources/static/adm/index.html"
$scriptPath = Join-Path $Root "adm/src/main/resources/static/adm/adm.js"
$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path -LiteralPath $indexPath)) {
    throw "ADM index.html not found: $indexPath"
}
if (-not (Test-Path -LiteralPath $scriptPath)) {
    throw "ADM adm.js not found: $scriptPath"
}

$index = [System.IO.File]::ReadAllText($indexPath, [System.Text.Encoding]::UTF8)
$script = [System.IO.File]::ReadAllText($scriptPath, [System.Text.Encoding]::UTF8)
$combined = "$index`n$script"

# 운영자가 반드시 접근해야 하는 주요 메뉴, 버튼, API 경로를 정적 smoke 기준으로 확인합니다.
$requiredMarkers = @(
    "batch",
    "logs",
    "auditLogs",
    "notifications",
    "downloads",
    "downloadLogDetail",
    "bizadm",
    "exs",
    "/api/bizadm",
    "/api/exs",
    "/adm/api/batch",
    "/adm/api/logs",
    "/adm/api/notifications",
    "/adm/api/downloads"
)

foreach ($marker in $requiredMarkers) {
    if ($combined.IndexOf($marker, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        $failures.Add("ADM UI 필수 요소 누락: $marker")
    }
}

$legacyPattern = ("F" + "PS") + "|" + ("F" + "ps") + "|" + ("f" + "ps")
if ($script -match $legacyPattern) {
    $failures.Add("ADM UI 스크립트에 과거 프로젝트명 잔재가 있습니다.")
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "ADM UI smoke check passed."
