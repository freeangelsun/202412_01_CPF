$ErrorActionPreference='Stop'
$root=(git rev-parse --show-toplevel 2>$null).Trim(); if($LASTEXITCODE-ne0-or[string]::IsNullOrWhiteSpace($root)){throw 'CPF GIT ROOT NOT FOUND'}
$root=[IO.Path]::GetFullPath($root)
$m=Join-Path $root 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt'
Get-Content -LiteralPath $m -Encoding UTF8 | ForEach-Object {
  $s=$_.Trim(); if(!$s -or $s.StartsWith('#')){return}
  if([IO.Path]::IsPathRooted($s)-or$s.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($s)){throw "UNSAFE DELETE PATH: $s"}
  $p=[IO.Path]::GetFullPath((Join-Path $root ($s -replace '/','\')))
  if(!$p.StartsWith($root.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $s"}
  if(Test-Path -LiteralPath $p){$i=Get-Item -LiteralPath $p -Force;if($i.PSIsContainer){throw "DIRECTORY DELETE FORBIDDEN: $s"};Remove-Item -LiteralPath $p -Force;Write-Host "DELETED=$s"}
}
Write-Host '[CPF][DOC] DELETE_MANIFEST PASS'
