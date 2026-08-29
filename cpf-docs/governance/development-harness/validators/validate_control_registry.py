#!/usr/bin/env python3
from pathlib import Path
import csv,json
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
c=json.loads((H/'contracts/contract-registry.json').read_text(encoding='utf-8'))
with (H/'contracts/harness-control-registry.csv').open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
errors=[]
ids=[r.get('control_id','').strip() for r in rows]; cats=[r.get('category','').strip() for r in rows]
if not rows: errors.append('EMPTY_CONTROL_REGISTRY')
if len(ids)!=len(set(ids)): errors.append('DUPLICATE_CONTROL_ID')
required=set(c.get('requiredControlCategories',[])); actual=set(cats)
for cat in sorted(required-actual): errors.append('MISSING_CONTROL_CATEGORY '+cat)
for r in rows:
 for k in ['control_id','category','rule','acceptance','enforcement','enforcement_type','mandatory_for_final','execution_control_id']:
  if not r.get(k,'').strip(): errors.append('EMPTY_FIELD '+r.get('control_id','?')+' '+k)
 ep=(H/r.get('enforcement','')) if r.get('enforcement','').startswith(('validators/','standards/','product/')) else (ROOT/r.get('enforcement',''))
 if r.get('enforcement') and not ep.exists(): errors.append('MISSING_ENFORCEMENT '+r['control_id']+' '+r['enforcement'])
 if r.get('enforcement_type') not in {'POLICY','STATIC_VERIFIER','RUNTIME_VERIFIER','EVIDENCE_GATE'}: errors.append('BAD_ENFORCEMENT_TYPE '+r['control_id'])
 if r.get('mandatory_for_final')=='true' and not r.get('execution_control_id','').strip(): errors.append('MISSING_EXECUTION_CONTROL '+r['control_id'])
if errors:
 [print('FAIL',e) for e in errors]; print('HARNESS_CONTROL_REGISTRY=FAIL ERRORS='+str(len(errors))); raise SystemExit(1)
print(f'HARNESS_CONTROL_REGISTRY=PASS CONTROLS={len(rows)} CATEGORIES={len(actual)}')
