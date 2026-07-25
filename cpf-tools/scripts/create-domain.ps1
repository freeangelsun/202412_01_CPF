param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GeneratorArgs)
$ErrorActionPreference='Stop'
$canonical=Join-Path (Split-Path $PSScriptRoot -Parent) 'generator/create-domain.ps1'
if(-not (Test-Path $canonical)){throw "Canonical CPF generator not found: $canonical"}
& $canonical @GeneratorArgs
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
