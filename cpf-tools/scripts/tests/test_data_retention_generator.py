from __future__ import annotations

import importlib.util
import json
import os
import tempfile
import unittest
from pathlib import Path

ROOT = Path(os.environ.get("CPF_REPO_ROOT", Path(__file__).resolve().parents[3])).resolve()
GENERATOR = ROOT / "cpf-tools/scripts/generate-cpf-data-retention-sql.py"
POLICY = ROOT / "cpf-tools/db/cpf-data-retention-policy.json"
SCHEMA = ROOT / "cpf-tools/db/canonical/platform-schema.json"


def load_module():
    spec = importlib.util.spec_from_file_location("cpf_retention_generator", GENERATOR)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Unable to load retention generator: {GENERATOR}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class DataRetentionGeneratorTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.module = load_module()
        cls.contract = json.loads(POLICY.read_text(encoding="utf-8-sig"))
        cls.schema = json.loads(SCHEMA.read_text(encoding="utf-8-sig"))
        cls.policy = cls.module.select_policy(cls.contract, "BAT_OPERATION_LOG_RETENTION_V1")
        cls.source_columns, cls.archive_columns = cls.module.resolve_archive_contract(cls.schema, cls.policy)

    def generate(self, vendor: str, keys=None):
        return self.module.generate(
            vendor,
            self.policy,
            list(keys or []),
            "2026-08-05T00:00:00+00:00",
            self.source_columns,
            self.archive_columns,
            "operator-1",
            "approved retention",
        )

    def test_all_official_vendors_archive_before_purge_and_are_transactional(self):
        for vendor in self.module.VENDORS:
            with self.subTest(vendor=vendor):
                sql = self.generate(vendor)
                self.assertLess(sql.index("INSERT INTO bat_operation_log_archive"), sql.index("DELETE"))
                self.assertIn("COMMIT", sql)
                self.assertIn("NOT EXISTS", sql)
                self.assertIn("EXISTS", sql)

    def test_legal_hold_keys_are_escaped_and_excluded(self):
        sql = self.generate("postgresql", ["id-1", "id'2"])
        self.assertIn("NOT IN ('id-1', 'id''2')", sql)

    def test_unknown_vendor_and_unsafe_identifiers_fail_closed(self):
        with self.assertRaises(ValueError):
            self.generate("mysql")
        with self.assertRaises(ValueError):
            self.module.ident("unsafe;drop")

    def test_archive_contract_is_exact_source_plus_audit_columns(self):
        self.assertEqual(
            self.archive_columns,
            self.source_columns + list(self.module.ARCHIVE_METADATA_COLUMNS),
        )

    def test_sql_is_deterministic_for_same_approved_inputs(self):
        for vendor in self.module.VENDORS:
            with self.subTest(vendor=vendor):
                self.assertEqual(self.generate(vendor, ["hold-1"]), self.generate(vendor, ["hold-1"]))


if __name__ == "__main__":
    unittest.main()
