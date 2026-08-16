param([string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,[string]$ExpectedSha,[switch]$RequireClean,[string]$JsonOutput)
$python=Get-Command python -ErrorAction Stop
$args=@((Join-Path $PSScriptRoot 'verify-cpf-split-master-dataset.py'),'--root',$Root)
if($ExpectedSha){$args+=@('--expected-sha',$ExpectedSha)}
if($RequireClean){$args+='--require-clean'}
if($JsonOutput){$args+=@('--json-output',$JsonOutput)}
& $python.Source @args
if($LASTEXITCODE -ne 0){throw "CPF split master verification failed (exit=$LASTEXITCODE)"}
