[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('ADM','BZA')][string]$Module,
    [Parameter(Mandatory)][string]$BaseUrl,
    [string]$Root = (Get-Location).Path,
    [string]$EvidenceOutput = '',
    [switch]$Release
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$modulePath = if ($Module -eq 'ADM') { 'cpf-admin' } else { 'cpf-biz-admin' }
$frontend = if ($Module -eq 'ADM') { Join-Path $rootPath 'cpf-admin/frontend' } else { Join-Path $rootPath 'cpf-biz-frontend' }
$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') { throw 'exact source SHA resolution failed' }
$before = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
if ($LASTEXITCODE -ne 0) { throw 'git status failed' }
if ($Release -and $before.Count -gt 0) { throw "Release OpenAPI synchronization requires clean tree.`n$($before -join [Environment]::NewLine)" }

$workRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-openapi-sync-{0}-{1}" -f $Module,[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $workRoot -Force | Out-Null
$canonical = Join-Path $workRoot 'cpf-openapi.runtime-canonical.json'
$exportEvidence = Join-Path $workRoot 'openapi-export.sanitized.json'
$operationEvidenceDir = Join-Path $workRoot 'operation-source'
$startedAt = [DateTimeOffset]::UtcNow
$failures = [Collections.Generic.List[string]]::new()
$steps = [Collections.Generic.List[object]]::new()
function Step([string]$Name,[scriptblock]$Action) {
    $started=[DateTimeOffset]::UtcNow; $code=0; $message='PASS'
    try { & $Action; if($LASTEXITCODE -and $LASTEXITCODE -ne 0){throw "exit=$LASTEXITCODE"} }
    catch { $code=1; $message=$_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+','$1=***'; $script:failures.Add("$Name`: $message")|Out-Null }
    finally { $script:steps.Add([ordered]@{name=$Name;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=$code;result=$message})|Out-Null }
}
Step 'explicit-operation-id-source-contract' {
    & (Join-Path $rootPath 'cpf-tools/contracts/openapi/ensure-explicit-openapi-operation-ids.ps1') -Root $rootPath -ResultDir $operationEvidenceDir
}
Step 'runtime-openapi-export-and-controller-coverage' {
    & (Join-Path $rootPath 'cpf-tools/contracts/openapi/export-cpf-backend-openapi.ps1') -Module $Module -BaseUrl $BaseUrl -Root $rootPath -Output $canonical -EvidenceOutput $exportEvidence
}
if ($failures.Count -eq 0) {
    Step 'runtime-openapi-product-validation' {
        Push-Location $frontend
        try { $env:CPF_OPENAPI_FILE=$canonical; & node 'scripts/validate-openapi.mjs' $canonical }
        finally { Remove-Item Env:CPF_OPENAPI_FILE -ErrorAction SilentlyContinue; Pop-Location }
    }
    Step 'clean-orval-operation-contract-generation' {
        Push-Location $frontend
        try {
            $env:CPF_OPENAPI_FILE=$canonical; $env:CPF_SOURCE_SHA=$sourceSha
            if ($Module -eq 'BZA') { Copy-Item -LiteralPath $canonical -Destination (Join-Path $frontend 'openapi/cpf-openapi.json') -Force; & node 'scripts/generate-reference-client.mjs' } else { & node 'scripts/generate-checked-client.mjs' }
        } finally {
            Remove-Item Env:CPF_OPENAPI_FILE -ErrorAction SilentlyContinue
            Remove-Item Env:CPF_SOURCE_SHA -ErrorAction SilentlyContinue
            Pop-Location
        }
    }
    Step 'generated-operation-consumer-coverage' {
        Push-Location $frontend
        try { if ($Module -eq 'BZA') { & node 'scripts/generate-reference-client.mjs' } else { & node 'scripts/verify-operation-consumer.mjs' } }
        finally { Pop-Location }
    }
}

$trackedOpenApi = if ($Module -eq 'ADM') { Join-Path $frontend 'openapi/cpf-openapi.json' } else { Join-Path $rootPath 'cpf-biz-admin/openapi/cpf-openapi.json' }
if ($failures.Count -eq 0) {
    if ($Release) {
        Step 'tracked-openapi-drift-zero' {
            if (-not (Test-Path -LiteralPath $trackedOpenApi -PathType Leaf)) { throw "Tracked OpenAPI missing: $trackedOpenApi" }
            $runtimeHash=(Get-FileHash -LiteralPath $canonical -Algorithm SHA256).Hash
            $trackedHash=(Get-FileHash -LiteralPath $trackedOpenApi -Algorithm SHA256).Hash
            if ($runtimeHash -ne $trackedHash) { throw "Tracked OpenAPI drift: runtime=$runtimeHash tracked=$trackedHash" }
        }
    } else {
        Copy-Item -LiteralPath $canonical -Destination $trackedOpenApi -Force
    }
}
$after = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
$changed = @($after | Where-Object { $before -notcontains $_ })
if ($Release -and $changed.Count -gt 0) { $failures.Add("Release synchronization changed tracked source: $($changed -join ', ')")|Out-Null }
if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) { $EvidenceOutput = Join-Path $workRoot "CPF_SELF_DEV_025_OPENAPI_$Module.sanitized.json" }
$spec = $null; $operationCount=0; $openApiHash=$null
if (Test-Path -LiteralPath $canonical -PathType Leaf) {
    $spec=Get-Content -LiteralPath $canonical -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
    $operationCount=[int]$spec.'x-cpf-openapi-operation-count'
    $openApiHash=(Get-FileHash -LiteralPath $canonical -Algorithm SHA256).Hash.ToLowerInvariant()
}
$evidence=[ordered]@{
    schemaVersion=1;evidenceId="CPF-SELF-DEV-025-$Module-OPENAPI-GENERATED-CONSUMER";module=$Module;
    sourceSha=$sourceSha;resultSha=if($failures.Count-eq0 -and $Release){$sourceSha}else{$null};
    operationCount=$operationCount;runtimeOpenApiSha256=$openApiHash;startedAt=$startedAt.ToString('o');
    finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=if($failures.Count-eq0){0}else{1};
    releaseMode=[bool]$Release;steps=$steps;failures=$failures;sanitized=$true;releaseEligible=($Release -and $failures.Count-eq0)
}
[IO.File]::WriteAllText([IO.Path]::GetFullPath($EvidenceOutput),($evidence|ConvertTo-Json -Depth 20)+"`n",$Utf8NoBom)
if($failures.Count-gt0){throw "OpenAPI/generated-client synchronization failed: $($failures -join '; ')"}
Write-Host "[CPF][PASS] runtime OpenAPI/generated client/consumer module=$Module operations=$operationCount evidence=$EvidenceOutput"
