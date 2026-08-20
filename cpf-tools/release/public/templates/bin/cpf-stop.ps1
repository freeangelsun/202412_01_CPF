param([int]$TimeoutSeconds=300)
$ErrorActionPreference='Stop'; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
& java (Join-Path $PSScriptRoot 'CpfBootstrap.java') stop --workspace $root --timeout-seconds $TimeoutSeconds; if($LASTEXITCODE -ne 0){throw "CPF stop failed exit=$LASTEXITCODE"}
