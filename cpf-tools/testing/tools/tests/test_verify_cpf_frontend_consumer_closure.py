from __future__ import annotations
import importlib.util, tempfile, unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py"
spec = importlib.util.spec_from_file_location("gate", SCRIPT)
gate = importlib.util.module_from_spec(spec)
spec.loader.exec_module(gate)

class GateTest(unittest.TestCase):
    def fixture(self) -> Path:
        root = Path(tempfile.mkdtemp())
        adm = root / "cpf-admin/frontend/src"
        (adm / "generated").mkdir(parents=True)
        (adm / "features").mkdir()
        (adm / "generated/cpf-operation-contract.ts").write_text('export type CpfOperationId = "admGood";', encoding="utf-8")
        (adm / "features/helper.ts").write_text("export const value=1;", encoding="utf-8")
        (adm / "features/Page.ts").write_text('import {value} from "./helper"; admInvokeOperation("admGood");', encoding="utf-8")
        bza = root / "cpf-backoffice-web/frontend/src"
        (bza / "generated").mkdir(parents=True)
        (bza / "shared/api").mkdir(parents=True)
        (bza / "features").mkdir()
        (bza / "generated/backoffice-api.ts").write_text('/* AUTO-GENERATED */ import { invokeBackoffice } from "../shared/api/channelHttpClient";', encoding="utf-8")
        (bza / "shared/api/channelHttpClient.ts").write_text('const x="VITE_MBW_WEB_BASE_URL"; export function invokeBackoffice(){}', encoding="utf-8")
        (bza / "features/Page.ts").write_text('import { invokeBackoffice } from "../generated/backoffice-api"; invokeBackoffice();', encoding="utf-8")
        return root

    def test_positive(self):
        self.assertEqual("PASS", gate.verify(self.fixture())["status"])

    def test_missing_import_fails(self):
        root = self.fixture()
        (root / "cpf-admin/frontend/src/features/Page.ts").write_text('import x from "./missing";', encoding="utf-8")
        self.assertIn("MISSING_RELATIVE_IMPORT", {item["type"] for item in gate.verify(root)["findings"]})

    def test_native_confirmation_fails(self):
        root = self.fixture()
        (root / "cpf-admin/frontend/src/features/Page.ts").write_text('window.confirm("x")', encoding="utf-8")
        self.assertIn("BROWSER_NATIVE_DANGEROUS_CONFIRMATION", {item["type"] for item in gate.verify(root)["findings"]})

if __name__ == "__main__":
    unittest.main()
