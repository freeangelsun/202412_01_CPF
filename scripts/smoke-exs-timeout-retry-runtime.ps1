param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $ExsBaseUrl = "http://127.0.0.1:8092",
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")
. (Join-Path $PSScriptRoot "runtime-diagnostics.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "exs-timeout-retry-runtime-result.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    baseUrl = $ExsBaseUrl
    sourceMarkers = [ordered]@{
        endpointTimeoutRetry = $false
        retryLedger = $false
        timeoutFailureLog = $false
        cpfErrorMappingCandidate = $false
        adminLedgerQuery = $false
    }
}

function Save-ExsResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Get-ObjectPropertyValue {
    param(
        [object] $Object,
        [string] $Name
    )

    if ($null -eq $Object) {
        return $null
    }
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

try {
    $repositoryText = [System.IO.File]::ReadAllText((Join-Path $Root "exs/src/main/java/cpf/exs/operation/repository/ExsOperationRepository.java"), [System.Text.Encoding]::UTF8)
    $serviceText = [System.IO.File]::ReadAllText((Join-Path $Root "exs/src/main/java/cpf/exs/operation/service/ExsOperationService.java"), [System.Text.Encoding]::UTF8)
    $admUiText = [System.IO.File]::ReadAllText((Join-Path $Root "adm/src/main/resources/static/adm/adm.js"), [System.Text.Encoding]::UTF8)
    $result.sourceMarkers.endpointTimeoutRetry = $repositoryText.Contains("timeout_ms") -and $repositoryText.Contains("retry_count")
    $result.sourceMarkers.retryLedger = $repositoryText.Contains("exs_retry_log")
    $result.sourceMarkers.timeoutFailureLog = $serviceText.Contains("timeoutYn") -and $serviceText.Contains("EXS_TIMEOUT")
    $result.sourceMarkers.cpfErrorMappingCandidate = $serviceText.Contains("failureCode") -and $serviceText.Contains("responseCode")
    $result.sourceMarkers.adminLedgerQuery = $admUiText.Contains("/api/exs/operations/retries") -and $admUiText.Contains("/api/exs/transactions")

    $portListening = Test-CpfRuntimeTcpPort -Port 8092
    $result.portListening = $portListening
    if (-not $portListening) {
        $result.status = $(if ($RequireRuntime) { Get-CpfRuntimeStatusText "Failed" } else { Get-CpfRuntimeStatusText "NotVerified" })
        $result.failureClassification = "environment"
        $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "EXS" -Ports @(8092) -ErrorMessage "EXS runtime port is not listening." -ResultDir $ResultDir
        Save-ExsResult
        if ($RequireRuntime) {
            exit 1
        }
        return
    }

    $exsHeaders = New-CpfRuntimeTransactionHeaders -Module "EXS" -WasId "exsrt01" -ClientAppId "cpf-exs-timeout-smoke"
    $endpointList = Invoke-RestMethod -Uri "$ExsBaseUrl/api/exs/endpoints" -Method Get -Headers $exsHeaders -TimeoutSec 8
    $result.endpointListCount = @($endpointList).Count
    $result.endpointTimeoutRetryFields = @($endpointList | Select-Object -First 3 timeoutMs,retryCount,retryableYn)

    $failureBody = @{
        institutionCode = "BANK01"
        endpointCode = "BANK01_BALANCE"
        externalTransactionId = "EXT-SMOKE-TIMEOUT"
        failureCode = "EXS_TIMEOUT"
        failureMessage = "smoke timeout"
        retryCount = 2
        timeoutMs = 1500
    }
    try {
        Invoke-RestMethod -Uri "$ExsBaseUrl/api/exs/edu/external-transfer/failure" -Method Post -Headers (New-CpfRuntimeTransactionHeaders -Module "EXS" -WasId "exsfail" -ClientAppId "cpf-exs-timeout-smoke") -Body ($failureBody | ConvertTo-Json -Depth 10) -ContentType "application/json" -TimeoutSec 15 | Out-Null
        $result.failureCall = [ordered]@{ status = "UNEXPECTED_SUCCESS" }
    } catch {
        $statusCode = $null
        if ($_.Exception.Response -ne $null) {
            $statusCode = [int] $_.Exception.Response.StatusCode
        }
        $result.failureCall = [ordered]@{
            status = "EXPECTED_FAILURE"
            httpStatus = $statusCode
            error = $_.Exception.GetType().Name
        }
    }

    $transactionsResponse = Invoke-RestMethod -Uri "$ExsBaseUrl/api/exs/transactions?limit=20" -Method Get -Headers (New-CpfRuntimeTransactionHeaders -Module "EXS" -WasId "exslist" -ClientAppId "cpf-exs-timeout-smoke") -TimeoutSec 8
    $transactions = if ($null -ne $transactionsResponse.PSObject.Properties["items"]) { @($transactionsResponse.items) } else { @($transactionsResponse) }
    $result.transactionLedgerCount = @($transactions).Count
    $result.timeoutFailureFound = @($transactions | Where-Object {
            (Get-ObjectPropertyValue -Object $_ -Name "failureCode") -eq "EXS_TIMEOUT" `
                -or (Get-ObjectPropertyValue -Object $_ -Name "timeoutYn") -eq "Y" `
                -or [int] (Get-ObjectPropertyValue -Object $_ -Name "retryCount") -gt 0
        }).Count -gt 0
    $result.status = $(if ($result.timeoutFailureFound) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    if (-not $result.timeoutFailureFound) {
        $result.failureClassification = "implementation"
        $result.error = "EXS timeout/retry ledger did not expose failure or retry evidence."
    }
    Save-ExsResult
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.failureClassification = Get-CpfRuntimeFailureClassification -Message $_.Exception.Message
    $result.error = $_.Exception.Message
    $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "EXS" -Ports @(8092) -ErrorMessage $_.Exception.Message -ResultDir $ResultDir
    Save-ExsResult
    throw
}

Write-Host "EXS timeout/retry runtime smoke finished. status=$($result.status) result=$resultPath"
