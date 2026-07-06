param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $BatBaseUrl = "http://127.0.0.1:8093",
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")
. (Join-Path $PSScriptRoot "runtime-diagnostics.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "bat-log-bean-runtime-result.json"
$batchLogPath = Join-Path $Root "logs/bat/cpf-bat-batch.log"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    baseUrl = $BatBaseUrl
    diagnosticApi = "/bat/api/diagnostics/logging"
    smokeJobApi = "/bat/api/smoke/jobs/CPF_BAT_SMOKE_JOB/run"
    batchLogPath = Get-CpfRelativePath -Root $Root -Path $batchLogPath
}

function Save-BatBeanResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
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

    $diagnosticUri = "$BatBaseUrl/bat/api/diagnostics/logging"
    $diagnostic = Invoke-RestMethod -Uri $diagnosticUri -Method Get -TimeoutSec 8
    $result.loggingDiagnostics = $diagnostic

    $runUri = "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_SMOKE_JOB/run"
    $runResult = Invoke-RestMethod -Uri $runUri -Method Post -TimeoutSec 30
    $result.smokeJobRun = $runResult

    Start-Sleep -Milliseconds 700
    $logExists = Test-Path -LiteralPath $batchLogPath
    $result.batchLogExists = $logExists
    $result.batchLogBytes = $(if ($logExists) { (Get-Item -LiteralPath $batchLogPath).Length } else { 0 })
    $result.batchLogTail = Get-CpfRuntimeTail -Path $batchLogPath -LineCount 40

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
