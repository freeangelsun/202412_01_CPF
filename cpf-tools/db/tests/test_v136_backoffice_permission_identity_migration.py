from __future__ import annotations

import json
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
DB = ROOT / "cpf-tools/db"
EXPECTED_COLUMNS = "role_code, menu_code, button_code, permission_type, environment_code"


class V136BackofficePermissionIdentityMigrationTest(unittest.TestCase):
    def setUp(self) -> None:
        self.paths = {
            "mariadb": (
                DB / "vendor/mariadb/migration/flyway/V136__backoffice_permission_identity.sql",
                DB / "vendor/mariadb/rollback/R136__backoffice_permission_identity.sql",
            ),
            "postgresql": (
                DB / "vendor/postgresql/migration/flyway/mbwDB/V136__backoffice_permission_identity.sql",
                DB / "vendor/postgresql/rollback/mbwDB/R136__backoffice_permission_identity.sql",
            ),
            "oracle": (
                DB / "vendor/oracle/migration/flyway/mbwDB/V136__backoffice_permission_identity.sql",
                DB / "vendor/oracle/rollback/mbwDB/R136__backoffice_permission_identity.sql",
            ),
        }

    def test_all_official_vendors_publish_the_same_mbw_identity(self) -> None:
        for vendor, (upgrade, rollback) in self.paths.items():
            self.assertTrue(upgrade.is_file(), vendor)
            self.assertTrue(rollback.is_file(), vendor)
            upgrade_sql = upgrade.read_text(encoding="utf-8")
            rollback_sql = rollback.read_text(encoding="utf-8")
            self.assertIn("CPF_LOGICAL_DATABASE=mbwDB", upgrade_sql)
            self.assertIn("CPF_LOGICAL_DATABASE=mbwDB", rollback_sql)
            self.assertIn("ADD CONSTRAINT uk_mbw_permission_scope", upgrade_sql)
            self.assertIn(f"UNIQUE ({EXPECTED_COLUMNS})", upgrade_sql)
            self.assertIn("uk_mbw_permission_scope", rollback_sql)
            self.assertRegex(rollback_sql, r"(?i)\bDROP\s+(?:CONSTRAINT|INDEX)\b")

    def test_migration_identity_matches_canonical_schema(self) -> None:
        schema = json.loads((DB / "canonical/platform-schema.json").read_text(encoding="utf-8"))
        table = next(row for row in schema["tables"] if row["targetTableName"] == "MBW_PERMISSION")
        unique = next(row for row in table["uniqueKeys"] if row["name"] == "uk_mbw_permission_scope")
        self.assertEqual(EXPECTED_COLUMNS.split(", "), unique["columns"])

    def test_mariadb_immutable_v136_is_explicitly_routed_to_mbw(self) -> None:
        routing = json.loads(
            (DB / "canonical/mariadb-historical-migration-routing.json").read_text(encoding="utf-8")
        )
        for name in (
            "V136__backoffice_permission_identity.sql",
            "R136__backoffice_permission_identity.sql",
        ):
            sections = routing["files"][name]["sections"]
            self.assertEqual([{"logicalDatabase": "mbwDB"}], sections, name)


if __name__ == "__main__":
    unittest.main()
