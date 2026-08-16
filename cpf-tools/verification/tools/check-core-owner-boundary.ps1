param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
& pwsh -NoProfile -File (Join-Path $Root 'cpf-tools/verification/tools/verify-cpf-owner-boundaries.ps1') -Root $Root
if ($LASTEXITCODE -ne 0) { throw "Core/Admin/BAT owner boundary gate failed (exit=$LASTEXITCODE)" }
Write-Host 'Core/Admin/BAT owner boundary compatibility gate PASS.'
