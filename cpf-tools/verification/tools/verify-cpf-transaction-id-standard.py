#!/usr/bin/env python3
"""Repository-wide transactionId and controller annotation/fallback gate."""
from __future__ import annotations
import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 진단 메시지가 깨져 원인 판별을 방해한다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,json,re,sys
from pathlib import Path
class GateError(RuntimeError):pass
TEXT_EXT={'.java','.kt','.kts','.ts','.tsx','.js','.json','.yaml','.yml','.sql','.xml','.properties','.gradle'}
LEGACY=re.compile(r'(?<![A-Za-z0-9_])(globalId|gid)(?![A-Za-z0-9_])',re.I);CONTROLLER=re.compile(r'@(RestController|Controller)\b');ROUTE=re.compile(r'@(Get|Post|Put|Patch|Delete|Request)Mapping\b');ANNOT=re.compile(r'@(CpfOnlineTransaction|CpfSharedApi|CpfTransaction)\b')
ALLOW=('cpf-docs/','migration/','rollback/','cpf-tools/verification/tools/verify-cpf-transaction-id-standard.py')
def verify(root:Path):
 findings=[];scanned=0;controllers=0;routes=0;annotated=0;legacy=[]
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in TEXT_EXT or any(x in p.parts for x in ('.git','build','.gradle','node_modules','dist')):continue
  rel=p.relative_to(root).as_posix();text=p.read_text(encoding='utf-8-sig',errors='replace');scanned+=1
  if p.suffix=='.java' and CONTROLLER.search(text):
   controllers+=1;r=len(ROUTE.findall(text));a=len(ANNOT.findall(text));routes+=r;annotated+=a
  if not any(rel.startswith(x) or x in rel for x in ALLOW):
   for m in LEGACY.finditer(text):legacy.append({'file':rel,'line':text.count('\n',0,m.start())+1,'identifier':m.group(1)})
 web_filter=root/'cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfWebContextFilter.java'
 inbound=root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java'
 if not web_filter.is_file():
  findings.append('web context filter missing')
 else:
  t=web_filter.read_text(encoding='utf-8-sig')
  # Verify behavior, not stale local variable names. The filter must resolve ingress trust,
  # delegate identity construction to the canonical inbound adapter, and bind the resolved context.
  for token in ('OncePerRequestFilter','trustResolver.resolve','inbound.resolve','CpfContexts.bind'):
   if token not in t: findings.append(f'web filter semantic witness missing: {token}')
 if not inbound.is_file():
  findings.append('transaction inbound adapter missing')
 else:
  t=inbound.read_text(encoding='utf-8-sig')
  semantic_groups = {
   'canonical transaction validation': ('CpfTransactionIds.isCanonical', 'canonicalTransactionId('),
   'trusted internal branch': ('CpfHttpIngressTrust.TRUSTED_INTERNAL',),
   # External CPF channels serialize the canonical six system headers. The receiver validates
   # X-System-Code and X-Target-System-Code against authenticated runtime identity; headers are not authentication proof.
   'external canonical six system contract': (
       'requireExternal(rawTx, CpfHttpHeaderNames.TRANSACTION_ID)',
       'requireExternal(originalSystem, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE)',
       'requireExternal(inboundSystem, CpfHttpHeaderNames.SYSTEM_CODE)',
       'requireExternal(callerSystem, CpfHttpHeaderNames.CALLER_SYSTEM_CODE)',
       'requireExternal(targetSystem, CpfHttpHeaderNames.TARGET_SYSTEM_CODE)',
       'requireExternal(targetOperation, CpfHttpHeaderNames.TARGET_OPERATION_ID)'),
   'receiver runtime system validation': ('runtime.systemCode()', 'validateReceiverSystem(runtimeSystem, inboundSystem, targetSystem)'),
   'system target receiver validation': ('SYSTEM_CODE_MISMATCH', 'TARGET_SYSTEM_CODE_MISMATCH'),
   # issuer는 최초 신뢰 ChannelCode, Original-System은 업무 System lineage다. 둘의 universal
   # equality는 Namespace 혼합이므로 금지한다(Harness 30.7).
   'issuer and original-system namespace separation': ('CpfTransactionContext(',),
   # Channel remains optional policy/context metadata and cannot substitute for the six System headers.
   'optional channel context': ('inboundCurrentChannel = normalizeChannel(inboundCurrentChannel)', 'validateOptionalReceiverChannel('),
   'generated transaction fallback': ('requireGeneratedTransactionId(transactionIds.newTransactionId())',),
  }
  for label,tokens in semantic_groups.items():
   if not all(token in t for token in tokens): findings.append(f'inbound adapter semantic witness missing: {label}')
  if 'ORIGINAL_SYSTEM_CODE_MISMATCH' in t or 'issuerCode(tx)' in t:
   findings.append('inbound adapter must not force TransactionId issuer == X-Original-System-Code')
 if legacy:findings.append(f'legacy transaction identifier occurrences={len(legacy)}')
 # Zero annotations never proves route coverage; fallback must be present and measured.
 if routes and annotated==0 and not web_filter.is_file():findings.append('controller routes exist but no filter coverage')
 result={'status':'PASS' if not findings else 'FAIL','scannedFileCount':scanned,'controllerCount':controllers,'routeMappingCount':routes,'transactionAnnotationCount':annotated,'fallbackHeaderValidation':web_filter.is_file() and inbound.is_file(),'legacyOccurrences':legacy[:100],'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
