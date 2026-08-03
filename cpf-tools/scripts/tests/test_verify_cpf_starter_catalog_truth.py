#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import json
import shutil
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "verify-cpf-starter-catalog-truth.py"
spec = importlib.util.spec_from_file_location("starter_truth", MODULE_PATH)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)

SOURCE_ROOT = Path(__file__).resolve().parents[3]


class StarterCatalogTruthTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        for relative in (
            "settings.gradle",
            "cpf-tools/generator/contracts/cpf-starter-catalog.json",
            "cpf-tools/generator/contracts/capability-profiles.json",
            "cpf-tools/build/platform-bom/public-bom/build.gradle",
        ):
            source = SOURCE_ROOT / relative
            target = self.root / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)

    def tearDown(self) -> None:
        self.temp.cleanup()

    def read_json(self, relative: str) -> dict:
        return json.loads((self.root / relative).read_text(encoding="utf-8"))

    def write_json(self, relative: str, value: dict) -> None:
        (self.root / relative).write_text(
            json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )

    def assert_gate_error(self, fragment: str) -> None:
        with self.assertRaises(module.GateError) as caught:
            module.validate(self.root, metadata_only=True)
        self.assertIn(fragment, str(caught.exception))

    def test_valid_canonical_derivatives_pass(self) -> None:
        result = module.validate(self.root, metadata_only=True)
        self.assertEqual("PASS", result["status"])
        self.assertEqual(38, result["module_count"])
        self.assertEqual(6, result["public_profile_count"])
        self.assertEqual(32, result["internal_module_count"])
        self.assertEqual(7, result["capability_group_count"])

    def test_provider_coordinate_drift_fails(self) -> None:
        path = "cpf-tools/generator/contracts/capability-profiles.json"
        data = self.read_json(path)
        data["providerSlots"]["notification"]["sms-spi"]["coordinate"] = (
            "com.cpf.notification:cpf-starter-notification-sms-spi"
        )
        self.write_json(path, data)
        self.assert_gate_error("profiles providerSlots drift")

    def test_runtime_composition_omission_fails(self) -> None:
        path = "cpf-tools/generator/contracts/capability-profiles.json"
        data = self.read_json(path)
        item = data["capabilityComposition"]["integration"]
        item["runtimeProjects"].remove(":cpf-starter-integration-resilience")
        item["runtimeCoordinates"].remove("com.cpf.starter:cpf-starter-integration-resilience")
        self.write_json(path, data)
        self.assert_gate_error("profiles capabilityComposition drift")

    def test_internal_literal_public_bom_leak_fails(self) -> None:
        catalog = self.read_json("cpf-tools/generator/contracts/cpf-starter-catalog.json")
        internal = next(item["artifactId"] for item in catalog["modules"] if item["visibility"] == "internal")
        bom_path = self.root / "cpf-tools/build/platform-bom/public-bom/build.gradle"
        bom_path.write_text(bom_path.read_text(encoding="utf-8") + f'\n"com.cpf.starter:{internal}"\n', encoding="utf-8")
        self.assert_gate_error("internal starter literal leaked")

    def test_settings_without_catalog_consumer_fails(self) -> None:
        path = self.root / "settings.gradle"
        path.write_text(path.read_text(encoding="utf-8").replace("cpf-starter-catalog.json", "missing.json"), encoding="utf-8")
        self.assert_gate_error("does not consume canonical starter catalog")


if __name__ == "__main__":
    unittest.main()
