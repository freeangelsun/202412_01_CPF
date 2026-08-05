[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$BackupFile,
    [Parameter(Mandatory)][string]$TargetDirectory,
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9][A-Za-z0-9._-]{0,62}$')][string]$TargetRegion,
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [switch]$ConfirmReplication,
    [string]$EvidenceDirectory='cpf-docs/work/evidence/backup-governance',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'
if(-not $ConfirmReplication){throw 'Cross-region backup replication requires -ConfirmReplication.'}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/scripts/cpf-backup-lifecycle-common.ps1')
$Operator=Assert-CpfBackupScalar $Operator 'Operator';$Reason=Assert-CpfBackupScalar $Reason 'Reason';$ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
$backup=(Resolve-Path $BackupFile).Path;$manifestPath="$backup.manifest.json";$manifestHashPath="$manifestPath.sha256"
[void](Assert-CpfManifestHash $manifestPath)
$m=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30
Assert-CpfBackupManifest $m
if([string]$m.primaryRegion -eq $TargetRegion){throw 'TargetRegion must differ from the backup primaryRegion.'}
$actual=(Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash.ToLowerInvariant()
if($actual -ne ([string]$m.artifactSha256).ToLowerInvariant()){throw 'Source backup artifact SHA-256 mismatch.'}
$sourceEvidence="$backup.replica.$TargetRegion.json"
if(Test-Path -LiteralPath $sourceEvidence){throw "Replica region evidence already exists: $TargetRegion"}
$target=[IO.Path]::GetFullPath($TargetDirectory);New-Item -ItemType Directory -Force -Path $target|Out-Null
$names=@((Split-Path -Leaf $backup),(Split-Path -Leaf $manifestPath),(Split-Path -Leaf $manifestHashPath))
foreach($name in $names){if(Test-Path -LiteralPath (Join-Path $target $name)){throw "Target artifact already exists: $name"}}
$stage=Join-Path $target (".cpf-replica-stage-$($m.backupId)-$([guid]::NewGuid().ToString('N'))")
New-Item -ItemType Directory -Path $stage|Out-Null
try{
    Copy-Item -LiteralPath $backup -Destination (Join-Path $stage $names[0])
    Copy-Item -LiteralPath $manifestPath -Destination (Join-Path $stage $names[1])
    Copy-Item -LiteralPath $manifestHashPath -Destination (Join-Path $stage $names[2])
    $targetArtifact=Join-Path $stage $names[0];$targetManifest=Join-Path $stage $names[1]
    if((Get-FileHash -LiteralPath $targetArtifact -Algorithm SHA256).Hash.ToLowerInvariant() -ne $actual){throw 'Replicated artifact SHA-256 mismatch.'}
    [void](Assert-CpfManifestHash $targetManifest)
    foreach($name in $names){Move-Item -LiteralPath (Join-Path $stage $name) -Destination (Join-Path $target $name)}
    $e=[ordered]@{schemaVersion=1;status='VERIFIED';backupId=[string]$m.backupId;vendor=[string]$m.vendor;database=[string]$m.database;sourceRegion=[string]$m.primaryRegion;targetRegion=$TargetRegion;artifactSha256=$actual;replicatedAt=(Get-Date).ToUniversalTime().ToString('o');operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;targetDirectory=$target;sanitized=$true}
    Write-CpfJsonAtomic $e $sourceEvidence;[void](Write-CpfManifestHash $sourceEvidence)
    $targetEvidence=Join-Path $target (Split-Path -Leaf $sourceEvidence);Write-CpfJsonAtomic $e $targetEvidence;[void](Write-CpfManifestHash $targetEvidence)
    $audit=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'REPLICATE' -Status 'PASS' -BackupId ([string]$m.backupId) -Vendor ([string]$m.vendor) -Database ([string]$m.database) -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference
    Write-Host "BACKUP_REPLICA_OK backupId=$($m.backupId) targetRegion=$TargetRegion artifactSha256=$actual evidence=$audit"
} catch {
    $failureCode=$_.Exception.GetType().Name
    try{[void](New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'REPLICATE' -Status 'FAIL' -BackupId ([string]$m.backupId) -Vendor ([string]$m.vendor) -Database ([string]$m.database) -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference -ExitCode 1 -FailureCode $failureCode)}catch{}
    throw "Backup replication failed. failureCode=$failureCode"
} finally {
    if(Test-Path -LiteralPath $stage){Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue}
}
