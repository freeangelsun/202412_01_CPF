from __future__ import annotations

import hashlib
import json
import os
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
DB = ROOT / "cpf-tools/db"
JAVA = ROOT / "cpf-common/src/main/java/com/cpf/common/template"
CACHE = ROOT / "cpf-starters/common/src/main/java/com/cpf/common/runtime/cache"
AUTOCONFIG = ROOT / "cpf-starters/common/src/main/java/com/cpf/common/runtime/CpfCommonJdbcAutoConfiguration.java"
VENDORS = ("mariadb", "postgresql", "oracle")


class CmnTemplatePersistenceTest(unittest.TestCase):
    def test_canonical_tables_are_owned_by_cpf_platform_db(self):
        schema = json.loads((DB / "canonical/platform-schema.json").read_text(encoding="utf-8-sig"))
        tables = {table["name"]: table for table in schema["tables"]}
        definition = tables["CMN_TEMPLATE_DEFINITION"]
        audit = tables["CMN_TEMPLATE_AUDIT"]
        self.assertEqual("cpfDB", definition["logicalDatabase"])
        self.assertEqual("cpfDB", audit["logicalDatabase"])
        self.assertEqual(["TEMPLATE_CODE", "TEMPLATE_VERSION", "CHANNEL_CODE"], definition["primaryKey"])
        self.assertIn("REVISION_NO", {column["name"] for column in definition["columns"]})
        self.assertEqual(["AUDIT_ID"], audit["primaryKey"])
        self.assertTrue(any(fk["refTable"] == "CMN_TEMPLATE_DEFINITION" for fk in audit["foreignKeys"]))

    def test_three_vendor_generated_platform_packs_include_template_schema_verify_rollback(self):
        for vendor in VENDORS:
            pack = DB / "generated/current" / vendor
            manifest = json.loads((pack / "manifest.json").read_text(encoding="utf-8-sig"))
            for name in ("cpf-platform-schema.sql", "cpf-platform-verify.sql", "cpf-platform-rollback.sql"):
                path = pack / name
                self.assertTrue(path.is_file(), f"missing {path}")
                text = path.read_text(encoding="utf-8-sig").lower()
                self.assertIn("cmn_template_definition", text, f"{vendor}:{name}")
                if name != "cpf-platform-verify.sql":
                    self.assertIn("cmn_template_audit", text, f"{vendor}:{name}")
                self.assertEqual(manifest["artifacts"][name], hashlib.sha256(path.read_bytes()).hexdigest())

    def test_store_serializes_approval_enforces_sod_cas_and_append_only_audit(self):
        source = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        query_root = DB / "runtime-template/cmn/template"
        self.assertIn("FOR UPDATE", (query_root / "lock-history.sql").read_text(encoding="utf-8"))
        self.assertIn("template creator and approver must be different", source)
        self.assertIn("revision_no=?", (query_root / "approve.sql").read_text(encoding="utf-8"))
        # 정본 Query Pack 은 canonical 대문자 테이블명을 렌더링한다(Linux MariaDB 는 대소문자를
        # 구분하고, PostgreSQL/Oracle 은 unquoted 를 각각 소문자/대문자로 접는다).
        # 소문자를 기대하던 이 단정은 정본과 어긋나 stale contract 였다.
        self.assertIn(
            "INSERT INTO CMN_TEMPLATE_AUDIT",
            (query_root / "insert-audit.sql").read_text(encoding="utf-8"),
        )
        self.assertIn('insertAudit("SUPERSEDE"', source)
        self.assertNotIn("cmn_template_variable", source)

    def test_management_mutations_and_audit_query_use_cpf_common_transaction(self):
        source = (JAVA / "CmnTemplateManagementService.java").read_text(encoding="utf-8")
        self.assertIn('CpfCommonPersistenceNames.TX_MANAGER_BEAN', source)
        self.assertGreaterEqual(source.count('transactionManager = CpfCommonPersistenceNames.TX_MANAGER_BEAN'), 4)
        self.assertIn('readOnly = true', source)
        self.assertIn("store.approve", source)
        self.assertIn("store.retire", source)
        self.assertIn("store.auditHistory", source)
        self.assertIn("publishRequired", source)

    def test_product_renderer_provider_and_generic_refresh_consumer_are_wired(self):
        renderer = (JAVA / "CmnTemplateRenderer.java").read_text(encoding="utf-8")
        service = (JAVA / "CmnTemplateService.java").read_text(encoding="utf-8")
        store = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        management = (JAVA / "CmnTemplateManagementService.java").read_text(encoding="utf-8")
        listener = (CACHE / "CpfCommonCacheRefreshListener.java").read_text(encoding="utf-8")
        wiring = AUTOCONFIG.read_text(encoding="utf-8")
        self.assertIn("@Component", renderer)
        self.assertNotIn("@Service", service)
        self.assertNotIn("@Repository", store)
        self.assertIn("@ConditionalOnMissingBean(CmnTemplateService.class)", wiring)
        self.assertIn("return new CmnTemplateService(provider, renderer);", wiring)
        self.assertIn("CmnTemplateStore cmnTemplateStore", wiring)
        self.assertIn("return new CmnJdbcTemplateStore(dataSource);", wiring)
        self.assertIn("findActive", store)
        self.assertIn('CACHE_NAME = "commonTemplate"', management)
        self.assertIn("refresher.refresh(cache);", listener)
        self.assertIn("repository.advanceCheckpoint", listener)

    def test_only_store_is_product_spring_persistence_provider(self):
        self.assertTrue((JAVA / "CmnTemplateStore.java").is_file())
        store = (JAVA / "CmnJdbcTemplateStore.java").read_text(encoding="utf-8")
        self.assertNotIn("@Repository", store)
        wiring = AUTOCONFIG.read_text(encoding="utf-8")
        self.assertIn("CmnTemplateStore cmnTemplateStore", wiring)
        self.assertIn("return new CmnJdbcTemplateStore(dataSource);", wiring)
        legacy = (JAVA / "CmnJdbcTemplateRepository.java").read_text(encoding="utf-8")
        self.assertIn("@Deprecated", legacy)
        self.assertNotIn("@Repository", legacy)
        self.assertNotIn("@Component", legacy)
        self.assertNotIn("@Service", legacy)


if __name__ == "__main__":
    unittest.main()
