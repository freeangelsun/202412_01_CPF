from __future__ import annotations

import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
DB = ROOT / "cpf-tools/db"
DESCRIPTION = "generic_runtime_application_role"


class V137GenericRuntimeApplicationRoleTest(unittest.TestCase):
    def paths(self, vendor: str) -> tuple[Path, Path]:
        logical = Path() if vendor == "mariadb" else Path("cpfDB")
        return (
            DB / f"vendor/{vendor}/migration/flyway" / logical / f"V137__{DESCRIPTION}.sql",
            DB / f"vendor/{vendor}/rollback" / logical / f"R137__{DESCRIPTION}.sql",
        )

    def test_shared_runtime_accepts_application_while_batch_roles_stay_closed(self) -> None:
        schema = json.loads((DB / "canonical/platform-schema.json").read_text(encoding="utf-8"))
        tables = {table["targetTableName"]: table for table in schema["tables"]}

        ops = next(
            check
            for check in tables["OPS_RUNTIME_INSTANCE_STATE"]["checks"]
            if check["name"] == "ck_ops_runtime_instance_role"
        )
        self.assertIn("'APPLICATION'", ops["expression"])
        self.assertIn("'APPLICATION'", ops["vendorExpressions"]["mariadb"])

        for table_name, check_name in (
            ("BAT_DEPLOYMENT_CELL", "ck_bat_deployment_runtime_role"),
            ("BAT_RUNTIME_INSTANCE", "ck_bat_runtime_instance_role"),
        ):
            check = next(item for item in tables[table_name]["checks"] if item["name"] == check_name)
            self.assertNotIn("APPLICATION", check["expression"])
            self.assertNotIn("APPLICATION", check["vendorExpressions"]["mariadb"])

    def test_all_vendors_publish_fail_closed_upgrade_and_rollback(self) -> None:
        for vendor in ("mariadb", "postgresql", "oracle"):
            upgrade_path, rollback_path = self.paths(vendor)
            self.assertTrue(upgrade_path.is_file(), vendor)
            self.assertTrue(rollback_path.is_file(), vendor)
            upgrade = upgrade_path.read_text(encoding="utf-8-sig")
            rollback = rollback_path.read_text(encoding="utf-8-sig")
            self.assertIn("OPS_RUNTIME_INSTANCE_STATE", upgrade)
            self.assertIn("'APPLICATION'", upgrade)
            self.assertIn("OPS_RUNTIME_INSTANCE_STATE", rollback)
            self.assertIn("'APPLICATION'", rollback)
            self.assertRegex(rollback, r"(?i)(invalid_count|RAISE(?:_APPLICATION_ERROR|\s+EXCEPTION))")
            self.assertRegex(
                rollback,
                r"(?s)CHECK\s*\([^)]*runtime_role.*?'CONTROL_PLANE'.*?'AGENT'",
            )

    def test_version_and_mariadb_publication_routing_are_registered(self) -> None:
        catalog = json.loads((DB / "canonical/migration-intent-catalog.json").read_text(encoding="utf-8"))
        intent = next(
            item
            for item in catalog["currentIntents"]
            if item["id"] == "D-013-GENERIC-RUNTIME-APPLICATION-ROLE"
        )
        self.assertEqual(137, intent["allocatedVersion"])

        routing = json.loads(
            (DB / "canonical/mariadb-historical-migration-routing.json").read_text(encoding="utf-8")
        )
        for prefix in ("V", "R"):
            name = f"{prefix}137__{DESCRIPTION}.sql"
            self.assertEqual(
                {"sections": [{"logicalDatabase": "cpfDB"}]},
                routing["files"].get(name),
                name,
            )


if __name__ == "__main__":
    unittest.main()
