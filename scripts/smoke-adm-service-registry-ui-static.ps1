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
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "adm-service-registry-ui-static-smoke.sanitized.json"
$logPath = Join-Path $ResultDir "adm-service-registry-ui-static-smoke.log"

$jsPath = Join-Path $Root "cpf-admin/frontend/src/App.vue"
$htmlPath = Join-Path $Root "cpf-admin/frontend/src/App.vue"
$js = [System.IO.File]::ReadAllText($jsPath, [System.Text.Encoding]::UTF8)
$html = [System.IO.File]::ReadAllText($htmlPath, [System.Text.Encoding]::UTF8)

$checks = [ordered]@{
    menuId = $js.Contains("SERVICE_REGISTRY")
    state = $js.Contains("serviceRegistryResult")
    loader = $js.Contains("loadServiceRegistry")
    servicesApi = $js.Contains("/adm/api/service-registry/services")
    circuitApi = $js.Contains("/adm/api/service-registry/circuit-states")
    callHistoryApi = $js.Contains("/adm/api/service-registry/call-history")
    htmlPanel = $html.Contains("activeMenu === 'serviceRegistry'")
    htmlTitle = $html.Contains("Service Call Registry")
}
$failed = @($checks.GetEnumerator() | Where-Object { -not $_.Value })
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    validationMode = "adm-static-ui-contract"
    checks = $checks
    status = $(if ($failed.Count -eq 0) { "DONE" } else { "FAILED" })
    failed = @($failed | ForEach-Object { $_.Key })
    finishedAt = (Get-Date).ToString("o")
}

[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
[System.IO.File]::WriteAllText($logPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
if ($result.status -eq "FAILED") {
    throw "ADM service registry UI static smoke failed: $($result.failed -join ', ')"
}
Write-Host "ADM service registry UI static smoke passed. result=$resultPath"
