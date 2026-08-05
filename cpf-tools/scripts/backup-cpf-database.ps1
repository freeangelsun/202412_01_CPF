[CmdletBinding()]
param(
    [ValidateSet('mariadb','postgresql','oracle')][string]$Vendor='mariadb',
    [Parameter(Mandatory)][string]$Database,
    [Alias('Host')][string]$DatabaseHost='127.0.0.1',
    [int]$Port=0,
    [string]$User='root',
    [string]$Region='primary',
    [string]$OutputDirectory='cpf-docs/work/evidence/backup',
    [ValidateRange(1,3650)][int]$RetentionDays=35,
    [string]$EncryptionKeyEnvironmentVariable='CPF_BACKUP_ENCRYPTION_KEY_B64',
    [switch]$LegalHold,
    [string]$LegalHoldReason='',
    [string]$Operator=$env:USERNAME,
    [string]$Reason='scheduled-backup',
    [string]$OracleConnectIdentifier='',
    [string]$OracleDirectoryObject='',
    [string]$OracleServerArtifactFile='',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/scripts/database-profile-common.ps1')
. (Join-Path $rootPath 'cpf-tools/scripts/cpf-backup-lifecycle-common.ps1')
$Vendor=Assert-CpfSupportedDatabaseVendor $Vendor
$Database=Assert-CpfBackupIdentifier $Database 'Database'
$DatabaseHost=Assert-CpfBackupScalar $DatabaseHost 'DatabaseHost'
$User=Assert-CpfBackupScalar $User 'User'
$Region=Assert-CpfBackupScalar $Region 'Region'
$Operator=Assert-CpfBackupScalar $Operator 'Operator'
$Reason=Assert-CpfBackupScalar $Reason 'Reason'
if($LegalHold -and [string]::IsNullOrWhiteSpace($LegalHoldReason)){throw 'Legal Hold backup은 -LegalHoldReason이 필요합니다.'}
if($LegalHold){$LegalHoldReason=Assert-CpfBackupScalar $LegalHoldReason 'LegalHoldReason'}
if($Port -le 0){$Port=Get-CpfVendorDefaultPort $Vendor}
if($Port -lt 1 -or $Port -gt 65535){throw "Port 범위가 올바르지 않습니다: $Port"}
$plan=Get-Content (Join-Path $rootPath 'cpf-tools/config/database-source-plan.json') -Raw -Encoding UTF8|ConvertFrom-Json -Depth 20
if($plan.vendorSourceStatus.$Vendor.status -ne 'implemented'){throw "DB vendor '$Vendor'는 구현되지 않았습니다."}
$out=Join-Path $rootPath $OutputDirectory
New-Item -ItemType Directory -Force -Path $out|Out-Null
$started=(Get-Date).ToUniversalTime()
$backupId="CPF-$($started.ToString('yyyyMMddTHHmmssfffZ'))-$([guid]::NewGuid().ToString('N').Substring(0,12))"
$plain=Join-Path $out "$Database-$backupId.plain.tmp"
$artifact=Join-Path $out "$Database-$backupId.cpfbak"
$manifestPath="$artifact.manifest.json"
$stderr=Join-Path $out "$Database-$backupId.command.stderr.tmp"
$nativeFormat=''
$pitrCapable=$false
$auditPath=''
$backupCompleted=$false
try{
    switch($Vendor){
        'mariadb' {
            $tool=Get-Command mariadb-dump -ErrorAction SilentlyContinue
            if(-not $tool){throw 'mariadb-dump를 찾을 수 없습니다.'}
            $args=@('--single-transaction','--routines','--events','--triggers','--hex-blob','--skip-comments','--host',$DatabaseHost,'--port',"$Port",'--user',$User,$Database)
            $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardOutput $plain -RedirectStandardError $stderr
            if($process.ExitCode -ne 0){throw "backup command failed: vendor=mariadb exit=$($process.ExitCode)"}
            $nativeFormat='mariadb-logical-sql'
        }
        'postgresql' {
            $tool=Get-Command pg_dump -ErrorAction SilentlyContinue
            if(-not $tool){throw 'pg_dump를 찾을 수 없습니다.'}
            $args=@('--format=custom','--no-owner','--no-acl','--host',$DatabaseHost,'--port',"$Port",'--username',$User,'--file',$plain,$Database)
            $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardError $stderr
            if($process.ExitCode -ne 0){throw "backup command failed: vendor=postgresql exit=$($process.ExitCode)"}
            $nativeFormat='postgresql-custom'
        }
        'oracle' {
            $OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier'
            $OracleDirectoryObject=Assert-CpfBackupIdentifier $OracleDirectoryObject 'OracleDirectoryObject'
            if([string]::IsNullOrWhiteSpace($OracleServerArtifactFile)){throw 'Oracle backup은 Data Pump Directory와 공유되는 -OracleServerArtifactFile이 필요합니다.'}
            $serverFile=[IO.Path]::GetFullPath($OracleServerArtifactFile)
            $tool=Get-Command expdp -ErrorAction SilentlyContinue
            if(-not $tool){throw 'expdp를 찾을 수 없습니다.'}
            $dumpName=Split-Path -Leaf $serverFile
            if($dumpName -notmatch '^[A-Za-z0-9_.-]+$'){throw 'Oracle Data Pump dump file 이름이 안전하지 않습니다.'}
            $logName="$dumpName.log"
            $args=@("/@$OracleConnectIdentifier","DIRECTORY=$OracleDirectoryObject","DUMPFILE=$dumpName","LOGFILE=$logName",'FULL=Y','FLASHBACK_TIME=SYSTIMESTAMP','ENCRYPTION=ALL','ENCRYPTION_MODE=TRANSPARENT','REUSE_DUMPFILES=NO')
            $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardError $stderr
            if($process.ExitCode -ne 0){throw "backup command failed: vendor=oracle exit=$($process.ExitCode)"}
            if(-not (Test-Path -LiteralPath $serverFile -PathType Leaf)){throw 'Oracle Data Pump artifact를 공유 경로에서 찾을 수 없습니다.'}
            Move-Item -LiteralPath $serverFile -Destination $plain
            $nativeFormat='oracle-datapump-transparent-encryption'
        }
    }
    if(-not (Test-Path -LiteralPath $plain -PathType Leaf) -or (Get-Item -LiteralPath $plain).Length -le 0){throw 'backup plaintext artifact가 비어 있거나 생성되지 않았습니다.'}
    $crypto=Invoke-CpfBackupCrypto -Mode encrypt -RootPath $rootPath -InputPath $plain -OutputPath $artifact -KeyEnvironmentVariable $EncryptionKeyEnvironmentVariable
    $finished=(Get-Date).ToUniversalTime()
    $manifest=[ordered]@{
        schemaVersion=1; backupId=$backupId; status='COMPLETE'; vendor=$Vendor; database=$Database; primaryRegion=$Region
        backupType='LOGICAL_FULL'; nativeFormat=$nativeFormat; artifactFile=(Split-Path -Leaf $artifact)
        artifactSha256=[string]$crypto.artifactSha256; artifactSize=[int64]$crypto.artifactSize
        sourceSha256=[string]$crypto.sourceSha256; sourceSize=[int64]$crypto.sourceSize
        encrypted=$true; encryptionAlgorithm=[string]$crypto.algorithm; encryptionKeyReference=$EncryptionKeyEnvironmentVariable
        startedAt=$started.ToString('o'); finishedAt=$finished.ToString('o'); retentionUntil=$started.AddDays($RetentionDays).ToString('o')
        legalHold=[bool]$LegalHold; legalHoldReason=if($LegalHold){$LegalHoldReason}else{''}; legalHoldHistory=@()
        pitrCapable=$pitrCapable; recoveryAnchor=$started.ToString('o'); baseCommit=(Get-CpfGitHeadOrUnknown $rootPath)
        credentialEmbedded=$false; sanitized=$true; handling='RESTRICTED'; operator=$Operator; reason=$Reason
    }
    Write-CpfJsonAtomic $manifest $manifestPath
    $manifestHash=Write-CpfManifestHash $manifestPath
    $auditPath=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory 'cpf-docs/work/evidence/backup-governance' -Operation 'BACKUP' -Status 'PASS' -BackupId $backupId -Vendor $Vendor -Database $Database -Operator $Operator -Reason $Reason
    $backupCompleted=$true
    Write-Host "BACKUP_OK backupId=$backupId vendor=$Vendor artifact=$artifact artifactSha256=$($crypto.artifactSha256) manifestSha256=$manifestHash evidence=$auditPath"
} catch {
    $failureCode=$_.Exception.GetType().Name
    if(-not $backupCompleted){
        if(Test-Path -LiteralPath "$manifestPath.sha256"){Remove-Item -LiteralPath "$manifestPath.sha256" -Force -ErrorAction SilentlyContinue}
        if(Test-Path -LiteralPath $manifestPath){Remove-Item -LiteralPath $manifestPath -Force -ErrorAction SilentlyContinue}
        if(Test-Path -LiteralPath $artifact){Remove-Item -LiteralPath $artifact -Force -ErrorAction SilentlyContinue}
    }
    try{$auditPath=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory 'cpf-docs/work/evidence/backup-governance' -Operation 'BACKUP' -Status 'FAIL' -BackupId $backupId -Vendor $Vendor -Database $Database -Operator $Operator -Reason $Reason -ExitCode 1 -FailureCode $failureCode}catch{$auditPath='AUDIT_WRITE_FAILED'}
    throw "Backup failed. incomplete artifacts removed. failureCode=$failureCode sanitizedEvidence=$auditPath"
} finally {
    if(Test-Path -LiteralPath $plain){Remove-Item -LiteralPath $plain -Force -ErrorAction SilentlyContinue}
    if(Test-Path -LiteralPath $stderr){Remove-Item -LiteralPath $stderr -Force -ErrorAction SilentlyContinue}
}
