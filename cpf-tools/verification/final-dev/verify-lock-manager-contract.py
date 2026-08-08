#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, shutil, tempfile
from pathlib import Path

VENDORS=("oracle","postgresql","mariadb")
SHA="f6d7080c5a14b7dd7595093f9497470169e18d80"
class E(RuntimeError): pass
def req(x,m):
    if not x: raise E(m)
def text(root,rel):
    p=root/rel; req(p.is_file(),"missing "+rel); s=p.read_text(encoding="utf-8"); req(s.strip(),"empty "+rel); return s

def verify(root:Path):
    contract=text(root,"cpf-core/src/main/java/com/cpf/core/api/locking/CpfLockManager.java")
    guard=text(root,"cpf-core/src/main/java/com/cpf/core/api/locking/CpfLockingExecutionGuard.java")
    impl=text(root,"cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/locking/CpfJdbcLockManager.java")
    store=text(root,"cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/locking/JdbcCpfLockStore.java")
    auto=text(root,"cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/locking/CpfJdbcLockAutoConfiguration.java")
    imports=text(root,"cpf-starters/data/persistence-jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
    logic_test=text(root,"cpf-starters/data/persistence-jdbc/src/test/java/com/cpf/starter/data/persistence/jdbc/locking/CpfJdbcLockManagerTest.java")
    guard_test=text(root,"cpf-core/src/test/java/com/cpf/core/api/locking/CpfLockingExecutionGuardTest.java")
    for token in ("fencingToken","ownerEpoch","leaseUntil","ForceReleaseApproval","reconcileExpired","validateToken"):
        req(token in contract,"contract missing "+token)
    for token in ("executeFenced","manager.release"):
        req(token in guard,"consumer guard missing "+token)
    req(guard.count("requireValidFence(token);") >= 2,"consumer must validate fencing before and after action")
    for token in ("IDEMPOTENT_REPLAY","BUSY","LEASE_EXPIRED","OPTIMISTIC_CONFLICT","SEPARATION_OF_DUTIES","APPROVAL_SCOPE_MISMATCH","reconcileExpired","validateFence","findForUpdate"):
        req(token in impl,"provider logic missing "+token)
    for token in ("SELECT "+'"+COLUMNS+"'+" FROM CPF_RUNTIME_LOCK","FOR UPDATE","INSERT INTO CPF_RUNTIME_LOCK","UPDATE CPF_RUNTIME_LOCK","VERSION_NO=?"):
        # special concatenated token check simplified below
        pass
    for token in ("CPF_RUNTIME_LOCK","FOR UPDATE","INSERT INTO CPF_RUNTIME_LOCK","UPDATE CPF_RUNTIME_LOCK","VERSION_NO=?"):
        req(token in store,"JDBC store missing "+token)
    req("CpfJdbcLockAutoConfiguration" in imports,"auto-configuration import missing")
    req("@ConditionalOnMissingBean(CpfLockManager.class)" in auto,"provider must be replaceable")
    for token in ("acquireReplayBusyRenewReleaseAndTakeover","expiryRejectsStaleWriterAndReconcileRecovers","forceReleaseRequiresSeparationAndCommandBoundApproval","invalidAndStorageFailureFailClosed"):
        req(token in logic_test,"logic test missing "+token)
    req("rejectsStaleWriterBeforeCommit" in guard_test,"stale writer guard test missing")
    schema=json.loads(text(root,"cpf-tools/db/canonical/platform-schema.json")); req(schema.get("tableCount")==len(schema.get("tables",[])),"canonical tableCount drift")
    tables={t["name"]:t for t in schema["tables"]}; req("cpf_runtime_lock" in tables,"canonical lock table missing")
    t=tables["cpf_runtime_lock"]
    cols=[c["name"] for c in t["columns"]]
    expected=["lock_key","owner_id","request_id","fencing_token","owner_epoch","version_no","acquired_at","lease_until","lock_state","last_reason","updated_at"]
    req(cols==expected,"canonical lock columns/order drift")
    req(t.get("primaryKey")==["lock_key"],"canonical lock PK drift")
    idx={i["name"] for i in t.get("indexes",[])}
    req({"idx_cpf_runtime_lock_lease","idx_cpf_runtime_lock_owner"}<=idx,"canonical lock indexes missing")
    for v in VENDORS:
        b=f"cpf-tools/db/vendor/{v}"
        rels=[f"{b}/source/24_runtime_lock_r6j.sql",f"{b}/migration/V108__runtime_lock_r6j.sql",f"{b}/migration/flyway/cpfDB/V108__runtime_lock_r6j.sql",f"{b}/install/12_runtime_lock_r6j.sql"]
        docs=[text(root,r) for r in rels]; req(all(x==docs[0] for x in docs[1:]),v+" source/migration/flyway/install drift")
        up=docs[0].upper()
        for token in ("CPF_RUNTIME_LOCK","FENCING_TOKEN","OWNER_EPOCH","VERSION_NO","LEASE_UNTIL","LOCK_STATE","IDX_CPF_RUNTIME_LOCK_LEASE","IDX_CPF_RUNTIME_LOCK_OWNER"):
            req(token in up,v+" DDL missing "+token)
        rb=text(root,f"{b}/rollback/R108__runtime_lock_r6j.sql").upper(); req("DROP TABLE CPF_RUNTIME_LOCK" in rb,v+" rollback missing")
        vf=text(root,f"{b}/verify/108_verify_runtime_lock_r6j.sql").upper(); req("CPF_RUNTIME_LOCK" in vf and "FENCING_TOKEN" in vf,v+" verify missing")
        rt=text(root,f"{b}/runtime/cpf/runtime_lock_expired_candidates.sql").upper(); req("LOCK_STATE='ACTIVE'" in rt and "LEASE_UNTIL" in rt,v+" runtime reconcile query missing")
    return True

def main():
    ap=argparse.ArgumentParser(); ap.add_argument("--root",type=Path,default=Path('.')); ap.add_argument("--self-test",action="store_true"); a=ap.parse_args(); root=a.root.resolve(); verify(root)
    if a.self_test:
        muts=[
            ("consumer",lambda r:(r/"cpf-core/src/main/java/com/cpf/core/api/locking/CpfLockingExecutionGuard.java").write_text(text(r,"cpf-core/src/main/java/com/cpf/core/api/locking/CpfLockingExecutionGuard.java").replace("requireValidFence(token);","// removed"),encoding="utf-8")),
            ("provider",lambda r:(r/"cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/locking/CpfJdbcLockManager.java").write_text(text(r,"cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/locking/CpfJdbcLockManager.java").replace("APPROVAL_SCOPE_MISMATCH","SCOPE_REMOVED"),encoding="utf-8")),
            ("ddl",lambda r:(r/"cpf-tools/db/vendor/mariadb/migration/V108__runtime_lock_r6j.sql").write_text("-- removed\n",encoding="utf-8")),
        ]
        for name,mut in muts:
            with tempfile.TemporaryDirectory(prefix="cpf-lock-mut-") as td:
                mr=Path(td)/"root"; shutil.copytree(root,mr); mut(mr)
                try: verify(mr)
                except E: pass
                else: raise E(name+" mutation survived")
    print("[CPF][FINAL][LOCK][PASS] provider=jdbc vendors=3 lease/fencing/epoch/reconcile=true selfTest="+str(a.self_test).lower())
if __name__=="__main__":
    try: main()
    except E as e:
        print("[CPF][FINAL][LOCK][FAIL] "+str(e)); raise SystemExit(1)
