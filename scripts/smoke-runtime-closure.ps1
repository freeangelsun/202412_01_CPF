param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string[]] $Modules = @("MBR", "ADM", "BZA", "REF"),
    [int] $StartupTimeoutSeconds = 150,
    [switch] $StartServices,
    [switch] $StopAfterRun
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

$resultPath = Join-Path $ResultDir "runtime-closure-result.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @((Resolve-CpfRuntimeModules -Modules $Modules) | ForEach-Object { $_.module })
    runtimeStart = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    runtimeStatus = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    runtimeDiagnostics = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    fileLogStandard = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    traceBoost = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    batTraceBoost = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    batLogBean = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    admOperationConsole = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    admLogPolicyUiStatic = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
    cmnFixedLengthAdvanced = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
}

function Save-ClosureResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Read-SmokeResult {
    param(
        [string] $Name,
        [string] $FileName
    )

    $path = Join-Path $ResultDir $FileName
    if (-not (Test-Path -LiteralPath $path)) {
        return [ordered]@{
            status = Get-CpfRuntimeStatusText "NotVerified"
            name = $Name
            resultPath = Get-CpfRelativePath -Root $Root -Path $path
            reason = "result file was not found"
        }
    }
    try {
        $json = Get-Content -LiteralPath $path -Encoding UTF8 -Raw | ConvertFrom-Json
        return [ordered]@{
            status = [string] $json.status
            name = $Name
            resultPath = Get-CpfRelativePath -Root $Root -Path $path
            finishedAt = Get-JsonProperty -Value $json -Name "finishedAt"
            error = Get-JsonProperty -Value $json -Name "error"
            failureClassification = Get-JsonProperty -Value $json -Name "failureClassification"
        }
    } catch {
        return [ordered]@{
            status = Get-CpfRuntimeStatusText "Failed"
            name = $Name
            resultPath = Get-CpfRelativePath -Root $Root -Path $path
            error = $_.Exception.Message
        }
    }
}

function Get-JsonProperty {
    param(
        [object] $Value,
        [string] $Name
    )

    if ($null -eq $Value) {
        return $null
    }
    $property = $Value.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

try {
    if ($StartServices) {
        & (Join-Path $PSScriptRoot "runtime-start-services.ps1") `
            -Root $Root `
            -Modules $Modules `
            -ResultDir $ResultDir `
            -StartupTimeoutSeconds $StartupTimeoutSeconds `
            -NoExitOnFailure
    }

    & (Join-Path $PSScriptRoot "runtime-status.ps1") -Root $Root -Modules $Modules -ResultDir $ResultDir -NoExitOnFailure
    & (Join-Path $PSScriptRoot "runtime-diagnostics.ps1") -Root $Root -Modules $Modules -ResultDir $ResultDir -NoExitOnFailure

    $result.runtimeStart = Read-SmokeResult -Name "runtime-start-services" -FileName "runtime-start-services-result.json"
    $result.runtimeStatus = Read-SmokeResult -Name "runtime-status" -FileName "runtime-status-result.json"
    $result.runtimeDiagnostics = Read-SmokeResult -Name "runtime-diagnostics" -FileName "runtime-diagnostics-result.json"
    $result.fileLogStandard = Read-SmokeResult -Name "file-log-standard" -FileName "file-log-standard-result.json"
    $result.traceBoost = Read-SmokeResult -Name "trace-boost" -FileName "trace-boost-runtime-result.json"
    $result.batTraceBoost = Read-SmokeResult -Name "bat-trace-boost" -FileName "bat-trace-boost-runtime-result.json"
    $result.batLogBean = Read-SmokeResult -Name "bat-log-bean" -FileName "bat-log-bean-runtime-result.json"
    $result.admOperationConsole = Read-SmokeResult -Name "adm-operation-console" -FileName "adm-operation-console-runtime-result.json"
    $result.admLogPolicyUiStatic = Read-SmokeResult -Name "adm-log-policy-ui-static" -FileName "adm-log-policy-ui-static-result.sanitized.json"
    $result.cmnFixedLengthAdvanced = Read-SmokeResult -Name "cmn-fixed-length-advanced" -FileName "cmn-fixed-length-advanced-result.json"

    $required = @(
        $result.runtimeStart,
        $result.runtimeStatus,
        $result.runtimeDiagnostics,
        $result.fileLogStandard,
        $result.traceBoost,
        $result.batTraceBoost,
        $result.batLogBean,
        $result.admOperationConsole,
        $result.admLogPolicyUiStatic,
        $result.cmnFixedLengthAdvanced
    )
    $bad = @($required | Where-Object { $_.status -eq (Get-CpfRuntimeStatusText "Failed") -or $_.status -eq (Get-CpfRuntimeStatusText "NotVerified") })
    $result.status = $(if ($bad.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    Save-ClosureResult
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.error = $_.Exception.Message
    Save-ClosureResult
    throw
} finally {
    if ($StopAfterRun) {
        & (Join-Path $PSScriptRoot "runtime-stop-services.ps1") -Root $Root -Modules $Modules -ResultDir $ResultDir
    }
}

Write-Host "runtime closure smoke finished. status=$($result.status) result=$resultPath"
