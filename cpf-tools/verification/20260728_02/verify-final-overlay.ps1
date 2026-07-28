param(
    [string]$OverlayRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
)
$ErrorActionPreference = "Stop"
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw "Python 3가 필요합니다." }
& $python.Source (Join-Path $PSScriptRoot "verify-final-overlay.py") --root $OverlayRoot --report (Join-Path $PSScriptRoot "STATIC_VALIDATION_RESULT.json")
if ($LASTEXITCODE -ne 0) { throw "CPF final overlay static validation failed (exit=$LASTEXITCODE)" }
Write-Host "CPF final overlay static validation PASS"
