#!/usr/bin/env python3
from __future__ import annotations
import argparse,re,shutil,subprocess,sys,tempfile
from pathlib import Path
VENDORS=('oracle','postgresql','mariadb')
REQUIRED_COLS=('lineage_id','transaction_id','segment_id','parent_segment_id','attempt_no','trace_id','span_id','request_id','idempotency_key','tenant_id','channel_code','actor_id_masked','instance_id','was_id','agent_id','worker_id','remote_system','operation_id','message_id','consumer_group','dlq_id','batch_job_instance_id','batch_job_execution_id','batch_step_execution_id','partition_id','file_id','source_type','source_ref_id','lifecycle_state','failure_stage','unknown_yn','reconcile_state','occurred_at','freshness_at','payload_hash','archived_at')
class E(RuntimeError): pass
def req(v,m):
 if not v: raise E(m)
def text(root,rel):
 p=root/rel
 req(p.is_file(),'missing '+rel)
 return p.read_text(encoding='utf-8')
def ddl_cols(s):
 m=re.search(r'CREATE\s+TABLE\s+cpf_transaction_lineage\s*\((.*?)\)\s*(?:ENGINE|PARTITION)',s,re.I|re.S)
 req(m is not None,'cannot parse lineage table')
 body=m.group(1)
 out=[]
 for raw in body.splitlines():
  line=raw.strip().rstrip(',')
  if not line or line.upper().startswith('CONSTRAINT'): continue
  name=line.split()[0].lower()
  if name in REQUIRED_COLS: out.append(name)
 return tuple(out)
def verify(root):
 for v in VENDORS:
  base=f'cpf-tools/db/vendor/{v}'
  rels=[f'{base}/source/22_transaction_logging_lineage_r6j.sql',f'{base}/migration/V107__transaction_logging_lineage_r6j.sql',f'{base}/migration/flyway/cpfDB/V107__transaction_logging_lineage_r6j.sql',f'{base}/install/11_transaction_logging_lineage_r6j.sql']
  docs=[text(root,r) for r in rels]
  req(all(d==docs[0] for d in docs[1:]),f'{v} source/migration/flyway/install drift')
  ddl=docs[0]
  req(ddl_cols(ddl)==REQUIRED_COLS,f'{v} canonical column sequence drift: {ddl_cols(ddl)}')
  upper=ddl.upper()
  for token in ('CPF_TRANSACTION_LINEAGE_ARCHIVE','IDX_CPF_TX_LINEAGE_TX_TIME','IDX_CPF_TX_LINEAGE_TRACE','IDX_CPF_TX_LINEAGE_REQUEST','IDX_CPF_TX_LINEAGE_MESSAGE','IDX_CPF_TX_LINEAGE_BATCH','IDX_CPF_TX_LINEAGE_FILE','IDX_CPF_TX_LINEAGE_RETENTION'):
   req(token in upper,f'{v} missing {token}')
  if v=='postgresql': req('PARTITION BY RANGE (OCCURRED_AT)' in upper and 'PARTITION OF CPF_TRANSACTION_LINEAGE DEFAULT' in upper,'postgresql physical partition missing')
  if v=='mariadb': req('PARTITION BY RANGE COLUMNS (OCCURRED_AT)' in upper and 'MAXVALUE' in upper,'mariadb physical partition missing')
  if v=='oracle': req('PARTITION BY RANGE (OCCURRED_AT)' in upper and 'NUMTOYMINTERVAL(1, \'MONTH\')' in upper,'oracle interval partition missing')
  archive=text(root,f'{base}/runtime/cpf/transaction_lineage_archive.sql').upper()
  purge=text(root,f'{base}/runtime/cpf/transaction_lineage_purge.sql').upper()
  lookup=text(root,f'{base}/runtime/cpf/transaction_lineage_large_lookup.sql').upper()
  rollback=text(root,f'{base}/rollback/R107__transaction_logging_lineage_r6j.sql').upper()
  verify_sql=text(root,f'{base}/verify/107_verify_transaction_logging_lineage_r6j.sql').upper()
  req('INSERT INTO CPF_TRANSACTION_LINEAGE_ARCHIVE' in archive and 'NOT EXISTS' in archive,'archive must be idempotent '+v)
  req('ARCHIVED_AT' in archive and 'ARCHIVE_REASON' in archive,'archive audit metadata missing '+v)
  req('DELETE FROM CPF_TRANSACTION_LINEAGE' in purge and 'EXISTS' in purge and 'ARCHIVED_AT IS NOT NULL' in purge,'purge must only remove proven archived rows '+v)
  req('WHERE TRANSACTION_ID = :TRANSACTION_ID' in lookup and 'ORDER BY OCCURRED_AT' in lookup,'bounded transaction lookup missing '+v)
  req('CPF_TRANSACTION_LINEAGE_ARCHIVE' in rollback and 'CPF_TRANSACTION_LINEAGE' in rollback,'rollback incomplete '+v)
  req('CPF_TRANSACTION_LINEAGE' in verify_sql and ('PARTITION' in verify_sql or 'PARTSTRAT' in verify_sql),'partition verify missing '+v)
 return True
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path('.'));ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=a.root.resolve();verify(root)
 if a.self_test:
  with tempfile.TemporaryDirectory(prefix='cpf-r6j-db3-mutation-') as td:
   mr=Path(td)/'root';shutil.copytree(root,mr)
   p=mr/'cpf-tools/db/vendor/postgresql/source/22_transaction_logging_lineage_r6j.sql'
   p.write_text(p.read_text(encoding='utf-8').replace('trace_id VARCHAR(128),','trace_id_removed VARCHAR(128),',1),encoding='utf-8')
   try: verify(mr)
   except E: pass
   else: raise E('DB3 semantic mutation survived')
 print('[CPF][R6J][DB3][PASS] vendors=3 source=migration=flyway=install rollback=runtime verify partition=physical archive=durable selfTest='+str(a.self_test).lower())
if __name__=='__main__':
 try: main()
 except E as e: print('[CPF][R6J][DB3][FAIL] '+str(e),file=sys.stderr);raise SystemExit(1)
