param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $JsonOutput = ''
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python 3 is required for the CPF DB-less fail-closed gate.' }
$argsList = @((Join-Path $Root 'cpf-tools/scripts/verify-cpf-db-less-fail-closed.py'), '--root', $Root)
if (-not [string]::IsNullOrWhiteSpace($JsonOutput)) { $argsList += @('--json-output', $JsonOutput) }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw "CPF DB-less fail-closed gate failed (exit=$LASTEXITCODE)" }
