import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
DESCRIPTION = "current_binding_and_response_contract"


class V135CurrentEdgeMigrationTest(unittest.TestCase):
    def setUp(self):
        self.seed = json.loads((ROOT / "cpf-tools/db/canonical/seed-model.json").read_text(encoding="utf-8"))

    def paths(self, vendor: str) -> tuple[Path, Path, Path]:
        logical = Path() if vendor == "mariadb" else Path("cpfDB")
        migration = ROOT / f"cpf-tools/db/vendor/{vendor}/migration/flyway" / logical / f"V135__{DESCRIPTION}.sql"
        rollback = ROOT / f"cpf-tools/db/vendor/{vendor}/rollback" / logical / f"R135__{DESCRIPTION}.sql"
        manifest = migration.parent / "checksums.sha256"
        return migration, rollback, manifest

    def canonical_response_codes(self) -> set[str]:
        codes: set[str] = set()
        for statement in self.seed["statements"]:
            if (
                statement.get("productionDefault")
                and statement.get("currentTableName") == "CMN_RESPONSE_CODE"
                and statement.get("sourceKind") == "values"
            ):
                codes.update(re.findall(r"\('([^']+)'\s*,", statement["source"]))
        return codes

    def test_all_vendors_publish_data_preserving_current_edge(self):
        expected_codes = self.canonical_response_codes()
        self.assertEqual(35, len(expected_codes))
        for vendor in ("mariadb", "postgresql", "oracle"):
            migration_path, rollback_path, _ = self.paths(vendor)
            migration = migration_path.read_text(encoding="utf-8-sig")
            rollback = rollback_path.read_text(encoding="utf-8-sig")
            self.assertIn("binding_key_hash", migration)
            self.assertIn("uk_cpf_gwy_binding_key", migration)
            self.assertIn("http_status", migration)
            self.assertNotRegex(
                migration,
                r"UNIQUE\s*\(\s*environment_code\s*,\s*host_pattern\s*,\s*path_pattern",
            )
            for code in expected_codes:
                self.assertIn(f"WHEN '{code}' THEN", migration, f"{vendor}:{code}")
            self.assertIn("binding_key_hash", rollback)
            self.assertIn("compat", rollback)
            self.assertIn("http_status", rollback)
            self.assertNotRegex(rollback, r"(?i)DROP\s+COLUMN\s+(?:binding_key_hash|http_status)")

    def test_vendor_digest_uses_six_complete_utf8_normalized_components(self):
        tokens = {
            "mariadb": ("SHA2(", "TRIM(", "DEFAULT", "v1"),
            "postgresql": ("sha256(convert_to", "BTRIM(", "UTF8", "DEFAULT", "v1"),
            "oracle": ("STANDARD_HASH", "UTL_I18N.STRING_TO_RAW", "AL32UTF8", "DEFAULT", "v1"),
        }
        for vendor, required in tokens.items():
            migration_path, rollback_path, _ = self.paths(vendor)
            for path in (migration_path, rollback_path):
                text = path.read_text(encoding="utf-8-sig")
                for token in required:
                    self.assertIn(token, text, f"{vendor}:{path.name}:{token}")
                for component in (
                    "environment_code",
                    "host_pattern",
                    "path_pattern",
                    "http_method",
                    "api_version",
                    "route_version",
                ):
                    self.assertIn(component, text, f"{vendor}:{path.name}:{component}")

    def test_v135_is_append_registered_in_each_official_manifest(self):
        for vendor in ("mariadb", "postgresql", "oracle"):
            migration_path, _, manifest_path = self.paths(vendor)
            manifest = manifest_path.read_text(encoding="utf-8-sig")
            self.assertRegex(
                manifest,
                rf"(?m)^[0-9a-f]{{64}} \*{re.escape(migration_path.name)}$",
                vendor,
            )

    def test_mariadb_v135_and_r135_have_exact_whole_file_platform_routing(self):
        routing = json.loads(
            (ROOT / "cpf-tools/db/canonical/mariadb-historical-migration-routing.json").read_text(
                encoding="utf-8-sig"
            )
        )
        for name in (f"V135__{DESCRIPTION}.sql", f"R135__{DESCRIPTION}.sql"):
            self.assertEqual(
                {"sections": [{"logicalDatabase": "cpfDB"}]},
                routing["files"].get(name),
                name,
            )


if __name__ == "__main__":
    unittest.main()
