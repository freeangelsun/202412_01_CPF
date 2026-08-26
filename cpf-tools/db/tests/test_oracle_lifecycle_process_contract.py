from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
INITIALIZER = ROOT / "cpf-tools/db/tools/initialize-cpf-database.ps1"
VENDOR_RUNNER = ROOT / "cpf-tools/db/tools/invoke-official-db-vendor-sql.ps1"


class OracleLifecycleProcessContractTest(unittest.TestCase):
    def test_powershell_runner_does_not_read_unowned_native_exit_code(self) -> None:
        source = INITIALIZER.read_text(encoding="utf-8-sig")
        branch = source.split("if ($selectedVendor -in @('postgresql','oracle'))", 1)[1].split(
            "$logicalToKey", 1
        )[0]
        self.assertNotIn("$LASTEXITCODE", branch)
        self.assertGreaterEqual(branch.count("if (-not $?)"), 4)

    def test_sqlplus_redirected_stdin_is_utf8_without_bom(self) -> None:
        source = VENDOR_RUNNER.read_text(encoding="utf-8-sig")
        self.assertIn("$psi.StandardInputEncoding=[Text.UTF8Encoding]::new($false)", source)
        self.assertNotIn("$psi.StandardInputEncoding=[Text.Encoding]::UTF8", source)
        self.assertIn("SP2-0734", source)


if __name__ == "__main__":
    unittest.main()
