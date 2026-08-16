#!/usr/bin/env python3
"""Fail closed when unverified inbound caller identity can cross the HTTP trust boundary."""
from __future__ import annotations
import argparse,json
from pathlib import Path

class IdentityGateError(RuntimeError): pass

def read(root:Path, rel:str)->str:
 p=root/rel
 if not p.is_file(): raise IdentityGateError(f'missing {rel}')
 return p.read_text(encoding='utf-8-sig',errors='replace')

def verify(root:Path)->dict:
 root=root.resolve()
 filter_rel='cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfWebContextFilter.java'
 adapter_rel='cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java'
 rest_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfRestClientInterceptor.java'
 webclient_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfWebClientConfig.java'
 local_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfLocalServiceIdentity.java'
 sources={r:read(root,r) for r in (filter_rel,adapter_rel,rest_rel,webclient_rel,local_rel)}
 checks={
  'external_ingress_is_fail_closed':'CpfHttpIngressTrust.UNTRUSTED_EXTERNAL' in sources[filter_rel],
  'adapter_defaults_to_untrusted':'trust == null ? CpfHttpIngressTrust.UNTRUSTED_EXTERNAL : trust' in sources[adapter_rel],
  'adapter_does_not_promote_raw_caller_headers':'CALLER_SERVICE' not in sources[adapter_rel] and 'CALLER_INSTANCE_ID' not in sources[adapter_rel],
  'rest_client_overwrites_local_service':'headers.set(com.cpf.foundation.context.header.CpfHeaderNames.CALLER_SERVICE, localServiceIdentity.serviceId())' in sources[rest_rel],
  'rest_client_overwrites_local_instance':'headers.set(com.cpf.foundation.context.header.CpfHeaderNames.CALLER_INSTANCE_ID, localServiceIdentity.instanceId())' in sources[rest_rel],
  'webclient_overwrites_local_service':'headers.set(com.cpf.foundation.context.header.CpfHeaderNames.CALLER_SERVICE, localServiceIdentity.serviceId())' in sources[webclient_rel],
  'webclient_overwrites_local_instance':'headers.set(com.cpf.foundation.context.header.CpfHeaderNames.CALLER_INSTANCE_ID, localServiceIdentity.instanceId())' in sources[webclient_rel],
  'local_identity_is_configuration_bound':'cpf.framework.module-id' in sources[local_rel] and 'cpf.framework.instance-id' in sources[local_rel],
 }
 findings=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not findings else 'FAIL','checks':checks,'findings':findings,
         'policy':'Raw inbound caller headers are never identity proof; outbound caller identity is regenerated from the current CPF service.'}
 if findings: raise IdentityGateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args(); root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except Exception:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False,indent=2));return c
if __name__=='__main__':raise SystemExit(main())
