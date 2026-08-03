#!/usr/bin/env python3
"""Ensure Gateway, HTTP client, batch outbound, and Host Agent share one network policy."""
from __future__ import annotations
import argparse, json, pathlib, re, sys

class GateError(RuntimeError): pass
REQUIRED={
 "cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java":("CpfNetworkEndpointPolicy","validateEndpoint"),
 "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java":("CpfNetworkEndpointPolicy","validateEndpoint","validateResolvedAddresses"),
 "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java":("CpfNetworkEndpointPolicy","validateEndpoint","validateResolvedAddresses"),
}
POLICY="cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java"
TEST="cpf-core/src/test/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicyTest.java"
DUPLICATE_METHODS=re.compile(r"\b(?:private|protected|public|static|final|\s)+\s*(?:boolean|void)\s+(privateAddress|metadataAddress|validateAddress)\s*\(")

def read(root:pathlib.Path,rel:str)->str:
 p=root/rel
 if not p.is_file(): raise GateError(f"required network policy source is missing: {rel}")
 return p.read_text(encoding="utf-8")

def verify(root:pathlib.Path)->dict[str,object]:
 errors=[]
 for rel,tokens in REQUIRED.items():
  text=read(root,rel)
  for token in tokens:
   if token not in text: errors.append(f"{rel}: shared network policy token missing: {token}")
  for m in DUPLICATE_METHODS.finditer(text): errors.append(f"{rel}: duplicate network classifier forbidden: {m.group(1)}")
 policy=read(root,POLICY); tests=read(root,TEST)
 for token in ("allowPrivate","allowPublic","allowDns","requireTls","allowedCidrs","allowedPorts","specialUseDenied","validateEndpoint","validateResolvedAddresses"):
  if token not in policy: errors.append(f"{POLICY}: capability missing: {token}")
 for token in ("supportsPrivateOnlyGatewayPolicy","alwaysRejectsSpecialUseEvenWhenPrivateAllowed","validatesIpv6AndDnsRebinding","rejectsMalformedCorpus"):
  if token not in tests: errors.append(f"{TEST}: negative test missing: {token}")
 # The shared classifier itself may own validateAddress/privateAddress; consumers may not duplicate it.
 if "private void validateAddress(Address address)" not in policy:
  errors.append(f"{POLICY}: canonical address classifier is missing")
 if errors: raise GateError("\n".join(errors))
 return {"status":"PASS","consumerCount":len(REQUIRED),"sharedPolicy":POLICY,"dnsRebinding":True,"specialUseDenied":True}

def main()->int:
 p=argparse.ArgumentParser();p.add_argument("--root",default=".");p.add_argument("--json-output");a=p.parse_args()
 try:r=verify(pathlib.Path(a.root).resolve())
 except (GateError,OSError,UnicodeError) as exc:
  r={"status":"FAIL","message":str(exc)}
  if a.json_output:pathlib.Path(a.json_output).write_text(json.dumps(r,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
  print(f"CPF shared network policy FAIL\n{exc}",file=sys.stderr);return 1
 if a.json_output:pathlib.Path(a.json_output).write_text(json.dumps(r,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
 print("CPF shared network policy PASS");print(json.dumps(r,sort_keys=True));return 0
if __name__=="__main__":raise SystemExit(main())
