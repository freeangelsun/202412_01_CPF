import copy
import importlib.util
import json
import subprocess
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/runtime/tools/sync_bat_runtime_roles.py"
CONTRACT = ROOT / "cpf-tools/runtime/metadata/bat-runtime-role-contract.json"


def load_module():
    spec = importlib.util.spec_from_file_location("sync_bat_runtime_roles", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


class BatRuntimeRoleContractTest(unittest.TestCase):
    def test_repository_has_exact_db3_and_deploy_role_parity(self):
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--root", str(ROOT)],
            text=True,
            capture_output=True,
            check=False,
        )
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        payload = json.loads(result.stdout)
        self.assertEqual(
            ["CONTROL_PLANE", "SCHEDULER", "WORKER", "CENTER_CUT_RUNNER", "AGENT"],
            payload["canonicalRoles"],
        )
        self.assertEqual(["mariadb", "postgresql", "oracle"], payload["vendors"])
        self.assertEqual(116, payload["migrationVersion"])

    def test_alias_collision_fails_closed_before_render(self):
        module = load_module()
        contract = json.loads(CONTRACT.read_text(encoding="utf-8"))
        broken = copy.deepcopy(contract)
        broken["roles"][1]["legacyAliases"] = ["HOST_AGENT"]
        with self.assertRaisesRegex(module.ContractError, "collision"):
            module.replacement_map(broken)

    def test_historical_v116_is_immutable_and_v138_owns_center_cut_runner_currentization(self):
        module = load_module()
        contract = json.loads(CONTRACT.read_text(encoding="utf-8"))
        self.assertTrue(contract["migration"]["historical"])
        self.assertTrue(contract["migration"]["historicalMigrationsImmutable"])
        self.assertEqual(138, contract["migration"]["supersededByVersion"])
        self.assertEqual("CEC-CENTER-CUT-RUNNER-IDENTITY", contract["migration"]["supersededByIntentId"])
        module.verify_migration_version_lock(ROOT, contract)
        current_paths = [
            ROOT / "cpf-tools/db/vendor/mariadb/migration/flyway/V138__center_cut_runner_identity.sql",
            ROOT / "cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V138__center_cut_runner_identity.sql",
            ROOT / "cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V138__center_cut_runner_identity.sql",
        ]
        for path in current_paths:
            sql = path.read_text(encoding="utf-8")
            self.assertIn("CENTER_CUT_RUNNER", sql)
            self.assertIn("CENTER_CUT", sql)
            self.assertIn("UPDATE", sql)
            self.assertIn("CONSTRAINT", sql)

    def test_role_identity_namespaces_and_version_lock_fail_closed(self):
        module = load_module()
        contract = json.loads(CONTRACT.read_text(encoding="utf-8"))
        duplicate = copy.deepcopy(contract)
        duplicate["roles"][1]["projectPath"] = duplicate["roles"][0]["projectPath"]
        with self.assertRaisesRegex(module.ContractError, "duplicate canonical project"):
            module.replacement_map(duplicate)
        module.verify_migration_version_lock(ROOT, contract)

    def test_all_executables_publish_role_through_shared_runtime_namespace(self):
        module = load_module()
        contract = json.loads(CONTRACT.read_text(encoding="utf-8"))
        module.verify_application_runtime_role_properties(ROOT, contract)

        broken = copy.deepcopy(contract)
        broken["roles"][2]["name"] = "APPLICATION"
        with self.assertRaisesRegex(module.ContractError, "shared cpf.runtime.role"):
            module.verify_application_runtime_role_properties(ROOT, broken)


if __name__ == "__main__":
    unittest.main()
