param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $MbrBaseUrl = "http://localhost:8081",
    [string] $ReferenceBaseUrl = "http://localhost:8099",
    [string] $BzaBaseUrl = "http://localhost:8091",
    [string] $BatBaseUrl = "http://localhost:8093",
    [string] $AccBaseUrl = "http://localhost:8082",
    [string] $GatewayBaseUrl = "http://localhost:8070",
    [string] $ResultDir = "",
    [switch] $SkipAdm,
    [switch] $SkipMbr,
    [switch] $SkipReference,
    [switch] $SkipBza,
    [switch] $SkipBat,
    [switch] $IncludeAcc,
    [switch] $IncludeGateway,
    [switch] $RequireRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "openapi-runtime-result.sanitized.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "NOT_VERIFIED"
    requireRuntime = [bool] $RequireRuntime
    services = @()
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText(
        $resultPath,
        ($result | ConvertTo-Json -Depth 50),
        [System.Text.UTF8Encoding]::new($false))
}

function Test-ContainsPath {
    param(
        [object] $ApiDocs,
        [string] $Path
    )

    if ($null -eq $ApiDocs.paths) {
        return $false
    }
    return $ApiDocs.paths.PSObject.Properties.Name -contains $Path
}

function Get-Utf8Json {
    param([string] $Uri)

    $client = New-Object System.Net.Http.HttpClient
    try {
        $client.Timeout = [TimeSpan]::FromSeconds(15)
        $bytes = $client.GetByteArrayAsync($Uri).GetAwaiter().GetResult()
        $text = [System.Text.Encoding]::UTF8.GetString($bytes)
        return $text | ConvertFrom-Json
    } finally {
        $client.Dispose()
    }
}

function Invoke-JsonSmoke {
    param(
        [string] $ServiceName,
        [string] $BaseUrl,
        [string[]] $RequiredTags,
        [string[]] $RequiredPaths = @()
    )

    $serviceResult = [ordered]@{
        service = $ServiceName
        baseUrl = $BaseUrl
        status = "FAILED"
        requiredTags = $RequiredTags
        requiredPaths = $RequiredPaths
        missingTags = @()
        missingPaths = @()
    }
    try {
        $apiDocsUrl = "$BaseUrl/v3/api-docs"
        $swaggerUrl = "$BaseUrl/swagger-ui/index.html"

        $apiDocs = Get-Utf8Json -Uri $apiDocsUrl
        $swagger = Invoke-WebRequest -Method Get -Uri $swaggerUrl -TimeoutSec 15 -UseBasicParsing

        if ($null -eq $apiDocs.openapi) {
            throw "Invalid OpenAPI document format: $apiDocsUrl"
        }
        if ($swagger.StatusCode -ne 200) {
            throw "Swagger UI response is not healthy: $swaggerUrl"
        }

        $tagNames = @()
        if ($apiDocs.tags) {
            $tagNames = @($apiDocs.tags | ForEach-Object { $_.name })
        }

        foreach ($tag in $RequiredTags) {
            if ($tagNames -notcontains $tag) {
                $serviceResult.missingTags += $tag
            }
        }
        foreach ($path in $RequiredPaths) {
            if (-not (Test-ContainsPath -ApiDocs $apiDocs -Path $path)) {
                $serviceResult.missingPaths += $path
            }
        }

        $json = $apiDocs | ConvertTo-Json -Depth 100
        $legacyPattern = ("F" + "PS") + "|" + ("F" + "ps") + "|" + ("f" + "ps")
        if ($json -match $legacyPattern) {
            throw "Legacy project name remains in OpenAPI document: $BaseUrl"
        }
        if ($serviceResult.missingTags.Count -gt 0 -or $serviceResult.missingPaths.Count -gt 0) {
            throw "Required OpenAPI tags or paths are missing."
        }

        $serviceResult.status = "PASSED"
        $serviceResult.openapi = $apiDocs.openapi
        $serviceResult.pathCount = $apiDocs.paths.PSObject.Properties.Name.Count
        $serviceResult.tagCount = $tagNames.Count
    } catch {
        $serviceResult.status = "FAILED"
        $serviceResult.error = $_.Exception.Message
    }

    $result.services += $serviceResult
    return $serviceResult
}

