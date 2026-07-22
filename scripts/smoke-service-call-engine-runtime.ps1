param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $RunRuntime,
    [switch] $RequireRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "service-call-engine-runtime-success.sanitized.json"

function Read-Text {
    param([string] $Path)
    $fullPath = Join-Path $Root $Path
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "required file missing: $Path"
    }
    return [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
}

function Test-Contains {
    param([string] $Text, [string] $Needle, [string] $Name)
    if ($Text.IndexOf($Needle, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        throw "required marker missing: $Name"
    }
}

function New-RuntimeHeaders {
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    return @{
        "X-Transaction-Id" = "$timestamp" + "CPFgwsmk01" + "0000001"
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Cpf-Standard-Execution-Id" = "OACCQY0001"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-App-Id" = "cpf-service-call-smoke"
        "X-Client-Version" = "1.0.0"
        "X-User-Id" = "runtime-smoke"
        "X-Operator-Id" = "runtime-smoke"
        "X-Audit-Reason" = "CPF service-call runtime verification"
        "Authorization" = "Bearer runtime-smoke-placeholder"
    }
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    runRuntimeRequested = [bool] $RunRuntime
    validationMode = "source-and-unit-contract"
    checks = @()
}

try {
    $engineText = Read-Text "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceCallEngine.java"
    $webClientText = Read-Text "cpf-core/src/main/java/com/cpf/core/common/http/CpfWebClient.java"
    $repositoryText = Read-Text "cpf-core/src/main/java/com/cpf/core/common/servicecall/CpfServiceRegistryRepository.java"
    $testText = Read-Text "cpf-core/src/test/java/com/cpf/core/common/servicecall/CpfServiceCallEngineTest.java"

    Test-Contains $engineText "Function<ServiceCallResolvedTarget, T>" "target-aware runtime adapter"
    Test-Contains $engineText "isCircuitOpen" "circuit open check"
    Test-Contains $engineText "markFailure" "failure state transition"
    Test-Contains $engineText "excludedInstanceIds" "failover excluded instance tracking"
    Test-Contains $webClientText "invokeThroughEngineOrFallback" "CpfWebClient service-call engine path"
    Test-Contains $webClientText "webClient(target)" "selected target HTTP adapter"
    Test-Contains $repositoryText "insertCallHistory" "service call history persistence"
    Test-Contains $repositoryText "recordCircuitFailure" "circuit persistence"
    Test-Contains $testText "invokeRetriesAndFailoversToNextInstance" "retry failover unit test"
    Test-Contains $testText "invokeBlocksRemoteCallWhenCircuitIsOpen" "circuit unit test"

    $result.checks += "service-call-engine-source-contract"
    $result.status = "DONE"
    if ($RunRuntime) {
        try {
            $response = Invoke-WebRequest `
                -Method Post `
                -Uri "http://127.0.0.1:8070/cpf/execute/OACCQY0001" `
                -Headers (New-RuntimeHeaders) `
                -TimeoutSec 15 `
                -UseBasicParsing
            $routeId = [string] $response.Headers["X-Cpf-Gateway-Route-Id"]
            $instanceId = [string] $response.Headers["X-Cpf-Gateway-Instance-Id"]
            $body = if ([string]::IsNullOrWhiteSpace($response.Content)) {
                $null
            } else {
                $response.Content | ConvertFrom-Json
            }
            $result.runtime = [ordered]@{
                statusCode = [int] $response.StatusCode
                standardExecutionId = "OACCQY0001"
                routeHeader = $routeId
                selectedInstanceHeaderPresent = -not [string]::IsNullOrWhiteSpace($instanceId)
                responsePresent = $null -ne $body
            }
            if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 300 `
                    -or $routeId -ne "OACCQY0001" `
                    -or [string]::IsNullOrWhiteSpace($instanceId) `
                    -or $null -eq $body) {
                throw "Gateway to ACC service-call runtime verification failed."
            }
            $result.validationMode = "source-unit-and-multi-service-runtime"
            $result.checks += "gateway-to-acc-runtime"
        } catch {
            $result.runtime = [ordered]@{
                status = "NOT_VERIFIED"
                error = $_.Exception.Message
            }
            if ($RequireRuntime) {
                throw
            }
            $result.status = "PARTIAL"
        }
    }
} catch {
    $result.status = "FAILED"
    $result.error = $_.Exception.Message
}

$result.finishedAt = (Get-Date).ToString("o")
[System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)

if ($result.status -eq "FAILED") {
    throw $result.error
}

Write-Host "service call engine runtime source smoke passed. result=$resultPath"
