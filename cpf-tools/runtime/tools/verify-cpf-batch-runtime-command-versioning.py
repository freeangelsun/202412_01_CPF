#!/usr/bin/env python3
"""Fail closed if BAT runtime commands can bypass optimistic version fencing.

Current architecture intentionally keeps browser input small: the browser sends only
approvalRequestId + reason.  The immutable approval risk snapshot owns target,
action and expectedVersion, and the BAT owner adapter must forward that exact
version into the runtime registry CAS.  This verifier checks that end-to-end path
instead of requiring the removed pre-approval controller contract.
"""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
class VersionError(RuntimeError):pass

def read(p:Path)->str:
 if not p.is_file():raise VersionError(f'missing {p}')
 return p.read_text(encoding='utf-8-sig',errors='replace')

def method(text:str,name:str)->str:
 # Find a declaration occurrence: calls are followed by ';' or another token, while
 # a method declaration is followed by a body brace after its balanced signature.
 for m in re.finditer(rf'\b{re.escape(name)}\s*\(',text):
  paren=text.find('(',m.start());depth=0;close=-1
  for i in range(paren,len(text)):
   if text[i]=='(':depth+=1
   elif text[i]==')':
    depth-=1
    if depth==0:close=i;break
  if close<0:continue
  k=close+1
  while k<len(text) and text[k].isspace():k+=1
  if k>=len(text) or text[k]!='{':continue
  op=k;depth=0
  for i in range(op,len(text)):
   if text[i]=='{':depth+=1
   elif text[i]=='}':
    depth-=1
    if depth==0:return text[op+1:i]
  raise VersionError(f'unclosed {name}')
 raise VersionError(f'method missing {name}')

def verify(root:Path)->dict:
 root=root.resolve()
 cp=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
 rq=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java'
 ap=root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java'
 rp=root/'cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeCommand.java'
 jp=root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java'
 c=read(cp);request=read(rq);adapter=read(ap);r=read(rp);j=read(jp)
 command=method(c,'command');update=method(j,'updateDesiredState');execute=method(adapter,'executeRuntimeCommand')
 checks={
  'browser_command_requires_approval_id':'approvalRequestId' in command and 'approvalService.execute' in command,
  'browser_request_does_not_accept_target_or_version':all(token not in request for token in ('expectedVersion','targetIds','commandType','approvedBy')),
  'approval_owner_requires_snapshot_version':'risk.expectedVersion() == null' in execute or 'risk.expectedVersion()==null' in execute,
  'approval_owner_forwards_exact_snapshot_version':'request.put("expectedVersion", risk.expectedVersion())' in execute,
  'contract_rejects_negative_version':bool(re.search(r'expectedVersion\s*<\s*0',r)),
  'registry_rejects_negative_version':bool(re.search(r'expectedVersion\s*<\s*0',update)),
  'registry_does_not_replace_zero_with_current':not bool(re.search(r'expectedVersion\s*>\s*0\s*\?\s*expectedVersion\s*:\s*current',update)),
  'registry_compares_exact_current_version':bool(re.search(r'current(?:\.longValue\(\))?\s*!=\s*expectedVersion|expectedVersion\s*!=\s*current(?:\.longValue\(\))?',update)),
  'registry_uses_caller_version_in_cas':bool(re.search(r'runtime-desired-state-update.*?expectedVersion|expectedVersion.*?runtime-desired-state-update',update,re.S)),
 }
 missing=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not missing else 'FAIL','files':[p.relative_to(root).as_posix() for p in (cp,rq,ap,rp,jp)],'checks':checks,'findings':missing,'architecture':'BROWSER_APPROVAL_ID -> IMMUTABLE_APPROVAL_SNAPSHOT -> BAT_OWNER -> EXACT_VERSION_CAS','risk':'missing/zero/negative expectedVersion must never be silently replaced with the current row version'}
 if missing:raise VersionError(json.dumps(result,ensure_ascii=False,indent=2))
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
