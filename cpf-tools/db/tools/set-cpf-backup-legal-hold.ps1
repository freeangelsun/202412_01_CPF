[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$BackupFile,
    [Parameter(Mandatory)][ValidateSet('apply','release')][string]$Action,
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [switch]$ConfirmChange,
    [string]$EvidenceDirectory='cpf-docs/work/evidence/backup-governance',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'
if(-not $ConfirmChange){throw 'Legal Hold 변경은 -ConfirmChange 명시가 필요합니다.'}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1')
$Operator=Assert-CpfBackupScalar $Operator 'Operator'; $Reason=Assert-CpfBackupScalar $Reason 'Reason'; $ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
$backup=(Resolve-Path $BackupFile).Path; $manifestPath="$backup.manifest.json"
[void](Assert-CpfManifestHash $manifestPath)
$m=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30
Assert-CpfBackupManifest $m
$expected=($Action -eq 'apply')
if([bool]$m.legalHold -eq $expected){throw "Legal Hold 상태가 이미 $expected 입니다."}
$history=@($m.legalHoldHistory)
$history+= [pscustomobject]@{action=$Action;operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;occurredAt=(Get-Date).ToUniversalTime().ToString('o')}
$m.legalHold=$expected; $m.legalHoldReason=if($expected){$Reason}else{''}; $m.legalHoldHistory=$history
Write-CpfJsonAtomic $m $manifestPath
$hash=Write-CpfManifestHash $manifestPath
$evidence=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation ("LEGAL_HOLD_"+$Action.ToUpperInvariant()) -Status 'PASS' -BackupId ([string]$m.backupId) -Vendor ([string]$m.vendor) -Database ([string]$m.database) -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference
Write-Host "LEGAL_HOLD_OK action=$Action backupId=$($m.backupId) manifestSha256=$hash evidence=$evidence"
