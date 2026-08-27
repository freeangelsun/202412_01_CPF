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
    'docxOpeningMetaTable' {$failed=([string]$i.presentation -eq 'table' -and @($i.labels) -match '누가 보는가|이 문서로 끝낼 일|기준')}
    'docxUserFacingProvenance' {$failed=([string]$i.text -match '(?i)(Harness\s*(?:v)?\d+(?:\.\d+)+|Source\s*(?:SHA\s*)?[0-9A-F]{16,})')}
    'docxSingleRowLayoutTable' {$failed=([int]$i.rows -eq 1 -and [string]$i.purpose -match 'single message/callout|reader metadata|layout')}
    'isolatedTrailingContentPage' {$failed=([bool]$i.isLastPage -and [int]$i.meaningfulBlocks -le 2 -and [double]$i.contentOccupancyRatio -lt 0.30)}
    'feedbackFixedOnlyInArtifactWithoutHarnessGate' {$failed=([bool]$i.userFinding -and [bool]$i.artifactPatched -and (-not [bool]$i.harnessRuleAdded -or -not [bool]$i.negativeFixtureAdded))}
    'connectorCrossesUnrelatedNode' {$failed=([bool]$i.crossesUnrelatedNodeInterior)}
    'connectorCrossesTextOrLabel' {$failed=([bool]$i.crossesTextOrLabel)}
    'connectorEndpointNotOnTargetBoundary' {$failed=([double]$i.targetBoundaryDistancePx -gt 2.0)}
    'connectorSourceNodeIntrusion' {$failed=([double]$i.sourceInteriorPenetrationPx -gt 0)}
    'tocTabStopOutsideWritableArea' {$failed=([double]$i.tabStopTwips -gt [double]$i.writableWidthTwips -or [bool]$i.pageNumberVisible -eq $false)}
    default {$errors += "$($x.id): unknown trigger $trigger"}
  }
  if(-not $failed){$errors += "$($x.id): fixture was not rejected"}
  if(-not ($v.hardFail.PSObject.Properties.Name -contains $trigger) -or [int]$v.hardFail.$trigger -ne 0){$errors += "$($x.id): hardFail missing"}
}
if($errors.Count -gt 0){Write-Host 'QUALITY_FIXTURES=FAIL';$errors|ForEach-Object{Write-Host "- $_"};throw 'QUALITY_FIXTURES_FAIL'}
Write-Host "QUALITY_FIXTURES=PASS COUNT=$($f.fixtures.Count)"
