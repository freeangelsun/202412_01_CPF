param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
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

function Read-CpfLiveLogText {
    param([string] $Path)
    # 이 검증기는 Runtime 이 살아 있는 동안 그 Runtime 이 지금도 쓰고 있는 File Log 를 읽는다.
    # Windows 에서 File Log Owner(CpfFileLogWriter)는 rolling 파일 핸들을 연 채 유지하는데
    # [IO.File]::ReadAllText/ReadAllLines/ReadLines 는 FileShare.Read 로만 열기 때문에
    # 쓰기 핸들이 살아 있으면 "다른 프로세스가 사용 중" 으로 던진다.
    # (같은 결함으로 Batch Two-Worker 검증이 업무 단정을 모두 통과한 뒤 이 지점에서만 실패했다.)
    # 파일 부재/권한 오류는 그대로 예외로 남긴다 — '잠김' 만 허용하고 증적 부재는 숨기지 않는다.
    $stream = $null; $reader = $null
    try {
        $stream = [IO.FileStream]::new($Path, [IO.FileMode]::Open, [IO.FileAccess]::Read,
            ([IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete))
        $reader = [IO.StreamReader]::new($stream, [Text.UTF8Encoding]::new($false), $true)
        return $reader.ReadToEnd()
    } finally {
        if ($null -ne $reader) { $reader.Dispose() } elseif ($null -ne $stream) { $stream.Dispose() }
    }
}
$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) { $ResultDir = Join-Path $Root "build/runtime-smoke" }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
. (Join-Path $Root "cpf-tools/runtime/tools/runtime-diagnostics.ps1")
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
        pwsh -NoProfile -File (Join-Path $Root "cpf-tools/runtime/tools/smoke-bat-runtime.ps1")
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
        $content = Read-CpfLiveLogText -Path $batLog
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
