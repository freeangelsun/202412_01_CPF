from __future__ import annotations
import importlib.util,shutil,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-bza-route-contract.py";spec=importlib.util.spec_from_file_location('bza_route',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(module)
class BzaRouteContractTest(unittest.TestCase):
    def test_real_overlay(self):self.assertEqual(27,module.validate(Path(__file__).parents[4])['routes'])
    def test_dashboard_fallback_rejected(self):
        root=Path(__file__).parents[4]
        with tempfile.TemporaryDirectory() as tmp:
            target=Path(tmp);shutil.copytree(root/'cpf-biz-admin',target/'cpf-biz-admin')
            p=target/'cpf-biz-admin/frontend/src/App.vue';p.write_text(p.read_text(encoding="utf-8")+'\n<!-- allowed[0] -->', encoding="utf-8")
            with self.assertRaises(module.ContractError):module.validate(target)
if __name__=='__main__':unittest.main()
