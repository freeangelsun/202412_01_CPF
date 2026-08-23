from __future__ import annotations

import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
INITIALIZER = ROOT / "cpf-tools/generator/tools/initialize-domain-database.ps1"
DEFINITION = ROOT / "cpf-member/gradle.properties"
CANONICAL_TEMPLATE_ROOT = ROOT / "cpf-tools/db/generated/domain-template"
VENDORS = ("mariadb", "postgresql", "oracle")
EXPECTED_ARTIFACTS = {
    "install/10_empty_install.sql.template",
    "seed/20_product_seed.sql.template",
    "migration/V1____DOMAIN___domain.sql.template",
    "verify/90_verify.sql.template",
    "rollback/R1__remove___DOMAIN___domain.sql.template",
}


def invoke_plan(vendor: str, result_dir: Path, template_root: Path = CANONICAL_TEMPLATE_ROOT) -> subprocess.CompletedProcess[str]:
    database_name = "FREEPDB1" if vendor == "oracle" else "customerDB"
    return subprocess.run(
        [
            "pwsh",
            "-NoProfile",
            "-File",
            str(INITIALIZER),
            "-Root",
            str(ROOT),
            "-DomainName",
            "member",
            "-SystemCode",
            "MBR",
            "-DefinitionPath",
            str(DEFINITION),
            "-DatabaseVendor",
            vendor,
            "-DatabaseHost",
            "127.0.0.1",
            "-DatabaseName",
            database_name,
            "-TemplateRoot",
            str(template_root),
            "-ResultDir",
            str(result_dir),
            "-Operation",
            "bootstrap",
        ],
        cwd=ROOT,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )


class GeneratedDomainDatabaseInstallerContractTest(unittest.TestCase):
    def test_canonical_manifests_are_exact_and_hash_verified(self) -> None:
        for vendor in VENDORS:
            vendor_root = CANONICAL_TEMPLATE_ROOT / vendor
            manifest = json.loads((vendor_root / "manifest.json").read_text(encoding="utf-8"))
            self.assertEqual(vendor, manifest["vendor"])
            self.assertEqual("CUSTOMER_BUSINESS_DB", manifest["businessDatabaseRole"])
            self.assertEqual(EXPECTED_ARTIFACTS, set(manifest["artifacts"]))
            actual = {
                path.relative_to(vendor_root).as_posix()
                for path in vendor_root.rglob("*.template")
            }
            self.assertEqual(EXPECTED_ARTIFACTS, actual)
            for relative, recorded_hash in manifest["artifacts"].items():
                import hashlib

                self.assertEqual(
                    recorded_hash,
                    hashlib.sha256((vendor_root / relative).read_bytes()).hexdigest(),
                )

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh required for PowerShell runtime verification")
    def test_actual_dry_run_for_every_vendor_uses_five_artifact_manifest(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-domain-db-plan-") as temporary:
            temporary_root = Path(temporary)
            for vendor in VENDORS:
                result_dir = temporary_root / vendor
                completed = invoke_plan(vendor, result_dir)
                self.assertEqual(0, completed.returncode, completed.stdout + completed.stderr)
                result = json.loads((result_dir / "domain-db-init-result.json").read_text(encoding="utf-8"))
                self.assertEqual("미검증", result["status"])
                self.assertFalse(result["applied"])
                self.assertFalse(result["physicalDatabaseCreated"])
                self.assertEqual(5, result["templateArtifactCount"])
                self.assertEqual(["install", "seed", "verify"], [row["phase"] for row in result["phases"]])
                self.assertNotIn("runtimeUsername", result)
                self.assertNotIn("adminUsername", result)

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh required for PowerShell runtime verification")
    def test_hash_tamper_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-domain-db-hash-") as temporary:
            template_root = Path(temporary) / "templates"
            shutil.copytree(CANONICAL_TEMPLATE_ROOT, template_root)
            target = template_root / "mariadb/install/10_empty_install.sql.template"
            target.write_text(target.read_text(encoding="utf-8") + "\n-- tampered\n", encoding="utf-8")
            completed = invoke_plan("mariadb", Path(temporary) / "result", template_root)
            self.assertNotEqual(0, completed.returncode)
            self.assertIn("hash", completed.stdout + completed.stderr)

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh required for PowerShell runtime verification")
    def test_extra_template_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-domain-db-extra-") as temporary:
            template_root = Path(temporary) / "templates"
            shutil.copytree(CANONICAL_TEMPLATE_ROOT, template_root)
            extra = template_root / "postgresql/provision/01_provision.sql.template"
            extra.parent.mkdir(parents=True)
            extra.write_text("CREATE DATABASE forbidden;\n", encoding="utf-8")
            completed = invoke_plan("postgresql", Path(temporary) / "result", template_root)
            self.assertNotEqual(0, completed.returncode)
            self.assertIn("canonical 5", completed.stdout + completed.stderr)

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh required for PowerShell runtime verification")
    def test_legacy_vendor_domain_template_fallback_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-domain-db-legacy-") as temporary:
            template_root = Path(temporary) / "templates"
            legacy_root = template_root / "oracle/domain-template"
            shutil.copytree(CANONICAL_TEMPLATE_ROOT / "oracle", legacy_root)
            completed = invoke_plan("oracle", Path(temporary) / "result", template_root)
            self.assertNotEqual(0, completed.returncode)
            self.assertIn("template manifest", completed.stdout + completed.stderr)

    def test_active_surfaces_have_no_generated_principal_inputs(self) -> None:
        surfaces = (
            "cpf-tools/generator/tools/initialize-domain-database.ps1",
            "cpf-tools/generator/tools/initialize-generated-domain-databases.ps1",
            "cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1",
            "cpf-tools/generator/initialize-domain-database.sh",
        )
        forbidden = (
            "RuntimeUsername",
            "RuntimePassword",
            "AdminUsername",
            "AdminPassword",
            "CPF_DOMAIN_DB_RUNTIME_",
            "CPF_DOMAIN_DB_ADMIN_",
            "secretBearing",
            "GetTempFileName",
            "legacyResourceRoot",
        )
        for relative in surfaces:
            text = (ROOT / relative).read_text(encoding="utf-8-sig")
            for token in forbidden:
                self.assertNotIn(token, text, f"{relative}: {token}")


if __name__ == "__main__":
    unittest.main()
