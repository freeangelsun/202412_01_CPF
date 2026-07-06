param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string[]] $Modules = @(),
    [string] $ResultDir = "",
    [int] $StopTimeoutSeconds = 20
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "runtime-stop-services-result.json"
$state = Read-CpfRuntimeState -ResultDir $ResultDir
$requestedModules = @(Resolve-CpfRuntimeModules -Modules $Modules)
$requested = @($requestedModules | ForEach-Object { $_.module })
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @()
    rule = "stop only PIDs recorded by this harness; do not kill arbitrary port owners"
}

try {
    $startedItems = @()
    if ($state -ne $null -and $state.started -ne $null) {
        $startedItems = @($state.started)
    }

    foreach ($item in $startedItems) {
        $moduleName = [string] $item.module
        if ($requested.Count -gt 0 -and $requested -notcontains $moduleName) {
            continue
        }
        $moduleResult = [ordered]@{
            module = $moduleName
            pid = $item.pid
            status = Get-CpfRuntimeStatusText "NotVerified"
        }
        $process = Get-Process -Id ([int] $item.pid) -ErrorAction SilentlyContinue
        if ($null -eq $process) {
            $moduleResult.status = Get-CpfRuntimeStatusText "Done"
            $moduleResult.reason = "recorded PID is already stopped"
            $result.modules += $moduleResult
            continue
        }

        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        $deadline = (Get-Date).AddSeconds($StopTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            if ($null -eq (Get-Process -Id $process.Id -ErrorAction SilentlyContinue)) {
                $moduleResult.status = Get-CpfRuntimeStatusText "Done"
                break
            }
            Start-Sleep -Milliseconds 500
        }
        if ($moduleResult.status -ne (Get-CpfRuntimeStatusText "Done")) {
            $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
            $moduleResult.error = "process stop timeout exceeded"
        }
        $result.modules += $moduleResult
    }

    foreach ($module in $requestedModules) {
        $pidPath = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".pid")
        if (Test-Path -LiteralPath $pidPath) {
            Remove-Item -LiteralPath $pidPath -Force -ErrorAction SilentlyContinue
        }
    }

    $failed = @($result.modules | Where-Object { $_.status -ne (Get-CpfRuntimeStatusText "Done") })
    $result.status = $(if ($failed.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
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

Write-Host "runtime stop finished. status=$($result.status) result=$resultPath"
