$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Join-Path $here 'validate_harness.py'
$cmd=Get-Command python -ErrorAction SilentlyContinue
if(-not $cmd){$cmd=Get-Command py -ErrorAction SilentlyContinue}
if(-not $cmd){throw 'HARNESS=FAIL exact current Harness self-validator requires Python; do not downgrade to weaker PASS'}
if($cmd.Name-eq 'py'){& $cmd.Source -3 $py}else{& $cmd.Source $py}
if($LASTEXITCODE-ne 0){throw 'HARNESS=FAIL'}
