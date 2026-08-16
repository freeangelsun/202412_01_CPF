import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()
const controller = fs.readFileSync(path.resolve(root, '../src/main/java/com/cpf/admin/opr/controller/AdmCenterCutController.java'), 'utf8')
const service = fs.readFileSync(path.resolve(root, '../src/main/java/com/cpf/admin/opr/service/AdmCenterCutOperationService.java'), 'utf8')
const owner = fs.readFileSync(path.resolve(root, '../src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java'), 'utf8')
const approval = fs.readFileSync(path.resolve(root, '../src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java'), 'utf8')
const api = fs.readFileSync(path.resolve(root, 'src/features/batch-runtime-control/api.ts'), 'utf8')
const page = fs.readFileSync(path.resolve(root, 'src/features/batch-center-cut/BatchCenterCutPage.vue'), 'utf8')

const required = [
  ['/executions/{executionId}/reprocess-failed', controller],
  ['/executions/{executionId}/reconcile-unknown', controller],
  ['admCenterCutReprocessFailedExecution', controller],
  ['admCenterCutReconcileUnknownExecution', controller],
  ['auditLogService.executeAudited(', controller],
  ['parseApprovalRequestId(', controller],
  ['approvalService.detail(approvalRequestId)', service],
  ['approvalService.execute(approvalRequestId', service],
  ['requireMatch(approval, "requestKey", safeRequestKey)', service],
  ['requireMatch(approval, "ownerCommand", ownerCommand)', service],
  ['requireMatchIgnoreCase(approval, "targetType", "center_cut_execution")', service],
  ['implements AdmApprovalOwnerCommandPort', owner],
  ['owner.reprocessFailed(risk.targetId(), risk)', owner],
  ['owner.reconcileUnknown(risk.targetId(), risk)', owner],
  ['risk.fingerprint().equalsIgnoreCase(command.payloadHash())', owner],
  ['repository.reserveExecution(', approval],
  ['repository.finishExecutionAndRequest(', approval],
  ['admApprovalRequest<Record<string, unknown>>({ data:', api],
  ['admApprovalRequestDetail<Record<string, unknown>>({ path:', api],
  ['admCenterCutReprocessFailedExecution({', api],
  ['admCenterCutReconcileUnknownExecution({', api],
  ['createCenterCutApproval(', page],
  ['executeCenterCutAction(pending.value.ticket, command)', page],
  ['pending.value = { ...pending.value, ticket }', page],
  [':approval-required="Boolean(pending?.ticket)"', page],
  ["resultStatus(row) === 'FAILED'", page],
  ["resultStatus(row) === 'UNKNOWN'", page],
]
for (const [token, source] of required) {
  if (!source.includes(token)) throw new Error(`Center-Cut execution contract missing: ${token}`)
}
for (const token of [
  'AdmBatchApprovalService', 'approvals.reserve(', 'commandClient.reprocessFailed(',
  'commandClient.reconcileUnknown(', 'finishExecutionAndRequest(', 'reserveExecution(']) {
  if (service.includes(token)) throw new Error(`Center-Cut service must not own a second execution engine: ${token}`)
}
for (const token of ['/jobs/{jobId}/reprocess-failed','/jobs/{jobId}/reconcile-unknown','/reprocess-all','/reconcile-all','blind retry']) {
  if (api.includes(token) || page.includes(token) || controller.includes(token)) {
    throw new Error(`Job-scope or blind Center-Cut mutation is forbidden: ${token}`)
  }
}
if (api.includes("request(`/adm/api/center-cut/executions/") || api.includes('admInvokeOperation')) {
  throw new Error('Center-Cut mutation/approval must use typed generated operation consumers, not raw/generic request')
}
console.log('PASS center-cut canonical-approval/generated-client/execution-scope contract')
