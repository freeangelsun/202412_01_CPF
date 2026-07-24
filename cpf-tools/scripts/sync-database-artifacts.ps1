param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)
$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "build-all-install-sql.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { throw "DB bundle generation failed." }

& (Join-Path $PSScriptRoot "generate-database-schema-manifest.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { throw "DB schema manifest generation failed." }

& (Join-Path $PSScriptRoot "check-database-schema-drift.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { throw "DB schema drift check failed." }

& (Join-Path $PSScriptRoot "check-database-profile-standard.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) { throw "DB profile/generated-domain standard check failed." }

Write-Host "CPF DB artifacts synchronized. Canonical SQL -> bundle/vendor pack -> metadata manifest parity PASS."
