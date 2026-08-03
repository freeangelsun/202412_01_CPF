param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $MetadataOnly,
    [string] $JsonOutput = ''
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python 3 is required for the CPF DB vendor manifest gate.' }
$argsList = @((Join-Path $Root 'cpf-tools/scripts/verify-cpf-db-vendor-manifest.py'), '--root', $Root)
if ($MetadataOnly) { $argsList += '--metadata-only' }
if (-not [string]::IsNullOrWhiteSpace($JsonOutput)) { $argsList += @('--json-output', $JsonOutput) }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw "CPF DB vendor manifest gate failed (exit=$LASTEXITCODE)" }
