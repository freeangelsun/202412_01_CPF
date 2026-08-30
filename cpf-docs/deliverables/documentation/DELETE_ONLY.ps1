$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim());Set-Location $root
$m=Join-Path $root 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt';if(!(Test-Path -LiteralPath $m -PathType Leaf)){throw 'DELETE_MANIFEST.txt missing'}
$sep=[IO.Path]::DirectorySeparatorChar;$rootPrefix=$root.TrimEnd($sep)+$sep
Get-Content -LiteralPath $m -Encoding UTF8|ForEach-Object{$r=$_.Trim();if(!$r-or$r.StartsWith('#')){return};if([IO.Path]::IsPathRooted($r)-or$r.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($r)){throw "UNSAFE DELETE: $r"};$norm=$r.Replace('\','/').TrimStart('/');if($norm -eq 'cpf-docs/deliverables/documentation' -or $norm -eq 'cpf-docs/guides' -or $norm -eq 'cpf-docs/governance/documentation-harness'){throw "CURRENT CANONICAL DELETE FORBIDDEN: $r"};$p=[IO.Path]::GetFullPath((Join-Path $root ($r.Replace('/',[string]$sep))));if(!$p.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $r"};if(Test-Path -LiteralPath $p){Remove-Item -LiteralPath $p -Recurse -Force;Write-Host "DELETED=$r"}}
Get-ChildItem -LiteralPath (Join-Path $root 'cpf-docs') -Directory -Recurse|Sort-Object FullName -Descending|Where-Object{-not (Get-ChildItem -LiteralPath $_.FullName -Force|Select-Object -First 1)}|Remove-Item -Force
Write-Host '[CPF][DOC] CURRENT-ONLY CLEANUP PASS'
