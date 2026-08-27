param([string]$ZipPath='')
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((git rev-parse --show-toplevel).Trim())
if([string]::IsNullOrWhiteSpace($ZipPath)){
  $cand=@(Get-ChildItem -LiteralPath (Join-Path $HOME 'Downloads') -File -Filter 'CPF_DOCUMENTATION_V2_3_0_FINAL_OVERLAY_*.zip' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
  if($cand.Count-eq 0){throw 'FINAL OVERLAY ZIP NOT FOUND under $HOME\Downloads'}
  $ZipPath=$cand[0].FullName
}
$ZipPath=[IO.Path]::GetFullPath($ZipPath)
if(!(Test-Path -LiteralPath $ZipPath -PathType Leaf)){throw "ZIP NOT FOUND: $ZipPath"}
$tmp=Join-Path $env:TEMP ('cpf-doc-v230-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tmp -Force|Out-Null
try{
  Expand-Archive -LiteralPath $ZipPath -DestinationPath $tmp -Force
  $manifest=Join-Path $tmp 'cpf-docs\deliverables\documentation\DELETE_MANIFEST.txt'
  if(!(Test-Path -LiteralPath $manifest -PathType Leaf)){throw 'DELETE_MANIFEST.txt missing from overlay'}
  Get-Content -LiteralPath $manifest -Encoding UTF8|ForEach-Object{
    $r=$_.Trim()
    if($r-and-not$r.StartsWith('#')){
      if([IO.Path]::IsPathRooted($r)-or$r.Contains('..')-or[Management.Automation.WildcardPattern]::ContainsWildcardCharacters($r)){throw "UNSAFE DELETE: $r"}
      $p=[IO.Path]::GetFullPath((Join-Path $root ($r-replace '/','\')))
      if(!$p.StartsWith($root.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){throw "OUTSIDE ROOT: $r"}
      if(Test-Path -LiteralPath $p){
        if((Get-Item -LiteralPath $p).PSIsContainer){throw "DIRECTORY DELETE FORBIDDEN: $r"}
        Remove-Item -LiteralPath $p -Force
      }
    }
  }
  Get-ChildItem -LiteralPath $tmp -Force|ForEach-Object{Copy-Item -LiteralPath $_.FullName -Destination $root -Recurse -Force}
  Write-Host "[CPF][DOC] APPLIED=$ZipPath"
} finally {Remove-Item -LiteralPath $tmp -Recurse -Force -ErrorAction SilentlyContinue}
