#!/usr/bin/env python3
from __future__ import annotations
import json, hashlib
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
if h.get('locked') is not True or h.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail('change authority')
if h.get('changePolicy',{}).get('autoModify') is not False: fail('auto modify must be false')
scope=load('scope.json')
arts=scope.get('officialArtifacts',[])
if len(arts)!=12: fail(f'official artifact count {len(arts)} != 12')
if scope.get('officialDocxCount')!=11 or scope.get('officialPdfCount')!=11: fail('docx/pdf counts')
ids=[a['id'] for a in arts]
if len(ids)!=len(set(ids)): fail('duplicate document id')
paths=[a['path'] for a in arts]
if len(paths)!=len(set(paths)): fail('duplicate output path')
models=load('content-models.json')['models']
table_presets=load('table-presets.json')['presets']
figure_presets=load('figure-presets.json')['presets']
for name,t in table_presets.items():
    widths=t.get('widthPct',[])
    if not widths or sum(widths)!=100: fail(f'table width sum {name}: {widths}')
    if len(set(widths))==1 and len(widths)>2: fail(f'mechanical equal-width table preset {name}')
    if len(t.get('columns',[]))!=len(widths) or len(t.get('align',[]))!=len(widths): fail(f'table shape mismatch {name}')
for a in arts:
    p=ROOT/'profiles'/a['profile']
    if not p.is_file(): fail(f'missing profile {p.name}')
    pr=json.loads(p.read_text(encoding='utf-8'))
    if pr.get('documentId')!=a['id']: fail(f'profile id mismatch {p.name}')
    if not pr.get('locked') or pr.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail(f'profile lock {p.name}')
    if pr.get('additionalH1') is not False: fail(f'extra H1 allowed {p.name}')
    h1_seen=[]
    for s in pr.get('sections',[]):
        title=s.get('title')
        if not title or not s.get('requiredH2'): fail(f'incomplete section {p.name}')
        if s.get('additionalH2') is not False or s.get('additionalH3') is not False: fail(f'extra heading allowed {p.name}:{title}')
        if title in h1_seen: fail(f'duplicate H1 {p.name}:{title}')
        h1_seen.append(title)
        h2=s['requiredH2']
        if len(h2)!=len(set(h2)): fail(f'duplicate H2 {p.name}:{title}')
        if s.get('model') not in models: fail(f'unknown content model {p.name}:{title}:{s.get("model")}')
        for t in s.get('tables',[]):
            if t not in table_presets: fail(f'unknown table preset {p.name}:{title}:{t}')
        for f in s.get('figures',[]):
            if f not in figure_presets: fail(f'unknown figure preset {p.name}:{title}:{f}')
# user-specific hard rules
D=load('design-tokens.json')
if D['tables']['body_default_alignment']!='left': fail('table body default must be left')
if D['tables']['equal_width_default']!='forbidden': fail('equal-width table must be forbidden')
if D['paragraph']['alignment']!='left' or D['paragraph']['center_body_text']!='forbidden': fail('body alignment')
if D['visual_quality']['final_visual_review_required'] is not True: fail('visual review required')
if D['visual_quality']['contact_sheet_only_pass']!='forbidden': fail('contact sheet only pass')
T=load('terminology.json')
if T.get('dbVendorsAllowed')!=['Oracle','PostgreSQL','MariaDB']: fail('DB3 order')
C=load('product-coverage.json')
items=C.get('items',[])
if len(items)<55: fail(f'coverage too small: {len(items)}')
for x in items:
    for k in ['primaryDocument','secondaryDocument']:
        if x.get(k) not in ids: fail(f'coverage unknown document {x.get("id")}:{x.get(k)}')
    for d in x.get('alsoRequiredIn',[]):
        if d not in ids: fail(f'coverage unknown also document {x.get("id")}:{d}')
# delete manifest exact path discipline
for raw in (ROOT/'DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
    s=raw.strip()
    if not s or s.startswith('#'): continue
    if '*' in s or '?' in s or s.startswith('/') or '..' in Path(s).parts: fail(f'unsafe delete manifest path {s}')
# lock check last
lock=load('HARNESS_LOCK.json')
if lock.get('changeAuthority')!='USER_EXPLICIT_REQUEST_ONLY': fail('lock change authority')
for rel, expected in lock.get('files',{}).items():
    p=ROOT/rel
    if not p.is_file(): fail(f'lock missing {rel}')
    actual=hashlib.sha256(p.read_bytes()).hexdigest()
    if actual!=expected: fail(f'lock mismatch {rel}')
print('HARNESS=PASS')
print('VERSION='+str(h.get('version')))
print('ARTIFACTS='+str(len(arts)))
print('COVERAGE_ITEMS='+str(len(items)))
print('PROFILES='+str(len(list((ROOT/'profiles').glob('*.json')))))
print('TABLE_PRESETS='+str(len(table_presets)))
print('FIGURE_PRESETS='+str(len(figure_presets)))
