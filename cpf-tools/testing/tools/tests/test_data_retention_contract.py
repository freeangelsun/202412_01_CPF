from __future__ import annotations

import importlib.util
import json
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
GENERATOR_PATH = ROOT / "cpf-tools/db/tools/generate-cpf-data-retention-sql.py"
SPEC = importlib.util.spec_from_file_location("generate_cpf_data_retention_sql", GENERATOR_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)
POLICY = json.loads((ROOT / "cpf-tools/db/cpf-data-retention-policy.json").read_text(encoding="utf-8"))
SCHEMA = json.loads((ROOT / "cpf-tools/db/canonical/platform-schema.json").read_text(encoding="utf-8"))
TARGET = POLICY["policies"][0]
SOURCE_COLUMNS, ARCHIVE_COLUMNS = MODULE.resolve_archive_contract(SCHEMA, TARGET)


class DataRetentionContractTest(unittest.TestCase):
    def test_policy_requires_archive_legal_hold_plan_hash_unknown(self):
        controls = POLICY["executionControls"]
        for key in [
            "planFirst",
            "expectedPlanSha256Required",
            "archiveBeforePurge",
            "unknownOnPartialFailure",
            "legalHoldOverridesRetention",
            "sanitizedEvidenceOnly",
        ]:
            self.assertTrue(controls[key])

    def test_canonical_archive_columns_are_source_plus_audit_metadata(self):
        self.assertEqual(14, len(SOURCE_COLUMNS))
        self.assertEqual(17, len(ARCHIVE_COLUMNS))
        self.assertEqual(SOURCE_COLUMNS, ARCHIVE_COLUMNS[: len(SOURCE_COLUMNS)])
        self.assertEqual(["archived_at", "archived_by", "archive_reason"], ARCHIVE_COLUMNS[-3:])

    def test_three_vendor_sql_is_bounded_held_archived_and_delete_guarded(self):
        for vendor in ["mariadb", "postgresql", "oracle"]:
            sql = MODULE.generate(
                vendor,
                TARGET,
                ["hold-1", "hold'2"],
                "2026-08-05T00:00:00+00:00",
                SOURCE_COLUMNS,
                ARCHIVE_COLUMNS,
                "operator-1",
                "approved retention run",
            )
            self.assertIn("10000", sql)
            self.assertIn("NOT IN", sql)
            self.assertIn("INSERT INTO bat_operation_log_archive (", sql)
            self.assertIn("archived_at, archived_by, archive_reason", sql)
            self.assertNotIn("SELECT s.*", sql)
            self.assertNotIn("INSERT IGNORE", sql)
            self.assertIn("EXISTS (SELECT 1 FROM bat_operation_log_archive", sql)
            self.assertIn("operator-1", sql)
            self.assertIn("approved retention run", sql)
            self.assertTrue("COMMIT" in sql.upper())

    def test_duplicate_hold_keys_are_rejected_by_main_contract(self):
        required = POLICY["legalHoldManifest"]["requiredFields"]
        self.assertIn("holdKeys", required)
        self.assertTrue(POLICY["legalHoldManifest"]["holdKeysMustBeUnique"])

    def test_wrapper_requires_plan_hash_archive_confirmation_and_unknown(self):
        text = (ROOT / "cpf-tools/verification/tools/invoke-cpf-data-retention.ps1").read_text(encoding="utf-8")
        for token in [
            "ExpectedPlanSha256",
            "ConfirmArchiveBeforePurge",
            "$result.status='UNKNOWN'",
            "$result.reconcileRequired=$true",
            "--operator",
            "--reason",
        ]:
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
