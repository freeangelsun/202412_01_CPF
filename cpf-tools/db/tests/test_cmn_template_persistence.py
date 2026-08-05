from __future__ import annotations

import hashlib
import json
import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
DB = ROOT / "cpf-tools/db"
JAVA = ROOT / "cpf-common/src/main/java/com/cpf/common/template"
VENDORS = ("mariadb", "postgresql", "oracle")


class CmnTemplatePersistenceTest(unittest.TestCase):
    def test_canonical_tables_are_owned_by_cmn_db(self):
        schema = json.loads((DB / "canonical/platform-schema.json").read_text(encoding="utf-8-sig"))
        tables = {table["name"]: table for table in schema["tables"]}
        definition = tables["CMN_TEMPLATE_DEFINITION"]
        audit = tables["CMN_TEMPLATE_AUDIT"]
        self.assertEqual("cmnDB", definition["logicalDatabase"])
        self.assertEqual("cmnDB", audit["logicalDatabase"])
        self.assertEqual(["TEMPLATE_CODE", "TEMPLATE_VERSION", "CHANNEL_CODE"], definition["primaryKey"])
        self.assertIn("REVISION_NO", {column["name"] for column in definition["columns"]})
        self.assertEqual(["AUDIT_ID"], audit["primaryKey"])
        self.assertTrue(any(fk["refTable"] == "CMN_TEMPLATE_DEFINITION" for fk in audit["foreignKeys"]))

    def test_three_vendor_source_install_migration_rollback_verify_exist(self):
        for vendor in VENDORS:
            source = (DB / f"vendor/{vendor}/source/20_cmn_schema.sql").read_text(encoding="utf-8-sig").lower()
            self.assertIn("cmn_template_definition", source, vendor)
            self.assertIn("cmn_template_audit", source, vendor)
            self.assertIn("revision_no", source, vendor)
            logical = Path() if vendor == "mariadb" else Path("cmnDB")
            migration = DB / f"vendor/{vendor}/migration/flyway" / logical / "V101__cmn_template_definition.sql"
            rollback = DB / f"vendor/{vendor}/rollback" / logical / "R101__cmn_template_definition.sql"
            verify = DB / f"vendor/{vendor}/verify/V101__cmn_template_definition.sql"
            install = DB / f"vendor/{vendor}/install/04_cmn_template_definition.sql"
            for path in (migration, rollback, verify, install):
                self.assertTrue(path.is_file(), f"missing {path}")
                self.assertGreater(path.stat().st_size, 40)
            self.assertLess(rollback.read_text(encoding="utf-8").lower().index("cmn_template_audit"),
                            rollback.read_text(encoding="utf-8").lower().index("cmn_template_definition"))
            manifest = migration.parent / "checksums.sha256"
            line = next(line for line in manifest.read_text(encoding="utf-8").splitlines()
                        if "V101__cmn_template_definition.sql" in line)
            self.assertEqual(hashlib.sha256(migration.read_bytes()).hexdigest(), line.split()[0])

    def test_store_serializes_approval_enforces_sod_cas_and_append_only_audit(self):
        source = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        query_root = DB / "runtime-template/cmn/template"
        self.assertIn("FOR UPDATE", (query_root / "lock-history.sql").read_text(encoding="utf-8"))
        self.assertIn("template creator and approver must be different", source)
        self.assertIn("revision_no=?", (query_root / "approve.sql").read_text(encoding="utf-8"))
        self.assertIn("INSERT INTO cmn_template_audit", (query_root / "insert-audit.sql").read_text(encoding="utf-8"))
        self.assertIn('insertAudit("SUPERSEDE"', source)
        self.assertNotIn("cmn_template_variable", source)

    def test_management_mutations_and_audit_query_use_cmn_transaction(self):
        source = (JAVA / "CmnTemplateManagementService.java").read_text(encoding="utf-8")
        self.assertGreaterEqual(source.count('@Transactional(transactionManager = "cmnTransactionManager")'), 3)
        self.assertIn('readOnly = true', source)
        self.assertIn("store.approve", source)
        self.assertIn("store.retire", source)
        self.assertIn("store.auditHistory", source)
        self.assertIn("publishRequired", source)

    def test_product_renderer_and_provider_have_actual_spring_consumers(self):
        renderer = (JAVA / "CmnTemplateRenderer.java").read_text(encoding="utf-8")
        service = (JAVA / "CmnTemplateService.java").read_text(encoding="utf-8")
        store = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        self.assertIn("@Component", renderer)
        self.assertIn("@Service", service)
        self.assertIn("@Repository", store)
        self.assertIn("findActive", store)
        listener = (ROOT / "cpf-common/src/main/java/com/cpf/common/ref/service/CacheRefreshEventListener.java").read_text(encoding="utf-8")
        self.assertIn('"commonTemplate"', listener)

    def test_only_store_is_product_spring_persistence_provider(self):
        # Deletion of the legacy manual adapter requires an approved delete manifest. Until then it may remain
        # as deprecated source compatibility, but it must never be auto-discovered as a product Spring provider.
        self.assertTrue((JAVA / "CmnTemplateStore.java").is_file())
        store = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        self.assertIn("@Repository", store)
        legacy = (JAVA / "CmnJdbcTemplateRepository.java").read_text(encoding="utf-8")
        self.assertIn("@Deprecated", legacy)
        self.assertNotIn("@Repository", legacy)
        self.assertNotIn("@Component", legacy)
        self.assertNotIn("@Service", legacy)


if __name__ == "__main__":
    unittest.main()
