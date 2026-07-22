param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $ResultDir = "",
    [int] $TimeoutSec = 10,
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
$resultPath = Join-Path $ResultDir "adm-service-registry-runtime-result.json"

$StatusDone = "DONE"
$StatusFailed = "FAILED"
$StatusNotVerified = "NOT_VERIFIED"

function Save-Result {
    param([object] $Result)
    $Result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($Result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function Invoke-Json {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null
    )
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
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

function New-CpfSmokeHeaders {
    return @{
        "X-Transaction-Id" = ((Get-Date).ToString("yyyyMMddHHmmssfff") + "ADMadmsvc0000001")
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "SMOKE"
        "X-Channel-Code" = "ADM"
        "X-Client-App-Id" = "cpf-adm-service-registry-smoke"
        "X-User-Id" = "runtime-smoke"
    }
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    probes = [ordered]@{}
}

try {
    $controllerPath = Join-Path $Root "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmServiceRegistryController.java"
    $filterPath = Join-Path $Root "cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java"
    $result.probes.staticContract = [ordered]@{
        controllerExists = Test-Path -LiteralPath $controllerPath
        authFilterHasRoute = ([System.IO.File]::ReadAllText($filterPath, [System.Text.Encoding]::UTF8).Contains("/adm/api/service-registry"))
    }
    if (-not $result.probes.staticContract.controllerExists -or -not $result.probes.staticContract.authFilterHasRoute) {
        throw "ADM service registry source contract is incomplete."
    }

    if (-not $RunRuntime) {
        $result.status = $StatusNotVerified
        $result.reason = "RunRuntime option was not provided, so real ADM API call verification was not executed."
        Save-Result $result
        Write-Host "ADM service registry runtime smoke not verified. result=$resultPath"
        exit 0
    }

    try {
        $null = Invoke-WebRequest -Method Get -Uri "$AdmBaseUrl/adm/api/health" -Headers (New-CpfSmokeHeaders) -TimeoutSec $TimeoutSec -UseBasicParsing
    } catch {
        $result.status = $StatusNotVerified
        $result.reason = "ADM server was not reachable, so real ADM API call verification was not executed."
        Save-Result $result
        if ($RequireRuntime) {
            throw $result.reason
        }
        Write-Host "ADM service registry runtime smoke not verified. result=$resultPath"
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
    }

    $headers = New-CpfSmokeHeaders
    $login = Invoke-Json -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Headers (New-CpfSmokeHeaders) -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    $headers.Authorization = "Bearer $($login.accessToken)"

    $services = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/services?limit=10" -Headers $headers
    $endpoints = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/endpoints?limit=10" -Headers $headers
    $instances = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/instances?limit=10" -Headers $headers
    $health = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/health?limit=10" -Headers $headers
    $routingPolicies = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/routing-policies?limit=10" -Headers $headers
    $circuitStates = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/circuit-states?limit=10" -Headers $headers
    $callHistory = Invoke-Json -Method Get -Uri "$AdmBaseUrl/adm/api/service-registry/call-history?limit=10" -Headers $headers

    $result.probes.api = [ordered]@{
        services = @($services).Count
        endpoints = @($endpoints).Count
        instances = @($instances).Count
        health = @($health).Count
        routingPolicies = @($routingPolicies).Count
        circuitStates = @($circuitStates).Count
        callHistory = @($callHistory).Count
    }
    $result.status = $StatusDone
    Save-Result $result
    Write-Host "ADM service registry runtime smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    Save-Result $result
    throw
}
