#!/usr/bin/env python3
"""Fail-closed Git LFS transport and materialization verifier for CPF Public Runtime.

The LFS scope comes from the Runtime Target Catalog through the Release Asset Policy.
It deliberately does not infer a decision from a file extension, a directory glob, or a
size threshold.  This tool is read-only: it never runs ``git add``, writes a manifest,
or changes an LFS object.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path
from typing import Any


LFS_POINTER_PREFIX = b"version https://git-lfs.github.com/spec/v1"
MANIFEST_NAME = "package-manifest.json"


class LfsContractError(RuntimeError):
    """An error that preserves the public bootstrap/release failure code."""

    def __init__(self, code: str, detail: str) -> None:
        super().__init__(f"{code}: {detail}")
        self.code = code


def read_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"invalid JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"JSON object required: {path}")
    return value


def source_policy(root: Path) -> dict[str, Any]:
    policy = read_json(root / "cpf-tools/release/open-git/open-git-surface-policy.json")
    authority = policy.get("releaseAssetPolicy")
    if not isinstance(authority, dict):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "releaseAssetPolicy is missing")
    return authority


def binary_runtime_artifact_ids(root: Path) -> list[str]:
    catalog = read_json(root / "cpf-tools/runtime/cpf-runtime-target-catalog.json")
    runtimes = catalog.get("runtimes")
    if not isinstance(runtimes, list):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "runtime catalog runtimes is missing")
    ids = sorted({str(row.get("artifactId") or "") for row in runtimes
                  if isinstance(row, dict) and row.get("provision") == "binary"})
    if not ids or any(not artifact_id for artifact_id in ids):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "binary runtime artifactId is missing")
    return ids


def executable_rule(authority: dict[str, Any]) -> dict[str, Any]:
    classification = authority.get("artifactClassification")
    if not isinstance(classification, dict):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "artifactClassification is missing")
    matches = [row for row in classification.get("rules", [])
               if isinstance(row, dict) and row.get("id") == "publicBinaryRuntimeExecutable"]
    if len(matches) != 1:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "publicBinaryRuntimeExecutable rule must exist exactly once")
    rule = matches[0]
    if (rule.get("assetClass") != "LARGE_RELEASE_BINARY" or rule.get("transport") != "GIT_LFS"
            or rule.get("masterTracked") is not True or rule.get("publicRelease") is not True):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "binary runtime rule is not the canonical GIT_LFS class")
    return rule


def expected_attribute_lines(root: Path) -> set[str]:
    authority = source_policy(root)
    rule = executable_rule(authority)
    template = str(rule.get("gitAttributesPathTemplate") or "")
    if "{artifactId}" not in template:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "LFS attribute path template is missing artifactId")
    return {template.format(artifactId=artifact_id) + " filter=lfs diff=lfs merge=lfs -text"
            for artifact_id in binary_runtime_artifact_ids(root)}


def verify_attributes(root: Path, attributes_root: Path) -> set[str]:
    attribute_file = attributes_root / ".gitattributes"
    if not attribute_file.is_file():
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f".gitattributes missing: {attribute_file}")
    lines = [line.strip() for line in attribute_file.read_text(encoding="utf-8").splitlines()
             if line.strip() and not line.lstrip().startswith("#")]
    actual = {line for line in lines if "filter=lfs" in line}
    expected = expected_attribute_lines(root)
    if actual != expected:
        missing = sorted(expected - actual)
        extra = sorted(actual - expected)
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH",
                               f"LFS attribute scope drift missing={missing} extra={extra}")
    broad = {"*.jar", "**/*.jar", "cpf-release/binary-repository/**/*.jar"}
    if {line.split(maxsplit=1)[0] for line in actual}.intersection(broad):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "global JAR LFS attribute is forbidden")
    return expected


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def is_lfs_pointer(path: Path) -> bool:
    with path.open("rb") as stream:
        return stream.read(len(LFS_POINTER_PREFIX)).startswith(LFS_POINTER_PREFIX)


def _safe_relative(value: object) -> Path:
    raw = str(value or "")
    path = Path(raw)
    if not raw or path.is_absolute() or ".." in path.parts:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"unsafe manifest path={raw!r}")
    return path


def load_manifest(repository: Path) -> list[dict[str, Any]]:
    manifest_path = repository / MANIFEST_NAME
    manifest = read_json(manifest_path)
    if manifest.get("contract") != "CPF_PUBLIC_PACKAGE_MANIFEST":
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "unexpected package manifest contract")
    source_identity = str(manifest.get("sourceIdentitySha256") or "")
    if len(source_identity) != 64 or any(char not in "0123456789abcdef" for char in source_identity.lower()):
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "package manifest sourceIdentitySha256 is invalid")
    artifacts = manifest.get("artifacts")
    if not isinstance(artifacts, list) or not artifacts:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "package manifest artifacts is empty")
    result: list[dict[str, Any]] = []
    seen: set[Path] = set()
    for row in artifacts:
        if not isinstance(row, dict):
            raise LfsContractError("RELEASE_MANIFEST_MISMATCH", "package manifest artifact is not an object")
        relative = _safe_relative(row.get("relativePath"))
        if relative in seen:
            raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"duplicate manifest path={relative}")
        seen.add(relative)
        expected_sha = str(row.get("sha256") or "")
        if len(expected_sha) != 64 or any(char not in "0123456789abcdef" for char in expected_sha.lower()):
            raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"invalid SHA-256 for {relative}")
        target = repository / relative
        if not target.is_file():
            raise LfsContractError("RELEASE_MANIFEST_MISMATCH", f"manifest artifact missing={relative}")
        if is_lfs_pointer(target):
            raise LfsContractError("LFS_OBJECT_NOT_MATERIALIZED", f"manifest artifact remains an LFS pointer={relative}")
        actual_sha = sha256(target)
        if actual_sha != expected_sha:
            code = "LFS_HASH_MISMATCH" if target.suffix == ".jar" else "RELEASE_MANIFEST_MISMATCH"
            raise LfsContractError(code, f"manifest SHA-256 mismatch={relative}")
        result.append(dict(row))

    actual_files = {path.relative_to(repository) for path in repository.rglob("*")
                    if path.is_file() and path.name != MANIFEST_NAME}
    manifest_files = {_safe_relative(row.get("relativePath")) for row in result}
    if actual_files != manifest_files:
        raise LfsContractError("RELEASE_MANIFEST_MISMATCH",
                               f"manifest file set drift missing={sorted(actual_files - manifest_files)} "
                               f"extra={sorted(manifest_files - actual_files)}")
    return result


def verify_runtime_artifacts(root: Path, repository: Path, artifacts: list[dict[str, Any]]) -> None:
    for artifact_id in binary_runtime_artifact_ids(root):
        matches = [row for row in artifacts
                   if str(row.get("artifactId")) == artifact_id
                   and str(row.get("relativePath", "")).endswith(".jar")]
        if len(matches) != 1:
            raise LfsContractError("RUNTIME_ARTIFACT_INVALID",
                                   f"expected one executable runtime artifact={artifact_id}, actual={len(matches)}")
        target = repository / _safe_relative(matches[0].get("relativePath"))
        if is_lfs_pointer(target):
            raise LfsContractError("LFS_OBJECT_NOT_MATERIALIZED", f"runtime remains an LFS pointer={target}")
        if not zipfile.is_zipfile(target):
            raise LfsContractError("RUNTIME_ARTIFACT_INVALID", f"runtime JAR is not a ZIP={target}")


def verify_git_lfs_available(*, pull: bool, checkout: Path) -> None:
    git = shutil.which("git")
    if not git:
        raise LfsContractError("GIT_LFS_NOT_AVAILABLE", "git command is unavailable")
    version = subprocess.run([git, "lfs", "version"], cwd=checkout, text=True,
                             stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
    if version.returncode != 0:
        raise LfsContractError("GIT_LFS_NOT_AVAILABLE", version.stdout.strip() or "git lfs version failed")
    if pull and (checkout / ".git").exists():
        result = subprocess.run([git, "lfs", "pull"], cwd=checkout, text=True,
                                stdout=subprocess.PIPE, stderr=subprocess.STDOUT, check=False)
        if result.returncode != 0:
            raise LfsContractError("LFS_DOWNLOAD_FAILED", result.stdout.strip() or "git lfs pull failed")


def verify(root: Path, repository: Path, attributes_root: Path, *, require_git_lfs: bool,
           pull_if_checkout: bool) -> dict[str, Any]:
    root = root.resolve()
    repository = repository.resolve()
    attributes_root = attributes_root.resolve()
    expected = verify_attributes(root, attributes_root)
    if require_git_lfs:
        verify_git_lfs_available(pull=pull_if_checkout, checkout=attributes_root)
    artifacts = load_manifest(repository)
    verify_runtime_artifacts(root, repository, artifacts)
    return {
        "status": "PASS",
        "runtimeArtifactCount": len(binary_runtime_artifact_ids(root)),
        "manifestArtifactCount": len(artifacts),
        "lfsAttributeCount": len(expected),
        "repository": str(repository),
        "attributesRoot": str(attributes_root),
        "gitLfsRequired": require_git_lfs,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Verify CPF Git LFS release transport and materialization")
    parser.add_argument("--root", type=Path, required=True)
    parser.add_argument("--repository", type=Path, required=True)
    parser.add_argument("--attributes-root", type=Path, required=True)
    parser.add_argument("--require-git-lfs", action="store_true")
    parser.add_argument("--pull-if-checkout", action="store_true")
    args = parser.parse_args()
    try:
        print(json.dumps(verify(args.root, args.repository, args.attributes_root,
                                 require_git_lfs=args.require_git_lfs,
                                 pull_if_checkout=args.pull_if_checkout), ensure_ascii=False, sort_keys=True))
        return 0
    except LfsContractError as exc:
        print(f"CPF_RELEASE_LFS=FAIL {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
