$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$py=Join-Path $here 'validate_docx_artifacts.py'
$cmd=Get-Command python -ErrorAction SilentlyContinue
if(-not $cmd){$cmd=Get-Command py -ErrorAction SilentlyContinue}
if(-not $cmd){throw 'DOCX_STRUCTURE=FAIL exact strict validator requires Python + python-docx; do not downgrade to a weaker PASS'}
if($cmd.Name -eq 'py'){& $cmd.Source -3 $py}else{& $cmd.Source $py}
if($LASTEXITCODE-ne 0){throw 'DOCX_STRUCTURE=FAIL'}