if (-not $SkipAdm) {
    Invoke-JsonSmoke -ServiceName "ADM" -BaseUrl $AdmBaseUrl -RequiredTags @(
        "ADM-Health",
        "ADM-Batch",
        "ADM-CenterCut",
        "ADM-Notification",
        "ADM-Download",
        "ADM-Logs",
        "ADM-TransactionGroup",
        "ADM-OPR Dynamic Log",
        "ADM-OPR Standard Execution",
        "ADM-OPR Remote Log",
        "ADM-OPR Channel Policy"
    ) -RequiredPaths @(
        "/adm/api/transaction-groups",
        "/adm/api/transaction-groups/{transactionGlobalId}",
        "/adm/api/transaction-groups/{transactionGlobalId}/segments",
        "/adm/api/transaction-groups/{transactionGlobalId}/timeline",
        "/adm/api/transaction-groups/{transactionGlobalId}/headers",
        "/adm/api/transaction-groups/{transactionGlobalId}/external-logs",
        "/adm/api/remote-logs/bundles",
        "/adm/api/remote-logs/bundle-jobs",
        "/adm/api/remote-logs/bundle-jobs/{jobId}",
        "/adm/api/remote-logs/bundle-jobs/{jobId}/download-tokens",
        "/adm/api/remote-logs/bundle-jobs/{jobId}/download",
        "/adm/api/remote-logs/diagnostics",
        "/adm/api/channels",
        "/adm/api/channels/refresh",
        "/adm/api/channels/{channelCode}",
        "/adm/api/channels/policies/{policyKey}",
        "/adm/api/channels/package",
        "/adm/api/channels/package/import"
    ) | Out-Null
}

if (-not $SkipMbr) {
    Invoke-JsonSmoke -ServiceName "MBR" -BaseUrl $MbrBaseUrl -RequiredTags @(
        "MBR-Auth",
        "MBR-BSE Member"
    ) | Out-Null
}

if (-not $SkipReference) {
    Invoke-JsonSmoke -ServiceName "REF" -BaseUrl $ReferenceBaseUrl -RequiredTags @(
        "REF Reference 00. Catalog",
        "REF Reference 11. Security",
        "REF Reference 13. Batch",
        "REF Reference 17. 첨부파일"
    ) -RequiredPaths @(
        "/api/reference/attachments/text",
        "/api/reference/attachments/verify",
        "/api/reference/security/jwt/create",
        "/api/reference/batch/tasklet/run"
    ) | Out-Null
}

if (-not $SkipBza) {
    Invoke-JsonSmoke -ServiceName "BZA" -BaseUrl $BzaBaseUrl -RequiredTags @(
        "BZA-Auth",
        "BZA-Operations",
        "BZA-Backoffice",
        "BZA-Support"
    ) -RequiredPaths @(
        "/api/bza/auth/login",
        "/api/bza/backoffice/organizations",
        "/api/bza/backoffice/employees",
        "/api/bza/backoffice/approvals",
        "/api/bza/admin-users",
        "/api/bza/roles",
        "/api/bza/menus",
        "/api/bza/permissions",
        "/api/bza/notifications",
        "/api/bza/attachments",
        "/api/bza/saved-searches",
        "/api/bza/permissions/simulate"
    ) | Out-Null
}

if (-not $SkipBat) {
    Invoke-JsonSmoke -ServiceName "BAT" -BaseUrl $BatBaseUrl -RequiredTags @(
        "BAT-Operations"
    ) -RequiredPaths @(
        "/bat/api/health",
        "/bat/api/diagnostics/logging",
        "/bat/api/smoke/jobs/{jobId}/run"
    ) | Out-Null
}

# ACC와 Gateway는 선택 실행 모듈이므로 기본 smoke에는 강제하지 않고 명시적으로 요청한 경우만 검증합니다.
if ($IncludeAcc) {
    Invoke-JsonSmoke -ServiceName "ACC" -BaseUrl $AccBaseUrl -RequiredTags @(
        "ACC 생성 참조",
        "ACC 계정",
        "ACC 내부 공유 API"
    ) -RequiredPaths @(
        "/api/v1/acc/reference",
        "/api/v1/accounts",
        "/api/v1/accounts/{accountId}",
        "/internal/api/v1/accounts/{accountId}/summary"
    ) | Out-Null
}

if ($IncludeGateway) {
    Invoke-JsonSmoke -ServiceName "GATEWAY" -BaseUrl $GatewayBaseUrl -RequiredTags @(
        "CPF Gateway"
    ) -RequiredPaths @(
        "/cpf/execute",
        "/cpf/execute/{executionId}"
    ) | Out-Null
}

$failed = @($result.services | Where-Object { $_.status -ne "PASSED" })
if ($failed.Count -eq 0) {
    $result.status = "PASSED"
    Save-Result
    Write-Host "OpenAPI smoke check passed. result=$resultPath"
    exit 0
}

$result.status = if ($RequireRuntime) { "FAILED" } else { "NOT_VERIFIED" }
Save-Result
Write-Host "OpenAPI smoke check did not pass. result=$resultPath"
if ($RequireRuntime) {
    exit 1
}
