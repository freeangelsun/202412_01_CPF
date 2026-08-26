#!/usr/bin/env python3
import json, hashlib, re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def fail(msg):
    print('HARNESS=FAIL',msg); raise SystemExit(1)
def load(name):
    p=ROOT/name
    if not p.is_file(): fail(f'missing {name}')
    try:return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: fail(f'json {name}: {e}')

h=load('harness.json')
if h.get('version')!='2.1.0': fail('version')
if h.get('locked') is not True or h.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail('change authority')
if h.get('changePolicy',{}).get('autoModify') is not False: fail('auto modify')
for f in ['design-tokens.json','writing-style.json','content-density.json','visual-system.json','document-output-rules.json','readme-value-inventory.json']:
    d=load(f)
    if d.get('harnessVersion')!='2.1.0': fail(f'version {f}')
D=load('design-tokens.json')
if D['toc']['readme_toc']!='forbidden': fail('README TOC must be forbidden')
if D['tables']['body_default_alignment']!='left': fail('table body left')
if D['tables']['equal_width_default']!='forbidden': fail('equal width')
if D['tables']['max_columns_portrait']!=4 or D['tables']['max_columns_landscape']!=5: fail('table columns')
if D['tables']['max_cell_korean_chars_review']>70: fail('cell prose limit')
W=load('writing-style.json')
if W['license']['exact_user_facing_sentence']!='CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.': fail('license exact phrase')
O=load('document-output-rules.json')
if O['README']['licenseExactSentence']!=W['license']['exact_user_facing_sentence']: fail('license output rules')

# documentation usability gates
if h.get('changePolicy',{}).get('documentationFeedbackIsHarnessChangeRequest') is not True: fail('harness-first feedback rule')
if D.get('headingNumbering',{}).get('H2')!='1.1 / 1.2 / 2.1 ...': fail('H2 numbering')
if D['paragraph'].get('h1_space_before_pt',0) < 28: fail('H1 spacing')
if D['paragraph'].get('h2_space_before_pt',0) < 16: fail('H2 spacing')
if D['figures'].get('low_contrast_label')!='hard_fail': fail('figure contrast')
if D['fonts'].get('pdf_korean_font_embedding_required') is not True: fail('pdf korean font embedding')
# v2.1.0 visual geometry / balance gates
FG=D.get('figures',{})
if FG.get('node_inner_padding_px_min',0) < 18: fail('figure inner padding')
if FG.get('label_to_label_gap_px_min',0) < 20: fail('figure label gap')
if FG.get('node_to_node_gap_px_min',0) < 24: fail('figure node gap')
if FG.get('label_to_connector_clearance_px_min',0) < 12: fail('figure connector clearance')
if FG.get('group_title_band_height_px_min',0) < 44: fail('figure group title band')
if FG.get('text_node_boundary_collision') != 0 or FG.get('text_connector_collision') != 0: fail('figure collision gate')
if D.get('visual_quality',{}).get('page_visual_balance_required') is not True: fail('page visual balance')
if h.get('qualityDoctrine',{}).get('singleCurrentHarness') is None: fail('single current harness')
if O.get('globalPathGate',{}).get('absolutePathMaxChars') != 150: fail('path max 150')
if O.get('harnessRetention',{}).get('currentOnly') is not True: fail('current harness only')
if O.get('DOCX',{}).get('tocMaterializedVisibleEntriesRequired') is not True: fail('visible TOC entries')
Q=load('visual-qa.json')
for k in ['figureTextNodeBoundaryCollision','figureTextConnectorCollision','figureTitleChildLabelOverlap','pageAccidentalVisualImbalance','figureAccidentalVisualImbalance','unresolvedLargeDeadSpace']:
    if Q.get('hardFail',{}).get(k) != 0: fail('visual qa '+k)
FROOT=load('figure-presets.json')
CG=FROOT.get('commonGeometryGate',{})
if CG.get('nodeInnerPaddingPxMin',0) < 18 or CG.get('labelConnectorClearancePxMin',0) < 12: fail('figure preset common geometry')
if O['README'].get('manualNavigation','').startswith('mandatory') is not True: fail('README manual navigation')
if O['README'].get('bootstrapRuntimeBlock','').startswith('mandatory') is not True: fail('README bootstrap/runtime')

