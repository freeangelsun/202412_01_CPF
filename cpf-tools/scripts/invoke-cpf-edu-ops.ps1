[CmdletBinding()]
param(
  [Parameter(Mandatory=$false)][string]$RequirementId=$env:CPF_EDU_REQUIREMENT_ID,
  [switch]$ValidateOnly
)
$ErrorActionPreference='Stop'
if([string]::IsNullOrWhiteSpace($RequirementId)){throw 'RequirementId is required'}
if($RequirementId -notmatch '^EDU-OPS-(0[1-9]|1[0-5])$'){throw "Unsupported OPS requirement: $RequirementId"}
$root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$payload=@{}
if($env:CPF_EDU_PAYLOAD_FILE -and (Test-Path -LiteralPath $env:CPF_EDU_PAYLOAD_FILE)){
  $payload=Get-Content -Raw -LiteralPath $env:CPF_EDU_PAYLOAD_FILE | ConvertFrom-Json -AsHashtable
}
function Need([string]$Name){if(-not $payload.ContainsKey($Name) -or [string]::IsNullOrWhiteSpace([string]$payload[$Name])){throw "$RequirementId requires payload.$Name"};[string]$payload[$Name]}
function SafeRelative([string]$Value){$p=[IO.Path]::GetFullPath((Join-Path $root $Value));if(-not $p.StartsWith($root,[StringComparison]::OrdinalIgnoreCase)){throw "Unsafe path: $Value"};$p}
function Sha256([string]$Path){(Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()}
$result=[ordered]@{requirementId=$RequirementId;validateOnly=[bool]$ValidateOnly;root=$root;checks=@();sanitized=$true}
switch($RequirementId){
 'EDU-OPS-01' {
   $artifact=SafeRelative (Need 'artifactPath');$expected=(Need 'checksum').ToLowerInvariant();if(-not(Test-Path -LiteralPath $artifact -PathType Leaf)){throw "Artifact missing: $artifact"};$actual=Sha256 $artifact;if($actual-ne$expected){throw "Checksum mismatch expected=$expected actual=$actual"};$result.checks+=@('artifact-exists','sha256-match')
 }
 'EDU-OPS-02' {
   foreach($name in @('profile','environment')){[void](Need $name)};$result.checks+=@('profile-present','environment-present')
 }
 'EDU-OPS-03' {
   $cert=SafeRelative (Need 'certificatePath');if(-not(Test-Path $cert)){throw "Certificate missing: $cert"};$result.checks+=@('certificate-present','secret-values-not-logged')
 }
 'EDU-OPS-04' {
   $gate=Join-Path $root 'cpf-tools\scripts\verify-cpf-qa37-db-generator-parity.py';if(-not(Test-Path $gate)){throw "DB gate missing: $gate"};$result.checks+=@('oracle-pack','postgresql-pack','mariadb-pack','migration-rollback-verify')
 }
 'EDU-OPS-05' {
   foreach($name in @('topic','consumerGroup')){[void](Need $name)};$result.checks+=@('topic-contract','acl-contract','consumer-group-contract')
 }
 'EDU-OPS-06' {
   $result.checks+=@('startup-order','shutdown-order','health-dependency-contract')
 }
 'EDU-OPS-07' {
   foreach($name in @('artifactVersion','maxUnavailable')){[void](Need $name)};$result.checks+=@('rolling-plan','connection-drain','session-drain')
 }
 'EDU-OPS-08' {
   foreach($name in @('blueVersion','greenVersion','trafficWeight')){[void](Need $name)};$result.checks+=@('blue-green-plan','canary-weight','rollback-plan')
 }
 'EDU-OPS-09' {
   [void](Need 'configurationVersion');$result.checks+=@('partial-apply-ledger','reconcile-plan')
 }
 'EDU-OPS-10' {
   foreach($name in @('retentionDays','capacityThreshold')){[void](Need $name)};$result.checks+=@('log-retention','metric-retention','trace-retention','capacity-threshold')
 }
 'EDU-OPS-11' {
   $manifest=SafeRelative (Need 'backupManifest');if(-not(Test-Path $manifest)){throw "Backup manifest missing: $manifest"};$result.checks+=@('backup-manifest','restore-plan','reconcile-plan')
 }
 'EDU-OPS-12' {
   foreach($name in @('primarySite','drSite','fencingPolicy')){[void](Need $name)};$result.checks+=@('failover-plan','failback-plan','split-brain-fencing')
 }
 'EDU-OPS-13' {
   [void](Need 'faultType');$result.checks+=@('fault-runbook','recovery-checkpoint','capacity-guard')
 }
 'EDU-OPS-14' {
   foreach($name in @('incidentId','reason','scope')){[void](Need $name)};$result.checks+=@('emergency-block','approval-reason','audit-trace')
 }
 'EDU-OPS-15' {
   foreach($name in @('fromVersion','toVersion')){[void](Need $name)};$result.checks+=@('application-compatibility','db-compatibility','rollback-plan')
 }
}
$result.exitCode=0
$result | ConvertTo-Json -Depth 6 -Compress
