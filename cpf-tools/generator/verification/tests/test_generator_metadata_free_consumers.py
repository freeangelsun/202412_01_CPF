from __future__ import annotations

import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
TARGETS = (
    "cpf-tools/generator/tools/export-generated-domain-capability-inventory.ps1",
    "cpf-tools/generator/tools/initialize-domain-database.ps1",
    "cpf-tools/generator/tools/initialize-generated-domain-databases.ps1",
    "cpf-tools/generator/tools/remove-domain.ps1",
    "cpf-tools/generator/tools/sync-generated-domain-artifacts.ps1",
    "cpf-tools/generator/verification/check-generated-domain-parity.ps1",
    "cpf-tools/generator/verification/check-generator-arbitrary-domain-parity.ps1",
    "cpf-tools/generator/verification/check-generator-golden-path.ps1",
    "cpf-tools/generator/verification/smoke-create-domain.ps1",
    "cpf-tools/generator/verification/smoke-domain-capability-matrix.ps1",
    "cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1",
    "cpf-tools/generator/verification/smoke-remove-domain.ps1",
    "cpf-tools/generator/verification/verify-domain.ps1",
)


class MetadataFreeGeneratorConsumerTest(unittest.TestCase):
    def test_explicit_arbitrary_definition_inventory(self) -> None:
        with tempfile.TemporaryDirectory(prefix="cpf-generator-consumer-") as temporary:
            definition = Path(temporary) / "cpf-domain.yaml"
            definition.write_text(
                """domain:
  name: arbitraryconsumer
  systemCode: ARC
  packageName: arbitraryconsumer
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: ARC
preset: standard-enterprise
modules:
  online: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
runtime:
  localOnlinePort: 18940
generation:
  sampleTransaction: true
""",
                encoding="utf-8",
                newline="\n",
            )
            completed = subprocess.run(
                [
                    "python",
                    str(ROOT / "cpf-tools/generator/tools/generated_domain_inventory.py"),
                    "--root",
                    str(ROOT),
                    "--file",
                    str(definition),
                    "--include-missing",
                ],
                check=True,
                capture_output=True,
                text=True,
                encoding="utf-8",
            )
            rows = json.loads(completed.stdout)["domains"]
            self.assertEqual(1, len(rows))
            self.assertEqual("arbitraryconsumer", rows[0]["domainName"])
            self.assertEqual("ARC", rows[0]["systemCode"])
            self.assertEqual("NONE", rows[0]["generatedProjectMetadata"])
            self.assertEqual([], rows[0]["forbiddenPermanentMetadata"])

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh required for PowerShell parser verification")
    def test_nested_entrypoints_parse_and_default_to_repository_root(self) -> None:
        quoted = ",".join("'" + str(ROOT / target).replace("'", "''") + "'" for target in TARGETS)
        command = (
            f"$files=@({quoted});$bad=@();foreach($file in $files){{"
            "$tokens=$null;$errors=$null;"
            "[Management.Automation.Language.Parser]::ParseFile($file,[ref]$tokens,[ref]$errors)|Out-Null;"
            "if($errors.Count){$bad+=@($errors|ForEach-Object{$_.Message})}};"
            "if($bad.Count){$bad|ForEach-Object{[Console]::Error.WriteLine($_)};exit 1}"
        )
        subprocess.run(
            ["pwsh", "-NoProfile", "-Command", command],
            check=True,
            cwd=ROOT,
            capture_output=True,
            text=True,
            encoding="utf-8",
        )
        for relative in TARGETS:
            text = (ROOT / relative).read_text(encoding="utf-8-sig")
            self.assertIn('$PSScriptRoot\\..\\..\\..', text, relative)

    def test_active_consumers_do_not_read_project_local_manifest(self) -> None:
        forbidden_read_tokens = (
            "Join-Path $projectDir \"manifest/domain-manifest.json\"",
            "Join-Path $projectDir 'manifest/domain-manifest.json'",
            "manifest/generator-ownership.json\") -Raw",
            "manifest/domain-manifest.json\") -Raw",
            "$ownership.createdFiles",
        )
        for relative in TARGETS:
            text = (ROOT / relative).read_text(encoding="utf-8-sig")
            for token in forbidden_read_tokens:
                self.assertNotIn(token, text, f"{relative}: {token}")
            self.assertNotIn("Generated Domain manifest가 없습니다", text, relative)

    def test_posix_database_wrapper_forwards_external_selection(self) -> None:
        text = (ROOT / "cpf-tools/generator/initialize-domain-database.sh").read_text(encoding="utf-8")
        for token in (
            "--definition",
            "-DefinitionPath",
            "--database-vendor",
            "-DatabaseVendor",
            "--operation",
            "-Operation",
        ):
            self.assertIn(token, text)
        self.assertNotIn("manifest/domain-manifest.json", text)


if __name__ == "__main__":
    unittest.main()
