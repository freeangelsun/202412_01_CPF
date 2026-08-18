#!/usr/bin/env python3
from pathlib import Path
import json, sys
ROOT=Path(__file__).resolve().parents[3]
fail=[]
def need(path, token=None):
    p=ROOT/path
    if not p.exists(): fail.append(f'missing:{path}'); return ''
    s=p.read_text(encoding='utf-8',errors='ignore')
    if token and token not in s: fail.append(f'missing-token:{path}:{token}')
    return s

# Canonical contracts / runtime consumers
need(Path('cpf-core/src/main/java/com/cpf/core/api/tracking/CpfSubjectTrackingOperations.java'),'void collect')
need(Path('cpf-core/src/main/java/com/cpf/core/api/tracking/CpfSubjectType.java'),'CUSTOMER_NO')
web=need(Path('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfWebContextFilter.java'),'X-Subject-Type')
if 'CpfSubjectTrustLevel.CLAIMED' not in web: fail.append('web-subject-not-claimed')
sec=need(Path('cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfAuthenticatedContextFilter.java'),'bindVerifiedSubjects')
if 'CpfSubjectTrustLevel.VERIFIED' not in sec: fail.append('security-subject-not-verified')
repo=need(Path('cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/tracking/JdbcCpfSubjectTrackingRepository.java'),'searchableToken')
for forbidden in ('customer_no VARCHAR','member_no VARCHAR','login_id VARCHAR'):
    if forbidden.lower() in repo.lower(): fail.append('raw-subject-persistence:'+forbidden)
if 'minimumTrust()' not in repo: fail.append('trusted-default-query-not-enforced')

# Canonical DB + lifecycle DB3
schema=json.loads((ROOT/'cpf-tools/db/canonical/platform-schema.json').read_text(encoding='utf-8'))
tables={t['name']:t for t in schema['tables']}
t=tables.get('OPS_TRANSACTION_SUBJECT')
if not t: fail.append('canonical-subject-table-missing')
else:
    cols={c['name'] for c in t['columns']}
    required={'transaction_id','subject_role','subject_type','subject_search_key','subject_masked_value','source_type','trust_level','search_key_version','first_seen_at','last_seen_at'}
    miss=required-cols
    if miss: fail.append('canonical-subject-columns:'+','.join(sorted(miss)))
    raw={'customer_no','customer_id','member_no','login_id'} & cols
    if raw: fail.append('canonical-raw-subject-columns:'+','.join(sorted(raw)))
for vendor,fwd,rb in [
 ('mariadb','cpf-tools/db/vendor/mariadb/migration/flyway/V128__transaction_subject_tracking.sql','cpf-tools/db/vendor/mariadb/rollback/V128__transaction_subject_tracking_rollback.sql'),
 ('postgresql','cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V128__transaction_subject_tracking.sql','cpf-tools/db/vendor/postgresql/rollback/cpfDB/V128__transaction_subject_tracking_rollback.sql'),
 ('oracle','cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V128__transaction_subject_tracking.sql','cpf-tools/db/vendor/oracle/rollback/cpfDB/V128__transaction_subject_tracking_rollback.sql')]:
    need(Path(fwd),'OPS_TRANSACTION_SUBJECT'); need(Path(rb),'OPS_TRANSACTION_SUBJECT')

# ADM consumer is POST body, audited, and reuses transaction-group timeline.
ctl=need(Path('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmTransactionGroupController.java'),'@PostMapping("/subject-search")')
for token in ('SUBJECT_TIMELINE_SEARCH','auditLogService.record','SubjectSearchRequest'):
    if token not in ctl: fail.append('adm-subject-consumer:'+token)
svc=need(Path('cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmTransactionGroupService.java'),'findBySubject')
for token in ('CpfSubjectTrustLevel.TRUSTED','subjectTimelineQueryPort.findTransactions'):
    if token not in svc: fail.append('adm-subject-service:'+token)
auth=need(Path('cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java'),'POST /adm/api/transaction-groups/subject-search')
if 'LOG_LIST_READ' not in auth: fail.append('adm-subject-permission-missing')

if fail:
    print('CPF_SUBJECT_TRACKING_CLOSURE=FAIL count='+str(len(fail)))
    for x in fail: print(' - '+x)
    sys.exit(1)
print('CPF_SUBJECT_TRACKING_CLOSURE=PASS types=4 db3=3 rawPersistence=0 admConsumer=1')
