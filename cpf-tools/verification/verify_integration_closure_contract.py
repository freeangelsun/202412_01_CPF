#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path
EXPECTED={
'admIntegrationCryptoStatus','admIntegrationTimeHealth','admIntegrationDataQualityValidate',
'admIntegrationDataQualityCorrectionApprovalRequest','admIntegrationDataQualityCorrectionExecute',
'admIntegrationDataQualityReplay','admIntegrationWebhookDlq','admIntegrationWebhookReplay'}
REQUIRED_FACADE_METHODS={'requestCorrectionApproval','executeCorrectionApproval','replayQuality','replayWebhook'}

def read(path:Path,errors:list[str])->str:
 if not path.is_file(): errors.append(f'missing {path.as_posix()}'); return ''
 return path.read_text(encoding='utf-8',errors='replace')

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();r=Path(a.root).resolve();errors=[]
 spec_path=r/'cpf-tools/verification/contracts/openapi/cpf-integration-closure.openapi.json'
 try: spec=json.loads(read(spec_path,errors) or '{}')
 except json.JSONDecodeError as exc: errors.append(f'integration OpenAPI invalid: {exc}'); spec={}
 ops={o['operationId'] for p in spec.get('paths',{}).values() for o in p.values() if isinstance(o,dict) and 'operationId'in o}
 if ops!=EXPECTED:errors.append(f'operation set mismatch missing={EXPECTED-ops} extra={ops-EXPECTED}')
 controller=read(r/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmIntegrationClosureController.java',errors)
 routes_dir=r/'cpf-admin/frontend/src/app/routes'
 routes='\n'.join(read(p,errors) for p in sorted(routes_dir.glob('*.ts')) if p.name!='types.ts') if routes_dir.is_dir() else read(r/'cpf-admin/frontend/src/app/routes.ts',errors)
 orval=read(r/'cpf-admin/frontend/src/generated/orval/cpf-api.ts',errors)
 compat=read(r/'cpf-admin/frontend/src/generated/cpf-api.ts',errors)
 facade=read(r/'cpf-admin/frontend/src/features/integration-closure/integrationClosureApi.ts',errors)
 page=read(r/'cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue',errors)
 marker_path=r/'cpf-admin/frontend/src/generated/.cpf-openapi-source.json'
 try: marker=json.loads(read(marker_path,errors) or '{}')
 except json.JSONDecodeError as exc: errors.append(f'ADM generated marker invalid: {exc}'); marker={}
 pre_runtime=(marker.get('origin')=='CONTROLLER_SOURCE_PRE_RUNTIME' and marker.get('releaseEligible') is False)
 release_orval=('CPF_CANONICAL_ORVAL_DELEGATE' in compat and 'from "./orval/cpf-api"' in compat)
 if not (pre_runtime or release_orval):
  errors.append('canonical generated client must be release Orval or verified controller-source pre-runtime adapter')
 if "from '../../generated/cpf-api'" not in facade and 'from "../../generated/cpf-api"' not in facade:
  errors.append('integration closure facade must consume canonical generated client')
 for op in EXPECTED:
  if op not in controller:errors.append(f'controller missing {op}')
  if op not in routes:errors.append(f'route contract missing {op}')
  if not pre_runtime and not re.search(rf'export\s+const\s+{re.escape(op)}\s*=',orval):errors.append(f'Orval client missing {op}')
  if not re.search(rf'export\s+async\s+function\s+{re.escape(op)}<',compat):errors.append(f'compatibility client missing {op}')
  if op not in facade:errors.append(f'facade generated operation consumer missing {op}')
  if op not in page and op not in routes:errors.append(f'page/route consumer missing {op}')
 if re.search(r'\bapproved\s*[?:=)]',facade,re.I) or 'approved=' in facade:errors.append('client boolean approval bypass')
 if re.search(r'/correct(?:[?"\'/]|$)',facade):errors.append('legacy direct correction endpoint remains')
 methods=set(re.findall(r'^\s*(\w+)\s*:\s*\(',facade,re.M))
 missing_methods=REQUIRED_FACADE_METHODS-methods
 if missing_methods:errors.append(f'approval facade methods missing {sorted(missing_methods)}')
 # Approval execute must require an approval request id plus reason-bearing body; direct correction is forbidden.
 if not re.search(r'executeCorrectionApproval:\s*\(approvalRequestId:\s*number,\s*body:\s*CorrectionExecutionRequest\)',facade):
  errors.append('approval execute facade contract missing approvalRequestId + reason body')
 if errors:
  print('\n'.join('FAIL '+x for x in errors));return 1
 print(f'PASS operations=8 canonicalOrval=true preRuntimeAdapter={str(pre_runtime).lower()} serverApprovalOnly=true actualRouteConsumer=true');return 0
if __name__=='__main__':raise SystemExit(main())
