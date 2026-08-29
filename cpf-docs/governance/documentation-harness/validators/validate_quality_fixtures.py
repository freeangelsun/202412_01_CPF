#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
F=json.loads((ROOT/'quality-fixtures.json').read_text(encoding='utf-8'))
V=json.loads((ROOT/'visual-qa.json').read_text(encoding='utf-8'))

def evaluate(x):
    i=x.get('input',{}); trig=x['mustTrigger']
    if trig=='readerOpeningEncodedAsTable': return i.get('presentation')=='table'
    if trig=='userFacingHarnessVersion': return bool(re.search(r'Harness\s+(?:v)?\d',i.get('text',''),re.I))
    if trig=='userFacingSourceSha': return bool(re.search(r'Source\s+(?:SHA\s*)?[0-9A-F]{16,}',i.get('text',''),re.I))
    if trig=='tableHeaderWrap': return i.get('headerVisualLines',0)>1
    if trig=='nonTabularContentEncodedAsTable': return i.get('rows',0)<2 or i.get('dimensions',0)<2 or i.get('purpose')=='reader metadata'
    if trig=='connectorTargetNodeIntrusion': return i.get('targetInteriorPenetrationPx',0)>0
    if trig=='connectorArrowheadInsideTargetNode': return i.get('arrowheadBodyInsideTargetPx',0)>0
    if trig=='connectorEndsInUnlabeledEmptySpace': return i.get('to')=='EMPTY' and not i.get('junctionLabel')
    if trig=='graphicalObjectContrastBelow3to1': return float(i.get('graphicalObjectContrast',99))<3.0
    if trig=='tableTextContrastBelow4to5to1': return float(i.get('tableTextContrast',99))<4.5
    if trig=='colorOnlyMeaning': return i.get('meaningEncodedBy')==['color']
    if trig=='embeddedEffectiveTextTooSmall': return float(i.get('effectiveMinTextPt',99))<10.5
    if trig=='coarseGeometryManifestAcceptedAsPass': return i.get('meaningfulNodesDetailed') is False
    if trig=='docxOpeningMetaTable': return i.get('presentation')=='table' and any(x in i.get('labels',[]) for x in ['누가 보는가','이 문서로 끝낼 일','기준'])
    if trig=='docxUserFacingProvenance': return bool(re.search(r'(Harness\s*(?:v)?\d+(?:\.\d+)+|Source\s*(?:SHA\s*)?[0-9A-F]{16,})',i.get('text',''),re.I))
    if trig=='docxSingleRowLayoutTable': return int(i.get('rows',0))==1 and i.get('purpose') in ('single message/callout','reader metadata','layout')
    if trig=='isolatedTrailingContentPage': return i.get('isLastPage') is True and int(i.get('meaningfulBlocks',99))<=2 and float(i.get('contentOccupancyRatio',1))<0.30
    if trig=='feedbackFixedOnlyInArtifactWithoutHarnessGate': return i.get('userFinding') is True and i.get('artifactPatched') is True and (i.get('harnessRuleAdded') is not True or i.get('negativeFixtureAdded') is not True)
    if trig=='connectorCrossesUnrelatedNode': return i.get('crossesUnrelatedNodeInterior') is True
    if trig=='connectorCrossesTextOrLabel': return i.get('crossesTextOrLabel') is True
    if trig=='connectorEndpointNotOnTargetBoundary': return float(i.get('targetBoundaryDistancePx',0))>2.0
    if trig=='connectorSourceNodeIntrusion': return float(i.get('sourceInteriorPenetrationPx',0))>0
    if trig=='tocTabStopOutsideWritableArea': return float(i.get('tabStopTwips',0))>float(i.get('writableWidthTwips',0)) or i.get('pageNumberVisible') is False
    if trig=='unjustifiedEqualWidthByContent': return i.get('equalWidths') is True and i.get('symmetricComparison') is False and float(i.get('contentDemandVariancePct',0))>12
    if trig=='excessiveCellWrapDensity': return float(i.get('longColumnMedianLines',0))>=4 and float(i.get('shortColumnMedianLines',99))<=1.5 and int(i.get('repeatedRows',0))>=3
    if trig=='majorSectionBreathingInsufficient': return float(i.get('majorHeadingGapBeforePt',99))<float(i.get('minimumPt',0))
    if trig=='semanticBlocksVisuallyCrowded': return float(i.get('semanticTransitionGapPt',99))<float(i.get('minimumPt',0)) and int(i.get('denseBlocks',0))>=3
    if trig=='tableFragmentOrphan': return int(i.get('headerRows',0))>=1 and int(i.get('dataRowsOnPage',99))<=1 and i.get('continuesNextPage') is True
    if trig=='readerTopTaskMissing': return i.get('present') is False
    if trig=='readmeValueCoverageInsufficient': return int(i.get('visibleValueGroupsBeforeArchitecture',99))<int(i.get('requiredValueGroups',0))
    if trig=='promotionalBenefitHeading': return bool(re.search(r'(장점|왜\s*좋|달라지는|좋아지는|편해지는|차별점|효익|핵심\s*해석|기반\s*기술)',i.get('heading',''),re.I))
    if trig=='readmeDedicatedBenefitSection': return i.get('benefitOnlySection') is True
    if trig=='readmeDenseWallOfText': return int(i.get('consecutiveLongParagraphs',0))>=3 and int(i.get('longParagraphChars',0))>=220
    if trig=='readmeConsecutiveTables': return int(i.get('consecutiveTables',0))>=2
    if trig=='readmeConsecutiveLargeFigures': return int(i.get('consecutiveLargeFiguresWithoutExplanation',0))>=2
    if trig=='readmeValueConcentratedInOneSection': return float(i.get('maxValueShareInSingleSectionPct',0))>float(i.get('allowedMaxPct',45))
    if trig=='semanticTableWidthInversion': return int(i.get('rows',0))>=3 and float(i.get('longWidthPct',99))<=float(i.get('shortWidthPct',0))
    if trig=='renderedTableWidthMismatch': return abs(float(i.get('expectedSharePct',0))-float(i.get('actualSharePct',0)))>float(i.get('tolerancePct',15))
    if trig=='shortTokenWrap': return int(i.get('visualLines',1))>1
    if trig=='excessiveCellWrap': return int(i.get('visualLines',0))>4 and int(i.get('repeatedRows',0))>=3
    if trig=='insufficientH1Breathing': return i.get('level')=='H1' and float(i.get('spaceBeforePt',99))<float(i.get('minimumPt',52))
    if trig=='insufficientH2Breathing': return i.get('level')=='H2' and float(i.get('spaceBeforePt',99))<float(i.get('minimumPt',28))
    if trig=='readerTaskKeywordOnlyFalseGreen': return i.get('keywordPresent') is True and i.get('taskActionable') is False
    if trig=='manualGateNotExecuted': return i.get('required') is True and i.get('status')!='PASS'
    if trig=='manualEvidenceMissing': return i.get('requiredManualGate') is True and i.get('status')=='PASS' and not i.get('evidenceRefs')
    if trig=='requiredGateNonPass': return i.get('required') is True and i.get('status')!='PASS'
    if trig=='automatedOnlyFinalPassAttempt': return i.get('finalStatus')=='PASS' and i.get('automatedAllPass') is True and i.get('manualAllPass') is not True

    if trig=='documentTotalSizeCap': return any(i.get(k) not in (None,'NONE',False) for k in ['totalPageLimit','totalByteLimit','totalWordLimit','totalCharacterLimit','h2UpperBound','figureUpperBound'])
    if trig=='coverageTruncatedForLength': return i.get('requiredCoverage') is True and bool(re.search(r'(page|length|size|페이지|길이|용량)',str(i.get('removedBecause','')),re.I))
    if trig=='readmeBrochureStructureMissing': return not (i.get('hero') is True and i.get('darkCpfOwnedSurface') is True and i.get('visualNarrative') is True)
    if trig=='readmeVisualKoreanCompanionMissing': return i.get('informativeFigure') is True and i.get('koreanCompanion') is not True
    if trig=='readmeImageAltMissing': return i.get('informativeFigure') is True and not str(i.get('altText','')).strip()
    if trig=='readmeBrochureVisualRhythmMissing': return i.get('tableHeavyLandingPage') is True or i.get('visualNarrative') is not True
    if trig=='informationArchitectureReaderNeedMismatch': return i.get('profileReaderNeedMatched') is not True
    if trig=='longDocumentNavigationMissing': return i.get('longDocument') is True and i.get('tocOrEquivalentNavigation') is not True
    if trig=='fixedWidthTableCausesWrap': return i.get('fixedWidth') is True and i.get('shortTokenWrap') is True
    if trig=='manualFreshEyesReviewMissing': return i.get('scanPass') is not True or i.get('detailPass') is not True
    if trig=='darkTableHeaderAutomaticOrDarkText': return i.get('darkHeader') is True and (str(i.get('textColor','')).upper() in ('','AUTO','000000') or float(i.get('contrast',99))<4.5)
    if trig=='readmeViewerSectionBreathingInsufficient': return int(i.get('viewerWidthPx',0)) in (900,1200,1440) and float(i.get('majorSectionGapPx',99))<float(i.get('minimumPx',0))
    if trig=='readmeHeaderWrapAt900or1200': return int(i.get('viewerWidthPx',0)) in (900,1200,1440) and int(i.get('headerVisualLines',1))>1
    if trig=='visualSafeAreaClipAtViewerWidth': return int(i.get('viewerWidthPx',0)) in (900,1200,1440) and (int(i.get('crop',0))!=0 or int(i.get('boundaryIntrusion',0))!=0)
    if trig=='approvedBaselineQualityRegression': return i.get('approvedBaseline') is True and i.get('changedOutsideFindingScope') is True and i.get('qualityRegressed') is True
    if trig=='windowsVsCodeMarkdownPreviewRuntimeFailure': return i.get('platform')=='windows' and i.get('previewRequired') is True and i.get('runtimeStatus')=='FAIL'
    if trig=='selectionWithoutNextAction': return i.get('selectionPresented') is True and i.get('nextActionPresent') is not True
    if trig=='apiSummaryWithoutWorkingExample': return int(i.get('apiSummaryTables',0))>=1 and i.get('workingExampleOrProcedure') is not True
    if trig=='developerChapterTableWall': return i.get('developerChapter') is True and int(i.get('semanticTables',0))>=2 and int(i.get('actionBlocks',0))==0
    if trig=='readmeDenseCenteredHero': return i.get('centeredHero') is True and int(i.get('bodyChars',0))>int(i.get('limitChars',260))
    if trig=='readmeFlatLongNavigation': return int(i.get('navigationListItems',0))>int(i.get('limit',6)) and i.get('grouped') is not True
    if trig=='readmeStackedCodeBlocks': return int(i.get('consecutiveCodeBlocks',0))>=3 and i.get('purposeTextBetween') is not True
    if trig=='longFlatListWall': return int(i.get('flatListItems',0))>=7 and int(i.get('semanticGroups',1))<=1
    if trig=='consecutiveLongBulletWall': return int(i.get('consecutiveLongBullets',0))>=6 and int(i.get('charsEachAtLeast',0))>=90
    if trig=='heavyBlockWall': return int(i.get('consecutiveHeavyBlocks',0))>=4 and i.get('semanticExplanationBetween') is not True
    if trig=='uniformManualScoresWithoutEvidence': return int(i.get('scoreCount',0))>=10 and int(i.get('sameScoreCount',0))/max(1,int(i.get('scoreCount',1)))>=0.8 and i.get('dimensionEvidencePresent') is not True
    if trig=='genericReaderPassEvidence': return int(i.get('readerTaskTraceCount',0))<3
    if trig=='pagePackedForLength': return i.get('spacingReducedForPageCount') is True
    if trig=='architectureVisualOwnerMisclassified': return bool(i.get('node')) and i.get('actualZone')!=i.get('expectedZone')
    if trig=='backofficePlacedInOperationsEdge': return i.get('node')=='cpf-backoffice' and i.get('zone') in ('PLATFORM_OPERATIONS_RUNTIME','CHANNEL_EDGE','OPERATIONS_EDGE')
    if trig=='backofficeBffConflated': return i.get('conflated') is True and set(i.get('singleNodeLabels',[]))>={'cpf-backoffice','cpf-backoffice-web'}
    if trig=='pdfOpenabilityFailure': return i.get('pdfHeaderValid') is not True or i.get('parserOpen') is not True or i.get('firstLastRender') is not True
    return False

errors=[]
for x in F.get('fixtures',[]):
    if x.get('expect')!='FAIL': errors.append(x.get('id','?')+': expect must be FAIL'); continue
    if V.get('hardFail',{}).get(x['mustTrigger'])!=0: errors.append(x['id']+': hardFail missing/nonzero '+x['mustTrigger'])
    if not evaluate(x): errors.append(x['id']+': fixture was not rejected by '+x['mustTrigger'])
if errors:
    print('QUALITY_FIXTURES=FAIL COUNT='+str(len(errors)))
    for e in errors: print('-',e)
    sys.exit(1)
print('QUALITY_FIXTURES=PASS COUNT='+str(len(F.get('fixtures',[]))))
