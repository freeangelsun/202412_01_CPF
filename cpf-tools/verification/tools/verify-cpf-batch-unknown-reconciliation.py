#!/usr/bin/env python3
"""DB3 fail-closed gate for Kafka-free Batch UNKNOWN/reconciliation contracts."""
from pathlib import Path
import argparse,json,sys
VENDORS=('oracle','postgresql','mariadb')
REQUIRED=(
 'scheduler-trigger-find-dispatchable.sql','scheduler-trigger-claim.sql',
 'scheduler-trigger-reconcile-load.sql','scheduler-trigger-reconcile-unknown-retry.sql',
 'scheduler-trigger-reconcile-audit.sql','scheduler-trigger-reconcile-audit-find.sql',
 'centercut-item-complete.sql','centercut-item-mark-unknown.sql',
 'centercut-claim-find-expired-running.sql','centercut-item-reconcile-unknown.sql',
)

def main():
 ap=argparse.ArgumentParser();ap.add_argument('positional_root',nargs='?');ap.add_argument('--root',dest='root_opt');a=ap.parse_args();root=Path(a.root_opt or a.positional_root or '.').resolve();errors=[]
 # Actual consumers: scheduler and Center-Cut only.
 consumers=(
  (root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/SchedulerTriggerReconciliationController.java',('actorResolver.approved','scheduler-trigger-reconcile-load','scheduler-trigger-reconcile-unknown-retry','scheduler-trigger-reconcile-audit','@Transactional')),
  (root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/CenterCutReconciliationController.java',(
   '@PostMapping("/executions/{executionId}/reconcile-unknown")',
   '@PostMapping("/executions/{executionId}/reprocess-failed")',
   'centercut-reconcile-unknown-items','centercut-reconcile-unknown-execution',
   'centercut-reconcile-failed-items','centercut-reconcile-failed-execution',
   'centercut-reconcile-audit','actorResolver.approved','@Transactional')),
  (root/'cpf-batch/center-cut-runtime/src/main/java/com/cpf/batch/centercut/runtime/JdbcCenterCutClaimRepository.java',('recoverExpiredToUnknown','reconcileUnknown')),
 )
 for p,tokens in consumers:
  if not p.is_file(): errors.append(f'consumer:missing:{p.relative_to(root).as_posix()}');continue
  text=p.read_text(encoding='utf-8')
  for token in tokens:
   if token not in text:errors.append(f'consumer:{p.name}:missing:{token}')
 # ADM approval owner must remain the scheduler dangerous-operation consumer.
 for p,tokens in (
  (root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',('reconcileSchedulerTrigger','executeSchedulerTriggerReconcile','observeSchedulerTrigger')),
  (root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java',('schedulerTriggerReconcileApproved','schedulerTriggerState')),
 ):
  if not p.is_file():errors.append(f'adm-consumer:missing:{p.relative_to(root).as_posix()}');continue
  text=p.read_text(encoding='utf-8')
  for token in tokens:
   if token not in text:errors.append(f'adm-consumer:{p.name}:missing:{token}')
 # DB3 query packs must contain the same non-remote semantic keys and none of the retired ledger queries.
 for v in VENDORS:
  pack=root/f'cpf-tools/db/vendor/{v}/runtime/bat/repository'
  names={p.name for p in pack.glob('*.sql')}
  for name in REQUIRED:
   if name not in names:errors.append(f'{v}:missing:{name}')
  for name in names:
   if name.startswith('execution-remote-message-'):errors.append(f'{v}:retired-query:{name}')
 # Metadata cannot retain dead consumers/queries.
 meta=root/'cpf-tools/db/metadata/bat-runtime-query-contract.json'
 text=meta.read_text(encoding='utf-8') if meta.is_file() else ''
 for token in ('execution-remote-message-','RemoteMessageReconciliationController','JdbcCpfBatchRemoteMessageLedger'):
  if token in text:errors.append(f'metadata:retired:{token}')
 result={'vendors':list(VENDORS),'requiredRuntimeFiles':list(REQUIRED),'errors':errors,'pass':not errors}
 print(json.dumps(result,ensure_ascii=False,indent=2));return 0 if not errors else 1
if __name__=='__main__':raise SystemExit(main())
