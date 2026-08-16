[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string]$SourceSha='',
    [string]$EvidenceRoot='build/cpf-db-verifier-owned',
    [string]$DockerImage='cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1',
    [string]$DockerNetwork='cpf_default'
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'Verifier-owned DB lifecycle requires pwsh 7+.'}
$rootPath=(Resolve-Path -LiteralPath $Root).Path
$evidence=if([IO.Path]::IsPathRooted($EvidenceRoot)){[IO.Path]::GetFullPath($EvidenceRoot)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $EvidenceRoot))}
[IO.Directory]::CreateDirectory($evidence)|Out-Null
if($SourceSha -notmatch '^[0-9a-f]{40}$'){throw 'Exact 40-char source content identity is required.'}
$adminSecret=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process')
if([string]::IsNullOrWhiteSpace($adminSecret)){throw 'CPF_ADMIN_PASSWORD is required from the FullLocal Docker secret environment.'}
$runId=([guid]::NewGuid().ToString('N').Substring(0,12)).ToLowerInvariant()
$approval="CPF-VERIFY-$runId";$operator='CPF_FULLLOCAL';$reason='cpf-full-local-isolated-db-lifecycle'
$profilePath=Join-Path $evidence "database-install.$Vendor.$runId.json"
$baseProfile=Get-Content -LiteralPath (Join-Path $rootPath 'cpf-tools/db/config/database-install.default.json') -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
$baseProfile.profileName="full-local-$Vendor-$runId";$baseProfile.environment='test'
$baseProfile.description='FullLocal verifier-owned isolated DB lifecycle profile. Contains env references only.'
$baseProfile.policy.allowMixedHostConfiguration=$false
$service=@{mariadb='mariadb';postgresql='postgresql';oracle='oracle'}[$Vendor]
$port=@{mariadb=3306;postgresql=5432;oracle=1521}[$Vendor]
$adminUser=@{mariadb='root';postgresql='postgres';oracle='system'}[$Vendor]
$platformDb="cpf_verify_${runId}_platform";$bzaDb="cpf_verify_${runId}_bza"
$platformMig="cpfv_${runId}_pm";$platformRun="cpfv_${runId}_pr";$bzaMig="cpfv_${runId}_bm";$bzaRun="cpfv_${runId}_br"
if($Vendor -eq 'oracle'){$platformMig=$platformMig.ToUpperInvariant();$platformRun=$platformRun.ToUpperInvariant();$bzaMig=$bzaMig.ToUpperInvariant();$bzaRun=$bzaRun.ToUpperInvariant()}
foreach($prop in @($baseProfile.modules.PSObject.Properties)){
    $key=[string]$prop.Name;$m=$prop.Value
    $m.enabled=($key -in @('core','common','admin','bizAdmin','batch'))
    $m.required=($key -eq 'core')
    $m.vendor=$Vendor;$m.host=$service;$m.port=$port;$m.clientPath='';$m.sslMode='disabled'
    $isBza=($key -eq 'bizAdmin')
    if($Vendor -eq 'oracle'){
        $m.databaseName='FREEPDB1'
        $m.schemaName=if($isBza){$bzaMig}else{$platformMig}
    }else{
        $m.databaseName=if($isBza){$bzaDb}else{$platformDb}
        $m.schemaName=if($Vendor -eq 'postgresql'){if($isBza){"cpfv_${runId}_bs"}else{"cpfv_${runId}_ps"}}else{$m.databaseName}
    }
    $m.admin.username=$adminUser;$m.admin.userHost='%';$m.admin.password=[pscustomobject]@{env='CPF_VERIFY_DB_ADMIN_PASSWORD'}
    $m.migration.username=if($isBza){$bzaMig}else{$platformMig};$m.migration.userHost='%';$m.migration.password=[pscustomobject]@{env='CPF_VERIFY_DB_MIGRATION_PASSWORD'}
    $m.runtime.username=if($isBza){$bzaRun}else{$platformRun};$m.runtime.userHost='%';$m.runtime.password=[pscustomobject]@{env='CPF_VERIFY_DB_RUNTIME_PASSWORD'}
    $m.seed.product=$true;$m.seed.optionalSample=$false;$m.seed.test=$false
}
$baseProfile|ConvertTo-Json -Depth 100|Set-Content -LiteralPath $profilePath -Encoding UTF8
$oldAdmin=$env:CPF_VERIFY_DB_ADMIN_PASSWORD;$oldMig=$env:CPF_VERIFY_DB_MIGRATION_PASSWORD;$oldRun=$env:CPF_VERIFY_DB_RUNTIME_PASSWORD
$env:CPF_VERIFY_DB_ADMIN_PASSWORD=$adminSecret
$env:CPF_VERIFY_DB_MIGRATION_PASSWORD=('M!g_'+[guid]::NewGuid().ToString('N')+'9z')
$env:CPF_VERIFY_DB_RUNTIME_PASSWORD=('R!n_'+[guid]::NewGuid().ToString('N')+'8y')
$lifecycle=Join-Path $rootPath 'cpf-tools/db/tools/run-db-vendor-lifecycle.ps1'
$migration=Join-Path $rootPath 'cpf-tools/db/tools/invoke-platform-database-migration.ps1'
$cleanup=Join-Path $rootPath 'cpf-tools/db/verification/cleanup-cpf-db-verifier-owned.ps1'
$result=[ordered]@{schemaVersion=1;vendor=$Vendor;runId=$runId;sourceSha=$SourceSha;status='RUNNING';stages=@();profile=(Split-Path -Leaf $profilePath);sanitized=$true;startedAt=[DateTimeOffset]::UtcNow.ToString('o')}
function Invoke-Lifecycle([string]$Mode,[switch]$PreCurrent,[switch]$CurrentApplied){
    $stem=$Mode.ToLowerInvariant();$plan=Join-Path $evidence "$stem.plan.json";$res=Join-Path $evidence "$stem.result.json";$logs=Join-Path $evidence "$stem.logs"
    & $lifecycle -Vendor $Vendor -Mode $Mode -Root $rootPath -ProfilePath $profilePath -ClientAdapter Docker -DockerImage $DockerImage -DockerNetwork $DockerNetwork -LogDir $logs -LifecyclePlanPath $plan -ResultPath $res -VerifierOwnedDisposable -VerifierRunId $runId
    if($LASTEXITCODE -ne 0){throw "DB lifecycle plan failed mode=$Mode"}
    $planned=Get-Content -LiteralPath $res -Raw -Encoding UTF8|ConvertFrom-Json -Depth 50
    $args=@('-NoProfile','-File',$lifecycle,'-Vendor',$Vendor,'-Mode',$Mode,'-Root',$rootPath,'-ProfilePath',$profilePath,'-ClientAdapter','Docker','-DockerImage',$DockerImage,'-DockerNetwork',$DockerNetwork,'-LogDir',$logs,'-LifecyclePlanPath',$plan,'-ResultPath',$res,'-ConfirmExecute','-ConfirmApplicationsStopped','-ConfirmRollbackReady','-ExpectedLifecyclePlanSha256',[string]$planned.lifecyclePlanSha256,'-Operator',$operator,'-Reason',$reason,'-ApprovalReference',$approval,'-VerifierOwnedDisposable','-VerifierRunId',$runId)
    if($PreCurrent){$args+='-ConfirmPreCurrentFixture'}
    if($CurrentApplied){$args+='-ConfirmCurrentMigrationApplied'}
    if($Mode -ne 'FreshInstall'){
        $planJson=Get-Content -LiteralPath $plan -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
        $upgrade=@($planJson.stages|Where-Object direction -eq 'upgrade'|Select-Object -First 1)
        if($upgrade.Count -eq 0){$upgrade=@($planJson.stages|Where-Object stage -eq 'Reapply'|Select-Object -First 1)}
        if($upgrade.Count -gt 0){$args+=@('-ExpectedPlanSha256',[string]$upgrade[0].planSha256)}
        $rollback=@($planJson.stages|Where-Object direction -eq 'rollback'|Select-Object -First 1)
        if($rollback.Count -gt 0){$args+=@('-ExpectedRollbackPlanSha256',[string]$rollback[0].planSha256)}
    }
    & $pwsh @args
    if($LASTEXITCODE -ne 0){throw "DB lifecycle execution failed mode=$Mode"}
    $executed=Get-Content -LiteralPath $res -Raw -Encoding UTF8|ConvertFrom-Json -Depth 50
    if([string]$executed.status -cne 'SUCCEEDED'){throw "DB lifecycle status is not SUCCEEDED mode=$Mode status=$($executed.status)"}
    $script:result.stages+= [ordered]@{stage=$Mode;status='PASS';planSha256=[string]$executed.lifecyclePlanSha256}
}
function Invoke-PreCurrentRollback {
    $dry=Join-Path $evidence 'prepare-pre-current.dry-run.json';$applied=Join-Path $evidence 'prepare-pre-current.result.json'
    & $migration -Root $rootPath -ProfilePath $profilePath -Direction rollback -ResultPath $dry -DryRun
    if($LASTEXITCODE -ne 0){throw 'Pre-current rollback dry-run failed.'}
    $plan=Get-Content -LiteralPath $dry -Raw -Encoding UTF8|ConvertFrom-Json -Depth 50
    $execRoot=Join-Path $evidence 'prepare-pre-current-docker';[IO.Directory]::CreateDirectory($execRoot)|Out-Null
    Copy-Item -LiteralPath $profilePath -Destination (Join-Path $execRoot 'profile.json') -Force
    $dockerArgs=@('run','--rm','--network',$DockerNetwork,'--mount',"type=bind,source=$rootPath,target=/workspace/cpf,readonly",'--mount',"type=bind,source=$execRoot,target=/workspace/result",'--workdir','/workspace/cpf','--env','CPF_VERIFY_DB_ADMIN_PASSWORD','--env','CPF_VERIFY_DB_MIGRATION_PASSWORD','--env','CPF_VERIFY_DB_RUNTIME_PASSWORD',$DockerImage,'pwsh','-NoProfile','-File','/workspace/cpf/cpf-tools/db/tools/invoke-platform-database-migration.ps1','-Root','/workspace/cpf','-ProfilePath','/workspace/result/profile.json','-Direction','rollback','-ResultPath','/workspace/result/rollback.json','-Apply','-ConfirmApply','-ConfirmApplicationsStopped','-ConfirmRollbackReady','-ExpectedPlanSha256',[string]$plan.planSha256,'-Operator',$operator,'-Reason',$reason,'-ApprovalReference',$approval,'-VerifierOwnedDisposable','-VerifierRunId',$runId)
    & docker @dockerArgs
    if($LASTEXITCODE -ne 0){throw 'Pre-current rollback execution failed.'}
    $script:result.stages += [ordered]@{stage='PreparePreCurrentFixture';status='PASS';planSha256=[string]$plan.planSha256}
}
$pwsh=(Get-Command pwsh -ErrorAction Stop).Source
try{
    Invoke-Lifecycle 'FreshInstall'
    Invoke-PreCurrentRollback
    Invoke-Lifecycle 'Upgrade' -PreCurrent
    Invoke-Lifecycle 'RollbackReapply' -CurrentApplied
    $result.status='PASS'
}catch{
    $result.status='FAIL';$result.error=$_.Exception.Message;throw
}finally{
    try{
        $cleanupRoot=Join-Path $evidence 'cleanup';[IO.Directory]::CreateDirectory($cleanupRoot)|Out-Null
        Copy-Item -LiteralPath $profilePath -Destination (Join-Path $cleanupRoot 'profile.json') -Force
        $dockerArgs=@('run','--rm','--network',$DockerNetwork,'--mount',"type=bind,source=$rootPath,target=/workspace/cpf,readonly",'--mount',"type=bind,source=$cleanupRoot,target=/workspace/result",'--workdir','/workspace/cpf','--env','CPF_VERIFY_DB_ADMIN_PASSWORD','--env','CPF_VERIFY_DB_MIGRATION_PASSWORD','--env','CPF_VERIFY_DB_RUNTIME_PASSWORD',$DockerImage,'pwsh','-NoProfile','-File','/workspace/cpf/cpf-tools/db/verification/cleanup-cpf-db-verifier-owned.ps1','-Vendor',$Vendor,'-ProfilePath','/workspace/result/profile.json','-VerifierRunId',$runId,'-Root','/workspace/cpf')
        & docker @dockerArgs
        if($LASTEXITCODE -ne 0){throw "cleanup exit=$LASTEXITCODE"}
        $result.cleanup='PASS'
    }catch{$result.cleanup='FAIL';$result.cleanupError=$_.Exception.Message;if($result.status -eq 'PASS'){$result.status='FAIL'}}
    $result.finishedAt=[DateTimeOffset]::UtcNow.ToString('o')
    $result|ConvertTo-Json -Depth 50|Set-Content -LiteralPath (Join-Path $evidence 'verifier-owned-lifecycle.json') -Encoding UTF8
    $env:CPF_VERIFY_DB_ADMIN_PASSWORD=$oldAdmin;$env:CPF_VERIFY_DB_MIGRATION_PASSWORD=$oldMig;$env:CPF_VERIFY_DB_RUNTIME_PASSWORD=$oldRun
}
if($result.status -ne 'PASS'){throw "Verifier-owned DB lifecycle failed vendor=$Vendor"}
Write-Host "[CPF][DB][VERIFIER][PASS] vendor=$Vendor runId=$runId evidence=$evidence"
