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

    $paths = Get-JsonProperty -Value $ApiDocs -Name "paths"
    return $null -ne $paths -and $paths.PSObject.Properties.Name -contains $Path
}

function Get-JsonProperty {
    param(
        [object] $Value,
        [string] $Name,
        [object] $Default = $null
    )

    if ($null -eq $Value) { return $Default }
    $property = $Value.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) { return $Default }
    return $property.Value
}

function Get-OpenApiTagNames {
    param([object] $ApiDocs)

    # OpenAPI top-level `tags` is a catalog/description and is optional.  The actual operation
    # tags are authoritative for a generated document, so inspect both forms without accessing a
    # missing JSON property directly under StrictMode.
    $names = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    foreach ($tag in @(Get-JsonProperty -Value $ApiDocs -Name "tags" -Default @())) {
        $name = [string](Get-JsonProperty -Value $tag -Name "name" -Default "")
        if (-not [string]::IsNullOrWhiteSpace($name)) { [void]$names.Add($name) }
    }
    $paths = Get-JsonProperty -Value $ApiDocs -Name "paths"
    if ($null -ne $paths) {
        foreach ($pathEntry in @($paths.PSObject.Properties)) {
            foreach ($operation in @($pathEntry.Value.PSObject.Properties.Value)) {
                foreach ($tag in @(Get-JsonProperty -Value $operation -Name "tags" -Default @())) {
                    $name = [string]$tag
                    if (-not [string]::IsNullOrWhiteSpace($name)) { [void]$names.Add($name) }
                }
            }
        }
    }
    return @($names | Sort-Object)
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
        "MBW" {
            return [ordered]@{
                # Backoffice Domain은 Generated Business Domain과 같은 업무 API 계약을 사용하며,
                # runtime smoke에서는 핵심 업무 경로의 존재를 확인합니다. Tag는 Source OpenAPI가
                # top-level tag catalog를 사용하지 않아 강제하지 않습니다.
                tags = @()
                paths = @(
                    "/api/v1/backoffice/auth/login",
                    "/api/v1/backoffice/backoffice/organizations",
                    "/api/v1/backoffice/backoffice/employees",
                    "/api/v1/backoffice/approvals/inbox",
                    "/api/v1/backoffice/admin-users",
                    "/api/v1/backoffice/roles",
                    "/api/v1/backoffice/menus",
                    "/api/v1/backoffice/permissions",
                    "/api/v1/backoffice/notifications",
                    "/api/v1/backoffice/attachments",
                    "/api/v1/backoffice/saved-searches",
                    "/api/v1/backoffice/permissions/simulate"
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
        if ($null -eq (Get-JsonProperty -Value $apiDocs -Name "openapi")) {
            throw "Invalid OpenAPI document format: $apiDocsUrl"
        }
        if ($swagger.StatusCode -ne 200) {
            throw "Swagger UI response is not healthy: $swaggerUrl"
        }

        $tagNames = @(Get-OpenApiTagNames -ApiDocs $apiDocs)
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
        $serviceResult.openapi = Get-JsonProperty -Value $apiDocs -Name "openapi"
        $paths = Get-JsonProperty -Value $apiDocs -Name "paths"
        $serviceResult.pathCount = if ($null -eq $paths) { 0 } else { @($paths.PSObject.Properties).Count }
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
