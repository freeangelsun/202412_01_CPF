from __future__ import annotations

import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest


class AdmRouteRegistryParserTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        root = Path(__file__).resolve().parents[4]
        script = root / "cpf-tools/verification/tools/verify-cpf-adm-route-source-consumers.py"
        spec = importlib.util.spec_from_file_location("cpf_adm_route_gate", script)
        assert spec and spec.loader
        module = importlib.util.module_from_spec(spec)
        sys.modules[spec.name] = module
        spec.loader.exec_module(module)
        cls.gate = module
        cls.root = root

    def test_current_registry_is_fully_parsed_without_a_stale_fixed_count(self) -> None:
        route_path = self.root / "cpf-admin/frontend/src/app/routes.ts"
        source = route_path.read_text(encoding="utf-8")
        declared = {match.group("id") for match in self.gate.ROUTE_REGISTRY_ENTRY.finditer(source)}
        routes = self.gate.read_routes(route_path)
        self.assertTrue(declared)
        self.assertEqual(declared, set(routes))
        self.assertIn("transactions", routes)
        self.assertIn("gateway-dashboard", routes)

    def test_unparsed_registry_entry_fails_closed(self) -> None:
        source = '''
export const admCapabilityRegistry = {
  "ok": { routeId: "ok", path: "/ok", menuId: "OK", expectedOperationIds: ["admOk"], component: defineAsyncComponent(() => import("../Ok.vue")) },
  "broken": { routeId: "broken", path: "/broken", menuId: "BROKEN", expectedOperationIds: ["admBroken"], component: unsupportedLoader("../Broken.vue") }
};
export function menuIdFromRouteName(name: unknown): string | undefined {
  return findCapabilityByRouteName(name)?.menuId;
}
'''
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "routes.ts"
            path.write_text(source, encoding="utf-8")
            with self.assertRaisesRegex(self.gate.ContractError, "parser/registry mismatch"):
                self.gate.read_routes(path)

    def test_route_id_projection_is_rejected(self) -> None:
        source = '''
export const admCapabilityRegistry = {
  "ok": { routeId: "ok", path: "/ok", menuId: "OK", expectedOperationIds: ["admOk"], component: defineAsyncComponent(() => import("../Ok.vue")) }
};
export function menuIdFromRouteName(name: unknown): string | undefined {
  return findCapabilityByRouteName(name)?.routeId;
}
'''
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "routes.ts"
            path.write_text(source, encoding="utf-8")
            with self.assertRaisesRegex(self.gate.ContractError, "backend menuId"):
                self.gate.read_routes(path)


if __name__ == "__main__":
    unittest.main()
