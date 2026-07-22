param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $RuntimeDir = "",
    [string] $ResultPath = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusNeedsReview = New-UnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)

if ([string]::IsNullOrWhiteSpace($RuntimeDir)) { $RuntimeDir = Join-Path $Root "build/runtime-smoke" }
if ([string]::IsNullOrWhiteSpace($ResultPath)) { $ResultPath = Join-Path $RuntimeDir "runtime-smoke-summary.json" }
New-Item -ItemType Directory -Force -Path $RuntimeDir | Out-Null

function Read-Status {
    param([string] $FileName)

    $path = Join-Path $RuntimeDir $FileName
    if (-not (Test-Path -LiteralPath $path)) {
        return [ordered]@{
            file = $FileName
            status = $StatusNotVerified
            exitCode = 1
            reason = "result file missing"
        }
    }

    try {
        $json = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        $status = [string] $json.status
        $ok = $status -eq $StatusDone -or $status -eq "PASSED"
        return [ordered]@{
            file = $FileName
            status = $status
            exitCode = $(if ($ok) { 0 } else { 1 })
        }
    } catch {
        return [ordered]@{
            file = $FileName
            status = $StatusNeedsReview
            exitCode = 1
            reason = $_.Exception.Message
        }
    }
}

$items = [ordered]@{
    standardHeader = Read-Status "standard-header-e2e-result.json"
    compositeSuccess = Read-Status "composite-transaction-runtime-result.json"
    compositeFailure = Read-Status "composite-transaction-failure-runtime-result.json"
    admGroupSuccess = Read-Status "adm-transaction-group-runtime-result.json"
    admGroupFailure = Read-Status "adm-transaction-group-failure-runtime-result.json"
    openapi = Read-Status "openapi-runtime-result.json"
    fileLogStandard = Read-Status "file-log-standard-result.json"
    traceBoost = Read-Status "trace-boost-runtime-result.json"
    batTraceBoost = Read-Status "bat-trace-boost-runtime-result.json"
    createDomain = Read-Status "create-domain-result.json"
}

$summary = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    items = $items
    exitCode = $(if (@($items.Values | Where-Object { $_.exitCode -ne 0 }).Count -eq 0) { 0 } else { 1 })
}

[System.IO.File]::WriteAllText($ResultPath, ($summary | ConvertTo-Json -Depth 30), $Utf8NoBom)
Write-Host "runtime smoke summary synced. result=$ResultPath exitCode=$($summary.exitCode)"

if ($summary.exitCode -ne 0) {
    exit 1
}
