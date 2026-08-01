#!/usr/bin/env python3
"""Ensure Gateway, batch outbound, and Host Agent share the CPF network policy."""
from __future__ import annotations
import argparse,re
from pathlib import Path

REQUIRED={
 'cpf-core/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java':['CpfNetworkEndpointPolicy','validateEndpoint'],
 'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java':['CpfNetworkEndpointPolicy','validateResolvedAddresses'],
 'cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java':['CpfNetworkEndpointPolicy','validateResolvedAddresses'],
}
DUPLICATE_METHODS=re.compile(r'\b(?:private|protected|public|static|final|\s)+\s*(?:boolean|void)\s+(privateAddress|metadataAddress|validateAddress)\s*\(')

def validate(root:Path):
 errors=[]
 for rel,tokens in REQUIRED.items():
  p=root/rel
  if not p.is_file(): errors.append(f'missing network policy consumer: {rel}');continue
  text=p.read_text(encoding='utf-8')
  for token in tokens:
   if token not in text: errors.append(f'{rel}: shared network policy token missing: {token}')
  for m in DUPLICATE_METHODS.finditer(text): errors.append(f'{rel}: duplicate network classifier method forbidden: {m.group(1)}')
 policy=root/'cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java'
 test=root/'cpf-core/src/test/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicyTest.java'
 if not policy.is_file() or not test.is_file(): errors.append('shared network policy source/test missing')
 else:
  source=policy.read_text(encoding='utf-8');tests=test.read_text(encoding='utf-8')
  for token in ['allowPrivate','allowPublic','allowDns','requireTls','allowedCidrs','allowedPorts','specialUseDenied']:
   if token not in source: errors.append(f'network policy capability missing: {token}')
  for token in ['supportsPrivateOnlyGatewayPolicy','alwaysRejectsSpecialUseEvenWhenPrivateAllowed','validatesIpv6AndDnsRebinding']:
   if token not in tests: errors.append(f'network policy negative test missing: {token}')
 return errors

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',type=Path,default=Path.cwd());a=p.parse_args();errors=validate(a.root.resolve())
 for e in errors: print('[FAIL]',e)
 if errors:return 1
 print('[PASS] CPF shared network policy consumers=3 gateway/batch-outbound/host-agent duplicateClassifiers=0')
 return 0
if __name__=='__main__':raise SystemExit(main())
