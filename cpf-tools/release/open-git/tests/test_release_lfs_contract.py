"""Executable negative tests for CPF's catalog-derived Git LFS Release contract."""

from __future__ import annotations

import hashlib
import importlib.util
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

import pytest


ROOT = Path(__file__).resolve().parents[4]
TOOL = ROOT / "cpf-tools/release/open-git/verify_release_lfs_contract.py"
SPEC = importlib.util.spec_from_file_location("cpf_release_lfs_contract", TOOL)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)

ENGINE_TOOL = ROOT / "cpf-tools/release/open-git/cpf_open_git.py"
ENGINE_SPEC = importlib.util.spec_from_file_location("cpf_open_git_engine", ENGINE_TOOL)
ENGINE = importlib.util.module_from_spec(ENGINE_SPEC)
assert ENGINE_SPEC and ENGINE_SPEC.loader
ENGINE_SPEC.loader.exec_module(ENGINE)

BINARY_DIR = "binary-repository"


def _git(checkout: Path, *args: str) -> str:
    executable = shutil.which("git")
    assert executable, "git is required for the Git LFS contract test"
    result = subprocess.run([executable, *args], cwd=checkout, text=True, encoding="utf-8",
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    assert result.returncode == 0, result.stdout
    return result.stdout


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def _repository(tmp_path: Path, *, project_attributes: bool = True) -> tuple[Path, Path]:
    """공개 저장소의 실제 배치를 만든다.

    Binary Repository 는 checkout 안의 binary-repository 다. 형제 디렉터리로 두면 LFS 패턴이
    어디에 붙어야 하는지를 테스트가 검사하지 못한다.
    """
    attributes_root = tmp_path / "open-git"
    repository = attributes_root / BINARY_DIR
    attributes_root.mkdir()
    (attributes_root / ".gitattributes").write_text(
        (ROOT / ".gitattributes").read_text(encoding="utf-8"), encoding="utf-8")
    artifacts: list[dict[str, object]] = []
    for artifact_id in MODULE.binary_runtime_artifact_ids(ROOT):
        relative = Path("com/cpf/runtime") / artifact_id / "1.0.0" / f"{artifact_id}-1.0.0.jar"
        path = repository / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(path, "w") as archive:
            archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
        artifacts.append({
            "artifactId": artifact_id,
            "relativePath": relative.as_posix(),
            "sha256": _sha256(path),
        })
    (repository / "package-manifest.json").write_text(json.dumps({
        "contract": "CPF_PUBLIC_PACKAGE_MANIFEST",
        "sourceIdentitySha256": "a" * 64,
        "artifacts": artifacts,
    }), encoding="utf-8")
    if project_attributes:
        ENGINE.project_lfs_attributes(ROOT, attributes_root)
    return repository, attributes_root


def test_catalog_derived_lfs_contract_accepts_materialized_runtime_jars(tmp_path: Path):
    repository, attributes_root = _repository(tmp_path)
    result = MODULE.verify(ROOT, repository, attributes_root, require_git_lfs=False, pull_if_checkout=False)
    assert result["status"] == "PASS"
    assert result["runtimeArtifactCount"] == 9


def test_lfs_pointer_is_rejected_before_runtime_or_maven_resolution(tmp_path: Path):
    repository, attributes_root = _repository(tmp_path)
    target = next(repository.rglob("*.jar"))
    target.write_bytes(b"version https://git-lfs.github.com/spec/v1\noid sha256:" + b"0" * 64 + b"\nsize 1\n")
    with pytest.raises(MODULE.LfsContractError, match="LFS_OBJECT_NOT_MATERIALIZED"):
        MODULE.verify(ROOT, repository, attributes_root, require_git_lfs=False, pull_if_checkout=False)


def test_runtime_lfs_sha_mismatch_is_rejected(tmp_path: Path):
    repository, attributes_root = _repository(tmp_path)
    manifest_path = repository / "package-manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    manifest["artifacts"][0]["sha256"] = "0" * 64
    manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
    with pytest.raises(MODULE.LfsContractError, match="LFS_HASH_MISMATCH"):
        MODULE.verify(ROOT, repository, attributes_root, require_git_lfs=False, pull_if_checkout=False)


def test_global_jar_lfs_attribute_is_rejected(tmp_path: Path):
    repository, attributes_root = _repository(tmp_path)
    attributes = attributes_root / ".gitattributes"
    attributes.write_text(attributes.read_text(encoding="utf-8")
                          + "\n*.jar filter=lfs diff=lfs merge=lfs -text\n", encoding="utf-8")
    with pytest.raises(MODULE.LfsContractError, match="RELEASE_MANIFEST_MISMATCH"):
        MODULE.verify(ROOT, repository, attributes_root, require_git_lfs=False, pull_if_checkout=False)


def test_master_relative_lfs_scope_is_rejected_for_the_public_projection(tmp_path: Path):
    """CRF-59. Master 의 .gitattributes 를 그대로 복사한 공개 저장소는 통과하면 안 된다.

    증상 근거: 공개 저장소의 Binary Repository 는 checkout 최상위의 binary-repository 인데
    투영된 패턴은 cpf-release/binary-repository 를 가리켰다. 어떤 Runtime 실행물도 LFS 대상이
    되지 못했고 110.8MiB 인 cpf-admin 실행물이 일반 Git blob 으로 남았다.
    """
    repository, attributes_root = _repository(tmp_path, project_attributes=False)
    with pytest.raises(MODULE.LfsContractError, match="RELEASE_MANIFEST_MISMATCH"):
        MODULE.verify(ROOT, repository, attributes_root,
                      require_git_lfs=False, pull_if_checkout=False)


def test_binary_repository_outside_the_attributes_root_is_rejected(tmp_path: Path):
    """LFS 패턴이 붙을 위치를 구할 수 없으면 추측하지 않고 실패한다."""
    repository, attributes_root = _repository(tmp_path)
    outside = tmp_path / "elsewhere"
    shutil.copytree(repository, outside)
    with pytest.raises(MODULE.LfsContractError, match="RELEASE_MANIFEST_MISMATCH"):
        MODULE.verify(ROOT, outside, attributes_root,
                      require_git_lfs=False, pull_if_checkout=False)


def test_declared_lfs_scope_must_actually_apply_to_the_artifact_path(tmp_path: Path):
    """CRF-59. 문자열이 맞아도 git 이 적용하지 않으면 실패한다.

    되돌리면 재발할 증상: .gitattributes 문자열 비교만 남아, 적용되지 않는 scope 를 PASS 로
    보고하고 push 단계에서야 거부된다.
    """
    repository, attributes_root = _repository(tmp_path)
    _git(attributes_root, "init", "--quiet")
    passing = MODULE.verify(ROOT, repository, attributes_root,
                            require_git_lfs=False, pull_if_checkout=False)
    assert passing["attributeEffect"] == {"verified": True, "artifactCount": 9}

    attributes = attributes_root / ".gitattributes"
    attributes.write_text(
        attributes.read_text(encoding="utf-8") + f"{BINARY_DIR}/**/*.jar -filter -diff -merge\n",
        encoding="utf-8")
    with pytest.raises(MODULE.LfsContractError, match="LFS_ATTRIBUTE_NOT_APPLIED"):
        MODULE.verify(ROOT, repository, attributes_root,
                      require_git_lfs=False, pull_if_checkout=False)


def test_file_over_the_transport_limit_without_lfs_is_rejected(tmp_path: Path):
    """외부 transport 한도를 넘는 일반 Git blob 은 push 전에 걸러진다.

    이 한도는 Packaging 목표가 아니라 호스팅 제약이다. Runtime 을 줄이라는 뜻이 아니라
    LFS 로 옮기라는 뜻이다.
    """
    repository, attributes_root = _repository(tmp_path)
    _git(attributes_root, "init", "--quiet")
    limit = MODULE.source_policy(ROOT)["artifactClassification"]["gitLfs"][
        "regularGitTransportLimitBytes"]
    oversized = attributes_root / "docs" / "oversized-attachment.bin"
    oversized.parent.mkdir(parents=True, exist_ok=True)
    with oversized.open("wb") as stream:
        stream.truncate(limit + 1)
    with pytest.raises(MODULE.LfsContractError, match="GIT_TRANSPORT_LIMIT_EXCEEDED"):
        MODULE.verify(ROOT, repository, attributes_root,
                      require_git_lfs=False, pull_if_checkout=False)


def test_transport_limit_ignores_paths_git_would_not_push(tmp_path: Path):
    """gitignore 된 산출물은 전송 대상이 아니므로 한도 위반이 아니다."""
    repository, attributes_root = _repository(tmp_path)
    _git(attributes_root, "init", "--quiet")
    limit = MODULE.source_policy(ROOT)["artifactClassification"]["gitLfs"][
        "regularGitTransportLimitBytes"]
    (attributes_root / ".gitignore").write_text("work/\n", encoding="utf-8")
    ignored = attributes_root / "work" / "candidate.bin"
    ignored.parent.mkdir(parents=True, exist_ok=True)
    with ignored.open("wb") as stream:
        stream.truncate(limit + 1)
    result = MODULE.verify(ROOT, repository, attributes_root,
                           require_git_lfs=False, pull_if_checkout=False)
    assert result["transportLimit"]["verified"] is True
