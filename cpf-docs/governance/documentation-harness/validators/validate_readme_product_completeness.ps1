$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
python (Join-Path $here 'validate_readme_product_completeness.py') @args
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
