#!/usr/bin/env python3
"""QA31 BZA vertical capability coverage gate."""
from __future__ import annotations
import argparse,json,sys
from datetime import datetime,timezone
from pathlib import Path

ROWS={
"QA31-BZA-001":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/directory","cpf-biz-admin/frontend/src/features/organizations","cpf-biz-admin/src/test/java/com/cpf/bizadmin/directory"),
"QA31-BZA-002":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/directory","cpf-biz-admin/frontend/src/features/assignments","cpf-biz-admin/src/test/java/com/cpf/bizadmin/directory"),
"QA31-BZA-003":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth","cpf-biz-admin/frontend/src/features/users","cpf-biz-admin/src/test/java/com/cpf/bizadmin/auth"),
"QA31-BZA-004":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth","cpf-biz-admin/frontend/src/features/users","cpf-biz-admin/src/test/java/com/cpf/bizadmin/auth"),
"QA31-BZA-005":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval","cpf-biz-admin/frontend/src/features/approval-simulation","cpf-biz-admin/src/test/java/com/cpf/bizadmin/approval"),
"QA31-BZA-006":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval","cpf-biz-admin/frontend/src/features/approval-inbox","cpf-biz-admin/src/test/java/com/cpf/bizadmin/approval"),
"QA31-BZA-007":("cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval","cpf-biz-admin/frontend/src/features/approval-delegations","cpf-biz-admin/src/test/java/com/cpf/bizadmin/approval"),
"QA31-BZA-008":("cpf-biz-admin/frontend/src/App.test.ts","cpf-biz-admin/frontend/src/features"),
}
def now():return datetime.now(timezone.utc).isoformat()
def has_file(p:Path):
 if p.is_file():return p.stat().st_size>0
 return p.is_dir() and any(x.is_file() and x.stat().st_size>0 for x in p.rglob('*'))
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--output');ap.add_argument('--source-sha',default='WORKTREE-OVERLAY');a=ap.parse_args();root=Path(a.root).resolve();start=now();rows=[]
 for rid,rels in ROWS.items():
  paths=[{'path':r,'present':has_file(root/r)} for r in rels];rows.append({'requirementId':rid,'passed':all(x['present'] for x in paths),'paths':paths})
 ok=all(x['passed'] for x in rows);report={'schemaVersion':1,'gate':'CPF_BZA_QA31_COVERAGE','sourceSha':a.source_sha,'command':'verify-cpf-bza-qa31-coverage.py','startedAt':start,'finishedAt':now(),'exitCode':0 if ok else 1,'expected':'all BZA backend/frontend/test paths present','actual':f"passed={sum(1 for x in rows if x['passed'])}/{len(rows)}",'environment':{'runtime':'python3'},'profile':'structural-coverage','relatedIds':[x['requirementId'] for x in rows],'status':'PASS' if ok else 'FAIL','rows':rows,'sensitiveDataRemoved':True};text=json.dumps(report,ensure_ascii=False,indent=2)+'\n'
 if a.output:
  out=Path(a.output);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(text,encoding='utf-8')
 print(text,end='');return report['exitCode']
if __name__=='__main__':sys.exit(main())
