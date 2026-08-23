from __future__ import annotations

import subprocess
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CHECK = ROOT / "cpf-tools/verification/tools/check-data-safety-schema-contract.ps1"


class DataSafetySchemaContractCurrentizationTest(unittest.TestCase):
    def test_current_contract_uses_mbw_and_independent_history_evidence(self):
        source = CHECK.read_text(encoding="utf-8-sig")
        self.assertIn('$backofficeSchema = Require-File', source)
        self.assertIn("uk_mbw_admin_user_create_operation", source)
        self.assertIn("CREATE TABLE IF NOT EXISTS\\s+MBW_LOGIN_OPERATION", source)
        self.assertIn("Require-ManifestHash $sourceArchive $sourceMigrationManifest", source)
        self.assertIn("Require-ManifestHash $lifecycleHistory $lifecycleMigrationManifest", source)
        self.assertIn("Require-PublishedHash $lifecycleHistory", source)
        self.assertNotIn("Require-SameHash", source)
        self.assertNotIn("Require-Contains $bzaSchema", source)

    def test_actual_contract_gate_passes(self):
        result = subprocess.run(
            ["pwsh", "-NoProfile", "-File", str(CHECK), "-Root", str(ROOT)],
            cwd=ROOT,
            text=True,
            capture_output=True,
            timeout=60,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn("CPF data-safety schema contract: PASS_STATIC_ONLY", result.stdout)


if __name__ == "__main__":
    unittest.main()
