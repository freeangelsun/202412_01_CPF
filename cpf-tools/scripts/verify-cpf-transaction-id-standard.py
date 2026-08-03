#!/usr/bin/env python3
"""Repository-wide transactionId and controller annotation/fallback gate."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
class GateError(RuntimeError):pass
TEXT_EXT={'.java','.kt','.kts','.ts','.tsx','.js','.json','.yaml','.yml','.sql','.xml','.properties','.gradle'}
LEGACY=re.compile(r'(?<![A-Za-z0-9_])(globalId|gid)(?![A-Za-z0-9_])',re.I);CONTROLLER=re.compile(r'@(RestController|Controller)\b');ROUTE=re.compile(r'@(Get|Post|Put|Patch|Delete|Request)Mapping\b');ANNOT=re.compile(r'@(CpfOnlineTransaction|CpfSharedApi|CpfTransaction)\b')
ALLOW=('cpf-docs/','migration/','rollback/','cpf-tools/scripts/verify-cpf-transaction-id-standard.py','cpf-tools/scripts/tests/test_verify_cpf_transaction-id-standard.py')
def verify(root:Path):
 findings=[];scanned=0;controllers=0;routes=0;annotated=0;legacy=[]
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in TEXT_EXT or any(x in p.parts for x in ('.git','build','.gradle','node_modules','dist')):continue
  rel=p.relative_to(root).as_posix();text=p.read_text(encoding='utf-8-sig',errors='replace');scanned+=1
  if p.suffix=='.java' and CONTROLLER.search(text):
   controllers+=1;r=len(ROUTE.findall(text));a=len(ANNOT.findall(text));routes+=r;annotated+=a
  if not any(rel.startswith(x) or x in rel for x in ALLOW):
   for m in LEGACY.finditer(text):legacy.append({'file':rel,'line':text.count('\n',0,m.start())+1,'identifier':m.group(1)})
 interceptor=root/'cpf-core/src/main/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptor.java'
 if not interceptor.is_file():findings.append('transaction header interceptor missing')
 else:
  t=interceptor.read_text(encoding='utf-8-sig')
  for token in ('isInfrastructureEndpoint','transaction == null','validateRequiredHeaders(request)','/actuator/','/v3/api-docs','/swagger-ui'):
   if token not in t:findings.append(f'interceptor fallback token missing: {token}')
  old='transaction == null) {\n                return true;'
  if old in t:findings.append('unannotated controller header bypass remains')
 if legacy:findings.append(f'legacy transaction identifier occurrences={len(legacy)}')
 # Zero annotations never proves route coverage; fallback must be present and measured.
 if routes and annotated==0 and not interceptor.is_file():findings.append('controller routes exist but no annotation/fallback coverage')
 result={'status':'PASS' if not findings else 'FAIL','scannedFileCount':scanned,'controllerCount':controllers,'routeMappingCount':routes,'transactionAnnotationCount':annotated,'fallbackHeaderValidation':not any('fallback' in x or 'bypass' in x for x in findings),'legacyOccurrences':legacy[:100],'findings':findings}
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
