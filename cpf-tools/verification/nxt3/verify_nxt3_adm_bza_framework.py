#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse,json,re
from pathlib import Path
MODULES={
 'ADM':('cpf-admin/src/main/java','AdmBaseController','AdmBaseService','AdmBaseRepository'),
 'BZA':('cpf-biz-admin/src/main/java','BzaBaseController','BzaBaseService','BzaBaseRepository'),
}

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-out');a=ap.parse_args();root=Path(a.root).resolve();fail=[];metrics={}
 for label,(rel,bc,bs,bd) in MODULES.items():
  src=root/rel
  texts=[]
  if not src.exists():fail.append(f'{label} source missing');continue
  for p in src.rglob('*.java'):
   try:texts.append((p,p.read_text(encoding='utf-8')))
   except UnicodeDecodeError:pass
  alltext='\n'.join(t for _,t in texts)
  base={
   'controller': f'abstract class {bc} extends CpfBaseController' in alltext,
   'service': f'abstract class {bs} extends CpfBaseService' in alltext and 'operationCode(' in alltext,
   'repository': f'abstract class {bd} extends CpfBaseRepository' in alltext and 'operationPageSize(' in alltext,
  }
  for k,v in base.items():
   if not v:fail.append(f'{label} Domain Base missing/non-functional: {k}')
  consumers={'CpfController':0,'CpfService':0,'CpfRepository':0,'CpfOnlineTransaction':alltext.count('@CpfOnlineTransaction')}
  bypass=[]
  for p,t in texts:
   n=p.stem
   if n.endswith('Controller') and n!=bc and '@RestController' in t:bypass.append(str(p.relative_to(root)))
   if n.endswith('Service') and n!=bs and re.search(r'@Service\b',t):bypass.append(str(p.relative_to(root)))
   if n.endswith('Repository') and n!=bd and re.search(r'@Repository\b',t):bypass.append(str(p.relative_to(root)))
   consumers['CpfController']+=t.count('@CpfController');consumers['CpfService']+=t.count('@CpfService');consumers['CpfRepository']+=t.count('@CpfRepository')
  metrics[label]={'base':base,'consumers':consumers,'springStereotypeBypass':len(bypass)}
  if bypass:fail.append(f'{label} concrete Spring stereotype bypass count={len(bypass)} examples={bypass[:8]}')
  for k in ('CpfController','CpfService','CpfRepository'):
   if consumers[k]<=0:fail.append(f'{label} actual @{k} consumer missing')
  if consumers['CpfOnlineTransaction']<=0:fail.append(f'{label} @CpfOnlineTransaction consumer missing')
 # control-plane separation evidence must be visible in ADM source/config by management semantics.
 adm='\n'.join(p.read_text(encoding='utf-8') for p in (root/'cpf-admin/src/main/java').rglob('*.java')) if (root/'cpf-admin/src/main/java').exists() else ''
 cp={k:(k in adm) for k in ['AdmControlPlane','maintenance','audit','transactionId']}
 metrics['controlPlaneSemantics']=cp
 for k,v in cp.items():
  if not v:fail.append('ADM control-plane semantic missing: '+k)
 result={'status':'PASS' if not fail else 'FAIL','failures':fail,'metrics':metrics,'runtimeFailureDomainVerification':'UNVERIFIED_EXTERNAL_RUNTIME'}
 print('CPF_NXT3_ADM_BZA_FRAMEWORK_GATE='+result['status']);print(json.dumps(result,ensure_ascii=False,indent=2))
 if a.json_out:Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 raise SystemExit(0 if not fail else 1)
if __name__=='__main__':main()
