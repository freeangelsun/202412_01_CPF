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
    [switch] $Release,
    [switch] $UpdateSnapshot
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($Release -and $UpdateSnapshot) {
    throw 'Release 검증과 Snapshot 갱신은 동시에 수행할 수 없습니다.'
}

$rootPath = (Resolve-Path $Root).Path
$modulePath = if ($Module -eq 'ADM') { 'cpf-admin' } else { 'cpf-biz-admin' }
if ([string]::IsNullOrWhiteSpace($Output)) {
    $Output = Join-Path $rootPath "$modulePath/frontend/openapi/cpf-openapi.json"
}
$Output = [IO.Path]::GetFullPath($Output)

$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') {
    throw 'exact Git SHA resolution failed'
}
$sourceStatus = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
$sourceDirty = $sourceStatus.Count -gt 0
if ($Release -and $sourceDirty) {
    throw 'Release OpenAPI export requires a clean working tree.'
}

$workRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-openapi-{0}-{1}" -f $Module, [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $workRoot -Force | Out-Null
$rawPath = Join-Path $workRoot 'runtime-raw.json'
$canonicalPath = Join-Path $workRoot 'runtime-canonical.json'
if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) {
    $EvidenceOutput = Join-Path $workRoot ("CPF_QA34_OPENAPI_{0}.sanitized.json" -f $Module)
}
$EvidenceOutput = [IO.Path]::GetFullPath($EvidenceOutput)
$startedAt = [DateTimeOffset]::UtcNow
$exitCode = 1
$releaseEligible = $false
$operationCount = 0

try {
    $uri = $BaseUrl.TrimEnd('/') + '/v3/api-docs'
    Invoke-WebRequest -Uri $uri -OutFile $rawPath -TimeoutSec 60 -MaximumRedirection 0 -UseBasicParsing

    & python (Join-Path $rootPath 'cpf-tools/scripts/canonicalize-cpf-openapi.py') `
        --input $rawPath `
        --output $canonicalPath `
        --module $Module
    if ($LASTEXITCODE -ne 0) {
        throw 'OpenAPI canonicalization failed'
    }

    Push-Location (Join-Path $rootPath "$modulePath/frontend")
    try {
        $env:CPF_OPENAPI_FILE = $canonicalPath
        & node 'scripts/validate-openapi.mjs' $canonicalPath
        if ($LASTEXITCODE -ne 0) {
            throw 'OpenAPI product validation failed'
        }
    } finally {
        Remove-Item Env:CPF_OPENAPI_FILE -ErrorAction SilentlyContinue
        Pop-Location
    }

    & python (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-openapi-controller-coverage.py') `
        --root $rootPath `
        --module $modulePath `
        --openapi $canonicalPath
    if ($LASTEXITCODE -ne 0) {
        throw 'OpenAPI controller coverage failed'
    }

    $canonical = Get-Content -LiteralPath $canonicalPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $operationCount = [int] $canonical.'x-cpf-openapi-operation-count'
    $canonicalHash = (Get-FileHash -LiteralPath $canonicalPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $trackedHash = $null
    $snapshotMatches = $false

    if (Test-Path -LiteralPath $Output -PathType Leaf) {
        $trackedHash = (Get-FileHash -LiteralPath $Output -Algorithm SHA256).Hash.ToLowerInvariant()
        $snapshotMatches = $trackedHash -eq $canonicalHash
    }

    if ($Release) {
        if (-not (Test-Path -LiteralPath $Output -PathType Leaf)) {
            throw "Tracked OpenAPI snapshot is missing: $Output"
        }
        if (-not $snapshotMatches) {
            throw "Tracked OpenAPI snapshot is stale. runtime=$canonicalHash tracked=$trackedHash"
        }
    } elseif ($UpdateSnapshot) {
        New-Item -ItemType Directory -Path (Split-Path $Output) -Force | Out-Null
        Copy-Item -LiteralPath $canonicalPath -Destination $Output -Force
        $trackedHash = $canonicalHash
        $snapshotMatches = $true
    }

    $currentSha = (& git -C $rootPath rev-parse HEAD).Trim()
    if ($currentSha -ne $sourceSha) {
        throw 'Git HEAD changed during OpenAPI export.'
    }
    if ($Release) {
        $currentStatus = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
        if ($currentStatus.Count -gt 0) {
            throw 'Release OpenAPI export changed the source working tree.'
        }
    }

    $exitCode = 0
    $releaseEligible = $Release -and -not $sourceDirty -and $snapshotMatches
    $evidence = [ordered]@{
        schemaVersion = 2
        evidenceId = "QA34-OPENAPI-$Module"
        sourceSha = $sourceSha
        resultSha = $sourceSha
        sourceDirty = $sourceDirty
        module = $Module
        command = 'export-cpf-backend-openapi.ps1 -Module <ADM|BZA> -BaseUrl <sanitized> -Release'
        profile = 'BACKEND_RUNTIME_OPENAPI'
        environment = [ordered]@{
            os = [Environment]::OSVersion.ToString()
            java = (& java -version 2>&1 | Select-Object -First 1 | Out-String).Trim()
            node = (& node --version).Trim()
        }
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = $exitCode
        operationCount = $operationCount
        canonicalOpenApiSha256 = $canonicalHash
        trackedOpenApiPath = [IO.Path]::GetRelativePath($rootPath, $Output).Replace('\', '/')
        trackedOpenApiSha256 = $trackedHash
        snapshotMatches = $snapshotMatches
        updateSnapshot = [bool] $UpdateSnapshot
        sanitized = $true
        releaseEligible = [bool] $releaseEligible
        requirements = @('QA34-REQ-004', 'QA34-REQ-005', 'QA34-REQ-006')
    }
    New-Item -ItemType Directory -Path (Split-Path $EvidenceOutput) -Force | Out-Null
    [IO.File]::WriteAllText(
        $EvidenceOutput,
        ($evidence | ConvertTo-Json -Depth 12) + "`n",
        [Text.UTF8Encoding]::new($false))
    Write-Host "[CPF][OPENAPI][PASS] module=$Module operations=$operationCount sha=$canonicalHash releaseEligible=$releaseEligible"
} catch {
    $failure = [ordered]@{
        schemaVersion = 2
        evidenceId = "QA34-OPENAPI-$Module"
        sourceSha = $sourceSha
        resultSha = $sourceSha
        sourceDirty = $sourceDirty
        module = $Module
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = 1
        error = ($_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+', '$1=***')
        sanitized = $true
        releaseEligible = $false
        requirements = @('QA34-REQ-004', 'QA34-REQ-005', 'QA34-REQ-006')
    }
    New-Item -ItemType Directory -Path (Split-Path $EvidenceOutput) -Force | Out-Null
    [IO.File]::WriteAllText(
        $EvidenceOutput,
        ($failure | ConvertTo-Json -Depth 12) + "`n",
        [Text.UTF8Encoding]::new($false))
    throw
} finally {
    if (-not $env:CPF_KEEP_QA34_WORK) {
        Remove-Item -LiteralPath $workRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
