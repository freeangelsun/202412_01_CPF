param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $DatabaseUsername = $env:CPF_RUNTIME_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_RUNTIME_DB_PASSWORD,
    [int] $StartupTimeoutSeconds = 180,
    [int] $BatchWaitSeconds = 60,
    [switch] $BuildBeforeRun
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$runtimeDir = Join-Path $Root "build/runtime-smoke/gateway-bat-e2e"
$resultPath = Join-Path $ResultDir "gateway-bat-runtime-result.sanitized.json"
$utf8NoBom = [Text.UTF8Encoding]::new($false)
$statusDone = Get-CpfRuntimeStatusText "Done"
$statusFailed = Get-CpfRuntimeStatusText "Failed"
$statusNotVerified = Get-CpfRuntimeStatusText "NotVerified"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $statusNotVerified
    databaseCredentialsProvided = -not [string]::IsNullOrWhiteSpace($DatabasePassword)
    services = [ordered]@{}
    gatewayToAcc = [ordered]@{}
    batchOnDemand = [ordered]@{}
    cleanup = [ordered]@{}
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $utf8NoBom)
}

function New-ScenarioHeaders {
    param(
        [string] $ExecutionId,
        [string] $Module = "BAT",
        [string] $WasId = "smoke01"
    )

    $headers = New-CpfRuntimeTransactionHeaders -Module $Module -WasId $WasId -RequestType "SMOKE"
    $headers["X-Cpf-Standard-Execution-Id"] = $ExecutionId
    $headers["X-Original-Channel-Code"] = "ADM"
    $headers["X-Channel-Code"] = "ADM"
    $headers["X-Operator-Id"] = "runtime-smoke"
    $headers["X-Audit-Reason"] = "CPF Gateway and BAT runtime integration verification"
    $headers["Authorization"] = "Bearer runtime-smoke-placeholder"
    return $headers
}

function ConvertFrom-JsonContent {
    param([object] $Response)

    if ($null -eq $Response -or [string]::IsNullOrWhiteSpace($Response.Content)) {
        return $null
    }
    return $Response.Content | ConvertFrom-Json
}

function Invoke-JsonStatus {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers,
        [object] $Body = $null
    )

    $parameters = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        TimeoutSec = 30
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json;charset=UTF-8"
        $parameters.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    try {
        $response = Invoke-WebRequest @parameters
        return [ordered]@{
            statusCode = [int] $response.StatusCode
            headers = $response.Headers
            body = ConvertFrom-JsonContent $response
        }
    } catch {
        $statusCode = 0
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int] $_.Exception.Response.StatusCode
        }
        return [ordered]@{
            statusCode = $statusCode
            headers = @{}
            body = $null
            errorType = $_.Exception.GetType().Name
            error = $_.Exception.Message
        }
    }
}

