#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
F=json.loads((ROOT/'quality-fixtures.json').read_text(encoding='utf-8'))
V=json.loads((ROOT/'visual-qa.json').read_text(encoding='utf-8'))
D=json.loads((ROOT/'design-tokens.json').read_text(encoding='utf-8'))
expected={}

def evaluate(x):
    i=x['input']; c=x['category']; trig=x['mustTrigger']
    fail=False
    if trig=='readerOpeningEncodedAsTable': fail=i.get('presentation')=='table'
    elif trig=='userFacingHarnessVersion': fail=bool(re.search(r'Harness\s+\d',i.get('text',''),re.I))
    elif trig=='userFacingSourceSha': fail=bool(re.search(r'Source\s+[0-9A-F]{16,}',i.get('text',''),re.I))
    elif trig=='tableHeaderWrap': fail=i.get('headerVisualLines',0)>1
    elif trig=='nonTabularContentEncodedAsTable': fail=i.get('rows',0)<2 or i.get('dimensions',0)<2 or i.get('purpose')=='reader metadata'
    elif trig=='connectorTargetNodeIntrusion': fail=i.get('targetInteriorPenetrationPx',0)>0
    elif trig=='connectorArrowheadInsideTargetNode': fail=i.get('arrowheadBodyInsideTargetPx',0)>0
    elif trig=='connectorEndsInUnlabeledEmptySpace': fail=i.get('to')=='EMPTY' and not i.get('junctionLabel')
    elif trig=='graphicalObjectContrastBelow3to1': fail=float(i.get('graphicalObjectContrast',99))<3.0
    elif trig=='tableTextContrastBelow4to5to1': fail=float(i.get('tableTextContrast',99))<4.5
    elif trig=='colorOnlyMeaning': fail=i.get('meaningEncodedBy')==['color']
    elif trig=='embeddedEffectiveTextTooSmall': fail=float(i.get('effectiveMinTextPt',99))<10.5
    elif trig=='coarseGeometryManifestAcceptedAsPass': fail=i.get('meaningfulNodesDetailed') is False
    elif trig=='docxOpeningMetaTable': fail=i.get('presentation')=='table' and any(x in i.get('labels',[]) for x in ['누가 보는가','이 문서로 끝낼 일','기준'])
    elif trig=='docxUserFacingProvenance': fail=bool(re.search(r'(Harness\s*(?:v)?\d+(?:\.\d+)+|Source\s*(?:SHA\s*)?[0-9A-F]{16,})',i.get('text',''),re.I))
    elif trig=='docxSingleRowLayoutTable': fail=int(i.get('rows',0))==1 and i.get('purpose') in ('single message/callout','reader metadata','layout')
    elif trig=='isolatedTrailingContentPage': fail=i.get('isLastPage') is True and int(i.get('meaningfulBlocks',99))<=2 and float(i.get('contentOccupancyRatio',1))<0.30
    elif trig=='feedbackFixedOnlyInArtifactWithoutHarnessGate': fail=i.get('userFinding') is True and i.get('artifactPatched') is True and (i.get('harnessRuleAdded') is not True or i.get('negativeFixtureAdded') is not True)
    elif trig=='connectorCrossesUnrelatedNode': fail=i.get('crossesUnrelatedNodeInterior') is True
    elif trig=='connectorCrossesTextOrLabel': fail=i.get('crossesTextOrLabel') is True
    elif trig=='connectorEndpointNotOnTargetBoundary': fail=float(i.get('targetBoundaryDistancePx',0))>2.0
    elif trig=='connectorSourceNodeIntrusion': fail=float(i.get('sourceInteriorPenetrationPx',0))>0
    elif trig=='tocTabStopOutsideWritableArea': fail=float(i.get('tabStopTwips',0))>float(i.get('writableWidthTwips',0)) or i.get('pageNumberVisible') is False
    return fail

errors=[]
for x in F.get('fixtures',[]):
    if x.get('expect')!='FAIL': errors.append(x['id']+': expect must be FAIL'); continue
    if V.get('hardFail',{}).get(x['mustTrigger'])!=0: errors.append(x['id']+': hardFail missing')
    if not evaluate(x): errors.append(x['id']+': fixture was not rejected')
if errors:
    print('QUALITY_FIXTURES=FAIL')
    for e in errors: print('-',e)
    sys.exit(1)
print('QUALITY_FIXTURES=PASS COUNT='+str(len(F.get('fixtures',[]))))
