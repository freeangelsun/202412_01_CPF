from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPTS = ROOT / "cpf-tools" / "scripts"


class BackupOperationalSafetyTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.common = (SCRIPTS / "cpf-backup-lifecycle-common.ps1").read_text(encoding="utf-8-sig")
        cls.backup = (SCRIPTS / "backup-cpf-database.ps1").read_text(encoding="utf-8-sig")
        cls.restore = (SCRIPTS / "restore-cpf-database.ps1").read_text(encoding="utf-8-sig")
        cls.retention = (SCRIPTS / "invoke-cpf-backup-retention.ps1").read_text(encoding="utf-8-sig")
        cls.test_data = (SCRIPTS / "prepare-cpf-test-data.ps1").read_text(encoding="utf-8-sig")

    def test_restore_records_unknown_and_reconcile_after_side_effect_boundary(self):
        for token in [
            "$executionStarted=$false",
            "$executionStarted=$true",
            "if($executionStarted){'UNKNOWN'}else{'FAIL'}",
            "-ReconcileRequired $executionStarted",
        ]:
            self.assertIn(token, self.restore)
        self.assertIn("reconcileRequired=$ReconcileRequired", self.common)

    def test_retention_purge_requires_approval_and_reviewed_plan_hash(self):
        for token in [
            "ApprovalReference",
            "Approver",
            "ExpectedPlanSha256",
            "PURGE_CANDIDATE",
            "PURGE_APPLYING",
            "PURGE_UNKNOWN",
            "planSha256",
            "reconcileRequired",
            "purge-intent-",
        ]:
            self.assertIn(token, self.retention)
        self.assertIn("Operator와 Approver는 달라야", self.retention)

    def test_backup_failure_removes_incomplete_artifacts_and_writes_audit(self):
        for token in [
            "$backupCompleted=$false",
            "$backupCompleted=$true",
            "if(-not $backupCompleted)",
            "Remove-Item -LiteralPath $artifact",
            "-Operation 'BACKUP' -Status 'FAIL'",
        ]:
            self.assertIn(token, self.backup)

    def test_test_data_client_drains_both_streams_before_dispose(self):
        self.assertIn("$stdoutText=$o.GetAwaiter().GetResult()", self.test_data)
        self.assertIn("$stderrText=$e.GetAwaiter().GetResult()", self.test_data)
        self.assertIn("outputSha256", self.test_data)
        self.assertNotIn("$o=$p.StandardOutput.ReadToEndAsync();$e=$p.StandardError.ReadToEndAsync();$p.WaitForExit();if", self.test_data)


if __name__ == "__main__":
    unittest.main()
