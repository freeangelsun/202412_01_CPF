import json
import os
import re
import shutil
import subprocess
import tempfile
import textwrap
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
RUNTIME_TOOLS = ROOT / "cpf-tools/runtime/tools"
DEFAULT_CONSUMERS = {
    RUNTIME_TOOLS / "runtime-start-services.ps1": "Modules",
    RUNTIME_TOOLS / "runtime-status.ps1": "Modules",
    RUNTIME_TOOLS / "runtime-diagnostics.ps1": "DiagnosticsModules",
    RUNTIME_TOOLS / "check-packaged-runtime-resources.ps1": "Modules",
    RUNTIME_TOOLS / "smoke-runtime-closure.ps1": "Modules",
    ROOT / "cpf-tools/verification/openapi/smoke-openapi.ps1": "Modules",
}


class RuntimeDefaultInventoryTest(unittest.TestCase):
    @staticmethod
    def _write_pay_inventory_stub(sandbox: Path) -> None:
        common_dir = sandbox / "cpf-tools/generator/tools"
        common_dir.mkdir(parents=True)
        (common_dir / "generated-domain-common.ps1").write_text(
            textwrap.dedent(
                """
                function Get-CpfGeneratedDomainInventory {
                    param([string] $Root)
                    return @([pscustomobject]@{
                        exists = $true
                        onlineEnabled = $true
                        forbiddenPermanentMetadata = @()
                        localOnlinePort = 19070
                        systemCode = 'PAY'
                        projectName = 'cpf-payment'
                        domainName = 'payment'
                        moduleName = 'payment'
                        databaseEnabled = $true
                        databaseRole = 'CUSTOMER_BUSINESS_DB'
                        contractPath = 'cpf-payment/gradle.properties'
                        generationMode = 'generated'
                    })
                }
                """
            ).strip()
            + "\n",
            encoding="utf-8",
        )

    def test_default_consumers_do_not_pin_generated_or_retired_domains(self):
        for path, parameter in DEFAULT_CONSUMERS.items():
            text = path.read_text(encoding="utf-8")
            self.assertRegex(
                text,
                rf"\[string\[\]\]\s+\${re.escape(parameter)}\s*=\s*@\(\s*\)",
                path.as_posix(),
            )
            for code in ("MBR", "EXS", "REF"):
                self.assertNotIn(f'"{code}"', text, path.as_posix())

    def test_runtime_start_uses_canonical_profile_and_deployment_binding(self):
        text = (RUNTIME_TOOLS / "runtime-start-services.ps1").read_text(encoding="utf-8")
        self.assertIn("cpf-tools\\db\\config\\database-install.default.json", text)
        self.assertIn("cpf-tools\\db\\tools\\database-profile-common.ps1", text)
        self.assertIn('Add-CpfRuntimeDatabaseEnvironment -Prefix "CPF_PLATFORM" -Target $coreTarget', text)
        self.assertNotIn("databaseProfilePath", text)
        self.assertNotIn("deploy/database/database-profile.json", text)
        self.assertIn("CUSTOMER_BUSINESS_DB", text)
        self.assertIn("${prefix}_DATASOURCE_URL", text)
        self.assertIn("deployment-environment", text)

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh 7 is required")
    def test_arbitrary_system_code_is_discovered_without_runtime_source_change(self):
        with tempfile.TemporaryDirectory(prefix="cpf-runtime-inventory-") as temporary:
            sandbox = Path(temporary)
            self._write_pay_inventory_stub(sandbox)
            command = textwrap.dedent(
                f"""
                $ErrorActionPreference = 'Stop'
                . '{(RUNTIME_TOOLS / 'runtime-common.ps1').as_posix()}'
                $items = @(Resolve-CpfRuntimeModules -Modules @() -Root '{sandbox.as_posix()}')
                @($items | ForEach-Object {{
                    [ordered]@{{
                        module = [string] $_.module
                        projectName = [string] $_.projectName
                        generatedDomain = [bool] $_.generatedDomain
                        databaseRole = [string] $_['databaseRole']
                        contractPath = [string] $_['contractPath']
                    }}
                }}) | ConvertTo-Json -Depth 8 -Compress
                """
            )
            completed = subprocess.run(
                ["pwsh", "-NoProfile", "-Command", command],
                encoding="utf-8",
                errors="replace",
                capture_output=True,
                check=False,
            )
            self.assertEqual(0, completed.returncode, completed.stdout + completed.stderr)
            payload = json.loads(completed.stdout.strip().splitlines()[-1])
            by_code = {item["module"]: item for item in payload}
            self.assertEqual({"ADM", "BAT", "MBW", "EDU", "GWY", "PAY"}, set(by_code))
            self.assertTrue(by_code["PAY"]["generatedDomain"])
            self.assertEqual("cpf-payment", by_code["PAY"]["projectName"])
            self.assertEqual("CUSTOMER_BUSINESS_DB", by_code["PAY"]["databaseRole"])
            self.assertTrue(by_code["PAY"]["contractPath"].endswith("payment/gradle.properties"))

    @unittest.skipUnless(shutil.which("pwsh"), "pwsh 7 is required")
    def test_arbitrary_system_code_runtime_start_consumes_deployment_binding(self):
        with tempfile.TemporaryDirectory(prefix="cpf-runtime-start-") as temporary:
            sandbox = Path(temporary)
            self._write_pay_inventory_stub(sandbox)

            helper_dir = sandbox / "cpf-tools/db/tools"
            helper_dir.mkdir(parents=True)
            shutil.copy2(
                ROOT / "cpf-tools/db/tools/database-profile-common.ps1",
                helper_dir / "database-profile-common.ps1",
            )
            pack_dir = sandbox / "cpf-tools/db/vendor/mariadb"
            pack_dir.mkdir(parents=True)
            (pack_dir / "pack.json").write_text("{}\n", encoding="utf-8")
            profile_path = sandbox / "cpf-tools/db/config/database-install.default.json"
            profile_path.parent.mkdir(parents=True)
            profile = json.loads(
                (ROOT / "cpf-tools/db/config/database-install.default.json").read_text(encoding="utf-8")
            )
            for module in profile["modules"].values():
                module["enabled"] = False
            profile_path.write_text(
                json.dumps(profile),
                encoding="utf-8",
            )
            result_dir = sandbox / "result"
            environment = {
                **dict(os.environ),
                "PAY_DATASOURCE_URL": "jdbc:mariadb://127.0.0.1:3306/customerDB",
                "PAY_DATASOURCE_USERNAME": "cpf_pay_app",
                "PAY_DATASOURCE_PASSWORD": "runtime-secret",
                "PAY_DATASOURCE_DRIVER": "org.mariadb.jdbc.Driver",
            }
            completed = subprocess.run(
                [
                    "pwsh",
                    "-NoProfile",
                    "-File",
                    str(RUNTIME_TOOLS / "runtime-start-services.ps1"),
                    "-Root",
                    str(sandbox),
                    "-Modules",
                    "PAY",
                    "-ResultDir",
                    str(result_dir),
                    "-DbVendor",
                    "mariadb",
                    "-DbResourceRoot",
                    str(pack_dir),
                    "-DatabaseProfilePath",
                    str(profile_path),
                    "-NoExitOnFailure",
                ],
                encoding="utf-8",
                errors="replace",
                capture_output=True,
                env=environment,
                check=False,
            )
            self.assertEqual(0, completed.returncode, completed.stdout + completed.stderr)
            result = json.loads(
                (result_dir / "runtime-start-services-result.json").read_text(encoding="utf-8")
            )
            self.assertEqual(1, len(result["generatedDatabaseBindings"]))
            binding = result["generatedDatabaseBindings"][0]
            self.assertEqual("PAY", binding["module"])
            self.assertEqual("CUSTOMER_BUSINESS_DB", binding["databaseRole"])
            self.assertEqual("mariadb", binding["vendor"])
            self.assertEqual("deployment-environment", binding["source"])
            self.assertNotIn("runtime-secret", json.dumps(result))
            self.assertEqual("PAY", result["modules"][0]["module"])
            self.assertIn("bootJar file was not found", result["modules"][0]["failureRootCause"])


if __name__ == "__main__":
    unittest.main()
