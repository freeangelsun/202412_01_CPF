param([Parameter(Mandatory=$true)][string]$RepositoryRoot)
$ErrorActionPreference='Stop'
$repo=[IO.Path]::GetFullPath($RepositoryRoot)
$overlay=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
if(-not (Test-Path -LiteralPath (Join-Path $repo 'settings.gradle') -PathType Leaf)){ throw "RepositoryRoot is not a CPF repository root: $repo" }
Get-ChildItem -LiteralPath $overlay -Recurse -File | ForEach-Object {
  $rel=[IO.Path]::GetRelativePath($overlay,$_.FullName)
  $dst=[IO.Path]::GetFullPath((Join-Path $repo $rel))
  if(-not $dst.StartsWith($repo,[StringComparison]::OrdinalIgnoreCase)){ throw "Path escape rejected: $rel" }
  $parent=Split-Path -Parent $dst
  if(-not (Test-Path -LiteralPath $parent)){ New-Item -ItemType Directory -Path $parent -Force | Out-Null }
  Copy-Item -LiteralPath $_.FullName -Destination $dst -Force
}
Write-Host "CPF S18 overlay applied without deletions: $repo"
