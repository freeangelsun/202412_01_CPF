param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [int] $ScopeLimit = 10027,
    [switch] $RepairIndex,
    [switch] $RepairManifests,
    [string] $JsonOutput = ""
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$ScriptPath = Join-Path $PSScriptRoot 'verify-cpf-split-master-dataset.py'
if (-not (Test-Path -LiteralPath $ScriptPath -PathType Leaf)) {
    throw "CPF split master verifier is missing: $ScriptPath"
}

$Python = Get-Command python -ErrorAction SilentlyContinue
$PythonArgs = @()
if ($null -eq $Python) {
    $Python = Get-Command py -ErrorAction SilentlyContinue
    if ($null -eq $Python) { throw 'Python 3 executable was not found (python or py).' }
    $PythonArgs += '-3'
}
$PythonArgs += @($ScriptPath, '--root', $Root, '--scope-limit', $ScopeLimit)
if ($RepairIndex) { $PythonArgs += '--repair-index' }
if ($RepairManifests) { $PythonArgs += '--repair-manifests' }
if (-not [string]::IsNullOrWhiteSpace($JsonOutput)) {
    $PythonArgs += @('--json-output', $JsonOutput)
}

& $Python.Source @PythonArgs
if ($LASTEXITCODE -ne 0) {
    throw "CPF split master validation failed (exit=$LASTEXITCODE)."
}
