[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$ManifestPath,
    [Parameter(Mandatory)][datetime]$TargetTimeUtc,
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [switch]$PlanOnly,
    [switch]$Execute,
    [switch]$ConfirmApplicationsStopped,
    [switch]$ConfirmIsolatedTarget,
    [string]$ExpectedPlanSha256='',
    [string]$EncryptionKeyEnvironmentVariable='CPF_BACKUP_ENCRYPTION_KEY_B64',
    [string]$Host='127.0.0.1',
    [int]$Port=0,
    [string]$User='root',
    [string]$TargetDataDirectory='',
    [string]$PostgreSqlRestoreCommandTemplate='',
    [ValidateRange(30,7200)][int]$RecoveryTimeoutSeconds=900,
    [string]$OracleConnectIdentifier='',
    [string]$OracleCatalogDirectory='',
    [string]$ResultPath='build/db-pitr/pitr-result.sanitized.json',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'

# Child/native DB process에서도 UTF-8 계약을 유지합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $CpfUtf8ChildJavaOptions
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding=UTF-8(?:\s|$)') {
    $env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS.Trim() + ' ' + $CpfUtf8ChildJavaOptions)
}
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:PGCLIENTENCODING = 'UTF8'
$env:NLS_LANG = '.AL32UTF8'
if($PlanOnly -and $Execute){throw '-PlanOnly과 -Execute를 동시에 지정할 수 없습니다.'}
if(-not $PlanOnly -and -not $Execute){$PlanOnly=$true}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/database-profile-common.ps1')
. (Join-Path $rootPath 'cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1')
$Operator=Assert-CpfBackupScalar $Operator 'Operator';$Reason=Assert-CpfBackupScalar $Reason 'Reason';$ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
$manifestAbsolute=(Resolve-Path $ManifestPath).Path
$m=Get-Content -LiteralPath $manifestAbsolute -Raw -Encoding UTF8|ConvertFrom-Json -Depth 40
foreach($field in @('schemaVersion','vendor','database','recoveryWindowStart','recoveryWindowEnd','artifacts')){if($null -eq $m.PSObject.Properties[$field]){throw "PITR manifest required field가 없습니다: $field"}}
if([int]$m.schemaVersion -ne 1){throw '지원하지 않는 PITR manifest version입니다.'}
$vendor=Assert-CpfSupportedDatabaseVendor ([string]$m.vendor)
$database=Assert-CpfBackupIdentifier ([string]$m.database) 'manifest.database'
$windowStart=[datetime]::Parse([string]$m.recoveryWindowStart).ToUniversalTime();$windowEnd=[datetime]::Parse([string]$m.recoveryWindowEnd).ToUniversalTime();$target=$TargetTimeUtc.ToUniversalTime()
if($windowStart -gt $windowEnd){throw 'PITR recovery window가 역전되었습니다.'}
if($target -lt $windowStart -or $target -gt $windowEnd){throw "TargetTimeUtc가 recovery window 밖입니다: target=$($target.ToString('o'))"}
$manifestDir=Split-Path -Parent $manifestAbsolute;$resolved=@();$seenSequence=@{}
foreach($a in @($m.artifacts)){
    foreach($field in @('kind','sequence','path','sha256')){if($null -eq $a.PSObject.Properties[$field]){throw "PITR artifact required field가 없습니다: $field"}}
    $sequence=[int]$a.sequence;if($sequence -lt 0 -or $seenSequence.ContainsKey($sequence)){throw "PITR artifact sequence가 중복 또는 음수입니다: $sequence"};$seenSequence[$sequence]=$true
    $path=if([IO.Path]::IsPathRooted([string]$a.path)){[IO.Path]::GetFullPath([string]$a.path)}else{[IO.Path]::GetFullPath((Join-Path $manifestDir ([string]$a.path)))}
    if(-not (Test-Path -LiteralPath $path -PathType Leaf)){throw "PITR artifact가 없습니다: $path"}
    $expected=([string]$a.sha256).ToLowerInvariant();if($expected -notmatch '^[0-9a-f]{64}$'){throw 'PITR artifact SHA-256 형식이 올바르지 않습니다.'}
    $actual=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant();if($actual -ne $expected){throw "PITR artifact SHA-256 mismatch: $path"}
    $resolved+=[pscustomobject]@{kind=[string]$a.kind;sequence=$sequence;path=$path;sha256=$actual;fileName=(Split-Path -Leaf $path)}
}
$resolved=@($resolved|Sort-Object sequence)
$requiredKinds=switch($vendor){'mariadb'{@('binlog')};'postgresql'{@('base','wal')};'oracle'{@('rman-piece')}}
foreach($kind in $requiredKinds){if(-not @($resolved|Where-Object kind -eq $kind)){throw "PITR required artifact kind가 없습니다: vendor=$vendor kind=$kind"}}
$plan=[ordered]@{schemaVersion=1;vendor=$vendor;database=$database;targetTimeUtc=$target.ToString('o');recoveryWindowStart=$windowStart.ToString('o');recoveryWindowEnd=$windowEnd.ToString('o');artifacts=@($resolved|ForEach-Object{[ordered]@{kind=$_.kind;sequence=$_.sequence;fileName=$_.fileName;sha256=$_.sha256}});operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;sourceSha=(Get-CpfGitHeadOrUnknown $rootPath);sanitized=$true}
$planJson=$plan|ConvertTo-Json -Depth 30 -Compress;$planHash=([BitConverter]::ToString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($planJson)))).Replace('-','').ToLowerInvariant()
$result=[ordered]@{schemaVersion=2;operation='PITR';mode=if($Execute){'EXECUTE'}else{'PLAN'};status=if($Execute){'APPLYING'}else{'PLANNED'};sourceSha=(Get-CpfGitHeadOrUnknown $rootPath);vendor=$vendor;database=$database;targetTimeUtc=$target.ToString('o');planSha256=$planHash;operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;startedAt=(Get-Date).ToUniversalTime().ToString('o');finishedAt='';reconcileRequired=$false;failureCode='';outputSha256='';sanitized=$true}
$resultAbsolute=if([IO.Path]::IsPathRooted($ResultPath)){[IO.Path]::GetFullPath($ResultPath)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $ResultPath))}
try{
    if($Execute){
        if(-not $ConfirmApplicationsStopped){throw 'PITR Execute에는 -ConfirmApplicationsStopped가 필요합니다.'}
        if(-not $ConfirmIsolatedTarget){throw 'PITR Execute에는 -ConfirmIsolatedTarget이 필요합니다.'}
        if($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or $ExpectedPlanSha256.ToLowerInvariant() -ne $planHash){throw "검토한 PITR plan SHA와 현재 plan이 다릅니다: current=$planHash"}
        if($Port -le 0){$Port=Get-CpfVendorDefaultPort $vendor}
        $combinedOutput=''
        switch($vendor){
            'mariadb' {
                if($null -eq $m.PSObject.Properties['baseBackupFile']){throw 'MariaDB PITR manifest에는 baseBackupFile이 필요합니다.'}
                $base=if([IO.Path]::IsPathRooted([string]$m.baseBackupFile)){[string]$m.baseBackupFile}else{Join-Path $manifestDir ([string]$m.baseBackupFile)}
                & (Join-Path $rootPath 'cpf-tools/db/tools/restore-cpf-database.ps1') -Vendor mariadb -Database $database -BackupFile $base -Host $Host -Port $Port -User $User -Operator $Operator -Reason $Reason -ApprovalReference $ApprovalReference -EncryptionKeyEnvironmentVariable $EncryptionKeyEnvironmentVariable -ConfirmRestore -Root $rootPath
                if($LASTEXITCODE -notin @(0,$null)){throw "MariaDB base restore failed: exit=$LASTEXITCODE"}
                $binlog=Get-Command mariadb-binlog -ErrorAction SilentlyContinue;if(-not $binlog){throw 'mariadb-binlog를 찾을 수 없습니다.'}
                $client=Get-Command mariadb -ErrorAction SilentlyContinue;if(-not $client){throw 'mariadb client를 찾을 수 없습니다.'}
                $replay=Join-Path ([IO.Path]::GetTempPath()) ("cpf-pitr-$([guid]::NewGuid().ToString('N')).sql")
                try{
                    $args=@("--stop-datetime=$($target.ToString('yyyy-MM-dd HH:mm:ss'))")+@($resolved|Where-Object kind -eq 'binlog'|ForEach-Object path)
                    $p1=Start-Process -FilePath $binlog.Source -ArgumentList $args -NoNewWindow -Wait -PassThru -RedirectStandardOutput $replay
                    if($p1.ExitCode -ne 0){throw "mariadb-binlog replay generation failed: exit=$($p1.ExitCode)"}
                    $p2=Start-Process -FilePath $client.Source -ArgumentList @('--default-character-set=utf8mb4','--host',$Host,'--port',"$Port",'--user',$User,$database) -NoNewWindow -Wait -PassThru -RedirectStandardInput $replay
                    if($p2.ExitCode -ne 0){throw "MariaDB binlog replay failed: exit=$($p2.ExitCode)"}
                } finally {if(Test-Path -LiteralPath $replay){Remove-Item -LiteralPath $replay -Force -ErrorAction SilentlyContinue}}
            }
            'postgresql' {
                $TargetDataDirectory=Assert-CpfBackupScalar $TargetDataDirectory 'TargetDataDirectory'
                $PostgreSqlRestoreCommandTemplate=Assert-CpfBackupScalar $PostgreSqlRestoreCommandTemplate 'PostgreSqlRestoreCommandTemplate'
                if($PostgreSqlRestoreCommandTemplate -notmatch '%f' -or $PostgreSqlRestoreCommandTemplate -notmatch '%p'){throw 'PostgreSQL restore_command template에는 %f와 %p가 필요합니다.'}
                $data=[IO.Path]::GetFullPath($TargetDataDirectory)
                if(Test-Path -LiteralPath $data){if(@(Get-ChildItem -LiteralPath $data -Force).Count -gt 0){throw 'PostgreSQL PITR target data directory가 비어 있지 않습니다.'}}else{New-Item -ItemType Directory -Force -Path $data|Out-Null}
                $base=@($resolved|Where-Object kind -eq 'base');if($base.Count -ne 1){throw 'PostgreSQL base artifact는 정확히 하나여야 합니다.'}
                $tar=Get-Command tar -ErrorAction SilentlyContinue;if(-not $tar){throw 'tar를 찾을 수 없습니다.'}
                $extract=Start-Process -FilePath $tar.Source -ArgumentList @('-xf',$base[0].path,'-C',$data) -NoNewWindow -Wait -PassThru;if($extract.ExitCode -ne 0){throw "PostgreSQL base extract failed: exit=$($extract.ExitCode)"}
                $escapedRestore=$PostgreSqlRestoreCommandTemplate.Replace("'","''");$auto=Join-Path $data 'postgresql.auto.conf'
                Add-Content -LiteralPath $auto -Encoding UTF8 -Value "restore_command = '$escapedRestore'"
                Add-Content -LiteralPath $auto -Encoding UTF8 -Value "recovery_target_time = '$($target.ToString('yyyy-MM-dd HH:mm:ss.fff+00'))'"
                Add-Content -LiteralPath $auto -Encoding UTF8 -Value "recovery_target_action = 'promote'"
                New-Item -ItemType File -Force -Path (Join-Path $data 'recovery.signal')|Out-Null
                $pgctl=Get-Command pg_ctl -ErrorAction SilentlyContinue;if(-not $pgctl){throw 'pg_ctl을 찾을 수 없습니다.'}
                $psql=Get-Command psql -ErrorAction SilentlyContinue;if(-not $psql){throw 'psql을 찾을 수 없습니다.'}
                $start=Start-Process -FilePath $pgctl.Source -ArgumentList @('-D',$data,'start','-w','-t',"$RecoveryTimeoutSeconds") -NoNewWindow -Wait -PassThru;if($start.ExitCode -ne 0){throw "PostgreSQL recovery start failed: exit=$($start.ExitCode)"}
                $deadline=(Get-Date).AddSeconds($RecoveryTimeoutSeconds);$promoted=$false
                while((Get-Date) -lt $deadline){
                    $checkOut=Join-Path ([IO.Path]::GetTempPath()) ("cpf-pitr-pg-$([guid]::NewGuid().ToString('N')).out")
                    try{
                        $check=Start-Process -FilePath $psql.Source -ArgumentList @('--host',$Host,'--port',"$Port",'--username',$User,'--dbname',$database,'--tuples-only','--no-align','--command','SELECT pg_is_in_recovery();') -NoNewWindow -Wait -PassThru -RedirectStandardOutput $checkOut
                        $state=if(Test-Path -LiteralPath $checkOut){(Get-Content -LiteralPath $checkOut -Raw -Encoding UTF8).Trim().ToLowerInvariant()}else{''}
                        if($check.ExitCode -eq 0 -and $state -eq 'f'){$promoted=$true;break}
                    } finally {if(Test-Path -LiteralPath $checkOut){Remove-Item -LiteralPath $checkOut -Force -ErrorAction SilentlyContinue}}
                    Start-Sleep -Seconds 2
                }
                if(-not $promoted){throw 'PostgreSQL recovery가 제한 시간 내 promotion 완료 상태로 전환되지 않았습니다.'}
            }
            'oracle' {
                $OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier'
                if([string]::IsNullOrWhiteSpace($OracleCatalogDirectory)){$OracleCatalogDirectory=Split-Path -Parent (@($resolved|Where-Object kind -eq 'rman-piece'|Select-Object -First 1).path)}
                $OracleCatalogDirectory=Assert-CpfBackupScalar $OracleCatalogDirectory 'OracleCatalogDirectory'
                $catalogPath=[IO.Path]::GetFullPath($OracleCatalogDirectory)
                if(-not (Test-Path -LiteralPath $catalogPath -PathType Container)){throw "Oracle RMAN catalog directory가 없습니다: $catalogPath"}
                $catalogPrefix=$catalogPath.TrimEnd([IO.Path]::DirectorySeparatorChar,[IO.Path]::AltDirectorySeparatorChar)+[IO.Path]::DirectorySeparatorChar
                foreach($piece in @($resolved|Where-Object kind -eq 'rman-piece')){if(-not ([IO.Path]::GetFullPath($piece.path).StartsWith($catalogPrefix,[StringComparison]::OrdinalIgnoreCase))){throw 'Oracle RMAN piece가 catalog directory 밖에 있습니다.'}}
                $escapedCatalog=$catalogPrefix.Replace("'","''")
                $rman=Get-Command rman -ErrorAction SilentlyContinue;if(-not $rman){throw 'rman을 찾을 수 없습니다.'}
                $psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=$rman.Source;$psi.UseShellExecute=$false;$psi.CreateNoWindow=$true;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$psi.StandardOutputEncoding=[Text.Encoding]::UTF8;$psi.StandardErrorEncoding=[Text.Encoding]::UTF8;[void]$psi.ArgumentList.Add('target');[void]$psi.ArgumentList.Add("/@$OracleConnectIdentifier")
                $untilExpression="TO_DATE('$($target.ToString('yyyy-MM-dd HH:mm:ss'))','YYYY-MM-DD HH24:MI:SS')"
                $script=(@(
                    "CATALOG START WITH '$escapedCatalog' NOPROMPT;",
                    'RUN {',
                    ('SET UNTIL TIME "{0}";' -f $untilExpression),
                    'RESTORE DATABASE;',
                    'RECOVER DATABASE;',
                    "SQL 'ALTER DATABASE OPEN RESETLOGS';",
                    '}',
                    'EXIT'
                ) -join "`n")+"`n"
                $process=[Diagnostics.Process]::new();$process.StartInfo=$psi
                try{if(-not $process.Start()){throw 'RMAN process start failed'};$process.StandardInput.Write($script);$process.StandardInput.Close();$o=$process.StandardOutput.ReadToEndAsync();$e=$process.StandardError.ReadToEndAsync();$process.WaitForExit();$combinedOutput=$o.GetAwaiter().GetResult()+"`n"+$e.GetAwaiter().GetResult();if($process.ExitCode -ne 0){throw "Oracle RMAN PITR failed: exit=$($process.ExitCode)"}}finally{$process.Dispose()}
            }
        }
        $result.status='COMPLETED';$result.outputSha256=([BitConverter]::ToString([Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($combinedOutput)))).Replace('-','').ToLowerInvariant()
    }
} catch {
    if($Execute){$result.status='UNKNOWN';$result.reconcileRequired=$true}else{$result.status='FAILED'}
    $result.failureCode=$_.Exception.GetType().Name
    throw
} finally {
    $result.finishedAt=(Get-Date).ToUniversalTime().ToString('o');Write-CpfJsonAtomic $result $resultAbsolute
    Write-Host "PITR_RESULT mode=$($result.mode) status=$($result.status) planSha256=$planHash result=$resultAbsolute"
}
