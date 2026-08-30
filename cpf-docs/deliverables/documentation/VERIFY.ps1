$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim());Set-Location $root
python .\cpf-docs\deliverables\documentation\verify_documentation_delivery.py
if($LASTEXITCODE-ne0){throw "DOCUMENTATION VERIFY FAILED exit=$LASTEXITCODE"}
Write-Host '[CPF][DOC] HARNESS 2.15.4 VERIFY PASS'
