param()
$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Get-Command python -ErrorAction SilentlyContinue
if(-not $py){$py=Get-Command py -ErrorAction SilentlyContinue}
if(-not $py){throw 'READABILITY_ACTIONABILITY=FAIL exact v2.10 validator requires Python; weaker fallback forbidden'}
$script=Join-Path $here 'validate_readability_actionability.py'
if($py.Name-eq 'py'){& $py.Source -3 $script}else{& $py.Source $script}
if($LASTEXITCODE-ne 0){throw 'READABILITY_ACTIONABILITY=FAIL'}
