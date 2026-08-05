[CmdletBinding()]
param(
    [ValidateSet('mariadb','postgresql','oracle')][string]$Vendor='mariadb',
    [ValidateSet('mask','synthetic')][string]$Mode='synthetic',
    [Parameter(Mandatory)][string]$TargetEnvironment,
    [Parameter(Mandatory)][string]$Database,
    [string]$Host='127.0.0.1',[int]$Port=0,[string]$User='root',[string]$OracleConnectIdentifier='',
    [string]$DeterministicSeed='CPF_TEST_DATA_V1',
    [Parameter(Mandatory)][string]$Operator,[Parameter(Mandatory)][string]$Reason,[Parameter(Mandatory)][string]$ApprovalReference,
    [switch]$PlanOnly,[switch]$Execute,[switch]$ConfirmNonProduction,[switch]$ConfirmSourceDataAuthorized,[switch]$ConfirmRawDataPurge,
    [string]$ExpectedSqlSha256='',[string]$OutputDirectory='build/test-data',[string]$ResultPath='build/test-data/test-data-result.sanitized.json',[string]$Root='.'
)
$ErrorActionPreference='Stop';if($PlanOnly -and $Execute){throw '-PlanOnly과 -Execute를 동시에 지정할 수 없습니다.'};if(-not $PlanOnly -and -not $Execute){$PlanOnly=$true}
$rootPath=(Resolve-Path $Root).Path;. (Join-Path $rootPath 'cpf-tools/scripts/database-profile-common.ps1');. (Join-Path $rootPath 'cpf-tools/scripts/cpf-backup-lifecycle-common.ps1')
$Vendor=Assert-CpfSupportedDatabaseVendor $Vendor;$Database=Assert-CpfBackupIdentifier $Database 'Database';$TargetEnvironment=(Assert-CpfBackupScalar $TargetEnvironment 'TargetEnvironment').ToLowerInvariant();$Operator=Assert-CpfBackupScalar $Operator 'Operator';$Reason=Assert-CpfBackupScalar $Reason 'Reason';$ApprovalReference=Assert-CpfBackupScalar $ApprovalReference 'ApprovalReference'
$policy=Get-Content (Join-Path $rootPath 'cpf-tools/db/cpf-test-data-policy.json') -Raw -Encoding UTF8|ConvertFrom-Json -Depth 20
if($TargetEnvironment -in @($policy.productionEnvironmentNames)){throw "Production 환경에는 Test Data pack을 적용할 수 없습니다: $TargetEnvironment"}
if($Execute -and -not $ConfirmNonProduction){throw 'Execute에는 -ConfirmNonProduction이 필요합니다.'}
if($Mode -eq 'mask' -and $Execute -and (-not $ConfirmSourceDataAuthorized -or -not $ConfirmRawDataPurge)){throw 'Masked copy Execute에는 Source 권한과 Raw Data purge 확인이 필요합니다.'}
if($Port -le 0){$Port=Get-CpfVendorDefaultPort $Vendor}
$out=Join-Path $rootPath $OutputDirectory;New-Item -ItemType Directory -Force -Path $out|Out-Null;$sql=Join-Path $out "$Vendor-$Mode.sql";$inventory=Join-Path $out "$Vendor-$Mode.inventory.json"
$python=Get-CpfPythonCommand;$generator=Join-Path $rootPath 'cpf-tools/scripts/generate-cpf-test-data-pack.py';$generated=& $python $generator --root $rootPath --vendor $Vendor --mode $Mode --seed $DeterministicSeed --output $sql --inventory $inventory 2>&1;if($LASTEXITCODE -ne 0){throw "Test Data pack generation failed: exit=$LASTEXITCODE"};$meta=($generated -join "`n")|ConvertFrom-Json -Depth 20
$result=[ordered]@{schemaVersion=1;operation='TEST_DATA_PREPARATION';mode=if($Execute){'EXECUTE'}else{'PLAN'};status=if($Execute){'APPLYING'}else{'PLANNED'};vendor=$Vendor;database=$Database;targetEnvironment=$TargetEnvironment;dataMode=$Mode;sqlSha256=[string]$meta.sha256;maskedColumnCount=[int]$meta.maskedColumnCount;synthetic=[bool]$meta.synthetic;productionDerived=$false;operator=$Operator;reason=$Reason;approvalReference=$ApprovalReference;startedAt=(Get-Date).ToUniversalTime().ToString('o');finishedAt='';reconcileRequired=$false;failureCode='';outputSha256='';sanitized=$true}
$resultAbsolute=if([IO.Path]::IsPathRooted($ResultPath)){$ResultPath}else{Join-Path $rootPath $ResultPath}
$executionStarted=$false
try{
 if($Execute){
  if($ExpectedSqlSha256 -notmatch '^[0-9a-fA-F]{64}$' -or $ExpectedSqlSha256.ToLowerInvariant() -ne ([string]$meta.sha256)){throw "검토한 SQL SHA와 생성 SQL이 다릅니다: current=$($meta.sha256)"}
  $psi=[Diagnostics.ProcessStartInfo]::new();$psi.UseShellExecute=$false;$psi.CreateNoWindow=$true;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$input=Get-Content -LiteralPath $sql -Raw -Encoding UTF8
  switch($Vendor){
   'mariadb' {$tool=Get-Command mariadb -ErrorAction SilentlyContinue;if(-not $tool){throw 'mariadb client를 찾을 수 없습니다.'};$psi.FileName=$tool.Source;foreach($a in @('--host',$Host,'--port',"$Port",'--user',$User,$Database)){[void]$psi.ArgumentList.Add($a)}}
   'postgresql' {$tool=Get-Command psql -ErrorAction SilentlyContinue;if(-not $tool){throw 'psql을 찾을 수 없습니다.'};$psi.FileName=$tool.Source;foreach($a in @('-X','-q','--set=ON_ERROR_STOP=1','--host',$Host,'--port',"$Port",'--username',$User,'--dbname',$Database)){[void]$psi.ArgumentList.Add($a)}}
   'oracle' {$OracleConnectIdentifier=Assert-CpfBackupScalar $OracleConnectIdentifier 'OracleConnectIdentifier';$tool=Get-Command sqlplus -ErrorAction SilentlyContinue;if(-not $tool){throw 'sqlplus를 찾을 수 없습니다.'};$psi.FileName=$tool.Source;foreach($a in @('-L','-S','/nolog')){[void]$psi.ArgumentList.Add($a)};$input="SET ECHO OFF`nSET VERIFY OFF`nSET DEFINE OFF`nWHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT /@$OracleConnectIdentifier`n$input`nEXIT`n"}
  }
  $p=[Diagnostics.Process]::new();$p.StartInfo=$psi;try{if(-not $p.Start()){throw 'Test Data client start failed'};$executionStarted=$true;$p.StandardInput.Write($input);$p.StandardInput.Close();$o=$p.StandardOutput.ReadToEndAsync();$e=$p.StandardError.ReadToEndAsync();$p.WaitForExit();$stdoutText=$o.GetAwaiter().GetResult();$stderrText=$e.GetAwaiter().GetResult();$outputBytes=[Text.Encoding]::UTF8.GetBytes($stdoutText+"`n"+$stderrText);$hash=[Security.Cryptography.SHA256]::Create();try{$result.outputSha256=([BitConverter]::ToString($hash.ComputeHash($outputBytes))).Replace('-','').ToLowerInvariant()}finally{$hash.Dispose()};if($p.ExitCode -ne 0){throw "Test Data apply failed: vendor=$Vendor exit=$($p.ExitCode)"};$result.status='COMPLETED'}finally{$p.Dispose()}
 }
} catch {if($Execute -and $executionStarted){$result.status='UNKNOWN';$result.reconcileRequired=$true}else{$result.status='FAILED'};$result.failureCode=$_.Exception.GetType().Name;throw} finally {$result.finishedAt=(Get-Date).ToUniversalTime().ToString('o');Write-CpfJsonAtomic $result $resultAbsolute;Write-Host "TEST_DATA_RESULT status=$($result.status) sqlSha256=$($result.sqlSha256) result=$resultAbsolute"}
