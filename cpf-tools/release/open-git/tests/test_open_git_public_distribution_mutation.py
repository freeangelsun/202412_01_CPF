"""Open Git Public Product Distribution negative mutation.

Final Tree 검증이 실제로 fail-closed 인지 확인한다. 각 mutation 은 과거에 실제로 발생했거나
Allowlist 를 되돌리면 즉시 재발하는 결함이다.

  * checksum sidecar 1258개와 timestamped SNAPSHOT 49개가 Public Tree 로 통과하던 상태
  * sources/javadoc JAR 만 지워 orphan sidecar 128개가 남던 상태
  * Binary 는 있는데 Launcher 가 없거나 한쪽 OS 만 있던 상태
  * README 가 <cpf-binary-repository-url> placeholder 라 실행 불가능하던 상태
  * governance 7784건이 통째로 공개될 뻔한 상태
"""
from __future__ import annotations

import hashlib
import importlib.util
import json
import re
import shutil
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[4]
SPEC = importlib.util.spec_from_file_location(
    "cpf_open_git_mutation", ROOT / "cpf-tools/release/open-git/cpf_open_git.py")
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def _repository(tmp_path: Path) -> Path:
    """Allowlist 를 통과하는 최소 Public Binary Repository."""
    repository = tmp_path / "binary-repository"
    artifact_dir = repository / "com/cpf/core/cpf-core/1.0.0"
    artifact_dir.mkdir(parents=True)
    jar = artifact_dir / "cpf-core-1.0.0.jar"
    pom = artifact_dir / "cpf-core-1.0.0.pom"
    jar.write_bytes(b"jar")
    pom.write_text("<project/>", encoding="utf-8")
    artifacts = [{
        "group": "com.cpf.core", "artifactId": "cpf-core", "module": "cpf-core",
        "version": "1.0.0", "classifier": None, "type": path.suffix.lstrip("."),
        "relativePath": path.relative_to(repository).as_posix(),
        "fileSize": path.stat().st_size,
        "sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
        "publicationType": "PUBLIC_COMPILE_TIME_JAVA", "classification": "PUBLIC",
        "sourceIdentitySha256": "0" * 64,
    } for path in (jar, pom)]
    (repository / "package-manifest.json").write_text(json.dumps({
        "contract": "CPF_PUBLIC_PACKAGE_MANIFEST", "schemaVersion": 1,
        "publicVersion": "1.0.0", "developmentVersion": "1.0.0-SNAPSHOT",
        "sourceIdentitySha256": "0" * 64, "artifactCount": len(artifacts),
        "artifacts": artifacts,
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return repository


def test_clean_repository_passes(tmp_path: Path):
    MODULE.verify_public_binary_repository_tree(_repository(tmp_path))


@pytest.mark.parametrize("name", [
    "cpf-core-1.0.0.jar.sha256", "cpf-core-1.0.0.jar.md5",
    "cpf-core-1.0.0.jar.sha1", "cpf-core-1.0.0.jar.sha512",
])
def test_checksum_sidecar_is_rejected(tmp_path: Path, name: str):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0" / name).write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def test_timestamped_snapshot_artifact_is_rejected(tmp_path: Path):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0/cpf-core-1.0.0-20260901.123456-1.jar").write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def test_snapshot_artifact_is_rejected(tmp_path: Path):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0/cpf-core-1.0.0-SNAPSHOT.jar").write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


@pytest.mark.parametrize("name", ["cpf-core-1.0.0.module", "maven-metadata.xml"])
def test_unapproved_metadata_is_rejected(tmp_path: Path, name: str):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0" / name).write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


@pytest.mark.parametrize("classifier", ["sources", "javadoc"])
def test_denied_classifier_jar_is_rejected(tmp_path: Path, classifier: str):
    repository = _repository(tmp_path)
    (repository / f"com/cpf/core/cpf-core/1.0.0/cpf-core-1.0.0-{classifier}.jar").write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def test_manifest_unregistered_binary_is_rejected(tmp_path: Path):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0/cpf-extra-1.0.0.jar").write_bytes(b"x")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def test_manifest_entry_without_file_is_rejected(tmp_path: Path):
    repository = _repository(tmp_path)
    (repository / "com/cpf/core/cpf-core/1.0.0/cpf-core-1.0.0.pom").unlink()
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def test_manifest_sha_tampering_is_rejected(tmp_path: Path):
    repository = _repository(tmp_path)
    manifest_path = repository / "package-manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    manifest["artifacts"][0]["sha256"] = "0" * 64
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_binary_repository_tree(repository)


def _launcher_tree(tmp_path: Path) -> Path:
    open_git = tmp_path / "open-git"
    bin_dir = open_git / "bin"
    bin_dir.mkdir(parents=True)
    for name in ("start", "stop", "status", "restart", "health", "log", "help"):
        (bin_dir / f"cpf-{name}.ps1").write_text("", encoding="utf-8")
        (bin_dir / f"cpf-{name}.sh").write_text("", encoding="utf-8")
    return open_git


def test_launcher_parity_passes_when_complete(tmp_path: Path):
    MODULE.verify_public_launcher_parity(_launcher_tree(tmp_path))


def test_windows_only_launcher_is_rejected(tmp_path: Path):
    open_git = _launcher_tree(tmp_path)
    (open_git / "bin/cpf-start.sh").unlink()
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_launcher_parity(open_git)


def test_linux_only_launcher_is_rejected(tmp_path: Path):
    open_git = _launcher_tree(tmp_path)
    (open_git / "bin/cpf-status.ps1").unlink()
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_launcher_parity(open_git)


def test_missing_lifecycle_launcher_is_rejected(tmp_path: Path):
    open_git = _launcher_tree(tmp_path)
    (open_git / "bin/cpf-stop.ps1").unlink()
    (open_git / "bin/cpf-stop.sh").unlink()
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_launcher_parity(open_git)


def _readme_tree(tmp_path: Path, body: str) -> Path:
    open_git = tmp_path / "open-git-readme"
    (open_git / "cpf-docs/guides").mkdir(parents=True, exist_ok=True)
    (open_git / "cpf-docs/guides/guide.pdf").write_bytes(b"pdf")
    (open_git / "README.md").write_text(body, encoding="utf-8")
    return open_git


def test_readme_with_valid_reference_passes(tmp_path: Path):
    MODULE.verify_public_readme(_readme_tree(tmp_path, "# CPF\n\n`cpf-docs/guides/guide.pdf`\n"))


def test_readme_placeholder_is_rejected(tmp_path: Path):
    tree = _readme_tree(tmp_path, "# CPF\n\nCPF_MAVEN_REPOSITORY_URL='<cpf-binary-repository-url>'\n")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_readme(tree)


def test_readme_broken_document_link_is_rejected(tmp_path: Path):
    tree = _readme_tree(tmp_path, "# CPF\n\n`cpf-docs/guides/missing.pdf`\n")
    with pytest.raises(MODULE.OpenGitReleaseError):
        MODULE.verify_public_readme(tree)


def test_governance_documentation_is_not_public():
    """governance/work 등 내부 관리자료는 Public Documentation Allowlist 밖이다."""
    for internal in ("cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md",
                     "cpf-docs/work/anything.md",
                     "cpf-docs/deliverables/CHANGE_MANIFEST.csv"):
        assert MODULE._is_forbidden_public_document(ROOT, internal), internal
    for public in ("cpf-docs/guides/04_운영자_매뉴얼.pdf",
                   "cpf-docs/legal/THIRD_PARTY_NOTICES.md"):
        assert not MODULE._is_forbidden_public_document(ROOT, public), public


def test_runtime_alias_allowlist_is_derived_from_catalog():
    """com.cpf.runtime alias 를 verifier 가 따로 하드코딩하면 catalog 와 갈라진다.

    실측: Public Runtime 9종을 catalog 에 추가했더니 verifier 의 고정 RUNTIME_ALIASES 에 없어
    정상 게시물이 'unclassified CPF runtime alias' 로 오탐되어 Release 가 실패했다.
    """
    verifier_spec = importlib.util.spec_from_file_location(
        "cpf_public_binary_verifier",
        ROOT / "cpf-tools/release/public/verify-cpf-public-binary-repository.py")
    assert verifier_spec and verifier_spec.loader
    verifier = importlib.util.module_from_spec(verifier_spec)
    verifier_spec.loader.exec_module(verifier)

    catalog = json.loads(
        (ROOT / "cpf-tools/release/cpf-final-artifact-catalog.json").read_text(encoding="utf-8-sig"))
    expected = {
        str(row["artifactId"]) for row in catalog["artifacts"]
        if str(row.get("publicGroupId") or "") == "com.cpf.runtime"
    }
    derived = verifier._catalog_runtime_aliases(ROOT)
    missing = sorted(expected - derived)
    assert not missing, f"catalog runtime alias not recognised by verifier: {missing}"


def test_generator_distribution_is_admitted_but_plain_checksum_is_not():
    """Generator 배포본은 Public artifact 이고, 일반 JAR checksum sidecar 는 아니다.

    실측: Allowlist 를 jar/pom 으로만 두었더니 필수 Public artifact 인
    com.cpf.tooling:cpf-generator-cli 배포본(zip)이 통째로 탈락해 Release 가 실패했다.
    """
    admitted = {
        "cpf-generator-cli-1.0.0-windows-x64.zip": "generator-distribution",
        "cpf-generator-cli-1.0.0-linux-x64.zip.sha256": "generator-distribution-checksum",
        "cpf-generator-cli-1.0.0-linux-x64.json": "generator-distribution-manifest",
    }
    for name, expected in admitted.items():
        assert MODULE._classify_public_artifact(name) == expected, name
    # 일반 artifact 의 checksum sidecar 는 여전히 제외 대상이다.
    assert MODULE._classify_public_artifact("cpf-core-1.0.0.jar.sha256") == "checksum-sidecar"
    assert MODULE._classify_public_artifact("cpf-core-1.0.0.jar.md5") == "checksum-sidecar"


def test_public_gradle_wrapper_does_not_write_into_governance():
    """Public wrapper 가 governance 경로를 쓰면 사용자가 Gradle 을 한 번만 돌려도 tree 가 오염된다.

    실측: 개발 저장소 gradlew 가 그대로 투영되어 Open Git checkout 에서 Gradle 을 실행하자
    cpf-docs/governance/development-harness/evidence/... 아래 10개 파일이 생성됐다.
    """
    for name in ("gradlew", "gradlew.bat"):
        template = ROOT / "cpf-tools/release/open-git/templates" / name
        assert template.is_file(), f"public gradle wrapper template missing: {name}"
        text = template.read_text(encoding="utf-8")
        assert "cpf-docs/governance" not in text, f"{name} still writes into governance"
        assert "cpf-docs\governance" not in text, f"{name} still writes into governance"


def test_surface_policy_projects_public_wrapper_not_development_one():
    """개발 저장소 wrapper 를 직접 복사하는 sourceRule 이 되살아나면 안 된다."""
    policy = json.loads(
        (ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json").read_text(encoding="utf-8-sig"))
    for rule in policy["sourceRules"]:
        assert rule.get("pattern") not in ("gradlew", "gradlew.bat"), (
            "development gradle wrapper must not be copied directly")
    template_targets = {rule.get("target") for rule in policy["templateRules"]}
    assert {"gradlew", "gradlew.bat"} <= template_targets


def test_surface_policy_projects_gradle_runtime_resources():
    """gradlew 는 gradle/cpf-runtime/*.properties 를 fail-closed 로 요구한다."""
    policy = json.loads(
        (ROOT / "cpf-tools/release/open-git/open-git-surface-policy.json").read_text(encoding="utf-8-sig"))
    patterns = {rule.get("pattern") for rule in policy["sourceRules"]}
    assert "gradle/cpf-runtime/**" in patterns, (
        "Fresh checkout 에서 Gradle 을 실행하려면 resource policy 가 projection 되어야 한다")


def test_release_root_is_rebuilt_from_scratch():
    """Release 는 exact generated root 를 지우고 0부터 만든다(Current-only)."""
    engine = (ROOT / "cpf-tools/release/open-git/cpf_open_git.py").read_text(encoding="utf-8")
    body = engine.split("def clean_release_root", 1)[1].split("\ndef ", 1)[0]
    assert "verify_release_root_safety" in body, "cleanup must be bounded to the approved root"
    assert "shutil.rmtree(target)" in body, "previous release must be removed, not merged"
    assert "mkdir(parents=True, exist_ok=False)" in body, "release root must be recreated empty"


def test_legacy_publisher_owns_no_independent_release_root():
    """legacy publisher 가 timestamp Release directory 를 만들면 과거 산출물이 축적된다."""
    legacy = (ROOT / "cpf-tools/release/public/publish-cpf-public-repository.py").read_text(encoding="utf-8")
    body = legacy.split("def release_root", 1)[1].split("\ndef ", 1)[0]
    assert "CPF_PUBLIC_RELEASE_" not in body, (
        "legacy publisher must not generate its own timestamped release root")
    assert "legacy publisher no longer owns a release root" in body


def test_canonical_engine_uses_legacy_only_as_staging_backend():
    """canonical engine 은 legacy publish() 전체 흐름을 호출하지 않는다."""
    engine = (ROOT / "cpf-tools/release/open-git/cpf_open_git.py").read_text(encoding="utf-8")
    used = set(re.findall(r"backend\.([a-z_]+)", engine))
    assert "publish" not in used, "canonical engine must not delegate the whole legacy publish flow"
    assert "release_root" not in used, "canonical engine must own the release root"
    assert used <= {"private_build_and_publication", "private_gates",
                    "publish_generator_distributions", "run", "sync_public_surface"}, used
