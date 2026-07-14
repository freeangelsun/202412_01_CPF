param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $ResultDir = "",
    [int] $TimeoutSec = 20,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
function New-UnicodeText { param([int[]] $CodePoints) return -join ($CodePoints | ForEach-Object { [char] $_ }) }
$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) { $ResultDir = Join-Path $Root "build/runtime-smoke" }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
. (Join-Path $Root "scripts/runtime-diagnostics.ps1")
$resultPath = Join-Path $ResultDir "trace-boost-runtime-result.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    login = [ordered]@{ status = $StatusNotVerified }
    createTraceBoost = [ordered]@{ status = $StatusNotVerified }
    runtimeState = [ordered]@{ status = $StatusNotVerified }
    history = [ordered]@{ status = $StatusNotVerified }
    fileLog = [ordered]@{ status = $StatusNotVerified }
    cleanup = [ordered]@{ status = $StatusNotVerified }
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function Invoke-Json {
    param([string] $Method, [string] $Uri, [hashtable] $Headers = @{}, [object] $Body = $null)
    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        TimeoutSec = $TimeoutSec
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $params.ContentType = "application/json;charset=UTF-8"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    $response = Invoke-WebRequest @params
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json
}

function Test-EndpointListening {
    param([string] $BaseUrl)

    try {
        $uri = [System.Uri] $BaseUrl
        $port = if ($uri.Port -gt 0) { $uri.Port } elseif ($uri.Scheme -eq "https") { 443 } else { 80 }
        $client = [System.Net.Sockets.TcpClient]::new()
        try {
            $task = $client.ConnectAsync("127.0.0.1", $port)
            return $task.Wait(700) -and $client.Connected
        } finally {
            $client.Dispose()
        }
    } catch {
        return $false
    }
}

function New-Headers {
    return @{
        "X-Transaction-Id" = "$(Get-Date -Format yyyyMMddHHmmssfff)" + "ADM" + "trb0001" + "0000001"
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-Version" = "1.0.0"
    }
}

try {
    if (-not (Test-EndpointListening -BaseUrl $AdmBaseUrl)) {
        throw "ADM runtime port is not listening. baseUrl=$AdmBaseUrl"
    }
    if ([string]::IsNullOrWhiteSpace($AdmPassword)) { $AdmPassword = "Adm!n12345" }
    $login = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Headers (New-Headers) -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    if ([string]::IsNullOrWhiteSpace($login.accessToken)) { throw "ADM login token was not returned." }
    $headers = New-Headers
    $headers.Authorization = "Bearer $($login.accessToken)"
    $result.login.status = $StatusDone

    $created = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/log-policies/trace-boost" -Headers $headers -Body @{
        businessTransactionId = "ADM01TRN0010"
        logLevel = "DEBUG"
        ttlSeconds = 600
        requestUser = "runtime-smoke"
        reason = "runtime-smoke-trace-boost"
    }
    $result.createTraceBoost.status = $StatusDone
    $result.createTraceBoost.response = $created
    $traceBoostPolicyId = [string] $created.traceBoostPolicyId

    $state = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/log-policies/runtime-state?limit=20" -Headers $headers
    $result.runtimeState.status = $(if (@($state.items).Count -gt 0) { $StatusDone } else { $StatusPartial })
    $result.runtimeState.count = @($state.items).Count

    Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/transactions?activeYn=Y&limit=1" -Headers $headers | Out-Null
    Start-Sleep -Seconds 2

    $history = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/log-policies/history?limit=20" -Headers $headers
    $result.history.status = $(if (@($history.items).Count -gt 0) { $StatusDone } else { $StatusPartial })
    $result.history.count = @($history.items).Count

    $admLogRoot = Join-Path $Root "logs/local/adm/adm-local-01"
    $admLog = Get-ChildItem -LiteralPath $admLogRoot -Recurse -File -Filter "*.log" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTimeUtc -Descending |
        Where-Object { [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8).Contains($traceBoostPolicyId) } |
        Select-Object -First 1 -ExpandProperty FullName
    $result.fileLog.path = $(if ($admLog) { $admLog.Substring($Root.Length).TrimStart('\', '/') } else { $null })
    if ($admLog -and (Test-Path -LiteralPath $admLog)) {
        $content = [System.IO.File]::ReadAllText($admLog, [System.Text.Encoding]::UTF8)
        $result.fileLog.containsTraceBoostPolicyId = -not [string]::IsNullOrWhiteSpace($traceBoostPolicyId) -and $content.Contains($traceBoostPolicyId)
        $result.fileLog.status = $(if ($result.fileLog.containsTraceBoostPolicyId) { $StatusDone } else { $StatusPartial })
    }

    if (-not [string]::IsNullOrWhiteSpace($traceBoostPolicyId)) {
        Invoke-Json -Method Patch -Uri "$AdmBaseUrl/adm/api/log-policies/overrides/$traceBoostPolicyId/disable?reason=runtime-smoke-cleanup" -Headers $headers | Out-Null
        $result.cleanup.status = $StatusDone
        $result.cleanup.overrideId = $traceBoostPolicyId
    }

    $result.status = $(if ($result.createTraceBoost.status -eq $StatusDone -and $result.runtimeState.status -ne $StatusNotVerified -and $result.fileLog.status -ne $StatusNotVerified) { $StatusDone } else { $StatusPartial })
    Save-Result
    Write-Host "Trace Boost smoke finished. status=$($result.status) result=$resultPath"
} catch {
    $result.status = $(if ($RequireRuntime) { $StatusFailed } else { $StatusNotVerified })
    $result.error = $_.Exception.Message
    $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "ADM" -Ports @(8090) -ErrorMessage $_.Exception.Message
    Save-Result
    if ($RequireRuntime) { throw }
    Write-Host "Trace Boost smoke not verified. result=$resultPath"
}
