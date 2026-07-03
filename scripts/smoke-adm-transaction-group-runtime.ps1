param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AccBaseUrl = "http://localhost:8080",
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $TransactionGlobalId = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $ResultDir = "",
    [int] $TimeoutSec = 20
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "adm-transaction-group-runtime-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    accBaseUrl = $AccBaseUrl
    admBaseUrl = $AdmBaseUrl
    transactionGlobalId = $TransactionGlobalId
    probes = [ordered]@{}
}

$script:sequence = 1

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
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

function New-CpfSmokeHeaders {
    $timestamp = (Get-Date).ToString("yyyyMMddHHmmssfff")
    $sequence = $script:sequence.ToString("0000000")
    $script:sequence++
    $transactionId = "$timestamp" + "ADM" + "admgrp1" + $sequence
    return @{
        "X-Transaction-Id" = $transactionId
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "SMOKE"
        "X-Channel-Code" = "ACC"
        "X-Client-App-Id" = "cpf-adm-transaction-group-smoke"
        "X-User-Id" = "runtime-smoke"
    }
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
        $AdmPassword = "Adm!n12345"
    }

    if ([string]::IsNullOrWhiteSpace($TransactionGlobalId)) {
        $composite = Invoke-Json -Method Post -Uri "$AccBaseUrl/acc/edu/composite/member-then-external?memberId=1" -Headers (New-CpfSmokeHeaders)
        $TransactionGlobalId = $composite.transactionGlobalId
        $result.transactionGlobalId = $TransactionGlobalId
        $result.probes.generatedComposite = [ordered]@{
            transactionGlobalId = $TransactionGlobalId
            segmentCount = $composite.segmentCount
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

    $list = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups?transactionGlobalId=$TransactionGlobalId&includedModuleCode=MBR&status=SUCCESS&failureYn=N&sort=startedAtDesc&limit=10" -Headers $headers
    $detail = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$TransactionGlobalId" -Headers $headers
    $segments = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$TransactionGlobalId/segments" -Headers $headers
    $timeline = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$TransactionGlobalId/timeline" -Headers $headers
    $headersResult = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$TransactionGlobalId/headers" -Headers $headers
    $externalLogs = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transaction-groups/$TransactionGlobalId/external-logs" -Headers $headers

    $segmentItems = @($segments.items)
    $timelineItems = @($timeline.items)
    $externalLogItems = @($externalLogs.items)
    $segmentIds = @($segmentItems | ForEach-Object { Get-PropertyValue $_ @("transaction_segment_id", "transactionSegmentId") } | Where-Object { -not [string]::IsNullOrWhiteSpace([string] $_) })
    $uniqueSegmentIds = @($segmentIds | Sort-Object -Unique)
    $moduleCodes = @($segmentItems | ForEach-Object { Get-PropertyValue $_ @("module_code", "moduleCode") } | Where-Object { -not [string]::IsNullOrWhiteSpace([string] $_) } | Sort-Object -Unique)

    $result.probes.list = [ordered]@{ count = @($list.items).Count; sort = $list.sort }
    $result.probes.detail = [ordered]@{ segmentCount = @($detail.segments).Count; summary = $detail.summary }
    $result.probes.segments = [ordered]@{
        count = $segmentItems.Count
        uniqueSegmentIdCount = $uniqueSegmentIds.Count
        moduleCodes = $moduleCodes
    }
    $result.probes.timeline = [ordered]@{ count = $timelineItems.Count }
    $result.probes.headers = [ordered]@{ count = @($headersResult.headers).Count }
    $result.probes.externalLogs = [ordered]@{ count = $externalLogItems.Count }
    $result.probes.dbSegmentRows = $segmentItems.Count

    Assert-Condition (@($list.items).Count -ge 1) "ADM transaction group list must contain the generated transactionGlobalId"
    Assert-Condition (@($detail.segments).Count -ge 3) "ADM detail must contain at least 3 segments"
    Assert-Condition ($segmentItems.Count -ge 3) "ADM segment API must contain at least 3 segments"
    Assert-Condition ($timelineItems.Count -ge 3) "ADM timeline must contain at least 3 items"
    Assert-Condition ($segmentIds.Count -eq $uniqueSegmentIds.Count) "ADM segment API must not expose duplicated transactionSegmentId"
    Assert-Condition ($moduleCodes -contains "ACC") "ADM segment API must contain ACC segment"
    Assert-Condition ($moduleCodes -contains "MBR") "ADM segment API must contain MBR segment"
    Assert-Condition ($moduleCodes -contains "EXS") "ADM segment API must contain EXS segment"
    Assert-Condition ($externalLogItems.Count -ge 1) "ADM external logs API must contain at least one external candidate row"
    Assert-Condition (($detail.summary.moduleFlowText -match "ACC") -and ($detail.summary.moduleFlowText -match "MBR") -and ($detail.summary.moduleFlowText -match "EXS")) "ADM summary moduleFlowText must contain ACC, MBR, EXS"
    Assert-Condition ($detail.summary.totalDurationMs -ge 0) "ADM summary totalDurationMs must exist"

    $serialized = (@($detail, $headersResult, $externalLogs) | ConvertTo-Json -Depth 30)
    Assert-Condition ($serialized -notmatch "Bearer\s+[A-Za-z0-9._~+/=-]+") "sensitive bearer token must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)password\s*[:=]\s*[^,*}\]]+") "password-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)api[-_]?key\s*[:=]\s*[^,*}\]]+") "api key-like raw value must not be exposed"
    Assert-Condition ($serialized -notmatch "(?i)secret\s*[:=]\s*[^,*}\]]+") "secret-like raw value must not be exposed"

    $result.status = $StatusDone
    Save-SmokeResult
    Write-Host "ADM transaction group runtime smoke passed. Result: $resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    Save-SmokeResult
    throw
}
