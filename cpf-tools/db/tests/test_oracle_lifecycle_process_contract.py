from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
INITIALIZER = ROOT / "cpf-tools/db/tools/initialize-cpf-database.ps1"
VENDOR_RUNNER = ROOT / "cpf-tools/db/tools/invoke-official-db-vendor-sql.ps1"


class OracleLifecycleProcessContractTest(unittest.TestCase):
    def test_powershell_runner_does_not_read_unowned_native_exit_code(self) -> None:
        source = INITIALIZER.read_text(encoding="utf-8-sig")
        branch = source.split("if ($selectedVendor -in @('postgresql','oracle'))", 1)[1].split(
            "$logicalToKey", 1
        )[0]
        self.assertNotIn("$LASTEXITCODE", branch)
        self.assertGreaterEqual(branch.count("if (-not $?)"), 4)

    def test_sqlplus_redirected_stdin_is_utf8_without_bom(self) -> None:
        source = VENDOR_RUNNER.read_text(encoding="utf-8-sig")
        self.assertIn("$psi.StandardInputEncoding=[Text.UTF8Encoding]::new($false)", source)
        self.assertNotIn("$psi.StandardInputEncoding=[Text.Encoding]::UTF8", source)
        self.assertIn("SP2-0734", source)

    def test_sqlplus_pipe_failure_still_harvests_database_diagnostics(self) -> None:
        source = VENDOR_RUNNER.read_text(encoding="utf-8-sig")
        write_failure = source.index("$writeFailure=$null")
        wait_for_exit = source.index("$process.WaitForExit()", write_failure)
        stdout_harvest = source.index("$stdout=$stdoutTask.GetAwaiter().GetResult()", wait_for_exit)
        failure_gate = source.index(
            "if($process.ExitCode -ne 0 -or $null-ne$writeFailure)", stdout_harvest
        )

        self.assertIn("$writeFailure=$_", source[write_failure:wait_for_exit])
        self.assertIn("$stderr=$stderrTask.GetAwaiter().GetResult()", source[wait_for_exit:failure_gate])
        self.assertIn("transport=$safeTransport error=$safe", source[failure_gate:])

    def test_sqlplus_output_is_bounded_without_hiding_terminal_failure(self) -> None:
        source = VENDOR_RUNNER.read_text(encoding="utf-8-sig")
        self.assertIn("SET FEEDBACK OFF", source)
        self.assertIn("function Limit-CpfDiagnosticText", source)
        self.assertIn("[CPF SQL diagnostic truncated:", source)
        self.assertIn("$safe=Limit-CpfDiagnosticText (Protect-CpfSecretText", source)
        self.assertNotIn("Write-Host (Protect-CpfSecretText $stdout", source)


if __name__ == "__main__":
    unittest.main()
