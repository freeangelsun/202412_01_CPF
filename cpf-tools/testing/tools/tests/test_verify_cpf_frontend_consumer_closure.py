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
        for surface, prefix in (("cpf-admin", "adm"), ("cpf-biz-admin", "bza")):
            src = root / surface / "frontend/src"
            (src / "generated").mkdir(parents=True)
            (src / "features").mkdir()
            (src / "generated/cpf-operation-contract.ts").write_text(f'export type CpfOperationId = "{prefix}Good";', encoding="utf-8")
            (src / "features/helper.ts").write_text("export const value=1;", encoding="utf-8")
            (src / "features/Page.ts").write_text(f'import {{value}} from "./helper"; {prefix}InvokeOperation("{prefix}Good");', encoding="utf-8")
        return root

    def test_positive(self):
        self.assertEqual("PASS", gate.verify(self.fixture())["status"])

    def test_missing_import_fails(self):
        root = self.fixture()
        (root / "cpf-admin/frontend/src/features/Page.ts").write_text('import x from "./missing";', encoding="utf-8")
        self.assertIn("MISSING_RELATIVE_IMPORT", {item["type"] for item in gate.verify(root)["findings"]})

    def test_unknown_operation_fails(self):
        root = self.fixture()
        (root / "cpf-biz-admin/frontend/src/features/Page.ts").write_text('bzaInvokeOperation("bzaMissing");', encoding="utf-8")
        self.assertIn("UNKNOWN_OPERATION_ID", {item["type"] for item in gate.verify(root)["findings"]})

    def test_native_confirmation_fails(self):
        root = self.fixture()
        (root / "cpf-admin/frontend/src/features/Page.ts").write_text('window.confirm("x")', encoding="utf-8")
        self.assertIn("BROWSER_NATIVE_DANGEROUS_CONFIRMATION", {item["type"] for item in gate.verify(root)["findings"]})

if __name__ == "__main__":
    unittest.main()