# incremental/visual/link/content-rail gates
if h.get('changePolicy',{}).get('artifactEvolutionPolicy',{}).get('defaultMode')!='PATCH_FIRST': fail('patch first policy')
_evo=h.get('changePolicy',{}).get('artifactEvolutionPolicy',{})
if _evo.get('freshRebuildException',{}).get('afterApproval')!='PATCH_ONLY': fail('fresh rebuild lifecycle')
if _evo.get('freshRebuildException',{}).get('maxConsecutiveFreshRebuilds')!=1: fail('fresh rebuild max once')

if D['paragraph'].get('h2_space_after_pt',99)>6 or D['paragraph'].get('h3_space_after_pt',99)>5: fail('subheading content gap')
if D.get('indentation',{}).get('subheading_content_indent_mm',0)<4: fail('subheading content rail')
if D['figures'].get('canvas_safe_margin_px_min',0)<48: fail('figure canvas safe margin')
if D['figures'].get('rounded_rectangle_arrow_chain_default')!='forbidden': fail('box arrow default')
if O.get('linkIntegrity',{}).get('pdfLabelMustTargetPdf') is not True: fail('pdf link target rule')
if O.get('artifactEvolution',{}).get('default')!='PATCH_FIRST': fail('incremental artifact rule')
if O.get('windowsValidation',{}).get('pythonRequired') is not False: fail('python must not be required on Windows')
VS=load('visual-system.json')
if VS.get('readme',{}).get('uniqueVisualGrammarsMinWhenFiveOrMore',0)<4: fail('visual grammar diversity')
if VS.get('readme',{}).get('roundedRectangleArrowChainMaxTotal',99)>1: fail('box arrow monoculture')
if VS.get('readme',{}).get('backgroundContrast','').find('dark-on-dark hard_fail')<0: fail('readme surface contrast')
if not (ROOT/'validators'/'validate_readme.ps1').is_file(): fail('PowerShell README validator missing')

T=load('table-presets.json')['presets']
for name,t in T.items():
    widths=t.get('widthPct',[])
    if sum(widths)!=100: fail(f'width sum {name}')
    if len(t.get('columns',[]))!=len(widths): fail(f'columns {name}')
    if len(widths)>5: fail(f'too many cols {name}')
    if len(widths)>2 and len(set(widths))==1: fail(f'equal widths {name}')
F=load('figure-presets.json')['presets']
if 'README_ARCHITECTURE_MAP' not in F: fail('architecture visual')
scope=load('scope.json'); arts=scope.get('officialArtifacts',[])
if len(arts)!=12 or scope.get('officialDocxCount')!=11 or scope.get('officialPdfCount')!=11: fail('scope count')
models=load('content-models.json')['models']
for a in arts:
    p=ROOT/'profiles'/a['profile']; pr=json.loads(p.read_text(encoding='utf-8'))
    if pr.get('documentId')!=a['id'] or pr.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail(f'profile {p.name}')
    if pr.get('additionalH1') is not False: fail(f'extra h1 {p.name}')
    if a['id']=='FRAMEWORK_DEVELOPER_GUIDE':
        gp=pr.get('guidePolicy',{})
        if gp.get('catalogStyle')!='forbidden' or gp.get('frequencyFirst') is not True: fail('framework guide frequency policy')
        if '내부 Domain↔Domain 호출은 Gateway 미경유' not in gp.get('gatewayRule',''): fail('framework gateway rule')
    if a['id']=='BATCH_DEVELOPER_GUIDE':
        gp=pr.get('guidePolicy',{})
        if gp.get('catalogStyle')!='forbidden' or gp.get('frequencyFirst') is not True: fail('batch guide frequency policy')

    if a['id']=='README':
        if pr.get('tocRequired') is not False: fail('README profile TOC')
        nums=[]
        for s in pr['sections']:
            m=re.match(r'^(\d+)\. ',s['title'])
            if not m: fail(f'README unnumbered {s["title"]}')
            nums.append(int(m.group(1)))
        if nums!=list(range(1,len(nums)+1)): fail('README numbering')
        if pr.get('specialRules',{}).get('architectureMap',{}).get('required') is not True: fail('README architecture')
        sr=pr.get('specialRules',{})
        if sr.get('gatewayOptionality',{}).get('internalDomainViaGateway')!='forbidden': fail('README gateway internal-domain rule')
        if sr.get('manualNavigation',{}).get('required') is not True: fail('README manual navigation profile')
        if sr.get('developerEntryBlock',{}).get('required') is not True: fail('README developer entry block')
        if len(pr.get('sections',[]))<6: fail('README coverage section minimum')
        # Every non-license README subsection is hierarchically numbered.
        for secidx,sec in enumerate(pr.get('sections',[])[:-1],start=1):
            for h2idx,t in enumerate(sec.get('requiredH2',[]),start=1):
                if not re.match(r'^%d\.%d\s+'%(secidx,h2idx), t): fail(f'README H2 numbering {t}')
        expected=W['license']['exact_user_facing_sentence']
        if pr.get('specialRules',{}).get('license',{}).get('exactSentence')!=expected: fail('license README profile')
        matches=sum(1 for sec in pr.get('sections',[]) if sec.get('requiredH2')==[expected])
        if matches!=1: fail('license README H2')
    else:
        if pr.get('tocRequired') is not True: fail(f'DOCX toc {p.name}')
    for s in pr.get('sections',[]):
        if s.get('additionalH2') is not False or s.get('additionalH3') is not False: fail(f'extra heading {p.name}')
        if s.get('model') not in models: fail(f'model {p.name}:{s.get("model")}')
        for t in s.get('tables',[]):
            if t not in T: fail(f'table {p.name}:{t}')
        for f in s.get('figures',[]):
            if f not in F: fail(f'figure {p.name}:{f}')
