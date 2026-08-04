from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "verify-cpf-bat-operation-ledger-lifecycle.py"
HELPER = "cpf_assert_empty_bat_operation_request_r100"


def load():
    spec = importlib.util.spec_from_file_location("bat_v100_gate", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader
    spec.loader.exec_module(module)
    return module


class BatLedgerLifecycleGateTest(unittest.TestCase):
    def fixture(self, missing_v100=False, wrong_order=False, missing_table=False):
        temporary = tempfile.TemporaryDirectory(); root = Path(temporary.name)
        canonical = {"tableCount": 0 if missing_table else 1, "tables": [] if missing_table else [{"name": "bat_operation_request"}]}
        path = root / "cpf-tools/db/canonical/platform-schema.json"; path.parent.mkdir(parents=True); path.write_text(json.dumps(canonical), encoding="utf-8")
        for vendor in ("mariadb", "postgresql", "oracle"):
            base = root / "cpf-tools/db/vendor" / vendor
            for directory in ("source", "install", "migration", "rollback", "verify"):
                (base / directory).mkdir(parents=True, exist_ok=True)
            (base / "source/35_bat_schema.sql").write_text("CREATE TABLE bat_operation_request(id BIGINT);", encoding="utf-8")
            (base / "install/00_empty_install.sql").write_text("CREATE TABLE bat_operation_request(id BIGINT);", encoding="utf-8")
            if not (missing_v100 and vendor == "oracle"):
                (base / "migration/V100__bat_operation_request_ledger.sql").write_text("CREATE TABLE bat_operation_request(id BIGINT);", encoding="utf-8")
            if vendor == "mariadb":
                guard = f"""
DROP PROCEDURE IF EXISTS {HELPER};
CREATE PROCEDURE {HELPER}() BEGIN
 IF EXISTS (SELECT 1 FROM bat_operation_request LIMIT 1) THEN SIGNAL SQLSTATE '45000'; END IF;
END;
CALL {HELPER}();
DROP PROCEDURE {HELPER};
"""
            elif vendor == "postgresql":
                guard = "IF EXISTS (SELECT 1 FROM bat_operation_request LIMIT 1) THEN RAISE EXCEPTION 'NONEMPTY'; END IF;"
            else:
                guard = "SELECT COUNT(*) INTO n FROM bat_operation_request WHERE ROWNUM=1; IF n>0 THEN RAISE_APPLICATION_ERROR(-20996,'NONEMPTY'); END IF;"
            (base / "rollback/R100__bat_operation_request_ledger.sql").write_text(guard + " DROP TABLE bat_operation_request;", encoding="utf-8")
            (base / "verify/V100__bat_operation_request_ledger.sql").write_text("SELECT 1 FROM bat_operation_request WHERE 1=0;", encoding="utf-8")
        upgrade = "Run-Sql $v98;Run-Sql $v99;Run-Sql $v100;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100;Run-Sql $verify"
        if wrong_order:
            upgrade = "Run-Sql $v100;Run-Sql $v98;Run-Sql $v99;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100;Run-Sql $verify"
        lifecycle = f"""
function P([string]$Relative){{}}
$v100=P 'migration/V100__bat_operation_request_ledger.sql'
$r100=P 'rollback/R100__bat_operation_request_ledger.sql'
$verify100=P 'verify/V100__bat_operation_request_ledger.sql'
switch($Mode){{
 'FreshInstall' {{Run-Sql $install;Run-Sql $verify;Run-Sql $verify100}}
 'Upgrade' {{{upgrade}}}
 'RollbackReapply' {{Run-Sql $r100;Run-Sql $r99;Run-Sql $r98;Run-Sql $v98;Run-Sql $v99;Run-Sql $v100;Run-Sql $verify98;Run-Sql $verify99;Run-Sql $verify100;Run-Sql $verify}}
}}
"""
        script = root / "cpf-tools/scripts/run-db-vendor-lifecycle.ps1"; script.parent.mkdir(parents=True); script.write_text(lifecycle, encoding="utf-8")
        return temporary, root

    def test_positive_closure(self):
        temporary, root = self.fixture(); self.addCleanup(temporary.cleanup)
        self.assertEqual("PASS", load().verify(root)["status"])

    def test_missing_vendor_migration_fails(self):
        temporary, root = self.fixture(missing_v100=True); self.addCleanup(temporary.cleanup)
        with self.assertRaises(Exception): load().verify(root)

    def test_wrong_upgrade_order_fails(self):
        temporary, root = self.fixture(wrong_order=True); self.addCleanup(temporary.cleanup)
        with self.assertRaises(Exception): load().verify(root)

    def test_missing_canonical_table_fails(self):
        temporary, root = self.fixture(missing_table=True); self.addCleanup(temporary.cleanup)
        with self.assertRaises(Exception): load().verify(root)

    def test_rollback_must_drop_table(self):
        temporary, root = self.fixture(); self.addCleanup(temporary.cleanup)
        (root / "cpf-tools/db/vendor/postgresql/rollback/R100__bat_operation_request_ledger.sql").write_text("SELECT 1;", encoding="utf-8")
        with self.assertRaises(Exception): load().verify(root)

    def test_rollback_without_nonempty_guard_fails(self):
        temporary, root = self.fixture(); self.addCleanup(temporary.cleanup)
        (root / "cpf-tools/db/vendor/mariadb/rollback/R100__bat_operation_request_ledger.sql").write_text("DROP TABLE bat_operation_request;", encoding="utf-8")
        with self.assertRaises(Exception): load().verify(root)

    def test_mariadb_retry_cleanup_must_precede_helper_creation(self):
        temporary, root = self.fixture(); self.addCleanup(temporary.cleanup)
        path = root / "cpf-tools/db/vendor/mariadb/rollback/R100__bat_operation_request_ledger.sql"
        path.write_text(path.read_text(encoding="utf-8").replace(f"DROP PROCEDURE IF EXISTS {HELPER};", ""), encoding="utf-8")
        with self.assertRaisesRegex(Exception, "retry-safe pre-cleanup"):
            load().verify(root)


if __name__ == "__main__": unittest.main()
