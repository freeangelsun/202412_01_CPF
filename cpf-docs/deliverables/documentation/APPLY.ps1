param([Parameter(Mandatory=$true)][string]$ZipPath)
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim());Set-Location $root
$ZipPath=[IO.Path]::GetFullPath($ZipPath)
if(!(Test-Path -LiteralPath $ZipPath -PathType Leaf)){throw "ZIP NOT FOUND: $ZipPath"}
$tmp=Join-Path $env:TEMP ('cpf-doc-2120-'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path $tmp -Force|Out-Null
try{
  Expand-Archive -LiteralPath $ZipPath -DestinationPath $tmp -Force
  $m=Join-Path $tmp 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt'
  if(!(Test-Path -LiteralPath $m -PathType Leaf)){throw 'DELETE_MANIFEST.txt missing from overlay'}
  Get-Content -LiteralPath $m -Encoding UTF8|ForEach-Object{
    $r=$_.Trim();if(!$r-or$r.StartsWith('#')){return}
    if([IO.Path]::IsPathRooted($r)-or$r.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($r)){throw "UNSAFE DELETE: $r"}
    $norm=$r.Replace('\','/').TrimStart('/')
    if($norm -eq 'cpf-docs/deliverables/documentation' -or $norm -eq 'cpf-docs/guides' -or $norm -eq 'cpf-docs/governance/documentation-harness'){throw "CURRENT CANONICAL DELETE FORBIDDEN: $r"}
    $sep=[IO.Path]::DirectorySeparatorChar;$rootPrefix=$root.TrimEnd($sep)+$sep;$rel=$r.Replace('/',[string]$sep);$p=[IO.Path]::GetFullPath((Join-Path $root $rel))
    if(!$p.StartsWith($rootPrefix,[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $r"}
    if(Test-Path -LiteralPath $p){Remove-Item -LiteralPath $p -Recurse -Force;Write-Host "DELETED=$r"}
  }
  Get-ChildItem -LiteralPath $tmp -Force|ForEach-Object{Copy-Item -LiteralPath $_.FullName -Destination $root -Recurse -Force}
  Write-Host "[CPF][DOC] APPLIED=2.12.0 ZIP=$ZipPath"
} finally {Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}
