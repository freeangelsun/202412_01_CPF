$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Join-Path $here 'validate_quality_fixtures.py'
$cmd=Get-Command python -ErrorAction SilentlyContinue
if(-not $cmd){$cmd=Get-Command py -ErrorAction SilentlyContinue}
if(-not $cmd){throw 'QUALITY_FIXTURES=FAIL Python runtime required for exact fixture parity'}
if($cmd.Name -eq 'py'){& $cmd.Source -3 $py}else{& $cmd.Source $py}
if($LASTEXITCODE-ne 0){throw 'QUALITY_FIXTURES=FAIL'}
