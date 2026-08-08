#!/usr/bin/env python3
from pathlib import Path
import argparse, sys

def check(root: Path):
    required={
      'port': root/'cpf-core/src/main/java/com/cpf/core/api/reliability/CpfReliabilityOperationsPort.java',
      'facade': root/'cpf-core/src/main/java/com/cpf/core/common/reliability/CpfReliabilityOperationsFacade.java',
      'dto': root/'cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmReliabilityActionRequest.java',
      'controller': root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmReliabilityController.java',
      'service': root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmReliabilityService.java',
      'api': root/'cpf-admin/frontend/src/features/operations/api.ts',
      'page': root/'cpf-admin/frontend/src/features/operations/ErrorWorkbenchPage.vue',
      'model': root/'cpf-admin/frontend/src/generated/orval/model/admReliabilityActionRequest.ts',
    }
    miss=[str(p) for p in required.values() if not p.exists()]
    if miss: return [f'missing:{x}' for x in miss]
    t={k:p.read_text(encoding='utf-8') for k,p in required.items()}
    errs=[]
    for needle in ['row_version,','AND row_version = ?','row_version = row_version + 1','expectedVersion']:
        if needle not in t['facade']: errs.append('facade:'+needle)
    if 'request.requestUser()' in t['controller']: errs.append('client-requestUser-trusted')
    if 'request.expectedVersion()' not in t['controller']: errs.append('controller-no-version')
    if 'expectedVersion' not in t['dto'] or 'requestUser' in t['dto'].split('record AdmReliabilityActionRequest',1)[-1]: errs.append('dto-contract')
    if '{ targetStatus, reason, expectedVersion }' not in t['api']: errs.append('frontend-no-cas')
    if 'as Parameters<typeof resolveAdmUnknownResult>' in t['api']: errs.append('unsafe-reliability-cast')
    for bad in ['<option>RECONCILE</option>','<option>REPLAY</option>','<option>COMPENSATE</option>','<option>MARK_FAILED</option>']:
        if bad in t['page']: errs.append('invalid-status:'+bad)
    if "'rowVersion','row_version'" not in t['page']: errs.append('page-no-row-version')
    if 'expectedVersion?: number' not in t['model']: errs.append('generated-model-no-version')
    return errs

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=Path(a.root)
    errs=check(root)
    if errs:
        print('[CPF][ADM-RELIABILITY-CAS][FAIL]',*errs,sep='\
');return 1
    if a.self_test:
        facade=root/'cpf-core/src/main/java/com/cpf/core/common/reliability/CpfReliabilityOperationsFacade.java'
        original=facade.read_text();facade.write_text(original.replace('AND row_version = ?', 'AND 1 = 1',1))
        detected=bool(check(root));facade.write_text(original)
        if not detected:
            print('[CPF][ADM-RELIABILITY-CAS][FAIL] mutation not detected');return 1
    print('[CPF][ADM-RELIABILITY-CAS][PASS] cas=row_version generatedClient=true serverIdentity=true selfTest='+str(a.self_test).lower());return 0
if __name__=='__main__': sys.exit(main())
