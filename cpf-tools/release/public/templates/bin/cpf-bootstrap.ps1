param([ValidateSet('oracle','postgresql','mariadb')][string]$Db,[int]$TimeoutSeconds=300,[switch]$SkipBuild,[switch]$SkipTest,[switch]$StartRuntime)
$ErrorActionPreference='Stop'; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$args=@('bootstrap','--workspace',$root,'--timeout-seconds',[string]$TimeoutSeconds); if($Db){$args+=@('--db',$Db)}; if($SkipBuild){$args+='--skip-build'}; if($SkipTest){$args+='--skip-test'}; if($StartRuntime){$args+='--start-runtime'}
& java (Join-Path $PSScriptRoot 'CpfBootstrap.java') @args; if($LASTEXITCODE -ne 0){throw "CPF bootstrap failed exit=$LASTEXITCODE"}
