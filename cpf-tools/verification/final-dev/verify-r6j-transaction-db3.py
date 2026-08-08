#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,shutil,tempfile
from pathlib import Path
VENDORS=("oracle","postgresql","mariadb")
REQ=("lineage_id","transaction_id","segment_id","parent_segment_id","attempt_no","trace_id","span_id","request_id","idempotency_key","tenant_id","channel_code","actor_id_masked","instance_id","was_id","agent_id","worker_id","remote_system","operation_id","message_id","consumer_group","dlq_id","batch_job_instance_id","batch_job_execution_id","batch_step_execution_id","partition_id","file_id","source_type","source_ref_id","lifecycle_state","failure_stage","unknown_yn","reconcile_state","occurred_at","freshness_at","payload_hash","archived_at")
class E(RuntimeError): pass
def req(x,m):
 if not x: raise E(m)
def text(root,rel):
 p=root/rel; req(p.is_file(),"missing "+rel); return p.read_text(encoding="utf-8")
def ddl_cols(s):
 m=re.search(r"CREATE\s+TABLE\s+cpf_transaction_lineage\s*\((.*?)\)\s*(?:ENGINE|PARTITION)",s,re.I|re.S); req(m,"cannot parse lineage table")
 return tuple(line.split()[0].lower() for line in (x.strip().rstrip(',') for x in m.group(1).splitlines()) if line and not line.upper().startswith('CONSTRAINT') and line.split()[0].lower() in REQ)
def verify(root):
 schema=json.loads(text(root,"cpf-tools/db/canonical/platform-schema.json")); names={t["name"]:t for t in schema["tables"]}
 req(schema.get("tableCount")==len(schema["tables"]),"canonical tableCount drift")
 for n in ("cpf_transaction_lineage","cpf_transaction_lineage_archive"): req(n in names,"canonical missing "+n)
 line=names["cpf_transaction_lineage"]; req(tuple(c["name"].lower() for c in line["columns"][:len(REQ)])==REQ,"canonical lineage column sequence drift")
 txcol=next(c for c in line["columns"] if c["name"].lower()=="transaction_id"); req(txcol["type"].upper()=="CHAR(34)","canonical transactionId must remain 34 chars")
 idx={i["name"] for i in line["indexes"]};
 for n in ("idx_cpf_tx_lineage_tx_time","idx_cpf_tx_lineage_trace","idx_cpf_tx_lineage_request","idx_cpf_tx_lineage_message","idx_cpf_tx_lineage_batch","idx_cpf_tx_lineage_file","idx_cpf_tx_lineage_retention"): req(n in idx,"canonical missing index "+n)
 for v in VENDORS:
  b=f"cpf-tools/db/vendor/{v}"; rels=[f"{b}/source/22_transaction_logging_lineage_r6j.sql",f"{b}/migration/V107__transaction_logging_lineage_r6j.sql",f"{b}/migration/flyway/cpfDB/V107__transaction_logging_lineage_r6j.sql",f"{b}/install/11_transaction_logging_lineage_r6j.sql"]
  docs=[text(root,r) for r in rels]; req(all(d==docs[0] for d in docs[1:]),v+" source/migration/flyway/install drift"); ddl=docs[0]; req(ddl_cols(ddl)==REQ,v+" column sequence drift"); req(re.search(r"transaction_id\s+(?:CHAR|CHARACTER)\s*\(?34\)?",ddl,re.I) is not None,v+" transactionId width drift")
  up=ddl.upper();
  for token in ("CPF_TRANSACTION_LINEAGE_ARCHIVE","IDX_CPF_TX_LINEAGE_TX_TIME","IDX_CPF_TX_LINEAGE_TRACE","IDX_CPF_TX_LINEAGE_REQUEST","IDX_CPF_TX_LINEAGE_MESSAGE","IDX_CPF_TX_LINEAGE_BATCH","IDX_CPF_TX_LINEAGE_FILE","IDX_CPF_TX_LINEAGE_RETENTION"): req(token in up,v+" missing "+token)
  rollback=text(root,f"{b}/rollback/R107__transaction_logging_lineage_r6j.sql").upper(); req("CPF_TRANSACTION_LINEAGE_ARCHIVE" in rollback and "CPF_TRANSACTION_LINEAGE" in rollback,v+" rollback incomplete")
  archive=text(root,f"{b}/runtime/cpf/transaction_lineage_archive.sql").upper(); purge=text(root,f"{b}/runtime/cpf/transaction_lineage_purge.sql").upper(); lookup=text(root,f"{b}/runtime/cpf/transaction_lineage_large_lookup.sql").upper(); verify_sql=text(root,f"{b}/verify/107_verify_transaction_logging_lineage_r6j.sql").upper(); req("INSERT INTO CPF_TRANSACTION_LINEAGE_ARCHIVE" in archive and "NOT EXISTS" in archive,v+" archive not idempotent"); req("DELETE FROM CPF_TRANSACTION_LINEAGE" in purge and "EXISTS" in purge and "ARCHIVED_AT IS NOT NULL" in purge,v+" unsafe purge"); req("TRANSACTION_ID" in lookup and "ORDER BY OCCURRED_AT" in lookup,v+" lookup missing"); req("CPF_TRANSACTION_LINEAGE" in verify_sql,v+" verify SQL missing")
 writer=text(root,"cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentPersistenceService.java"); req("CpfTransactionLineageRecord.fromSegment" in writer and "lineageProjection.upsert" in writer,"real lineage writer path missing")
 adapter=text(root,"cpf-starters/data/persistence-mybatis/src/main/java/com/cpf/starter/persistence/mybatis/logging/JdbcTransactionLineageProjectionAdapter.java"); req("INSERT INTO cpf_transaction_lineage" in adapter and "UPDATE cpf_transaction_lineage" in adapter,"idempotent downstream writer missing")
 return True
def main():
 a=argparse.ArgumentParser();a.add_argument("--root",type=Path,default=Path('.'));a.add_argument("--self-test",action='store_true');x=a.parse_args();root=x.root.resolve();verify(root)
 if x.self_test:
  muts=[("canonical",lambda r:(r/"cpf-tools/db/canonical/platform-schema.json").write_text(text(r,"cpf-tools/db/canonical/platform-schema.json").replace('cpf_transaction_lineage','cpf_transaction_lineage_removed',1),encoding='utf-8')),("writer",lambda r:(r/"cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentPersistenceService.java").write_text(text(r,"cpf-core/src/main/java/com/cpf/core/common/logging/segment/TransactionSegmentPersistenceService.java").replace('lineageProjection.upsert','lineageProjection_removed.upsert',1),encoding='utf-8')),("rollback",lambda r:(r/"cpf-tools/db/vendor/mariadb/rollback/R107__transaction_logging_lineage_r6j.sql").write_text('-- removed\n',encoding='utf-8'))]
  for name,mut in muts:
   with tempfile.TemporaryDirectory(prefix='cpf-db3-mut-') as td:
    mr=Path(td)/'root';shutil.copytree(root,mr);mut(mr)
    try: verify(mr)
    except E: pass
    else: raise E(name+" mutation survived")
 print("[CPF][FINAL][DB3][PASS] canonical=2 vendor=3 writer=idempotent rollback/runtime/index=true selfTest="+str(x.self_test).lower())
if __name__=='__main__':
 try: main()
 except E as e: print("[CPF][FINAL][DB3][FAIL] "+str(e));raise SystemExit(1)
