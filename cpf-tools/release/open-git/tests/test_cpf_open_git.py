from __future__ import annotations

import importlib.util
import json
import pytest
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


def _is_command(cmd: list[object], executable: str) -> bool:
    """Match a command executable without making the mock OS-suffix dependent."""
    actual = str(cmd[0]).replace("\\", "/").rsplit("/", 1)[-1].casefold()
    return actual.removesuffix(".exe") == executable.casefold()


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
        "cpf-education",
        "cpf-member/gradle.properties",
        "cpf-external/gradle.properties",
        "bin/cpf",
        "bin/cpf.cmd",
        "bin/cpf.ps1",
        "tools/verify-open-git-workspace.ps1",
    ):
        assert (staging / required).exists(), required
    # Backoffice is optional; absence is a normal NOT_SELECTED state unless explicitly included.
    assert not (staging / "cpf-backoffice").exists()
    assert not (staging / "cpf-backoffice-web").exists()
    for legacy in ("cpf-bootstrap", "cpf-domain-new", "cpf-domain-sync", "cpf-build", "cpf-test", "cpf-stop", "cpf-reset"):
        assert not (staging / legacy).exists(), legacy

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
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"



def test_open_git_backoffice_is_optional_and_explicit(tmp_path: Path):
    staging = tmp_path / "staging"
    command = [
        sys.executable,
        str(ROOT / "cpf-tools/release/public/prepare-cpf-public-workspace.py"),
        "--root", str(ROOT), "--staging", str(staging),
        "--policy", str(ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"),
        "--source-identity", "TEST-IDENTITY", "--include-backoffice",
    ]
    cp = subprocess.run(command, cwd=ROOT, text=True, encoding="utf-8", errors="replace", capture_output=True)
    assert cp.returncode == 0, cp.stdout + cp.stderr
    assert (staging / "cpf-backoffice/gradle.properties").is_file()
    assert (staging / "cpf-backoffice-web").is_dir()
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"

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
    assert not (final / paths["common-source"].relative_to(raw)).exists()
    assert not (final / paths["common-javadoc"].relative_to(raw)).exists()
    assert not (final / paths["core-source"].relative_to(raw)).exists()
    assert not (final / paths["batch-source"].relative_to(raw)).exists()
    assert (final / paths["common-binary"].relative_to(raw)).is_file()
    assert (final / paths["core-binary"].relative_to(raw)).is_file()
    assert result["keptSources"] == 0
    assert result["keptJavadocs"] == 0
    removed = {row["artifactId"] for row in result["removedSourceOrJavadoc"]}
    assert {"cpf-common", "cpf-core", "cpf-batch-api"}.issubset(removed)


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
    (root / "cpf-tools/runtime/cli/java").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/contracts").mkdir(parents=True)
    (root / "cpf-docs/governance").mkdir(parents=True)
    (root / "cpf-docs/work/current").mkdir(parents=True)
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / ".gitignore").write_text("# keep-existing\n", encoding="utf-8")
    (root / "cpf-tools/verification/tools/cpf-source-state.py").write_text(
        'GENERATED_PARTS = {\n    ".git",\n}\n', encoding="utf-8"
    )
    legacy_cli = root / "cpf-tools/runtime/cli/cpf.py"
    legacy_cli.write_text("# low-level generator engine; no Open Git surface\n", encoding="utf-8")
    java_cli = root / "cpf-tools/runtime/cli/java/CpfCli.java"
    java_cli.write_text(
        'final class CpfCli { static final String HELP = "cpf release open-git"; String ns = "release"; }\n',
        encoding="utf-8",
    )
    (root / "cpf-tools/runtime/cli/contracts/cpf-command-catalog.json").write_text(
        json.dumps({
            "internalNamespaces": [{"namespace": "release", "commands": ["open-git"]}]
        }),
        encoding="utf-8",
    )
    canonical = root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"
    canonical.write_text(
        "# Canonical\n\n### 21.3 Open Git Release Packaging\n\n- `cpf release open-git`\n\n## 22. EDU Canonical 35\n",
        encoding="utf-8",
    )
    (root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text(
        "# Current Open Git Work Package\n", encoding="utf-8"
    )

    first = MODULE.setup_integration(root)
    second = MODULE.setup_integration(root)
    assert first["status"] == "PASS"
    assert second["changed"] == []
    assert "# keep-existing" in (root / ".gitignore").read_text(encoding="utf-8")
    assert "/cpf-release/" in (root / ".gitignore").read_text(encoding="utf-8")
    assert '"cpf-release"' in (root / "cpf-tools/verification/tools/cpf-source-state.py").read_text(encoding="utf-8")
    text = canonical.read_text(encoding="utf-8")
    assert text.count("### 21.3 Open Git Release Packaging") == 1
    assert "sub.add_parser('open-git'" not in legacy_cli.read_text(encoding="utf-8")
    assert "cpf release open-git" in java_cli.read_text(encoding="utf-8")
    assert first["canonicalCli"] == "cpf release open-git"
    assert (root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").is_file()


def test_setup_integration_rejects_legacy_independent_open_git_surface(tmp_path: Path):
    root = tmp_path / "cpf"
    (root / "cpf-tools/verification/tools").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/java").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/contracts").mkdir(parents=True)
    (root / "cpf-docs/governance").mkdir(parents=True)
    (root / "cpf-docs/work/current").mkdir(parents=True)
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / ".gitignore").write_text("/cpf-release/\n", encoding="utf-8")
    (root / "cpf-tools/verification/tools/cpf-source-state.py").write_text(
        'GENERATED_PARTS = {\n    "cpf-release",\n}\n', encoding="utf-8"
    )
    (root / "cpf-tools/runtime/cli/java/CpfCli.java").write_text(
        'final class CpfCli { static final String HELP = "cpf release open-git"; String ns = "release"; }\n', encoding="utf-8"
    )
    (root / "cpf-tools/runtime/cli/contracts/cpf-command-catalog.json").write_text(
        json.dumps({"internalNamespaces":[{"namespace":"release","commands":["open-git"]}]}), encoding="utf-8"
    )
    (root / "cpf-tools/runtime/cli/cpf.py").write_text(
        "open_git=sub.add_parser('open-git')\n", encoding="utf-8"
    )
    (root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md").write_text(
        "### 21.3 Open Git Release Packaging\n`cpf release open-git`\n", encoding="utf-8"
    )
    (root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text("# current\n", encoding="utf-8")
    with pytest.raises(MODULE.OpenGitReleaseError, match="legacy independent"):
        MODULE.setup_integration(root)


def test_policies_are_default_deny_and_manual_push_only():
    surface = json.loads((ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json").read_text(encoding="utf-8"))
    artifact = json.loads((ROOT / "cpf-tools/release/open-git/open-git-artifact-policy.json").read_text(encoding="utf-8"))
    assert surface["defaultPolicy"] == "DENY"
    assert artifact["sourceJarPolicy"]["default"] == "DENY"
    assert artifact["javadocJarPolicy"]["default"] == "DENY"
    source = TOOL.read_text(encoding="utf-8")
    assert 'run([git, "add"' not in source
    assert 'run([git, "commit"' not in source
    assert 'run([git, "push"' not in source
    assert '"gitAddExecuted": False' in source
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
    for required in ("cpf", "cpf.cmd", "bin/cpf", "bin/cpf.cmd", "bin/cpf.ps1"):
        assert (staging / required).is_file(), required
    for legacy in ("cpf-bootstrap", "cpf-domain-new", "cpf-domain-sync", "cpf-build", "cpf-test", "cpf-stop", "cpf-reset"):
        assert not (staging / legacy).exists(), legacy
    for wrapper in ("bin/cpf", "bin/cpf.cmd", "bin/cpf.ps1"):
        text=(staging/wrapper).read_text(encoding="utf-8")
        assert "cpf-cli.jar" in text
        assert "gradlew" not in text.lower()
        assert "docker compose" not in text.lower()
    assert not (staging / "bin/CpfBootstrap.java").exists()
    assert not (staging / "bin/CpfGeneratorLauncher.java").exists()

def test_open_git_release_build_progress_and_failure_guidance_are_visible():
    source = TOOL.read_text(encoding="utf-8")
    assert 'BUILD_STAGE_TOTAL = 14' in source
    assert 'Release Root 안전 확인' in source
    assert 'Fresh Workspace Build/Test' in source
    assert 'Open Git Working Tree 검증' in source
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

    monkeypatch.setattr(MODULE, "private_git_context", lambda _root: {"head":"NO_GIT","branch":"NO_GIT","statusShort":[],"dirty":False,"releaseTracked":False})
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
    (root / "cpf-tools/runtime/cli/java").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/contracts").mkdir(parents=True)
    (root / "cpf-docs/governance").mkdir(parents=True)
    (root / "cpf-docs/work/current").mkdir(parents=True)
    (root / "settings.gradle").write_text("rootProject.name='x'\n", encoding="utf-8")
    (root / ".gitignore").write_text("# existing\n", encoding="utf-8")
    (root / "cpf-tools/verification/tools/cpf-source-state.py").write_text('GENERATED_PARTS = {\n    ".git",\n}\n', encoding="utf-8")
    (root / "cpf-tools/runtime/cli/java/CpfCli.java").write_text(
        'final class CpfCli { static final String HELP = "cpf release open-git"; String ns = "release"; }\n',
        encoding="utf-8",
    )
    (root / "cpf-tools/runtime/cli/contracts/cpf-command-catalog.json").write_text(
        json.dumps({"internalNamespaces":[{"namespace":"release","commands":["open-git"]}]}), encoding="utf-8"
    )
    # Legacy Python is retained only as an internal engine and must not own an
    # independent Open Git CLI surface.
    (root / "cpf-tools/runtime/cli/cpf.py").write_text("# internal engine only\n", encoding="utf-8")
    canonical = root / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md"
    canonical.write_text(
        "# Canonical\n\n### 21.3 Open Git Release Packaging\n"
        "cpf release open-git\nOpen Git 개발자 Workspace는 `cpf bootstrap`으로 시작한다.\n"
        "\n## 22. EDU Canonical 35\nKEEP-EDU\n", encoding="utf-8"
    )
    (root / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text("# current\n", encoding="utf-8")
    first = MODULE.setup_integration(root)
    assert first["status"] == "PASS"
    assert "/cpf-release/" in (root / ".gitignore").read_text(encoding="utf-8")
    result = MODULE.setup_integration(root)
    assert result["status"] == "PASS"
    # setup is compatibility-only: it must not rewrite canonical product steering.
    updated = canonical.read_text(encoding="utf-8")
    assert "Open Git 개발자 Workspace는 `cpf bootstrap`" in updated
    assert "## 22. EDU Canonical 35\nKEEP-EDU" in updated

def test_cross_platform_cli_is_single_java_implementation(tmp_path: Path, monkeypatch):
    staging = tmp_path / "staging"
    for source, target in (
        (ROOT / "cpf-tools/release/open-git/templates/bin/cpf", staging / "bin/cpf"),
        (ROOT / "cpf-tools/release/open-git/templates/bin/cpf.cmd", staging / "bin/cpf.cmd"),
        (ROOT / "cpf-tools/release/open-git/templates/bin/cpf.ps1", staging / "bin/cpf.ps1"),
    ):
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
    original_run=MODULE.run
    def fake_java25_probe(cmd, cwd, *, capture=False, env=None):
        if len(cmd) >= 2 and _is_command(cmd, "javac") and cmd[1] == "-version":
            return "javac 25.0.3"
        if _is_command(cmd, "javac") and "--release" in cmd:
            adjusted=list(cmd); adjusted[adjusted.index("--release")+1]="21"
            return original_run(adjusted, cwd, capture=capture, env=env)
        return original_run(cmd, cwd, capture=capture, env=env)
    monkeypatch.setattr(MODULE, "run", fake_java25_probe)
    result = MODULE.build_cross_platform_cli(ROOT, staging, "a" * 64, "9.9.9-test")
    assert result["status"] == "PASS"
    assert (staging / "bin/lib/cpf-cli.jar").is_file()
    verified = MODULE.verify_cross_platform_cli(staging, "a" * 64)
    assert verified["status"] == "PASS"
    cp = subprocess.run(
        [shutil.which("java") or "java", "-jar", str(staging / "bin/lib/cpf-cli.jar"), "version"],
        cwd=staging, text=True, encoding="utf-8", errors="replace", capture_output=True, check=False,
    )
    assert cp.returncode == 0, cp.stdout + cp.stderr
    assert "CPF_CLI_VERSION=9.9.9-test" in cp.stdout
    assert "SOURCE_IDENTITY=" + "a" * 64 in cp.stdout


def test_cli_wrappers_are_thin_and_no_powershell_or_bash_dependency_crosses_os():
    shell = (ROOT / "cpf-tools/release/open-git/templates/bin/cpf").read_text(encoding="utf-8")
    cmd = (ROOT / "cpf-tools/release/open-git/templates/bin/cpf.cmd").read_text(encoding="utf-8")
    ps1 = (ROOT / "cpf-tools/release/open-git/templates/bin/cpf.ps1").read_text(encoding="utf-8")
    assert "cpf-cli.jar" in shell and "java" in shell
    assert "cpf-cli.jar" in cmd and "java" in cmd.lower()
    assert "cpf-cli.jar" in ps1 and "java" in ps1.lower()
    assert "powershell" not in shell.lower()
    assert "bash" not in cmd.lower() and "bash" not in ps1.lower()
    for text in (shell, cmd, ps1):
        assert "docker compose" not in text.lower()
        assert "gradlew" not in text.lower()
        assert "domain new" not in text.lower()


def test_cross_platform_cli_build_requires_java25(monkeypatch, tmp_path: Path):
    staging = tmp_path / "staging"; (staging / "bin").mkdir(parents=True)
    calls = []
    def fake_old_javac(cmd, cwd, *, capture=False, env=None):
        calls.append([str(value) for value in cmd])
        if len(cmd) >= 2 and _is_command(cmd, "javac") and cmd[1] == "-version":
            return "javac 21.0.11"
        raise AssertionError(f"unexpected command after Java version rejection: {cmd}")
    monkeypatch.setattr(MODULE, "run", fake_old_javac)
    with pytest.raises(MODULE.OpenGitReleaseError, match="Java 25 javac is required"):
        MODULE.build_cross_platform_cli(ROOT, staging, "b" * 64, "9.9.9-test")
    assert len(calls) == 1 and calls[0][1] == "-version"


def test_cross_platform_cli_source_owns_public_commands_and_java25_gate():
    source = (ROOT / "cpf-tools/runtime/cli/java/CpfCli.java").read_text(encoding="utf-8")
    for command in ("bootstrap", "domain-new", "domain-sync", "build", "test", "run", "stop", "reset", "status", "version"):
        assert f'"{command}"' in source
    assert "requireJava25Then" in source
    assert "CPF-CLI-JAVA-VERSION" in source
    assert "domainNew" in source and "--name" in source and "--system-code" in source
    assert '"--confirm".equals(arg) ? "--confirm-local-reset"' in source
    assert "StandardCharsets.UTF_8" in source

def test_cross_platform_cli_public_surface_has_no_java_implementation_sources():
    for policy_name in (
        "cpf-tools/release/open-git/open-git-surface-policy.json",
        "cpf-tools/release/public/cpf-public-surface-policy.json",
    ):
        policy = json.loads((ROOT / policy_name).read_text(encoding="utf-8"))
        rules = policy.get("templateRules", []) + policy.get("sourceRules", [])
        targets = {str(row.get("target", "")) for row in rules}
        assert "bin/CpfCli.java" not in targets
        assert "bin/CpfBootstrap.java" not in targets
        assert "bin/CpfGeneratorLauncher.java" not in targets


def test_open_git_git_boundary_is_read_only_until_user_review():
    source = TOOL.read_text(encoding="utf-8")
    assert 'run([git, "add"' not in source
    assert 'run([git, "commit"' not in source
    assert 'run([git, "push"' not in source
    assert 'git, "status", "--short"' in source
    assert 'git, "diff", "--check"' in source
    assert '"result": "VERIFIED"' in source
    assert '"userReviewRequired": True' in source

    final_target = (ROOT / "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md").read_text(encoding="utf-8")
    fresh_requirement = (ROOT / "cpf-docs/work/current/CPF_OPEN_GIT_FRESH_RELEASE_REQUIREMENT.md").read_text(encoding="utf-8")
    work_package = (ROOT / "cpf-docs/work/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").read_text(encoding="utf-8")
    command_catalog = json.loads((ROOT / "cpf-tools/runtime/cli/contracts/cpf-command-catalog.json").read_text(encoding="utf-8"))

    for text in (final_target, fresh_requirement, work_package):
        assert "READY_TO_COMMIT" not in text
        assert "READY_FOR_USER_COMMIT" not in text
    assert "Private master" in final_target
    assert "VERIFIED" in final_target
    boundary = command_catalog["releaseGitBoundary"]
    assert boundary["privateRepositoryGit"] == "READ_ONLY_PROVENANCE_STATUS"
    assert boundary["openGitWorkingRepository"] == "FRESH_CLONE_READ_ONLY_STATUS_DIFF_UNTIL_USER_APPROVAL"
    assert boundary["toolMaxState"] == "VERIFIED"
    assert boundary["automaticGitWrite"] is False
    assert boundary["privateMasterIncludesCpfRelease"] is False


def test_private_git_context_allows_dirty_working_tree_without_cleanup(monkeypatch, tmp_path: Path):
    root = tmp_path / "cpf"
    root.mkdir()
    (root / ".git").mkdir()
    commands = []

    def fake_run(cmd, cwd, *, capture=False, env=None):
        commands.append(tuple(cmd))
        if cmd[1:3] == ["rev-parse", "--is-inside-work-tree"]:
            return "true"
        if cmd[1:3] == ["ls-files", MODULE.RELEASE_DIR_NAME]:
            return ""
        if cmd[1:3] == ["status", "--short"]:
            return " M cpf-tools/example.txt"
        if cmd[1:3] == ["rev-parse", "--abbrev-ref"]:
            return "master"
        if cmd[1:3] == ["rev-parse", "HEAD"]:
            return "abc123"
        raise AssertionError(cmd)

    monkeypatch.setattr(MODULE.shutil, "which", lambda name: "git" if name == "git" else None)
    monkeypatch.setattr(MODULE, "run", fake_run)
    result = MODULE.private_git_context(root)
    assert result["dirty"] is True
    assert result["statusShort"] == [" M cpf-tools/example.txt"]
    assert result["head"] == "abc123"
    assert result["branch"] == "master"
    forbidden = {"add", "commit", "push", "reset", "restore", "stash", "clean"}
    assert not any(any(part in forbidden for part in cmd) for cmd in commands)
