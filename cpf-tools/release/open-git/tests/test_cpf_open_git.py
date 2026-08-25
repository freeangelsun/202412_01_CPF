from __future__ import annotations

import importlib.util
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path
from types import SimpleNamespace


ROOT = Path(__file__).resolve().parents[4]
TOOL = ROOT / "cpf-tools/release/open-git/cpf_open_git.py"
SPEC = importlib.util.spec_from_file_location("cpf_open_git_tested", TOOL)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def test_open_git_surface_projection_contains_only_developer_source(tmp_path: Path):
    staging = tmp_path / "staging"
    command = [
        sys.executable,
        str(ROOT / "cpf-tools/release/public/prepare-cpf-public-workspace.py"),
        "--root", str(ROOT),
        "--staging", str(staging),
        "--policy", str(ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"),
        "--source-identity", "TEST-IDENTITY",
    ]
    cp = subprocess.run(command, cwd=ROOT, text=True, encoding="utf-8", errors="replace", capture_output=True)
    assert cp.returncode == 0, cp.stdout + cp.stderr

    for required in (
        "cpf-member",
        "cpf-external",
        "cpf-backoffice",
        "cpf-backoffice-web",
        "cpf-education",
        "cpf-member/gradle.properties",
        "cpf-external/gradle.properties",
        "cpf-backoffice/gradle.properties",
        "bin/cpf-bootstrap.ps1",
        "bin/cpf-domain-new.ps1",
        "tools/verify-open-git-workspace.ps1",
    ):
        assert (staging / required).exists(), required

    assert not (staging / "domains").exists()
    assert not list(staging.rglob("cpf-domain.yaml"))
    assert not list(staging.rglob("cpf-generator.lock.json"))

    for forbidden in (
        "cpf-core",
        "cpf-common",
        "cpf-admin",
        "cpf-biz-admin",
        "cpf-batch",
        "cpf-gateway",
        "cpf-starters",
        "cpf-tools",
        "cpf-docs",
    ):
        assert not (staging / forbidden).exists(), forbidden

    unexpected_archives = [
        p.relative_to(staging).as_posix()
        for p in staging.rglob("*")
        if p.is_file()
        and p.suffix.lower() in {".jar", ".war"}
        and p.relative_to(staging).as_posix() != "gradle/wrapper/gradle-wrapper.jar"
    ]
    assert unexpected_archives == []
    assert "project(" not in (staging / "cpf-education/build.gradle").read_text(encoding="utf-8")
    assert MODULE.verify_open_git_tree(staging)["status"] == "PASS"


def test_binary_source_and_javadoc_policy_is_default_deny(tmp_path: Path):
    raw = tmp_path / "raw"
    final = tmp_path / "final"
    version = "1.2.3"
    paths = {
        "common-source": raw / "com/cpf/common/cpf-common" / version / f"cpf-common-{version}-sources.jar",
        "common-javadoc": raw / "com/cpf/common/cpf-common" / version / f"cpf-common-{version}-javadoc.jar",
        "core-source": raw / "com/cpf/core/cpf-core" / version / f"cpf-core-{version}-sources.jar",
        "batch-source": raw / "com/cpf/batch/cpf-batch-api" / version / f"cpf-batch-api-{version}-sources.jar",
        "common-binary": raw / "com/cpf/common/cpf-common" / version / f"cpf-common-{version}.jar",
        "core-binary": raw / "com/cpf/core/cpf-core" / version / f"cpf-core-{version}.jar",
    }
    for path in paths.values():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(b"test")

    result = MODULE.sanitize_binary_repository(ROOT, raw, final)
    assert (final / paths["common-source"].relative_to(raw)).is_file()
    assert (final / paths["common-javadoc"].relative_to(raw)).is_file()
    assert not (final / paths["core-source"].relative_to(raw)).exists()
    assert not (final / paths["batch-source"].relative_to(raw)).exists()
    assert (final / paths["common-binary"].relative_to(raw)).is_file()
    assert (final / paths["core-binary"].relative_to(raw)).is_file()
    assert result["keptSources"] == 1
    assert result["keptJavadocs"] == 1
    removed = {row["artifactId"] for row in result["removedSourceOrJavadoc"]}
    assert {"cpf-core", "cpf-batch-api"}.issubset(removed)


def test_release_rebuild_deletes_only_exact_generated_root(tmp_path: Path):
    root = tmp_path / "cpf"
    root.mkdir()
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / "cpf-tools").mkdir()
    (root / "cpf-docs").mkdir()
    (root / ".gitignore").write_text("/cpf-release/\n", encoding="utf-8")
    release = root / "cpf-release"
    (release / "open-git").mkdir(parents=True)
    (release / "open-git/stale.txt").write_text("stale", encoding="utf-8")
    outside = root / "keep.txt"
    outside.write_text("keep", encoding="utf-8")

    cleaned = MODULE.clean_release_root(root)
    assert cleaned == release
    assert cleaned.is_dir()
    assert list(cleaned.iterdir()) == []
    assert outside.read_text(encoding="utf-8") == "keep"


