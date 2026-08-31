from __future__ import annotations

import json
import subprocess
import sys
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
            self.assertIn("& python -B $seedSync", source)
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


class VendorSeedWriterOwnershipTest(unittest.TestCase):
    """Vendor seed source 의 writer 는 canonical renderer 하나여야 한다.

    generate-official-db-vendor-source.ps1 이 seed-model.json 을 자체 규칙으로 다시 렌더링해
    sync-canonical-seed-bundles.py 와 같은 파일을 덮어쓰던 시기가 있었다. 두 결과는 header 줄,
    USE <db> 문, 변수 inline 여부가 달라서 파이프라인이 둘을 연달아 호출하는 순서에 최종 산출물이
    좌우됐다. 생성기는 schema/provision/verify 만 소유하고 seed 는 위임한다.
    """

    GENERATOR = ROOT / "cpf-tools/db/generator/generate-official-db-vendor-source.ps1"
    CANONICAL_RENDERER = "cpf-tools/db/tools/sync-canonical-seed-bundles.py"

    def test_generator_delegates_seed_rendering_to_the_canonical_renderer(self) -> None:
        source = self.GENERATOR.read_text(encoding="utf-8-sig")
        self.assertIn(self.CANONICAL_RENDERER, source)
        self.assertIn("Canonical seed source synchronization failed", source)

    def test_generator_owns_no_independent_seed_rendering_logic(self) -> None:
        source = self.GENERATOR.read_text(encoding="utf-8-sig")
        # seed-model 문장을 vendor SQL 로 직접 변환하던 함수/루프가 남아 있으면 경쟁 write 가 재발한다.
        for dead in (
            "seed.canonicalPolicy.sourceFiles",
            "function Render-Insert",
            "function Convert-Expr",
            "function Alias-Select",
            "function Split-Top",
            "function Rows(",
        ):
            self.assertNotIn(dead, source, f"independent seed rendering returned: {dead}")

    def test_generator_does_not_write_canonical_seed_source_files(self) -> None:
        source = self.GENERATOR.read_text(encoding="utf-8-sig")
        seed_model = json.loads(
            (ROOT / "cpf-tools/db/canonical/seed-model.json").read_text(encoding="utf-8-sig"))
        for seed_file in seed_model["canonicalPolicy"]["sourceFiles"]:
            self.assertNotIn(seed_file, source, f"generator still writes {seed_file}")

    def test_canonical_renderer_is_the_single_writer_and_is_idempotent(self) -> None:
        pwsh_free_check = subprocess.run(
            [sys.executable, "-B", str(ROOT / self.CANONICAL_RENDERER),
             "--root", str(ROOT), "--check"],
            capture_output=True, text=True, encoding="utf-8", errors="replace")
        self.assertEqual(0, pwsh_free_check.returncode,
                         pwsh_free_check.stdout + pwsh_free_check.stderr)
        self.assertIn("written=0", pwsh_free_check.stdout)

    def test_canonical_seed_header_marks_derived_authority(self) -> None:
        """두 생성기를 구분하던 표식. canonical renderer 결과만 이 header 를 갖는다."""
        seed_model = json.loads(
            (ROOT / "cpf-tools/db/canonical/seed-model.json").read_text(encoding="utf-8-sig"))
        for vendor in ("mariadb", "postgresql", "oracle"):
            for seed_file in seed_model["canonicalPolicy"]["sourceFiles"]:
                rendered = ROOT / f"cpf-tools/db/vendor/{vendor}/source/{seed_file}"
                if not rendered.is_file():
                    continue
                head = rendered.read_text(encoding="utf-8-sig").splitlines()[:4]
                self.assertTrue(
                    any("canonical authority is cpf-tools/db/canonical" in line for line in head),
                    f"{vendor}/{seed_file} was not written by the canonical renderer")


if __name__ == "__main__":
    unittest.main()
