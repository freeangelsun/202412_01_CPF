#!/usr/bin/env python3
from pathlib import Path
import argparse,json,re,sys
MUT={'POST','PUT','PATCH','DELETE'}
def walk(d): return [p for p in d.rglob('*') if p.suffix in {'.ts','.vue'} and '/generated/' not in str(p).replace('\\','/')]
def check(root):
 fe=root/'cpf-admin/frontend'; routes=(fe/'src/app/routes.ts').read_text(); spec=json.loads((fe/'openapi/cpf-openapi.json').read_text()); gen=(fe/'src/generated/cpf-api.ts').read_text();
 ops={}
 for path,item in spec.get('paths',{}).items():
  for m,o in item.items():
   if m.upper() in MUT|{'GET'} and isinstance(o,dict) and o.get('operationId'):ops[o['operationId']]={'method':m.upper(),'path':path}
 high=set()
 for line in routes.splitlines():
  if not re.search(r'riskLevel:\s*"(?:HIGH|CRITICAL)"',line):continue
  b=re.search(r'expectedOperationIds\s*:\s*\[([^\]]*)\]',line)
  if b:high.update(re.findall(r'["\']([^"\']+)["\']',b.group(1)))
 targets={x for x in high if x in ops and ops[x]['method'] in MUT}
 src='\n'.join(p.read_text(errors='ignore') for p in walk(fe/'src'))
 errs=[];consumed=[]
 for op in sorted(targets):
  if not re.search(rf'export async function\s+{re.escape(op)}\b',gen):errs.append('generated-missing:'+op);continue
  # Direct generated import plus invocation anywhere in actual source.
  imported=False;called=False
  for f in walk(fe/'src'):
   t=f.read_text(errors='ignore')
   if re.search(rf'import\s*\{{[^}}]*\b{re.escape(op)}\b[^}}]*\}}\s*from\s*["\'][^"\']*generated/(?:orval/)?cpf-api["\']',t,re.S):
    imported=True
    if re.search(rf'\b{re.escape(op)}(?:<[^>]+>)?\s*\(',t):called=True;break
  if not imported:errs.append('generated-import-missing:'+op)
  elif not called:errs.append('generated-call-missing:'+op)
  else:consumed.append(op)
 # raw/generic invocation of high-risk operation IDs is forbidden even if another generated call exists
 for op in sorted(targets):
  if re.search(rf'admInvokeOperation(?:<[^>]+>)?\s*\(\s*["\']{re.escape(op)}["\']',src):errs.append('generic-bypass:'+op)
 return errs,len(targets),len(consumed)
def main():
 a=argparse.ArgumentParser();a.add_argument('--root',required=True);a.add_argument('--self-test',action='store_true');x=a.parse_args();r=Path(x.root);e,n,c=check(r)
 if e:print('[CPF][ADM-HIGH-RISK][FAIL]',*e,sep='\n');return 1
 if x.self_test:
  p=r/'cpf-admin/frontend/src/features/batch-runtime-control/BatchOperationsWorkbench.vue';o=p.read_text();p.write_text(o.replace('submitBatchRuntimeCommand({approvalRequestId:command.approvalId,reason:command.reason})','void command.approvalId',1));d=bool(check(r)[0]);p.write_text(o)
  if not d:print('[CPF][ADM-HIGH-RISK][FAIL] mutation-not-detected');return 1
 print(f'[CPF][ADM-HIGH-RISK][PASS] mutations={n} generatedConsumed={c} violations=0 selfTest={str(x.self_test).lower()}');return 0
if __name__=='__main__':sys.exit(main())
