$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$py=$null
if (Get-Command py -ErrorAction SilentlyContinue) { $py='py'; & py -3 (Join-Path $PSScriptRoot 'validate_harness.py'); exit $LASTEXITCODE }
if (Get-Command python -ErrorAction SilentlyContinue) { & python (Join-Path $PSScriptRoot 'validate_harness.py'); exit $LASTEXITCODE }
if (Get-Command python3 -ErrorAction SilentlyContinue) { & python3 (Join-Path $PSScriptRoot 'validate_harness.py'); exit $LASTEXITCODE }
Write-Error 'Python 3 is required to validate CPF Documentation Harness.'
