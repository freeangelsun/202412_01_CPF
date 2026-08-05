[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
  [Parameter(Mandatory=$true)][ValidateSet('FreshInstall','Upgrade','RollbackReapply')][string]$Mode,
  [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
  [string]$LogDir='cpf-docs/evidence/runtime/db-lifecycle'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path -LiteralPath $Root).Path
$vendorRoot=Join-Path $rootPath "cpf-tools/db/vendor/$Vendor"
$logRoot=Join-Path $rootPath $LogDir
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss'
$log=Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.log"
function Need([string]$Name){$v=[Environment]::GetEnvironmentVariable($Name);if([string]::IsNullOrWhiteSpace($v)){throw "필수 환경변수 누락: $Name"};return $v}
function Run-Sql([string]$File){
  $filePath=(Resolve-Path -LiteralPath $File).Path
  "RUN $filePath" | Tee-Object -FilePath $log -Append
  $previousPgPassword=$env:PGPASSWORD
  $previousMariaPassword=$env:MYSQL_PWD
  $clientExitCode=1
  try {
    switch($Vendor){
      'postgresql' {
        $env:PGPASSWORD=Need 'CPF_PG_PASSWORD';$hostName=Need 'CPF_PG_HOST';$port=Need 'CPF_PG_PORT';$db=Need 'CPF_PG_DATABASE';$user=Need 'CPF_PG_USER'
        & psql -X -v ON_ERROR_STOP=1 -h $hostName -p $port -U $user -d $db -f $filePath 2>&1 | Tee-Object -FilePath $log -Append
        $clientExitCode=$LASTEXITCODE
      }
      'mariadb' {
        $env:MYSQL_PWD=Need 'CPF_MARIADB_PASSWORD';$hostName=Need 'CPF_MARIADB_HOST';$port=Need 'CPF_MARIADB_PORT';$db=Need 'CPF_MARIADB_DATABASE';$user=Need 'CPF_MARIADB_USER'
        Get-Content -LiteralPath $filePath -Raw | & mariadb --protocol=tcp --host=$hostName --port=$port --user=$user --database=$db --show-warnings 2>&1 | Tee-Object -FilePath $log -Append
        $clientExitCode=$LASTEXITCODE
      }
      'oracle' {
        $user=Need 'CPF_ORACLE_USER';$password=Need 'CPF_ORACLE_PASSWORD';$connect=Need 'CPF_ORACLE_CONNECT'
        $driver=Join-Path $env:TEMP "cpf_oracle_${stamp}_$([Guid]::NewGuid().ToString('N')).sql"
        try {
          @(
            'whenever sqlerror exit sql.sqlcode rollback',
            'set echo off verify off define off',
            "connect $user/`"$password`"@$connect",
            'set echo on feedback on serveroutput on',
            "@$($filePath.Replace('\\','/'))",
            'exit success'
          ) | Set-Content -LiteralPath $driver -Encoding utf8
          Get-Content -LiteralPath $driver -Raw | & sqlplus -L /nolog 2>&1 | Tee-Object -FilePath $log -Append
          $clientExitCode=$LASTEXITCODE
        } finally {
          Remove-Item -LiteralPath $driver -Force -ErrorAction SilentlyContinue
          $password=$null
        }
      }
    }
  } finally {
    if($null-eq$previousPgPassword){Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue}else{$env:PGPASSWORD=$previousPgPassword}
    if($null-eq$previousMariaPassword){Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue}else{$env:MYSQL_PWD=$previousMariaPassword}
  }
  if($clientExitCode-ne0){throw "SQL 실행 실패 vendor=$Vendor mode=$Mode file=$filePath exit=$clientExitCode"}
}
function P([string]$Relative){Join-Path $vendorRoot $Relative}
$install=P 'install/00_empty_install.sql';$verify=P 'verify/00_verify.sql'
$v98=P 'migration/flyway/batDB/V98__bat_operation_expected_version.sql';$v99=P 'migration/flyway/batDB/V99__bat_abandon_two_phase_state.sql';$v100=P 'migration/V100__bat_operation_request_ledger.sql'
$r98=P 'rollback/R98__bat_operation_expected_version.sql';$r99=P 'rollback/R99__bat_abandon_two_phase_state.sql';$r100=P 'rollback/R100__bat_operation_request_ledger.sql'
$verify98=P 'verify/V98__bat_operation_expected_version.sql';$verify99=P 'verify/V99__bat_abandon_two_phase_state.sql';$verify100=P 'verify/V100__bat_operation_request_ledger.sql'
switch($Mode){
 'FreshInstall' {Run-Sql $install;Run-Sql $verify;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100}
 'Upgrade' {Run-Sql $v98;Run-Sql $v99;Run-Sql $v100;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100;Run-Sql $verify}
 'RollbackReapply' {Run-Sql $r100;Run-Sql $r99;Run-Sql $r98;Run-Sql $v98;Run-Sql $v99;Run-Sql $v100;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100;Run-Sql $verify}
}
$result=[ordered]@{status='PASS';vendor=$Vendor;mode=$Mode;log=$log;completedAt=(Get-Date).ToString('o')}
$result|ConvertTo-Json|Tee-Object -FilePath $log -Append
