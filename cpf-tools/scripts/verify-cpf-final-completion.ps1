param(
 [string]$RepoRoot=(Resolve-Path "$PSScriptRoot\..\..").Path,
 [switch]$SkipFrontend,
 [switch]$SkipRuntime,
 [switch]$RunDatabaseLifecycle,
 [switch]$RunGitHubGovernance
)
$ErrorActionPreference='Stop'
$gradle=if($IsWindows){'.\gradlew.bat'}else{'./gradlew'}
function Gate([string]$Name,[scriptblock]$Body){Write-Host "==> $Name";&$Body;if($LASTEXITCODE-ne0){throw "$Name failed"}}
Push-Location $RepoRoot
try{
 if(Test-Path 'cpf-batch\src'){throw 'Legacy executable cpf-batch/src still exists.'}
 if(Test-Path 'cpf-external'){throw 'EXS fixed module regression detected.'}
 if(Test-Path 'cpf-tools\db\source'){throw 'standalone cpf-tools/db/source regression detected.'}
 $bad=Get-ChildItem -Recurse -File -Include *.java,*.gradle,*.kts |
   Where-Object {$_.FullName-notmatch '[\\/]cpf-core[\\/]'} |
   Select-String -Pattern 'com\.cpf\.core\.common\.'
 if($bad){$bad|Format-Table Path,LineNumber,Line -AutoSize;throw 'Public API/SPI boundary regression.'}

 Gate 'Final source architecture gates' {& $gradle verifyCpfFinalSourceGates --no-daemon}
 Gate 'Java 25 standard' {& $gradle checkJava25Standard --no-daemon}
 Gate 'Full Java tests' {& $gradle clean test --no-daemon}
 Gate 'Domain federation static gate' {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\generator\verify-domain-federation.ps1}
 Gate 'SQL canonical/static synchronization gate' {& $gradle checkSqlCanonical --no-daemon}

 if(-not$SkipFrontend){
   Gate 'ADM frontend' {& $gradle :cpf-admin:frontendVerify --no-daemon}
   Gate 'BZA frontend' {& $gradle :cpf-biz-admin:frontendVerify --no-daemon}
 }
 if($RunDatabaseLifecycle){
   Gate 'Database artifact synchronization' {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1}
   Write-Host 'Run environment-specific MariaDB fresh/upgrade/rollback/reapply command set and preserve raw Evidence.'
 }
 if(-not$SkipRuntime){
   Gate 'BAT local distributed topology' {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\start-bat-local-distributed.ps1}
   try{
     $registry=Get-Content '.\build\bat-local-runtime\process-registry.json' -Raw|ConvertFrom-Json
     foreach($role in @('CONTROL_SERVER','SCHEDULER','WORKER','CENTER_CUT_RUNNER')){
       if(@($registry|Where-Object {$_.role-eq$role}).Count-lt2){throw "$role instance count < 2"}
     }
   } finally {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1}
   Gate 'BAT two-worker lease/drain/crash scenario' {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-bat-two-worker-runtime.ps1}
 }
 if($RunGitHubGovernance){Gate 'GitHub branch/source governance' {& pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-github-governance.ps1}}
 Write-Host 'Selected CPF completion gates PASS.'
 Write-Host 'Browser E2E, real remote Host Agent, commercial release signing/CVE, MariaDB lifecycle and fault injection are PASS only when their environment Evidence is actually produced.'
} finally {Pop-Location}
