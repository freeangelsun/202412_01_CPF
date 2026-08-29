param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = "",
    [string] $DatabaseProfilePath = "",
    [string] $DbVendor = $env:CPF_DB_VENDOR,
    [string] $DbResourceRoot = $env:CPF_DB_RESOURCE_ROOT,
    [int] $StartupTimeoutSeconds = 180,
    [switch] $BuildBeforeRun
)

# 이 파일명은 기존 자동화 호환을 위해 유지합니다. 검증 대상은 고정 업무 Domain이 아니라
# Platform Gateway OpenAPI와 BAT Control Server readiness 계약입니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
$runtimeDir = Join-Path $ResultDir "gateway-bat-services"
$resultPath = Join-Path $ResultDir "gateway-bat-runtime-result.sanitized.json"
New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null

$statusDone = Get-CpfRuntimeStatusText "Done"
$statusFailed = Get-CpfRuntimeStatusText "Failed"
$statusNotVerified = Get-CpfRuntimeStatusText "NotVerified"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $statusNotVerified
    contract = "platform-gateway-batch-readiness-v2"
    fixedBusinessDomainRequired = $false
    modules = @("GWY", "BAT")
    services = [ordered]@{}
    gatewayOpenApi = [ordered]@{ status = $statusNotVerified }
    batchReadiness = [ordered]@{ status = $statusNotVerified }
    cleanup = [ordered]@{ status = $statusNotVerified }
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

try {
    $startArgs = @{
        Root = $Root
        Modules = @("GWY", "BAT")
        ResultDir = $runtimeDir
        StartupTimeoutSeconds = $StartupTimeoutSeconds
        NoExitOnFailure = $true
        BuildBeforeRun = [bool] $BuildBeforeRun
    }
    if (-not [string]::IsNullOrWhiteSpace($DatabaseProfilePath)) {
        $startArgs.DatabaseProfilePath = $DatabaseProfilePath
    }
    if (-not [string]::IsNullOrWhiteSpace($DbVendor)) {
        $startArgs.DbVendor = $DbVendor
    }
    if (-not [string]::IsNullOrWhiteSpace($DbResourceRoot)) {
        $startArgs.DbResourceRoot = $DbResourceRoot
    }

    & (Join-Path $PSScriptRoot "runtime-start-services.ps1") @startArgs
    $startResultPath = Join-Path $runtimeDir "runtime-start-services-result.json"
    if (-not (Test-Path -LiteralPath $startResultPath -PathType Leaf)) {
        throw "Runtime start 결과가 생성되지 않았습니다: $startResultPath"
    }
    $startResult = Get-Content -LiteralPath $startResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $result.services = [ordered]@{
        status = [string] $startResult.status
        modules = @($startResult.modules | ForEach-Object {
                [ordered]@{
                    module = [string] $_.module
                    status = [string] $_.status
                    port = [int] $_.port
                    healthCheckPassed = [bool] $_.healthCheckPassed
                    processStillAliveAfterProbe = [bool] $_.processStillAliveAfterProbe
                    finalRuntimeUsable = [bool] $_.finalRuntimeUsable
                    failureClassification = [string] $_.failureClassification
                    failureRootCause = [string] $_.failureRootCause
                    bootJarBuildStatus = [string] $_.bootJarBuildStatus
                    stdout = [string] $_.stdout
                    stderr = [string] $_.stderr
                    stdoutTail = [string] $_.stdoutTail
                    stderrTail = [string] $_.stderrTail
                }
            })
    }
    $unusable = @($startResult.modules | Where-Object { $_.finalRuntimeUsable -ne $true })
    if ($unusable.Count -gt 0) {
        $failureDetails = @($unusable | ForEach-Object {
                $moduleName = [string] $_.module
                $classification = [string] $_.failureClassification
                $rootCause = [string] $_.failureRootCause
                $stderrTail = ([string] $_.stderrTail -replace "`r`n", ' ' -replace "`n", ' ').Trim()
                if ($stderrTail.Length -gt 600) { $stderrTail = $stderrTail.Substring($stderrTail.Length - 600) }
                "$moduleName classification=$classification rootCause=$rootCause stderrTail=$stderrTail"
            })
        throw ("Gateway/BAT runtime readiness failed: " + ($failureDetails -join ' || '))
    }

    $batchResult = $startResult.modules | Where-Object { $_.module -eq "BAT" } | Select-Object -First 1
    if ($null -eq $batchResult -or -not [bool] $batchResult.healthCheckPassed) {
        throw "BAT Control Server readiness 검증 결과가 없습니다."
    }
    $result.batchReadiness = [ordered]@{
        status = $statusDone
        port = [int] $batchResult.port
        health = $batchResult.health
    }

    & (Join-Path $PSScriptRoot "smoke-openapi.ps1") `
        -Root $Root `
        -Modules @("GWY") `
        -ResultDir $runtimeDir `
        -RequireRuntime
    if ($LASTEXITCODE -ne 0) {
        throw "Gateway OpenAPI runtime 검증이 실패했습니다."
    }
    $openApiResultPath = Join-Path $runtimeDir "openapi-runtime-result.sanitized.json"
    $openApiResult = Get-Content -LiteralPath $openApiResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $result.gatewayOpenApi = [ordered]@{
        status = $(if ([string] $openApiResult.status -eq "PASSED") { $statusDone } else { $statusFailed })
        result = Get-CpfRelativePath -Root $Root -Path $openApiResultPath
    }
    if ($result.gatewayOpenApi.status -ne $statusDone) {
        throw "Gateway OpenAPI contract가 통과하지 못했습니다."
    }

    $result.status = $statusDone
} catch {
    $result.status = $statusFailed
    $result.errorType = $_.Exception.GetType().Name
    $result.error = $_.Exception.Message
    throw
} finally {
    try {
        & (Join-Path $PSScriptRoot "runtime-stop-services.ps1") `
            -Root $Root `
            -Modules @("GWY", "BAT") `
            -ResultDir $runtimeDir
        $stopResultPath = Join-Path $runtimeDir "runtime-stop-services-result.json"
        $stopResult = Get-Content -LiteralPath $stopResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
        $result.cleanup = [ordered]@{
            status = [string] $stopResult.status
            modules = @($stopResult.modules | Select-Object module, status)
        }
    } catch {
        $result.cleanup = [ordered]@{
            status = $statusFailed
            error = $_.Exception.Message
        }
        if ($result.status -eq $statusDone) {
            $result.status = $statusFailed
        }
    }
    Save-Result
}

if ($result.status -ne $statusDone) {
    throw "Gateway/BAT platform runtime smoke가 완료 상태가 아닙니다. result=$resultPath"
}
Write-Host "Gateway/BAT platform runtime smoke completed. status=$($result.status) result=$resultPath"
