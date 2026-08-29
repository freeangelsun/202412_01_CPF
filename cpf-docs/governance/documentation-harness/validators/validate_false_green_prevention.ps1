$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
python (Join-Path $here 'validate_false_green_prevention.py') @args
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
