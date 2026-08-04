#!/usr/bin/env python3
"""Fail closed when internal-service headers are trusted without identity binding."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path

class IdentityGateError(RuntimeError):pass


def method_body(text:str,name:str)->str:
 m=re.search(rf'\b{name}\s*\([^)]*\)\s*\{{',text)
 if not m:raise IdentityGateError(f'method missing: {name}')
 opening=text.find('{',m.start());depth=0
 for i in range(opening,len(text)):
  if text[i]=='{':depth+=1
  elif text[i]=='}':
   depth-=1
   if depth==0:return text[opening+1:i]
 raise IdentityGateError(f'unclosed method: {name}')


def verify(root:Path)->dict:
 root=root.resolve();path=root/'cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java'
 if not path.is_file():raise IdentityGateError(f'missing {path}')
 text=path.read_text(encoding='utf-8-sig')
 default=method_body(text,'defaultIdentityVerifier')
 cert=method_body(text,'hasClientCertificate') if 'hasClientCertificate' in text else ''
 implementations=[]
 for p in root.rglob('*.java'):
  if p==path or any(x in p.parts for x in ('build','generated')):continue
  t=p.read_text(encoding='utf-8-sig',errors='replace')
  if re.search(r'(?:implements\s+CpfInternalServiceIdentityVerifier|CpfInternalServiceIdentityVerifier\s+\w+\s*\()',t):
   implementations.append(p.relative_to(root).as_posix())
 direct_cert_trust=bool(re.search(r'if\s*\(\s*hasClientCertificate\s*\(\s*request\s*\)',default))
 cert_binds_service='callerServiceId' in cert or 'callerInstanceId' in cert
 explicit_required=bool(re.search(r'productionProfile.*?(?:return\s+false|throw\s+new)',default,re.S)) and not direct_cert_trust
 checks={
  'no_unbound_client_certificate_trust':not direct_cert_trust or cert_binds_service,
  'production_requires_bound_or_explicit_verifier':explicit_required or cert_binds_service or bool(implementations),
  'shared_api_calls_verifier':'internalServiceIdentityVerifier.isTrusted(request, callerService, callerInstance)' in text,
 }
 missing=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not missing else 'FAIL','file':path.relative_to(root).as_posix(),'checks':checks,'explicitImplementations':implementations,'findings':missing}
 if missing:raise IdentityGateError(json.dumps(result,ensure_ascii=False,indent=2))
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
