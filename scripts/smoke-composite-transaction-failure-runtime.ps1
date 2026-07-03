param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AccBaseUrl = "http://localhost:8080",
    [string] $ResultDir = "",
    [int] $MemberId = 1,
    [int] $TimeoutSec = 20,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "composite-transaction-failure-runtime-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "NOT_VERIFIED"
    accBaseUrl = $AccBaseUrl
    probes = [ordered]@{}
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function New-CpfSmokeHeaders {
    $timestamp = (Get-Date).ToString("yyyyMMddHHmmssfff")
    return @{
        "X-Transaction-Id" = "$timestamp" + "ACC" + "failrun" + "0000001"
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "SMOKE"
        "X-Channel-Code" = "ACC"
        "X-Client-App-Id" = "cpf-composite-failure-smoke"
        "X-Client-Version" = "1.0.0"
        "X-Caller-Service" = "runtime-smoke"
        "X-User-Id" = "runtime-smoke-user"
        "X-Operator-Id" = "runtime-smoke-operator"
        "X-Customer-No" = "CUST-SMOKE-001"
        "X-Member-No" = "MBR-SMOKE-001"
    }
}

function Invoke-Json {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null
    )

    $invokeParams = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        TimeoutSec = $TimeoutSec
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    $response = Invoke-WebRequest @invokeParams
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

function Assert-Condition {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) {
        throw $Message
    }
}

try {
    $uri = "$AccBaseUrl/acc/edu/composite/member-then-external-failure?memberId=$MemberId"
    $body = Invoke-Json -Method Post -Uri $uri -Headers (New-CpfSmokeHeaders)
    $result.probes.response = [ordered]@{
        transactionGlobalId = $body.transactionGlobalId
        segmentCount = $body.segmentCount
        overallStatus = $body.overallStatus
        failedSegmentId = $body.failedSegmentId
        failureCode = $body.failureCode
        failedModuleCode = $body.failedModuleCode
    }

    Assert-Condition (-not [string]::IsNullOrWhiteSpace($body.transactionGlobalId)) "transactionGlobalId is empty"
    Assert-Condition ($body.overallStatus -eq "FAILED") "overallStatus must be FAILED"
    Assert-Condition ($body.segmentCount -ge 3) "failure composite segmentCount must be at least 3"
    Assert-Condition (-not [string]::IsNullOrWhiteSpace($body.failedSegmentId)) "failedSegmentId is empty"
    Assert-Condition ($body.failureCode -eq "EXS_TIMEOUT") "failureCode must be EXS_TIMEOUT"
    Assert-Condition ($body.failedModuleCode -eq "EXS") "failedModuleCode must be EXS"
    Assert-Condition (-not [string]::IsNullOrWhiteSpace($body.failureMessageMasked)) "failureMessageMasked is empty"

    $serialized = $body | ConvertTo-Json -Depth 30
    Assert-Condition ($serialized -notmatch "Bearer\s+[A-Za-z0-9._~+/=-]+") "sensitive bearer token must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)password\s*[:=]\s*[^,*}\]]+") "password-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)api[-_]?key\s*[:=]\s*[^,*}\]]+") "api key-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)secret\s*[:=]\s*[^,*}\]]+") "secret-like raw value must not be exposed"

    $result.transactionGlobalId = $body.transactionGlobalId
    $result.status = "PASSED"
    Save-SmokeResult
    Write-Host "Composite transaction failure runtime smoke passed. Result: $resultPath"
} catch {
    $result.status = "FAILED"
    $result.error = $_.Exception.Message
    Save-SmokeResult
    if ($RequireRuntime) {
        throw
    }
    Write-Host "Composite transaction failure runtime smoke failed. Result: $resultPath"
}
