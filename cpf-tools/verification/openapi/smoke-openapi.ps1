param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string[]] $Modules = @(),
    [hashtable] $BaseUrlOverrides = @{},
    [string] $ResultDir = "",
    [switch] $RequireRuntime
)

# PowerShell 7과 Java 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
Add-Type -AssemblyName System.Net.Http
. (Join-Path $PSScriptRoot "..\..\runtime\tools\runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "openapi-runtime-result.sanitized.json"
$selectedModules = @(Resolve-CpfRuntimeModules -Modules $Modules -Root $Root)

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "NOT_VERIFIED"
    requireRuntime = [bool] $RequireRuntime
    modules = @($selectedModules | ForEach-Object { $_.module })
    services = @()
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Test-ContainsPath {
    param(
        [object] $ApiDocs,
        [string] $Path
    )

    return $null -ne $ApiDocs.paths -and $ApiDocs.paths.PSObject.Properties.Name -contains $Path
}

function Get-Utf8Json {
    param([string] $Uri)

    $client = [System.Net.Http.HttpClient]::new()
    try {
        $client.Timeout = [TimeSpan]::FromSeconds(15)
        $bytes = $client.GetByteArrayAsync($Uri).GetAwaiter().GetResult()
        return [System.Text.Encoding]::UTF8.GetString($bytes) | ConvertFrom-Json
    } finally {
        $client.Dispose()
    }
}

function Get-PlatformOpenApiContract {
    param([string] $Module)

    switch ($Module) {
        "ADM" {
            return [ordered]@{
                tags = @(
                    "ADM-Health", "ADM-Batch", "ADM-CenterCut", "ADM-Notification",
                    "ADM-Download", "ADM-Logs", "ADM-TransactionGroup",
                    "ADM-OPR Dynamic Log", "ADM-OPR Standard Execution",
                    "ADM-OPR Remote Log", "ADM-OPR Channel Policy"
                )
                paths = @(
                    "/adm/api/transaction-groups",
                    "/adm/api/transaction-groups/{transactionId}",
                    "/adm/api/transaction-groups/{transactionId}/segments",
                    "/adm/api/transaction-groups/{transactionId}/timeline",
                    "/adm/api/transaction-groups/{transactionId}/headers",
                    "/adm/api/transaction-groups/{transactionId}/external-logs",
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
                )
            }
        }
        "BZA" {
            return [ordered]@{
                tags = @("BZA-Auth", "BZA-Operations", "BZA-Backoffice", "BZA-Support")
                paths = @(
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
                )
            }
        }
        "EDU" {
            return [ordered]@{
                tags = @(
                    "EDU Education 00. Catalog",
                    "EDU Education 11. Security",
                    "EDU Education 13. Batch",
                    "EDU Education 17. 첨부파일"
                )
                paths = @(
                    "/api/education/attachments/text",
                    "/api/education/attachments/verify",
                    "/api/education/security/jwt/create",
                    "/api/education/batch/tasklet/run"
                )
            }
        }
        "GWY" {
            return [ordered]@{
                tags = @("CPF Gateway")
                paths = @("/cpf/execute", "/cpf/execute/{executionId}")
            }
        }
        default {
            throw "OpenAPI contract가 정의되지 않은 Platform module입니다: $Module"
        }
    }
}

function Get-OpenApiContract {
    param([object] $Module)

    if ([bool] $Module.generatedDomain) {
        return [ordered]@{
            tags = @("$($Module.module) Sample Item")
            paths = @(
                "/api/v1/$($Module.domainName)/sample-items",
                "/api/v1/$($Module.domainName)/sample-items/{sampleKey}",
                "/api/v1/$($Module.domainName)/sample-items/{sampleItemId}/update",
                "/api/v1/$($Module.domainName)/sample-items/{sampleItemId}/delete",
                "/api/v1/$($Module.domainName)/sample-items/cursor",
                "/api/v1/$($Module.domainName)/sample-items/rollback-verify"
            )
        }
    }
    return Get-PlatformOpenApiContract -Module ([string] $Module.module)
}

function Invoke-JsonSmoke {
    param(
        [object] $Module,
        [string] $BaseUrl,
        [object] $Contract
    )

    $serviceResult = [ordered]@{
        service = $Module.module
        projectName = $Module.projectName
        generatedDomain = [bool] $Module.generatedDomain
        baseUrl = $BaseUrl
        status = "FAILED"
        requiredTags = @($Contract.tags)
        requiredPaths = @($Contract.paths)
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
            $tagNames = @($apiDocs.tags | ForEach-Object { [string] $_.name })
        }
        foreach ($tag in @($Contract.tags)) {
            if ($tagNames -notcontains $tag) { $serviceResult.missingTags += $tag }
        }
        foreach ($path in @($Contract.paths)) {
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
        $serviceResult.error = $_.Exception.Message
    }

    $result.services += $serviceResult
}

foreach ($module in $selectedModules) {
    if (-not [bool] $module.openApi) {
        $result.services += [ordered]@{
            service = $module.module
            projectName = $module.projectName
            status = "NOT_APPLICABLE"
            reason = "module does not publish an OpenAPI endpoint"
        }
        continue
    }
    $baseUrl = "http://localhost:$($module.port)"
    if ($BaseUrlOverrides.ContainsKey([string] $module.module)) {
        $baseUrl = [string] $BaseUrlOverrides[[string] $module.module]
    }
    Invoke-JsonSmoke -Module $module -BaseUrl $baseUrl -Contract (Get-OpenApiContract -Module $module)
}

$failed = @($result.services | Where-Object { $_.status -eq "FAILED" })
$applicable = @($result.services | Where-Object { $_.status -ne "NOT_APPLICABLE" })
if ($failed.Count -eq 0 -and $applicable.Count -gt 0) {
    $result.status = "PASSED"
} elseif ($applicable.Count -eq 0) {
    $result.status = "NOT_APPLICABLE"
} else {
    $result.status = if ($RequireRuntime) { "FAILED" } else { "NOT_VERIFIED" }
}
Save-Result

Write-Host "OpenAPI smoke check finished. status=$($result.status) result=$resultPath"
if ($RequireRuntime -and $result.status -eq "FAILED") {
    exit 1
}
exit 0
