$ErrorActionPreference='Stop'
$canonical=Join-Path (Split-Path $PSScriptRoot -Parent) 'create-domain.ps1'
if(-not (Test-Path $canonical)){throw "Canonical CPF generator not found: $canonical"}
& $canonical @args
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
