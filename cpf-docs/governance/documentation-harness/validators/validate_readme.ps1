param([string]$Path='README.md')
$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Get-Command python -ErrorAction SilentlyContinue
if(-not $py){$py=Get-Command py -ErrorAction SilentlyContinue}
if(-not $py){throw 'README=FAIL exact v2.10 validator requires Python; weaker fallback forbidden'}
$script=Join-Path $here 'validate_readme.py'
if($py.Name-eq 'py'){& $py.Source -3 $script $Path}else{& $py.Source $script $Path}
if($LASTEXITCODE-ne 0){throw 'README=FAIL'}
