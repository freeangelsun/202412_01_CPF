param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $BatBaseUrl = "http://localhost:8093",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 60,
    [string] $LogDir = "",
    [switch] $AllowLegacyFallback
)

$ErrorActionPreference = "Stop"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    batBaseUrl = $BatBaseUrl
    process = [ordered]@{}
    health = [ordered]@{}
    successJob = [ordered]@{}
    heartbeatJob = [ordered]@{}
    centerCutJob = [ordered]@{}
    failJob = [ordered]@{}
    cleanup = [ordered]@{}
}
$smokeSequence = 0

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$stdoutLog = Join-Path $LogDir "bat-boot.out.log"
$stderrLog = Join-Path $LogDir "bat-boot.err.log"
$resultPath = Join-Path $LogDir "bat-runtime-smoke-result.json"

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function Resolve-BatBootJar {
    $libsDir = Join-Path $Root "bat/build/libs"
    if (-not (Test-Path -LiteralPath $libsDir)) {
        return $null
    }
    $jar = Get-ChildItem -LiteralPath $libsDir -File -Filter "*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*plain*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jar) {
        return $null
    }
    return $jar.FullName
}

function Invoke-BatJson {
    param(
        [string] $Method,
        [string] $Uri,
        [int] $TimeoutSec = 15
    )

    $response = Invoke-WebRequest -Method $Method -Uri $Uri -TimeoutSec $TimeoutSec -Headers (New-SmokeHeaders) -UseBasicParsing
    if ($null -ne $response.RawContentStream) {
        $response.RawContentStream.Position = 0
        $reader = [System.IO.StreamReader]::new($response.RawContentStream, [System.Text.UTF8Encoding]::new($false), $true)
        try {
            return $reader.ReadToEnd() | ConvertFrom-Json
        } finally {
            $reader.Dispose()
        }
    }
    return $response.Content | ConvertFrom-Json
}

function New-SmokeHeaders {
    $script:smokeSequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $sequence = $script:smokeSequence.ToString("0000000")
    return @{
        "X-Transaction-Id" = "$timestamp" + "BAT" + "smoke01" + $sequence
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "BAT"
        "X-Channel-Code" = "BAT"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-bat-smoke"
    }
}

function Test-BatHealth {
    try {
        return Invoke-BatJson -Method Get -Uri "$BatBaseUrl/bat/api/health" -TimeoutSec 5
    } catch {
        return $null
    }
}

