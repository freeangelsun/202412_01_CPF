#!/usr/bin/env python3
# CPF ADM/BZA/Gateway management-boundary verifier.
from __future__ import annotations
import argparse,json,re
from pathlib import Path

MODULES={
 'ADM':'cpf-admin/src/main/java',
 'BZA':'cpf-biz-admin/src/main/java',
 'GATEWAY':'cpf-gateway/src/main/java',
}

def java_files(root:Path,rel:str):
 p=root/rel
 return [] if not p.exists() else [(f,f.read_text(encoding='utf-8',errors='ignore')) for f in p.rglob('*.java')]

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-out');a=ap.parse_args();root=Path(a.root).resolve();fail=[];metrics={}
 for label,rel in MODULES.items():
  rows=java_files(root,rel)
  if not rows: fail.append(f'{label} source missing'); continue
  controllers=[(p,t) for p,t in rows if p.stem.endswith('Controller')]
  wrong_tx=[str(p.relative_to(root)) for p,t in controllers if re.search(r'(?m)^\s*@CpfOnlineTransaction(?:\s*\(|\s*$)',t)]
  wrong_tx_headers=[str(p.relative_to(root)) for p,t in controllers if all(x in t for x in ['X-Transaction-Id','X-Caller-System-Code','X-Target-Operation-Id'])]
  direct_internal=[str(p.relative_to(root)) for p,t in rows if re.search(r'import\s+com\.cpf\.core\.(?:internal|impl)\.',t)]
  cross_internal=[str(p.relative_to(root)) for p,t in rows if label=='BZA' and re.search(r'import\s+com\.cpf\.(?:member|external)\..*\.internal\.',t)]
  web_controllers=[str(p.relative_to(root)) for p,t in controllers if '@RestController' in t or '@Controller' in t]
  metrics[label]={
   'controllerCount':len(controllers),'springWebControllerCount':len(web_controllers),
   'businessTransactionAnnotationOnManagementController':len(wrong_tx),
   'businessSixHeaderContractOnManagementController':len(wrong_tx_headers),
   'directCpfCoreInternalImports':len(direct_internal),'businessDomainInternalImports':len(cross_internal),
  }
  if wrong_tx: fail.append(f'{label} management controller must not be @CpfOnlineTransaction: {wrong_tx[:12]}')
  if wrong_tx_headers: fail.append(f'{label} management controller forces business 6-header contract: {wrong_tx_headers[:12]}')
  if direct_internal: fail.append(f'{label} direct cpf-core internal coupling: {direct_internal[:12]}')
  if cross_internal: fail.append(f'{label} direct business-domain internal coupling: {cross_internal[:12]}')
  if controllers and not web_controllers: fail.append(f'{label} management controllers are not ordinary Spring Web controllers')
 result={'status':'PASS' if not fail else 'FAIL','failures':fail,'metrics':metrics,
         'boundary':'ADM/BZA/Gateway management API is not business Domain Online Transaction; outbound Domain Client boundary owns business transaction context.'}
 print('CPF_NXT3_ADM_BZA_FRAMEWORK_GATE='+result['status']);print(json.dumps(result,ensure_ascii=False,indent=2))
 if a.json_out: Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 raise SystemExit(0 if not fail else 1)
if __name__=='__main__':main()
