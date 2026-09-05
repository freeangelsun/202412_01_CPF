"""Executable negative tests for CPF's catalog-derived Git LFS Release contract."""

from __future__ import annotations

import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path

import pytest


ROOT = Path(__file__).resolve().parents[4]
TOOL = ROOT / "cpf-tools/release/open-git/verify_release_lfs_contract.py"
SPEC = importlib.util.spec_from_file_location("cpf_release_lfs_contract", TOOL)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


def _sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def _repository(tmp_path: Path) -> tuple[Path, Path]:
    repository = tmp_path / "binary-repository"
    attributes_root = tmp_path / "open-git"
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
