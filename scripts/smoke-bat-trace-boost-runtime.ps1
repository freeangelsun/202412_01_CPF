param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $RunBatRuntime,
    [switch] $RequireRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
function New-UnicodeText { param([int[]] $CodePoints) return -join ($CodePoints | ForEach-Object { [char] $_ }) }
$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) { $ResultDir = Join-Path $Root "build/runtime-smoke" }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
. (Join-Path $Root "scripts/runtime-diagnostics.ps1")
$resultPath = Join-Path $ResultDir "bat-trace-boost-runtime-result.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    batRuntime = [ordered]@{ status = $StatusNotVerified }
    fileLog = [ordered]@{ status = $StatusNotVerified }
    traceBoost = [ordered]@{
        status = $StatusPartial
        basis = "CPF_BATCH_RUNTIME_LISTENER_POLICY_CONTEXT_AND_FILE_LOG"
    }
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

try {
    if ($RunBatRuntime) {
        powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root "scripts/smoke-bat-runtime.ps1")
        $result.batRuntime.status = $StatusDone
    } elseif (Test-Path -LiteralPath (Join-Path $ResultDir "bat-runtime-smoke-result.json")) {
        $result.batRuntime.status = $StatusDone
        $result.batRuntime.source = "build/runtime-smoke/bat-runtime-smoke-result.json"
    }

    $logRoot = if ([string]::IsNullOrWhiteSpace($env:CPF_LOG_ROOT)) {
        Join-Path $Root "logs"
    } else {
        [System.IO.Path]::GetFullPath($env:CPF_LOG_ROOT)
    }
    $environmentCode = if ([string]::IsNullOrWhiteSpace($env:CPF_ENV)) { "local" } else { $env:CPF_ENV.Trim().ToLowerInvariant() }
    $batLogRoot = Join-Path $logRoot ("{0}/bat/jobs" -f $environmentCode)
    $batLog = Get-ChildItem -LiteralPath $batLogRoot -Recurse -File -Filter "cpf-bat-*.log" `
            -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1 -ExpandProperty FullName
    $result.fileLog.root = $batLogRoot.Substring($Root.Length).TrimStart('\', '/')
    $result.fileLog.path = $(if ($batLog) { $batLog.Substring($Root.Length).TrimStart('\', '/') } else { $null })
    if ($batLog -and (Test-Path -LiteralPath $batLog)) {
        $content = [System.IO.File]::ReadAllText($batLog, [System.Text.Encoding]::UTF8)
        $result.fileLog.containsJobName = $content.Contains("jobName")
        $result.fileLog.containsJobExecutionId = $content.Contains("jobExecutionId")
        $result.fileLog.containsTraceBoostPolicyId = $content.Contains("traceBoostPolicyId")
        $result.fileLog.status = $(if ($result.fileLog.containsJobName -and $result.fileLog.containsJobExecutionId -and $result.fileLog.containsTraceBoostPolicyId) { $StatusDone } else { $StatusPartial })
    }

    $runtimeVerified = $result.batRuntime.status -eq $StatusDone
    $result.status = $(if ($result.fileLog.status -eq $StatusDone) { $StatusDone } elseif ($RequireRuntime) { $StatusFailed } elseif (-not $runtimeVerified) { $StatusNotVerified } else { $StatusPartial })
    Save-Result
    if ($result.status -eq $StatusFailed) { throw "BAT trace boost runtime smoke failed. result=$resultPath" }
    Write-Host "BAT trace boost smoke finished. status=$($result.status) result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "BAT" -Ports @(8093) -ErrorMessage $_.Exception.Message
    Save-Result
    throw
}
