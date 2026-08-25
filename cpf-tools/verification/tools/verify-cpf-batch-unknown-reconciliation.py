#!/usr/bin/env python3
"""DB3 fail-closed gate for Batch UNKNOWN/reconciliation runtime SQL and current vendor-pack contract."""
from pathlib import Path
import argparse,json,re,sys
ap=argparse.ArgumentParser()
ap.add_argument('positional_root',nargs='?',default=None)
ap.add_argument('--root',dest='root_opt',default=None)
ns=ap.parse_args()
root=Path(ns.root_opt or ns.positional_root or '.').resolve()
vendors=('oracle','postgresql','mariadb')
required=('scheduler-trigger-find-dispatchable.sql','scheduler-trigger-claim.sql','centercut-item-complete.sql','execution-remote-message-reclaim.sql','scheduler-trigger-reconcile-load.sql','scheduler-trigger-reconcile-unknown-retry.sql','scheduler-trigger-reconcile-audit.sql','scheduler-trigger-reconcile-audit-find.sql','execution-remote-message-reconcile-load.sql','execution-remote-message-reconcile-unknown-retry.sql','execution-remote-message-reconcile-audit.sql')
errors=[]; fingerprints={}
# SQL 존재만으로 PASS하지 않고 실제 Runtime Consumer와 fail-closed 계약을 같이 확인한다.
scheduler_controller=root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/SchedulerTriggerReconciliationController.java'
remote_controller=root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/RemoteMessageReconciliationController.java'
for path, required_tokens in (
    (scheduler_controller, ('actorResolver.approved', 'scheduler-trigger-reconcile-load', 'scheduler-trigger-reconcile-unknown-retry', 'scheduler-trigger-reconcile-audit', 'scheduler-trigger-reconcile-audit-find', '@Transactional')),
    (remote_controller, ('actorResolver.approved', 'execution-remote-message-reconcile-load', 'execution-remote-message-reconcile-unknown-retry', 'execution-remote-message-reconcile-audit', '@Transactional'))):
    if not path.is_file():
        errors.append(f'consumer:missing:{path.relative_to(root).as_posix()}')
        continue
    source=path.read_text(encoding='utf-8')
    for token in required_tokens:
        if token not in source: errors.append(f'consumer:{path.name}:missing:{token}')
# Scheduler UNKNOWN은 BAT 내부 API 존재만으로 완료가 아니다. 정식 ADM Approval Owner가 실제 실행/재조회 Consumer여야 한다.
adm_owner=root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java'
adm_client=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java'
for path, required_tokens in (
    (adm_owner, ('tuple("reconcileSchedulerTrigger", "BATCH_SCHEDULER_RECONCILE_UNKNOWN", "bat_schedule_trigger")',
                 'executeSchedulerTriggerReconcile', 'observeSchedulerTrigger',
                 'schedulerTriggerReconcileApproved', 'schedulerTriggerState')),
    (adm_client, ('schedulerTriggerReconcileApproved', 'schedulerTriggerState',
                  '/triggers/reconcile-unknown/retry', '/triggers?scheduledFireAt='))):
    if not path.is_file():
        errors.append(f'adm-consumer:missing:{path.relative_to(root).as_posix()}')
        continue
    source=path.read_text(encoding='utf-8')
    for token in required_tokens:
        if token not in source: errors.append(f'adm-consumer:{path.name}:missing:{token}')

metadata_path=root/'cpf-tools/db/metadata/bat-runtime-query-contract.json'
if not metadata_path.is_file():
    errors.append('metadata:missing:bat-runtime-query-contract.json')
else:
    metadata_text=metadata_path.read_text(encoding='utf-8')
    for key in ('scheduler-trigger-reconcile-load','scheduler-trigger-reconcile-unknown-retry','scheduler-trigger-reconcile-audit','scheduler-trigger-reconcile-audit-find'):
        if key not in metadata_text or 'SchedulerTriggerReconciliationController' not in metadata_text:
            errors.append(f'metadata:consumer-drift:{key}')
