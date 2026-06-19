param(
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $XyzBaseUrl = "http://localhost:8099",
    [switch] $SkipXyz
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
    $swagger = Invoke-WebRequest -Method Get -Uri $swaggerUrl -TimeoutSec 15

    if ($null -eq $apiDocs.openapi) {
        throw "OpenAPI 문서 형식이 아닙니다: $apiDocsUrl"
    }
    if ($swagger.StatusCode -ne 200) {
        throw "Swagger UI 응답이 정상이 아닙니다: $swaggerUrl"
    }

    $tagNames = @()
    if ($apiDocs.tags) {
        $tagNames = @($apiDocs.tags | ForEach-Object { $_.name })
    }

    foreach ($tag in $RequiredTags) {
        if ($tagNames -notcontains $tag) {
            throw "필수 Swagger tag가 없습니다: $BaseUrl -> $tag"
        }
    }

    $json = $apiDocs | ConvertTo-Json -Depth 100
    $legacyPattern = ("F" + "PS") + "|" + ("F" + "ps") + "|" + ("f" + "ps")
    if ($json -match $legacyPattern) {
        throw "과거 프로젝트명 잔재가 OpenAPI 문서에 남아 있습니다: $BaseUrl"
    }
}

Invoke-JsonSmoke -BaseUrl $AdmBaseUrl -RequiredTags @(
    "ADM-Batch",
    "ADM-Notification",
    "ADM-Download",
    "ADM-Log",
    "ADM-Dynamic-Log-Level"
)

if (-not $SkipXyz) {
    Invoke-JsonSmoke -BaseUrl $XyzBaseUrl -RequiredTags @("EDU")
}

Write-Host "OpenAPI smoke check passed."
