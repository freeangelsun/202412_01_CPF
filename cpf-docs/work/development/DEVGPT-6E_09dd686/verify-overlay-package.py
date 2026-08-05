#!/usr/bin/env python3
from pathlib import Path
import csv,hashlib,json,sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve();doc=root/'cpf-docs/work/development/DEVGPT-6E_09dd686'
manifest=json.loads((doc/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8'));errors=[]
def rows(n):
 with (doc/n).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
for i in manifest['files']:
 p=root/i['path']
 if not p.is_file():errors.append('missing:'+i['path']);continue
 if p.stat().st_size!=i['size'] or hashlib.sha256(p.read_bytes()).hexdigest()!=i['sha256']:errors.append('hash:'+i['path'])
for n,k,e in [('WORK_ITEM_SCOPE.csv','work_item_id',60),('CANONICAL_REQUIREMENT_SCOPE.csv','canonical_requirement_id',12),('CPF_FR_SCOPE.csv','requirement_id',1044),('CPF_SC_SCOPE.csv','scenario_id',1449),('ENGINEERING_GATE_SCOPE.csv','engineering_gate_id',16),('WORK_ITEM_DEVELOPMENT_REVIEW.csv','work_item_id',60),('REQUIREMENT_DEVELOPMENT_REVIEW.csv','requirement_id',1044),('SCENARIO_STATUS.csv','scenario_id',1449)]:
 rr=rows(n);v=[r[k] for r in rr]
 if len(rr)!=e:errors.append(f'count:{n}:{len(rr)}:{e}')
 if len(v)!=len(set(v)):errors.append('duplicate:'+n)
for r in rows('REQUIREMENT_DEVELOPMENT_REVIEW.csv'):
 for c in ('acceptance_criteria','actual_source_and_implementation','actual_consumer_and_call_path','test_method_and_assertion','executed_command','exit_code','actual_result','evidence_path','development_status','verification_status','remaining_runtime_gap','review_status'):
  if not r.get(c):errors.append(f'requirement-blank:{r.get("requirement_id")}:{c}')
for r in rows('SCENARIO_STATUS.csv'):
 for c in ('expected_result','failure_criteria','actual_source_and_consumer','test_method_and_assertion','executed_command','exit_code','actual_result','evidence_path','development_status','verification_status','remaining_runtime_gap','review_status'):
  if not r.get(c):errors.append(f'scenario-blank:{r.get("scenario_id")}:{c}')
if rows('DELETE_MANIFEST.csv'):errors.append('delete-targets-present')
actual={p.relative_to(root).as_posix() for p in (root/'cpf-tools').rglob('*') if p.is_file()};changes={r['path'] for r in rows('CHANGE_MANIFEST.csv')}
if actual!=changes:errors.append('change-manifest-mismatch')
for p in root.rglob('*'):
 if p.is_file() and p.suffix.lower() in {'.md','.csv','.json','.txt','.py','.ps1'}:
  if p.name == 'verify-overlay-package.py': continue
  t=p.read_text(encoding='utf-8-sig',errors='ignore')
  if 'faedf43' in t or 'faedf43a7baffdad456bf40f8e46d622db9cfc76' in t or 'CPF_DEVELOPMENT_WORKLIST_V7_1' in t:errors.append('stale:'+p.relative_to(root).as_posix())
if errors:print('PACKAGE_VERIFY_FAIL');print('\n'.join(errors[:500]));raise SystemExit(1)
print('PACKAGE_VERIFY_PASS baseline=09dd686c5ae0826594b9c5e1f871d95d95d3ce1c files='+str(len(manifest['files']))+' productFiles='+str(len(actual))+' workItems=60 cpfFr=1044 cpfSc=1449 gates=16')
