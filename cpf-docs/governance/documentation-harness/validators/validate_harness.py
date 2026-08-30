#!/usr/bin/env python3
import json,hashlib,re,subprocess,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
VER='2.15.4'

def fail(msg):
    print('HARNESS=FAIL',msg); raise SystemExit(1)
def load(rel):
    p=H/rel
    if not p.is_file(): fail('missing '+rel)
    try:return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: fail(f'json {rel}: {e}')
# Required executable-quality files.
required=[
 'harness.json','validators/validate_visual_comfort.py','validators/validate_rendered_page_composition.py','validators/validate_source_currentization.py','validators/capture_source_currentization.py','visual-comfort.json','VISUAL_COMFORT_STANDARD.md','rendered-page-composition.json','RENDERED_PAGE_COMPOSITION_STANDARD.md','source-currentization.json','SOURCE_CURRENTIZATION_STANDARD.md','READABILITY_AND_ACTIONABILITY_STANDARD.md','HARNESS_DIAGNOSTIC_AND_REINFORCEMENT.md','readability-actionability.json','architecture-visual-semantics.json','ARCHITECTURE_VISUAL_SEMANTIC_STANDARD.md','PDF_OPENABILITY_STANDARD.md','validators/validate_architecture_visual_semantics.py','validators/validate_pdf_openability.py','ANTI_PATTERN_CATALOG.md','MANUAL_REVIEW_SCORECARD.md','DOCUMENT_DESIGN_PLAYBOOK.md','README_BROCHURE_AND_AI_TEXT_STANDARD.md','INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md','AUTHORING_EXECUTION_PROTOCOL.md','design-tokens.json','writing-style.json','content-density.json','document-output-rules.json','quality-acceptance.json','quality-fixtures.json','visual-qa.json','reader-task-coverage.json','readme-value-inventory.json','scope.json','table-presets.json','figure-presets.json','HARNESS_LOCK.json','PACKAGE_MANIFEST.json','DELETE_MANIFEST.txt','DELETE_MANIFEST.json','HARD_GATE_POLICY.md','HARNESS_OWNERSHIP_BOUNDARY_STANDARD.md','ownership-boundary.json',
 'profiles/README.json','templates/ARTIFACT_REVIEW.template.json','templates/SESSION_RUN_MANIFEST.template.json','templates/FINAL_ACCEPTANCE.template.json',
 'validators/validate_source_alignment.py','validators/validate_quality_fixtures.py','validators/validate_readability_actionability.py','validators/validate_readme.py','validators/validate_docx_artifacts.py','validators/validate_reader_task_coverage.py','validators/validate_final_acceptance.py','validators/run_all_gates.py',
 'validators/validate_source_alignment.ps1','validators/validate_quality_fixtures.ps1','validators/validate_readability_actionability.ps1','validators/validate_readme.ps1','validators/validate_docx_artifacts.ps1','validators/validate_final_acceptance.ps1','validators/run_all_gates.ps1',
 'README_PRODUCT_COMPLETENESS_STANDARD.md','VISUAL_HUMAN_QUALITY_STANDARD.md','FALSE_GREEN_PREVENTION_STANDARD.md','readme-product-completeness.json','visual-human-quality.json','false-green-policy.json',
 'validators/validate_readme_product_completeness.py','validators/validate_visual_human_review.py','validators/validate_user_finding_closure.py','validators/validate_false_green_prevention.py','validators/selftest_no_false_green.py',
 'validators/validate_readme_product_completeness.ps1','validators/validate_visual_human_review.ps1','validators/validate_user_finding_closure.ps1','validators/validate_false_green_prevention.ps1'
]
for r in required:
    if not (H/r).is_file(): fail('required file '+r)
# Current-only: no history/backup/session files or folders in canonical harness.
for p in H.rglob('*'):
    n=p.name.lower()
    if p==H/'DELETE_MANIFEST.txt' or p==H/'DELETE_MANIFEST.json': continue
    if n in {'changelog.md','.pytest_cache','__pycache__'} or n.endswith('.pyc') or any(x in n for x in ['_backup','_history','_session']) or re.match(r'documentation-harness-v\d',n): fail('stale/history artifact '+str(p.relative_to(H)))
