param(
 [Parameter(Mandatory=$true)][string]$PackageRoot,
 [Parameter(Mandatory=$true)][string]$RepositoryRoot
)
$ErrorActionPreference='Stop'
$PackageRoot=(Resolve-Path $PackageRoot).Path
$RepositoryRoot=(Resolve-Path $RepositoryRoot).Path
if($PackageRoot-eq$RepositoryRoot){throw 'PackageRoot and RepositoryRoot must differ.'}
$required=@('settings.gradle','cpf-batch\contract','cpf-batch\control-server','cpf-batch\scheduler','cpf-batch\worker','cpf-batch\center-cut-runner','cpf-batch\host-agent','cpf-tools\scripts\config\CPF_FINAL_COMPLETION_DELETE_PATHS.txt')
foreach($item in $required){if(-not(Test-Path(Join-Path $PackageRoot $item))){throw "Incomplete completion package: $item"}}

# File-by-file overlay prevents Copy-Item from accidentally creating nested top-level directories.
Get-ChildItem $PackageRoot -Recurse -File -Force | Where-Object {$_.FullName -notlike '*cpf-tools\scripts\config\CPF_FINAL_COMPLETION_DELETE_PATHS.txt'} | ForEach-Object {
  $relative=$_.FullName.Substring($PackageRoot.Length).TrimStart([IO.Path]::DirectorySeparatorChar,[IO.Path]::AltDirectorySeparatorChar)
  $target=Join-Path $RepositoryRoot $relative
  $parent=Split-Path -Parent $target
  if($parent){New-Item -ItemType Directory -Force -Path $parent|Out-Null}
  Copy-Item $_.FullName $target -Force
}

# Destructive migration happens only after every replacement file is present.
Get-Content (Join-Path $PackageRoot 'cpf-tools\scripts\config\CPF_FINAL_COMPLETION_DELETE_PATHS.txt') |
 Where-Object {$_ -and -not $_.Trim().StartsWith('#')} | ForEach-Object {
  $relative=$_.Trim()
  if($relative -match '(^|[\\/])\.\.([\\/]|$)'){throw "Unsafe delete path: $relative"}
  $target=[IO.Path]::GetFullPath((Join-Path $RepositoryRoot $relative))
  if(-not $target.StartsWith([IO.Path]::GetFullPath($RepositoryRoot),[StringComparison]::OrdinalIgnoreCase)){throw "Delete path escaped repository: $relative"}
  if(Test-Path $target){Remove-Item $target -Recurse -Force}
 }
Write-Host 'CPF final completion overlay applied. Commit/push/branch was NOT performed.'
Write-Host 'Run cpf-tools/scripts/verify-cpf-final-completion.ps1 before committing.'
