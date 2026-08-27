from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools" / "db" / "tools" / "invoke-platform-database-migration.ps1"


class PlatformMigrationSafetyContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.text = SCRIPT.read_text(encoding="utf-8-sig")

    def test_only_encrypted_hashed_complete_backup_manifest_is_accepted(self):
        for token in [
            "Assert-CpfManifestHash", "Assert-CpfBackupManifest", "artifactFile",
            "artifactSha256", "Backup artifact checksum", "Backup manifest vendor",
        ]:
            self.assertIn(token, self.text)
        self.assertNotIn("$manifest.backupFile", self.text)
        self.assertNotIn("$manifest.sha256", self.text)

    def test_apply_requires_operator_reason_approval_and_reviewed_plan(self):
        for token in [
            '$Operator = Assert-CpfBackupScalar', '$Reason = Assert-CpfBackupScalar',
            '$ApprovalReference = Assert-CpfBackupScalar', 'ExpectedPlanSha256',
            'ConfirmApplicationsStopped', 'ConfirmRollbackReady',
        ]:
            self.assertIn(token, self.text)

    def test_partial_failure_is_unknown_and_reconcile_required(self):
        self.assertIn('status = "NOT_EXECUTED"', self.text)
        self.assertIn('status = "APPLYING"', self.text)
        self.assertIn('status = "COMPLETED"', self.text)
        self.assertIn('status = "UNKNOWN"', self.text)
        self.assertIn('$result.reconcileRequired = $true', self.text)
        self.assertIn('Never infer rollback from a non-zero client exit', self.text)

    def test_oracle_secret_transport_disables_echo_and_masks_escaped_form(self):
        for token in ["/nolog", "SET ECHO OFF", "SET VERIFY OFF", "SET DEFINE OFF", "RedirectStandardInput"]:
            self.assertIn(token, self.text)
        self.assertIn("$migrationPassword.Replace", self.text)
        self.assertIn("Sort-Object { $_.Length } -Descending -Unique", self.text)
        self.assertNotIn('ArgumentList.Add($connect)', self.text)
        self.assertIn(
            "$psi.StandardInputEncoding = [Text.UTF8Encoding]::new($false)", self.text
        )
        self.assertNotIn("$psi.StandardInputEncoding = [Text.Encoding]::UTF8", self.text)

    def test_immutable_oracle_compatibility_is_exact_digest_and_evidenced(self):
        for token in [
            "Convert-CpfOracleImmutableExecutionSql",
            "R140__remove_batch_remote_kafka_execution.sql",
            "d644d262458956aa035d537c2788388f41bf12ab61757937201e0144942c07d6",
            "ORACLE_COLUMN_DEFAULT_BEFORE_NOT_NULL_V1",
            "executionCompatibilityRule",
            "executionCompatibilityReplacements",
            "Unregistered Oracle immutable execution grammar",
            "compatibility digest mismatch",
            "compatibility match count mismatch",
        ]:
            self.assertIn(token, self.text)

        immutable = (
            ROOT
            / "cpf-tools/db/vendor/oracle/rollback/cpfDB/R140__remove_batch_remote_kafka_execution.sql"
        )
        import hashlib

        self.assertEqual(
            "d644d262458956aa035d537c2788388f41bf12ab61757937201e0144942c07d6",
            hashlib.sha256(immutable.read_bytes()).hexdigest(),
        )
        self.assertEqual(2, immutable.read_text(encoding="utf-8-sig").count("NOT NULL DEFAULT"))

    def test_control_character_injection_is_rejected(self):
        self.assertIn("Assert-CpfProcessScalar", self.text)
        for field in ["Target.host", "Target.databaseName", "Target.migrationUsername", "Target.migrationPassword"]:
            self.assertIn(field, self.text)

    def test_consolidated_modules_plan_one_verified_shared_database_owner(self):
        for token in [
            "$moduleKeysByLogicalDatabase",
            "$migrationTargetKeys",
            "$declaredOwners.Count -ne 1",
            "$connectionIdentities.Count -ne 1",
            "$migrationTargetKeys.Add($selectedTargetKey)",
        ]:
            self.assertIn(token, self.text)
        self.assertNotIn("logicalDatabase가 비어 있거나 중복되었습니다", self.text)

    def test_mariadb_forward_and_rollback_use_their_own_routing_entries(self):
        self.assertIn(
            "(Get-CpfMariaRoutingEntry $mariaRoutingManifest $migrationFile.Name)",
            self.text,
        )

    def test_verifier_owned_host_check_does_not_assign_powershell_host(self):
        self.assertIn("$targetHost = ([string]$target.host)", self.text)
        self.assertNotIn("$host = ([string]$target.host)", self.text)
        self.assertIn(
            "(Get-CpfMariaRoutingEntry $mariaRoutingManifest $rollbackFile.Name)",
            self.text,
        )

    def test_current_batch_telemetry_uses_central_runtime_lifecycle_owner(self):
        import json

        schema = json.loads(
            (ROOT / "cpf-tools/db/canonical/platform-schema.json").read_text(encoding="utf-8")
        )
        tables = {table["name"]: table for table in schema["tables"]}
        for name in ("BAT_RUNTIME_CAPABILITY", "BAT_RUNTIME_HEARTBEAT"):
            table = tables[name]
            instance = next(column for column in table["columns"] if column["name"] == "instance_id")
            foreign_key = next(
                key for key in table["foreignKeys"]
                if key["columns"] == ["instance_id"]
            )
            self.assertEqual("VARCHAR(120)", instance["type"], name)
            self.assertEqual("OPS_SERVICE_INSTANCE", foreign_key["refTable"], name)


if __name__ == "__main__":
    unittest.main()
