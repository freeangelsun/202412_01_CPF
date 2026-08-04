#!/usr/bin/env python3
"""Fail closed if BAT runtime commands can bypass optimistic version fencing."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
class VersionError(RuntimeError):pass

def read(p:Path)->str:
 if not p.is_file():raise VersionError(f'missing {p}')
 return p.read_text(encoding='utf-8-sig',errors='replace')
def method(text:str,name:str)->str:
 m=re.search(rf'\b{name}\s*\([^)]*\)\s*\{{',text)
 if not m:raise VersionError(f'method missing {name}')
 op=text.find('{',m.start());d=0
 for i in range(op,len(text)):
  if text[i]=='{':d+=1
  elif text[i]=='}':
   d-=1
   if d==0:return text[op+1:i]
 raise VersionError(f'unclosed {name}')

def verify(root:Path)->dict:
 root=root.resolve()
 cp=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
 rp=root/'cpf-batch/contract/src/main/java/com/cpf/batch/api/RuntimeCommand.java'
 jp=root/'cpf-batch/control-server/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java'
 c=read(cp);r=read(rp);j=read(jp)
 command=method(c,'command');update=method(j,'updateDesiredState')
 checks={
  'controller_requires_expected_version':'requireExpectedVersion(request)' in command,
  'contract_rejects_negative_version':bool(re.search(r'expectedVersion\s*<\s*0',r)),
  'registry_does_not_replace_zero_with_current':not bool(re.search(r'expectedVersion\s*>\s*0\s*\?\s*expectedVersion\s*:\s*current',update)),
  'registry_uses_caller_version_in_cas':bool(re.search(r'runtime-desired-state-update.*?expectedVersion|expectedVersion.*?runtime-desired-state-update',update,re.S)),
 }
 missing=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not missing else 'FAIL','files':[cp.relative_to(root).as_posix(),rp.relative_to(root).as_posix(),jp.relative_to(root).as_posix()],'checks':checks,'findings':missing,'risk':'missing/zero/negative expectedVersion silently uses current row and defeats CAS'}
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
