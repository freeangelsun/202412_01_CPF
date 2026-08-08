#!/usr/bin/env python3
from pathlib import Path
import argparse, re, sys

FORBIDDEN=('admMutation(', 'this.sendJson(', 'this.rawResponse(')

def check(root: Path):
    targets={
      'transactions':root/'cpf-admin/frontend/src/features/transactions/api.ts',
      'platform':root/'cpf-admin/frontend/src/app/methods/platformMethods.ts',
      'channelController':root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmChannelController.java',
      'generated':root/'cpf-admin/frontend/src/generated/orval/cpf-api.ts',
      'routes':root/'cpf-admin/frontend/src/app/routes.ts',
    }
    errs=[]
    for k,p in targets.items():
      if not p.exists(): errs.append(f'missing:{k}:{p}')
    if errs:return errs
    tx=targets['transactions'].read_text(); platform=targets['platform'].read_text(); gen=targets['generated'].read_text(); ctrl=targets['channelController'].read_text()
    if 'admMutation' in tx: errs.append('transactions-raw-mutation')
    for fn in ('admTransactionMetaScan','admTransactionMetaInactivate'):
      if fn not in tx: errs.append('transactions-missing:'+fn)
    channel_section=platform.split('async loadServiceRegistry()',1)[0]
    for raw in FORBIDDEN:
      if raw in channel_section: errs.append('channel-raw:'+raw)
    for fn in ('admChannelFindSnapshot','admChannelSave','admChannelRefreshSnapshot','admChannelExportPackage','admChannelImportPackage'):
      if fn not in channel_section: errs.append('channel-missing:'+fn)
    if '@org.springframework.web.bind.annotation.RequestParam' not in ctrl or 'String requestUser,' in ctrl.split('public ResponseEntity<CpfChannelPolicySnapshot> refresh',1)[1].split('}',1)[0]: errs.append('channel-refresh-openapi')
    if 'admChannelRefreshSnapshot = async (params: AdmChannelRefreshSnapshotParams' not in gen or 'params,' not in gen.split('admChannelRefreshSnapshot = async',1)[1].split('};',1)[0]: errs.append('generated-refresh-untyped')
    return errs

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=Path(a.root);errs=check(root)
 if errs: print('[CPF][ADM-HIGH-RISK][FAIL]',*errs,sep='\
');return 1
 if a.self_test:
   p=root/'cpf-admin/frontend/src/features/transactions/api.ts';o=p.read_text();p.write_text(o+'\
// admMutation( mutation probe\
');det=bool(check(root));p.write_text(o)
   if not det: print('[CPF][ADM-HIGH-RISK][FAIL] mutation not detected');return 1
 print('[CPF][ADM-HIGH-RISK][PASS] transactions=true channelPolicy=true generatedRefresh=true selfTest='+str(a.self_test).lower());return 0
if __name__=='__main__':sys.exit(main())
