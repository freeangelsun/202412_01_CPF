$ErrorActionPreference='Stop'
$Root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..'))
$Manifest=Join-Path $Root 'cpf-docs\assets\product-docs\visual-geometry.json'
if(!(Test-Path -LiteralPath $Manifest)){throw 'VISUAL_GEOMETRY=FAIL manifest missing'}
$d=Get-Content -LiteralPath $Manifest -Raw -Encoding utf8 | ConvertFrom-Json
if([string]$d.harnessVersion -ne '2.10.0'){throw 'VISUAL_GEOMETRY=FAIL manifest harnessVersion; regenerate/re-manifest current assets'}
if([string]$d.schemaVersion -ne '2.0'){throw 'VISUAL_GEOMETRY=FAIL schemaVersion 2.0 required'}
foreach($a in $d.assets){
  if([double]$a.canvas.safeMargin -lt 64){throw "VISUAL_GEOMETRY=FAIL safeMargin $($a.asset)"}
  $ids=@{}; foreach($o in $a.objects){$ids[[string]$o.id]=$o}
  $meaningful=@($a.objects|Where-Object{$_.kind -in @('node','text','annotation','junction')})
  if($meaningful.Count -lt 3){throw "VISUAL_GEOMETRY=FAIL coarse manifest $($a.asset)"}
  if($null -eq $a.connectors){throw "VISUAL_GEOMETRY=FAIL connectors missing $($a.asset)"}
  foreach($c in $a.connectors){
    if(!$ids.ContainsKey([string]$c.from) -or !$ids.ContainsKey([string]$c.to)){throw "VISUAL_GEOMETRY=FAIL connector refs $($a.asset)"}
    if(@($c.points).Count -lt 2){throw "VISUAL_GEOMETRY=FAIL route missing $($a.asset)"}
    if([double]$c.targetInteriorPenetrationPx -gt 0){throw "VISUAL_GEOMETRY=FAIL target intrusion $($a.asset)"}
    if([double]$c.arrowheadBodyInsideTargetPx -gt 0){throw "VISUAL_GEOMETRY=FAIL arrowhead intrusion $($a.asset)"}
    if($c.crossesUnrelatedNodeInterior -eq $true -or $c.crossesTextOrLabel -eq $true){throw "VISUAL_GEOMETRY=FAIL connector collision $($a.asset)"}
    if($c.endsInUnlabeledEmptySpace -eq $true){throw "VISUAL_GEOMETRY=FAIL empty connector end $($a.asset)"}
  }
  if(@($a.embeddedRenders).Count -eq 0){throw "VISUAL_GEOMETRY=FAIL embedded renders missing $($a.asset)"}
  foreach($r in $a.embeddedRenders){if([double]$r.effectiveMinTextPt -lt 10.5 -or [int]$r.crop -ne 0 -or [int]$r.boundaryIntrusion -ne 0 -or $r.contrastPass -ne $true){throw "VISUAL_GEOMETRY=FAIL embedded render $($a.asset) $($r.surface)"}}
}
Write-Host "VISUAL_GEOMETRY=PASS ASSETS=$(@($d.assets).Count)"