function Stop-ProcessTree {
    param([int] $ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId ([int] $child.ProcessId)
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
    if (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue) {
        & "$env:WINDIR\System32\taskkill.exe" /PID $ProcessId /T /F | Out-Null
    }
}

function Assert-BatchResult {
    param(
        [object] $Payload,
        [string] $ExpectedStatus,
        [string] $Name
    )

    if ($null -eq $Payload) {
        throw "$Name response is empty."
    }
    if ($Payload.status -ne $ExpectedStatus) {
        throw "$Name status mismatch. expected=$ExpectedStatus actual=$($Payload.status)"
    }
    if ($null -eq $Payload.pfwExecutionId -or $Payload.pfwExecutionId -lt 1) {
        throw "$Name pfwExecutionId was not created."
    }
    if ($null -eq $Payload.springBatchExecutionId -or $Payload.springBatchExecutionId -lt 1) {
        throw "$Name springBatchExecutionId was not created."
    }
    $execution = $Payload.detail.execution
    if ($null -eq $execution -or [string]::IsNullOrWhiteSpace($execution.transaction_global_id)) {
        throw "$Name transaction_global_id was not linked."
    }
    $steps = @($Payload.detail.steps)
    if ($steps.Count -lt 1 -or $null -eq $steps[0].spring_batch_step_execution_id) {
        throw "$Name spring_batch_step_execution_id was not linked."
    }
}

function Assert-HeartbeatBatchResult {
    param(
        [object] $Payload
    )

    Assert-BatchResult -Payload $Payload -ExpectedStatus "COMPLETED" -Name "heartbeatJob"

    $detail = $Payload.detail
    $execution = $detail.execution
    $extendedExecution = $detail.extendedExecution
    $steps = @($detail.steps)
    $extendedSteps = @($detail.extendedSteps)
    if ($null -eq $extendedExecution -or $null -eq $extendedExecution.processed_count) {
        if (-not $AllowLegacyFallback) {
            throw "heartbeatJob V10 extendedExecution.processed_count is missing. Apply V10 schema/runtime or rerun with -AllowLegacyFallback for legacy inspection."
        }
    }
    $processedCount = if ($null -ne $extendedExecution -and $null -ne $extendedExecution.processed_count) {
        [int64] $extendedExecution.processed_count
    } elseif ($AllowLegacyFallback -and $null -ne $execution -and $null -ne $execution.write_count) {
        [int64] $execution.write_count
    } else {
        0
    }
    if ($processedCount -lt 2) {
        throw "heartbeatJob processed_count was not updated at least twice. actual=$processedCount"
    }
    if (-not $AllowLegacyFallback) {
        if ($null -eq $extendedExecution.progress_rate) {
            throw "heartbeatJob V10 extendedExecution.progress_rate is missing."
        }
        if ($null -eq $extendedExecution.last_heartbeat_at) {
            throw "heartbeatJob V10 extendedExecution.last_heartbeat_at is missing."
        }
        if ($extendedSteps.Count -lt 1 -or $null -eq $extendedSteps[0].processed_count) {
            throw "heartbeatJob V10 extendedSteps processed_count evidence is missing."
        }
    }

    $stepLog = ""
    if ($steps.Count -gt 0 -and $null -ne $steps[0].step_log) {
        $stepLog = [string] $steps[0].step_log
    }
    if ($stepLog -notmatch "heartbeatCount=") {
        throw "heartbeatJob step_log does not contain heartbeatCount evidence."
    }
}

function Assert-CenterCutBatchResult {
    param(
        [object] $Payload
    )

    Assert-BatchResult -Payload $Payload -ExpectedStatus "COMPLETED" -Name "centerCutJob"

    $steps = @($Payload.detail.steps)
    if ($steps.Count -lt 1) {
        throw "centerCutJob step evidence is missing."
    }
    $stepLog = ""
    if ($null -ne $steps[0].step_log) {
        $stepLog = [string] $steps[0].step_log
    }
    if ($stepLog -notmatch "centerCutRequested=" -or $stepLog -notmatch "centerCutSuccess=") {
        throw "centerCutJob step_log does not contain center-cut summary evidence."
    }
}

$startedProcess = $null
$previousServerInstanceId = $env:SERVER_INSTANCE_ID

try {
    $bootJar = Resolve-BatBootJar
    if ($null -eq $bootJar) {
        throw "BAT bootJar was not found. Run .\gradlew.bat :bat:bootJar --offline first."
    }

    if (Test-Path -LiteralPath $stdoutLog) { Remove-Item -LiteralPath $stdoutLog -Force }
    if (Test-Path -LiteralPath $stderrLog) { Remove-Item -LiteralPath $stderrLog -Force }

    $env:SERVER_INSTANCE_ID = "bat-smoke-01"
    $startedProcess = Start-Process `
        -FilePath "java.exe" `
        -ArgumentList @(
            "-jar",
            $bootJar,
            "--spring.profiles.active=bat-local",
            "--server.port=8093",
            "--cpf.bat.smoke.run-on-startup=true",
            "--cpf.bat.smoke.run-failure-on-startup=true"
        ) `
        -WorkingDirectory $Root `
        -RedirectStandardOutput $stdoutLog `
        -RedirectStandardError $stderrLog `
        -WindowStyle Hidden `
        -PassThru

    $result.process.status = "STARTED"
    $result.process.pid = $startedProcess.Id
    $result.process.bootJar = $bootJar
    $result.process.stdoutLog = $stdoutLog
    $result.process.stderrLog = $stderrLog

    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    $health = $null
    while ((Get-Date) -lt $deadline) {
        $health = Test-BatHealth
        if ($null -ne $health -and $health.status -eq "UP") {
            $startupSuccess = $health.smoke.latestSuccess
            if ($null -ne $startupSuccess -and $startupSuccess.status -eq "COMPLETED") {
                break
            }
        }
        if ($startedProcess.HasExited) {
            throw "BAT application exited before health readiness. Check logs: $stdoutLog, $stderrLog"
        }
        Start-Sleep -Seconds 2
    }

    if ($null -eq $health -or $health.status -ne "UP") {
        throw "BAT health endpoint did not respond with UP before timeout. Check logs: $stdoutLog, $stderrLog"
    }
    $result.health.status = "PASSED"
    $result.health.response = $health

    $successJob = Invoke-BatJson -Method Post -Uri "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_SMOKE_JOB/run"
    Assert-BatchResult -Payload $successJob -ExpectedStatus "COMPLETED" -Name "successJob"
    $result.successJob.status = "PASSED"
    $result.successJob.response = $successJob

    $heartbeatJob = Invoke-BatJson -Method Post -Uri "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_HEARTBEAT_JOB/run" -TimeoutSec 90
    Assert-HeartbeatBatchResult -Payload $heartbeatJob
    $result.heartbeatJob.status = "PASSED"
    $result.heartbeatJob.response = $heartbeatJob

    $centerCutJob = Invoke-BatJson -Method Post -Uri "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_CENTER_CUT_JOB/run" -TimeoutSec 90
    Assert-CenterCutBatchResult -Payload $centerCutJob
    $result.centerCutJob.status = "PASSED"
    $result.centerCutJob.response = $centerCutJob

    $failJob = Invoke-BatJson -Method Post -Uri "$BatBaseUrl/bat/api/smoke/jobs/CPF_BAT_FAIL_JOB/run"
    Assert-BatchResult -Payload $failJob -ExpectedStatus "FAILED" -Name "failJob"
    $result.failJob.status = "PASSED"
    $result.failJob.response = $failJob
} catch {
    $result.error = $_.Exception.Message
    Save-SmokeResult
    throw
} finally {
    if ($null -ne $startedProcess) {
        Stop-ProcessTree -ProcessId $startedProcess.Id
        Wait-Process -Id $startedProcess.Id -Timeout $ShutdownTimeoutSeconds -ErrorAction SilentlyContinue
        if (-not (Get-Process -Id $startedProcess.Id -ErrorAction SilentlyContinue)) {
            $result.cleanup.status = "PASSED"
            $result.cleanup.message = "BAT process was stopped."
        } else {
            $result.cleanup.status = "WARNING"
            $result.cleanup.message = "BAT process may still be running."
        }
    }
    if ($null -eq $previousServerInstanceId) {
        Remove-Item Env:SERVER_INSTANCE_ID -ErrorAction SilentlyContinue
    } else {
        $env:SERVER_INSTANCE_ID = $previousServerInstanceId
    }
    Save-SmokeResult
}

Write-Host "BAT runtime smoke completed. Result: $resultPath"
