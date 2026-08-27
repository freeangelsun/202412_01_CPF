$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$f=Get-Content -LiteralPath (Join-Path $Root 'quality-fixtures.json') -Raw -Encoding utf8 | ConvertFrom-Json
$v=Get-Content -LiteralPath (Join-Path $Root 'visual-qa.json') -Raw -Encoding utf8 | ConvertFrom-Json
$errors=@()
foreach($x in $f.fixtures){
  $i=$x.input; $trigger=[string]$x.mustTrigger; $failed=$false
  switch($trigger){
    'readerOpeningEncodedAsTable' {$failed=([string]$i.presentation -eq 'table')}
    'userFacingHarnessVersion' {$failed=([string]$i.text -match 'Harness\s+\d')}
    'userFacingSourceSha' {$failed=([string]$i.text -match 'Source\s+[0-9A-Fa-f]{16,}')}
    'tableHeaderWrap' {$failed=([int]$i.headerVisualLines -gt 1)}
    'nonTabularContentEncodedAsTable' {$failed=([int]$i.rows -lt 2 -or [int]$i.dimensions -lt 2 -or [string]$i.purpose -eq 'reader metadata')}
    'connectorTargetNodeIntrusion' {$failed=([double]$i.targetInteriorPenetrationPx -gt 0)}
    'connectorArrowheadInsideTargetNode' {$failed=([double]$i.arrowheadBodyInsideTargetPx -gt 0)}
    'connectorEndsInUnlabeledEmptySpace' {$failed=([string]$i.to -eq 'EMPTY' -and -not $i.junctionLabel)}
    'graphicalObjectContrastBelow3to1' {$failed=([double]$i.graphicalObjectContrast -lt 3.0)}
    'tableTextContrastBelow4to5to1' {$failed=([double]$i.tableTextContrast -lt 4.5)}
    'colorOnlyMeaning' {$failed=($i.meaningEncodedBy.Count -eq 1 -and [string]$i.meaningEncodedBy[0] -eq 'color')}
    'embeddedEffectiveTextTooSmall' {$failed=([double]$i.effectiveMinTextPt -lt 10.5)}
    'coarseGeometryManifestAcceptedAsPass' {$failed=($i.meaningfulNodesDetailed -eq $false)}
    default {$errors += "$($x.id): unknown trigger $trigger"}
  }
  if(-not $failed){$errors += "$($x.id): fixture was not rejected"}
  if(-not ($v.hardFail.PSObject.Properties.Name -contains $trigger) -or [int]$v.hardFail.$trigger -ne 0){$errors += "$($x.id): hardFail missing"}
}
if($errors.Count -gt 0){Write-Host 'QUALITY_FIXTURES=FAIL';$errors|ForEach-Object{Write-Host "- $_"};throw 'QUALITY_FIXTURES_FAIL'}
Write-Host "QUALITY_FIXTURES=PASS COUNT=$($f.fixtures.Count)"
