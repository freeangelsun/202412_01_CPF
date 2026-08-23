from __future__ import annotations

import json
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
CONTRACT = ROOT / "cpf-tools/db/metadata/platform-runtime-query-contract.json"
SYNC = ROOT / "cpf-tools/runtime/tools/sync-platform-runtime-query-packs.ps1"


class RuntimeQueryModuleContractCurrentizationTest(unittest.TestCase):
    def test_required_module_inventory_matches_current_contract_exactly(self):
        contract = json.loads(CONTRACT.read_text(encoding="utf-8-sig"))
        required = ["cpf", "backoffice", "cmn"]
        self.assertEqual(required, contract["requiredModules"])
        self.assertEqual(required, [module["module"] for module in contract["modules"]])
        self.assertNotIn("ref", required)
        self.assertNotIn("education", required)

    def test_sync_uses_contract_inventory_instead_of_retired_hardcode(self):
        source = SYNC.read_text(encoding="utf-8-sig")
        self.assertIn("$contract.requiredModules", source)
        self.assertIn("($actualModules -join", source)
        self.assertNotIn("$modules.Count -ne 3", source)
        self.assertNotIn('"ref" -notin', source)


if __name__ == "__main__":
    unittest.main()
