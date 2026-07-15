param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $LogDir = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$resultPath = Join-Path $LogDir "adm-center-cut-runtime-smoke-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    login = [ordered]@{}
    centerCutApi = [ordered]@{}
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function New-SmokeHeaders {
    $sequence = Get-Random -Minimum 1 -Maximum 9999999
    $transactionId = "{0:yyyyMMddHHmmssfff}ADMsmoke01{1:0000000}" -f (Get-Date), $sequence
    return @{
        "X-Request-Type" = "INQUIRY"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Transaction-Id" = $transactionId
        "X-Trace-Id" = $transactionId
        "X-User-Id" = "runtime-smoke"
        "X-Client-App-Id" = "cpf-smoke"
        "X-Client-Version" = "1.0.0"
        "X-Caller-Service" = "smoke-adm-center-cut-runtime"
    }
}

function Invoke-SmokeJson {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null
    )

    $mergedHeaders = New-SmokeHeaders
    foreach ($key in $Headers.Keys) {
        $mergedHeaders[$key] = $Headers[$key]
    }
    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $mergedHeaders
        TimeoutSec = 20
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params.ContentType = "application/json;charset=UTF-8"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    $response = Invoke-WebRequest @params
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

try {
    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
    }

    $login = Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    if ([string]::IsNullOrWhiteSpace($login.accessToken)) {
        throw "ADM login response does not contain accessToken."
    }
    $result.login.status = "PASSED"

    $headers = @{ Authorization = "Bearer $($login.accessToken)" }
    $jobId = "CPF_XYZ_CENTER_CUT_SAMPLE_JOB"
    $endpoints = @(
        "/adm/api/center-cut/jobs",
        "/adm/api/center-cut/jobs/$jobId",
        "/adm/api/center-cut/jobs/$jobId/parameters",
        "/adm/api/center-cut/jobs/$jobId/summary",
        "/adm/api/center-cut/jobs/$jobId/targets?limit=20",
        "/adm/api/center-cut/jobs/$jobId/results?limit=20"
    )
    $checkedEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $endpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedEndpoints.Add($endpoint)
    }

    $summary = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/$jobId/summary" -Headers $headers
    $targets = @(Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/$jobId/targets?limit=20" -Headers $headers)
    $results = @(Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/center-cut/jobs/$jobId/results?limit=20" -Headers $headers)
    $resultJson = $results | ConvertTo-Json -Depth 20
    if ($resultJson -match '"resultPayload"\s*:') {
        throw "Center-Cut resultPayload raw field must not be exposed."
    }

    $result.centerCutApi.status = "PASSED"
    $result.centerCutApi.checkedEndpoints = $checkedEndpoints
    $result.centerCutApi.summary = $summary
    $result.centerCutApi.targetCount = $targets.Count
    $result.centerCutApi.resultCount = $results.Count
    $result.centerCutApi.hasParentTransactionGlobalId = @($targets | Where-Object { $_.parentTransactionGlobalId }).Count -gt 0
    $result.centerCutApi.hasChildTransactionGlobalId = @($targets | Where-Object { $_.childTransactionGlobalId }).Count -gt 0
    $result.centerCutApi.hasFailureReason = @($targets | Where-Object { $_.lastErrorMessage }).Count -gt 0
    $result.centerCutApi.rawPayloadExposed = $false
    Save-Result
    Write-Host "ADM Center-Cut runtime smoke passed. Result: $resultPath"
} catch {
    $result.centerCutApi.status = "FAILED"
    $result.centerCutApi.error = $_.Exception.Message
    Save-Result
    throw
}
