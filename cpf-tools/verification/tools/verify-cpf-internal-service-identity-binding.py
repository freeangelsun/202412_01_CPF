#!/usr/bin/env python3
"""Verify CPF internal caller trust and canonical Domain-header ownership."""
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
 resolver_rel='cpf-starters/web/src/main/java/com/cpf/web/context/CpfConfiguredIngressTrustResolver.java'
 rest_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfRestClientInterceptor.java'
 webclient_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfWebClientConfig.java'
 domain_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/domaincall/CpfHttpDomainRemoteTransport.java'
 outbound_rel='cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpOutboundContextAdapter.java'
 local_rel='cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfLocalServiceIdentity.java'
 sources={r:read(root,r) for r in (filter_rel,adapter_rel,resolver_rel,rest_rel,webclient_rel,domain_rel,outbound_rel,local_rel)}
 checks={
  'ingress_uses_trust_resolver':'trustResolver.resolve(request)' in sources[filter_rel],
  'unverified_ingress_defaults_external':'return new Decision(CpfHttpIngressTrust.UNTRUSTED_EXTERNAL, null)' in sources[resolver_rel],
  'verified_caller_not_sourced_from_header':'VERIFIED_INTERNAL_CALLER_ATTRIBUTE' in sources[resolver_rel]
      and 'request.getHeader(' not in sources[resolver_rel],
  'adapter_defaults_to_untrusted':'trust == null ? CpfHttpIngressTrust.UNTRUSTED_EXTERNAL : trust' in sources[adapter_rel],
  'generic_rest_client_does_not_inject_internal_headers':'Intentionally no-op' in sources[rest_rel]
      and 'CALLER_SYSTEM_CODE' not in sources[rest_rel],
  'generic_webclient_does_not_inject_internal_headers':'CpfHttpOutboundContextAdapter' not in sources[webclient_rel]
      and 'CALLER_SYSTEM_CODE' not in sources[webclient_rel],
  'typed_domain_transport_owns_internal_propagation':'CpfHttpOutboundContextAdapter' in sources[domain_rel]
      and 'trustedInternal' in sources[outbound_rel],
  'outbound_adapter_emits_canonical_system_headers':all(token in sources[outbound_rel] for token in (
      'TRANSACTION_ID','ORIGINAL_SYSTEM_CODE','SYSTEM_CODE','CALLER_SYSTEM_CODE','TARGET_SYSTEM_CODE','TARGET_OPERATION_ID')),
  'external_outbound_has_separate_allowlist_path':'putAllowedCustom(headers, target.customHeaders(), false)' in sources[outbound_rel],
  'local_identity_uses_canonical_runtime_instance':'CpfInstanceIdentity.current().instanceId()' in sources[local_rel],
 }
 findings=[k for k,v in checks.items() if not v]
 result={'status':'PASS' if not findings else 'FAIL','checks':checks,'findings':findings,
         'policy':'Inbound caller identity comes from verified security/peer mapping; generic HTTP clients never inject CPF internal protocol headers; typed Domain transport owns canonical six.'}
 if findings: raise IdentityGateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except Exception:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False,indent=2));return c
if __name__=='__main__':raise SystemExit(main())
