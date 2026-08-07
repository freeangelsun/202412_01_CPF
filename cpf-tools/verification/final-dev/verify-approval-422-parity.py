#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path

def fail(m): print('[CPF][R6J][APPROVAL422][FAIL] '+m,file=sys.stderr); raise SystemExit(1)
def main():
 a=argparse.ArgumentParser();a.add_argument('--root',type=Path,default=Path('.'));ns=a.parse_args();r=ns.root.resolve()
 specs=[r/'cpf-admin/frontend/openapi/cpf-openapi.json',r/'cpf-biz-admin/frontend/openapi/cpf-openapi.json']
 total=0
 for spec in specs:
  data=json.loads(spec.read_text(encoding='utf-8-sig'))
  for path,item in data.get('paths',{}).items():
   if 'approval' not in path.lower(): continue
   for method in ('post','put','patch','delete'):
    op=item.get(method)
    if isinstance(op,dict):
     total+=1
     if '422' not in op.get('responses',{}): fail(f'{spec}: {method.upper()} {path} missing 422')
 for rel in ['cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java','cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/controller/BzaApprovalPolicyController.java']:
  text=(r/rel).read_text(encoding='utf-8')
  mutations=len(re.findall(r'@(Post|Put|Patch|Delete)Mapping',text))
  annotations=text.count('@ApiResponse(responseCode = "422"')
  if annotations < mutations: fail(f'{rel}: 422 annotations {annotations} < mutations {mutations}')
 for rel in ['cpf-admin/frontend/src/shared/orval-mutator.ts','cpf-biz-admin/frontend/src/shared/orval-mutator.ts']:
  text=(r/rel).read_text(encoding='utf-8')
  if 'status === 422' not in text or '"VALIDATION"' not in text: fail(rel+' missing 422 validation taxonomy')
 print(f'[CPF][R6J][APPROVAL422][PASS] approvalMutations={total}')
if __name__=='__main__': main()