# JSON parse and version consistency.
for p in H.rglob('*.json'):
    d=load(str(p.relative_to(H)))
    hv=d.get('harnessVersion')
    if hv is not None and hv!=VER: fail(f'version mismatch {p.relative_to(H)}={hv}')
h=load('harness.json')
if h.get('version')!=VER or h.get('locked') is not True: fail('harness version/lock')
if h.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail('change authority')
# Strict final acceptance must exist and be all-required.
cg=h.get('completionGate',{})
if cg.get('allRequired') is not True or cg.get('partialPass')!='forbidden': fail('completion gate strictness')
for gid in ['SOURCE_ALIGNMENT_PASS','READABILITY_AND_ACTIONABILITY_PASS','SELECTION_TO_ACTION_PASS','DEVELOPER_WORKING_EXAMPLE_PASS','VISUAL_COMFORT_AND_DENSITY_PASS','README_SCANABILITY_PASS','README_NATURAL_VALUE_PASS','TABLE_PROPORTION_RENDER_PASS','READER_TASK_COMPLETENESS_PASS','DOCUMENT_SIZE_CAP_ABSENCE_PASS','CONTENT_COVERAGE_NOT_TRUNCATED_PASS','README_BROCHURE_PASS','README_AI_TEXT_COMPANION_PASS','INFORMATION_ARCHITECTURE_PASS','FULL_PAGE_FRESH_EYES_REVIEW_PASS','FLEXIBLE_TABLE_LAYOUT_PASS','LONG_DOCUMENT_NAVIGATION_PASS','MANUAL_EVIDENCE_COMPLETE_PASS','ARCHITECTURE_VISUAL_SEMANTIC_PASS','PDF_OPENABILITY_PASS','FINAL_ACCEPTANCE_AGGREGATOR_PASS','README_PRODUCT_COMPLETENESS_PASS','README_ARCHITECTURE_COMPLETENESS_PASS','README_EXPLANATION_DEPTH_PASS','README_DEVELOPER_GOLDEN_PATH_PASS','VISUAL_SEMANTIC_QUALITY_PASS','CURRENT_ARTIFACT_SHA_REVIEW_PASS','USER_FINDING_CLOSURE_PASS','FALSE_GREEN_PREVENTION_PASS']:
    if gid not in cg.get('required',[]): fail('missing completion gate '+gid)
qa=load('quality-acceptance.json'); qids={s['id'] for s in qa.get('stages',[]) if s.get('required')}
for gid in cg.get('required',[]):
    if gid not in qids: fail('completion stage missing in quality-acceptance '+gid)
fa=qa.get('finalAcceptance',{})
if 'EVERY required stage exactly PASS' not in fa.get('passOnlyIf',''): fail('final acceptance exact PASS rule')
if fa.get('manualEvidenceMandatory') is not True: fail('manual evidence mandatory')
for bad in ['AUTOMATED_PASS_ONLY','NOT_EXECUTED','BLOCKED','UNKNOWN','SKIPPED','PARTIAL','WAIVED']:
    if bad not in fa.get('forbiddenFinalStates',[]): fail('forbidden final state missing '+bad)
# README rules: natural value, no promotional headings, scanability.
rp=load('profiles/README.json')
if rp.get('structureLocked') is not False or rp.get('coverageLocked') is not True: fail('README profile flexibility/coverage')
sp=rp.get('specialRules',{})
if sp.get('naturalValueDiscovery',{}).get('forbiddenDedicatedValueSection') is not True: fail('README benefit section prohibition')
sc=sp.get('scanability',{})
if sc.get('h2Min')!=5 or sc.get('h2UpperBound')!='NONE': fail('README H2 no-upper-bound policy')
if rp.get('specialRules',{}).get('totalSizePolicy',{}).get('h2UpperBound')!='NONE': fail('README total size policy')
if rp.get('specialRules',{}).get('aiTextCompanion',{}).get('requiredForEveryInformativeFigure') is not True: fail('README AI text companion policy')
if rp.get('specialRules',{}).get('brochureMode',{}).get('required') is not True: fail('README brochure policy')
forbidden=' '.join(sp.get('forbidden',[]))
for term in ['CPF를 적용하면 무엇이 달라지는가','핵심 장점','왜 좋은가','이 구조의 장점','핵심 해석','기반 기술']:
    if term not in forbidden: fail('README forbidden label missing '+term)
