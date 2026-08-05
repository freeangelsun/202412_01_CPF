from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
MODULE_PATH = ROOT / "cpf-tools/scripts/verify-cpf-db-development-contract.py"
SPEC = importlib.util.spec_from_file_location("verify_cpf_db_development_contract", MODULE_PATH)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = MODULE
SPEC.loader.exec_module(MODULE)


class DbDevelopmentStaticContractTest(unittest.TestCase):
    def test_all_owned_db_scripts_have_balanced_static_structure(self):
        failures, summary = MODULE.verify(ROOT)
        self.assertEqual([], failures)
        self.assertEqual("PASS", summary["status"])
        self.assertEqual("STATIC_SUBSTITUTE_ONLY", summary["runtimeClaim"])
        self.assertEqual(len(MODULE.DB_SCRIPTS), summary["checkedPowerShellFiles"])

    def test_unterminated_string_is_detected(self):
        with self.assertRaises(ValueError):
            MODULE.strip_powershell_non_code("Write-Host 'unterminated")

    def test_mismatched_delimiter_is_detected(self):
        import tempfile
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "bad.ps1"
            path.write_text("if($true){ Write-Host 'x' ) }", encoding="utf-8")
            failures = MODULE.check_balanced_powershell(path)
            self.assertTrue(failures)


if __name__ == "__main__":
    unittest.main()
