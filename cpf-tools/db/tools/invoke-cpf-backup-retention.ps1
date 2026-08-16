[CmdletBinding()]
param(
    [string]$BackupDirectory='cpf-docs/work/evidence/backup',
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [string]$ApprovalReference='',
    [string]$Approver='',
    [switch]$ConfirmPurge,
    [ValidatePattern('^$|^[0-9a-fA-F]{64}$')][string]$ExpectedPlanSha256='',
    [datetime]$AsOfUtc=(Get-Date).ToUniversalTime(),
    [string]$EvidenceDirectory='cpf-docs/work/evidence/backup-governance',
    [string]$ResultPath='build/db-backup-retention/retention-result.sanitized.json',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1')
$Operator=Assert-CpfBackupScalar $Operator 'Operator'
$Reason=Assert-CpfBackupScalar $Reason 'Reason'
if($ConfirmPurge){
    $ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
    $Approver=Assert-CpfBackupScalar $Approver 'Approver'
    if($Operator.Equals($Approver,[StringComparison]::OrdinalIgnoreCase)){throw '위험한 Purge 작업의 Operator와 Approver는 달라야 합니다.'}
}
$directory=Join-Path $rootPath $BackupDirectory
if(-not (Test-Path -LiteralPath $directory -PathType Container)){throw "Backup directory가 없습니다: $directory"}
$contract=Get-Content -LiteralPath (Join-Path $rootPath 'cpf-tools/db/cpf-backup-lifecycle-contract.json') -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30
$MinimumVerifiedReplicaCount=[int]$contract.crossRegionReplication.minimumVerifiedReplicaCountBeforePrimaryPurge
$out=Join-Path $rootPath $EvidenceDirectory
New-Item -ItemType Directory -Force -Path $out|Out-Null
$asOf=$AsOfUtc.ToUniversalTime()
$entries=@();$invalid=0
foreach($manifestItem in Get-ChildItem -LiteralPath $directory -File -Filter '*.cpfbak.manifest.json'|Sort-Object FullName){
    try{
        $manifestPath=$manifestItem.FullName
        [void](Assert-CpfManifestHash $manifestPath)
        $m=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30
        Assert-CpfBackupManifest $m
        $artifact=Join-Path $manifestItem.DirectoryName ([string]$m.artifactFile)
        $manifestHashSidecar="$manifestPath.sha256"
        if(-not (Test-Path -LiteralPath $artifact -PathType Leaf)){throw 'backup artifact가 없습니다.'}
        $actual=(Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash.ToLowerInvariant()
        if($actual -ne ([string]$m.artifactSha256).ToLowerInvariant()){throw 'backup artifact SHA-256 mismatch'}
        $verifiedRegions=@()
        foreach($replicaItem in Get-ChildItem -LiteralPath $manifestItem.DirectoryName -File -Filter "$(Split-Path -Leaf $artifact).replica.*.json"){
            [void](Assert-CpfManifestHash $replicaItem.FullName)
            $replica=Get-Content -LiteralPath $replicaItem.FullName -Raw -Encoding UTF8|ConvertFrom-Json -Depth 20
            if([string]$replica.status -ne 'VERIFIED'){throw 'Replica evidence status is not VERIFIED.'}
            if([string]$replica.backupId -ne [string]$m.backupId){throw 'Replica evidence backupId mismatch.'}
            if(([string]$replica.artifactSha256).ToLowerInvariant() -ne $actual){throw 'Replica evidence artifact SHA-256 mismatch.'}
            if(-not ([string]$replica.sourceRegion).Equals([string]$m.primaryRegion,[StringComparison]::OrdinalIgnoreCase)){throw 'Replica evidence source region mismatch.'}
            if(([string]$replica.targetRegion).Equals([string]$m.primaryRegion,[StringComparison]::OrdinalIgnoreCase)){throw 'Replica target region must differ from primary region.'}
            $verifiedRegions+=[string]$replica.targetRegion
        }
        $verifiedReplicaCount=@($verifiedRegions|Sort-Object -Unique).Count
        $expires=[datetime]::Parse([string]$m.retentionUntil).ToUniversalTime()
        if([bool]$m.legalHold){$decision='HELD'}
        elseif($expires -gt $asOf){$decision='RETAINED'}
        elseif($verifiedReplicaCount -lt $MinimumVerifiedReplicaCount){$decision='REPLICA_REQUIRED'}
        else{$decision='PURGE_CANDIDATE'}
        $entries+=[pscustomobject][ordered]@{
            backupId=[string]$m.backupId;vendor=[string]$m.vendor;database=[string]$m.database;decision=$decision
            legalHold=[bool]$m.legalHold;retentionUntil=[string]$m.retentionUntil;artifactSha256=$actual
            primaryRegion=[string]$m.primaryRegion;verifiedReplicaCount=$verifiedReplicaCount
            artifactPath=$artifact;manifestPath=$manifestPath;manifestHashSidecar=$manifestHashSidecar
        }
    } catch {
        $invalid++
        $entries+=[pscustomobject][ordered]@{backupId='UNKNOWN';decision='INVALID_FAIL_CLOSED';failureCode=$_.Exception.GetType().Name;artifactPath='';manifestPath='';manifestHashSidecar=''}
    }
}
$publicEntries=@($entries|ForEach-Object{[ordered]@{
    backupId=$_.backupId;vendor=$_.vendor;database=$_.database;decision=$_.decision;legalHold=$_.legalHold
    retentionUntil=$_.retentionUntil;artifactSha256=$_.artifactSha256;primaryRegion=$_.primaryRegion;verifiedReplicaCount=$_.verifiedReplicaCount
}})
$plan=[ordered]@{
    schemaVersion=1;operation='BACKUP_RETENTION_PURGE';asOfUtc=$asOf.ToString('o');backupDirectory=$BackupDirectory
    minimumVerifiedReplicaCount=$MinimumVerifiedReplicaCount;entries=$publicEntries;sanitized=$true
}
$planSha=Get-CpfObjectSha256 $plan
$result=[ordered]@{
    schemaVersion=1;operation='BACKUP_RETENTION';mode=if($ConfirmPurge){'EXECUTE'}else{'PLAN'}
    status=if($invalid -gt 0){'FAILED'}else{'PLANNED'};asOfUtc=$asOf.ToString('o')
    operator=$Operator;approver=$Approver;reason=$Reason;approvalReference=$ApprovalReference;planSha256=$planSha
    minimumVerifiedReplicaCount=$MinimumVerifiedReplicaCount;purged=0;reconcileRequired=$false;failureCode='';entries=$publicEntries;sanitized=$true
}
$resultAbsolute=if([IO.Path]::IsPathRooted($ResultPath)){[IO.Path]::GetFullPath($ResultPath)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $ResultPath))}
try{
    if($invalid -gt 0){throw "Backup retention validation failed closed. invalid=$invalid"}
    if($ConfirmPurge){
        if(@($entries|Where-Object{$_.decision -eq 'REPLICA_REQUIRED'}).Count -gt 0){throw 'Primary backup purge requires verified cross-region replica'}
        if($ExpectedPlanSha256.ToLowerInvariant() -ne $planSha){throw "Reviewed retention plan SHA mismatch. current=$planSha"}
        $result.status='APPLYING'
        foreach($entry in @($entries|Where-Object{$_.decision -eq 'PURGE_CANDIDATE'})){
            $artifact=[string]$entry.artifactPath;$manifest=[string]$entry.manifestPath;$sidecar=[string]$entry.manifestHashSidecar
            $quarantine=Join-Path $directory (".cpf-purge-$($entry.backupId)-$([guid]::NewGuid().ToString('N'))")
            $intentPath=Join-Path $out ("purge-intent-$($entry.backupId)-$([guid]::NewGuid().ToString('N').Substring(0,8)).json")
            $intent=[ordered]@{
                schemaVersion=1;operation='PURGE';status='PURGE_APPLYING';backupId=[string]$entry.backupId
                operator=$Operator;approver=$Approver;reason=$Reason;approvalReference=$ApprovalReference;planSha256=$planSha
                quarantine=$quarantine;affectedFiles=@((Split-Path -Leaf $artifact),(Split-Path -Leaf $sidecar),(Split-Path -Leaf $manifest))
                movedFiles=@();deletionCommitted=$false;reconcileRequired=$true;failureCode=''
                startedAt=(Get-Date).ToUniversalTime().ToString('o');finishedAt='';sanitized=$true
            }
            Write-CpfJsonAtomic $intent $intentPath
            New-Item -ItemType Directory -Path $quarantine|Out-Null
            $moved=@();$deletionCommitted=$false
            try{
                foreach($source in @($artifact,$sidecar,$manifest)){
                    $destination=Join-Path $quarantine (Split-Path -Leaf $source)
                    Move-Item -LiteralPath $source -Destination $destination
                    $moved+=@([pscustomobject]@{source=$source;destination=$destination})
                    $intent.movedFiles+=Split-Path -Leaf $source
                    Write-CpfJsonAtomic $intent $intentPath
                }
                Remove-Item -LiteralPath $quarantine -Recurse -Force
                $deletionCommitted=$true
                $intent.deletionCommitted=$true;$intent.status='PURGED';$intent.reconcileRequired=$false;$intent.finishedAt=(Get-Date).ToUniversalTime().ToString('o')
                Write-CpfJsonAtomic $intent $intentPath
                $result.purged=[int]$result.purged+1
                [void](New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'PURGE' -Status 'PASS' -BackupId ([string]$entry.backupId) -Vendor ([string]$entry.vendor) -Database ([string]$entry.database) -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference -PlanSha256 $planSha -AffectedFiles $intent.affectedFiles)
            } catch {
                $failureCode=$_.Exception.GetType().Name
                $rollbackOk=-not $deletionCommitted
                if(-not $deletionCommitted){
                    foreach($item in @($moved|Sort-Object -Property destination -Descending)){
                        try{
                            if(Test-Path -LiteralPath $item.destination){Move-Item -LiteralPath $item.destination -Destination $item.source -Force}else{$rollbackOk=$false}
                        }catch{$rollbackOk=$false}
                    }
                    if(Test-Path -LiteralPath $quarantine){try{Remove-Item -LiteralPath $quarantine -Recurse -Force}catch{$rollbackOk=$false}}
                }
                $intent.status=if($rollbackOk){'PURGE_FAILED_ROLLED_BACK'}else{'PURGE_UNKNOWN'}
                $intent.reconcileRequired=-not $rollbackOk;$intent.failureCode=$failureCode;$intent.deletionCommitted=$deletionCommitted;$intent.finishedAt=(Get-Date).ToUniversalTime().ToString('o')
                Write-CpfJsonAtomic $intent $intentPath
                $result.status=if($rollbackOk){'FAILED'}else{'UNKNOWN'};$result.reconcileRequired=-not $rollbackOk;$result.failureCode=$failureCode
                if(-not $rollbackOk){
                    [void](New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'PURGE' -Status 'UNKNOWN' -BackupId ([string]$entry.backupId) -Vendor ([string]$entry.vendor) -Database ([string]$entry.database) -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference -ExitCode 1 -FailureCode $failureCode -ReconcileRequired $true -PlanSha256 $planSha -AffectedFiles $intent.affectedFiles)
                }
                throw
            }
        }
        $result.status='COMPLETED'
    }
} catch {
    if($result.status -in @('PLANNED','APPLYING')){$result.status='FAILED'}
    if([string]::IsNullOrWhiteSpace([string]$result.failureCode)){$result.failureCode=$_.Exception.GetType().Name}
    throw
} finally {
    Write-CpfJsonAtomic $result $resultAbsolute
    Write-Host "BACKUP_RETENTION_RESULT mode=$($result.mode) status=$($result.status) planSha256=$planSha purged=$($result.purged) result=$resultAbsolute"
}