rv=load('readme-value-inventory.json')
nd=rv.get('naturalDistribution',{})
if nd.get('requiredGroupsMin',0)<7 or nd.get('distinctFunctionalSectionsMin',0)<4 or nd.get('maxSingleSectionSharePct',99)>45: fail('README natural value distribution')
if len(rv.get('groups',[]))<8: fail('README value groups')
# Vertical rhythm and table width strictness.
d=load('design-tokens.json'); par=d.get('paragraph',{}); tab=d.get('tables',{})
mins={'body_line_spacing_multiple':1.32,'body_space_after_pt':8.5,'h1_space_before_pt':58,'h2_space_before_pt':32,'h3_space_before_pt':20,'semanticTransitionGapPtMin':16}
for k,v in mins.items():
    if float(par.get(k,0))<v: fail(f'vertical rhythm {k}')
wh=tab.get('widthHardGates',{})
for k in ['unjustifiedEqualWidthCount','headerWrapCount','shortTokenWrapCount','excessiveWrapDensityCount','semanticWidthInversionCount','renderedWidthMismatchCount']:
    if wh.get(k)!=0: fail('table hard gate '+k)
if '12' not in str(wh.get('equalWidthAllowedOnlyIf','')): fail('equal width 12 percent rule')
# Reader-task is not keyword-only.
rc=load('reader-task-coverage.json')
if len(rc.get('artifacts',[]))!=12: fail('reader task artifact count')
if len(rc.get('requiredTaskDimensions',[]))<8: fail('reader task dimensions')
if 'term presence is only a pre-check' not in rc.get('policy',''): fail('reader keyword-only false green')
# User visual connector finding remains hard-zero.
vq=load('visual-qa.json'); hf=vq.get('hardFail',{})
for k in ['connectorTargetNodeIntrusion','connectorArrowheadInsideTargetNode','connectorCrossesTextOrLabel','connectorEndpointNotOnTargetBoundary','connectorSourceNodeIntrusion','promotionalBenefitHeading','readmeDenseWallOfText','semanticTableWidthInversion','shortTokenWrap','readerTaskKeywordOnlyFalseGreen','manualGateNotExecuted','manualEvidenceMissing','requiredGateNonPass','automatedOnlyFinalPassAttempt','documentTotalSizeCap','coverageTruncatedForLength','readmeBrochureStructureMissing','readmeVisualKoreanCompanionMissing','readmeImageAltMissing','readmeBrochureVisualRhythmMissing','informationArchitectureReaderNeedMismatch','longDocumentNavigationMissing','fixedWidthTableCausesWrap','manualFreshEyesReviewMissing','selectionWithoutNextAction','apiSummaryWithoutWorkingExample','developerChapterTableWall','readmeDenseCenteredHero','readmeFlatLongNavigation','readmeStackedCodeBlocks','longFlatListWall','consecutiveLongBulletWall','heavyBlockWall','uniformManualScoresWithoutEvidence','genericReaderPassEvidence','pagePackedForLength','architectureVisualOwnerMisclassified','backofficePlacedInOperationsEdge','backofficeBffConflated','pdfOpenabilityFailure','readmeProductTooThin','readmeArchitectureCoverageMissing','readmeArchitectureModuleListOnly','readmeCliCommandListWithoutExplanation','readmeCliCurrentSourceMismatch','readmeBenefitKeywordOnly','visualReviewShaMismatch','visualContactSheetOnlyReview','visualRepeatedGrammarFingerprint','visualConnectorThroughText','visualNodeOutsideCanvas','userFindingStillOpen','previousPassNotReopenedAfterUserFinding','sourceValidatorHardcodedIdentity','readmeRenderedPreviewCorrupt','readmePreviewAbsoluteBase','visualAssetImageCorrupt']:
    if hf.get(k)!=0: fail('hardFail key '+k)
# High-quality human review threshold must not be weakened.
ms=qa.get('manualVisualScore',{})
if float(ms.get('minimumEach',0))<4 or float(ms.get('minimumAverage',0))<4.6: fail('manual visual score threshold weakened')
for dim in ['information_architecture_fit','no_content_truncation','fresh_eyes_scan_quality','readme_brochure_quality','readme_ai_text_companion','reader_actionability','selection_to_action','working_example_fit','failure_recovery_closure','visual_comfort','information_hierarchy','flat_list_density','heavy_block_rhythm']:
    if dim not in ms.get('dimensions',[]): fail('manual visual dimension missing '+dim)
