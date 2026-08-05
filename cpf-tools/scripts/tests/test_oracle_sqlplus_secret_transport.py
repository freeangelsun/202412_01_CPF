from __future__ import annotations

import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
RUNNER = ROOT / "cpf-tools/scripts/invoke-official-db-vendor-sql.ps1"


class OracleSqlPlusSecretTransportTest(unittest.TestCase):
    def setUp(self) -> None:
        self.text = RUNNER.read_text(encoding="utf-8")

    def test_oracle_password_is_not_a_process_argument(self) -> None:
        self.assertNotRegex(self.text, r"&\s*\$client\s+-L\s+-S\s+\$connect")
        self.assertNotIn("ArgumentList.Add($connect)", self.text)
        self.assertIn("foreach($argument in @('-L','-S','/nolog'))", self.text)

    def test_oracle_connect_is_written_only_to_redirected_stdin(self) -> None:
        self.assertIn("$psi.RedirectStandardInput=$true", self.text)
        self.assertIn("$process.StandardInput.Write($script)", self.text)
        self.assertIn("$connect='CONNECT '+$Username", self.text)
        self.assertIn("Invoke-SqlPlusText $t $t.adminUsername $t.adminPassword", self.text)
        self.assertIn("Invoke-SqlPlusText $t $t.migrationUsername $t.migrationPassword", self.text)

    def test_failure_output_is_secret_masked_and_fail_closed(self) -> None:
        self.assertIn("Protect-CpfSecretText", self.text)
        self.assertIn("if($process.ExitCode -ne 0)", self.text)
        self.assertIn("$allSecrets", self.text)
        self.assertIn("throw \"sqlplus failed", self.text)
        self.assertIn("WHENEVER SQLERROR EXIT SQL.SQLCODE", self.text)

    def test_verify_output_contract_is_preserved(self) -> None:
        self.assertIn("Assert-VerifyOutput @($stdout -split", self.text)
        self.assertIn("-Verify:($Mode -eq 'verify')", self.text)

    def test_provision_masks_all_embedded_credentials(self) -> None:
        self.assertIn("[string[]]$SensitiveValues", self.text)
        self.assertIn("$allSecrets=@($Password,$passwordLiteral)+@($SensitiveValues)", self.text)
        self.assertIn('Protect-CpfSecretText (($stderr+"`n"+$stdout).Trim()) $allSecrets', self.text)
        self.assertIn("Protect-CpfSecretText $stdout $allSecrets", self.text)
        self.assertIn("-SensitiveValues @($t.migrationPassword,$t.runtimePassword", self.text)

    def test_sqlplus_stdin_rejects_control_character_injection(self) -> None:
        self.assertIn("Assert-CpfSqlPlusScalar", self.text)
        self.assertIn("contains control characters", self.text)
        self.assertIn("Assert-CpfSqlPlusScalar 'Username' $Username", self.text)
        self.assertIn("Assert-CpfSqlPlusScalar 'Host' ([string]$t.host)", self.text)
        self.assertIn("Assert-CpfSqlPlusScalar 'DatabaseName' ([string]$t.databaseName)", self.text)
        self.assertIn("Assert-CpfSqlPlusScalar 'Password' $Password", self.text)

    def test_sqlplus_disables_command_and_substitution_echo(self) -> None:
        self.assertIn('SET ECHO OFF', self.text)
        self.assertIn('SET VERIFY OFF', self.text)


if __name__ == "__main__":
    unittest.main()
