param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $BatBaseUrl = "http://127.0.0.1:8093",
    [switch] $RequireRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")
. (Join-Path $PSScriptRoot "runtime-diagnostics.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "bat-log-bean-runtime-result.json"
$logRoot = if ([string]::IsNullOrWhiteSpace($env:CPF_LOG_ROOT)) {
    Join-Path $Root "logs"
} else {
    [System.IO.Path]::GetFullPath($env:CPF_LOG_ROOT)
}
$environmentCode = if ([string]::IsNullOrWhiteSpace($env:CPF_ENV)) { "local" } else { $env:CPF_ENV.Trim().ToLowerInvariant() }
$batchLogRoot = Join-Path $logRoot ("{0}/bat/jobs" -f $environmentCode)
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    baseUrl = $BatBaseUrl
    diagnosticApi = "/bat/api/diagnostics/logging"
    smokeJobApi = "/bat/api/smoke/jobs/CPF_BAT_SMOKE_JOB/run"
    batchLogRoot = Get-CpfRelativePath -Root $Root -Path $batchLogRoot
}

function Save-BatBeanResult {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 12), $script:CpfRuntimeUtf8NoBom)
}

try {
    $batPortListening = Test-CpfRuntimeTcpPort -Port 8093
    $result.portListening = $batPortListening
    if (-not $batPortListening) {
        $result.status = $(if ($RequireRuntime) { Get-CpfRuntimeStatusText "Failed" } else { Get-CpfRuntimeStatusText "NotVerified" })
        $result.failureClassification = "environment"
        $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "BAT" -Ports @(8093) -ErrorMessage "BAT runtime port is not listening." -ResultDir $ResultDir
        Save-BatBeanResult
        if ($RequireRuntime) {
            exit 1
        }
        return
    }

    $batHeaders = New-CpfRuntimeTransactionHeaders -Module "BAT" -WasId "batbean" -ClientAppId "cpf-bat-log-smoke"

    $diagnosticUri = "$BatBaseUrl/bat/api/diagnostics/logging"
    $diagnostic = Invoke-RestMethod -Uri $diagnosticUri -Method Get -Headers $batHeaders -TimeoutSec 8
    $result.loggingDiagnostics = [ordered]@{
        cpfBatchFileLogWriterBean = [bool] $diagnostic.cpfBatchFileLogWriterBean
        cpfBatchRuntimeListenerBean = [bool] $diagnostic.cpfBatchRuntimeListenerBean
    }

    $runUri = "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_SMOKE_JOB/run"
    $runResult = Invoke-RestMethod -Uri $runUri -Method Post -Headers (New-CpfRuntimeTransactionHeaders -Module "BAT" -WasId "batrun0" -ClientAppId "cpf-bat-log-smoke") -TimeoutSec 30
    $result.smokeJobRun = [ordered]@{
        executed = [bool] $runResult.executed
        jobId = [string] $runResult.jobId
        cpfExecutionId = $runResult.cpfExecutionId
        springBatchExecutionId = $runResult.springBatchExecutionId
        status = [string] $runResult.status
    }

    Start-Sleep -Milliseconds 700
    $batchLogPath = Get-ChildItem -LiteralPath $batchLogRoot -Recurse -File `
            -Filter "cpf-bat-CPF_BAT_SMOKE_JOB-*.log" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1 -ExpandProperty FullName
    $logExists = -not [string]::IsNullOrWhiteSpace($batchLogPath) -and (Test-Path -LiteralPath $batchLogPath)
    $result.batchLogExists = $logExists
    $result.batchLogPath = $(if ($logExists) { Get-CpfRelativePath -Root $Root -Path $batchLogPath } else { $null })
    $result.batchLogBytes = $(if ($logExists) { (Get-Item -LiteralPath $batchLogPath).Length } else { 0 })
    if ($logExists) {
        $batchLogContent = [System.IO.File]::ReadAllText($batchLogPath, [System.Text.Encoding]::UTF8)
        $result.batchLogContainsJobName = $batchLogContent.Contains("jobName")
        $result.batchLogContainsJobExecutionId = $batchLogContent.Contains("jobExecutionId")
    } else {
        $result.batchLogContainsJobName = $false
        $result.batchLogContainsJobExecutionId = $false
    }

    $beanOk = [bool] $diagnostic.cpfBatchFileLogWriterBean -and [bool] $diagnostic.cpfBatchRuntimeListenerBean
    $logOk = $logExists -and $result.batchLogBytes -gt 0
    $result.status = $(if ($beanOk -and $logOk) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    if (-not $beanOk) {
        $result.failureClassification = "implementation"
        $result.error = "BAT file log writer/listener bean was not found at runtime."
    } elseif (-not $logOk) {
        $result.failureClassification = "implementation"
        $result.error = "batch file log was not created after BAT smoke Job execution."
    }
    Save-BatBeanResult
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.failureClassification = Get-CpfRuntimeFailureClassification -Message $_.Exception.Message
    $result.error = $_.Exception.Message
    $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "BAT" -Ports @(8093) -ErrorMessage $_.Exception.Message -ResultDir $ResultDir
    Save-BatBeanResult
    throw
}

Write-Host "BAT log bean runtime smoke finished. status=$($result.status) result=$resultPath"
