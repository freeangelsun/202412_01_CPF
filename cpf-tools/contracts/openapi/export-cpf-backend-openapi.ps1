[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('ADM', 'BZA')]
    [string] $Module,

    [Parameter(Mandatory)]
    [string] $BaseUrl,

    [string] $Root = (Get-Location).Path,
    [string] $Output,
    [string] $EvidenceOutput,
    [string] $SourceSha,
    [string] $Python = 'python',
    [switch] $Release,
    [switch] $UpdateSnapshot
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($UpdateSnapshot) {
    throw 'Tracked frontend OpenAPI is CONTROLLER_SOURCE_PRE_RUNTIME and must not be replaced by BACKEND_RUNTIME during validation.'
}

$rootPath = (Resolve-Path $Root).Path
$modulePath = if ($Module -eq 'ADM') { 'cpf-admin' } else { 'cpf-biz-admin' }
$sourceOpenApi = Join-Path $rootPath "$modulePath/frontend/openapi/cpf-openapi.json"
if (-not (Test-Path -LiteralPath $sourceOpenApi -PathType Leaf)) {
    throw "Tracked source OpenAPI is missing: $sourceOpenApi"
}

function Resolve-CpfSourceSha {
    param([string] $Explicit)
    if ($Explicit -and $Explicit.Trim() -match '^[0-9a-fA-F]{40}$') {
        return $Explicit.Trim().ToLowerInvariant()
    }
    $baseSha = Join-Path $rootPath 'cpf-docs/work/BASE_SHA.txt'
    if (Test-Path -LiteralPath $baseSha -PathType Leaf) {
        $value = (Get-Content -LiteralPath $baseSha -Raw -Encoding UTF8).Trim()
        if ($value -match '^[0-9a-fA-F]{40}$') { return $value.ToLowerInvariant() }
    }
    $manifestPath = Join-Path $rootPath 'cpf-docs/work/PACKAGE_MANIFEST.json'
    if (Test-Path -LiteralPath $manifestPath -PathType Leaf) {
        try {
            $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
            foreach ($candidate in @($manifest.resultGitSha, $manifest.baselineSha)) {
                if ($candidate -and [string]$candidate -match '^[0-9a-fA-F]{40}$') {
                    return ([string]$candidate).ToLowerInvariant()
                }
            }
        } catch {}
    }
    throw 'exact package source identity resolution failed (SourceSha/BASE_SHA/PACKAGE_MANIFEST)'
}

$sourceIdentity = Resolve-CpfSourceSha $SourceSha
$workRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-openapi-{0}-{1}" -f $Module, [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $workRoot -Force | Out-Null
$rawPath = Join-Path $workRoot 'runtime-raw.json'
$canonicalPath = Join-Path $workRoot 'runtime-canonical.json'
if ([string]::IsNullOrWhiteSpace($Output)) {
    $Output = Join-Path $workRoot ("CPF_OPENAPI_{0}.runtime.json" -f $Module)
}
$Output = [IO.Path]::GetFullPath($Output)
if ([IO.Path]::GetFullPath($sourceOpenApi) -eq $Output) {
    throw 'Runtime OpenAPI output cannot overwrite tracked CONTROLLER_SOURCE_PRE_RUNTIME OpenAPI.'
}
if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) {
    $EvidenceOutput = Join-Path $workRoot ("CPF_OPENAPI_{0}.evidence.json" -f $Module)
}
$EvidenceOutput = [IO.Path]::GetFullPath($EvidenceOutput)
$startedAt = [DateTimeOffset]::UtcNow
$operationCount = 0

