[CmdletBinding()]
param(
    [ValidateSet('mariadb','postgresql','oracle')][string]$Vendor='mariadb',
    [Parameter(Mandatory)][string]$Database,
    [string]$Host='127.0.0.1',
    [int]$Port=0,
    [string]$User='root',
    [string]$VerifySql,
    [switch]$RunPlatformVerify,
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [string]$OracleConnectIdentifier='',
    [string]$Root='.',
    [string]$EvidenceDirectory='cpf-docs/work/evidence/dr'
)
$ErrorActionPreference='Stop'
if($VerifySql -and $RunPlatformVerify){throw '-VerifySql과 -RunPlatformVerify는 동시에 사용할 수 없습니다.'}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/db/tools/database-profile-common.ps1')
. (Join-Path $rootPath 'cpf-tools/db/tools/cpf-backup-lifecycle-common.ps1')
$Vendor=Assert-CpfSupportedDatabaseVendor $Vendor
$Database=Assert-CpfBackupIdentifier $Database 'Database'
$Host=Assert-CpfBackupScalar $Host 'Host'
$User=Assert-CpfBackupScalar $User 'User'
$Operator=Assert-CpfBackupScalar $Operator 'Operator'
$Reason=Assert-CpfBackupScalar $Reason 'Reason'
$ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
if($Port -le 0){$Port=Get-CpfVendorDefaultPort $Vendor}
$started=(Get-Date).ToUniversalTime(); $mode='ISOLATED_BASELINE'
$query=switch($Vendor){
    'mariadb' {'SELECT COUNT(*) AS object_count FROM information_schema.tables WHERE table_schema=DATABASE();'}
    'postgresql' {"SELECT COUNT(*) AS object_count FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog','information_schema');"}
    'oracle' {'SELECT COUNT(*) AS object_count FROM user_objects WHERE object_type IN (''TABLE'',''INDEX'',''VIEW'',''SEQUENCE'');'}
}
if($VerifySql){$mode='CUSTOM_SQL';$query=Get-Content (Resolve-Path $VerifySql) -Raw -Encoding UTF8}
elseif($RunPlatformVerify){
    $mode='FULL_PLATFORM'
    $queryPath=Join-Path $rootPath "cpf-tools/db/vendor/$Vendor/verify/00_verify.sql"
    if(-not (Test-Path -LiteralPath $queryPath -PathType Leaf)){throw "Vendor verify SQL이 없습니다: $queryPath"}
    $query=Get-Content -LiteralPath $queryPath -Raw -Encoding UTF8
}
$psi=[Diagnostics.ProcessStartInfo]::new();$psi.UseShellExecute=$false;$psi.CreateNoWindow=$true;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
$input=$query
switch($Vendor){
    'mariadb' {
        $tool=Get-Command mariadb -ErrorAction SilentlyContinue;if(-not $tool){throw 'mariadb client를 찾을 수 없습니다.'}
        $psi.FileName=$tool.Source
        foreach($arg in @('--batch','--raw','--skip-column-names','--host',$Host,'--port',"$Port",'--user',$User,$Database)){[void]$psi.ArgumentList.Add($arg)}
    }
    'postgresql' {
        $tool=Get-Command psql -ErrorAction SilentlyContinue;if(-not $tool){throw 'psql을 찾을 수 없습니다.'}
        $psi.FileName=$tool.Source
        foreach($arg in @('-X','-q','-t','-A','--set=ON_ERROR_STOP=1','--host',$Host,'--port',"$Port",'--username',$User,'--dbname',$Database)){[void]$psi.ArgumentList.Add($arg)}
    }
    'oracle' {
        $OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier'
        $tool=Get-Command sqlplus -ErrorAction SilentlyContinue;if(-not $tool){throw 'sqlplus를 찾을 수 없습니다.'}
        $psi.FileName=$tool.Source
        foreach($arg in @('-L','-S','/nolog')){[void]$psi.ArgumentList.Add($arg)}
        $input="SET ECHO OFF`nSET VERIFY OFF`nSET DEFINE OFF`nSET HEADING OFF`nSET FEEDBACK OFF`nWHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT /@$OracleConnectIdentifier`n$query`nEXIT`n"
    }
}
$process=[Diagnostics.Process]::new();$process.StartInfo=$psi
$exit=1;$stdout='';$stderr='';$status='FAIL';$failureCode=''
try{
    if(-not $process.Start()){throw "DB verify client를 시작할 수 없습니다: vendor=$Vendor"}
    $process.StandardInput.Write($input);$process.StandardInput.Close()
    $outTask=$process.StandardOutput.ReadToEndAsync();$errTask=$process.StandardError.ReadToEndAsync();$process.WaitForExit()
    $stdout=$outTask.GetAwaiter().GetResult();$stderr=$errTask.GetAwaiter().GetResult();$exit=$process.ExitCode
    if($exit -eq 0){$status='PASS'}else{$failureCode='CLIENT_EXIT_NONZERO'}
    if($mode -eq 'ISOLATED_BASELINE' -and $status -eq 'PASS'){
        $numbers=[regex]::Matches($stdout,'(?m)^\s*(\d+)\s*$')
        if($numbers.Count -eq 0 -or [int64]$numbers[$numbers.Count-1].Groups[1].Value -le 0){$status='FAIL';$failureCode='EMPTY_RESTORED_SCHEMA'}
    }
} catch {
    $failureCode=$_.Exception.GetType().Name
} finally {$process.Dispose()}
$finished=(Get-Date).ToUniversalTime();$combined=($stdout+"`n"+$stderr);$bytes=[Text.Encoding]::UTF8.GetBytes($combined);$sha256=[Security.Cryptography.SHA256]::HashData($bytes);$resultHash=([BitConverter]::ToString($sha256)).Replace('-','').ToLowerInvariant()
$out=Join-Path $rootPath $EvidenceDirectory;New-Item -ItemType Directory -Force -Path $out|Out-Null
$e=[ordered]@{schemaVersion=1;sourceSha=(Get-CpfGitHeadOrUnknown $rootPath);vendor=$Vendor;database=$Database;mode=$mode;operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;startedAt=$started.ToString('o');finishedAt=$finished.ToString('o');durationMs=[math]::Round(($finished-$started).TotalMilliseconds);status=$status;exitCode=$exit;failureCode=$failureCode;resultSha256=$resultHash;resultCaptured=$true;resultContentStored=$false;sanitized=$true}
$path=Join-Path $out ("dr-restore-{0}-{1}.json" -f $started.ToString('yyyyMMddTHHmmssfffZ'),([guid]::NewGuid().ToString('N').Substring(0,8)));Write-CpfJsonAtomic $e $path
if($status -ne 'PASS'){throw "DR restore verification failed. sanitizedEvidence=$path"}
Write-Host "DR_VERIFY_PASS vendor=$Vendor mode=$mode evidence=$path"
