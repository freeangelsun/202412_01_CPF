from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/tools/run-cpf-local-full-validation.ps1"


class CpfLocalFullValidationContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.text = SCRIPT.read_text(encoding="utf-8")

    def test_home_download_and_continue_contract(self):
        self.assertIn("Join-Path $HOME 'Downloads'", self.text)
        self.assertIn("--continue", self.text)
        self.assertIn("SUMMARY.csv", self.text)
        self.assertIn("Compress-Archive", self.text)
        self.assertIn("CPF_LOCAL_VALIDATION_ZIP", self.text)
        self.assertIn("$strictExitEffective=[bool]$StrictExit -or [bool]$FullLocal", self.text)
        self.assertIn("--expected-requirements','205','--expected-findings','63", self.text)
        self.assertIn("cpfPublishToIsolatedLocal", self.text)
        self.assertNotIn("publishToMavenLocal", self.text)
        self.assertIn("BaselineSourceZipSha256", self.text)
        self.assertIn("if($strictExitEffective -and ($fail -gt 0 -or $skip -gt 0 -or $notExecuted -gt 0)){exit 1}", self.text)

    def test_low_memory_sequential_contract(self):
        self.assertIn("-PcpfSkipFrontendBuild=true", self.text)
        self.assertLess(self.text.index("GRADLE_FULL_BUILD_QUALITY"), self.text.index("${name}_NPM_CI"))
        self.assertLess(self.text.index("${name}_NPM_CI"), self.text.index("${name}_FRONTEND_VERIFY"))
        self.assertLess(self.text.index("${name}_FRONTEND_VERIFY"), self.text.index("GRADLE_ASSEMBLE_AFTER_FRONTEND"))
        self.assertIn("frontend.node.maxOldSpace.mb", self.text)
        self.assertIn("--no-parallel", self.text)
        self.assertIn("LOCAL_ONE_WAS_START", self.text)

    def test_python_bootstrap_returns_only_python_executable(self):
        self.assertIn("PYTHON_BOOTSTRAP.log", self.text)
        self.assertIn("$pipOutput=@(& $venvPython -m pip install", self.text)
        self.assertIn("return [string]$venvPython", self.text)

    def test_live_and_destructive_steps_are_opt_in(self):
        self.assertIn("[switch] $IncludeDbRuntime", self.text)
        self.assertIn("[switch] $IncludeRuntimeClosure", self.text)
        self.assertIn("[switch] $IncludeBrowserE2E", self.text)
        self.assertIn("[switch] $FullLocal", self.text)
        self.assertIn("[switch] $AllowDestructiveDbRollback", self.text)
        self.assertIn("IncludeDbRuntime not requested", self.text)
        self.assertIn("IncludeRuntimeClosure not requested", self.text)
        self.assertIn("IncludeBrowserE2E not requested", self.text)
        self.assertIn("$IncludeDbRuntime=$true", self.text)
        self.assertIn("$IncludeRuntimeClosure=$true", self.text)
        self.assertIn("$IncludeBrowserE2E=$true", self.text)
        self.assertIn("'-VerifierOwnedIsolation'", self.text)
        self.assertIn("FullLocal 1-WAS requires verifier-owned MariaDB", self.text)
        self.assertIn("cleanup-cpf-local-runtime-db.ps1", self.text)

    def test_gradle_ux_quality_and_integration_are_all_in_full_local(self):
        for stage in (
            "GRADLE_PROJECTS", "GRADLE_HELP", "GRADLE_CPF_HELP", "GRADLE_CPF_MODULES",
            "GRADLE_FULL_BUILD_QUALITY", "GRADLE_ALL_JAVA_TESTS",
            "GRADLE_QA34_INTEGRATION", "GRADLE_PUBLICATION",
            "GRADLE_ASSEMBLE_AFTER_FRONTEND", "GRADLE_SBOM",
        ):
            self.assertIn(stage, self.text)
        self.assertLess(self.text.index("GRADLE_PROJECTS"), self.text.index("GRADLE_FULL_BUILD_QUALITY"))
        self.assertLess(self.text.index("GRADLE_FULL_BUILD_QUALITY"), self.text.index("GRADLE_PUBLICATION"))

    def test_codex_high_value_static_and_runtime_contracts_are_in_full_local(self):
        for stage in (
            "CODEX_COMMON_PRODUCT_SERVICE_DX", "CODEX_TRANSACTION_ID_CONTRACT",
            "CODEX_TXID_ALL_CHANNEL", "CODEX_CACHE_DB3_LIFECYCLE",
            "CODEX_FRONTEND_GOLDEN_PATH", "CODEX_INTEGRATION_CLOSURE_CONTRACT",
            "CODEX_BUSINESS_FRAMEWORK_CROSSCUT", "CODEX_EVENT_SCHEMA_CAPABILITY",
            "CODEX_OBJECT_STORAGE_CAPABILITY", "CODEX_ADMIN_DEPENDENCY_BOUNDARY",
            "CODEX_GRADLE_DEPENDENCY_CLOSURE", "CODEX_OWNER_BOUNDARIES",
            "CODEX_NO_PARTIAL_IMPLEMENTATION", "CODEX_STARTER_CATALOG",
            "CODEX_RETIRED_STARTER_DEPENDENCIES", "CODEX_COMMON_VALIDATION_OWNER",
            "CODEX_DOMAIN_EXCEPTION_ENFORCEMENT", "CODEX_LOGGING_DX",
            "CODEX_TESTKIT_CONTRACT", "CODEX_ZERO_FOOTPRINT",
            "CODEX_SPRING_ROUTE_UNIQUENESS", "CODEX_ASYNCAPI_LIFECYCLE",
            "CODEX_AUDIT_FAIL_CLOSED", "CODEX_TELEMETRY_LIFECYCLE",
            "CODEX_NETWORK_POLICY_CONSUMERS", "CODEX_NOTIFICATION_INCIDENT_LIFECYCLE",
            "CODEX_FRONTEND_CONSUMER_CLOSURE", "CODEX_EXECUTION_SCOPE_EXHAUSTIVE",
        ):
            self.assertIn(stage, self.text)
        self.assertIn("CPF_EXECUTION_AUDIT_CSV", self.text)
        self.assertIn("CPF_EXECUTION_WORK_PACKAGE_CSV", self.text)

        for stage in (
            "TRANSACTION_HEADER_STANDARD", "FIXED_LENGTH_CLOSURE", "EVENT_DLQ_APPROVAL_OWNER",
            "APPROVAL_STATE_MACHINE", "DANGEROUS_ACTION_APPROVAL", "OPERATOR_TRUST_BOUNDARY",
            "INTERNAL_SERVICE_IDENTITY", "THREAT_MODELS", "SECURITY_SESSION_OIDC",
            "GENERATOR_FULL_CONTRACT", "CACHE_CORRECTNESS", "CACHE_DURABLE_LIFECYCLE",
            "CONTEXT_ARCH_RUNTIME", "CONTEXT_RUNTIME_LIFECYCLE", "INTEGRATION_CONTEXT_RUNTIME",
            "MESSAGE_CONTEXT_RUNTIME", "BATCH_CONTEXT_RUNTIME", "SECURITY_CONTEXT_RUNTIME",
            "BATCH_UNKNOWN_RECONCILIATION", "WINDOWS_PATH_COMPATIBILITY", "GATEWAY_STATIC_CLOSURE",
            "MESSAGING_KAFKA_RELIABILITY", "BATCH_TWO_WORKER_CRASH_UNKNOWN",
        ):
            self.assertIn(stage, self.text)

    def test_full_local_browser_and_owned_db_lifecycle_are_fail_closed(self):
        self.assertIn("CPF_ADM_FRONTEND_URL='http://127.0.0.1:8080/adm/'", self.text)
        self.assertIn("CPF_BACKOFFICE_FRONTEND_URL", self.text)
        self.assertIn("$browserSecretPrevious=Import-CpfEnvFile $DockerSecretFile", self.text)
        self.assertIn("'-BrowserClick','-RequireBrowserClick'", self.text)
        self.assertIn("CPF_ADM_SMOKE_PASSWORD=$browserAdminPassword", self.text)
        self.assertIn("FullLocal requires a real authenticated ADM browser flow", self.text)
        self.assertIn("Restore-CpfEnvironment $browserSecretPrevious", self.text)
        self.assertIn("'-VerifierOwnedIsolation'", self.text)
        self.assertIn("FullLocal 1-WAS requires verifier-owned MariaDB", self.text)
        self.assertIn("cleanup-cpf-local-runtime-db.ps1", self.text)


if __name__ == "__main__":
    unittest.main()