for v in vendors:
 base=root/f'cpf-tools/db/vendor/{v}/runtime/bat/repository'
 texts={n:(base/n).read_text(encoding='utf-8') if (base/n).is_file() else '' for n in required}
 for n,t in texts.items():
  if not t: errors.append(f'{v}:missing:{n}')
 for n in ('scheduler-trigger-find-dispatchable.sql','scheduler-trigger-claim.sql'):
  if re.search(r"trigger_status\s+IN\s*\([^)]*'UNKNOWN'",texts[n],re.I|re.S): errors.append(f'{v}:UNKNOWN_AUTO_DISPATCH:{n}')
 if "item_status = 'RUNNING'" not in texts['centercut-item-complete.sql']: errors.append(f'{v}:CENTER_CUT_NO_RUNNING_CAS')
 if "status_cd IN ('PROCESSING','FAILED')" not in texts['execution-remote-message-reclaim.sql'].replace('  ',' '): errors.append(f'{v}:REMOTE_RECLAIM_NOT_FAIL_CLOSED')
 if not all(token in texts['scheduler-trigger-reconcile-unknown-retry.sql'] for token in ("trigger_status = 'UNKNOWN'",'idempotency_key = ?','attempt_count = ?')): errors.append(f'{v}:SCHEDULER_RECONCILE_CAS_INCOMPLETE')
 if not all(token in texts['execution-remote-message-reconcile-unknown-retry.sql'] for token in ("status_cd = 'UNKNOWN'",'attempt_no = ?','version_no = ?')): errors.append(f'{v}:REMOTE_RECONCILE_CAS_INCOMPLETE')
 audit_sql = texts['scheduler-trigger-reconcile-audit.sql'].lower()
 if not all(token.lower() in audit_sql for token in ('bat_reconciliation_audit', "'SCHEDULER_TRIGGER'", 'idempotency_key')): errors.append(f'{v}:SCHEDULER_RECONCILE_AUDIT_INCOMPLETE')
 audit_find_sql = texts['scheduler-trigger-reconcile-audit-find.sql'].lower()
 if not all(token.lower() in audit_find_sql for token in ('bat_reconciliation_audit', "entity_type = 'SCHEDULER_TRIGGER'", 'idempotency_key = ?')): errors.append(f'{v}:SCHEDULER_RECONCILE_IDEMPOTENCY_LOOKUP_INCOMPLETE')
 required_pack_paths=('source/18_batch_unknown_reconciliation.sql','install/07_batch_unknown_reconciliation.sql','migration/V103__batch_unknown_reconciliation.sql','rollback/R103__batch_unknown_reconciliation.sql','verify/103_verify_batch_unknown_reconciliation.sql')
 for rel in required_pack_paths:
  if not (root/f'cpf-tools/db/vendor/{v}'/rel).is_file(): errors.append(f'{v}:missing:{rel}')
 pack_path=root/f'cpf-tools/db/vendor/{v}/pack.json'
 if not pack_path.is_file(): errors.append(f'{v}:missing:pack.json')
 else:
  pack=json.loads(pack_path.read_text(encoding='utf-8'))
  # schemaVersion 5+ intentionally removed feature-specific table counters. Verify canonical lifecycle roots instead.
  for key in ('canonicalSchema','generatedCurrentRoot','historicalMigrationRoot','runtimeRoot'):
   value=pack.get(key)
   if not isinstance(value,str) or not value.strip(): errors.append(f'{v}:pack:{key}')
  if str(pack.get('schemaVersion','')) < '5': errors.append(f'{v}:pack:schemaVersion')
 fingerprints[v]=tuple(sorted(required))
semantic=len(set(fingerprints.values()))==1
result={'vendors':list(vendors),'requiredRuntimeKeys':list(required),'errors':errors,'semanticParity':semantic,'pass':not errors and semantic}
print(json.dumps(result,ensure_ascii=False,indent=2));sys.exit(0 if result['pass'] else 1)
