#!/usr/bin/env python3
"""Whole ADM/BZA frontend and controller actor trust-boundary gate."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
class GateError(RuntimeError):pass
ALIASES=('requestUser','requestedBy','actorId','operatorId','operatorIdOverride')
def verify(root:Path):
 findings=[];front=[];controllers=[]
 for base in ('cpf-admin/frontend/src','cpf-biz-admin/frontend/src'):
  for p in (root/base).rglob('*') if (root/base).is_dir() else []:
   if p.is_file() and p.suffix in ('.ts','.tsx','.js','.vue'):
    t=p.read_text(encoding='utf-8-sig',errors='replace');front.append(p.relative_to(root).as_posix())
    if ('fetch(' in t or 'cpfOrvalRequest' in t) and any(re.search(rf'\b{x}\b',t) for x in ALIASES) and 'assertNoClientActor' not in t:findings.append(f'{p.relative_to(root)}: privileged actor field may be client-controlled')
 for rel in ('cpf-admin/frontend/src/shared/cpfApi.ts','cpf-biz-admin/frontend/src/shared/cpfApi.ts'):
  p=root/rel
  if not p.is_file():findings.append(f'missing {rel}');continue
  t=p.read_text(encoding='utf-8-sig')
  for tok in ('URLSearchParams','FormData','Blob','JSON.parse','assertNoClientActorQuery'):
   if tok not in t:findings.append(f'{rel}: body/query guard missing {tok}')
 for base in ('cpf-admin/src/main/java','cpf-biz-admin/src/main/java'):
  for p in (root/base).rglob('*Controller.java') if (root/base).is_dir() else []:
   t=p.read_text(encoding='utf-8-sig');controllers.append(p.relative_to(root).as_posix())
   if re.search(r'@(Post|Put|Patch|Delete)Mapping',t) and any(re.search(rf'@RequestParam[^\n]*\b{x}\b|@RequestBody[^\n]*\b{x}\b',t,re.I) for x in ALIASES):findings.append(f'{p.relative_to(root)}: mutation accepts client actor')
 batch=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
 test=root/'cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java'
 if not batch.is_file():findings.append('BatchRuntimeControlController missing')
 else:
  t=batch.read_text(encoding='utf-8-sig')
  for tok in ('@RequestAttribute("adm.operatorId")','withServerActor','saveJobDefinition','transitionJobDefinition','command(','plan('):
   if tok not in t:findings.append(f'batch controller actor contract missing {tok}')
 if not test.is_file():findings.append('endpoint-level actor test missing')
 else:
  t=test.read_text(encoding='utf-8-sig')
  for tok in ('everyPrivilegedEndpointUsesAuthenticatedActorAndStripsNestedAliases','validationErrorsAreAlways400AndNeverUnknownResult','typedOwnerErrorsUseOneEndpointIndependentStatusMatrix','unexpectedTransportFailureIsOnlyCaseMappedToUnknownResult'):
   if tok not in t:findings.append(f'endpoint test missing {tok}')
 result={'status':'PASS' if not findings else 'FAIL','frontendFileCount':len(front),'controllerFileCount':len(controllers),'aliases':list(ALIASES),'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
