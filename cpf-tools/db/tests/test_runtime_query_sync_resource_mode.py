from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SYNC = ROOT / "cpf-tools/runtime/tools/sync-platform-runtime-query-packs.ps1"


class RuntimeQuerySyncResourceModeTest(unittest.TestCase):
    def test_sync_has_fail_closed_portable_resource_dispatch(self):
        source = SYNC.read_text(encoding="utf-8-sig")
        self.assertIn('$resourceStatements = @($statements | Where-Object', source)
        self.assertIn("module mixes repository keys and portable resources", source)
        self.assertIn("Portable Platform Runtime authoring/contract mismatch", source)
        self.assertIn("Assert-StatementParameters -Module $module -Statement $statement -Vendor \"portable\"", source)
        self.assertIn("WriteAllBytes", source)
        self.assertIn("Portable Platform Runtime target parity mismatch", source)
        self.assertIn('$module.PSObject.Properties.Name -contains "managedMyBatis"', source)


if __name__ == "__main__":
    unittest.main()