C=load('product-coverage.json'); items=C.get('items',[])
if len(items)<55: fail('coverage')
for raw in (ROOT/'DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
    s=raw.strip()
    if not s or s.startswith('#'): continue
    if '*' in s or '?' in s or s.startswith('/') or '..' in Path(s).parts: fail(f'unsafe delete {s}')
lock=load('HARNESS_LOCK.json')
for rel,expected in lock.get('files',{}).items():
    p=ROOT/rel
    if not p.is_file(): fail(f'lock missing {rel}')
    if hashlib.sha256(p.read_bytes()).hexdigest()!=expected: fail(f'lock mismatch {rel}')
# v2 executable design / acceptance gates
for req in ["component-system.json","quality-acceptance.json","golden-reference.json","GOLDEN_REFERENCE_STANDARD.md","templates/ARTIFACT_REVIEW.template.json"]:
    if not (ROOT/req).exists(): fail("missing v2 file "+req)
qam=load("quality-acceptance.json")
if qam.get("automatedPassIsQualityPass") is not False: fail("automated pass quality separation")
if set(qam.get("baselineEligibility",[])) != {"USER_APPROVED","VISUAL_QA_APPROVED"}: fail("baseline approval states")
cs=load("component-system.json")
for cid in ["H1_SECTION","H2_SUBSECTION","BODY_BLOCK","BULLET_GROUP","FIGURE_BLOCK","FIGURE_EXPLANATION","DECISION_TABLE","DOCUMENT_LINK_ROW"]:
    if cid not in cs.get("components",{}): fail("component "+cid)

for p in (ROOT/'profiles').glob('*.json'):
    pr=json.loads(p.read_text(encoding='utf-8'))
    if pr.get('structureLocked') is not False or pr.get('coverageLocked') is not True: fail('profile outcome-flex '+p.name)
    if pr.get('compositionPolicy',{}).get('mode')!='OUTCOME_LOCKED_LAYOUT_FLEXIBLE': fail('profile composition '+p.name)
if h.get('changePolicy',{}).get('artifactEvolutionPolicy',{}).get('freshRewriteDefault')!='FORBIDDEN': fail('fresh rewrite default')
if h.get('changePolicy',{}).get('artifactEvolutionPolicy',{}).get('automatedPassOnlyIsBaseline') is not False: fail('automated baseline')
if qam.get('manualVisualScore',{}).get('minimumEach',0)<4 or qam.get('manualVisualScore',{}).get('minimumAverage',0)<4.4: fail('manual visual score threshold')
print('HARNESS=PASS')
print('VERSION='+h['version'])
print('ARTIFACTS='+str(len(arts)))
print('COVERAGE_ITEMS='+str(len(items)))
print('PROFILES='+str(len(list((ROOT/'profiles').glob('*.json')))))
print('TABLE_PRESETS='+str(len(T)))
print('FIGURE_PRESETS='+str(len(F)))
print('QUALITY_MODEL=EXECUTABLE_DESIGN_SYSTEM')
print('DEFAULT_EVOLUTION=PATCH_FIRST')
print('FRESH_REWRITE_DEFAULT=FORBIDDEN')
