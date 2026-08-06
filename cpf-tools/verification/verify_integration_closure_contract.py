#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path
EXPECTED={
'admIntegrationCryptoStatus','admIntegrationTimeHealth','admIntegrationDataQualityValidate',
'admIntegrationDataQualityCorrectionApprovalRequest','admIntegrationDataQualityCorrectionExecute',
'admIntegrationDataQualityReplay','admIntegrationWebhookDlq','admIntegrationWebhookReplay'}
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();r=Path(a.root);errors=[]
 spec=json.loads((r/'cpf-tools/contracts/openapi/cpf-integration-closure.openapi.json').read_text(encoding='utf-8'))
 ops={o['operationId'] for p in spec['paths'].values() for o in p.values() if isinstance(o,dict) and 'operationId'in o}
 if ops!=EXPECTED:errors.append(f'operation set mismatch missing={EXPECTED-ops} extra={ops-EXPECTED}')
 controller=(r/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java').read_text(encoding='utf-8')
 routes=(r/'cpf-admin/frontend/src/app/routes.ts').read_text(encoding='utf-8')
 client=(r/'cpf-admin/frontend/src/generated/integrationClosureApi.ts').read_text(encoding='utf-8')
 for op in EXPECTED:
  if op not in controller:errors.append(f'controller missing {op}')
  if op not in routes:errors.append(f'route contract missing {op}')
 if re.search(r'\\bapproved\\s*[?:=)]',client,re.I) or 'approved=' in client:errors.append('client boolean approval bypass')
 if re.search(r'/correct(?:[?\"\'/]|$)',client):errors.append('legacy direct correction endpoint remains')
 if not {'requestCorrectionApproval','executeCorrectionApproval'}<=set(re.findall(r'\b(\w+):',client)):errors.append('approval client methods missing')
 if errors:
  print('\\n'.join('FAIL '+x for x in errors));return 1
 print('PASS operations=8 serverApprovalOnly=true actualRouteConsumer=true');return 0
if __name__=='__main__':raise SystemExit(main())
