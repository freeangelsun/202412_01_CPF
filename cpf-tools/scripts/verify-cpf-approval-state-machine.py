#!/usr/bin/env python3
"""Fail-closed source gate for ADM approval decision/execution state machines."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
class ApprovalStateError(RuntimeError):pass

def read(p:Path)->str:
 if not p.is_file():raise ApprovalStateError(f'missing {p}')
 return p.read_text(encoding='utf-8-sig',errors='replace')
def method(text:str,signature:str)->str:
 i=text.find(signature)
 if i<0:raise ApprovalStateError(f'method missing {signature}')
 op=text.find('{',i);d=0
 for n in range(op,len(text)):
  if text[n]=='{':d+=1
  elif text[n]=='}':
   d-=1
   if d==0:return text[op+1:n]
 raise ApprovalStateError(f'unclosed {signature}')

def verify(root:Path)->dict:
 root=root.resolve()
 legacy_path=root/'cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java'
 engine_path=root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmApprovalEngineService.java'
 legacy=read(legacy_path);engine=read(engine_path)
 legacy_execute=method(legacy,'public Map<String,Object> execute(')
 engine_decide=method(engine,'public Map<String, Object> decide(')
 engine_execute=method(engine,'public Object execute(')
 double_cas='startExecution(' in legacy_execute and bool(re.search(r'updateRequest\s*\(.*?"EXECUTING"',legacy_execute,re.S))
 participant_query=re.search(r'queryForMap\s*\((.*?)\);',engine_decide,re.S)
 query=participant_query.group(1) if participant_query else ''
 step_bound='step_no' in query.lower() and ('currentStep' in engine_decide or 'current_step' in engine_decide.lower())
 reserve=bool(re.search(r'(reserveExecution|startExecution|beginExecution|insertExecution)',engine_execute))
 finalize=bool(re.search(r'(finishExecution|completeExecution|markExecution|UNKNOWN|unknown)',engine_execute,re.I))
 idempotency=bool(re.search(r'(idempot|execution_status|EXECUTING)',engine_execute,re.I))
 checks={
  'legacy_execute_has_single_reservation_cas':not double_cas,
  'decision_is_bound_to_current_step':step_bound,
  'new_engine_reserves_execution_before_owner_call':reserve,
  'new_engine_records_final_or_unknown_result':finalize,
  'new_engine_execution_is_idempotent':idempotency,
 }
 missing=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not missing else 'FAIL','files':[legacy_path.relative_to(root).as_posix(),engine_path.relative_to(root).as_posix()],'checks':checks,'findings':missing,'risk':'future-step approval, duplicate owner execution, normal legacy execution CAS failure, UNKNOWN loss'}
 if missing:raise ApprovalStateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args()
 try:r=verify(Path(a.root));c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 text=json.dumps(r,ensure_ascii=False,indent=2)
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else Path(a.root).resolve()/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(text+'\n',encoding='utf-8')
 print(text);return c
if __name__=='__main__':raise SystemExit(main())
