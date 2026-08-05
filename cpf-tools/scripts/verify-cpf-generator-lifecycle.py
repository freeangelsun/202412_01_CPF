#!/usr/bin/env python3
"""Fail-closed static/release verifier for the CPF generated-domain lifecycle."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

VENDORS = ["mariadb", "postgresql", "oracle"]
STAGES = [
    "fresh-clone", "create", "upgrade-ownership", "database-bootstrap", "build-test", "runtime-smoke",
    "adm-registration", "user-change-protection", "safe-remove", "regenerate", "parity",
]
SHA_RE = re.compile(r"^[0-9a-f]{40}$")
HASH_RE = re.compile(r"^[0-9a-f]{64}$")


class ContractError(RuntimeError):
    pass


def load_json(path: Path) -> dict:
    if not path.is_file():
        raise ContractError(f"missing JSON: {path}")
    try:
        value = json.loads(path.read_text(encoding="utf-8-sig"))
    except Exception as exc:
        raise ContractError(f"invalid JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ContractError(f"JSON root must be object: {path}")
    return value


def require_file(root: Path, relative: str) -> Path:
    path = root / relative
    if not path.is_file():
        raise ContractError(f"required file missing: {relative}")
    return path


def assert_tokens(path: Path, tokens: list[str]) -> None:
    text = path.read_text(encoding="utf-8-sig")
    missing = [token for token in tokens if token not in text]
    if missing:
        raise ContractError(f"{path}: required lifecycle tokens missing={missing}")


def validate_contract(root: Path, contract: dict) -> None:
    if contract.get("schemaVersion") != 1:
        raise ContractError("schemaVersion must be 1")
    if contract.get("supportedVendors") != VENDORS:
        raise ContractError(f"supportedVendors must be exactly {VENDORS}")
    if contract.get("stages") != STAGES:
        raise ContractError(f"stages must be exact ordered lifecycle={STAGES}")
    marker = contract.get("disposableRootMarker")
    if not isinstance(marker, str) or not marker.startswith(".cpf-disposable-"):
        raise ContractError("disposableRootMarker must be an explicit .cpf-disposable-* marker")
    scripts = contract.get("requiredScripts")
    if not isinstance(scripts, dict) or set(scripts) != {"createEntryPoint", "create", "database", "remove", "lifecycle", "upgrade"}:
        raise ContractError("requiredScripts must define createEntryPoint/create/database/remove/lifecycle/upgrade")
    resolved = {name: require_file(root, rel) for name, rel in scripts.items()}
    assert_tokens(resolved["createEntryPoint"], [
        "generator/create-domain.ps1", "Canonical CPF generator not found", "@GeneratorArgs",
    ])
    assert_tokens(resolved["create"], [
        "generator-ownership.json", "createdFiles", "sha256", "Generated file already exists",
        "databaseRemovalPolicy",
    ])
    assert_tokens(resolved["database"], [
        'ValidateSet("bootstrap", "migration", "verify", "rollback")',
        "ConfirmRollback", "database-profile.json", "Assert-CpfSupportedDatabaseVendor",
    ])
    assert_tokens(resolved["remove"], [
        "changedGeneratedFiles", "userOwnedFiles", "externalReferences", "blockReasons",
        "databaseObjectsRemoved = $false", "DryRun",
    ])
    assert_tokens(resolved["upgrade"], [
        "managed file drift", "unmanaged target collision", "obsolete managed file drift",
        "retainedObsoleteOwnership", "ADD_USER_OWNED_DEFAULT",
    ])
    assert_tokens(resolved["lifecycle"], [
        marker, *STAGES, "generator-lifecycle-result.sanitized.json", "normalizedSha256",
        "changedGeneratedFiles", "userOwnedFiles", "externalReferences",
    ])

    expected_upgrade_protection = {
        "generatedOwnedFileUpdatedWhenChecksumMatches": True,
        "generatedOwnedFileDriftBlocksUpgrade": True,
        "unmanagedTargetCollisionBlocksUpgrade": True,
        "customerOwnedExtensionPreservedWithoutOwnershipCapture": True,
        "obsoleteGeneratedOwnershipRetainedUntilApprovedDeletion": True,
    }
    if contract.get("upgradeProtection") != expected_upgrade_protection:
        raise ContractError("upgradeProtection must remain fail-closed for all five policies")

    expected_protection = {
        "changedGeneratedFileBlocksRemoval": True,
        "userOwnedFileBlocksRemoval": True,
        "externalReferenceBlocksRemoval": True,
        "databaseObjectsNeverAutoDropped": True,
    }
    if contract.get("userProtection") != expected_protection:
        raise ContractError("userProtection must remain fail-closed for all four policies")
    parity = contract.get("parity")
    if not isinstance(parity, dict) or not parity.get("requireIdenticalFileSet") or not parity.get("requireIdenticalNormalizedSha256"):
        raise ContractError("parity must require identical file set and normalized hashes")
    required_files = contract.get("requiredGeneratedFiles")
    if not isinstance(required_files, list) or len(required_files) < 4 or len(required_files) != len(set(required_files)):
        raise ContractError("requiredGeneratedFiles must be a unique product file list")


def validate_release_evidence(root: Path, contract: dict, expected_sha: str, evidence_dir: Path | None = None) -> None:
    if not SHA_RE.fullmatch(expected_sha):
        raise ContractError("--expected-sha must be a lowercase 40-character Git SHA")
    evidence_cfg = contract.get("releaseEvidence")
    if not isinstance(evidence_cfg, dict):
        raise ContractError("releaseEvidence contract missing")
    base = evidence_dir.resolve() if evidence_dir is not None else root / str(evidence_cfg.get("directory", ""))
    pattern = str(evidence_cfg.get("filePattern", ""))
    for vendor in VENDORS:
        path = base / pattern.format(vendor=vendor)
        evidence = load_json(path)
        if evidence.get("status") != evidence_cfg.get("requiredStatus"):
            raise ContractError(f"{vendor}: lifecycle status is not PASS")
        if evidence.get("vendor") != vendor:
            raise ContractError(f"{vendor}: evidence vendor mismatch")
        if evidence.get("sourceSha") != expected_sha or evidence.get("resultSha") != expected_sha:
            raise ContractError(f"{vendor}: exact-SHA mismatch")
        if evidence.get("sanitized") is not True:
            raise ContractError(f"{vendor}: evidence is not sanitized")
        if evidence.get("cleanBefore") is not True or evidence.get("cleanAfter") is not True:
            raise ContractError(f"{vendor}: disposable clone was not clean before/after")
        stage_results = evidence.get("stages")
        ids = [item.get("id") for item in stage_results if isinstance(item, dict)] if isinstance(stage_results, list) else []
        if ids != STAGES:
            raise ContractError(f"{vendor}: stage results are incomplete or out of order")
        for item in stage_results:
            if item.get("status") != "PASS" or item.get("exitCode") != 0:
                raise ContractError(f"{vendor}: stage failed or unexecuted: {item.get('id')}")
            if not isinstance(item.get("assertions"), list) or not item["assertions"]:
                raise ContractError(f"{vendor}: stage assertions missing: {item.get('id')}")
        if evidence.get("upgradeOwnershipVerified") is not True:
            raise ContractError(f"{vendor}: upgrade ownership protection not verified")
        if evidence.get("userProtectionVerified") is not True:
            raise ContractError(f"{vendor}: user-change protection not verified")
        if evidence.get("parityVerified") is not True:
            raise ContractError(f"{vendor}: regeneration parity not verified")
        artifacts = evidence.get("artifacts")
        if not isinstance(artifacts, list) or not artifacts:
            raise ContractError(f"{vendor}: artifact list missing")
        for artifact in artifacts:
            if not isinstance(artifact, dict) or not HASH_RE.fullmatch(str(artifact.get("sha256", ""))):
                raise ContractError(f"{vendor}: invalid artifact SHA-256")
            artifact_path = root / str(artifact.get("path", ""))
            if not artifact_path.is_file():
                raise ContractError(f"{vendor}: artifact missing: {artifact.get('path')}")
            if hashlib.sha256(artifact_path.read_bytes()).hexdigest() != artifact["sha256"]:
                raise ContractError(f"{vendor}: artifact hash mismatch: {artifact.get('path')}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--contract", default="cpf-tools/generator/contracts/generator-lifecycle-contract.json")
    parser.add_argument("--expected-sha", default="")
    parser.add_argument("--release", action="store_true")
    parser.add_argument("--evidence-dir", type=Path)
    args = parser.parse_args()
    root = args.root.resolve()
    contract = load_json(root / args.contract)
    validate_contract(root, contract)
    if args.release:
        validate_release_evidence(root, contract, args.expected_sha, args.evidence_dir)
    print(f"[PASS] CPF generator lifecycle vendors={len(VENDORS)} stages={len(STAGES)} release={args.release}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ContractError as exc:
        print(f"[FAIL] {exc}", file=sys.stderr)
        raise SystemExit(1)
