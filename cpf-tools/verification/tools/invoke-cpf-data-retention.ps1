[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory)][string]$PolicyId,
    [Parameter(Mandatory)][string]$LegalHoldManifestPath,
    [Parameter(Mandatory)][datetime]$CutoffUtc,
    [Parameter(Mandatory)][string]$Operator,
    [Parameter(Mandatory)][string]$Reason,
    [Parameter(Mandatory)][string]$ApprovalReference,
    [switch]$PlanOnly,
    [switch]$Execute,
    [switch]$ConfirmArchiveBeforePurge,
    [string]$ExpectedPlanSha256='',
    [string]$Host='127.0.0.1',
    [int]$Port=0,
    [string]$User='root',
    [string]$Database='cpfDB',
    [string]$OracleConnectIdentifier='',
    [string]$ResultPath='build/db-retention/retention-result.sanitized.json',
    [string]$Root='.'
)
$ErrorActionPreference='Stop'

# Child/native DB process에서도 UTF-8 계약을 유지합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
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
$Database=Assert-CpfBackupIdentifier $Database 'Database'
$holdPath=(Resolve-Path $LegalHoldManifestPath).Path
$python=Get-CpfPythonCommand
$work=Join-Path ([IO.Path]::GetTempPath()) ("cpf-retention-$([guid]::NewGuid().ToString('N'))")
New-Item -ItemType Directory -Force -Path $work|Out-Null
$sql=Join-Path $work 'retention.sql'
$resultAbsolute=if([IO.Path]::IsPathRooted($ResultPath)){[IO.Path]::GetFullPath($ResultPath)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $ResultPath))}
$result=[ordered]@{schemaVersion=1;operation='DATA_RETENTION';mode=if($Execute){'EXECUTE'}else{'PLAN'};status=if($Execute){'APPLYING'}else{'PLANNED'};vendor=$Vendor;policyId=$PolicyId;database=$Database;cutoffUtc=$CutoffUtc.ToUniversalTime().ToString('o');operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;planSha256='';startedAt=(Get-Date).ToUniversalTime().ToString('o');finishedAt='';reconcileRequired=$false;failureCode='';outputSha256='';sanitized=$true}
try{
    $generateArgs=@((Join-Path $rootPath 'cpf-tools/db/tools/generate-cpf-data-retention-sql.py'),'--root',$rootPath,'--vendor',$Vendor,'--policy-id',$PolicyId,'--legal-hold-manifest',$holdPath,'--cutoff-utc',$CutoffUtc.ToUniversalTime().ToString('o'),'--operator',$Operator,'--reason',$Reason,'--output',$sql)
    $generatorOutput=& $python @generateArgs 2>&1
    if($LASTEXITCODE -ne 0){throw 'Retention SQL generation failed.'}
    $planHash=(Get-FileHash -LiteralPath $sql -Algorithm SHA256).Hash.ToLowerInvariant();$result.planSha256=$planHash
    if($Execute){
        if(-not $ConfirmArchiveBeforePurge){throw 'Retention Execute에는 -ConfirmArchiveBeforePurge가 필요합니다.'}
        if($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or $ExpectedPlanSha256.ToLowerInvariant() -ne $planHash){throw "검토한 retention plan SHA와 현재 plan이 다릅니다: current=$planHash"}
        if($Port -le 0){$Port=Get-CpfVendorDefaultPort $Vendor}
        $output=Join-Path $work 'client.out'
        switch($Vendor){
            'mariadb' {$client=Get-Command mariadb -ErrorAction SilentlyContinue;if(-not $client){throw 'mariadb client를 찾을 수 없습니다.'};$process=Start-Process -FilePath $client.Source -ArgumentList @('--default-character-set=utf8mb4','--host',$Host,'--port',"$Port",'--user',$User,$Database) -RedirectStandardInput $sql -RedirectStandardOutput $output -NoNewWindow -Wait -PassThru}
            'postgresql' {$client=Get-Command psql -ErrorAction SilentlyContinue;if(-not $client){throw 'psql을 찾을 수 없습니다.'};$process=Start-Process -FilePath $client.Source -ArgumentList @('--host',$Host,'--port',"$Port",'--username',$User,'--dbname',$Database,'--file',$sql,'--set','ON_ERROR_STOP=1') -RedirectStandardOutput $output -NoNewWindow -Wait -PassThru}
            'oracle' {$OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier';$client=Get-Command sqlplus -ErrorAction SilentlyContinue;if(-not $client){throw 'sqlplus를 찾을 수 없습니다.'};$psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=$client.Source;$psi.UseShellExecute=$false;$psi.CreateNoWindow=$true;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$psi.StandardOutputEncoding=[Text.Encoding]::UTF8;$psi.StandardErrorEncoding=[Text.Encoding]::UTF8;foreach($arg in @('-L','-S','/nolog')){[void]$psi.ArgumentList.Add($arg)};$process=[Diagnostics.Process]::new();$process.StartInfo=$psi;if(-not $process.Start()){throw 'sqlplus process start failed'};$body="CONNECT /@$OracleConnectIdentifier`n"+(Get-Content -LiteralPath $sql -Raw -Encoding UTF8);$process.StandardInput.Write($body);$process.StandardInput.Close();$stdout=$process.StandardOutput.ReadToEndAsync();$stderr=$process.StandardError.ReadToEndAsync();$process.WaitForExit();[IO.File]::WriteAllText($output,$stdout.GetAwaiter().GetResult()+"`n"+$stderr.GetAwaiter().GetResult(),[Text.UTF8Encoding]::new($false))}
        }
        $clientExitCode=$process.ExitCode
        if($Vendor -eq 'oracle'){$process.Dispose()}
        if($clientExitCode -ne 0){throw "Retention client failed: vendor=$Vendor exit=$clientExitCode"}
        $result.status='COMPLETED';$result.outputSha256=(Get-FileHash -LiteralPath $output -Algorithm SHA256).Hash.ToLowerInvariant()
    }
} catch {
    if($Execute){$result.status='UNKNOWN';$result.reconcileRequired=$true}else{$result.status='FAILED'};$result.failureCode=$_.Exception.GetType().Name;throw
} finally {
    $result.finishedAt=(Get-Date).ToUniversalTime().ToString('o');Write-CpfJsonAtomic $result $resultAbsolute
    if(Test-Path -LiteralPath $work){Remove-Item -LiteralPath $work -Recurse -Force -ErrorAction SilentlyContinue}
    Write-Host "DATA_RETENTION_RESULT mode=$($result.mode) status=$($result.status) planSha256=$($result.planSha256) result=$resultAbsolute"
}
