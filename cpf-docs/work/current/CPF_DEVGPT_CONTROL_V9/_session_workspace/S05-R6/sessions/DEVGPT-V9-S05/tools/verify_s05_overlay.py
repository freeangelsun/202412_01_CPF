#!/usr/bin/env python3
from pathlib import Path
import argparse,csv,json,hashlib,sys
p=argparse.ArgumentParser();p.add_argument('--repo',required=True);a=p.parse_args();root=Path(a.repo).resolve()
s=root/'cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/S05-R6/sessions/DEVGPT-V9-S05'
problems=[]
def rows(name):
 with (s/'results'/name).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
for name,key,n in [('WORK_ITEM_RESULT.csv','work_item_id',95),('DEVELOPMENT_REQUIREMENT_RESULT.csv','requirement_id',1159),('DEVELOPMENT_SCENARIO_RESULT.csv','scenario_id',1368),('ENGINEERING_GATE_RESULT.csv','gate_id',19)]:
 r=rows(name)
 if len(r)!=n or len({x[key] for x in r})!=n:problems.append(f'COUNT:{name}')
 if any(not x.get('evidence_ref','').strip() for x in r):problems.append(f'EVIDENCE:{name}')
for x in rows('CHANGE_MANIFEST.csv'):
 q=root/x['path']
 if not q.is_file():problems.append('MISSING:'+x['path']);continue
 if hashlib.sha256(q.read_bytes()).hexdigest()!=x['sha256']:problems.append('HASH:'+x['path'])
a=json.loads((s/'evidence/ASSIGNMENT_VALIDATION.json').read_text())
for k in ['unreviewed_ids','missing_ids','duplicate_primary_ids','unassigned_ids','adjudications_without_evidence','consumer_unconfirmed','actionable_p0_p1_unaddressed']:
 if a.get(k)!=0:problems.append(f'ASSIGNMENT:{k}={a.get(k)}')
print(json.dumps({'status':'PASS' if not problems else 'FAIL','problems':problems},ensure_ascii=False,indent=2))
sys.exit(1 if problems else 0)
