#!/usr/bin/env python3
"""Fail-closed network endpoint policy and DNS-to-socket pinning gate."""
from __future__ import annotations
import argparse, json, pathlib, re, sys

class GateError(RuntimeError): pass
POLICY="cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java"
POLICY_TEST="cpf-core/src/test/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicyTest.java"
REQUIRED={
 "cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java":(
  "validateEndpoint","validateResolvedAddresses","ResolvedEndpoint","pinnedAddress","rejectMixedAddressClasses","pinnedAddresses"),
 "cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfPinnedHttpConnectorFactory.java":(
  "remoteAddress","endpoint.pinnedAddress()","InetSocketAddress"),
 "cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfWebClient.java":(
  "resolvedEndpoint","pinnedConnectorFactory.connector","defaultHeader(\"Host\""),
 "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java":(
  "CpfNetworkEndpointPolicy","validateEndpoint","validateResolvedAddresses","pinned"),
 "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java":(
  "CpfNetworkEndpointPolicy","validateEndpoint","validateResolvedAddresses","openSocket","InetSocketAddress(target.address()"),
}
TESTS={
 "cpf-starters/integration/http-client/src/test/java/com/cpf/core/common/http/CpfServiceEndpointRegistryTest.java":(
  "dnsAddressIsValidatedAndReturnedAsTheActualConnectionPin","dnsRebindingToPrivateOrMixedAddressFailsBeforeConnectorCreation","configuredPinMismatchFailsClosed"),
 "cpf-starters/integration/http-client/src/test/java/com/cpf/core/common/http/CpfPinnedHttpConnectorFactoryContractTest.java":(
  "createsConnectorFromValidatedPinnedAddress",),
}
DUPLICATE_METHODS=re.compile(r"\b(?:private|protected|public|static|final|\s)+\s*(?:boolean|void)\s+(privateAddress|metadataAddress|validateAddress)\s*\(")
UNPINNED_BASEURL=re.compile(r"\.baseUrl\(\s*endpointRegistry\.baseUrl\(|URI\.create\(\s*endpointRegistry\.baseUrl\(")

def read(root:pathlib.Path,rel:str)->str:
 p=root/rel
 if not p.is_file(): raise GateError(f"required network source missing: {rel}")
 return p.read_text(encoding="utf-8-sig")

def verify(root:pathlib.Path)->dict[str,object]:
 errors=[]; scanned=[]
 policy=read(root,POLICY); tests=read(root,POLICY_TEST)
 for token in ("allowPrivate","allowPublic","allowDns","requireTls","allowedCidrs","allowedPorts","specialUseDenied","validateEndpoint","validateResolvedAddresses"):
  if token not in policy: errors.append(f"{POLICY}: capability missing: {token}")
 for token in ("supportsPrivateOnlyGatewayPolicy","alwaysRejectsSpecialUseEvenWhenPrivateAllowed","validatesIpv6AndDnsRebinding","rejectsMalformedCorpus"):
  if token not in tests: errors.append(f"{POLICY_TEST}: negative test missing: {token}")
 for rel,tokens in REQUIRED.items():
  text=read(root,rel);scanned.append(rel)
  for token in tokens:
   if token not in text: errors.append(f"{rel}: network/pinning token missing: {token}")
  if rel.endswith("CpfServiceEndpointRegistry.java") and "return validatedBaseUrl" in text:
   errors.append(f"{rel}: hostname-only endpoint return is forbidden")
  if rel.endswith("CpfWebClient.java") and UNPINNED_BASEURL.search(text):
   errors.append(f"{rel}: unpinned endpointRegistry.baseUrl transport is forbidden")
  for match in DUPLICATE_METHODS.finditer(text): errors.append(f"{rel}: duplicate network classifier forbidden: {match.group(1)}")
 for rel,tokens in TESTS.items():
  text=read(root,rel);scanned.append(rel)
  for token in tokens:
   if token not in text: errors.append(f"{rel}: pinning regression test missing: {token}")
 if "private void validateAddress(Address address)" not in policy:
  errors.append(f"{POLICY}: canonical address classifier missing")
 if errors: raise GateError("\n".join(errors))
 return {"status":"PASS","consumerCount":len(REQUIRED),"testFileCount":len(TESTS),"scannedFiles":scanned,
         "dnsRebinding":True,"socketAddressPinned":True,"mixedAddressResponseDenied":True}

def main()->int:
 p=argparse.ArgumentParser();p.add_argument("--root",default=".");p.add_argument("--json-output");a=p.parse_args();root=pathlib.Path(a.root).resolve()
 try:r=verify(root)
 except (GateError,OSError,UnicodeError) as exc:
  r={"status":"FAIL","message":str(exc)}
  if a.json_output:
   out=pathlib.Path(a.json_output);out=out if out.is_absolute() else root/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(r,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
  print(f"CPF shared network policy FAIL\n{exc}",file=sys.stderr);return 1
 if a.json_output:
  out=pathlib.Path(a.json_output);out=out if out.is_absolute() else root/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(r,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
 print(json.dumps(r,ensure_ascii=False));return 0
if __name__=="__main__":raise SystemExit(main())
