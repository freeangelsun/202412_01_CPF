param([string]$ProjectRoot=(Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path)
$ErrorActionPreference='Stop'
python (Join-Path $PSScriptRoot 'check_checkpoint_overlay.py') $ProjectRoot
if($LASTEXITCODE -ne 0){ throw "CPF checkpoint structural verification failed (exit=$LASTEXITCODE)" }
Write-Host 'CPF checkpoint structural verification completed. OPEN items are not final PASS.'
