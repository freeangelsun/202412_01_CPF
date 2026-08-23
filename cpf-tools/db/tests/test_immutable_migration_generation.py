from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


def load_module(name: str, relative: str):
    spec = importlib.util.spec_from_file_location(name, ROOT / relative)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


BAT = load_module("sync_bat_runtime_roles_immutable_test", "cpf-tools/runtime/tools/sync_bat_runtime_roles.py")
SEED = load_module("sync_platform_seed_immutable_test", "cpf-tools/db/tools/sync_platform_seed_currentization.py")


class ImmutableMigrationGenerationTest(unittest.TestCase):
    def test_bundle_builder_does_not_publish_versioned_history(self):
        text = (ROOT / "cpf-tools/db/tools/build-all-install-sql.ps1").read_text(encoding="utf-8-sig")
        self.assertNotIn("Publish-CentralMigrationFiles", text)
        self.assertNotIn("Publish-CentralDirectory", text)
        self.assertIn("immutable Migration/Rollback history was not written", text)

    def test_independent_published_mariadb_histories_are_not_cross_rewritten(self):
        verifier = (ROOT / "cpf-tools/db/verification/check-migration-checksums.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertNotIn("source/runtime migration drift", verifier)
        self.assertIn("Independent manifest integrity is the fail-closed contract", verifier)
        self.assertIn("Check-Pack $sourceRollback", verifier)
        self.assertNotIn("source/runtime modern rollback drift", verifier)
        self.assertIn("Check-PublishedSqlDirectory $sourceRollback", verifier)
        self.assertIn("Check-PublishedSqlDirectory $runtimeRollback", verifier)
        self.assertIn("published immutable artifact drift", verifier)

    def test_checksum_generator_requires_production_packs_but_maps_reference_fixture_to_history(self):
        text = (ROOT / "cpf-tools/db/tools/generate-migration-checksums.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("platformDatabaseArchitecture.REFERENCE_FIXTURE.physicalName", text)
        self.assertIn("$requiredProductionDatabases", text)
        self.assertIn("-cne $referenceFixtureDatabase", text)
        self.assertNotIn("$requiredPlatformDatabases", text)
        self.assertIn("current production migration pack missing", text)
        self.assertIn("foreach ($discoveredPackDirectory in $packDirectories)", text)
        self.assertNotIn("foreach ($packDirectory in $packDirectories)", text)

    def test_current_mbw_verify_generator_has_no_retired_bza_table(self):
        text = (ROOT / "cpf-tools/db/generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertNotIn("FROM bza_role", text)
        self.assertNotIn("FROM bza_menu", text)
        self.assertEqual(3, text.count("FROM MBW_ROLE"))
        self.assertEqual(3, text.count("FROM MBW_MENU"))

    def test_verify_source_is_not_rewrapped_over_its_canonical_owner(self):
        generator = (ROOT / "cpf-tools/db/generator/generate-official-db-vendor-source.ps1").read_text(
            encoding="utf-8-sig"
        )
        builder = (ROOT / "cpf-tools/db/tools/build-official-db-vendor-packs.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertIn("sync-platform-non-table-objects.ps1", generator)
        self.assertIn("verify/00_verify.sql", builder)
        self.assertNotIn(
            "Build-Bundle $vendor @('00_verify.sql') (Join-Path $vendorRoot 'source/00_verify.sql')",
            builder,
        )
        self.assertIn("Never use that file as both Build-Bundle input", builder)
        self.assertIn("$isUnpublishedEmptyPack", builder)
        self.assertIn("Refusing historical V63/R63 backfill into published pack", builder)
        self.assertIn("$existingVersioned.Count -eq 0", builder)
        self.assertIn("platformDatabaseArchitecture.REFERENCE_FIXTURE.physicalName", builder)
        self.assertIn("$historicalLogicalDatabases", builder)
        self.assertIn("$historicalLogicalDatabases -cnotcontains $db", builder)
        self.assertIn("Current-snapshot-only logical database has no independent historical pack", builder)

    def test_historical_nullable_migration_does_not_follow_current_schema_version(self):
        renderer = (
            ROOT / "cpf-tools/verification/tools/sync-platform-nullable-empty-string-repair.ps1"
        ).read_text(encoding="utf-8-sig")
        self.assertIn("$sourceSchemaVersion -gt $currentSchemaVersion", renderer)
        self.assertNotIn("$contract.sourceSchemaVersion -ne [int]$schema.schemaVersion", renderer)
        self.assertIn("canonical schemaVersion $sourceSchemaVersion", renderer)
        self.assertIn("Nullable repair target must be nullable with no default", renderer)
        self.assertIn("PSObject.Properties['currentName']", renderer)
        self.assertIn("[string]$_.currentName -ceq $historicalTableName", renderer)

    def test_python_batch_generator_rejects_existing_different_history(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "V1__history.sql"
            path.write_text("published\n", encoding="utf-8")
            with self.assertRaisesRegex(BAT.ContractError, "IMMUTABLE_MIGRATION_CONFLICT"):
                BAT.write_or_check(path, "rewritten\n", True, [], immutable_versioned=True)
            self.assertEqual("published\n", path.read_text(encoding="utf-8"))

    def test_python_batch_generator_may_create_new_version(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "V2__new.sql"
            changed: list[str] = []
            BAT.write_or_check(path, "new\n", True, changed, immutable_versioned=True)
            self.assertEqual("new\n", path.read_text(encoding="utf-8"))
            self.assertEqual([path.as_posix()], changed)

    def test_python_seed_generator_rejects_existing_different_history(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            path = root / "V1__history.sql"
            path.write_text("published\n", encoding="utf-8")
            with self.assertRaisesRegex(SEED.ContractError, "IMMUTABLE_MIGRATION_CONFLICT"):
                SEED.write_immutable_or_check(path, "rewritten\n", True, [], root)
            self.assertEqual("published\n", path.read_text(encoding="utf-8"))

    def test_powershell_renderers_have_fail_closed_guard(self):
        for relative in (
            "cpf-tools/verification/tools/sync-platform-non-table-objects.ps1",
            "cpf-tools/verification/tools/sync-platform-nullable-empty-string-repair.ps1",
        ):
            text = (ROOT / relative).read_text(encoding="utf-8-sig")
            self.assertIn("IMMUTABLE_MIGRATION_CONFLICT", text, relative)
        non_table = (ROOT / "cpf-tools/verification/tools/sync-platform-non-table-objects.ps1").read_text(
            encoding="utf-8-sig"
        )
        self.assertEqual(6, non_table.count("-ImmutableVersioned"))


if __name__ == "__main__":
    unittest.main()
