param([Parameter(Mandatory=$true)][string]$RepositoryRoot,[switch]$ConfirmDelete)
$ErrorActionPreference='Stop'
if(-not $ConfirmDelete){ throw 'Deletion not authorized. Re-run only after explicit user approval with -ConfirmDelete.' }
$repo=[IO.Path]::GetFullPath($RepositoryRoot)
$manifest=Join-Path $repo 'cpf-docs\work\CPF_DELETE_MANIFEST.csv'
if(-not (Test-Path -LiteralPath $manifest -PathType Leaf)){ throw "Delete manifest missing: $manifest" }
$protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
$rows=Import-Csv -LiteralPath $manifest | Where-Object { $_.delete_status -eq 'PENDING_USER_APPROVAL' }
foreach($row in $rows){
  $rel=($row.path -replace '\\','/').TrimStart('/')
  if($rel.Contains('..')){ throw "Unsafe delete path: $rel" }
  foreach($prefix in $protected){ if($rel.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){ throw "Protected path rejected: $rel" } }
  $target=[IO.Path]::GetFullPath((Join-Path $repo $rel))
  if(-not $target.StartsWith($repo,[StringComparison]::OrdinalIgnoreCase)){ throw "Path escape rejected: $rel" }
  if($row.replacement_path){
    $replacement=[IO.Path]::GetFullPath((Join-Path $repo $row.replacement_path))
    if(-not (Test-Path -LiteralPath $replacement -PathType Leaf)){ throw "Replacement missing; refusing delete: $($row.replacement_path)" }
  }
  if(Test-Path -LiteralPath $target){
    if(-not (Test-Path -LiteralPath $target -PathType Leaf)){ throw "Directory/special path rejected: $rel" }
    Remove-Item -LiteralPath $target -Force
    Write-Host "DELETED $rel"
  } else { Write-Host "ALREADY_ABSENT $rel" }
}
Write-Host "Exact relocation deletion complete. Count=$($rows.Count)"
