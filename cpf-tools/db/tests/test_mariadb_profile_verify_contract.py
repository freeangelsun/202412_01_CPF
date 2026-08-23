import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class MariaDbProfileVerifyContractTest(unittest.TestCase):
    def test_baseline_and_verify_are_profile_resolved_current_contracts(self):
        installer = (ROOT / "cpf-tools/db/tools/initialize-cpf-database.ps1").read_text(encoding="utf-8-sig")
        self.assertIn("OPS_SCHEMA_INSTALLATION", installer)
        self.assertNotRegex(installer, r"(?i)\bcpf_schema_installation\b")
        self.assertIn("$physicalOwnerTargets", installer)
        self.assertIn("Get-ModuleSql $verifyFile $target.logicalDatabase", installer)
        self.assertIn("$target.databaseName", installer)
        self.assertNotIn("Get-Content -LiteralPath $verifyFile -Raw", installer)

    def test_generated_mariadb_verify_has_no_retired_split_database_query(self):
        verify = (ROOT / "cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql").read_text(encoding="utf-8-sig")
        self.assertIn("-- CPF_LOGICAL_DATABASE=cpfDB", verify)
        self.assertIn("-- CPF_LOGICAL_DATABASE=mbwDB", verify)
        self.assertIn("table_schema = DATABASE()", verify)
        self.assertNotRegex(verify, r"(?i)\bFROM\s+(?:cpfDB|cmnDB|admDB|batDB|refDB)\.")
        for current_table in (
            "CMN_CODE", "CMN_MESSAGE", "CMN_RESPONSE_CODE", "CMN_PARAMETER",
            "ADM_ROLE", "ADM_MENU", "ADM_API_PERMISSION", "MBW_ROLE", "MBW_MENU", "MBW_PERMISSION",
        ):
            self.assertIn(current_table, verify)
        for retained_check in (
            "runtime_transaction_id_contract", "table_engine_collation", "response_code_http_status",
            "removed_stale_tables_absent", "adm_contact_ownership", "login_operation_contract",
            "bat_spring_batch_6_sequence_contract",
        ):
            self.assertIn(retained_check, verify)

    def test_current_sequence_projection_is_separate_from_immutable_history(self):
        contract = json.loads(
            (ROOT / "cpf-tools/db/canonical/platform-non-table-objects.json").read_text(encoding="utf-8")
        )
        self.assertEqual("cpfDB", contract["canonicalPolicy"]["currentLogicalDatabase"])
        self.assertEqual("10_cpf_schema.sql", contract["canonicalPolicy"]["currentSourceFile"])
        self.assertTrue(contract["canonicalPolicy"]["historicalMigrationImmutable"])
        self.assertTrue(all(item["logicalDatabase"] == "batDB" for item in contract["objects"]))
        current = (ROOT / "cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql").read_text(encoding="utf-8-sig")
        retired_source = (ROOT / "cpf-tools/db/vendor/mariadb/source/35_bat_schema.sql").read_text(encoding="utf-8-sig")
        for sequence in ("BAT_SB_JOB_INSTANCE_SEQ", "BAT_SB_JOB_EXECUTION_SEQ", "BAT_SB_STEP_EXECUTION_SEQ"):
            self.assertRegex(current, rf"(?i)CREATE\s+SEQUENCE\s+IF\s+NOT\s+EXISTS\s+{sequence}\b")
        for retired_sequence in ("BATCH_JOB_INSTANCE_SEQ", "BATCH_JOB_EXECUTION_SEQ", "BATCH_STEP_EXECUTION_SEQ"):
            self.assertNotRegex(current, rf"(?i)CREATE\s+SEQUENCE\s+IF\s+NOT\s+EXISTS\s+{retired_sequence}\b")
        self.assertNotIn("CPF_CANONICAL_OBJECTS_BEGIN spring-batch-6-sequences", retired_source)

    def test_product_seed_verify_is_projected_from_exact_canonical_business_keys(self):
        generator = (ROOT / "cpf-tools/db/generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("Get-CanonicalProductSeedKeyContract", generator)
        self.assertIn("Canonical product-seed key must be a deterministic SQL literal", generator)

        for vendor in ("mariadb", "postgresql", "oracle"):
            verify = (ROOT / f"cpf-tools/db/vendor/{vendor}/source/00_verify.sql").read_text(
                encoding="utf-8-sig"
            )
            self.assertIn("response_code_http_status", verify)
            self.assertIn("NOT BETWEEN 100 AND 599", verify)
            self.assertNotIn("CMN_RESPONSE_CODE) >= 40", verify)
            self.assertNotIn("CMN_MESSAGE) >= 40", verify)
            for expected_count in (121, 37, 35, 24):
                self.assertRegex(verify, rf"\) = {expected_count}(?:\s|,)")
            for canonical_key in (
                "'CODE_GROUP'",
                "'SORT_DIRECTION'",
                "'MCPF000000'",
                "'SCPF000000'",
                "'CPF.CMN.CACHE.PRELOAD_ENABLED'",
            ):
                self.assertIn(canonical_key, verify)


if __name__ == "__main__":
    unittest.main()
