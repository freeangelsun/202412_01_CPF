param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [switch] $Apply
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$python = Get-Command python -ErrorAction Stop
$tool = Join-Path $PSScriptRoot 'sync_platform_seed_currentization.py'
$arguments = @($tool, '--root', $Root)
if ($Apply) { $arguments += '--write' }
& $python.Source @arguments
if ($LASTEXITCODE -ne 0) {
    throw "Platform seed currentization sync failed. exitCode=$LASTEXITCODE"
}