art=load('templates/ARTIFACT_REVIEW.template.json')
for fld in ['scanPassEvidence','detailPassEvidence','readerPassEvidence']:
    if fld not in art: fail('artifact review evidence field missing '+fld)
for mg in ['contentCoverageNotTruncated','informationArchitecture','freshEyesTwoPass','flexibleTableLayout','longDocumentNavigation','readmeBrochure','readmeAiTextCompanion','selectionToAction','workingExampleFit','visualComfort','heavyBlockRhythm','flatListGrouping','failureRecoveryClosure']:
    if mg not in art.get('manualGates',{}): fail('artifact manual gate missing '+mg)
# Readability/actionability structural reinforcement.
ra=load('readability-actionability.json')
if ra.get('global',{}).get('longFlatListItemsHardFail',99)>7: fail('readability flat list threshold weakened')
if ra.get('global',{}).get('heavyBlocksConsecutiveHardFail',99)>4: fail('readability heavy block threshold weakened')
if ra.get('developerChapterContract',{}).get('apiSummaryIsNotHowTo',False) is False and ra.get('global',{}).get('apiSummaryIsNotHowTo') is not True: fail('API summary false-green policy')
if ra.get('developerChapterContract',{}).get('selectionTable','')=='': fail('selection-to-action contract missing')
for rel in ['READABILITY_AND_ACTIONABILITY_STANDARD.md','HARNESS_DIAGNOSTIC_AND_REINFORCEMENT.md']:
    if not (H/rel).is_file(): fail('readability standard missing '+rel)
# v2.8 no total size cap and self-contained authoring playbooks.
size=h.get('documentSizePolicy',{})
for k in ['totalFileSizeLimit','totalPageCountLimit','totalWordCountLimit','totalCharacterCountLimit','totalSectionCountLimit','totalFigureCountLimit']:
    if size.get(k)!='NONE': fail('total document size cap '+k)
if size.get('coverageReductionForLength')!='FORBIDDEN': fail('coverage reduction for length')
cd=load('content-density.json')
if 'pageBudgets' in cd or 'hardMaxPolicy' in cd: fail('legacy page budget/hardMax policy forbidden')
if cd.get('totalDocumentSizePolicy',{}).get('totalPageCountLimit')!='NONE': fail('content density total page cap')
# No H2/visual upper bound in README rules.
do=load('document-output-rules.json')
if do.get('README',{}).get('visualCountUpperBound')!='NONE': fail('README visual upper bound')
if 'visualCountMax' in do.get('README',{}): fail('README visualCountMax forbidden')
# Required playbooks must state core guarantees.
for rel,token in [('DOCUMENT_DESIGN_PLAYBOOK.md','총 페이지/용량'),('README_BROCHURE_AND_AI_TEXT_STANDARD.md','AI/텍스트'),('INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md','Reader Task'),('AUTHORING_EXECUTION_PROTOCOL.md','Clean replay'),('ANTI_PATTERN_CATALOG.md','FAIL'),('MANUAL_REVIEW_SCORECARD.md','평균 4.6')]:
    txt=(H/rel).read_text(encoding='utf-8')
    if token.lower() not in txt.lower(): fail('playbook missing token '+rel+' '+token)
# Every profile carries no-size policy and documentation intent.
for pp in (H/'profiles').glob('*.json'):
    pd=json.loads(pp.read_text(encoding='utf-8'))
    pol=pd.get('totalDocumentSizePolicy',{})
    if pol.get('totalPageLimit')!='NONE' or pol.get('coverageReductionForLength')!='FORBIDDEN': fail('profile size policy '+pp.name)
    if not pd.get('documentationIntent',{}).get('primaryMode'): fail('profile documentation intent '+pp.name)

