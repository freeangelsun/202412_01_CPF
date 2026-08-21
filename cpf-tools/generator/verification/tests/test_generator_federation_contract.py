from __future__ import annotations

import json
import shutil
import shutil as _shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
GENERATOR = ROOT / "cpf-tools" / "generator"
MEMBER_DEFINITION = GENERATOR / "definitions" / "member" / "cpf-domain.yaml"


def run_pwsh(script: Path, *arguments: str, expect: int = 0) -> subprocess.CompletedProcess[str]:
    if _shutil.which("pwsh") is None:
        raise unittest.SkipTest("pwsh is required for PowerShell federation runtime verification")
    process = subprocess.run(
        ["pwsh", "-NoProfile", "-File", str(script), *map(str, arguments)],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
        errors="replace",
        capture_output=True,
        env={**__import__("os").environ, "PYTHONDONTWRITEBYTECODE": "1"},
        check=False,
    )
    if process.returncode != expect:
        raise AssertionError(
            f"unexpected exit={process.returncode} expected={expect}\n"
            f"stdout={process.stdout}\nstderr={process.stderr}"
        )
    return process


class GeneratorFederationContractTest(unittest.TestCase):
    def setUp(self) -> None:
        (ROOT / "build").mkdir(exist_ok=True)
        self.work = Path(tempfile.mkdtemp(prefix="cpf-federation-test-", dir=ROOT / "build"))

    def tearDown(self) -> None:
        resolved = self.work.resolve()
        build = (ROOT / "build").resolve()
        self.assertIn(build, resolved.parents)
        self.assertTrue(resolved.name.startswith("cpf-federation-test-"))
        shutil.rmtree(resolved, ignore_errors=False)

    def export_member(self, vendor: str) -> Path:
        arguments = [
            "-DomainModule", "cpf-member",
            "-DefinitionFile", str(MEMBER_DEFINITION),
            "-SystemCode", "MBR",
            "-DatabaseVendor", vendor,
            "-OutputRoot", str(self.work),
            "-ArtifactMode", "LOCAL_DEV",
            "-SkipBuild",
        ]
        run_pwsh(GENERATOR / "export-domain-repository.ps1", *arguments)
        return self.work / "cpf-member"

    def test_framework_canonical_output_is_metadata_free(self) -> None:
        result = run_pwsh(
            GENERATOR / "verify-domain-federation.ps1",
            "-RepoRoot", str(ROOT),
            "-FrameworkRoot", str(ROOT),
        )
        self.assertIn('"status": "PASS"', result.stdout)
        self.assertIn('"generatedProjectMetadata": "NONE"', result.stdout)

    def test_export_selects_exactly_one_canonical_vendor_pack(self) -> None:
        repository = self.export_member("mariadb")
        vendor_root = repository / "cpf-db" / "generated" / "domain-template"
        self.assertEqual(["mariadb"], sorted(path.name for path in vendor_root.iterdir()))
        canonical = ROOT / "cpf-tools" / "db" / "generated" / "domain-template" / "mariadb"
        expected = sorted(path.relative_to(canonical).as_posix() for path in canonical.rglob("*") if path.is_file())
        actual = sorted(path.relative_to(vendor_root / "mariadb").as_posix()
                        for path in (vendor_root / "mariadb").rglob("*") if path.is_file())
        self.assertEqual(expected, actual)
        self.assertFalse((repository / "cpf-domain-manifest.json").exists())
        self.assertFalse((repository / "cpf-domain-ownership.json").exists())

    def test_generated_domain_batch_is_optional_and_jobpack_surface_is_not_reintroduced(self) -> None:
        engine = (ROOT / "cpf-tools/generator/engine/cpf_domain_generator.py").read_text(encoding="utf-8")
        schema = (ROOT / "cpf-tools/generator/contracts/cpf-domain.schema.json").read_text(encoding="utf-8")
        self.assertIn('modules.get("batch", False)', engine)
        self.assertIn('cpf-starter-batch', engine)
        self.assertIn('"batch"', schema)
        create_source = (GENERATOR / "create-domain-repository.ps1").read_text(encoding="utf-8")
        self.assertNotIn("IncludeJobPack", create_source)

    def test_arbitrary_domain_dry_run_uses_canonical_schema_without_writes(self) -> None:
        output = self.work / "output"
        result = run_pwsh(
            GENERATOR / "create-domain-repository.ps1",
            "-DomainName", "insurance",
            "-SystemCode", "INS",
            "-PackageName", "insurance",
            "-TablePrefix", "INS",
            "-Port", "18430",
            "-DatabaseVendor", "oracle",
            "-OutputRoot", str(output),
            "-ArtifactMode", "LOCAL_DEV",
            "-DryRun",
        )
        self.assertIn('"canonicalSchema": "cpf-tools/generator/contracts/cpf-domain.schema.json"', result.stdout)
        self.assertIn('"generatedProjectMetadata": "NONE"', result.stdout)
        self.assertFalse((output / "cpf-insurance").exists())

    def test_system_code_mismatch_fails_before_output(self) -> None:
        result = run_pwsh(
            GENERATOR / "export-domain-repository.ps1",
            "-DomainModule", "cpf-member",
            "-DefinitionFile", str(MEMBER_DEFINITION),
            "-SystemCode", "BAD",
            "-DatabaseVendor", "mariadb",
            "-OutputRoot", str(self.work),
            "-SkipBuild",
            expect=1,
        )
        self.assertIn("canonical definition", result.stderr)
        self.assertFalse((self.work / "cpf-member").exists())

    def test_forbidden_lifecycle_manifest_is_fail_closed(self) -> None:
        repository = self.export_member("oracle")
        (repository / "cpf-domain-manifest.json").write_text("{}\n", encoding="utf-8")
        result = run_pwsh(
            GENERATOR / "verify-domain-federation.ps1",
            "-RepoRoot", str(repository),
            "-FrameworkRoot", str(ROOT),
            "-DefinitionFile", str(MEMBER_DEFINITION),
            "-DomainName", "member",
            "-DatabaseVendor", "oracle",
            expect=1,
        )
        self.assertIn("lifecycle metadata", result.stderr)

    def test_competing_contracts_and_ownership_remover_are_absent(self) -> None:
        for relative in (
            "contracts/domain-metadata.schema.json",
            "contracts/generated-domain-standard-contract.json",
            "remove-domain-repository.ps1",
        ):
            self.assertFalse((GENERATOR / relative).exists(), relative)
        central = json.loads((GENERATOR / "contracts" / "central-domain-template-contract.json").read_text(encoding="utf-8"))
        self.assertNotIn("capabilityContract", central)
        self.assertNotIn("runtimeAgentContract", central)
        final_gate = (ROOT / "cpf-tools" / "verification" / "tools" / "verify-cpf-final-completion.ps1").read_text(encoding="utf-8")
        self.assertNotIn("manifest/domain-manifest.json", final_gate)
        self.assertIn("run-cpf-local-full-validation.ps1", final_gate)
        canonical_gate = (ROOT / "cpf-tools" / "verification" / "tools" / "run-cpf-local-full-validation.ps1").read_text(encoding="utf-8")
        self.assertIn("GENERATOR_FULL_CONTRACT", canonical_gate)
        self.assertIn("GENERATOR_LIFECYCLE", canonical_gate)


if __name__ == "__main__":
    unittest.main()
