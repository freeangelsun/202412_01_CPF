from pathlib import Path
import json,re,sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.')
vendors=('oracle','postgresql','mariadb')
required=('scheduler-trigger-find-dispatchable.sql','scheduler-trigger-claim.sql','centercut-item-complete.sql','execution-remote-message-reclaim.sql','scheduler-trigger-reconcile-load.sql','scheduler-trigger-unknown-retry.sql','scheduler-trigger-reconcile-audit.sql','execution-remote-message-reconcile-load.sql','execution-remote-message-unknown-retry.sql','execution-remote-message-reconcile-audit.sql')
errors=[]; fingerprints={}
for v in vendors:
 base=root/f'cpf-tools/db/vendor/{v}/runtime/bat/repository'
 texts={n:(base/n).read_text(encoding='utf-8') if (base/n).is_file() else '' for n in required}
 for n,t in texts.items():
  if not t: errors.append(f'{v}:missing:{n}')
 for n in ('scheduler-trigger-find-dispatchable.sql','scheduler-trigger-claim.sql'):
  if re.search(r"trigger_status\s+IN\s*\([^)]*'UNKNOWN'",texts[n],re.I|re.S): errors.append(f'{v}:UNKNOWN_AUTO_DISPATCH:{n}')
 if "item_status = 'RUNNING'" not in texts['centercut-item-complete.sql']: errors.append(f'{v}:CENTER_CUT_NO_RUNNING_CAS')
 if "status_cd IN ('PROCESSING','FAILED')" not in texts['execution-remote-message-reclaim.sql'].replace('  ',' '): errors.append(f'{v}:REMOTE_RECLAIM_NOT_FAIL_CLOSED')
 if not all(token in texts['scheduler-trigger-unknown-retry.sql'] for token in ("trigger_status = 'UNKNOWN'",'idempotency_key = ?','attempt_count = ?')): errors.append(f'{v}:SCHEDULER_RECONCILE_CAS_INCOMPLETE')
 if not all(token in texts['execution-remote-message-unknown-retry.sql'] for token in ("status_cd = 'UNKNOWN'",'attempt_no = ?','version_no = ?')): errors.append(f'{v}:REMOTE_RECONCILE_CAS_INCOMPLETE')
 for rel in ('source/18_batch_unknown_reconciliation.sql','install/07_batch_unknown_reconciliation.sql','migration/V103__batch_unknown_reconciliation.sql','rollback/R103__batch_unknown_reconciliation.sql','verify/103_verify_batch_unknown_reconciliation.sql'):
  if not (root/f'cpf-tools/db/vendor/{v}'/rel).is_file(): errors.append(f'{v}:missing:{rel}')
 pack=json.loads((root/f'cpf-tools/db/vendor/{v}/pack.json').read_text(encoding='utf-8'))
 if pack.get('batchUnknownReconciliationPack',{}).get('tableCount')!=1: errors.append(f'{v}:pack')
 fingerprints[v]=sorted(required)
result={'vendors':list(vendors),'requiredRuntimeKeys':list(required),'errors':errors,'semanticParity':len({tuple(x) for x in fingerprints.values()})==1,'pass':not errors}
print(json.dumps(result,ensure_ascii=False,indent=2));sys.exit(0 if result['pass'] and result['semanticParity'] else 1)
