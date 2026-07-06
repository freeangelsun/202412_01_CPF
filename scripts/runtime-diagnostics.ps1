param(
    [Alias("Root")]
    [string] $DiagnosticsRoot = "",
    [Alias("Modules")]
    [string[]] $DiagnosticsModules = @("ACC", "MBR", "EXS", "ADM", "BAT"),
    [Alias("ResultDir")]
    [string] $DiagnosticsResultDir = "",
    [Alias("ErrorMessage")]
    [string] $DiagnosticsErrorMessage = "",
    [switch] $NoExitOnFailure
)

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

    return [ordered]@{
        module = $Module
        classification = Get-CpfRuntimeFailureClassification -Message $ErrorMessage
        error = $ErrorMessage
        ports = $portStates
        pid = $(if ($pidInfo -ne $null) { $pidInfo.pid } else { $null })
        processAlive = $(if ($pidInfo -ne $null) { $pidInfo.processAlive } else { $false })
        processName = $(if ($pidInfo -ne $null) { $pidInfo.processName } else { $null })
        processProbe = "pid-file-and-port-probe"
        logFiles = $logFiles
    }
}

function Invoke-CpfRuntimeDiagnostics {
    param(
        [string] $Root = "",
        [string[]] $Modules = @("ACC", "MBR", "EXS", "ADM", "BAT"),
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
    $notListening = @($result.diagnostics | Where-Object {
            $ports = @($_.ports)
            $ports.Count -gt 0 -and @($ports | Where-Object { $_.state -eq "LISTENING" }).Count -eq 0
        })
    $result.status = $(if ($notListening.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
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
