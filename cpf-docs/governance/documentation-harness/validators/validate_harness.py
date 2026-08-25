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
if h.get('version')!='1.1.3': fail('version')
if h.get('locked') is not True or h.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail('change authority')
if h.get('changePolicy',{}).get('autoModify') is not False: fail('auto modify')
for f in ['design-tokens.json','writing-style.json','content-density.json','visual-system.json','document-output-rules.json','readme-value-inventory.json']:
    d=load(f)
    if d.get('harnessVersion')!='1.1.3': fail(f'version {f}')
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
    if a['id']=='README':
        if pr.get('tocRequired') is not False: fail('README profile TOC')
        nums=[]
        for s in pr['sections']:
            m=re.match(r'^(\d+)\. ',s['title'])
            if not m: fail(f'README unnumbered {s["title"]}')
            nums.append(int(m.group(1)))
        if nums!=list(range(1,len(nums)+1)): fail('README numbering')
        if pr.get('specialRules',{}).get('architectureMap',{}).get('required') is not True: fail('README architecture')
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
print('HARNESS=PASS')
print('VERSION='+h['version'])
print('ARTIFACTS='+str(len(arts)))
print('COVERAGE_ITEMS='+str(len(items)))
print('PROFILES='+str(len(list((ROOT/'profiles').glob('*.json')))))
print('TABLE_PRESETS='+str(len(T)))
print('FIGURE_PRESETS='+str(len(F)))
