param([int]$TimeoutSeconds=1800)
$ErrorActionPreference='Stop'; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
& java (Join-Path $PSScriptRoot 'CpfBootstrap.java') test --workspace $root --timeout-seconds $TimeoutSeconds; if($LASTEXITCODE -ne 0){throw "CPF test failed exit=$LASTEXITCODE"}
