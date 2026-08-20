param(
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
  [string]$PublicRepositoryUrl = '',
  [string]$OutputRoot = '',
  [switch]$ExcludeBackoffice
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$python = Get-Command python -ErrorAction Stop
$argsList = @((Join-Path $PSScriptRoot 'publish-cpf-public-repository.py'),'--root',$Root)
if (-not [string]::IsNullOrWhiteSpace($PublicRepositoryUrl)) { $argsList += @('--remote',$PublicRepositoryUrl) }
if (-not [string]::IsNullOrWhiteSpace($OutputRoot)) { $argsList += @('--output-root',$OutputRoot) }
if ($ExcludeBackoffice) { $argsList += '--exclude-backoffice' }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw 'CPF public release preparation failed. No commit/push was executed.' }