def test_release_cleanup_refuses_symlink(tmp_path: Path, monkeypatch):
    root = tmp_path / "cpf"
    root.mkdir()
    (root / "settings.gradle").write_text("x", encoding="utf-8")
    (root / "cpf-tools").mkdir(); (root / "cpf-docs").mkdir()
    (root / ".gitignore").write_text("/cpf-release/\n", encoding="utf-8")
    elsewhere = tmp_path / "elsewhere"; elsewhere.mkdir()
    release = root / "cpf-release"
    try:
        release.symlink_to(elsewhere, target_is_directory=True)
    except OSError as exc:
        # Restricted Windows sessions need SeCreateSymbolicLinkPrivilege. Exercise
        # the exact production rejection branch without skipping it; supported
        # hosts continue to use a real filesystem symlink above.
        if sys.platform != "win32" or getattr(exc, "winerror", None) != 1314:
            raise
        original_is_symlink = Path.is_symlink
        monkeypatch.setattr(Path, "is_symlink", lambda self: self == release or original_is_symlink(self))
    try:
        MODULE.verify_release_root_safety(root)
        assert False, "symlink release root must be rejected"
    except MODULE.OpenGitReleaseError as exc:
        assert "symlink" in str(exc)


def test_setup_integration_is_narrow_and_idempotent(tmp_path: Path):
    root = tmp_path / "cpf"
    (root / "cpf-tools/verification/tools").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli").mkdir(parents=True)
    (root / "cpf-docs/governance").mkdir(parents=True)
    (root / "cpf-docs/work/current").mkdir(parents=True)
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / ".gitignore").write_text("# keep-existing\n", encoding="utf-8")
    (root / "cpf-tools/verification/tools/cpf-source-state.py").write_text(
        'GENERATED_PARTS = {\n    ".git",\n}\n', encoding="utf-8"
    )
    cli = root / "cpf-tools/runtime/cli/cpf.py"
    cli.write_text(
        "import argparse, json, os, shutil, sys, tempfile, uuid\n"
        "def main():\n"
        "    p=argparse.ArgumentParser()\n"
        "    sub=p.add_subparsers(dest='group',required=True)\n"
        "    verify=sub.add_parser('verify')\n"
        "    vsub=verify.add_subparsers(dest='command',required=True)\n"
        "    vsub.add_parser('all')\n"
        "    ns=p.parse_args(); root=repo_root(ns.root)\n",
        encoding="utf-8",
    )
    canonical = root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"
    canonical.write_text("# Canonical\n\n## 22. EDU Canonical 35\n", encoding="utf-8")

    first = MODULE.setup_integration(root)
    second = MODULE.setup_integration(root)
    assert first["status"] == "PASS"
    assert second["changed"] == []
    assert "# keep-existing" in (root / ".gitignore").read_text(encoding="utf-8")
    assert "/cpf-release/" in (root / ".gitignore").read_text(encoding="utf-8")
    assert '"cpf-release"' in (root / "cpf-tools/verification/tools/cpf-source-state.py").read_text(encoding="utf-8")
    text = canonical.read_text(encoding="utf-8")
    assert text.count("### 21.3 Open Git Release Packaging") == 1
    cli_text = cli.read_text(encoding="utf-8")
    assert "sub.add_parser('open-git'" in cli_text
    assert "choices=['build','check','status']" in cli_text
    assert "subprocess.run([sys.executable,str(tool),ns.command" in cli_text
    assert (root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").is_file()


def test_policies_are_default_deny_and_manual_push_only():
    surface = json.loads((ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json").read_text(encoding="utf-8"))
    artifact = json.loads((ROOT / "cpf-tools/release/open-git/open-git-artifact-policy.json").read_text(encoding="utf-8"))
    assert surface["defaultPolicy"] == "DENY"
    assert artifact["sourceJarPolicy"]["default"] == "DENY"
    assert artifact["javadocJarPolicy"]["default"] == "DENY"
    source = TOOL.read_text(encoding="utf-8")
    assert "git, \"commit\"" not in source
    assert "git, \"push\"" not in source
    assert '"commitExecuted": False' in source
    assert '"pushExecuted": False' in source


def test_current_catalog_contract_matches_post_filtered_open_git_release():
    findings = MODULE.artifact_catalog_contract_findings(ROOT)
    assert findings == []


def test_owner_prefix_matching_requires_a_path_segment_boundary():
    config = {"allowOwnerPathPrefixes": ["cpf-common"], "denyOwnerPathPrefixes": []}
    assert MODULE._owner_allowed("cpf-common", config)
    assert MODULE._owner_allowed("cpf-common/contracts", config)
    assert not MODULE._owner_allowed("cpf-common-private", config)


def test_final_binary_verifier_rejects_unclassified_files_and_poms(tmp_path: Path):
    repo = tmp_path / "repo"
    unknown = repo / "com/private/leak/1.0.0"
    unknown.mkdir(parents=True)
    (unknown / "leak-1.0.0.jar").write_bytes(b"private")
    (unknown / "leak-1.0.0.pom").write_text(
        "<project><modelVersion>4.0.0</modelVersion><groupId>com.private</groupId>"
        "<artifactId>leak</artifactId><version>1.0.0</version></project>",
        encoding="utf-8",
    )
    try:
        MODULE.verify_binary_repository(ROOT, repo, "1.0.0")
        assert False, "unclassified repository content must fail closed"
    except MODULE.OpenGitReleaseError as exc:
        message = str(exc)
        assert "unclassified repository file" in message
        assert "unclassified binary artifact" in message
        assert "unclassified POM coordinate" in message


def test_pom_dependency_management_is_included_in_final_repository_closure(tmp_path: Path):
    pom = tmp_path / "bom.pom"
    pom.write_text(
        "<project><modelVersion>4.0.0</modelVersion><groupId>com.cpf</groupId>"
        "<artifactId>sample-bom</artifactId><version>1.0.0</version>"
        "<dependencyManagement><dependencies><dependency><groupId>com.cpf.runtime</groupId>"
        "<artifactId>missing-runtime</artifactId><version>1.0.0</version>"
        "</dependency></dependencies></dependencyManagement></project>",
        encoding="utf-8",
    )
    _, _, _, dependencies = MODULE._pom_coordinate(pom)
    assert dependencies == [("com.cpf.runtime", "missing-runtime", "1.0.0")]


def test_authenticated_remote_is_redacted_from_console_log_and_error(tmp_path: Path, monkeypatch, capsys):
    credential = "do-not-disclose-token"
    remote = f"https://release-user:{credential}@example.invalid/cpf-team/cpf-framework.git"
    received = []

    def fake_run(cmd, **kwargs):
        received.append(cmd)
        return SimpleNamespace(returncode=1, stdout=f"clone failed for {remote}\n", stderr="")

    monkeypatch.setattr(MODULE.subprocess, "run", fake_run)
    MODULE.ACTIVE_LOG_FILE = tmp_path / "release.log"
    try:
        MODULE.run(["git", "clone", remote, "target"], tmp_path, capture=True)
        assert False, "synthetic clone failure expected"
    except MODULE.OpenGitReleaseError as exc:
        assert credential not in str(exc)
    output = capsys.readouterr()
    log = MODULE.ACTIVE_LOG_FILE.read_text(encoding="utf-8")
    assert credential not in output.out + output.err + log
    assert "https://***@example.invalid/cpf-team/cpf-framework.git" in output.out + output.err + log
    assert received[0][2] == remote


def test_open_git_short_cli_and_compatibility_wrappers_are_canonical(tmp_path: Path):
    staging = tmp_path / "staging"
    command = [
        sys.executable,
        str(ROOT / "cpf-tools/release/public/prepare-cpf-public-workspace.py"),
        "--root", str(ROOT),
        "--staging", str(staging),
        "--policy", str(ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"),
        "--source-identity", "TEST-IDENTITY",
    ]
    cp = subprocess.run(command, cwd=ROOT, text=True, encoding="utf-8", errors="replace", capture_output=True)
    assert cp.returncode == 0, cp.stdout + cp.stderr

    for required in (
        "cpf",
        "cpf.cmd",
        "bin/cpf.ps1",
        "bin/cpf.sh",
        "bin/cpf-bootstrap.ps1",
        "bin/cpf-bootstrap.sh",
        "bin/cpf-build.ps1",
        "bin/cpf-build.sh",
        "bin/cpf-test.ps1",
        "bin/cpf-test.sh",
        "bin/cpf-domain-new.ps1",
        "bin/cpf-domain-new.sh",
        "bin/cpf-domain-sync.ps1",
        "bin/cpf-domain-sync.sh",
    ):
        assert (staging / required).is_file(), required

    shell = (staging / "bin/cpf.sh").read_text(encoding="utf-8")
    assert "CPF Command Result" in shell
    assert "CPF COMMAND FAILED" in shell
    assert "CPF LOCAL DEVELOPMENT READY" in shell
    assert "cpfBuild" in shell and "cpfTest" in shell and "cpfVerify" in shell
    assert "CpfBootstrap.java\" build" not in shell
    assert "--workspace" not in shell
    assert "--timeout-seconds" not in shell
    assert "--start-runtime" not in shell

    if sys.platform == "win32":
        shell_executable = shutil.which("pwsh") or shutil.which("powershell")
        assert shell_executable, "PowerShell is required for the native Windows CLI regression"
        help_command = [shell_executable, "-NoProfile", "-File", str(staging / "bin/cpf.ps1"), "help"]
    else:
        help_command = ["bash", str(staging / "bin/cpf.sh"), "help"]
    help_result = subprocess.run(help_command, cwd=staging, text=True, encoding="utf-8", errors="replace", capture_output=True)
    assert help_result.returncode == 0, help_result.stdout + help_result.stderr
    assert "domain new <name> <SYSTEM_CODE>" in help_result.stdout
    assert "reset --confirm" in help_result.stdout


def test_open_git_release_build_progress_and_failure_guidance_are_visible():
    source = TOOL.read_text(encoding="utf-8")
    assert 'BUILD_STAGE_TOTAL = 14' in source
    assert 'Release Root 안전 확인' in source
    assert 'Fresh Workspace Build/Test' in source
    assert 'Open Git Staged Diff 검증' in source
    assert 'CPF OPEN GIT RELEASE FAILED' in source
    assert 'ExitCode' in source
    assert 'Next      :' in source
    assert 'Commit    : NOT_EXECUTED' in source
    assert 'Push      : NOT_EXECUTED' in source


def test_canonical_publication_entrypoint_and_catalog_owned_documentation_variants_exist():
    convention = (ROOT / "cpf-tools/build/cpf-root-conventions.gradle").read_text(encoding="utf-8")
    assert "tasks.register('publishCpfVerifiedLocalPlatformArtifacts')" in convention
    assert "tasks.register('cpfPublishPublicToArtifactStaging')" in convention
    assert "cpfPublicPublicationOwners.contains" in convention
    assert "java.withSourcesJar()" in convention
    assert "java.withJavadocJar()" in convention
    backend = (ROOT / "cpf-tools/release/public/publish-cpf-public-repository.py").read_text(encoding="utf-8")
    assert "-PcpfPublicBinaryRepository=" in backend
    assert "-PcpfArtifactStagingRepository=" in backend

    catalog = json.loads((ROOT / "cpf-tools/release/cpf-final-artifact-catalog.json").read_text(encoding="utf-8"))
    by_id = {row["artifactId"]: row for row in catalog["artifacts"]}
    attachment = by_id["cpf-starter-file-attachment"]
    assert attachment["publicationClass"] == "PUBLIC_COMPILE_TIME_JAVA"
    assert attachment["publicGroupId"] == "com.cpf.starter"
    assert attachment["publicProjectPath"] == ":starters:file:attachment"
    assert by_id["cpf-gradle-plugin"]["publicGroupId"] == "com.cpf.gradle"


def test_required_javadoc_sources_do_not_expose_annotations_as_unknown_block_tags():
    catalog = json.loads((ROOT / "cpf-tools/release/cpf-final-artifact-catalog.json").read_text(encoding="utf-8"))
    offenders = []
    for row in catalog["artifacts"]:
        if row.get("publishJavadoc") is not True:
            continue
        for source in (ROOT / row["ownerPath"]).rglob("*.java"):
            for line_no, line in enumerate(source.read_text(encoding="utf-8").splitlines(), 1):
                if re.search(r"/\*\*\s+@Cpf[A-Za-z0-9_]*", line):
                    offenders.append(f"{source.relative_to(ROOT).as_posix()}:{line_no}")
    assert offenders == []


def test_open_git_rebuild_cleans_previous_release_before_contract_validation(tmp_path: Path, monkeypatch):
    root = tmp_path / "cpf"
    root.mkdir()
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / "cpf-tools").mkdir()
    (root / "cpf-docs").mkdir()
    (root / ".gitignore").write_text("/cpf-release/\n", encoding="utf-8")
    stale = root / "cpf-release/open-git/stale.txt"
    stale.parent.mkdir(parents=True)
    stale.write_text("old", encoding="utf-8")

    monkeypatch.setattr(MODULE, "require_clean_private_git", lambda _root: "NO_GIT")
    monkeypatch.setattr(MODULE, "canonical_source_state", lambda _root: {"contentSha256": "x", "fileCount": 1})
    monkeypatch.setattr(MODULE, "platform_version", lambda _root: "1.0.0")
    monkeypatch.setattr(MODULE, "canonical_remote", lambda _root, _remote: "https://example.invalid/cpf-team/cpf-framework.git")
    monkeypatch.setattr(MODULE, "verify_artifact_catalog_contract", lambda _root: (_ for _ in ()).throw(MODULE.OpenGitReleaseError("contract test failure")))

    try:
        MODULE.build_release(root, None, None)
        assert False, "contract failure expected"
    except MODULE.OpenGitReleaseError as exc:
        assert "contract test failure" in str(exc)
    assert not stale.exists(), "previous release must be removed before the new build proceeds"
    assert (root / "cpf-release/logs/open-git-release.log").is_file()


def test_setup_currentizes_only_owned_canonical_section(tmp_path: Path):
    root = tmp_path / "cpf"
    (root / "cpf-tools/verification/tools").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli").mkdir(parents=True)
    (root / "cpf-docs/governance").mkdir(parents=True)
    (root / "cpf-docs/work/current").mkdir(parents=True)
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / ".gitignore").write_text("# existing\n", encoding="utf-8")
    (root / "cpf-tools/verification/tools/cpf-source-state.py").write_text('GENERATED_PARTS = {\n    ".git",\n}\n', encoding="utf-8")
    (root / "cpf-tools/runtime/cli/cpf.py").write_text(
        "import argparse, json, os, shutil, sys, tempfile, uuid\n"
        "def main():\n"
        "    p=argparse.ArgumentParser()\n"
        "    sub=p.add_subparsers(dest='group',required=True)\n"
        "    verify=sub.add_parser('verify')\n"
        "    vsub=verify.add_subparsers(dest='command',required=True)\n"
        "    vsub.add_parser('all')\n"
        "    ns=p.parse_args(); root=repo_root(ns.root)\n",
        encoding="utf-8",
    )
    canonical = root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"
    canonical.write_text("# Canonical\n\n## 22. EDU Canonical 35\nKEEP-EDU\n", encoding="utf-8")
    MODULE.setup_integration(root)
    text = canonical.read_text(encoding="utf-8")
    text = text.replace("Open Git 개발자 Workspace는", "OLD Open Git 개발자 Workspace는", 1)
    canonical.write_text(text, encoding="utf-8")
    result = MODULE.setup_integration(root)
    updated = canonical.read_text(encoding="utf-8")
    assert "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md" in result["changed"]
    assert "OLD Open Git 개발자 Workspace는" not in updated
    assert "Open Git 개발자 Workspace는 `cpf bootstrap`" in updated
    assert "## 22. EDU Canonical 35\nKEEP-EDU" in updated
