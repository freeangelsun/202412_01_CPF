from __future__ import annotations

import importlib.util
import json
import pytest
import re
import shutil
import json
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



def _materialize_public_distribution(staging: Path) -> None:
    """Public Product Distribution 필수 구성을 staging 에 만들어 준다.

    verify_open_git_tree 는 bundled binary repository / cpf-docs / README / launcher 까지
    요구한다. 이 fixture 는 Stage 10 이 실제로 만드는 것과 같은 형태만 최소로 재현한다.
    """
    import hashlib

    repository = staging / "binary-repository"
    artifact_dir = repository / "com/cpf/core/cpf-core/1.0.0"
    artifact_dir.mkdir(parents=True, exist_ok=True)
    jar = artifact_dir / "cpf-core-1.0.0.jar"
    pom = artifact_dir / "cpf-core-1.0.0.pom"
    jar.write_bytes(b"jar")
    pom.write_text("<project/>", encoding="utf-8")
    # ADM 은 Source 를 공개하지 않고 Binary 로만 배포되는 Runtime 이다. 공개 Consumer 가 ADM 을
    # 기동할 수 있어야 Release 이므로, fixture 도 실제 배포와 같이 실행물을 공급한다.
    # bootstrap 은 Platform BOM 좌표로 Binary Repository 를 확인한다. 실제 배포와 같이 싣는다.
    bom_dir = repository / "com/cpf/cpf-platform-bom/1.0.0"
    bom_dir.mkdir(parents=True, exist_ok=True)
    bom_pom = bom_dir / "cpf-platform-bom-1.0.0.pom"
    bom_pom.write_text("<project/>", encoding="utf-8")
    admin_dir = repository / "com/cpf/runtime/cpf-admin/1.0.0"
    admin_dir.mkdir(parents=True, exist_ok=True)
    admin_jar = admin_dir / "cpf-admin-1.0.0.jar"
    admin_jar.write_bytes(b"jar")
    # 공개 checkout 은 cpf-tools/ 를 포함하지 않으므로 Runtime Target Catalog 는 config/ 로 투영된다.
    # 공개 workspace 기본 버전은 Release 가 실제 발행 버전으로 맞춘다. fixture 도 같은 경로를 쓴다.
    MODULE.currentize_public_workspace_version(staging, "1.0.0")
    published_catalog = staging / "config/cpf-runtime-target-catalog.json"
    published_catalog.parent.mkdir(parents=True, exist_ok=True)
    published_catalog.write_text(
        (ROOT / "cpf-tools/runtime/cpf-runtime-target-catalog.json").read_text(encoding="utf-8"),
        encoding="utf-8")
    artifacts = []
    for path in (jar, pom, admin_jar, bom_pom):
        if path is admin_jar:
            group, artifact_id = "com.cpf.runtime", "cpf-admin"
        elif path is bom_pom:
            group, artifact_id = "com.cpf", "cpf-platform-bom"
        else:
            group, artifact_id = "com.cpf.core", "cpf-core"
        artifacts.append({
            "group": group, "artifactId": artifact_id, "module": artifact_id,
            "version": "1.0.0", "classifier": None, "type": path.suffix.lstrip("."),
            "relativePath": path.relative_to(repository).as_posix(),
            "fileSize": path.stat().st_size,
            "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            "publicationType": "PUBLIC_RUNTIME" if path is admin_jar else "PUBLIC_COMPILE_TIME_JAVA",
            "classification": "PUBLIC",
            "sourceIdentitySha256": "0" * 64,
        })
    manifest = {
        "contract": "CPF_PUBLIC_PACKAGE_MANIFEST", "schemaVersion": 1,
        "publicVersion": "1.0.0", "developmentVersion": "1.0.0-SNAPSHOT",
        "sourceIdentitySha256": "0" * 64, "artifactCount": len(artifacts),
        "artifacts": artifacts,
    }
    (repository / "package-manifest.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    guide = staging / "cpf-docs/guides"
    guide.mkdir(parents=True, exist_ok=True)
    guide_name = "02_developer_guide.pdf"
    (guide / guide_name).write_bytes(b"pdf")
    # README 는 stub 을 쓰지 않는다. 실제 배포 README 계약(Consumer 실행 안내 포함)을 그대로
    # 검증해야 "Runtime 은 실렸는데 실행 방법이 없는" 상태를 잡을 수 있다.
    template = (ROOT / "cpf-tools/release/open-git/templates/README.md").read_text(encoding="utf-8")
    guide_reference = "\n`cpf-docs/guides/" + guide_name + "`\n"
    (staging / "README.md").write_text(template + guide_reference, encoding="utf-8")

    bin_dir = staging / "bin"
    bin_dir.mkdir(parents=True, exist_ok=True)
    # launcher 도 stub 을 쓰지 않는다. 파일 존재가 아니라 실제 배포 launcher 계약
    # (-Target 수용 -> --target 전달)을 검증해야 문서대로 동작하지 않는 배포를 잡는다.
    launcher_templates = ROOT / "cpf-tools/release/open-git/templates/bin"
    for name in ("start", "stop", "status", "restart", "health", "log", "help"):
        for suffix in (".ps1", ".sh"):
            source = launcher_templates / ("cpf-" + name + suffix)
            (bin_dir / ("cpf-" + name + suffix)).write_text(
                source.read_text(encoding="utf-8") if source.is_file() else "",
                encoding="utf-8")


def test_open_git_surface_projection_contains_only_developer_source(tmp_path: Path):
    staging = tmp_path / "staging"
    command = [
        sys.executable,
        str(ROOT / "cpf-tools/release/public/prepare-cpf-public-workspace.py"),
        "--root", str(ROOT),
        "--staging", str(staging),
        "--policy", str(ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json"),
        "--source-identity", "TEST-IDENTITY",
        # 공개 배포본은 ADM 과 함께 MBW/Backoffice Web 도 기동 가능해야 하므로 Release 는 항상
        # backoffice 를 포함한다. Leakage 검사도 그 구성 그대로 수행해야 의미가 있다.
        "--include-backoffice",
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
    # Backoffice(MBW Domain)와 Backoffice Web(그 Channel Front)은 공개 Consumer 실행 표면이다.
    # 공개 배포본은 ADM 뿐 아니라 이 둘도 실제로 기동할 수 있어야 한다.
    assert (staging / "cpf-backoffice").is_dir()
    assert (staging / "cpf-backoffice-web").is_dir()
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
    ):
        assert not (staging / forbidden).exists(), forbidden

    # cpf-docs 는 Public Documentation Allowlist 로 관리한다. root 자체는 허용하되
    # governance/work 같은 내부 관리자료가 한 건이라도 섞이면 Leakage 다.
    docs_root = staging / "cpf-docs"
    if docs_root.is_dir():
        leaked_docs = [
            p.relative_to(staging).as_posix()
            for p in docs_root.rglob("*")
            if p.is_file() and any(
                p.relative_to(staging).as_posix().startswith(prefix)
                for prefix in ("cpf-docs/governance/", "cpf-docs/work/", "cpf-docs/development/",
                               "cpf-docs/environment/", "cpf-docs/brand/", "cpf-docs/architecture/")
            )
        ]
        assert not leaked_docs, leaked_docs

    unexpected_archives = [
        p.relative_to(staging).as_posix()
        for p in staging.rglob("*")
        if p.is_file()
        and p.suffix.lower() in {".jar", ".war"}
        and p.relative_to(staging).as_posix() != "gradle/wrapper/gradle-wrapper.jar"
    ]
    assert unexpected_archives == []
    assert "project(" not in (staging / "cpf-education/build.gradle").read_text(encoding="utf-8")
    _materialize_public_distribution(staging)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"



def test_open_git_backoffice_is_part_of_the_public_consumer_runtime_surface(tmp_path: Path):
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
    # Public Workspace cpfTestAll은 포함 Build의 루트 :test를 호출한다. Backoffice는
    # multi-project Domain이므로 root test aggregate를 투영하지 않으면 Fresh Consumer
    # bootstrap이 ':cpf-backoffice:test' 경로 해석에서 실패한다.
    backoffice_build = (staging / "cpf-backoffice/build.gradle").read_text(encoding="utf-8")
    assert "tasks.register('test')" in backoffice_build
    assert 'dependsOn subprojects.collect { "${it.path}:test" }' in backoffice_build
    assert (staging / "cpf-backoffice-web").is_dir()
    _materialize_public_distribution(staging)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"

def _public_consumer_staging(tmp_path: Path) -> Path:
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
    _materialize_public_distribution(staging)
    return staging


def test_public_consumer_runtime_surface_fails_without_published_catalog(tmp_path: Path):
    """공개 checkout 에 Runtime Target Catalog 가 없으면 사용자는 어떤 Target 도 기동할 수 없다."""
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    (staging / "config/cpf-runtime-target-catalog.json").unlink()
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    assert "Runtime Target Catalog" in str(failure.value)


def test_public_consumer_runtime_surface_fails_without_adm_runnable(tmp_path: Path):
    """ADM 은 Binary 로만 배포된다. 실행물이 없으면 콘솔을 띄울 수 없으므로 Release 가 아니다."""
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    admin_jar = staging / "binary-repository/com/cpf/runtime/cpf-admin/1.0.0/cpf-admin-1.0.0.jar"
    admin_jar.unlink()
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    assert "cpf-admin" in str(failure.value) or "package manifest" in str(failure.value)


def test_public_consumer_runtime_surface_fails_without_backoffice_source(tmp_path: Path):
    """Backoffice Web 은 ADM 이 아니라 MBW Channel Front 이므로 MBW 와 함께 기동 가능해야 한다."""
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    shutil.rmtree(staging / "cpf-backoffice-web")
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    assert "backoffice-web" in str(failure.value)


def test_binary_provisioned_runtime_must_not_be_buildable_in_public_tree(tmp_path: Path):
    """ADM/Gateway 는 공개 배포본에서 기동만 되어야 하고 빌드 Task 가 나가면 안 된다."""
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    # Binary Runtime 의 Source Root 가 공개 트리에 들어오면 그 순간 빌드 대상이 된다.
    (staging / "cpf-admin").mkdir(parents=True, exist_ok=True)
    (staging / "cpf-admin/build.gradle").write_text("plugins { id 'java' }", encoding="utf-8")
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    message = str(failure.value)
    # 정본 경계 검사가 먼저 잡는다. 파일 경로뿐 아니라 project/task graph 까지 대상이다.
    assert "public distribution boundary violated" in message and "cpf-admin" in message


def test_publisher_task_graph_must_not_reach_the_public_consumer(tmp_path: Path):
    """Open Git Release Task 는 Development Master 전용이다.

    공개 Consumer 가 또 다른 공개 Release 를 만들거나 공식 Repository 에 publish 하는 구조를
    만들지 않는다. private source 가 없어도 task graph 가 공개되면 Architecture Leakage 다.
    """
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    build_file = staging / "build.gradle"
    build_file.write_text(
        build_file.read_text(encoding="utf-8")
        + "\ntasks.register('cpfOpenGitBuild') { group = '60. CPF 배포' }\n",
        encoding="utf-8")
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    assert "publisher task graph leaked" in str(failure.value)


def test_private_component_project_graph_must_not_reach_the_public_consumer(tmp_path: Path):
    """private source 가 없어도 Gradle project/included build 항목이 있으면 FAIL 이다."""
    staging = _public_consumer_staging(tmp_path)
    assert MODULE.verify_open_git_tree(ROOT, staging, "binary")["status"] == "PASS"
    settings = staging / "settings.gradle"
    settings.write_text(
        settings.read_text(encoding="utf-8") + "\nincludeBuild('cpf-gateway')\n", encoding="utf-8")
    with pytest.raises(MODULE.OpenGitReleaseError) as failure:
        MODULE.verify_open_git_tree(ROOT, staging, "binary")
    assert "gradle graph entry present" in str(failure.value) and "cpf-gateway" in str(failure.value)


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

    # 추가 garbage: allowlist 방식이면 이런 파일은 애초에 Final Tree 로 넘어오지 않는다.
    for extra in (f"cpf-common-{version}.jar.sha256", f"cpf-common-{version}.jar.md5",
                  f"cpf-common-{version}.module", "maven-metadata.xml"):
        garbage = raw / "com/cpf/common/cpf-common" / version / extra
        garbage.parent.mkdir(parents=True, exist_ok=True)
        garbage.write_bytes(b"test")

    result = MODULE.sanitize_binary_repository(
        ROOT, raw, final, "binary", development_version=version, source_identity="0" * 64)

    # sources/javadoc 은 Final Public Tree 에서 제외된다.
    assert not (final / paths["common-source"].relative_to(raw)).exists()
    assert not (final / paths["common-javadoc"].relative_to(raw)).exists()
    assert not (final / paths["core-source"].relative_to(raw)).exists()
    assert not (final / paths["batch-source"].relative_to(raw)).exists()
    # Main JAR 은 유지된다.
    assert (final / paths["common-binary"].relative_to(raw)).is_file()
    assert (final / paths["core-binary"].relative_to(raw)).is_file()
    # checksum sidecar / module / maven-metadata 는 기본 제외이고 orphan 도 남지 않는다.
    assert list(final.rglob("*.sha256")) == []
    assert list(final.rglob("*.md5")) == []
    assert list(final.rglob("*.module")) == []
    assert list(final.rglob("maven-metadata.xml")) == []
    assert result["mode"] == "ALLOWLIST_FAIL_CLOSED"
    assert result["publicVersion"] == version
    assert result["droppedArtifacts"] > 0
    # Package Manifest 는 Final Tree 의 모든 Public artifact 를 정확히 담는다.
    manifest = json.loads((final / "package-manifest.json").read_text(encoding="utf-8"))
    listed = {row["relativePath"] for row in manifest["artifacts"]}
    actual = {p.relative_to(final).as_posix() for p in final.rglob("*")
              if p.is_file() and p.name != "package-manifest.json"}
    assert listed == actual, listed ^ actual


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
    (root / "cpf-docs/governance/development-harness/product").mkdir(parents=True)
    (root / "cpf-docs/governance/development-harness/current").mkdir(parents=True)
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
    canonical = root / "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md"
    canonical.write_text(
        "# Canonical\n\n### 21.3 Open Git Release Packaging\n\n- `cpf release open-git`\n\n## 22. EDU Canonical 35\n",
        encoding="utf-8",
    )
    (root / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text(
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
    assert (root / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").is_file()


def test_setup_integration_rejects_legacy_independent_open_git_surface(tmp_path: Path):
    root = tmp_path / "cpf"
    (root / "cpf-tools/verification/tools").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/java").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/contracts").mkdir(parents=True)
    (root / "cpf-docs/governance/development-harness/product").mkdir(parents=True)
    (root / "cpf-docs/governance/development-harness/current").mkdir(parents=True)
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
    (root / "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md").write_text(
        "### 21.3 Open Git Release Packaging\n`cpf release open-git`\n", encoding="utf-8"
    )
    (root / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text("# current\n", encoding="utf-8")
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


def test_final_binary_verifier_accepts_only_catalog_derived_gradle_plugin_marker(tmp_path: Path, monkeypatch):
    version = "1.2.3"
    plugin_row = {
        "artifactId": "cpf-gradle-plugin",
        "ownerPath": "cpf-tools/build/gradle-plugin",
        "kind": "gradle-plugin",
        "gradlePluginId": "com.cpf.platform-conventions",
        "publicationClass": "PUBLIC_TOOLING",
        "publicGroupId": "com.cpf.gradle",
    }
    monkeypatch.setattr(MODULE, "artifact_rows", lambda _root: [plugin_row])
    monkeypatch.setattr(
        MODULE,
        "load_json",
        lambda _path: {"requiredBinaryArtifactIdsWhenPublished": ["cpf-gradle-plugin"]},
    )
    repo = tmp_path / "repo"
    implementation = repo / "com/cpf/gradle/cpf-gradle-plugin" / version
    implementation.mkdir(parents=True)
    (implementation / f"cpf-gradle-plugin-{version}.jar").write_bytes(b"plugin")
    (implementation / f"cpf-gradle-plugin-{version}.pom").write_text(
        "<project><modelVersion>4.0.0</modelVersion><groupId>com.cpf.gradle</groupId>"
        f"<artifactId>cpf-gradle-plugin</artifactId><version>{version}</version></project>",
        encoding="utf-8",
    )
    marker = repo / "com/cpf/platform-conventions/com.cpf.platform-conventions.gradle.plugin" / version
    marker.mkdir(parents=True)
    marker_pom = marker / f"com.cpf.platform-conventions.gradle.plugin-{version}.pom"

    def write_marker(dependency_version: str) -> None:
        marker_pom.write_text(
            "<project><modelVersion>4.0.0</modelVersion>"
            "<groupId>com.cpf.platform-conventions</groupId>"
            "<artifactId>com.cpf.platform-conventions.gradle.plugin</artifactId>"
            f"<version>{version}</version><packaging>pom</packaging><dependencies><dependency>"
            "<groupId>com.cpf.gradle</groupId><artifactId>cpf-gradle-plugin</artifactId>"
            f"<version>{dependency_version}</version></dependency></dependencies></project>",
            encoding="utf-8",
        )

    write_marker(version)
    result = MODULE.verify_binary_repository(ROOT, repo, version)
    assert result["gradlePluginMarkerCount"] == 1
    assert result["binaryJarCount"] == 1

    write_marker("9.9.9")
    with pytest.raises(MODULE.OpenGitReleaseError, match="Gradle plugin marker dependency mismatch"):
        MODULE.verify_binary_repository(ROOT, repo, version)


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
    assert 'Release 작업공간 안전 확인' in source
    assert 'Fresh Workspace 빌드·테스트' in source
    assert 'Open Git 변경사항 검증' in source
    assert 'CPF OPEN GIT RELEASE 실패' in source
    assert 'Exit Code' in source
    assert '다음 조치 :' in source
    assert 'Commit    : NOT_EXECUTED' in source
    assert 'Push      : NOT_EXECUTED' in source


def test_canonical_publication_entrypoint_and_catalog_owned_documentation_variants_exist():
    convention = (ROOT / "cpf-tools/build/cpf-root-conventions.gradle").read_text(encoding="utf-8")
    assert "tasks.register('cpfPublishAllVerifiedLocalPlatformArtifacts')" in convention
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


def test_windows_open_git_stage6_builds_missing_linux_generator_in_docker(tmp_path: Path, monkeypatch):
    """A Windows release must create a real Linux binary, never relabel Windows."""
    calls = []

    class Backend:
        class PublishError(RuntimeError):
            pass

        @staticmethod
        def _verify_generator_distribution(directory, version, classifier):
            if directory.name == "generator-linux-matrix" and version == "9.9.9" and classifier == "linux-x64":
                return directory / "archive.zip", directory / "archive.zip.sha256", directory / "archive.json"
            raise Backend.PublishError("missing")

    def fake_run(command, cwd, *, capture=False, env=None):
        calls.append([str(value) for value in command])
        if command[1:2] == ["version"]:
            return "linux/amd64"
        return ""

    monkeypatch.setattr(MODULE, "_is_windows_host", lambda: True)
    monkeypatch.setattr(MODULE.shutil, "which", lambda name: "docker.exe" if name == "docker" else None)
    monkeypatch.setattr(MODULE, "run", fake_run)

    matrix = MODULE.prepare_generator_matrix(tmp_path, tmp_path / "work", "9.9.9", Backend, None)

    assert matrix == tmp_path / "work/generator-linux-matrix"
    assert calls[0] == ["docker.exe", "version", "--format", "{{.Server.Os}}/{{.Server.Arch}}"]
    docker_run = calls[1]
    assert docker_run[:5] == ["docker.exe", "run", "--rm", "--workdir", "/src"]
    assert "type=bind,src=" + str(tmp_path) + ",dst=/src,readonly" in docker_run
    assert "type=bind,src=" + str(matrix) + ",dst=/out" in docker_run
    assert MODULE.GENERATOR_LINUX_BUILD_IMAGE in docker_run
    assert "build-cpf-generator-binary.py" in docker_run[-1]
    assert "--version 9.9.9" in docker_run[-1]
    assert "apt-get install --yes --no-install-recommends binutils" in docker_run[-1]


def test_windows_open_git_stage6_fails_closed_without_docker_or_valid_linux_matrix(tmp_path: Path, monkeypatch):
    class Backend:
        class PublishError(RuntimeError):
            pass

        @staticmethod
        def _verify_generator_distribution(_directory, _version, _classifier):
            raise Backend.PublishError("missing")

    monkeypatch.setattr(MODULE, "_is_windows_host", lambda: True)
    monkeypatch.setattr(MODULE.shutil, "which", lambda _name: None)

    with pytest.raises(MODULE.OpenGitReleaseError, match="Docker Desktop Linux/amd64"):
        MODULE.prepare_generator_matrix(tmp_path, tmp_path / "work", "9.9.9", Backend, None)


def test_setup_currentizes_only_owned_canonical_section(tmp_path: Path):
    root = tmp_path / "cpf"
    (root / "cpf-tools/verification/tools").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/java").mkdir(parents=True)
    (root / "cpf-tools/runtime/cli/contracts").mkdir(parents=True)
    (root / "cpf-docs/governance/development-harness/product").mkdir(parents=True)
    (root / "cpf-docs/governance/development-harness/current").mkdir(parents=True)
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
    canonical = root / "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md"
    canonical.write_text(
        "# Canonical\n\n### 21.3 Open Git Release Packaging\n"
        "cpf release open-git\nOpen Git 개발자 Workspace는 `cpf bootstrap`으로 시작한다.\n"
        "\n## 22. EDU Canonical 35\nKEEP-EDU\n", encoding="utf-8"
    )
    (root / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").write_text("# current\n", encoding="utf-8")
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

    final_target = (ROOT / "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md").read_text(encoding="utf-8")
    fresh_requirement = (ROOT / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_FRESH_RELEASE_REQUIREMENT.md").read_text(encoding="utf-8")
    work_package = (ROOT / "cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md").read_text(encoding="utf-8")
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
