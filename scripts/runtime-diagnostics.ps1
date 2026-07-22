param(
    [Alias("Root")]
    [string] $DiagnosticsRoot = "",
    [Alias("Modules")]
    [string[]] $DiagnosticsModules = @("MBR", "ADM", "BZA", "REF", "BAT"),
    [Alias("ResultDir")]
    [string] $DiagnosticsResultDir = "",
    [Alias("ErrorMessage")]
    [string] $DiagnosticsErrorMessage = "",
    [switch] $NoExitOnFailure
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

function New-CpfRuntimeDiagnostic {
    param(
        [string] $Root,
        [string] $Module,
        [int[]] $Ports = @(),
        [string] $ErrorMessage = "",
        [string] $ResultDir = ""
    )

    $resolvedRoot = Get-CpfRuntimeRoot -Root $Root
    $resolvedResultDir = Get-CpfRuntimeResultDir -Root $resolvedRoot -ResultDir $ResultDir
    $moduleMap = Get-CpfRuntimeModuleMap
    $moduleInfo = $moduleMap | Where-Object { $_.module -eq $Module.ToUpperInvariant() } | Select-Object -First 1
    $portStates = @()
    foreach ($port in $Ports) {
        $portStates += [ordered]@{
            port = $port
            state = $(if (Test-CpfRuntimeTcpPort -Port $port) { "LISTENING" } else { "NOT_LISTENING" })
            owningProcess = $null
        }
    }

    $pidInfo = $null
    $logFiles = @()
    if ($moduleInfo -ne $null) {
        $pidInfo = Get-CpfRuntimePidInfo -ResultDir $resolvedResultDir -Module $moduleInfo
        $logFiles = Get-CpfRuntimeLogFiles -Root $resolvedRoot -Module $moduleInfo -IncludeTail
    }

    $portOpened = (@($portStates | Where-Object { $_.state -eq "LISTENING" }).Count -gt 0)
    $processStillAliveAfterProbe = ($pidInfo -ne $null -and [bool] $pidInfo.processAlive)
    $finalRuntimeUsable = ($portOpened -and $processStillAliveAfterProbe)
    $failureRootCause = ""
    if (-not $portOpened) {
        $failureRootCause = "runtime port is not listening."
    } elseif (-not $processStillAliveAfterProbe) {
        $failureRootCause = "runtime port is listening but the harness pid is not alive."
    }

    return [ordered]@{
        module = $Module
        classification = Get-CpfRuntimeFailureClassification -Message $ErrorMessage
        error = $ErrorMessage
        ports = $portStates
        portOpened = $portOpened
        pid = $(if ($pidInfo -ne $null) { $pidInfo.pid } else { $null })
        processAlive = $(if ($pidInfo -ne $null) { $pidInfo.processAlive } else { $false })
        processName = $(if ($pidInfo -ne $null) { $pidInfo.processName } else { $null })
        processStillAliveAfterProbe = $processStillAliveAfterProbe
        finalRuntimeUsable = $finalRuntimeUsable
        failureClassification = $(if ($finalRuntimeUsable) { $null } else { Get-CpfRuntimeFailureClassification -Message $ErrorMessage })
        failureRootCause = $failureRootCause
        processProbe = "pid-file-and-port-probe"
        logFiles = $logFiles
    }
}

function Invoke-CpfRuntimeDiagnostics {
    param(
        [string] $Root = "",
        [string[]] $Modules = @("MBR", "ADM", "BZA", "REF", "BAT"),
        [string] $ResultDir = "",
        [string] $ErrorMessage = ""
    )

    $resolvedRoot = Get-CpfRuntimeRoot -Root $Root
    $resolvedResultDir = Get-CpfRuntimeResultDir -Root $resolvedRoot -ResultDir $ResultDir
    New-Item -ItemType Directory -Force -Path $resolvedResultDir | Out-Null
    $selectedModules = Resolve-CpfRuntimeModules -Modules $Modules
    $diagnosticItems = New-Object System.Collections.ArrayList
    $result = [ordered]@{
        checkedAt = (Get-Date).ToString("o")
        status = Get-CpfRuntimeStatusText "Partial"
        diagnostics = @()
    }

    foreach ($module in $selectedModules) {
        [void] $diagnosticItems.Add((New-CpfRuntimeDiagnostic `
            -Root $resolvedRoot `
            -Module $module.module `
            -Ports @([int] $module.port) `
            -ErrorMessage $ErrorMessage `
            -ResultDir $resolvedResultDir))
    }

    $result.diagnostics = @($diagnosticItems)
    $notUsable = @($result.diagnostics | Where-Object { $_.finalRuntimeUsable -ne $true })
    $result.status = $(if ($notUsable.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    $result.finishedAt = (Get-Date).ToString("o")
    $resultPath = Join-Path $resolvedResultDir "runtime-diagnostics-result.json"
    Write-CpfRuntimeJson -Path $resultPath -Value $result
    return [ordered]@{
        result = $result
        resultPath = $resultPath
    }
}

$dotSourced = $MyInvocation.Line -match '^\s*\.'
if (-not $dotSourced) {
    $diagnostics = Invoke-CpfRuntimeDiagnostics -Root $DiagnosticsRoot -Modules $DiagnosticsModules -ResultDir $DiagnosticsResultDir -ErrorMessage $DiagnosticsErrorMessage
    Write-Host "runtime diagnostics finished. status=$($diagnostics.result.status) result=$($diagnostics.resultPath)"
    if (-not $NoExitOnFailure -and $diagnostics.result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
}
