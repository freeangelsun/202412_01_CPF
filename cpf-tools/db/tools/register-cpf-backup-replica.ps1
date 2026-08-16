[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Root,
    [Parameter(Mandatory=$true)][string]$ManifestPath,
    [Parameter(Mandatory=$true)][string]$ReplicaArtifactPath,
    [Parameter(Mandatory=$true)][string]$ReplicaRegion,
    [Parameter(Mandatory=$true)][string]$Operator,
    [Parameter(Mandatory=$true)][string]$Reason,
    [Parameter(Mandatory=$true)][string]$ApprovalReference,
    [string]$EvidenceDirectory='cpf-docs/evidence/runtime/backup-replica'
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$rootPath=(Resolve-Path -LiteralPath $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1')
$manifestFile=(Resolve-Path -LiteralPath $ManifestPath).Path
$replicaFile=(Resolve-Path -LiteralPath $ReplicaArtifactPath).Path
Assert-CpfManifestHash -ManifestPath $manifestFile | Out-Null
$manifest=Get-Content -LiteralPath $manifestFile -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
Assert-CpfBackupManifest -Manifest $manifest
$operatorValue=Assert-CpfBackupScalar $Operator 'Operator'
$reasonValue=Assert-CpfBackupScalar $Reason 'Reason'
$approvalValue=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
$regionValue=Assert-CpfBackupScalar $ReplicaRegion 'ReplicaRegion'
if($regionValue -eq [string]$manifest.primaryRegion){throw 'Replica region must differ from primary region.'}
$primaryHash=([string]$manifest.artifactSha256).ToLowerInvariant()
if($primaryHash -notmatch '^[0-9a-f]{64}$'){throw 'Primary artifact SHA-256 is invalid.'}
$replicaHash=(Get-FileHash -LiteralPath $replicaFile -Algorithm SHA256).Hash.ToLowerInvariant()
if($replicaHash -ne $primaryHash){throw 'Replica artifact SHA-256 mismatch.'}
$now=(Get-Date).ToUniversalTime()
$record=[ordered]@{
    schemaVersion=1
    status='VERIFIED'
    backupId=[string]$manifest.backupId
    vendor=[string]$manifest.vendor
    database=[string]$manifest.database
    primaryRegion=[string]$manifest.primaryRegion
    replicaRegion=$regionValue
    replicaArtifactFile=$replicaFile
    primaryArtifactSha256=$primaryHash
    replicaArtifactSha256=$replicaHash
    operator=$operatorValue
    reason=$reasonValue
    approvalReference=$approvalValue
    sourceSha=(Get-CpfGitHeadOrUnknown $rootPath)
    verifiedAt=$now.ToString('o')
    sanitized=$true
}
$out=Join-Path $rootPath $EvidenceDirectory
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sidecar=Join-Path $out ("replica-{0}-{1}.verified.json" -f $manifest.backupId,([guid]::NewGuid().ToString('N').Substring(0,8)))
Write-CpfJsonAtomic -Value $record -Path $sidecar
Write-CpfManifestHash -ManifestPath $sidecar | Out-Null
New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'REGISTER_REPLICA' -Status 'VERIFIED' -BackupId ([string]$manifest.backupId) -Vendor ([string]$manifest.vendor) -Database ([string]$manifest.database) -Operator $operatorValue -Reason $reasonValue -ApprovalReference $approvalValue -AffectedFiles @($replicaFile,$sidecar) | Out-Null
Write-Host "[CPF][BACKUP][REPLICA][PASS] $sidecar"
