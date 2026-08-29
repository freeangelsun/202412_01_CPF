[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$VerifierRunId,
    [Parameter(Mandatory=$true)][string]$ProfilePath,
    [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string]$DockerImage='cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1',
    [string]$DockerNetwork='cpf_default'
)
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if($VerifierRunId -notmatch '^[a-f0-9]{8,24}$'){throw 'Invalid verifier run id.'}
foreach($name in @('CPF_ADMIN_PASSWORD','CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD','CPF_LOCAL_RUNTIME_DB_PASSWORD')){
    if([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name,'Process'))){throw "$name is required in process environment."}
}
$rootPath=(Resolve-Path -LiteralPath $Root).Path
$profileFull=(Resolve-Path -LiteralPath $ProfilePath).Path
$profileDir=Split-Path -Parent $profileFull;$profileLeaf=Split-Path -Leaf $profileFull
$dockerArgs=@(
    'run','--rm','--network',$DockerNetwork,
    '--mount',"type=bind,source=$rootPath,target=/workspace/cpf,readonly",
    '--mount',"type=bind,source=$profileDir,target=/workspace/result,readonly",
    '--workdir','/workspace/cpf',
    '--env','CPF_ADMIN_PASSWORD','--env','CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD','--env','CPF_LOCAL_RUNTIME_DB_PASSWORD',
    $DockerImage,'pwsh','-NoProfile','-File','/workspace/cpf/cpf-tools/db/verification/cleanup-cpf-db-verifier-owned.ps1',
    '-Vendor','mariadb','-ProfilePath',"/workspace/result/$profileLeaf",'-VerifierRunId',$VerifierRunId,'-Root','/workspace/cpf'
)
& docker @dockerArgs
if($LASTEXITCODE -ne 0){throw "FullLocal runtime DB cleanup failed exit=$LASTEXITCODE"}
Write-Host "CPF local runtime verifier DB cleanup PASS runId=$VerifierRunId"
