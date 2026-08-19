param(
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
  [string]$PublicRepositoryUrl = '',
  [string]$CommitMessage = '',
  [switch]$ExcludeBza,
  [switch]$Push
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$python = Get-Command python -ErrorAction Stop
$argsList = @((Join-Path $PSScriptRoot 'publish-cpf-public-repository.py'),'--root',$Root)
if (-not [string]::IsNullOrWhiteSpace($PublicRepositoryUrl)) { $argsList += @('--remote',$PublicRepositoryUrl) }
if ($ExcludeBza) { $argsList += '--exclude-bza' }
if (-not [string]::IsNullOrWhiteSpace($CommitMessage)) { $argsList += @('--commit-message',$CommitMessage) }
if ($Push) { $argsList += '--push' }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw "CPF public repository publication failed. Push was not completed." }
