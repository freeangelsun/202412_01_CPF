from __future__ import annotations

import csv
import importlib.util
import json
import subprocess
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-qa37-source-closure.py"
spec = importlib.util.spec_from_file_location("qa37_source_closure", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class Qa37SourceClosureModeContractTest(unittest.TestCase):
    def fixture(self) -> tuple[Path, tempfile.TemporaryDirectory]:
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        root = Path(temporary.name)
        manifest_dir = root / "cpf-docs/work/manifest"
        manifest_dir.mkdir(parents=True)

        add_paths = [
            module.PACKAGE_MANIFEST,
            module.ROOT_OVERLAY_MANIFEST,
            "payload/source.txt",
        ]
        exclusions = [module.CHANGE_MANIFEST, "cpf-docs/work/manifest/CPF_20260801_QA37_FILES.sha256"]
        declared_count = len(add_paths) + len(exclusions)
        (root / "payload").mkdir()
        (root / "payload/source.txt").write_text("source\n", encoding="utf-8")
        (root / module.PACKAGE_MANIFEST).write_text(
            json.dumps({"fileCount": declared_count}), encoding="utf-8"
        )
        (root / module.ROOT_OVERLAY_MANIFEST).write_text(
            json.dumps(
                {
                    "fileCount": declared_count,
                    "changeManifestExclusions": exclusions,
                }
            ),
            encoding="utf-8",
        )
        with (root / module.CHANGE_MANIFEST).open("w", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=["path", "change_type"])
            writer.writeheader()
            for relative in add_paths:
                writer.writerow({"path": relative, "change_type": "ADD_OR_REPLACE"})
        (root / exclusions[1]).write_text("hash manifest\n", encoding="utf-8")
        return root, temporary

    def test_root_build_uses_gradle9_exec_provider_api(self) -> None:
        legacy = "def result = exec { commandLine 'git', 'status' }"
        supported = "def result = providers.exec { commandLine 'git', 'status' }.result.get()"
        self.assertIsNotNone(module.LEGACY_PROJECT_EXEC_PATTERN.search(legacy))
        self.assertIsNone(module.LEGACY_PROJECT_EXEC_PATTERN.search(supported))
        root_build = (SCRIPT.parents[2] / "build.gradle").read_text(encoding="utf-8")
        self.assertIsNone(module.LEGACY_PROJECT_EXEC_PATTERN.search(root_build))

    def test_build_scripts_use_named_canonical_stack_properties(self) -> None:
        legacy = 'rootProject.ext.cpfStack["dbSchedulerVersion"]'
        supported = "rootProject.ext.cpfDbSchedulerVersion"
        self.assertIsNotNone(module.LEGACY_STACK_MAP_PATTERN.search(legacy))
        self.assertIsNone(module.LEGACY_STACK_MAP_PATTERN.search(supported))
        repository = SCRIPT.parents[2]
        scheduler_build = (repository / "cpf-batch/scheduler/build.gradle").read_text(
            encoding="utf-8"
        )
        root_build = (repository / "build.gradle").read_text(encoding="utf-8")
        self.assertIsNone(module.LEGACY_STACK_MAP_PATTERN.search(scheduler_build))
        self.assertIn("ext.cpfDbSchedulerVersion", root_build)

    def test_overlay_count_is_add_rows_plus_declared_exclusions(self) -> None:
        root, _ = self.fixture()
        contract = module.load_overlay_file_contract(root)
        self.assertEqual(len(contract.add_or_replace), 3)
        self.assertEqual(len(contract.exclusions), 2)
        self.assertEqual(len(contract.expected_files), 5)

    def test_merged_repository_allows_unrelated_files(self) -> None:
        root, _ = self.fixture()
        unrelated = root / "unrelated/repository-source.java"
        unrelated.parent.mkdir()
        unrelated.write_text("class RepositorySource {}\n", encoding="utf-8")
        contract = module.load_overlay_file_contract(root)
        module.validate_overlay_file_presence(root, contract, overlay_package=False)

    def test_overlay_package_requires_exact_declared_file_set(self) -> None:
        root, _ = self.fixture()
        contract = module.load_overlay_file_contract(root)
        module.validate_overlay_file_presence(root, contract, overlay_package=True)
        (root / "unexpected.txt").write_text("unexpected\n", encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.validate_overlay_file_presence(root, contract, overlay_package=True)

    def test_merged_repository_allows_remediated_overlay_path_deletion(self) -> None:
        root, _ = self.fixture()
        contract = module.load_overlay_file_contract(root)
        (root / "payload/source.txt").unlink()
        module.validate_overlay_file_presence(root, contract, overlay_package=False)

    def test_manifest_count_must_match_deterministic_overlay_count(self) -> None:
        root, _ = self.fixture()
        package_path = root / module.PACKAGE_MANIFEST
        package = json.loads(package_path.read_text(encoding="utf-8"))
        package["fileCount"] += 1
        package_path.write_text(json.dumps(package), encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.load_overlay_file_contract(root)

    def test_unknown_change_type_is_rejected(self) -> None:
        root, _ = self.fixture()
        with (root / module.CHANGE_MANIFEST).open("a", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=["path", "change_type"])
            writer.writerow({"path": "payload/unknown.txt", "change_type": "UNKNOWN"})
        with self.assertRaises(SystemExit):
            module.load_overlay_file_contract(root)

    def test_change_manifest_paths_are_globally_unique(self) -> None:
        root, _ = self.fixture()
        with (root / module.CHANGE_MANIFEST).open("a", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=["path", "change_type"])
            writer.writerow(
                {"path": "payload/source.txt", "change_type": "DELETE_REVIEW_REQUIRED"}
            )
        with self.assertRaises(SystemExit):
            module.load_overlay_file_contract(root)

    def test_delete_review_rows_must_equal_delete_manifest_paths(self) -> None:
        root, _ = self.fixture()
        with (root / module.CHANGE_MANIFEST).open("a", encoding="utf-8", newline="") as handle:
            writer = csv.DictWriter(handle, fieldnames=["path", "change_type"])
            writer.writerow(
                {
                    "path": "cpf-docs/work/current/stale.md",
                    "change_type": "DELETE_REVIEW_REQUIRED",
                }
            )
        contract = module.load_overlay_file_contract(root)
        module.validate_delete_review_contract(
            contract, ["cpf-docs/work/current/stale.md"]
        )
        with self.assertRaises(SystemExit):
            module.validate_delete_review_contract(
                contract, ["cpf-docs/work/current/different.md"]
            )

    def git_fixture(self) -> Path:
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        root = Path(temporary.name)
        subprocess.run(["git", "init", "--quiet", str(root)], check=True)
        (root / ".gitignore").write_text("build/\nignored-secret.txt\n", encoding="utf-8")
        tracked = root / "tracked.json"
        tracked.write_text('{"status":"valid"}\n', encoding="utf-8")
        ignored_build = root / "build/bad.json"
        ignored_build.parent.mkdir()
        ignored_build.write_text("not-json\n", encoding="utf-8")
        (root / "ignored-secret.txt").write_text(
            "-----BEGIN " + "PRIVATE KEY-----\n", encoding="utf-8"
        )
        subprocess.run(
            ["git", "-C", str(root), "add", ".gitignore", "tracked.json"], check=True
        )
        return root

    def test_git_source_traversal_includes_untracked_and_ignores_outputs(self) -> None:
        root = self.git_fixture()
        (root / "untracked.json").write_text('{"status":"valid"}\n', encoding="utf-8")

        files = module.git_source_files(root)
        relative = {path.relative_to(root).as_posix() for path in files}
        self.assertEqual(relative, {".gitignore", "tracked.json", "untracked.json"})
        module.check_syntax(root, files)
        module.check_secrets(root, files)

    def test_git_source_traversal_skips_intentional_tracked_deletions(self) -> None:
        root = self.git_fixture()
        (root / "tracked.json").unlink()

        files = module.git_source_files(root)
        relative = {path.relative_to(root).as_posix() for path in files}
        self.assertEqual(relative, {".gitignore"})

    def test_nonignored_untracked_invalid_json_is_rejected(self) -> None:
        root = self.git_fixture()
        (root / "bad.json").write_text("not-json\n", encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.check_syntax(root, module.git_source_files(root))

    def test_nonignored_untracked_secret_is_rejected(self) -> None:
        root = self.git_fixture()
        (root / "worktree-secret.txt").write_text(
            "-----BEGIN " + "PRIVATE KEY-----\n", encoding="utf-8"
        )
        with self.assertRaises(SystemExit):
            module.check_secrets(root, module.git_source_files(root))

    def test_canonical_placeholders_masking_and_expressions_are_allowed(self) -> None:
        placeholders = [
            "${CPF_DB_PASSWORD:__REPLACE_BY_ENV__}",
            "<provided-by-operator>",
            "REDACTED",
            "CHANGE_ME",
            "__SET_BY_SECRET_PROVIDER__",
            "__REPLACE_BY_ENV__",
            "__REPLACE_BY_SECRET_PROVIDER__",
            "********",
            "ab****yz",
        ]
        for placeholder in placeholders:
            with self.subTest(placeholder=placeholder):
                reasons = module.find_secret_reasons(
                    Path("deploy/env/service.env"), f"CPF_DB_PASSWORD={placeholder}\n"
                )
                self.assertEqual(reasons, set())

        java = "this.agentToken = tokenProvider.resolve();\n"
        sql = "fencing_token = fencing_token + 1,\n"
        powershell = "$DatabasePassword = Get-CpfSecret -Name $SecretName\n"
        commented = '// String password = "N0t' + '!A-Credential";\n'
        self.assertEqual(module.find_secret_reasons(Path("Main.java"), java), set())
        self.assertEqual(module.find_secret_reasons(Path("runtime.sql"), sql), set())
        self.assertEqual(module.find_secret_reasons(Path("runtime.ps1"), powershell), set())
        self.assertEqual(module.find_secret_reasons(Path("Main.java"), commented), set())

    def test_json_dotenv_environment_and_provider_references_are_allowed(self) -> None:
        cases = {
            Path(".env"): "CPF_DB_PASSWORD=$DB_PASSWORD\n",
            Path("service.env"): "CPF_DB_PASSWORD=%DB_PASSWORD%\n",
            Path("application.json"): '{"password": "${DB_PASSWORD}"}\n',
            Path("policy.json"): '{"unsupportedDatabaseTokens": ["mysql", "h2"]}\n',
            Path("application.yml"): "password: vault://cpf/runtime/database\n",
            Path("application.properties"): "api.secret=secretProvider.resolve(DB_SECRET)\n",
        }
        for path, source in cases.items():
            with self.subTest(path=path):
                self.assertEqual(module.find_secret_reasons(path, source), set())

    def test_high_confidence_config_and_quoted_source_credentials_are_rejected(self) -> None:
        credential = "S3cur3" + "!P@ss.word"
        cases = {
            Path("application.yml"): f'password: "{credential}"\n',
            Path("Main.java"): f'String apiToken = "{credential}";\n',
            Path("runtime.ps1"): f'$DatabasePassword = "{credential}"\n',
            Path("runtime.sql"): f"password = '{credential}'\n",
        }
        for path, source in cases.items():
            with self.subTest(path=path):
                self.assertIn(
                    "hard-coded-credential-assignment",
                    module.find_secret_reasons(path, source),
                )

    def test_json_dotenv_and_inline_comment_literals_are_rejected(self) -> None:
        credential = "Cr3d" + "!ble.Config"
        cases = {
            Path("application.json"): f'{{"password": "{credential}"}}\n',
            Path(".env"): f"DB_PASSWORD={credential} # injected by operator\n",
            Path("application.yml"): f"password: {credential} # runtime value\n",
        }
        for path, source in cases.items():
            with self.subTest(path=path):
                self.assertIn(
                    "hard-coded-credential-assignment",
                    module.find_secret_reasons(path, source),
                )

    def test_production_sentinel_substrings_and_spring_defaults_are_rejected(self) -> None:
        sentinel_substring = "RealSample" + "Secret123!"
        spring_default = "RealS3" + "cret!"
        cases = {
            Path("application.yml"): f"password: {sentinel_substring}\n",
            Path("application.properties"): (
                "db.password=${DB_PASSWORD:" + spring_default + "}\n"
            ),
        }
        for path, source in cases.items():
            with self.subTest(path=path):
                self.assertIn(
                    "hard-coded-credential-assignment",
                    module.find_secret_reasons(path, source),
                )

    def test_test_paths_only_allow_clearly_synthetic_fixture_credentials(self) -> None:
        synthetic = "T3st" + "!Password"
        credible = "Pr0d" + "!Cred3ntial.Value"
        path = Path("src/test/java/CredentialTest.java")
        self.assertEqual(
            module.find_secret_reasons(path, f'String password = "{synthetic}";\n'),
            set(),
        )
        self.assertIn(
            "hard-coded-credential-assignment",
            module.find_secret_reasons(path, f'String password = "{credible}";\n'),
        )

    def test_identifier_labels_and_test_fixtures_are_not_credentials(self) -> None:
        metadata = 'private static final String AGENT_TOKEN_HEADER = "X-CPF-RUNTIME-TOKEN";\n'
        uppercase_sentinel = '$DatabasePassword = "CPF_QA37_ROOT_PASSWORD"\n'
        verification_identifier = '$DatabasePassword = "CPF_DOMAIN_ROOT_PASSWORD"\n'
        property_key = 'def datasourcePasswordKey = "spring.datasource.password"\n'
        api_header = 'public static final String API_KEY = "X-API-KEY";\n'
        secret_reference = "secretReferences: [CPF_DB_PASSWORD, CPF_API_TOKEN]\n"
        result_token = "'typed-result-token': 'implementation'\n"
        fixture_value = "T3st" + "!Password"
        fixture = f'private static final String INITIAL_PASSWORD = "{fixture_value}";\n'
        self.assertEqual(module.find_secret_reasons(Path("Main.java"), metadata), set())
        self.assertEqual(
            module.find_secret_reasons(Path("verify.ps1"), uppercase_sentinel), set()
        )
        self.assertEqual(
            module.find_secret_reasons(Path("verify.ps1"), verification_identifier), set()
        )
        self.assertEqual(module.find_secret_reasons(Path("build.gradle"), property_key), set())
        self.assertEqual(module.find_secret_reasons(Path("Headers.java"), api_header), set())
        self.assertEqual(
            module.find_secret_reasons(Path("deployment.yml"), secret_reference), set()
        )
        self.assertEqual(module.find_secret_reasons(Path("verify.py"), result_token), set())
        self.assertEqual(
            module.find_secret_reasons(Path("src/test/java/PasswordTest.java"), fixture),
            set(),
        )

    def test_token_ttl_metadata_is_not_classified_as_a_token_credential(self) -> None:
        token_lifecycle_config = """
access-token-ttl-seconds: ${CPF_ACCESS_TOKEN_TTL_SECONDS:900}
refresh-token-ttl-seconds: ${CPF_REFRESH_TOKEN_TTL_SECONDS:604800}
download-token-ttl-seconds: ${CPF_DOWNLOAD_TOKEN_TTL_SECONDS:300}
"""
        self.assertEqual(
            module.find_secret_reasons(Path("application.yml"), token_lifecycle_config),
            set(),
        )

    def test_aws_key_private_key_and_well_known_token_patterns_remain_strong(self) -> None:
        aws = "AKIA" + "1234567890ABCDEF"
        temporary_aws = "ASIA" + "1234567890ABCDEF"
        private_key = "-----BEGIN " + "PRIVATE KEY-----"
        github_token = "ghp_" + "a" * 36
        reasons = module.find_secret_reasons(
            Path("notes.txt"),
            "\n".join((aws, temporary_aws, private_key, github_token)),
        )
        self.assertEqual(reasons, {"aws-access-key", "private-key", "well-known-token"})

    def test_nonignored_untracked_banned_output_is_rejected(self) -> None:
        root = self.git_fixture()
        output = root / "dist/result.txt"
        output.parent.mkdir()
        output.write_text("generated\n", encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.check_syntax(root, module.git_source_files(root))

    def test_tools_build_source_is_allowed_but_nested_outputs_are_rejected(self) -> None:
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        root = Path(temporary.name)
        source = root / "cpf-tools/build/gradle-plugin/src/Plugin.java"
        source.parent.mkdir(parents=True)
        source.write_text("class Plugin {}\n", encoding="utf-8")
        module.check_syntax(root, [source])

        nested_build = root / "cpf-tools/build/gradle-plugin/build/result.txt"
        nested_build.parent.mkdir(parents=True)
        nested_build.write_text("generated\n", encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.check_syntax(root, [nested_build])

        nested_dependencies = root / "cpf-tools/build/platform-bom/node_modules/pkg/index.js"
        nested_dependencies.parent.mkdir(parents=True)
        nested_dependencies.write_text("generated\n", encoding="utf-8")
        with self.assertRaises(SystemExit):
            module.check_syntax(root, [nested_dependencies])


if __name__ == "__main__":
    unittest.main()
