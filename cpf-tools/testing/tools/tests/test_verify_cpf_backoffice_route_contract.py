from __future__ import annotations

import importlib.util
import shutil
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/tools/verify-cpf-backoffice-route-contract.py"
spec = importlib.util.spec_from_file_location("backoffice_route", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec.loader
spec.loader.exec_module(module)


class BackofficeRouteContractTest(unittest.TestCase):
    def test_real_tree(self):
        result = module.validate(ROOT)
        self.assertEqual(96, result["routes"])
        self.assertEqual(96, result["operations"])

    def test_catalog_drift_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            target = Path(tmp)
            for rel in ("cpf-backoffice", "cpf-backoffice-web"):
                shutil.copytree(ROOT / rel, target / rel)
            path = target / "cpf-backoffice-web/src/main/resources/backoffice-routes.tsv"
            lines = path.read_text(encoding="utf-8").splitlines()
            path.write_text("\n".join(lines[:-1]) + "\n", encoding="utf-8")
            with self.assertRaises(module.ContractError):
                module.validate(target)

    def test_retired_root_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            target = Path(tmp)
            for rel in ("cpf-backoffice", "cpf-backoffice-web"):
                shutil.copytree(ROOT / rel, target / rel)
            (target / "cpf-biz-admin").mkdir()
            with self.assertRaises(module.ContractError):
                module.validate(target)


if __name__ == "__main__":
    unittest.main()
