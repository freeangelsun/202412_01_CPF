param([string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,[string]$JsonOutput)
$args=@((Join-Path $PSScriptRoot 'verify-cpf-owner-boundaries.py'),'--root',$Root)
if($JsonOutput){$args+=@('--json-output',$JsonOutput)}
& python @args
if($LASTEXITCODE -ne 0){throw "CPF owner boundary verification failed (exit=$LASTEXITCODE)"}
