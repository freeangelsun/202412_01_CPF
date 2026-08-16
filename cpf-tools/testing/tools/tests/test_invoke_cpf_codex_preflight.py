from __future__ import annotations

import json
import re
import subprocess
import shutil
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/tools/invoke-cpf-codex-preflight.ps1"

REQUIRED_DOCUMENTS = (
    "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",
    "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv",
    "cpf-docs/work/REQUIREMENT_STATUS.csv",
    "cpf-docs/work/QA_FINDING_REVALIDATION.csv",
    "cpf-docs/work/TEST_AND_EVIDENCE.md",
    "cpf-docs/work/OPEN_ISSUES.md",
    "cpf-docs/work/REVIEW_INDEX.md",
    "cpf-docs/work/current/CPF_CODEX_REVALIDATION_SCOPE.md",
    "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md",
)

RESTORED_BUILD_SOURCES = (
    "cpf-tools/build/gradle-plugin/build.gradle",
    "cpf-tools/build/gradle-plugin/settings.gradle",
    "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java",
    "cpf-tools/build/gradle-plugin/src/test/java/com/cpf/gradle/CpfPlatformConventionPluginTest.java",
    "cpf-tools/build/platform-bom/build.gradle",
    "cpf-tools/build/platform-bom/settings.gradle",
)


