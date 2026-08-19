from __future__ import annotations
import importlib.util,shutil,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-bza-route-contract.py";spec=importlib.util.spec_from_file_location('bza_route',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(module)
class BzaRouteContractTest(unittest.TestCase):
    def test_real_overlay(self):self.assertEqual(96,module.validate(Path(__file__).parents[4])['routes'])
    def test_dashboard_fallback_rejected(self):
        root=Path(__file__).parents[4]
        with tempfile.TemporaryDirectory() as tmp:
            target=Path(tmp)
            for rel in ('cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend'):
                shutil.copytree(root/rel,target/rel)
            p=target/'cpf-biz-channel/src/main/resources/bza-routes.tsv'
            lines=p.read_text(encoding="utf-8").splitlines(); p.write_text("\n".join(lines[:-1])+"\n",encoding="utf-8")
            with self.assertRaises(module.ContractError): module.validate(target)
if __name__=='__main__':unittest.main()
