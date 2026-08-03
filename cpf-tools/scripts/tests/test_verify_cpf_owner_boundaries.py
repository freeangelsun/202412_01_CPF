#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

MODULE_PATH = Path(__file__).resolve().parents[1] / "verify-cpf-owner-boundaries.py"
spec = importlib.util.spec_from_file_location("owner_gate", MODULE_PATH)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class OwnerBoundaryTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / "cpf-core/src/main/java").mkdir(parents=True)
        (self.root / "cpf-core/build.gradle").write_text("plugins {}\n", encoding="utf-8")
        (self.root / "cpf-admin/src/main/java/com/cpf/admin").mkdir(parents=True)
        (self.root / "cpf-admin/src/main/java/com/cpf/admin/Admin.java").write_text("class Admin {}\n", encoding="utf-8")
        for component in module.BATCH_COMPONENTS:
            base = self.root / "cpf-batch" / component
            (base / "src/main/java/com/cpf/batch").mkdir(parents=True)
            (base / "build.gradle").write_text("plugins {}\n", encoding="utf-8")
            (base / "src/main/java/com/cpf/batch/Runtime.java").write_text("class Runtime {}\n", encoding="utf-8")

    def tearDown(self) -> None:
        self.temp.cleanup()

    def assert_gate_error(self, fragment: str) -> None:
        with self.assertRaises(module.GateError) as caught:
            module.validate(self.root)
        self.assertIn(fragment, str(caught.exception))

    def test_valid_owner_layout_passes(self) -> None:
        result = module.validate(self.root)
        self.assertEqual("PASS", result["status"])
        self.assertEqual(8, result["batch_component_count"])

    def test_missing_execution_runtime_is_not_skipped(self) -> None:
        import shutil
        shutil.rmtree(self.root / "cpf-batch/execution-runtime")
        self.assert_gate_error("execution-runtime owner root")

    def test_admin_cross_owner_db_access_fails(self) -> None:
        target = self.root / "cpf-admin/src/main/java/com/cpf/admin/Admin.java"
        target.write_text('class Admin { String sql = "SELECT * FROM bat_job"; }\n', encoding="utf-8")
        self.assert_gate_error("ADM cross-owner DB access")

    def test_batch_core_runtime_import_fails(self) -> None:
        target = self.root / "cpf-batch/worker/src/main/java/com/cpf/batch/Runtime.java"
        target.write_text("import com.cpf.core.common.batch.CpfBatchLauncher;\nclass Runtime {}\n", encoding="utf-8")
        self.assert_gate_error("Core-owned runtime compatibility")

    def test_core_batch_dependency_fails(self) -> None:
        (self.root / "cpf-core/build.gradle").write_text("implementation project(':cpf-batch:worker')\n", encoding="utf-8")
        self.assert_gate_error("cpf-core must not depend on cpf-batch")


if __name__ == "__main__":
    unittest.main()
