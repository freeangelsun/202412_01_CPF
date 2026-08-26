param(
  [string]$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path,
  [string]$Manifest = 'cpf-docs/assets/product-docs/visual-geometry.json'
)
$ErrorActionPreference='Stop'
function Fail([string]$m){ Write-Host ('VISUAL_GEOMETRY=FAIL '+$m); exit 1 }
$path=Join-Path $RepositoryRoot ($Manifest -replace '/','\')
if(-not(Test-Path -LiteralPath $path -PathType Leaf)){ Fail('manifest missing '+$Manifest) }
try{$doc=Get-Content -LiteralPath $path -Raw -Encoding UTF8|ConvertFrom-Json}catch{Fail('manifest json '+$_.Exception.Message)}
if([string]$doc.harnessVersion -ne '2.3.0'){Fail('manifest harnessVersion')}
$issues=0
foreach($a in @($doc.assets)){
  $cw=[double]$a.canvas.width; $ch=[double]$a.canvas.height; $safe=[double]$a.canvas.safeMargin
  if($cw-le0-or$ch-le0-or$safe-lt64){Fail('canvas '+$a.asset)}
  $assetPath=Join-Path $RepositoryRoot ([string]$a.asset -replace '/','\')
  if(-not(Test-Path -LiteralPath $assetPath -PathType Leaf)){Fail('asset missing '+$a.asset)}
  $objects=@($a.objects)
  foreach($o in $objects){
    $x=[double]$o.x;$y=[double]$o.y;$w=[double]$o.w;$h=[double]$o.h
    if($w-le0-or$h-le0){Fail('invalid box '+$a.asset+' '+$o.id)}
    if($x-lt$safe-or$y-lt$safe-or($x+$w)-gt($cw-$safe)-or($y+$h)-gt($ch-$safe)){
      if([string]$o.allowSafeMargin -ne 'true'){Fail('safe-area '+$a.asset+' '+$o.id)}
    }
    if([string]$o.kind -eq 'text' -and $null-ne$o.parent){
      $p=@($objects|Where-Object{$_.id-eq$o.parent})|Select-Object -First 1
      if($null-eq$p){Fail('parent missing '+$a.asset+' '+$o.id)}
      $pad=28
      if($x-lt([double]$p.x+$pad)-or$y-lt([double]$p.y+$pad)-or($x+$w)-gt([double]$p.x+[double]$p.w-$pad)-or($y+$h)-gt([double]$p.y+[double]$p.h-$pad)){
        Fail('text outside parent '+$a.asset+' '+$o.id)
      }
    }
  }
  # Explicit pair constraints are generated with the visual and are deterministic to validate in PowerShell.
  foreach($pair in @($a.noOverlap)){
    $one=@($objects|Where-Object{$_.id-eq$pair[0]})|Select-Object -First 1
    $two=@($objects|Where-Object{$_.id-eq$pair[1]})|Select-Object -First 1
    if($null-eq$one-or$null-eq$two){Fail('overlap ref missing '+$a.asset)}
    $over=([double]$one.x-lt([double]$two.x+[double]$two.w))-and([double]$one.x+[double]$one.w-gt[double]$two.x)-and([double]$one.y-lt([double]$two.y+[double]$two.h))-and([double]$one.y+[double]$one.h-gt[double]$two.y)
    if($over){Fail('overlap '+$a.asset+' '+$pair[0]+' '+$pair[1])}
  }
  foreach($g in @($a.minVerticalGaps)){
    $one=@($objects|Where-Object{$_.id-eq$g.from})|Select-Object -First 1
    $two=@($objects|Where-Object{$_.id-eq$g.to})|Select-Object -First 1
    if($null-eq$one-or$null-eq$two){Fail('gap ref missing '+$a.asset)}
    $gap=[double]$two.y-([double]$one.y+[double]$one.h)
    if($gap-lt[double]$g.min){Fail('vertical gap '+$a.asset+' '+$g.from+' '+$g.to+' actual='+$gap)}
  }
}
Write-Host ('VISUAL_GEOMETRY=PASS ASSETS='+@($doc.assets).Count)
exit 0
