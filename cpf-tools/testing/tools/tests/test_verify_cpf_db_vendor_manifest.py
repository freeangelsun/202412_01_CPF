#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import json
import shutil
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[4] / "cpf-tools/db/verification/verify-cpf-db-vendor-manifest.py"
spec = importlib.util.spec_from_file_location("db_vendor_gate", MODULE_PATH)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)

SOURCE_MANIFEST = Path(__file__).resolve().parents[4] / "cpf-tools/db/vendor-pack-manifest.json"


class DbVendorManifestTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        target = self.root / "cpf-tools/db/vendor-pack-manifest.json"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(SOURCE_MANIFEST, target)
        self._materialize_paths()

    def tearDown(self) -> None:
        self.temp.cleanup()

    @property
    def manifest_path(self) -> Path:
        return self.root / "cpf-tools/db/vendor-pack-manifest.json"

    def read_manifest(self) -> dict:
        return json.loads(self.manifest_path.read_text(encoding="utf-8"))

    def write_manifest(self, value: dict) -> None:
        self.manifest_path.write_text(
            json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )

    def _materialize_paths(self) -> None:
        data = self.read_manifest()
        for vendor, entry in data["vendors"].items():
            for key in ("vendorRoot", "runtimeRoot", "domainTemplateRoot", "historicalMigration", "historicalRollback", "runtimeDialectRoot"):
                (self.root / entry[key]).mkdir(parents=True, exist_ok=True)
            for key in ("generatedCurrent", "generatedDomainTemplate"):
                generated = self.root / entry[key]
                generated.parent.mkdir(parents=True, exist_ok=True)
                generated.write_text("{}\n", encoding="utf-8")
            for key, raw in entry["lifecycle"].items():
                if "{" in raw:
                    concrete = raw.split("/{", 1)[0]
                    (self.root / concrete).mkdir(parents=True, exist_ok=True)
                elif key in {"migration", "rollback"}:
                    (self.root / raw).mkdir(parents=True, exist_ok=True)
                else:
                    path = self.root / raw
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(f"-- {vendor} {key}\n", encoding="utf-8")
            pack = self.root / entry["pack"]
            pack.parent.mkdir(parents=True, exist_ok=True)
            pack.write_text("{}\n", encoding="utf-8")

    def assert_gate_error(self, fragment: str, *, metadata_only: bool = True) -> None:
        with self.assertRaises(module.GateError) as caught:
            module.validate(self.root, metadata_only)
        self.assertIn(fragment, str(caught.exception))

    def test_valid_exact_three_vendor_manifest_passes(self) -> None:
        result = module.validate(self.root, metadata_only=False)
        self.assertEqual("PASS", result["status"])
        self.assertEqual(["mariadb", "postgresql", "oracle"], result["official_vendors"])
        self.assertEqual(3, result["vendor_count"])
        self.assertEqual(51, result["checked_path_count"])

    def test_path_escape_fails(self) -> None:
        data = self.read_manifest()
        data["vendors"]["mariadb"]["runtimeRoot"] = "cpf-tools/db/vendor/mariadb/../oracle/runtime"
        self.write_manifest(data)
        self.assert_gate_error("escapes or is not canonical")

    def test_non_official_vendor_set_fails(self) -> None:
        data = self.read_manifest()
        data["supportedVendors"] = ["mariadb", "postgresql", "oracle", "mysql"]
        self.write_manifest(data)
        self.assert_gate_error("supportedVendors must be exactly")

    def test_missing_required_directory_fails_closed(self) -> None:
        shutil.rmtree(self.root / "cpf-tools/db/vendor/oracle/runtime")
        self.assert_gate_error("required DB directory missing", metadata_only=False)

    def test_fixed_generated_domain_list_fails(self) -> None:
        data = self.read_manifest()
        data["generatedDomainRegistration"]["fixedDomainList"] = True
        self.write_manifest(data)
        self.assert_gate_error("without a fixed domain list")

    def test_source_tree_mutation_policy_fails(self) -> None:
        data = self.read_manifest()
        data["selectionPolicy"]["sourceTreeMutation"] = True
        self.write_manifest(data)
        self.assert_gate_error("prohibit source mutation")

    def test_unknown_vendor_status_fails(self) -> None:
        data = self.read_manifest()
        data["vendors"]["oracle"]["status"] = "PASS"
        self.write_manifest(data)
        self.assert_gate_error("invalid vendor status")


if __name__ == "__main__":
    unittest.main()
