from __future__ import annotations
import importlib.util,tempfile,unittest
from pathlib import Path
MODULE=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-adm-route-interaction-contract.py"
spec=importlib.util.spec_from_file_location("gate",MODULE);gate=importlib.util.module_from_spec(spec);spec.loader.exec_module(gate)
REAL_ROOT=Path(__file__).resolve().parents[4]

class RouteContractTest(unittest.TestCase):
    def test_real_overlay_contract(self):
        routes,caps,errors=gate.validate(REAL_ROOT)
        self.assertEqual([],errors)
        self.assertGreater(len(routes),0)
        self.assertEqual(80,len(caps))

    def test_empty_route_registry_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d)/"routes.ts";p.write_text("export const routes = {}\n",encoding="utf-8")
            with self.assertRaises(gate.ContractError): gate.read_routes(p)

    def test_duplicate_route_path_rejected(self):
        source=(REAL_ROOT/"cpf-admin/frontend/src/app/routes/operations.ts").read_text(encoding="utf-8")
        # Duplicate one known path while preserving route syntax.
        import re
        paths=re.findall(r'path: "([^"]+)"',source)
        self.assertGreaterEqual(len(paths),2)
        mutated=source.replace(f'path: "{paths[1]}"',f'path: "{paths[0]}"',1)
        with tempfile.TemporaryDirectory() as d:
            route_dir=Path(d)/"routes";route_dir.mkdir()
            (route_dir/"operations.ts").write_text(mutated,encoding="utf-8")
            with self.assertRaises(gate.ContractError): gate.read_routes(route_dir)

    def test_required_error_statuses_are_canonical(self):
        self.assertEqual(("401","403","404","409","429","500","503"),gate.REQUIRED_ERRORS)

if __name__=="__main__": unittest.main()