function Set-ProcessEnvironment {
    param([hashtable] $Values)

    $previous = @{}
    foreach ($name in $Values.Keys) {
        $previous[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
        [Environment]::SetEnvironmentVariable($name, $Values[$name], "Process")
    }
    return $previous
}

function Restore-ProcessEnvironment {
    param([hashtable] $Previous)

    foreach ($name in $Previous.Keys) {
        [Environment]::SetEnvironmentVariable($name, $Previous[$name], "Process")
    }
}

$environment = @{
    "DEBUG" = "false"
    "CPF_DB_URL" = "jdbc:mariadb://localhost:3306/cpfDB"
    "CPF_DB_USERNAME" = $DatabaseUsername
    "CPF_DB_PASSWORD" = $DatabasePassword
    "GWY_DATASOURCE_URL" = "jdbc:mariadb://localhost:3306/cpfDB"
    "GWY_DATASOURCE_USERNAME" = $DatabaseUsername
    "GWY_DATASOURCE_PASSWORD" = $DatabasePassword
    "ACC_DATASOURCE_URL" = "jdbc:mariadb://localhost:3306/accDB"
    "ACC_DATASOURCE_USERNAME" = $DatabaseUsername
    "ACC_DATASOURCE_PASSWORD" = $DatabasePassword
}
$previousEnvironment = $null

try {
    if ([string]::IsNullOrWhiteSpace($DatabaseUsername) -or [string]::IsNullOrWhiteSpace($DatabasePassword)) {
        throw "CPF_RUNTIME_DB_USERNAME and CPF_RUNTIME_DB_PASSWORD are required."
    }

    $runtimeFullPath = [IO.Path]::GetFullPath($runtimeDir)
    $allowedRuntimeRoot = [IO.Path]::GetFullPath((Join-Path $Root "build/runtime-smoke"))
    if (-not $runtimeFullPath.StartsWith($allowedRuntimeRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Runtime work directory is outside the allowed build path. path=$runtimeFullPath"
    }
    if (Test-Path -LiteralPath $runtimeFullPath) {
        Remove-Item -LiteralPath $runtimeFullPath -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $runtimeFullPath | Out-Null

    $previousEnvironment = Set-ProcessEnvironment -Values $environment
    & (Join-Path $Root "scripts/runtime-start-services.ps1") `
        -Root $Root `
        -Modules @("ACC", "GWY", "BAT") `
        -ResultDir $runtimeFullPath `
        -StartupTimeoutSeconds $StartupTimeoutSeconds `
        -BuildBeforeRun:$BuildBeforeRun `
        -NoExitOnFailure

    $startResult = Get-Content -LiteralPath (Join-Path $runtimeFullPath "runtime-start-services-result.json") -Raw -Encoding UTF8 | ConvertFrom-Json
    $result.services.status = $startResult.status
    $result.services.modules = @($startResult.modules | ForEach-Object {
            [ordered]@{
                module = $_.module
                status = $_.status
                port = $_.port
                healthCheckPassed = $_.healthCheckPassed
                processStillAliveAfterProbe = $_.processStillAliveAfterProbe
            }
        })
    $unusable = @($startResult.modules | Where-Object { $_.finalRuntimeUsable -ne $true })
    if ($unusable.Count -gt 0) {
        throw "One or more ACC, Gateway, or BAT runtimes failed to start."
    }

    $gatewayHeaders = New-ScenarioHeaders -ExecutionId "OACCQY0001" -Module "CPF" -WasId "gwsmk01"
    $gatewayResponse = Invoke-JsonStatus `
        -Method Post `
        -Uri "http://localhost:8070/cpf/execute/OACCQY0001" `
        -Headers $gatewayHeaders
    $routeHeader = if ($gatewayResponse.headers) { $gatewayResponse.headers["X-Cpf-Gateway-Route-Id"] } else { $null }
    $gatewayInstanceHeader = if ($gatewayResponse.headers) { $gatewayResponse.headers["X-Cpf-Gateway-Instance-Id"] } else { $null }
    $gatewayItems = @($gatewayResponse.body)
    $result.gatewayToAcc = [ordered]@{
        statusCode = $gatewayResponse.statusCode
        standardExecutionId = "OACCQY0001"
        routeHeader = [string] $routeHeader
        gatewayInstanceHeaderPresent = -not [string]::IsNullOrWhiteSpace([string] $gatewayInstanceHeader)
        responseItemCount = $gatewayItems.Count
    }
    if ($gatewayResponse.statusCode -lt 200 -or $gatewayResponse.statusCode -ge 300 `
            -or [string] $routeHeader -ne "OACCQY0001" `
            -or -not $result.gatewayToAcc.gatewayInstanceHeaderPresent) {
        throw "Gateway to ACC proxy verification failed. status=$($gatewayResponse.statusCode)"
    }

    $stamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $submitResponse = Invoke-JsonStatus `
        -Method Post `
        -Uri "http://localhost:8093/bat/api/v1/edu/on-demand" `
        -Headers (New-ScenarioHeaders -ExecutionId "OBATOD0001") `
        -Body @{
            standardBatchId = "BBATOD0001"
            businessDate = Get-Date -Format "yyyyMMdd"
            idempotencyKey = "runtime-smoke-$stamp"
            reason = "BAT on-demand JobRepository runtime verification"
            requestUser = "runtime-smoke"
            parameters = @{ verification = "gateway-bat-runtime" }
        }
    if ($submitResponse.statusCode -ne 202 -or [string]::IsNullOrWhiteSpace($submitResponse.body.executionRequestId)) {
        throw "BAT on-demand submission failed. status=$($submitResponse.statusCode)"
    }
    $executionRequestId = [string] $submitResponse.body.executionRequestId

    $terminal = $null
    $deadline = (Get-Date).AddSeconds($BatchWaitSeconds)
    do {
        Start-Sleep -Milliseconds 500
        $statusResponse = Invoke-JsonStatus `
            -Method Get `
            -Uri "http://localhost:8093/bat/api/v1/edu/on-demand/$executionRequestId" `
            -Headers (New-ScenarioHeaders -ExecutionId "OBATOD0002")
        if ($statusResponse.statusCode -eq 200 `
                -and $statusResponse.body.requestStatus -notin @("REQUESTED", "RUNNING")) {
            $terminal = $statusResponse.body
            break
        }
    } while ((Get-Date) -lt $deadline)
    if ($null -eq $terminal) {
        throw "BAT on-demand execution did not finish before the timeout."
    }

    $stepsResponse = Invoke-JsonStatus `
        -Method Get `
        -Uri "http://localhost:8093/bat/api/v1/edu/on-demand/$executionRequestId/steps" `
        -Headers (New-ScenarioHeaders -ExecutionId "OBATOD0003")
    $stepCount = @($stepsResponse.body).Count
    if ($terminal.requestStatus -ne "COMPLETED" -or $null -eq $terminal.springBatchExecutionId `
            -or $stepsResponse.statusCode -ne 200 -or $stepCount -lt 1) {
        throw "BAT Spring Batch completion or step verification failed. status=$($terminal.requestStatus)"
    }

    $restartResponse = Invoke-JsonStatus `
        -Method Post `
        -Uri "http://localhost:8093/bat/api/v1/edu/on-demand/$executionRequestId/restart" `
        -Headers (New-ScenarioHeaders -ExecutionId "OBATOD0005")
    $restartPolicyVerified = $restartResponse.statusCode -eq 202 `
        -or ($restartResponse.statusCode -ge 400 -and $restartResponse.statusCode -lt 500)
    if (-not $restartPolicyVerified) {
        throw "BAT restart policy verification failed. status=$($restartResponse.statusCode)"
    }

    $rerunResponse = Invoke-JsonStatus `
        -Method Post `
        -Uri "http://localhost:8093/bat/api/v1/edu/on-demand/$executionRequestId/rerun" `
        -Headers (New-ScenarioHeaders -ExecutionId "OBATOD0006")
    if ($rerunResponse.statusCode -ne 202) {
        throw "BAT rerun verification failed. status=$($rerunResponse.statusCode)"
    }

    $result.batchOnDemand = [ordered]@{
        submitStatusCode = $submitResponse.statusCode
        executionRequestId = $executionRequestId
        terminalStatus = $terminal.requestStatus
        cpfExecutionIdPresent = $null -ne $terminal.cpfExecutionId
        springBatchExecutionIdPresent = $null -ne $terminal.springBatchExecutionId
        stepCount = $stepCount
        restartStatusCode = $restartResponse.statusCode
        restartPolicyVerified = $restartPolicyVerified
        rerunStatusCode = $rerunResponse.statusCode
        rerunStatus = $rerunResponse.body.requestStatus
    }
    $result.status = $statusDone
} catch {
    $result.status = $statusFailed
    $result.errorType = $_.Exception.GetType().Name
    $result.error = $_.Exception.Message
    throw
} finally {
    try {
        if (Test-Path -LiteralPath (Join-Path $runtimeDir "runtime-services.json")) {
            & (Join-Path $Root "scripts/runtime-stop-services.ps1") `
                -Root $Root `
                -Modules @("ACC", "GWY", "BAT") `
                -ResultDir $runtimeDir
            $stopResult = Get-Content -LiteralPath (Join-Path $runtimeDir "runtime-stop-services-result.json") -Raw -Encoding UTF8 | ConvertFrom-Json
            $result.cleanup.status = $stopResult.status
            $result.cleanup.modules = @($stopResult.modules | Select-Object module, status)
        } else {
            $result.cleanup.status = $statusNotVerified
            $result.cleanup.reason = "Runtime state file was not created."
        }
    } catch {
        $result.cleanup.status = $statusFailed
        $result.cleanup.error = $_.Exception.Message
    }
    if ($null -ne $previousEnvironment) {
        Restore-ProcessEnvironment -Previous $previousEnvironment
    }
    Save-Result
}

Write-Host "Gateway and BAT runtime smoke completed: $resultPath"
