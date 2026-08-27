from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
SYNC = ROOT / "cpf-tools/db/tools/sync-database-artifacts.ps1"


class DatabaseArtifactSyncOrderTest(unittest.TestCase):
    def test_every_powershell_step_uses_its_repository_relative_owner(self) -> None:
        source = SYNC.read_text(encoding="utf-8-sig")
        owners = (
            "cpf-tools/db/generator/generate-official-db-vendor-source.ps1",
            "cpf-tools/db/tools/build-official-db-vendor-packs.ps1",
            "cpf-tools/db/tools/build-all-install-sql.ps1",
            "cpf-tools/verification/tools/check-spring-batch-sequence-contract.ps1",
            "cpf-tools/runtime/tools/sync-platform-runtime-query-packs.ps1",
            "cpf-tools/runtime/tools/sync-bat-runtime-query-pack.ps1",
            "cpf-tools/db/verification/check-query-contract-integrity.ps1",
            "cpf-tools/verification/tools/sync-platform-nullable-empty-string-repair.ps1",
            "cpf-tools/db/tools/sync-platform-seed-currentization.ps1",
            "cpf-tools/db/tools/generate-migration-checksums.ps1",
            "cpf-tools/db/tools/generate-database-schema-manifest.ps1",
            "cpf-tools/db/verification/check-database-schema-drift.ps1",
            "cpf-tools/db/verification/check-database-profile-standard.ps1",
            "cpf-tools/generator/tools/sync-generated-domain-artifacts.ps1",
        )
        for owner in owners:
            self.assertIn(f'"{owner}"', source)
            self.assertTrue((ROOT / owner).is_file(), owner)
        self.assertIn("if (-not (Test-Path -LiteralPath $generatedSync -PathType Leaf))", source)

    def test_canonical_seed_and_current_projections_precede_bundle_assembly(self) -> None:
        source = SYNC.read_text(encoding="utf-8-sig")
        generator = source.index('"cpf-tools/db/generator/generate-official-db-vendor-source.ps1"')
        seed_projection = source.index(
            '"cpf-tools/db/tools/sync-canonical-seed-bundles.py"'
        )
        current_projection = source.index('"cpf-tools/db/render_vendor_pack.py"')
        vendor_bundles = source.index('"cpf-tools/db/tools/build-official-db-vendor-packs.ps1"')
        mariadb_bundles = source.index('"cpf-tools/db/tools/build-all-install-sql.ps1"')

        self.assertLess(generator, seed_projection)
        self.assertLess(seed_projection, current_projection)
        self.assertLess(current_projection, vendor_bundles)
        self.assertLess(vendor_bundles, mariadb_bundles)

    def test_python_projection_failures_are_fail_closed(self) -> None:
        source = SYNC.read_text(encoding="utf-8-sig")
        self.assertIn("function Invoke-CpfPythonArtifactStep", source)
        self.assertIn("if ($LASTEXITCODE -ne 0)", source)
        self.assertIn("throw \"$FailureMessage exitCode=$LASTEXITCODE", source)
        self.assertIn("& python -B $absoluteScriptPath --root $Root @ExtraArgs", source)

    def test_seed_bundles_have_one_writer_and_downstream_builders_check_only(self) -> None:
        official = (
            ROOT / "cpf-tools/db/tools/build-official-db-vendor-packs.ps1"
        ).read_text(encoding="utf-8-sig")
        mariadb = (ROOT / "cpf-tools/db/tools/build-all-install-sql.ps1").read_text(
            encoding="utf-8-sig"
        )

        for source in (official, mariadb):
            self.assertIn("sync-canonical-seed-bundles.py", source)
            self.assertIn("--check", source)
        for bundle_key in (
            "$maria.productSeedFiles",
            "$maria.optionalSampleSeedFiles",
            "$maria.testSeedFiles",
        ):
            self.assertNotIn(f"Build-Bundle $vendor {bundle_key}", official)
        for bundle_name in (
            "00_product_seed.sql",
            "00_optional_sample_seed.sql",
            "00_test_seed.sql",
        ):
            self.assertNotIn(f'New-Bundle "{bundle_name}"', mariadb)
        self.assertIn("Assert-BundlePolicy $canonicalSeedBundle", mariadb)


if __name__ == "__main__":
    unittest.main()
