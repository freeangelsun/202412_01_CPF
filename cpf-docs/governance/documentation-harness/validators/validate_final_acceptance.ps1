param([Parameter(Mandatory=$true)][string]$Manifest)
$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$script=Join-Path $here 'validate_final_acceptance.py'
$py=Get-Command python -ErrorAction SilentlyContinue
if(-not $py){$py=Get-Command py -ErrorAction SilentlyContinue}
if(-not $py){throw 'FINAL_ACCEPTANCE=FAIL exact current validator requires Python; weaker fallback forbidden'}
if($py.Name-eq 'py'){& $py.Source -3 $script $Manifest}else{& $py.Source $script $Manifest}
if($LASTEXITCODE-ne 0){throw 'FINAL_ACCEPTANCE=FAIL'}
