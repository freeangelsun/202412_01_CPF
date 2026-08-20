param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$delegate = Join-Path $Root 'cpf-tools/verification/tools/check-backoffice-safe-defaults.ps1'
if(-not (Test-Path -LiteralPath $delegate -PathType Leaf)){throw "Canonical Backoffice safe-default verifier missing: $delegate"}
& powershell -NoProfile -ExecutionPolicy Bypass -File $delegate -Root $Root
if($LASTEXITCODE -ne 0){throw "Canonical Backoffice safe-default verifier failed exit=$LASTEXITCODE"}
Write-Host "[PASS] legacy BZA safe-default entry delegated to current Backoffice/MBW verifier"
