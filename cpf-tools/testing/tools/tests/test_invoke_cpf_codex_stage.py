from __future__ import annotations

import csv
import hashlib
import os
import re
import subprocess
import shutil
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/invoke-cpf-codex-stage.ps1"


@unittest.skipUnless(shutil.which("pwsh"), "PowerShell runtime required")
class InvokeCpfCodexStageTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.base = Path(self.temporary_directory.name)
        self.repository = self.base / "repository"
        self.ledger_root = self.base / "ledger"
        self.repository.mkdir()
        subprocess.run(["git", "init", "-q", str(self.repository)], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "config", "user.email", "qa37@example.test"],
            check=True,
        )
        subprocess.run(
            ["git", "-C", str(self.repository), "config", "user.name", "QA37 Test"],
            check=True,
        )
        (self.repository / "tracked.txt").write_text("baseline\n", encoding="utf-8")
        checkpoint = (
            self.repository
            / "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md"
        )
        checkpoint.parent.mkdir(parents=True)
        checkpoint.write_text("# checkpoint baseline\n", encoding="utf-8")
        subprocess.run(["git", "-C", str(self.repository), "add", "."], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "commit", "-qm", "initial"], check=True
        )

    def tearDown(self) -> None:
        self.temporary_directory.cleanup()

    @property
    def ledger_path(self) -> Path:
        return self.ledger_root / "execution-ledger.csv"

    def invoke(
        self,
        *,
        stage: str = "source",
        command: str = "Write-Output 'stage-ran'",
        vendor: str | None = "mariadb",
        environment_fingerprint: str | None = "docker-fixture-a",
        artifact: Path | None = None,
        require_artifact: bool = False,
        require_explicit_environment: bool = False,
        allow_rerun: bool = False,
    ) -> subprocess.CompletedProcess[str]:
        arguments = [
            "pwsh",
            "-NoProfile",
            "-File",
            str(SCRIPT),
            "-StageId",
            stage,
            "-Command",
            command,
            "-WorkingDirectory",
            str(self.repository),
            "-LedgerRoot",
            str(self.ledger_root),
        ]
        if vendor is not None:
            arguments.extend(["-DatabaseVendor", vendor])
        if environment_fingerprint is not None:
            arguments.extend(["-EnvironmentFingerprint", environment_fingerprint])
        if artifact is not None:
            arguments.extend(["-ArtifactPath", str(artifact)])
        if require_artifact:
            arguments.append("-RequireArtifact")
        if require_explicit_environment:
            arguments.append("-RequireExplicitEnvironmentFingerprint")
        if allow_rerun:
            arguments.append("-AllowRerun")
        return subprocess.run(
            arguments,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
        )

    def rows(self) -> list[dict[str, str]]:
        with self.ledger_path.open("r", encoding="utf-8-sig", newline="") as stream:
            return list(csv.DictReader(stream))

    def assert_success(self, result: subprocess.CompletedProcess[str]) -> None:
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)

    def test_matching_pass_is_skipped_only_with_valid_log_evidence(self) -> None:
        first = self.invoke()
        self.assert_success(first)
        first_rows = self.rows()
        self.assertEqual(1, len(first_rows))
        log_path = Path(first_rows[0]["logPath"])
        self.assertTrue(log_path.is_file())
        self.assertEqual(
            hashlib.sha256(log_path.read_bytes()).hexdigest(), first_rows[0]["logSha256"]
        )

        second = self.invoke()
        self.assert_success(second)
        self.assertIn("fully matching, evidence-valid PASS", second.stdout)
        self.assertEqual(1, len(self.rows()))

        allow_does_not_force_duplicate_pass = self.invoke(allow_rerun=True)
        self.assert_success(allow_does_not_force_duplicate_pass)
        self.assertIn("SKIP:", allow_does_not_force_duplicate_pass.stdout)
        self.assertEqual(1, len(self.rows()))

    def test_old_pass_missing_new_fields_is_invalid_and_reruns(self) -> None:
        self.ledger_root.mkdir()
        with self.ledger_path.open("w", encoding="utf-8", newline="") as stream:
            writer = csv.DictWriter(
                stream, fieldnames=["stageId", "status", "exitCode", "legacyNote"]
            )
            writer.writeheader()
            writer.writerow(
                {
                    "stageId": "source",
                    "status": "PASS",
                    "exitCode": "0",
                    "legacyNote": "preserve-me",
                }
            )

        result = self.invoke()
        self.assert_success(result)
        self.assertNotIn("SKIP:", result.stdout)
        rows = self.rows()
        self.assertEqual(2, len(rows))
        self.assertEqual("", rows[0]["worktreeFingerprint"])
        self.assertEqual("preserve-me", rows[0]["legacyNote"])
        self.assertRegex(rows[1]["worktreeFingerprint"], r"^[0-9a-f]{64}$")

    def test_head_command_and_dirty_worktree_changes_each_invalidate_pass(self) -> None:
        self.assert_success(self.invoke())

        (self.repository / "tracked.txt").write_text("committed change\n", encoding="utf-8")
        subprocess.run(["git", "-C", str(self.repository), "add", "tracked.txt"], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "commit", "-qm", "change head"], check=True
        )
        head_changed = self.invoke()
        self.assert_success(head_changed)
        self.assertNotIn("SKIP:", head_changed.stdout)

        command_changed = self.invoke(command="Write-Output 'different-command'")
        self.assert_success(command_changed)
        self.assertNotIn("SKIP:", command_changed.stdout)

        (self.repository / "tracked.txt").write_text("uncommitted change\n", encoding="utf-8")
        worktree_changed = self.invoke(command="Write-Output 'different-command'")
        self.assert_success(worktree_changed)
        self.assertNotIn("SKIP:", worktree_changed.stdout)
        self.assertEqual(4, len(self.rows()))

    def test_only_tracked_result_checkpoint_is_excluded_from_pass_fingerprint(self) -> None:
        self.assert_success(self.invoke())

        checkpoint = (
            self.repository
            / "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md"
        )
        checkpoint.write_text("# mandatory live checkpoint update\n", encoding="utf-8")
        checkpoint_only = self.invoke()
        self.assert_success(checkpoint_only)
        self.assertIn("SKIP:", checkpoint_only.stdout)
        self.assertEqual(1, len(self.rows()))

        (self.repository / "tracked.txt").write_text(
            "executable source/config change\n", encoding="utf-8"
        )
        tracked_change = self.invoke()
        self.assert_success(tracked_change)
        self.assertNotIn("SKIP:", tracked_change.stdout)
        self.assertEqual(2, len(self.rows()))

        (self.repository / "untracked-source.sql").write_text(
            "select 1;\n", encoding="utf-8"
        )
        untracked_change = self.invoke()
        self.assert_success(untracked_change)
        self.assertNotIn("SKIP:", untracked_change.stdout)
        self.assertEqual(3, len(self.rows()))

    def test_database_vendor_and_environment_fingerprint_changes_invalidate_pass(self) -> None:
        self.assert_success(self.invoke())
        vendor_changed = self.invoke(vendor="postgresql")
        self.assert_success(vendor_changed)
        self.assertNotIn("SKIP:", vendor_changed.stdout)
        environment_changed = self.invoke(
            vendor="postgresql", environment_fingerprint="docker-fixture-b"
        )
        self.assert_success(environment_changed)
        self.assertNotIn("SKIP:", environment_changed.stdout)
        rows = self.rows()
        self.assertEqual(3, len(rows))
        self.assertEqual(["mariadb", "postgresql", "postgresql"], [r["databaseVendor"] for r in rows])
        self.assertNotEqual(rows[1]["environmentFingerprint"], rows[2]["environmentFingerprint"])

    def test_tampered_or_missing_log_invalidates_pass(self) -> None:
        self.assert_success(self.invoke())
        first_log = Path(self.rows()[-1]["logPath"])
        first_log.write_text(first_log.read_text(encoding="utf-8") + "tampered\n", encoding="utf-8")

        tampered = self.invoke()
        self.assert_success(tampered)
        self.assertNotIn("SKIP:", tampered.stdout)
        latest_log = Path(self.rows()[-1]["logPath"])
        latest_log.unlink()

        missing = self.invoke()
        self.assert_success(missing)
        self.assertNotIn("SKIP:", missing.stdout)
        self.assertEqual(3, len(self.rows()))

    def test_required_artifact_path_and_hash_must_match(self) -> None:
        artifact = self.base / "result.sanitized.json"
        artifact.write_text('{"result":"one"}\n', encoding="utf-8")
        self.assert_success(self.invoke(artifact=artifact))
        first_rows = self.rows()
        self.assertEqual(hashlib.sha256(artifact.read_bytes()).hexdigest(), first_rows[0]["artifactSha256"])

        skipped = self.invoke(artifact=artifact)
        self.assert_success(skipped)
        self.assertIn("SKIP:", skipped.stdout)
        self.assertEqual(1, len(self.rows()))

        artifact.write_text('{"result":"two"}\n', encoding="utf-8")
        changed = self.invoke(artifact=artifact)
        self.assert_success(changed)
        self.assertNotIn("SKIP:", changed.stdout)
        self.assertEqual(2, len(self.rows()))

        artifact.unlink()
        missing = self.invoke(artifact=artifact)
        self.assertNotEqual(0, missing.returncode)
        latest = self.rows()[-1]
        self.assertEqual("FAIL", latest["status"])
        self.assertEqual("", latest["artifactSha256"])

    def test_required_artifact_cannot_be_omitted_and_directory_manifest_is_deterministic(self) -> None:
        omitted = self.invoke(stage="omitted", require_artifact=True)
        self.assertNotEqual(0, omitted.returncode)
        omitted_row = self.rows()[-1]
        self.assertEqual("FAIL", omitted_row["status"])
        self.assertEqual("true", omitted_row["artifactRequired"])
        self.assertEqual("", omitted_row["artifactPath"])

        evidence_directory = self.base / "evidence"
        (evidence_directory / "nested").mkdir(parents=True)
        (evidence_directory / "one.json").write_text("one\n", encoding="utf-8")
        (evidence_directory / "nested" / "two.log").write_text("two\n", encoding="utf-8")
        first = self.invoke(
            stage="directory",
            artifact=evidence_directory,
            require_artifact=True,
        )
        self.assert_success(first)
        directory_row = self.rows()[-1]
        self.assertEqual("directory", directory_row["artifactKind"])
        self.assertRegex(directory_row["artifactSha256"], r"^[0-9a-f]{64}$")

        skipped = self.invoke(
            stage="directory",
            artifact=evidence_directory,
            require_artifact=True,
        )
        self.assert_success(skipped)
        self.assertIn("SKIP:", skipped.stdout)

        (evidence_directory / "nested" / "two.log").write_text("changed\n", encoding="utf-8")
        changed = self.invoke(
            stage="directory",
            artifact=evidence_directory,
            require_artifact=True,
        )
        self.assert_success(changed)
        self.assertNotIn("SKIP:", changed.stdout)

    def test_db_or_runtime_contract_requires_explicit_environment_fingerprint(self) -> None:
        db_missing = self.invoke(
            stage="db",
            vendor="mariadb",
            environment_fingerprint=None,
        )
        self.assertNotEqual(0, db_missing.returncode)
        db_row = self.rows()[-1]
        self.assertEqual("FAIL", db_row["status"])
        self.assertEqual("true", db_row["explicitEnvironmentRequired"])
        self.assertEqual("false", db_row["environmentFingerprintExplicit"])

        runtime_missing = self.invoke(
            stage="runtime",
            vendor=None,
            environment_fingerprint=None,
            require_explicit_environment=True,
        )
        self.assertNotEqual(0, runtime_missing.returncode)
        self.assertEqual("FAIL", self.rows()[-1]["status"])

        runtime_explicit = self.invoke(
            stage="runtime-explicit",
            vendor=None,
            environment_fingerprint="runtime-docker-profile-a",
            require_explicit_environment=True,
        )
        self.assert_success(runtime_explicit)
        self.assertEqual("true", self.rows()[-1]["environmentFingerprintExplicit"])

    def test_database_vendor_is_restricted_to_official_values(self) -> None:
        unsupported = self.invoke(vendor="mysql")
        self.assertNotEqual(0, unsupported.returncode)
        self.assertFalse(self.ledger_path.exists())

    def test_fixed_qa37_database_stages_enforce_vendor_and_explicit_environment(self) -> None:
        expected_vendors = {
            "04_DB_MARIA": "mariadb",
            "05_DB_POSTGRES": "postgresql",
            "06_DB_ORACLE": "oracle",
        }
        wrong_vendors = {
            "mariadb": "oracle",
            "postgresql": "mariadb",
            "oracle": "postgresql",
        }

        for stage, expected_vendor in expected_vendors.items():
            with self.subTest(stage=stage, condition="omitted vendor"):
                omitted = self.invoke(
                    stage=stage,
                    vendor=None,
                    environment_fingerprint=f"{stage}-docker",
                )
                self.assertNotEqual(0, omitted.returncode)
                row = self.rows()[-1]
                self.assertEqual("FAIL", row["status"])
                self.assertEqual("unspecified", row["databaseVendor"])
                self.assertIn(
                    f"requires DatabaseVendor '{expected_vendor}'",
                    Path(row["logPath"]).read_text(encoding="utf-8"),
                )

            with self.subTest(stage=stage, condition="mismatched vendor"):
                mismatched = self.invoke(
                    stage=stage,
                    vendor=wrong_vendors[expected_vendor],
                    environment_fingerprint=f"{stage}-docker",
                    allow_rerun=True,
                )
                self.assertNotEqual(0, mismatched.returncode)
                row = self.rows()[-1]
                self.assertEqual("FAIL", row["status"])
                self.assertIn(
                    f"requires DatabaseVendor '{expected_vendor}'",
                    Path(row["logPath"]).read_text(encoding="utf-8"),
                )

            with self.subTest(stage=stage, condition="missing explicit environment"):
                missing_environment = self.invoke(
                    stage=stage,
                    vendor=expected_vendor,
                    environment_fingerprint=None,
                    allow_rerun=True,
                )
                self.assertNotEqual(0, missing_environment.returncode)
                row = self.rows()[-1]
                self.assertEqual("FAIL", row["status"])
                self.assertEqual(expected_vendor, row["databaseVendor"])
                self.assertEqual("true", row["explicitEnvironmentRequired"])
                self.assertEqual("false", row["environmentFingerprintExplicit"])

    def test_fixed_qa37_docker_sensitive_stages_require_explicit_environment(self) -> None:
        stage_ids = ("07_RUNTIME", "08_FAULT", "09_OTEL", "12_BROWSER", "13_SUPPLY")
        for stage in stage_ids:
            with self.subTest(stage=stage):
                result = self.invoke(
                    stage=stage,
                    command="Set-Content -LiteralPath 'must-not-run.txt' -Value 'bad'",
                    vendor=None,
                    environment_fingerprint=None,
                )
                self.assertNotEqual(0, result.returncode)
                row = self.rows()[-1]
                self.assertEqual("FAIL", row["status"])
                self.assertEqual("true", row["explicitEnvironmentRequired"])
                self.assertEqual("false", row["environmentFingerprintExplicit"])
                self.assertFalse((self.repository / "must-not-run.txt").exists())

    def test_command_source_mutation_is_failure_and_fail_requires_allow_rerun(self) -> None:
        mutation = self.invoke(
            command="Set-Content -LiteralPath 'tracked.txt' -Value 'changed' -Encoding utf8"
        )
        self.assertNotEqual(0, mutation.returncode)
        rows = self.rows()
        self.assertEqual("FAIL", rows[-1]["status"])
        self.assertEqual("true", rows[-1]["sourceSelfDirty"])
        self.assertNotEqual("0", rows[-1]["exitCode"])
        self.assertIn("changed Git HEAD or non-ignored worktree source", Path(rows[-1]["logPath"]).read_text(encoding="utf-8"))

        blocked = self.invoke(command="Write-Output 'would-rerun'")
        self.assertNotEqual(0, blocked.returncode)
        self.assertIn("previously failed", blocked.stdout + blocked.stderr)
        self.assertEqual(1, len(self.rows()))

        allowed = self.invoke(
            command="Write-Output 'allowed-rerun'", allow_rerun=True
        )
        self.assert_success(allowed)
        self.assertEqual(2, len(self.rows()))

    def test_safe_defaults_store_only_a_digest(self) -> None:
        result = self.invoke(vendor=None, environment_fingerprint=None)
        self.assert_success(result)
        row = self.rows()[0]
        self.assertEqual("unspecified", row["databaseVendor"])
        self.assertTrue(re.fullmatch(r"[0-9a-f]{64}", row["environmentFingerprint"]))
        self.assertNotIn(os.environ.get("COMPUTERNAME", "__not_present__"), row["environmentFingerprint"])


if __name__ == "__main__":
    unittest.main()
