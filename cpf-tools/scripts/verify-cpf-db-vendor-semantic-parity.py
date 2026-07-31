#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

VENDORS=("mariadb","postgresql","oracle")
CONTRACTS={
 "V83__spring_session_jdbc_bff.sql":{
  "tokens":{"spring_session","spring_session_attributes","primary_id","session_id","expiry_time","principal_name","attribute_bytes","spring_session_ix1","spring_session_ix2","spring_session_ix3","spring_session_attributes_fk"}},
 "V86__bff_encrypted_credential_vault.sql":{
  "tokens":{"cpf_bff_credential_vault","handle_id","key_id","access_iv","access_cipher_text","refresh_iv","refresh_cipher_text","access_expires_at","refresh_expires_at","version_no","idx_cpf_bff_credential_expiry","idx_cpf_bff_credential_key"}},
 "V87__batch_remote_message_ledger.sql":{
  "tokens":{"bat_remote_message_ledger","direction_cd","message_id","payload_sha256","status_cd","owner_id","lease_until","expires_at","attempt_no","version_no","idx_bat_remote_msg_status","idx_bat_remote_msg_expiry"}},
 "V88__scheduler_durable_launch_outbox.sql":{
  "tokens":{"bat_schedule_trigger","job_id","definition_version","definition_checksum","business_date","fire_zone","idempotency_key","dispatch_owner","dispatch_token","dispatch_lease_until","attempt_count","last_error_code","last_error_at","dispatched_at","updated_at","uq_bat_schedule_trigger_idem","ix_bat_schedule_trigger_dispatch"}},
 "V89__batch_execution_idempotency_lifecycle.sql":{
  "tokens":{"cpf_batch_execution_control","idempotency_scope","request_hash","plan_checksum","control_version","reconcile_attempts","reconcile_after","last_error_code","last_error_detail","uk_cpf_bat_exec_idem_scope","ix_cpf_bat_exec_reconcile","cpf_batch_execution_epoch","current_fencing_token","epoch_version"}},
 "V90__deployment_request_hash_reconciliation.sql":{
  "tokens":{"bat_deployment_execution","idempotency_scope","request_hash","reconcile_requested_by","reconcile_approved_by","reconcile_approval_request_id","reconcile_reason","reconciled_at","ix_bat_deploy_exec_reconciled"}},
 "V91__bza_bootstrap_claim_recovery.sql":{
  "tokens":{"bza_bootstrap_approval","claim_owner_id","claim_expires_at","cleanup_status","cleanup_failure_code","cleanup_updated_at","ix_bza_bootstrap_claim_lease"}},
}

def candidates(root:Path,vendor:str,name:str)->list[Path]:
 base=root/"cpf-tools/db/vendor"/vendor
 return list(base.rglob(name))

def normalized(path:Path)->str:
 text=path.read_text(encoding="utf-8").lower()
 text=re.sub(r"--.*?$"," ",text,flags=re.M)
 return re.sub(r"\s+"," ",text)

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument("--root",default=".");ap.add_argument("--json-report");a=ap.parse_args()
 root=Path(a.root).resolve();fail=[];matrix={}
 for name,contract in CONTRACTS.items():
  matrix[name]={}
  for vendor in VENDORS:
   found=candidates(root,vendor,name)
   if len(found)!=1:
    fail.append(f"{vendor}:{name}:expected exactly one file, found {len(found)}");continue
   path=found[0];text=normalized(path);missing=sorted(token for token in contract["tokens"] if token not in text)
   matrix[name][vendor]={"path":path.relative_to(root).as_posix(),"missing":missing}
   for token in missing:fail.append(f"{vendor}:{name}:missing semantic token:{token}")
 # Type family and integrity constraints must be vendor-native but logically equivalent.
 families={
  "mariadb":("longblob","varbinary","datetime(6)","bigint"),
  "postgresql":("bytea","timestamp(6)","bigint"),
  "oracle":("blob","raw(32)","timestamp(6)","number(19)"),
 }
 for vendor,tokens in families.items():
  texts=" ".join(normalized(p) for name in CONTRACTS for p in candidates(root,vendor,name))
  for token in tokens:
   if token not in texts:fail.append(f"{vendor}:vendor-native type family missing:{token}")
 report={"schemaVersion":1,"status":"PASS" if not fail else "FAIL","contracts":matrix,"failures":sorted(set(fail))}
 if a.json_report:
  out=Path(a.json_report);out=out if out.is_absolute() else root/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
 print(json.dumps(report,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=="__main__":raise SystemExit(main())
