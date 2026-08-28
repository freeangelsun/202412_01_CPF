param(
  [ValidateSet('build','check','status','setup')]
  [string]$Action = 'build',
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
  [string]$Remote = '',
  [string]$GeneratorArtifacts = '',
  [ValidateSet('binary','source')]
  [string]$Profile = 'binary'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$python = Get-Command python -ErrorAction Stop
$argsList = @((Join-Path $PSScriptRoot 'cpf_open_git.py'), $Action, '--root', $Root, '--profile', $Profile)
if (-not [string]::IsNullOrWhiteSpace($Remote)) { $argsList += @('--remote', $Remote) }
if (-not [string]::IsNullOrWhiteSpace($GeneratorArtifacts)) { $argsList += @('--generator-artifacts', $GeneratorArtifacts) }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw "CPF Open Git $Action failed. No commit/push was executed." }
