param(
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $MbrBaseUrl = "http://localhost:8081",
    [string] $XyzBaseUrl = "http://localhost:8099",
    [string] $BizAdmBaseUrl = "http://localhost:8091",
    [string] $ExsBaseUrl = "http://localhost:8092",
    [switch] $SkipMbr,
    [switch] $SkipXyz,
    [switch] $SkipBizAdm,
    [switch] $SkipExs
)

$ErrorActionPreference = "Stop"

function Invoke-JsonSmoke {
    param(
        [string] $BaseUrl,
        [string[]] $RequiredTags
    )

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
            throw "Required Swagger tag is missing: $BaseUrl -> $tag"
        }
    }

    $json = $apiDocs | ConvertTo-Json -Depth 100
    $legacyPattern = ("F" + "PS") + "|" + ("F" + "ps") + "|" + ("f" + "ps")
    if ($json -match $legacyPattern) {
        throw "Legacy project name remains in OpenAPI document: $BaseUrl"
    }
}

Invoke-JsonSmoke -BaseUrl $AdmBaseUrl -RequiredTags @(
    "ADM-Health",
    "ADM-Batch",
    "ADM-CenterCut",
    "ADM-Notification",
    "ADM-Download",
    "ADM-Logs",
    "ADM-OPR Dynamic Log"
)

if (-not $SkipMbr) {
    Invoke-JsonSmoke -BaseUrl $MbrBaseUrl -RequiredTags @("MBR-Auth")
}

if (-not $SkipXyz) {
    Invoke-JsonSmoke -BaseUrl $XyzBaseUrl -RequiredTags @(
        "XYZ-EDU 00. Catalog",
        "XYZ-EDU 11. Security",
        "XYZ-EDU 13. Batch"
    )
}

if (-not $SkipBizAdm) {
    Invoke-JsonSmoke -BaseUrl $BizAdmBaseUrl -RequiredTags @(
        "BIZADM-Auth",
        "BIZADM-Operations"
    )
}

if (-not $SkipExs) {
    Invoke-JsonSmoke -BaseUrl $ExsBaseUrl -RequiredTags @(
        "EXS-Admin",
        "EXS-Operations",
        "EXS-Flow"
    )
}

Write-Host "OpenAPI smoke check passed."
