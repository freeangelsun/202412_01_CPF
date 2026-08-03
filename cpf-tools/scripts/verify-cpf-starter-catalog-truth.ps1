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
if (-not $python) { throw 'Python 3 is required for the CPF starter catalog truth gate.' }

$arguments = @(
    (Join-Path $Root 'cpf-tools/scripts/verify-cpf-starter-catalog-truth.py'),
    '--root', $Root
)
if ($MetadataOnly) { $arguments += '--metadata-only' }
if (-not [string]::IsNullOrWhiteSpace($JsonOutput)) {
    $arguments += @('--json-output', $JsonOutput)
}
& $python.Source @arguments
if ($LASTEXITCODE -ne 0) { throw "CPF starter catalog truth gate failed (exit=$LASTEXITCODE)" }
