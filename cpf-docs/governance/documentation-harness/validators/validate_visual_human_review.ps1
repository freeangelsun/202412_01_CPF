$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
python (Join-Path $here 'validate_visual_human_review.py') @args
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
