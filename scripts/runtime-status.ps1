param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string[]] $Modules = @("MBR", "ADM", "BZA", "REF"),
    [string] $ResultDir = "",
    [int] $HttpTimeoutSeconds = 10,
    [switch] $NoExitOnFailure
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

$resultPath = Join-Path $ResultDir "runtime-status-result.json"
$selectedModules = Resolve-CpfRuntimeModules -Modules $Modules
$result = [ordered]@{
    checkedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @()
}

try {
    foreach ($module in $selectedModules) {
        $pidInfo = Get-CpfRuntimePidInfo -ResultDir $ResultDir -Module $module
        $listening = Test-CpfRuntimeTcpPort -Port $module.port
        $httpProbe = $null
        if ($listening) {
            $httpProbe = Invoke-CpfRuntimeHttpProbe -Port $module.port -Path $module.healthPath -TimeoutSeconds $HttpTimeoutSeconds
        }
        $healthCheckPassed = ($httpProbe -ne $null -and [bool] $httpProbe.success)
        $processStillAliveAfterProbe = [bool] $pidInfo.processAlive
        $finalRuntimeUsable = ($listening -and $processStillAliveAfterProbe -and $healthCheckPassed)
        $moduleStatus = if ($finalRuntimeUsable) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" }
        $failureRootCause = ""
        if (-not $listening) {
            $failureRootCause = "runtime port is not listening."
        } elseif (-not $processStillAliveAfterProbe) {
            $failureRootCause = "runtime port is listening but the harness pid is not alive."
        } elseif (-not $healthCheckPassed) {
            $failureRootCause = "runtime port is listening but health check failed."
        }
        $result.modules += [ordered]@{
            module = $module.module
            port = $module.port
            status = $moduleStatus
            listening = $listening
            portOpened = $listening
            pid = $pidInfo.pid
            processAlive = $pidInfo.processAlive
            processName = $pidInfo.processName
            processStillAliveAfterProbe = $processStillAliveAfterProbe
            health = $httpProbe
            healthCheckPassed = $healthCheckPassed
            finalRuntimeUsable = $finalRuntimeUsable
            failureClassification = $(if ($finalRuntimeUsable) { $null } else { "environment" })
            failureRootCause = $failureRootCause
            logFiles = Get-CpfRuntimeLogFiles -Root $Root -Module $module
        }
    }

    $failed = @($result.modules | Where-Object { $_.status -ne (Get-CpfRuntimeStatusText "Done") })
    $result.status = $(if ($failed.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    if (-not $NoExitOnFailure -and $result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.error = $_.Exception.Message
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    throw
}

Write-Host "runtime status finished. status=$($result.status) result=$resultPath"
