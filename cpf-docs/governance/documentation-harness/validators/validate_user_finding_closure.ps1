$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
python (Join-Path $here 'validate_user_finding_closure.py') @args
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
