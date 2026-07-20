param(
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $AccessToken = "",
    [string] $ResultPath = ""
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$sequence = 0
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    login = [ordered]@{}
    scan = [ordered]@{}
    list = [ordered]@{}
    detail = [ordered]@{}
}

if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $root = (Resolve-Path "$PSScriptRoot\..").Path
    $dir = Join-Path $root "build/runtime-smoke"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $ResultPath = Join-Path $dir "transaction-meta-runtime-smoke-result.json"
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [IO.File]::WriteAllText($ResultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
}

function New-Headers {
    param([hashtable] $Extra = @{})
    $script:sequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $headers = @{
        "X-Transaction-Id" = "$timestamp" + "ADM" + "trnmeta" + $script:sequence.ToString("0000000")
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
    }
    foreach ($key in $Extra.Keys) {
        $headers[$key] = $Extra[$key]
    }
    return $headers
}

function ConvertFrom-Utf8JsonResponse {
    param([object] $Response)
    $content = $Response.Content
    if ($null -ne $Response.RawContentStream) {
        $stream = $Response.RawContentStream
        if ($stream.CanSeek) {
            $stream.Position = 0
        }
        $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true, 1024, $true)
        try {
            $content = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    }
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }
    return $content | ConvertFrom-Json
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
        TimeoutSec = 20
        Headers = (New-Headers -Extra $Headers)
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    ConvertFrom-Utf8JsonResponse (Invoke-WebRequest @invokeParams)
}

function Get-Value {
    param(
        [object] $Object,
        [string[]] $Names
    )
    foreach ($name in $Names) {
        if ($null -ne $Object.$name) {
            return $Object.$name
        }
    }
    return $null
}

try {
    if ([string]::IsNullOrWhiteSpace($AccessToken)) {
        if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
            throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
        }
        $login = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
            operatorId = $AdmUsername
            password = $AdmPassword
        }
        if ([string]::IsNullOrWhiteSpace($login.accessToken)) {
            throw "ADM login response does not contain accessToken."
        }
        $AccessToken = $login.accessToken
        $result.login.status = "PASSED"
    } else {
        $result.login.status = "REUSED_TOKEN"
    }

    $headers = @{ Authorization = "Bearer $AccessToken" }
    $scan = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/transactions/scan?reason=runtime-smoke&requestUser=smoke" -Headers $headers
    $result.scan.status = "PASSED"
    $result.scan.response = $scan

    $list = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transactions?activeYn=Y&limit=20" -Headers $headers
    $items = @($list.items)
    if ($items.Count -lt 1) {
        throw "Transaction meta list is empty after scan."
    }
    $first = $items | Select-Object -First 1
    $transactionId = Get-Value -Object $first -Names @("transaction_id", "transactionId", "TRANSACTION_ID")
    if ([string]::IsNullOrWhiteSpace($transactionId)) {
        throw "Transaction meta list does not contain a transaction id."
    }
    $result.list.status = "PASSED"
    $result.list.count = $items.Count
    $result.list.firstTransactionId = $transactionId

    $detail = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transactions/$transactionId" -Headers $headers
    $result.detail.status = "PASSED"
    $result.detail.transactionId = $transactionId
    $result.detail.response = $detail
    Save-Result
} catch {
    $result.error = $_.Exception.Message
    Save-Result
    throw
}

Write-Host "Transaction meta runtime smoke completed. Result: $ResultPath"