try {
    $uri = $BaseUrl.TrimEnd('/') + '/v3/api-docs'
    Invoke-WebRequest -Uri $uri -OutFile $rawPath -TimeoutSec 60 -MaximumRedirection 0 -UseBasicParsing

    $canonicalArgs = @(
        (Join-Path $rootPath 'cpf-tools/contracts/openapi/canonicalize-cpf-openapi.py'),
        '--input', $rawPath,
        '--output', $canonicalPath,
        '--module', $Module
    )
    if ($Release) { $canonicalArgs += '--release' }
    & $Python @canonicalArgs
    if ($LASTEXITCODE -ne 0) { throw 'OpenAPI canonicalization failed' }

    Push-Location (Join-Path $rootPath "$modulePath/frontend")
    try {
        & node 'scripts/validate-openapi.mjs' '--scope=release' "--file=$canonicalPath"
        if ($LASTEXITCODE -ne 0) { throw 'Runtime OpenAPI release validation failed' }
        & node 'scripts/verify-runtime-openapi-parity.mjs' $sourceOpenApi $canonicalPath
        if ($LASTEXITCODE -ne 0) { throw 'Runtime/source OpenAPI parity failed' }
    } finally {
        Pop-Location
    }

    & $Python (Join-Path $rootPath 'cpf-tools/verification/openapi/verify-cpf-openapi-controller-coverage.py') `
        --root $rootPath `
        --module $modulePath `
        --openapi $canonicalPath
    if ($LASTEXITCODE -ne 0) { throw 'OpenAPI controller coverage failed' }

    $canonical = Get-Content -LiteralPath $canonicalPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $operationCount = [int] $canonical.'x-cpf-openapi-operation-count'
    $canonicalHash = (Get-FileHash -LiteralPath $canonicalPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $sourceHash = (Get-FileHash -LiteralPath $sourceOpenApi -Algorithm SHA256).Hash.ToLowerInvariant()
    New-Item -ItemType Directory -Path (Split-Path $Output) -Force | Out-Null
    Copy-Item -LiteralPath $canonicalPath -Destination $Output -Force

    $evidence = [ordered]@{
        schemaVersion = 3
        evidenceId = "CPF-OPENAPI-$Module"
        sourceSha = $sourceIdentity
        resultSha = $sourceIdentity
        module = $Module
        command = 'export-cpf-backend-openapi.ps1 -Module <ADM|BZA> -BaseUrl <sanitized> -SourceSha <exact> -Release'
        profile = 'BACKEND_RUNTIME_OPENAPI'
        sourceIdentityPolicy = 'EXPLICIT_OR_BASE_SHA_OR_PACKAGE_MANIFEST'
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = 0
        operationCount = $operationCount
        canonicalSchemaVersion = [int] $canonical.'x-cpf-canonical-schema-version'
        runtimeOpenApiSha256 = $canonicalHash
        sourceOpenApiPath = [IO.Path]::GetRelativePath($rootPath, $sourceOpenApi).Replace('\', '/')
        sourceOpenApiSha256 = $sourceHash
        runtimeSourceParity = $true
        controllerCoverage = $true
        runtimeOutput = $Output
        updateSnapshot = $false
        sanitized = $true
        releaseEligible = [bool]($Release -and $canonical.'x-cpf-release-eligible' -eq $true)
        requirements = @('CPF-OPENAPI-CANONICAL')
    }
    New-Item -ItemType Directory -Path (Split-Path $EvidenceOutput) -Force | Out-Null
    [IO.File]::WriteAllText($EvidenceOutput, ($evidence | ConvertTo-Json -Depth 12) + "`n", [Text.UTF8Encoding]::new($false))
    Write-Host "[CPF][OPENAPI][PASS] module=$Module operations=$operationCount sha=$canonicalHash releaseEligible=$($evidence.releaseEligible)"
} catch {
    $failure = [ordered]@{
        schemaVersion = 3
        evidenceId = "CPF-OPENAPI-$Module"
        sourceSha = $sourceIdentity
        resultSha = $sourceIdentity
        module = $Module
        profile = 'BACKEND_RUNTIME_OPENAPI'
        sourceIdentityPolicy = 'EXPLICIT_OR_BASE_SHA_OR_PACKAGE_MANIFEST'
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = 1
        error = ($_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+', '$1=***')
        updateSnapshot = $false
        sanitized = $true
        releaseEligible = $false
        requirements = @('CPF-OPENAPI-CANONICAL')
    }
    New-Item -ItemType Directory -Path (Split-Path $EvidenceOutput) -Force | Out-Null
    [IO.File]::WriteAllText($EvidenceOutput, ($failure | ConvertTo-Json -Depth 12) + "`n", [Text.UTF8Encoding]::new($false))
    throw
} finally {
    if (-not $env:CPF_KEEP_OPENAPI_WORK) {
        Remove-Item -LiteralPath $workRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