# Reject reintroduction of total-cap fields anywhere in JSON.
def walk_caps(obj,path=''):
    if isinstance(obj,dict):
        for k,v in obj.items():
            kp=(path+'.'+k).strip('.')
            if k in {'pageBudgets','hardMaxPolicy','h2Max','visualCountMax','hardMaxPages'}: fail('forbidden total-cap field '+kp)
            if k=='hardMax' and any(x in path.lower() for x in ['readme','page','document','visualcount']): fail('forbidden hardMax total cap '+kp)
            walk_caps(v,kp)
    elif isinstance(obj,list):
        for idx,v in enumerate(obj): walk_caps(v,f'{path}[{idx}]')
for jp in H.rglob('*.json'):
    if jp.name in {'HARNESS_LOCK.json','PACKAGE_MANIFEST.json'}: continue
    walk_caps(json.loads(jp.read_text(encoding='utf-8')),str(jp.relative_to(H)))
if 'Preferred Page Budget' in (H/'CONTENT_COMPRESSION_STANDARD.md').read_text(encoding='utf-8'): fail('legacy page budget phrase')

# Exact delete manifest only, no wildcard/parent traversal/current canonical path.
delete_txt=[]
for raw in (H/'DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
    s=raw.strip()
    if not s or s.startswith('#'): continue
    delete_txt.append(s)
    if '*' in s or '?' in s or s.startswith('/') or '..' in Path(s).parts: fail('unsafe delete '+s)
    if s.rstrip('/')=='cpf-docs/governance/documentation-harness': fail('delete current harness forbidden')
delete_json=load('DELETE_MANIFEST.json')
for key in ['delete','paths']:
    arr=delete_json.get(key)
    if arr!=delete_txt: fail('delete manifest TXT/JSON mismatch '+key)
if delete_json.get('wildcards') not in ([],None): fail('delete manifest wildcards must be empty')
if delete_json.get('currentCanonicalPath')!='cpf-docs/governance/documentation-harness': fail('delete manifest currentCanonicalPath mismatch')
# No stale version tokens outside delete manifest.
for p in H.rglob('*'):
    if not p.is_file() or p.name in {'DELETE_MANIFEST.txt','DELETE_MANIFEST.json','HARNESS_LOCK.json','PACKAGE_MANIFEST.json'}: continue
    try:txt=p.read_text(encoding='utf-8')
    except UnicodeDecodeError: continue
    if re.search(r'2\.(?:3|4|5|6)\.0',txt): fail('stale harness version token '+str(p.relative_to(H)))
# Delivery PowerShell Windows root-containment recurrence guard.
ROOT=H.parents[2]
DELIVERY=ROOT/'cpf-docs/deliverables/documentation'
for rel in ['APPLY.ps1','DELETE_ONLY.ps1']:
    p=DELIVERY/rel
    if p.is_file():
        txt=p.read_text(encoding='utf-8-sig')
        if "TrimEnd('" in txt: fail('delivery Windows separator literal in root prefix '+rel)
        if '[IO.Path]::DirectorySeparatorChar' not in txt or '$rootPrefix=$root.TrimEnd($sep)+$sep' not in txt or 'StartsWith($rootPrefix' not in txt:
            fail('delivery Windows root containment guard missing '+rel)

# Architecture semantic and PDF openability recurrence guard.
arch=load('architecture-visual-semantics.json')
if arch.get('nodes',{}).get('cpf-backoffice',{}).get('zone')!='BUSINESS_DOMAIN_CPF': fail('cpf-backoffice visual zone')
if arch.get('nodes',{}).get('cpf-backoffice-web',{}).get('zone')!='CHANNEL_EDGE': fail('cpf-backoffice-web visual zone')
if arch.get('hardRules',{}).get('backofficeAndBffConflated') is not False: fail('backoffice/BFF conflation rule')
for rel in ['ARCHITECTURE_VISUAL_SEMANTIC_STANDARD.md','PDF_OPENABILITY_STANDARD.md']:
    if not (H/rel).is_file(): fail('semantic/openability standard missing '+rel)

# No-excuse README/Visual hardening contract.
rpc=load('readme-product-completeness.json')
if int(rpc.get('minimumDepth',{}).get('visibleCharactersTotal',0))<22000: fail('README minimum explanation depth weakened')
if int(rpc.get('minimumDepth',{}).get('architectureSectionCharacters',0))<1800: fail('README architecture depth weakened')
if len(rpc.get('requiredCoverageGroups',[]))<19: fail('README product coverage groups incomplete')
if rpc.get('developerGoldenPath',{}).get('sourceAuthorityMode')!='DISCOVER_CURRENT_SOURCE_AT_VALIDATION': fail('README CLI source authority mode')
own=load('ownership-boundary.json')
if own.get('ownedRoot')!='cpf-docs/governance/documentation-harness/': fail('harness owned root')
if own.get('selfVerificationMustNotRequireExternalContext') is not True: fail('harness self verification external coupling')
if own.get('contextValidatorsAreSeparateFromHarnessSelfValidation') is not True: fail('context/self validation separation')
vhq=load('visual-human-quality.json')
if vhq.get('policy',{}).get('contactSheetOnlyReviewForbidden') is not True: fail('contact-sheet-only review must be forbidden')
if vhq.get('policy',{}).get('currentAssetShaRequired') is not True: fail('current asset SHA review required')
if int(vhq.get('grammar',{}).get('minimumDistinctGrammarsForFiveOrMoreVisuals',0))<5: fail('visual grammar diversity weakened')
for k in ['textOutsideCanvas','nodeOutsideCanvas','textNodeOverlap','connectorTextCollision','connectorNodeCollision','crop','overflow','semanticIncomplete']:
    if k not in vhq.get('hardZeroMetrics',[]): fail('visual human hard-zero metric missing '+k)
# Validator source may not embed a literal Source identity hash.
hex64=re.compile(r'(?<![A-Fa-f0-9])[A-Fa-f0-9]{64}(?![A-Fa-f0-9])')
for vp in (H/'validators').glob('*.py'):
    if hex64.search(vp.read_text(encoding='utf-8')): fail('hardcoded 64-hex identity in validator '+vp.name)
# Render evidence integrity recurrence guard.
rr=(H/'validators/validate_readme_render_review.py').read_text(encoding='utf-8')
if 'im.verify()' not in rr or 'im2.load()' not in rr: fail('README render validator full-decode integrity guard missing')
va=(H/'validators/validate_visual_assets.py').read_text(encoding='utf-8')
if 'im.verify()' not in va or 'im2.load()' not in va: fail('visual asset validator full-decode integrity guard missing')

# Hardening self-test must physically prove stale review/negative fixtures are rejected.
r=subprocess.run([sys.executable,str(H/'validators/selftest_no_false_green.py')],capture_output=True,text=True)
if r.returncode!=0: fail('hardening selftest\n'+r.stdout+r.stderr)

# Lock and package manifest hashes.
lock=load('HARNESS_LOCK.json'); pm=load('PACKAGE_MANIFEST.json')
actual_rel=sorted(str(x.relative_to(H)).replace('\\','/') for x in H.rglob('*') if x.is_file() and x.name not in {'HARNESS_LOCK.json','PACKAGE_MANIFEST.json'})
if sorted(lock.get('files',{}).keys())!=actual_rel: fail('HARNESS_LOCK full file coverage mismatch')
if sorted(x.get('path') for x in pm.get('files',[]))!=actual_rel: fail('PACKAGE_MANIFEST full file coverage mismatch')
if lock.get('harnessVersion')!=VER or pm.get('harnessVersion')!=VER: fail('lock/manifest version')
for rel,expected in lock.get('files',{}).items():
    p=H/rel
    if not p.is_file(): fail('lock missing '+rel)
    if hashlib.sha256(p.read_bytes()).hexdigest()!=expected: fail('lock mismatch '+rel)
if pm.get('fileCount')!=len(pm.get('files',[])): fail('package manifest count')
for x in pm.get('files',[]):
    p=H/x['path']
    if not p.is_file(): fail('package manifest missing '+x['path'])
    if hashlib.sha256(p.read_bytes()).hexdigest()!=x['sha256'] or p.stat().st_size!=x['size']: fail('package manifest mismatch '+x['path'])
# Negative fixtures must really fail.
r=subprocess.run([sys.executable,str(H/'validators/validate_quality_fixtures.py')],capture_output=True,text=True)
if r.returncode!=0: fail('negative fixture validator\n'+r.stdout+r.stderr)
print('HARNESS=PASS')
print('VERSION='+VER)
print('STRICT_FINAL_ACCEPTANCE=ENFORCED')
print('NEGATIVE_FIXTURES='+str(len(load('quality-fixtures.json').get('fixtures',[]))))
print('CURRENT_ONLY=PASS')
