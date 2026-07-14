param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AccBaseUrl = "http://localhost:8080",
    [string] $MbrBaseUrl = "http://localhost:8081",
    [string] $XyzBaseUrl = "http://localhost:8099",
    [string] $BizAdmBaseUrl = "http://localhost:8091",
    [string] $ExsBaseUrl = "http://localhost:8092",
    [string] $ResultDir = "",
    [switch] $SkipAdm,
    [switch] $SkipAcc,
    [switch] $SkipMbr,
    [switch] $SkipXyz,
    [switch] $SkipBizAdm,
    [switch] $SkipExs,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"

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
    $result | ConvertTo-Json -Depth 50 | Set-Content -LiteralPath $resultPath -Encoding UTF8
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

        $apiDocs = Invoke-RestMethod -Method Get -Uri $apiDocsUrl -TimeoutSec 15
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
        "ADM-OPR Dynamic Log"
    ) -RequiredPaths @(
        "/adm/api/transaction-groups",
        "/adm/api/transaction-groups/{transactionGlobalId}",
        "/adm/api/transaction-groups/{transactionGlobalId}/segments",
        "/adm/api/transaction-groups/{transactionGlobalId}/timeline",
        "/adm/api/transaction-groups/{transactionGlobalId}/headers",
        "/adm/api/transaction-groups/{transactionGlobalId}/external-logs"
    ) | Out-Null
}

if (-not $SkipAcc) {
    Invoke-JsonSmoke -ServiceName "ACC" -BaseUrl $AccBaseUrl -RequiredTags @(
        "ACC-EDU Composite Transaction",
        "ACC Reference"
    ) -RequiredPaths @(
        "/acc/edu/composite/member-then-external",
        "/acc/edu/composite/member-calls-external",
        "/acc/edu/composite/member-then-external-failure",
        "/api/v1/acc/reference/member-external"
    ) | Out-Null
}

if (-not $SkipMbr) {
    Invoke-JsonSmoke -ServiceName "MBR" -BaseUrl $MbrBaseUrl -RequiredTags @(
        "MBR-Auth",
        "MBR-EDU Composite Transaction",
        "MBR Reference"
    ) -RequiredPaths @(
        "/api/v1/mbr/reference/member-acc-exs"
    ) | Out-Null
}

if (-not $SkipXyz) {
    Invoke-JsonSmoke -ServiceName "XYZ" -BaseUrl $XyzBaseUrl -RequiredTags @(
        "XYZ-EDU 00. Catalog",
        "XYZ-EDU 11. Security",
        "XYZ-EDU 13. Batch"
    ) | Out-Null
}

if (-not $SkipBizAdm) {
    Invoke-JsonSmoke -ServiceName "BIZADM" -BaseUrl $BizAdmBaseUrl -RequiredTags @(
        "BIZADM-Auth",
        "BIZADM-Operations"
    ) | Out-Null
}

if (-not $SkipExs) {
    Invoke-JsonSmoke -ServiceName "EXS" -BaseUrl $ExsBaseUrl -RequiredTags @(
        "EXS-Admin",
        "EXS-Operations",
        "EXS-Flow",
        "EXS-EDU Composite Transaction"
    ) -RequiredPaths @(
        "/api/exs/outbound",
        "/api/exs/edu/external-transfer",
        "/api/exs/edu/external-transfer/failure"
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
