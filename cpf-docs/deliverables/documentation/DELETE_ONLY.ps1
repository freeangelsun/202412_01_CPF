$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim())
$m=Join-Path $root 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt'
if(!(Test-Path -LiteralPath $m)){throw "DELETE MANIFEST NOT FOUND: $m"}
Get-Content -LiteralPath $m -Encoding UTF8 | ForEach-Object {
  $r=$_.Trim()
  if($r -and -not $r.StartsWith('#')){
    if([IO.Path]::IsPathRooted($r)-or$r.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($r)){throw "UNSAFE DELETE: $r"}
    $p=[IO.Path]::GetFullPath((Join-Path $root ($r-replace '/','\')))
    if(!$p.StartsWith($root.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $r"}
    if(Test-Path -LiteralPath $p){if((Get-Item -LiteralPath $p).PSIsContainer){throw "DIRECTORY DELETE FORBIDDEN: $r"};Remove-Item -LiteralPath $p -Force}
  }
}
Write-Host '[CPF][DOC] DELETE_MANIFEST_APPLIED'
