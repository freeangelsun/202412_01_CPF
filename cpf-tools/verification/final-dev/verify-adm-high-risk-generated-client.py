#!/usr/bin/env python3
from pathlib import Path
import argparse, re, sys

FORBIDDEN=('admMutation(', 'this.sendJson(', 'this.rawResponse(')
RUNTIME_FORBIDDEN=('admApi(', 'admInvokeOperation', 'requestedBy')

def check(root: Path):
    targets={
      'transactions':root/'cpf-admin/frontend/src/features/transactions/api.ts',
      'platform':root/'cpf-admin/frontend/src/app/methods/platformMethods.ts',
      'runtimeControl':root/'cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue',
      'channelController':root/'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmChannelController.java',
      'generated':root/'cpf-admin/frontend/src/generated/orval/cpf-api.ts',
      'routes':root/'cpf-admin/frontend/src/app/routes.ts',
    }
    errs=[]
    for k,p in targets.items():
      if not p.exists(): errs.append(f'missing:{k}:{p}')
    if errs:return errs
    tx=targets['transactions'].read_text(encoding='utf-8')
    platform=targets['platform'].read_text(encoding='utf-8')
    runtime=targets['runtimeControl'].read_text(encoding='utf-8')
    gen=targets['generated'].read_text(encoding='utf-8')
    ctrl=targets['channelController'].read_text(encoding='utf-8')
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
    high_risk=(tx+'\n'+channel_section)
    if 'Record<string, unknown>' in high_risk or 'Record<string,unknown>' in high_risk: errs.append('high-risk-generic-record-body')
    required_typed=('admTransactionMetaScan','admTransactionMetaInactivate','admChannelSave','admChannelRefreshSnapshot','admChannelExportPackage','admChannelImportPackage')
    for fn in required_typed:
      if not re.search(r'export const '+re.escape(fn)+r'\s*=\s*async\s*\([^)]*:\s*[A-Z][A-Za-z0-9]*(?:Params|Request|Command|Input)',gen): errs.append('generated-operation-not-concretely-typed:'+fn)

    # CENTRAL-FINAL-030: runtime-control HIGH/CRITICAL mutations must consume
    # concrete Orval operation/request models; authenticated operator identity is
    # server-derived and must not be injected into a compatibility/raw request body.
    for raw in RUNTIME_FORBIDDEN:
      if raw in runtime: errs.append('runtime-control-raw-or-client-identity:'+raw)
    runtime_required_calls=(
      'admRuntimeControlCreateChange','admRuntimeControlCancelChange','admRuntimeControlRollbackChange',
      'admRuntimeControlSaveGroup','admRuntimeControlChangeGroupMember','admRuntimeControlDeleteGroup',
      'admRuntimeControlPreviewChange','admRuntimeControlPreviewTargets'
    )
    for fn in runtime_required_calls:
      if fn not in runtime: errs.append('runtime-control-missing-generated-call:'+fn)
    runtime_typed_markers=(
      'command(): AdmRuntimeControlCreateChangeRequest',
      'const request:AdmRuntimeControlPreviewTargetsRequest=',
      'const request:AdmRuntimeControlSaveGroupRequest=',
      'const request:AdmRuntimeControlChangeGroupMemberRequest=',
    )
    for marker in runtime_typed_markers:
      if marker not in runtime: errs.append('runtime-control-missing-typed-request:'+marker)
    for model in (
      'AdmRuntimeControlCreateChangeRequest','AdmRuntimeControlPreviewTargetsRequest',
      'AdmRuntimeControlSaveGroupRequest','AdmRuntimeControlChangeGroupMemberRequest'
    ):
      if f'import type {{ {model} }}' not in runtime: errs.append('runtime-control-missing-generated-model-import:'+model)
    generated_runtime_signatures=(
      'admRuntimeControlCreateChange = async (data: AdmRuntimeControlCreateChangeRequest',
      'admRuntimeControlCancelChange = async (changeId: string, data: AdmRuntimeControlCancelChangeRequest',
      'admRuntimeControlRollbackChange = async (changeId: string, data: AdmRuntimeControlRollbackChangeRequest',
      'admRuntimeControlSaveGroup = async (data: AdmRuntimeControlSaveGroupRequest',
      'admRuntimeControlChangeGroupMember = async (groupId: string, data: AdmRuntimeControlChangeGroupMemberRequest',
      'admRuntimeControlDeleteGroup = async (groupId: string, params: AdmRuntimeControlDeleteGroupParams',
      'admRuntimeControlPreviewTargets = async (data: AdmRuntimeControlPreviewTargetsRequest',
    )
    for marker in generated_runtime_signatures:
      if marker not in gen: errs.append('generated-runtime-operation-not-concretely-typed:'+marker.split(' = ',1)[0])
    return errs

def mutation_detected(root: Path, mutator):
    p=root/'cpf-admin/frontend/src/features/runtime-control/RuntimeControlPage.vue'
    original=p.read_text(encoding='utf-8')
    try:
      p.write_text(mutator(original),encoding='utf-8')
      return bool(check(root))
    finally:
      p.write_text(original,encoding='utf-8')

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=Path(a.root);errs=check(root)
 if errs: print('[CPF][ADM-HIGH-RISK][FAIL]',*errs,sep='\n');return 1
 if a.self_test:
   p=root/'cpf-admin/frontend/src/features/transactions/api.ts';o=p.read_text(encoding='utf-8');p.write_text(o+'\n// admMutation( mutation probe\n',encoding='utf-8');det=bool(check(root));p.write_text(o,encoding='utf-8')
   if not det: print('[CPF][ADM-HIGH-RISK][FAIL] transaction mutation not detected');return 1
   if not mutation_detected(root, lambda x: x+'\n<!-- admApi( raw mutation probe -->\n'):
     print('[CPF][ADM-HIGH-RISK][FAIL] runtime raw mutation not detected');return 1
   if not mutation_detected(root, lambda x: x.replace('const request:AdmRuntimeControlSaveGroupRequest=', 'const request:Json=',1)):
     print('[CPF][ADM-HIGH-RISK][FAIL] runtime typed-body mutation not detected');return 1
   if not mutation_detected(root, lambda x: x.replace('reason:this.form.reason, approvalId:', 'reason:this.form.reason, requestedBy:"spoofed", approvalId:',1)):
     print('[CPF][ADM-HIGH-RISK][FAIL] runtime client-identity mutation not detected');return 1
 print('[CPF][ADM-HIGH-RISK][PASS] transactions=true channelPolicy=true runtimeControlTyped=true generatedOperations=true selfTest='+str(a.self_test).lower());return 0
if __name__=='__main__':sys.exit(main())
