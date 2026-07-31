#!/usr/bin/env python3
from pathlib import Path
import csv, sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
sha='e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a'
required=[
'cpf-docs/work/review/CPF_20260801_QA34_POST_PUSH_REVIEW_PLAN.md',
'cpf-docs/work/review/CPF_20260801_QA34_POST_PUSH_INDEPENDENT_SOURCE_REVIEW.md',
'cpf-docs/work/review/CPF_20260801_QA35_ADM_BENCHMARK_REVIEW_PLAN.md',
'cpf-docs/work/review/CPF_20260801_QA35_ADM_REFERENCE_INVENTORY.md',
'cpf-docs/work/review/CPF_20260801_QA35_ADM_BATCH_ONLINE_BENCHMARK_REVIEW.md',
'cpf-docs/work/current/CPF_20260801_QA35_FINAL_COMPLETION_DEVELOPMENT_REQUEST.md',
'cpf-docs/work/current/CPF_20260801_QA35_SELF_DEVELOPMENT_REQUIREMENTS.md',
'cpf-docs/work/current/CPF_20260801_QA35_CODEX_FINAL_VERIFICATION_REQUEST.md',
'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md',
'cpf-docs/quality/CPF_20260801_QA35_DEFECT_REGISTER.csv',
'cpf-docs/quality/CPF_20260801_QA35_REQUIREMENT_MATRIX.csv',
'cpf-docs/quality/CPF_20260801_QA35_ROOT_CAUSE_MATRIX.csv',
'cpf-docs/quality/CPF_20260801_QA35_ADM_CAPABILITY_MATRIX.csv',
'cpf-docs/quality/CPF_20260801_QA35_ADM_MENU_ROUTE_MATRIX.csv',
'cpf-docs/work/handover/CPF_20260801_QA34_TO_QA35_HANDOVER.md',
'cpf-docs/work/state/CPF_20260801_QA35_CONTINUITY_STATE.md']
missing=[p for p in required if not (root/p).is_file()]
if missing: raise SystemExit('missing required files: '+', '.join(missing))
def rows(p):
 with (root/p).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
defects=rows('cpf-docs/quality/CPF_20260801_QA35_DEFECT_REGISTER.csv')
reqs=rows('cpf-docs/quality/CPF_20260801_QA35_REQUIREMENT_MATRIX.csv')
causes=rows('cpf-docs/quality/CPF_20260801_QA35_ROOT_CAUSE_MATRIX.csv')
caps=rows('cpf-docs/quality/CPF_20260801_QA35_ADM_CAPABILITY_MATRIX.csv')
menus=rows('cpf-docs/quality/CPF_20260801_QA35_ADM_MENU_ROUTE_MATRIX.csv')
expected=(36,43,15,68,59)
actual=(len(defects),len(reqs),len(causes),len(caps),len(menus))
if actual!=expected: raise SystemExit(f'count mismatch actual={actual} expected={expected}')
for name,data,key in [('defect',defects,'defect_id'),('requirement',reqs,'requirement_id'),('root cause',causes,'root_cause_id'),('capability',caps,'capability_id'),('menu route',menus,'route_id')]:
 ids=[r[key] for r in data]
 if len(ids)!=len(set(ids)): raise SystemExit(name+' duplicate IDs')
if any(r['development_status']=='완료' for r in reqs): raise SystemExit('QA35 request must not predeclare development complete')
if not any(r['status']=='미구현' for r in caps): raise SystemExit('ADM capability matrix unexpectedly has no missing capability')
current=(root/'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md').read_text(encoding='utf-8')
for token in ['CPF_20260801_QA35_FINAL_COMPLETION_DEVELOPMENT_REQUEST.md','CPF_20260801_QA35_ADM_CAPABILITY_MATRIX.csv',sha]:
 if token not in current: raise SystemExit('current request missing '+token)
review=(root/'cpf-docs/work/review/CPF_20260801_QA35_ADM_BATCH_ONLINE_BENCHMARK_REVIEW.md').read_text(encoding='utf-8')
for token in ['59개 메뉴','Generic Dynamic Table','Batch-Online-Gateway-Incident-Audit','Defect: 18 → 36건']:
 if token not in review: raise SystemExit('ADM review missing finding: '+token)
print(f'[CPF][QA35][PASS] request integrity defects={len(defects)} requirements={len(reqs)} rootCauses={len(causes)} admCapabilities={len(caps)} admMenus={len(menus)} baseline={sha}')
