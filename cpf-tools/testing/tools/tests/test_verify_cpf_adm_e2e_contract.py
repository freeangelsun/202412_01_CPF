from __future__ import annotations
import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-adm-e2e-contract.py"
spec=importlib.util.spec_from_file_location('adm_e2e_contract',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(module)
class AdmE2eContractTest(unittest.TestCase):
    def test_real_overlay(self):
        root=Path(__file__).parents[4]
        result=module.validate(root)
        routes_file=root/"cpf-admin/frontend/src/app/routes.ts"
        expected=len(module.ENTRY.findall(routes_file.read_text(encoding="utf-8")))
        self.assertGreater(expected,0)
        self.assertEqual(expected,result['routes'])
    def test_missing_browser_rejected(self):
        root=Path(__file__).parents[4]
        with tempfile.TemporaryDirectory() as temp:
            target=Path(temp)
            import shutil
            shutil.copytree(root/'cpf-admin',target/'cpf-admin')
            shutil.copytree(root/'cpf-docs',target/'cpf-docs')
            p=target/'cpf-admin/frontend/playwright.config.ts';p.write_text(p.read_text(encoding="utf-8").replace('name: "webkit"','name: "webkit-disabled"'), encoding="utf-8")
            with self.assertRaises(module.ContractError):module.validate(target)
if __name__=='__main__':unittest.main()
