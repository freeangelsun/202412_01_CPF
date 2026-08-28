$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim())
$m=Join-Path $root 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt'
if(!(Test-Path -LiteralPath $m -PathType Leaf)){throw "DELETE MANIFEST NOT FOUND: $m"}
$protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
Get-Content -LiteralPath $m -Encoding UTF8|ForEach-Object{
  $r=$_.Trim(); if(!$r -or $r.StartsWith('#')){return}
  if([IO.Path]::IsPathRooted($r)-or$r.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($r)){throw "UNSAFE DELETE: $r"}
  $norm=$r.Replace('\\','/').TrimStart('/')
  if($norm -eq 'cpf-docs/deliverables/documentation' -or $norm -eq 'cpf-docs/guides' -or $norm -eq 'cpf-docs/governance/documentation-harness'){throw "CURRENT CANONICAL DELETE FORBIDDEN: $r"}
  $sep=[IO.Path]::DirectorySeparatorChar
  $rootPrefix=$root.TrimEnd($sep)+$sep
  $rel=$r.Replace('/',[string]$sep)
  $p=[IO.Path]::GetFullPath((Join-Path $root $rel))
  if(!$p.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $r"}
  if(Test-Path -LiteralPath $p){Remove-Item -LiteralPath $p -Recurse -Force;Write-Host "DELETED=$r"}
}
Write-Host '[CPF][DOC] EXACT_STALE_DELETE_PASS'
