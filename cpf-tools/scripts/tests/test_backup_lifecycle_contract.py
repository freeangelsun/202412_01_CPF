from __future__ import annotations

import hashlib
import json
import tempfile
import unittest
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPTS = ROOT / "cpf-tools" / "scripts"
CONTRACT = ROOT / "cpf-tools" / "db" / "cpf-backup-lifecycle-contract.json"


class BackupLifecycleContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.contract = json.loads(CONTRACT.read_text(encoding="utf-8-sig"))
        cls.backup = (SCRIPTS / "backup-cpf-database.ps1").read_text(encoding="utf-8-sig")
        cls.restore = (SCRIPTS / "restore-cpf-database.ps1").read_text(encoding="utf-8-sig")
        cls.hold = (SCRIPTS / "set-cpf-backup-legal-hold.ps1").read_text(encoding="utf-8-sig")
        cls.retention = (SCRIPTS / "invoke-cpf-backup-retention.ps1").read_text(encoding="utf-8-sig")
        cls.common = (SCRIPTS / "cpf-backup-lifecycle-common.ps1").read_text(encoding="utf-8-sig")

    def test_official_vendor_backup_consumers_exist(self):
        self.assertEqual(["mariadb", "postgresql", "oracle"], self.contract["officialVendors"])
        for vendor, client in [("mariadb", "mariadb-dump"), ("postgresql", "pg_dump"), ("oracle", "expdp")]:
            self.assertIn(f"'{vendor}'", self.backup)
            self.assertIn(client, self.backup)
        self.assertIn("ENCRYPTION_MODE=TRANSPARENT", self.backup)
        self.assertIn('"/@$OracleConnectIdentifier"', self.backup)

    def test_plaintext_is_always_removed_and_credentials_are_not_supported_as_arguments(self):
        self.assertIn("finally", self.backup)
        self.assertIn("Remove-Item -LiteralPath $plain", self.backup)
        self.assertNotRegex(self.backup, r"(?i)\[string\]\$Password")
        self.assertNotRegex(self.restore, r"(?i)\[string\]\$Password")
        self.assertIn("credentialEmbedded=$false", self.backup)
        self.assertIn("encrypted=$true", self.backup)

    def test_restore_is_manifest_hash_approval_and_identity_guarded(self):
        for token in [
            "-ConfirmRestore", "Assert-CpfManifestHash", "Assert-CpfBackupManifest",
            "manifest vendor mismatch", "manifest database mismatch", "backup artifact SHA-256 mismatch",
            "Operator", "Reason", "ApprovalReference", "decrypted source SHA-256 mismatch",
        ]:
            self.assertIn(token, self.restore)
        self.assertNotIn("AllowMissingManifest", self.restore)
        self.assertIn("Manifest 없는 Legacy 복원은 금지", self.restore)
        self.assertIn("PITR capable artifact", self.restore)

    def test_legal_hold_overrides_retention_and_invalid_artifact_fails_closed(self):
        self.assertIn("if([bool]$m.legalHold)", self.retention)
        self.assertIn("INVALID_FAIL_CLOSED", self.retention)
        self.assertIn("if($invalid -gt 0)", self.retention)
        self.assertIn("ConfirmPurge", self.retention)
        self.assertIn("ConfirmChange", self.hold)
        self.assertIn("legalHoldHistory", self.hold)
        self.assertTrue(self.contract["retention"]["legalHoldOverridesExpiry"])

    def test_manifest_required_fields_match_writer(self):
        required = set(self.contract["manifest"]["requiredFields"])
        for field in required:
            self.assertIn(f"{field}=", self.backup, msg=field)
            self.assertIn(f"'{field}'", self.common, msg=field)

    def test_backup_success_and_failure_are_audited(self):
        self.assertIn("-Operation 'BACKUP' -Status 'PASS'", self.backup)
        self.assertIn("-Operation 'BACKUP' -Status 'FAIL'", self.backup)
        self.assertIn("sanitizedEvidence", self.backup)
        self.assertIn("LegalHoldReason=Assert-CpfBackupScalar", self.backup)

    def test_retention_decision_model(self):
        now = datetime(2026, 8, 5, tzinfo=timezone.utc)

        def decide(*, legal_hold: bool, expires: datetime, confirm: bool) -> str:
            if legal_hold:
                return "HELD"
            if expires > now:
                return "RETAINED"
            if not confirm:
                return "PURGE_CANDIDATE"
            return "PURGED"

        self.assertEqual("HELD", decide(legal_hold=True, expires=now - timedelta(days=10), confirm=True))
        self.assertEqual("RETAINED", decide(legal_hold=False, expires=now + timedelta(seconds=1), confirm=True))
        self.assertEqual("PURGE_CANDIDATE", decide(legal_hold=False, expires=now, confirm=False))
        self.assertEqual("PURGED", decide(legal_hold=False, expires=now, confirm=True))


if __name__ == "__main__":
    unittest.main()
