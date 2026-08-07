[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-fA-F]{40}$')][string]$ExpectedHead,
    [string]$EvidenceDir='build/evidence/r6-release/dr'
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$root=(& git -C $PSScriptRoot rev-parse --show-toplevel).Trim()
if($LASTEXITCODE -ne 0){throw 'Repository root resolution failed'}
$head=(& git -C $root rev-parse HEAD).Trim().ToLowerInvariant()
if($head -ne $ExpectedHead.ToLowerInvariant()){throw "DR ExpectedHead mismatch expected=$ExpectedHead actual=$head"}
if((& git -C $root status --porcelain=v1 --untracked-files=all|Out-String).Trim()){throw 'DR qualification requires clean exact-SHA source tree'}
if([string]::IsNullOrWhiteSpace($env:CPF_BACKUP_ENCRYPTION_KEY_B64)){throw 'CPF_BACKUP_ENCRYPTION_KEY_B64 is required for encrypted DR qualification'}
$out=if([IO.Path]::IsPathRooted($EvidenceDir)){$EvidenceDir}else{Join-Path $root $EvidenceDir}
New-Item -ItemType Directory -Force -Path $out|Out-Null
$rows=[Collections.Generic.List[object]]::new()
function Need([string]$Name){$v=[Environment]::GetEnvironmentVariable($Name);if([string]::IsNullOrWhiteSpace($v)){throw "$Name is required"};$v.Trim()}
function Optional([string]$Name,[string]$Default=''){$v=[Environment]::GetEnvironmentVariable($Name);if([string]::IsNullOrWhiteSpace($v)){$Default}else{$v.Trim()}}
function Port([string]$Name,[int]$Default){$v=Optional $Name; if(-not $v){return $Default};$n=0;if((-not [int]::TryParse($v,[ref]$n)) -or $n -lt 1 -or $n -gt 65535){throw "$Name must be a valid port"};$n}
function Assert-QualificationDb([string]$Name){if($Name-notmatch'^cpf_r6_[A-Za-z0-9_]+$'){throw "DR database must be a dedicated cpf_r6_* qualification database: $Name"};$Name}
function Invoke-Checked([string]$Id,[string]$Script,[hashtable]$Params){
  $started=[DateTimeOffset]::UtcNow
  try{& $Script @Params;if($LASTEXITCODE -ne 0){throw "$Id failed exit=$LASTEXITCODE"};$status='PASS'}catch{$status='FAIL';throw}finally{$rows.Add([ordered]@{id=$Id;status=$status;startedAt=$started.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O')})|Out-Null}
}
$backupScript=Join-Path $root 'cpf-tools/scripts/backup-cpf-database.ps1'
$restoreScript=Join-Path $root 'cpf-tools/scripts/restore-cpf-database.ps1'
$verifyScript=Join-Path $root 'cpf-tools/scripts/verify-dr-restore.ps1'
foreach($p in @($backupScript,$restoreScript,$verifyScript)){if(-not(Test-Path -LiteralPath $p -PathType Leaf)){throw "DR lifecycle script missing: $p"}}
foreach($vendor in @('mariadb','postgresql','oracle')){
  $prefix='CPF_R6_DR_'+$vendor.ToUpperInvariant()
  $db=Assert-QualificationDb (Need ($prefix+'_DATABASE'))
  $host=Optional ($prefix+'_HOST') '127.0.0.1'
  $user=Need ($prefix+'_USER')
  $port=switch($vendor){'mariadb'{Port ($prefix+'_PORT') 3306};'postgresql'{Port ($prefix+'_PORT') 5432};default{Port ($prefix+'_PORT') 1521}}
  $vendorOut=Join-Path $out $vendor;New-Item -ItemType Directory -Force -Path $vendorOut|Out-Null
  $oldMysql=$env:MYSQL_PWD;$oldPg=$env:PGPASSWORD
  try{
    if($vendor -eq 'mariadb'){$env:MYSQL_PWD=Need ($prefix+'_PASSWORD')}
    if($vendor -eq 'postgresql'){$env:PGPASSWORD=Need ($prefix+'_PASSWORD')}
    $oracleId=if($vendor -eq 'oracle'){Need ($prefix+'_CONNECT_IDENTIFIER')}else{''}
    $oracleDir=if($vendor -eq 'oracle'){Need ($prefix+'_DIRECTORY_OBJECT')}else{''}
    $oracleBackupFile=if($vendor -eq 'oracle'){Need ($prefix+'_SERVER_ARTIFACT_FILE')}else{''}
    $oracleRestoreFile=if($vendor -eq 'oracle'){Need ($prefix+'_SERVER_STAGING_FILE')}else{''}
    $backupParams=@{Vendor=$vendor;Database=$db;DatabaseHost=$host;Port=$port;User=$user;OutputDirectory=[IO.Path]::GetRelativePath($root,$vendorOut);Operator='cpf-r6-release';Reason='r6-dr-qualification';Root=$root}
    if($vendor -eq 'oracle'){$backupParams.OracleConnectIdentifier=$oracleId;$backupParams.OracleDirectoryObject=$oracleDir;$backupParams.OracleServerArtifactFile=$oracleBackupFile}
    Invoke-Checked "$vendor-backup" $backupScript $backupParams
    $backup=Get-ChildItem -LiteralPath $vendorOut -Filter '*.cpfbak' -File|Sort-Object LastWriteTimeUtc -Descending|Select-Object -First 1
    if(-not$backup){throw "$vendor backup artifact missing"}
    $restoreParams=@{Vendor=$vendor;Database=$db;BackupFile=$backup.FullName;Host=$host;Port=$port;User=$user;Operator='cpf-r6-release';Reason='r6-dr-qualification';ApprovalReference=('R6-'+$head);ConfirmRestore=$true;ReplaceExisting=$true;EvidenceDirectory=[IO.Path]::GetRelativePath($root,$vendorOut);Root=$root}
    if($vendor -eq 'oracle'){$restoreParams.OracleConnectIdentifier=$oracleId;$restoreParams.OracleDirectoryObject=$oracleDir;$restoreParams.OracleServerStagingFile=$oracleRestoreFile}
    Invoke-Checked "$vendor-restore" $restoreScript $restoreParams
    $verifyParams=@{Vendor=$vendor;Database=$db;Host=$host;Port=$port;User=$user;RunPlatformVerify=$true;Operator='cpf-r6-release';Reason='r6-dr-qualification';ApprovalReference=('R6-'+$head);EvidenceDirectory=[IO.Path]::GetRelativePath($root,$vendorOut);Root=$root}
    if($vendor -eq 'oracle'){$verifyParams.OracleConnectIdentifier=$oracleId}
    Invoke-Checked "$vendor-verify" $verifyScript $verifyParams
  } finally {$env:MYSQL_PWD=$oldMysql;$env:PGPASSWORD=$oldPg}
  if((& git -C $root rev-parse HEAD).Trim().ToLowerInvariant() -ne $head){throw 'Source SHA changed during DR qualification'}
}
$chaosPath=Join-Path $out 'dr-chaos.json'
& python (Join-Path $root 'cpf-tools/verification/final-dev/run-r6-dr-chaos-probe.py') --expected-head $head --output-json $chaosPath
if($LASTEXITCODE -ne 0){throw "DR chaos semantic probe failed: $LASTEXITCODE"}
$rows.Add([ordered]@{id='dr-chaos-semantic';status='PASS';startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O')})|Out-Null
$summary=[ordered]@{schemaVersion=1;protocol='CPF-R6-DR-QUALIFICATION-2';sourceSha=$head;vendors=@('mariadb','postgresql','oracle');status='PASS';steps=$rows;chaosEvidence=[IO.Path]::GetFileName($chaosPath);createdAt=[DateTimeOffset]::UtcNow.ToString('O')}
$summaryPath=Join-Path $out 'r6-dr-summary.json';$summary|ConvertTo-Json -Depth 8|Set-Content -LiteralPath $summaryPath -Encoding utf8NoBOM
Write-Host "[CPF][R6I][DR][PASS] sourceSha=$head vendors=3 evidence=$summaryPath"