@unittest.skipUnless(shutil.which("pwsh"), "PowerShell runtime required")
class InvokeCpfCodexPreflightTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.base = Path(self.temporary_directory.name)
        self.repository = self.base / "repository"
        self.docker_root = self.base / "docker"
        self.repository.mkdir()
        self.docker_root.mkdir()
        self.invocation = 0

        subprocess.run(["git", "init", "-q", str(self.repository)], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "config", "user.email", "preflight@example.test"],
            check=True,
        )
        subprocess.run(
            ["git", "-C", str(self.repository), "config", "user.name", "Preflight Test"],
            check=True,
        )

        for relative_path in REQUIRED_DOCUMENTS + RESTORED_BUILD_SOURCES:
            target = self.repository / relative_path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(f"fixture: {relative_path}\n", encoding="utf-8")
        (self.repository / "tracked.txt").write_text("baseline\n", encoding="utf-8")

        subprocess.run(["git", "-C", str(self.repository), "add", "."], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "commit", "-qm", "initial"],
            check=True,
        )
        self.head = subprocess.run(
            ["git", "-C", str(self.repository), "rev-parse", "HEAD"],
            check=True,
            capture_output=True,
            text=True,
            encoding="utf-8",
        ).stdout.strip()
        subprocess.run(
            [
                "git",
                "-C",
                str(self.repository),
                "update-ref",
                "refs/remotes/origin/master",
                self.head,
            ],
            check=True,
        )

    def tearDown(self) -> None:
        self.temporary_directory.cleanup()

    def invoke(
        self,
        *,
        require_clean: bool = False,
        require_tracked_build_sources: bool = False,
        expected_head: str | None = None,
    ) -> tuple[subprocess.CompletedProcess[str], dict[str, object]]:
        self.invocation += 1
        output = self.base / f"preflight-{self.invocation}.json"
        arguments = [
            "pwsh",
            "-NoProfile",
            "-File",
            str(SCRIPT),
            "-RepoRoot",
            str(self.repository),
            "-DockerRoot",
            str(self.docker_root),
            "-ExpectedHead",
            self.head if expected_head is None else expected_head,
            "-OutputPath",
            str(output),
        ]
        if require_clean:
            arguments.append("-RequireCleanWorktree")
        if require_tracked_build_sources:
            arguments.append("-RequireTrackedBuildSources")
        result = subprocess.run(
            arguments,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
        )
        self.assertTrue(output.is_file(), result.stdout + result.stderr)
        return result, json.loads(output.read_text(encoding="utf-8-sig"))

    def test_current_v2_required_paths_and_portable_repo_contract_pass(self) -> None:
        result, evidence = self.invoke()
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertEqual(2, evidence["schemaVersion"])
        self.assertTrue(evidence["sourceReady"])
        self.assertEqual(str(self.repository.resolve()), evidence["repoRoot"])
        self.assertEqual(list(REQUIRED_DOCUMENTS), evidence["repositoryContracts"]["requiredDocuments"])
        self.assertEqual([], evidence["repositoryContracts"]["missingDocuments"])
        self.assertEqual([], evidence["repositoryContracts"]["missingRestoredBuildSources"])
        self.assertTrue(re.fullmatch(r"[0-9a-f]{64}", evidence["git"]["worktreeFingerprint"]))

        source = SCRIPT.read_text(encoding="utf-8")
        self.assertNotIn(r"C:\dev\projects\jck\202412_01_CPF", source)
        self.assertNotIn("cpf-docs/work/codex/qa37", source)

    def test_dirty_wip_is_hashed_by_default_and_clean_switch_fails_closed(self) -> None:
        wip = self.repository / "untracked-wip.sql"
        wip.write_text("select 1;\n", encoding="utf-8")
        default_result, default_evidence = self.invoke()
        self.assertEqual(0, default_result.returncode, default_result.stdout + default_result.stderr)
        self.assertTrue(default_evidence["sourceReady"])
        self.assertFalse(default_evidence["git"]["clean"])
        first_hash = default_evidence["git"]["worktreeFingerprint"]

        wip.write_text("select 2;\n", encoding="utf-8")
        changed_result, changed_evidence = self.invoke()
        self.assertEqual(0, changed_result.returncode, changed_result.stdout + changed_result.stderr)
        self.assertNotEqual(first_hash, changed_evidence["git"]["worktreeFingerprint"])

        strict_result, strict_evidence = self.invoke(require_clean=True)
        self.assertEqual(2, strict_result.returncode)
        self.assertFalse(strict_evidence["sourceReady"])
        self.assertTrue(strict_evidence["sourcePolicy"]["requireCleanWorktree"])
        self.assertIn(
            "RequireCleanWorktree was specified but the worktree is dirty.",
            strict_evidence["sourcePolicy"]["failures"],
        )

    def test_untracked_build_source_is_reported_and_strict_switch_fails_closed(self) -> None:
        build_source = RESTORED_BUILD_SOURCES[0]
        subprocess.run(
            ["git", "-C", str(self.repository), "rm", "--cached", "--", build_source],
            check=True,
            capture_output=True,
            text=True,
            encoding="utf-8",
        )

        default_result, default_evidence = self.invoke()
        self.assertEqual(0, default_result.returncode, default_result.stdout + default_result.stderr)
        self.assertTrue(default_evidence["sourceReady"])
        self.assertIn(
            build_source,
            default_evidence["repositoryContracts"]["untrackedRestoredBuildSources"],
        )

        strict_result, strict_evidence = self.invoke(require_tracked_build_sources=True)
        self.assertEqual(2, strict_result.returncode)
        self.assertFalse(strict_evidence["sourceReady"])
        self.assertTrue(strict_evidence["sourcePolicy"]["requireTrackedBuildSources"])
        self.assertIn(
            "RequireTrackedBuildSources was specified but Build Owner Source files are untracked.",
            strict_evidence["sourcePolicy"]["failures"],
        )

    def test_head_must_match_origin_master_and_expected_head(self) -> None:
        wrong_expected, wrong_expected_evidence = self.invoke(expected_head="0" * 40)
        self.assertEqual(2, wrong_expected.returncode)
        self.assertFalse(wrong_expected_evidence["git"]["headMatchesExpected"])

        (self.repository / "tracked.txt").write_text("next head\n", encoding="utf-8")
        subprocess.run(["git", "-C", str(self.repository), "add", "tracked.txt"], check=True)
        subprocess.run(
            ["git", "-C", str(self.repository), "commit", "-qm", "next"], check=True
        )
        new_head = subprocess.run(
            ["git", "-C", str(self.repository), "rev-parse", "HEAD"],
            check=True,
            capture_output=True,
            text=True,
            encoding="utf-8",
        ).stdout.strip()
        mismatch_result, mismatch_evidence = self.invoke(expected_head=new_head)
        self.assertEqual(2, mismatch_result.returncode)
        self.assertFalse(mismatch_evidence["git"]["headMatchesOriginMaster"])
        self.assertIn(
            "HEAD must exactly match origin/master.",
            mismatch_evidence["sourcePolicy"]["failures"],
        )


if __name__ == "__main__":
    unittest.main()
