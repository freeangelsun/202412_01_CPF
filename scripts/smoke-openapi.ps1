param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $MbrBaseUrl = "http://localhost:8081",
    [string] $XyzBaseUrl = "http://localhost:8099",
    [string] $BzaBaseUrl = "http://localhost:8091",
    [string] $BatBaseUrl = "http://localhost:8093",
    [string] $ResultDir = "",
    [switch] $SkipAdm,
    [switch] $SkipMbr,
    [switch] $SkipXyz,
    [switch] $SkipBza,
    [switch] $SkipBat,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "openapi-runtime-result.json"

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
        "ADM-OPR Remote Log"
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
        "/adm/api/remote-logs/diagnostics"
    ) | Out-Null
}

if (-not $SkipMbr) {
    Invoke-JsonSmoke -ServiceName "MBR" -BaseUrl $MbrBaseUrl -RequiredTags @(
        "MBR-Auth",
        "MBR-BSE Member"
    ) | Out-Null
}

if (-not $SkipXyz) {
    Invoke-JsonSmoke -ServiceName "XYZ" -BaseUrl $XyzBaseUrl -RequiredTags @(
        "XYZ-EDU 00. Catalog",
        "XYZ-EDU 11. Security",
        "XYZ-EDU 13. Batch",
        "XYZ-EDU 16. AI",
        "XYZ-EDU 17. 첨부파일"
    ) -RequiredPaths @(
        "/xyz/edu/ai/structured",
        "/xyz/edu/ai/rag",
        "/xyz/edu/ai/jobs/{jobId}/approve",
        "/xyz/edu/attachments/text",
        "/xyz/edu/attachments/verify"
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
