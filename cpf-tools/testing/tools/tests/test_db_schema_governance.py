from __future__ import annotations

import copy
import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
MODULE_PATH = ROOT / "cpf-tools/db/verification/verify-cpf-db-schema-governance.py"
SPEC = importlib.util.spec_from_file_location("verify_cpf_db_schema_governance", MODULE_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)
SCHEMA_PATH = ROOT / "cpf-tools/db/canonical/platform-schema.json"


class DbSchemaGovernanceTest(unittest.TestCase):
    def test_canonical_schema_ownership_and_references_are_valid(self):
        failures, summary = MODULE.verify(SCHEMA_PATH)
        self.assertEqual([], failures)
        self.assertEqual("PASS", summary["status"])
        self.assertEqual(len(json.loads(SCHEMA_PATH.read_text(encoding="utf-8"))["tables"]), summary["tableCount"])
        self.assertEqual("STATIC_METADATA_ONLY", summary["runtimeClaim"])
        self.assertGreater(summary["foreignKeyCount"], 0)

    def test_prefix_length_index_column_maps_to_canonical_column(self):
        self.assertEqual("duplicate_key", MODULE.normalized_identifier("duplicate_key(255)"))

    def test_case_insensitive_index_column_maps_to_canonical_column(self):
        self.assertEqual("transaction_id", MODULE.normalized_identifier("TRANSACTION_ID"))

    def test_cross_logical_database_foreign_key_is_rejected(self):
        schema = json.loads(SCHEMA_PATH.read_text(encoding="utf-8-sig"))
        mutated = copy.deepcopy(schema)
        target = next(table for table in mutated["tables"] if table.get("foreignKeys"))
        target["logicalDatabase"] = "refDB" if target["logicalDatabase"] != "refDB" else "cpfDB"
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "schema.json"
            path.write_text(json.dumps(mutated), encoding="utf-8")
            failures, _ = MODULE.verify(path)
        self.assertTrue(any("cross-logical-database" in finding.message for finding in failures))

    def test_missing_index_column_is_rejected(self):
        schema = json.loads(SCHEMA_PATH.read_text(encoding="utf-8-sig"))
        mutated = copy.deepcopy(schema)
        target = next(table for table in mutated["tables"] if table.get("indexes"))
        target["indexes"][0]["columns"][0] = "__MISSING_COLUMN__"
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "schema.json"
            path.write_text(json.dumps(mutated), encoding="utf-8")
            failures, _ = MODULE.verify(path)
        self.assertTrue(any("missing column" in finding.message for finding in failures))

    def test_missing_required_column_shape_is_rejected(self):
        schema = json.loads(SCHEMA_PATH.read_text(encoding="utf-8-sig"))
        mutated = copy.deepcopy(schema)
        del mutated["tables"][0]["columns"][0]["autoIncrement"]
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "schema.json"
            path.write_text(json.dumps(mutated), encoding="utf-8")
            failures, _ = MODULE.verify(path)
        self.assertTrue(any("missing required fields" in finding.message for finding in failures))

    def test_missing_required_foreign_key_shape_is_rejected(self):
        schema = json.loads(SCHEMA_PATH.read_text(encoding="utf-8-sig"))
        mutated = copy.deepcopy(schema)
        target = next(table for table in mutated["tables"] if table.get("foreignKeys"))
        del target["foreignKeys"][0]["onDelete"]
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "schema.json"
            path.write_text(json.dumps(mutated), encoding="utf-8")
            failures, _ = MODULE.verify(path)
        self.assertTrue(any("foreign key is missing required fields" in finding.message for finding in failures))


if __name__ == "__main__":
    unittest.main()
