param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AccBaseUrl = "http://localhost:8080",
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $TransactionGlobalId = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $ResultDir = "",
    [int] $TimeoutSec = 20,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "adm-transaction-group-failure-runtime-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "NOT_VERIFIED"
    accBaseUrl = $AccBaseUrl
    admBaseUrl = $AdmBaseUrl
    transactionGlobalId = $TransactionGlobalId
    probes = [ordered]@{}
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 40 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function New-CpfSmokeHeaders {
    $timestamp = (Get-Date).ToString("yyyyMMddHHmmssfff")
    return @{
        "X-Transaction-Id" = "$timestamp" + "ADM" + "failgrp" + "0000001"
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "SMOKE"
        "X-Channel-Code" = "ADM"
        "X-Client-App-Id" = "cpf-adm-failure-group-smoke"
        "X-Client-Version" = "1.0.0"
        "X-Caller-Service" = "runtime-smoke"
        "X-User-Id" = "runtime-smoke-user"
        "X-Operator-Id" = "runtime-smoke-operator"
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

function Get-PropertyValue {
    param([object] $Object, [string[]] $Names)
    foreach ($name in $Names) {
        if ($null -ne $Object -and $null -ne $Object.PSObject.Properties[$name]) {
            return $Object.PSObject.Properties[$name].Value
        }
    }
    return $null
}

try {
    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
    }

    if ([string]::IsNullOrWhiteSpace($TransactionGlobalId)) {
        $composite = Invoke-Json -Method Post -Uri "$AccBaseUrl/acc/edu/composite/member-then-external-failure?memberId=1" -Headers (New-CpfSmokeHeaders)
        $TransactionGlobalId = $composite.transactionGlobalId
        $result.transactionGlobalId = $TransactionGlobalId
        $result.probes.generatedFailureComposite = [ordered]@{
            transactionGlobalId = $TransactionGlobalId
            segmentCount = $composite.segmentCount
            overallStatus = $composite.overallStatus
            failureCode = $composite.failureCode
            failedSegmentId = $composite.failedSegmentId
        }
    }

    $headers = New-CpfSmokeHeaders
    if (-not [string]::IsNullOrWhiteSpace($AdmPassword)) {
        $login = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
            operatorId = $AdmUsername
            password = $AdmPassword
        } -Headers (New-CpfSmokeHeaders)
        $headers.Authorization = "Bearer $($login.accessToken)"
    }

    $encodedId = [System.Uri]::EscapeDataString($TransactionGlobalId)
    $list = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups?transactionGlobalId=$encodedId&failureYn=Y&failureCode=EXS_TIMEOUT&moduleCode=EXS&sort=failedFirst&limit=10" -Headers $headers
    $detail = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$encodedId" -Headers $headers
    $segments = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$encodedId/segments" -Headers $headers
    $timeline = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$encodedId/timeline" -Headers $headers
    $externalLogs = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$encodedId/external-logs" -Headers $headers

    $segmentItems = @($segments.items)
    $timelineItems = @($timeline.items)
    $externalLogItems = @($externalLogs.items)
    $failedSegments = @($segmentItems | Where-Object { (Get-PropertyValue $_ @("failure_yn", "failureYn")) -eq "Y" })
    $failedTimeline = @($timelineItems | Where-Object { $_.status -eq "FAILED" -or $_.failureCode -eq "EXS_TIMEOUT" })
    $externalSources = @($externalLogItems | ForEach-Object { $_.source } | Where-Object { -not [string]::IsNullOrWhiteSpace([string] $_) } | Sort-Object -Unique)

    $result.probes.list = [ordered]@{ count = @($list.items).Count; sort = $list.sort }
    $result.probes.detail = [ordered]@{ summary = $detail.summary; segmentCount = @($detail.segments).Count }
    $result.probes.segments = [ordered]@{ count = $segmentItems.Count; failedCount = $failedSegments.Count }
    $result.probes.timeline = [ordered]@{ count = $timelineItems.Count; failedCount = $failedTimeline.Count }
    $result.probes.externalLogs = [ordered]@{ count = $externalLogItems.Count; sources = $externalSources; source = $externalLogs.source; fallbackUsed = $externalLogs.fallbackUsed }

    Assert-Condition (@($list.items).Count -ge 1) "ADM failure transaction group list must contain the generated transactionGlobalId"
    Assert-Condition ($detail.summary.overallStatus -eq "FAILED") "ADM detail summary overallStatus must be FAILED"
    Assert-Condition ($detail.summary.failureCode -eq "EXS_TIMEOUT") "ADM detail summary failureCode must be EXS_TIMEOUT"
    Assert-Condition (-not [string]::IsNullOrWhiteSpace([string] $detail.summary.failedSegmentId)) "ADM detail failedSegmentId is empty"
    Assert-Condition ($failedSegments.Count -ge 1) "ADM segments must include at least one failed segment"
    Assert-Condition ($failedTimeline.Count -ge 1) "ADM timeline must include at least one failed item"
    Assert-Condition ($externalLogItems.Count -ge 1) "ADM external logs must contain at least one item"
    Assert-Condition (($externalSources -contains "EXS_TRANSACTION_LOG") -or ($externalSources -contains "EXS_MESSAGE_LOG")) "ADM external logs must use EXS ledger rows before fallback"

    $serialized = (@($detail, $segments, $timeline, $externalLogs) | ConvertTo-Json -Depth 40)
    Assert-Condition ($serialized -notmatch "Bearer\s+[A-Za-z0-9._~+/=-]+") "sensitive bearer token must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)password\s*[:=]\s*[^,*}\]]+") "password-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)api[-_]?key\s*[:=]\s*[^,*}\]]+") "api key-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)secret\s*[:=]\s*[^,*}\]]+") "secret-like raw value must not be exposed"

    $result.status = "PASSED"
    Save-SmokeResult
    Write-Host "ADM transaction group failure runtime smoke passed. Result: $resultPath"
} catch {
    $result.status = "FAILED"
    $result.error = $_.Exception.Message
    Save-SmokeResult
    if ($RequireRuntime) {
        throw
    }
    Write-Host "ADM transaction group failure runtime smoke failed. Result: $resultPath"
}
