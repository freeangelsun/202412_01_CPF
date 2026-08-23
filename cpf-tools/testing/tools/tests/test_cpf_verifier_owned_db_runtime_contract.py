from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[4]
DBV = ROOT / "cpf-tools/db/verification"
FULL = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"


class CpfVerifierOwnedDbRuntimeContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.lifecycle = (DBV / "invoke-cpf-db-verifier-owned-lifecycle.ps1").read_text(encoding="utf-8")
        cls.prepare = (DBV / "prepare-cpf-local-runtime-db.ps1").read_text(encoding="utf-8")
        cls.cleanup = (DBV / "cleanup-cpf-db-verifier-owned.ps1").read_text(encoding="utf-8")
        cls.cleanup_local = (DBV / "cleanup-cpf-local-runtime-db.ps1").read_text(encoding="utf-8")
        cls.bootstrap = (DBV / "prepare-cpf-local-backoffice-bootstrap.ps1").read_text(encoding="utf-8")
        cls.full = FULL.read_text(encoding="utf-8")

    def test_verifier_owned_lifecycle_is_isolated_and_fail_closed(self):
        self.assertIn("^[0-9a-f]{40}$", self.lifecycle)
        self.assertIn("CPF_ADMIN_PASSWORD is required", self.lifecycle)
        self.assertIn("cpf_verify_${runId}_platform", self.lifecycle)
        self.assertIn("cpf_verify_${runId}_mbw", self.lifecycle)
        self.assertIn("-VerifierOwnedDisposable", self.lifecycle)
        self.assertIn("-ConfirmExecute", self.lifecycle)
        self.assertIn("-ConfirmApplicationsStopped", self.lifecycle)
        self.assertIn("-ConfirmRollbackReady", self.lifecycle)
        self.assertIn("Invoke-PreCurrentRollback", self.lifecycle)
        self.assertIn("RollbackReapply", self.lifecycle)
        self.assertIn("Get-CurrentEdgeVersionFromChecksumManifests", self.lifecycle)
        self.assertIn("CHECKSUM_MANIFEST_CURRENT_EDGE", self.lifecycle)
        self.assertIn("migrationSelection=$currentEdgeSelection", self.lifecycle)
        self.assertIn("Official vendors disagree on the current migration edge", self.lifecycle)
        self.assertIn("@{MigrationVersion=$currentEdgeVersion}", self.lifecycle)
        self.assertIn("@selectionParameters", self.lifecycle)
        self.assertNotIn("@selectionArgs", self.lifecycle)
        self.assertGreaterEqual(self.lifecycle.count("-MigrationVersion"), 3)
        self.assertIn("cleanup-cpf-db-verifier-owned.ps1", self.lifecycle)
        self.assertIn("if($result.status -ne 'PASS')", self.lifecycle)

    def test_local_runtime_db_uses_env_secrets_and_run_scoped_names(self):
        self.assertIn("cpf_verify_${VerifierRunId}_runtime", self.prepare)
        self.assertIn("cpf_verify_${VerifierRunId}_mbw", self.prepare)
        for name in (
            "CPF_ADMIN_PASSWORD",
            "CPF_LOCAL_RUNTIME_DB_MIGRATION_PASSWORD",
            "CPF_LOCAL_RUNTIME_DB_PASSWORD",
        ):
            self.assertIn(name, self.prepare)
        self.assertNotIn("password='", self.prepare.lower())
        self.assertIn("-RequireRun", self.prepare)
        self.assertIn("@('core','common','admin','batch','backoffice')", self.prepare)
        self.assertIn("$isBackoffice=($key -eq 'backoffice')", self.prepare)
        self.assertNotIn("bizAdmin", self.prepare)

    def test_cleanup_refuses_non_verifier_targets(self):
        self.assertIn("Refusing verifier cleanup for environment", self.cleanup)
        self.assertIn("Refusing verifier cleanup for host", self.cleanup)
        self.assertIn('cpf_verify_${VerifierRunId}_', self.cleanup)
        self.assertIn('cpfv_${VerifierRunId}_', self.cleanup)
        self.assertIn("DROP DATABASE IF EXISTS", self.cleanup)
        self.assertIn("DROP ROLE IF EXISTS", self.cleanup)
        self.assertIn("DROP USER", self.cleanup)
        self.assertNotIn("DROP DATABASE cpf_", self.cleanup)

    def test_local_cleanup_runs_inside_docker_not_host_db_client(self):
        self.assertIn("docker", self.cleanup_local)
        self.assertIn("cleanup-cpf-db-verifier-owned.ps1", self.cleanup_local)
        self.assertIn("target=/workspace/cpf,readonly", self.cleanup_local)
        self.assertIn("target=/workspace/result,readonly", self.cleanup_local)
        self.assertNotIn("& mariadb", self.cleanup_local)
        self.assertNotIn("& psql", self.cleanup_local)
        self.assertNotIn("sqlplus", self.cleanup_local)

    def test_backoffice_bootstrap_is_run_scoped_and_secret_safe(self):
        self.assertIn('^cpf_verify_${VerifierRunId}_mbw$', self.bootstrap)
        self.assertIn("CPF_BACKOFFICE_SMOKE_PASSWORD", self.bootstrap)
        self.assertIn("CPF_ADMIN_PASSWORD", self.bootstrap)
        self.assertIn("chmod 600", self.bootstrap)
        self.assertIn('MARIADB_ROOT_PASSWORD', self.bootstrap)
        self.assertNotIn('mariadb -p', self.bootstrap.lower())
        self.assertIn("Remove-Item -LiteralPath $sqlFile -Force", self.bootstrap)
        self.assertIn("sanitized=$true", self.bootstrap)

    def test_full_local_owns_runtime_db_and_cleans_it(self):
        for token in (
            "LOCAL_ONE_WAS_DB_PREP",
            "prepare-cpf-local-runtime-db.ps1",
            "LOCAL_ONE_WAS_BACKOFFICE_BOOTSTRAP_PREP",
            "prepare-cpf-local-backoffice-bootstrap.ps1",
            "LOCAL_ONE_WAS_DB_CLEANUP",
            "cleanup-cpf-local-runtime-db.ps1",
            "LOCAL_ONE_WAS_SECRET_CLEANUP",
        ):
            self.assertIn(token, self.full)
        self.assertIn("CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CUSTOMER_BUSINESS_DB_ENABLED='true'", self.full)
        self.assertIn("CPF_LOG_ROOT=$runtimeFileLogRoot", self.full)


if __name__ == "__main__":
    unittest.main()
