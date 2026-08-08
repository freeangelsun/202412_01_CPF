#!/usr/bin/env python3
from pathlib import Path
import argparse,re,sys

def check(root:Path):
 f={
 'dto':root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java',
 'plan':root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeDeploymentPlanRequest.java',
 'ctrl':root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java',
 'client':root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java',
 'svc':root/'cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java',
 'owner':root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
 'api':root/'cpf-admin/frontend/src/features/batch-runtime-control/api.ts',
 'wb':root/'cpf-admin/frontend/src/features/batch-runtime-control/BatchOperationsWorkbench.vue',
 'dp':root/'cpf-admin/frontend/src/features/batch-deployment/DeploymentPlanCreatePanel.vue',
 'gen':root/'cpf-admin/frontend/src/generated/cpf-api.ts'}
 e=[]
 for k,p in f.items():
  if not p.exists():e.append('missing:'+k)
 if e:return e
 t={k:p.read_text() for k,p in f.items()}
 if 'approvedBy' in t['dto'] or 'requestedBy' in t['dto']:e.append('browser-actor-field')
 if 'approvalRequestId' not in t['dto'] or 'reason' not in t['dto']:e.append('approval-exec-dto')
 if 'approvalService.execute(' not in t['ctrl']:e.append('controller-bypasses-approval-engine')
 if 'requireCommandField(request, "approvedBy")' in t['ctrl']:e.append('controller-trusts-approvedBy')
 for n in ['CpfHeaders.approvalRequestId()','CpfHeaders.approvalRequesterId()']:
  if t['client'].count(n)<2:e.append('missing-approved-header:'+n)
 if 'case "runtimeCommand" -> json(payload);' not in t['svc']:e.append('snapshot-payload-not-preserved')
 for n in ['tuple("runtimeCommand", "BATCH_RUNTIME_RESTART", "bat_runtime")','executeRuntimeCommand(command, risk)','observeRuntimeCommand(command)','runtimeClient.commandApproved']:
  if n not in t['owner']:e.append('owner:'+n)
 if 'request.put("approvedBy", command.approvedBy())' not in t['owner'] or 'request.put("requestedBy", command.requestedBy())' not in t['owner']:e.append('server-actor-rebuild')
 if 'submitBatchRuntimeCommand({approvalRequestId:command.approvalId,reason:command.reason})' not in t['wb']:e.append('runtime-ui-consumer')
 if 'admBatchRuntimeCommand({ data: command })' not in t['api']:e.append('runtime-generated-consumer')
 if 'manifest: Record<string, unknown>' not in t['gen'] or 'approvalRequestId: string; reason: string' not in t['gen']:e.append('generated-body-contract')
 if 'createDeploymentPlan({planId:' not in t['dp'] or 'manifest' not in t['plan']:e.append('deployment-manifest-consumer')
 return e

def main():
 a=argparse.ArgumentParser();a.add_argument('--root',required=True);a.add_argument('--self-test',action='store_true');x=a.parse_args();r=Path(x.root)
 e=check(r)
 if e:print('[CPF][ADM-BATCH-RUNTIME][FAIL]',*e,sep='\n');return 1
 if x.self_test:
  p=r/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java';o=p.read_text();p.write_text(o.replace('.header(CpfHeaders.approvalRequesterId(), approval.approvalRequesterId());',';',1));d=bool(check(r));p.write_text(o)
  if not d:print('[CPF][ADM-BATCH-RUNTIME][FAIL] mutation-not-detected');return 1
 print('[CPF][ADM-BATCH-RUNTIME][PASS] approvalEngine=true signedApprovalContext=true runtimeConsumer=true deploymentManifest=true selfTest='+str(x.self_test).lower());return 0
if __name__=='__main__':sys.exit(main())
