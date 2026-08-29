[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$VerifierRunId,
    [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string]$EvidenceRoot='build/cpf-local-runtime-db',
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
$evidence=if([IO.Path]::IsPathRooted($EvidenceRoot)){[IO.Path]::GetFullPath($EvidenceRoot)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $EvidenceRoot))}
[IO.Directory]::CreateDirectory($evidence)|Out-Null
$profile=Get-Content -LiteralPath (Join-Path $rootPath 'cpf-tools/db/config/database-install.default.json') -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
$profile.profileName="full-local-runtime-$VerifierRunId";$profile.environment='test';$profile.description='FullLocal 1-WAS verifier-owned MariaDB profile; secrets are environment references only.'
$platformDb="cpf_verify_${VerifierRunId}_runtime";$backofficeDb="cpf_verify_${VerifierRunId}_mbw"
$platformMig="cpfv_${VerifierRunId}_pm";$platformRun="cpfv_${VerifierRunId}_pr";$backofficeMig="cpfv_${VerifierRunId}_bm";$backofficeRun="cpfv_${VerifierRunId}_br"
foreach($prop in @($profile.modules.PSObject.Properties)){
    $key=[string]$prop.Name;$m=$prop.Value
    $m.enabled=($key -in @('core','common','admin','batch','backoffice'))
    $m.required=($key -eq 'core');$m.vendor='mariadb';$m.host='mariadb';$m.port=3306;$m.clientPath='';$m.sslMode='disabled'
    $isBackoffice=($key -eq 'backoffice')
    $m.databaseName=if($isBackoffice){$backofficeDb}else{$platformDb};$m.schemaName=$m.databaseName
    $m.admin.username='root';$m.admin.userHost='%';$m.admin.password=[pscustomobject]@{env='CPF_ADMIN_PASSWORD'}
    $m.migration.username=if($isBackoffice){$backofficeMig}else{$platformMig};$m.migration.userHost='%';$m.migration.password=[pscustomobject]@{env='CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD'}
    $m.runtime.username=if($isBackoffice){$backofficeRun}else{$platformRun};$m.runtime.userHost='%';$m.runtime.password=[pscustomobject]@{env='CPF_LOCAL_RUNTIME_DB_PASSWORD'}
    $m.seed.product=$true;$m.seed.optionalSample=$false;$m.seed.test=$false
}
$profilePath=Join-Path $evidence 'profile.json';$profile|ConvertTo-Json -Depth 100|Set-Content -LiteralPath $profilePath -Encoding UTF8
$dockerArgs=@('run','--rm','--network',$DockerNetwork,'--mount',"type=bind,source=$rootPath,target=/workspace/cpf,readonly",'--mount',"type=bind,source=$evidence,target=/workspace/result",'--workdir','/workspace/cpf','--env','CPF_ADMIN_PASSWORD','--env','CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD','--env','CPF_LOCAL_RUNTIME_DB_PASSWORD',$DockerImage,'pwsh','-NoProfile','-File','/workspace/cpf/cpf-tools/db/tools/initialize-cpf-database.ps1','-Root','/workspace/cpf','-ProfilePath','/workspace/result/profile.json','-ResultDir','/workspace/result/install','-All','-SeedMode','product','-RequireRun')
& docker @dockerArgs
if($LASTEXITCODE -ne 0){throw "FullLocal runtime DB initialization failed exit=$LASTEXITCODE"}
$result=[ordered]@{schemaVersion=1;runId=$VerifierRunId;vendor='mariadb';host='127.0.0.1';port=3306;platformDatabase=$platformDb;platformRuntimeUser=$platformRun;backofficeDatabase=$backofficeDb;backofficeRuntimeUser=$backofficeRun;profile='profile.json';status='PASS';sanitized=$true}
$result|ConvertTo-Json -Depth 20|Set-Content -LiteralPath (Join-Path $evidence 'runtime-db.json') -Encoding UTF8
Write-Host "CPF local runtime verifier DB PASS runId=$VerifierRunId"
