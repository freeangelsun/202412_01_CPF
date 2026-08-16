[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('ADM','BZA')][string]$Module,
    [Parameter(Mandatory)][string]$BaseUrl,
    [string]$Root = (Get-Location).Path,
    [string]$EvidenceDirectory = '',
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$modulePath = if ($Module -eq 'ADM') { 'cpf-admin' } else { 'cpf-biz-admin' }
$frontend = Join-Path $rootPath "$modulePath/frontend"
$stateTool = Join-Path $rootPath 'cpf-tools/verification/tools/cpf-source-state.py'
if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) {
    $EvidenceDirectory = Join-Path $rootPath "build/runtime-openapi/$($Module.ToLowerInvariant())"
} elseif (-not [IO.Path]::IsPathRooted($EvidenceDirectory)) {
    $EvidenceDirectory = Join-Path $rootPath $EvidenceDirectory
}
New-Item -ItemType Directory -Path $EvidenceDirectory -Force | Out-Null

function Get-SourceState {
    $output = @(& python $stateTool --root $rootPath --scope source 2>&1)
    if ($LASTEXITCODE -ne 0) { throw "Source identity calculation failed: $($output -join [Environment]::NewLine)" }
    return (($output | Select-Object -Last 1) | ConvertFrom-Json)
}

if ([string]::IsNullOrWhiteSpace($SourceIdentity)) { $SourceIdentity = [string](Get-SourceState).contentSha1 }
$SourceIdentity = $SourceIdentity.Trim().ToLowerInvariant()
if ($SourceIdentity -notmatch '^[0-9a-f]{40}$') { throw 'SourceIdentity must be a 40-hex Git-independent content identity.' }
$before = Get-SourceState
if ([string]$before.contentSha1 -ne $SourceIdentity) { throw "SourceIdentity drift before runtime OpenAPI validation: expected=$SourceIdentity actual=$($before.contentSha1)" }

$raw = Join-Path $EvidenceDirectory 'runtime-raw.json'
$canonical = Join-Path $EvidenceDirectory 'runtime-canonical.json'
$evidencePath = Join-Path $EvidenceDirectory 'runtime-openapi-release.sanitized.json'
$startedAt = [DateTimeOffset]::UtcNow
$steps = [Collections.Generic.List[object]]::new()
$failure = $null

function Invoke-Step([string]$Name,[scriptblock]$Action) {
    $started = [DateTimeOffset]::UtcNow
    try {
        & $Action
        if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { throw "exit=$LASTEXITCODE" }
        $steps.Add([ordered]@{name=$Name;status='PASS';startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o')}) | Out-Null
    } catch {
        $steps.Add([ordered]@{name=$Name;status='FAIL';startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');error=$_.Exception.Message}) | Out-Null
        throw
    }
}

try {
    Invoke-Step 'runtime-openapi-fetch' {
        $uri = $BaseUrl.TrimEnd('/') + '/v3/api-docs'
        Invoke-WebRequest -Uri $uri -OutFile $raw -TimeoutSec 60 -MaximumRedirection 0 -UseBasicParsing
    }
    Invoke-Step 'runtime-openapi-canonicalize-release-v5' {
        & python (Join-Path $rootPath 'cpf-tools/contracts/openapi/canonicalize-cpf-openapi.py') --input $raw --output $canonical --module $Module --release
    }
    Invoke-Step 'runtime-openapi-release-validation' {
        Push-Location $frontend
        try { & node 'scripts/validate-openapi.mjs' $canonical '--scope=release' }
        finally { Pop-Location }
    }
    Invoke-Step 'runtime-openapi-controller-coverage' {
        & python (Join-Path $rootPath 'cpf-tools/verification/openapi/verify-cpf-openapi-controller-coverage.py') --root $rootPath --module $modulePath --openapi $canonical
    }
    Invoke-Step 'runtime-openapi-source-parity' {
        Push-Location $frontend
        try { & node 'scripts/verify-runtime-openapi-parity.mjs' 'openapi/cpf-openapi.json' $canonical }
        finally { Pop-Location }
    }
    $after = Get-SourceState
    if ([string]$after.contentSha256 -ne [string]$before.contentSha256) { throw 'Runtime OpenAPI verification mutated product source bytes.' }
    $spec = Get-Content -LiteralPath $canonical -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    $result = [ordered]@{
        schemaVersion = 1
        requirementId = "CPF-RUNTIME-OPENAPI-$Module"
        status = 'PASS'
        sourceIdentity = $SourceIdentity
        identityPolicy = 'GIT_INDEPENDENT_CONTENT_SHA1'
        sourceContentSha256 = [string]$before.contentSha256
        module = $Module
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        runtimeOpenApiSha256 = (Get-FileHash -LiteralPath $canonical -Algorithm SHA256).Hash.ToLowerInvariant()
        operationCount = [int]$spec.'x-cpf-openapi-operation-count'
        publicOperationCount = [int]$spec.'x-cpf-public-operation-count'
        canonicalSchemaVersion = [int]$spec.'x-cpf-canonical-schema-version'
        releaseEligible = [bool]$spec.'x-cpf-release-eligible'
        steps = @($steps)
        sanitized = $true
    }
    [IO.File]::WriteAllText($evidencePath,($result|ConvertTo-Json -Depth 20)+"`n",$Utf8NoBom)
    Write-Host "[CPF][OPENAPI][RUNTIME][PASS] module=$Module operations=$($result.operationCount) evidence=$evidencePath"
} catch {
    $failure = $_.Exception.Message
    $result = [ordered]@{
        schemaVersion = 1
        requirementId = "CPF-RUNTIME-OPENAPI-$Module"
        status = 'FAIL'
        sourceIdentity = $SourceIdentity
        identityPolicy = 'GIT_INDEPENDENT_CONTENT_SHA1'
        module = $Module
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        error = $failure
        steps = @($steps)
        sanitized = $true
    }
    [IO.File]::WriteAllText($evidencePath,($result|ConvertTo-Json -Depth 20)+"`n",$Utf8NoBom)
    throw
}
