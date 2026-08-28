$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Get-Command python -ErrorAction SilentlyContinue
if(-not $py){$py=Get-Command py -ErrorAction SilentlyContinue}
if(-not $py){throw 'SOURCE_ALIGNMENT=FAIL exact current validator requires Python; weaker fallback forbidden'}
$script=Join-Path $here 'validate_source_alignment.py'
if($py.Name-eq 'py'){& $py.Source -3 $script}else{& $py.Source $script}
if($LASTEXITCODE-ne 0){throw 'SOURCE_ALIGNMENT=FAIL'}
