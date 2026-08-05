[CmdletBinding()]
param(
    [ValidateSet('mariadb','postgresql','oracle')][string]$Vendor='mariadb',
    [Parameter(Mandatory)][string]$Database,
    [Parameter(Mandatory)][string]$BackupFile,
    [Alias('DatabaseHost')][string]$Host='127.0.0.1',
    [int]$Port=0,
    [string]$User='root',
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [string]$EncryptionKeyEnvironmentVariable='CPF_BACKUP_ENCRYPTION_KEY_B64',
    [switch]$ConfirmRestore,
    [switch]$ReplaceExisting,
    [datetime]$TargetTimeUtc,
    [string]$OracleConnectIdentifier='',
    [string]$OracleDirectoryObject='',
    [string]$OracleServerStagingFile='',
    [string]$EvidenceDirectory='cpf-docs/work/evidence/dr',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'
if(-not $ConfirmRestore){throw 'Restore는 -ConfirmRestore 명시가 필요합니다.'}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/scripts/database-profile-common.ps1')
. (Join-Path $rootPath 'cpf-tools/scripts/cpf-backup-lifecycle-common.ps1')
$Vendor=Assert-CpfSupportedDatabaseVendor $Vendor
$Database=Assert-CpfBackupIdentifier $Database 'Database'
$Host=Assert-CpfBackupScalar $Host 'Host'
$User=Assert-CpfBackupScalar $User 'User'
$Operator=Assert-CpfBackupScalar $Operator 'Operator'
$Reason=Assert-CpfBackupScalar $Reason 'Reason'
$ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
if($Port -le 0){$Port=Get-CpfVendorDefaultPort $Vendor}
$backup=(Resolve-Path $BackupFile).Path
$manifestPath="$backup.manifest.json"
if(-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)){throw 'backup manifest가 없습니다. Manifest 없는 Legacy 복원은 금지됩니다.'}
[void](Assert-CpfManifestHash $manifestPath)
$m=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30
Assert-CpfBackupManifest $m
if([string]$m.vendor -ne $Vendor){throw "manifest vendor mismatch: $($m.vendor) != $Vendor"}
if([string]$m.database -ne $Database){throw "manifest database mismatch: $($m.database) != $Database"}
$actual=(Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash.ToLowerInvariant()
if($actual -ne ([string]$m.artifactSha256).ToLowerInvariant()){throw 'backup artifact SHA-256 mismatch'}
if($PSBoundParameters.ContainsKey('TargetTimeUtc') -and -not [bool]$m.pitrCapable){throw '해당 logical backup은 PITR capable artifact가 아닙니다. Vendor native PITR pack을 사용하십시오.'}
$plain=Join-Path ([IO.Path]::GetTempPath()) ("cpf-restore-$($m.backupId)-$([guid]::NewGuid().ToString('N')).tmp")
$auditPath=''
$executionStarted=$false
try{
    $crypto=Invoke-CpfBackupCrypto -Mode decrypt -RootPath $rootPath -InputPath $backup -OutputPath $plain -KeyEnvironmentVariable $EncryptionKeyEnvironmentVariable
    if(([string]$crypto.decryptedSha256).ToLowerInvariant() -ne ([string]$m.sourceSha256).ToLowerInvariant()){throw 'decrypted source SHA-256 mismatch'}
    switch($Vendor){
        'mariadb' {
            $tool=Get-Command mariadb -ErrorAction SilentlyContinue
            if(-not $tool){throw 'mariadb client를 찾을 수 없습니다.'}
            $args=@('--host',$Host,'--port',"$Port",'--user',$User,$Database)
            $executionStarted=$true
            $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardInput $plain
            if($process.ExitCode -ne 0){throw "restore command failed: vendor=mariadb exit=$($process.ExitCode)"}
        }
        'postgresql' {
            $tool=Get-Command pg_restore -ErrorAction SilentlyContinue
            if(-not $tool){throw 'pg_restore를 찾을 수 없습니다.'}
            $args=@('--exit-on-error','--no-owner','--no-acl','--host',$Host,'--port',"$Port",'--username',$User,'--dbname',$Database)
            if($ReplaceExisting){$args+=@('--clean','--if-exists')}
            $args+=$plain
            $executionStarted=$true
            $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru
            if($process.ExitCode -ne 0){throw "restore command failed: vendor=postgresql exit=$($process.ExitCode)"}
        }
        'oracle' {
            $OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier'
            $OracleDirectoryObject=Assert-CpfBackupIdentifier $OracleDirectoryObject 'OracleDirectoryObject'
            if([string]::IsNullOrWhiteSpace($OracleServerStagingFile)){throw 'Oracle restore는 Data Pump Directory와 공유되는 -OracleServerStagingFile이 필요합니다.'}
            $staging=[IO.Path]::GetFullPath($OracleServerStagingFile)
            if(Test-Path -LiteralPath $staging){throw 'Oracle staging artifact가 이미 존재합니다.'}
            Copy-Item -LiteralPath $plain -Destination $staging
            try{
                $tool=Get-Command impdp -ErrorAction SilentlyContinue
                if(-not $tool){throw 'impdp를 찾을 수 없습니다.'}
                $dumpName=Split-Path -Leaf $staging
                if($dumpName -notmatch '^[A-Za-z0-9_.-]+$'){throw 'Oracle Data Pump staging file 이름이 안전하지 않습니다.'}
                $args=@("/@$OracleConnectIdentifier","DIRECTORY=$OracleDirectoryObject","DUMPFILE=$dumpName","LOGFILE=$dumpName.restore.log",'FULL=Y')
                if($ReplaceExisting){$args+='TABLE_EXISTS_ACTION=REPLACE'}else{$args+='TABLE_EXISTS_ACTION=SKIP'}
                $executionStarted=$true
                $process=Start-Process -FilePath $tool.Source -ArgumentList $args -NoNewWindow -Wait -PassThru
                if($process.ExitCode -ne 0){throw "restore command failed: vendor=oracle exit=$($process.ExitCode)"}
            } finally {
                if(Test-Path -LiteralPath $staging){Remove-Item -LiteralPath $staging -Force -ErrorAction SilentlyContinue}
            }
        }
    }
    $auditPath=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'RESTORE' -Status 'PASS' -BackupId ([string]$m.backupId) -Vendor $Vendor -Database $Database -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference
    Write-Host "RESTORE_OK backupId=$($m.backupId) vendor=$Vendor database=$Database evidence=$auditPath"
} catch {
    $status=if($executionStarted){'UNKNOWN'}else{'FAIL'}
    $auditPath=New-CpfBackupAuditEvidence -RootPath $rootPath -EvidenceDirectory $EvidenceDirectory -Operation 'RESTORE' -Status $status -BackupId ([string]$m.backupId) -Vendor $Vendor -Database $Database -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference -ExitCode 1 -FailureCode $_.Exception.GetType().Name -ReconcileRequired $executionStarted -AffectedFiles @((Split-Path -Leaf $backup))
    throw "Restore failed. status=$status reconcileRequired=$executionStarted sanitizedEvidence=$auditPath"
} finally {
    if(Test-Path -LiteralPath $plain){Remove-Item -LiteralPath $plain -Force -ErrorAction SilentlyContinue}
}
