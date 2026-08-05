from __future__ import annotations

import importlib.util
import json
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-generator-lifecycle.py"
SPEC = importlib.util.spec_from_file_location("generator_lifecycle", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def write(root: Path, rel: str, content: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def contract() -> dict:
    return {
        "schemaVersion": 1,
        "supportedVendors": MODULE.VENDORS,
        "stages": MODULE.STAGES,
        "disposableRootMarker": ".cpf-disposable-generator-validation-root",
        "requiredScripts": {
            "createEntryPoint": "cpf-tools/scripts/create-domain.ps1",
            "create": "cpf-tools/generator/create-domain.ps1",
            "database": "cpf-tools/scripts/initialize-domain-database.ps1",
            "remove": "cpf-tools/scripts/remove-domain.ps1",
            "lifecycle": "cpf-tools/generator/verify-domain-lifecycle.ps1",
            "upgrade": "cpf-tools/generator/upgrade-domain.ps1",
        },
        "upgradeProtection": {
            "generatedOwnedFileUpdatedWhenChecksumMatches": True,
            "generatedOwnedFileDriftBlocksUpgrade": True,
            "unmanagedTargetCollisionBlocksUpgrade": True,
            "customerOwnedExtensionPreservedWithoutOwnershipCapture": True,
            "obsoleteGeneratedOwnershipRetainedUntilApprovedDeletion": True,
        },
        "userProtection": {
            "changedGeneratedFileBlocksRemoval": True,
            "userOwnedFileBlocksRemoval": True,
            "externalReferenceBlocksRemoval": True,
            "databaseObjectsNeverAutoDropped": True,
        },
        "parity": {"requireIdenticalFileSet": True, "requireIdenticalNormalizedSha256": True},
        "requiredGeneratedFiles": ["a", "b", "c", "d"],
    }


def populate(root: Path) -> None:
    write(root, "cpf-tools/scripts/create-domain.ps1", "generator/create-domain.ps1 @GeneratorArgs Canonical CPF generator not found")
    write(root, "cpf-tools/generator/create-domain.ps1", "generator-ownership.json createdFiles sha256 Generated file already exists databaseRemovalPolicy")
    write(root, "cpf-tools/scripts/initialize-domain-database.ps1", 'ValidateSet("bootstrap", "migration", "verify", "rollback") ConfirmRollback database-profile.json Assert-CpfSupportedDatabaseVendor')
    write(root, "cpf-tools/scripts/remove-domain.ps1", "changedGeneratedFiles userOwnedFiles externalReferences blockReasons databaseObjectsRemoved = $false DryRun")
    write(root, "cpf-tools/generator/upgrade-domain.ps1", "managed file drift unmanaged target collision obsolete managed file drift retainedObsoleteOwnership ADD_USER_OWNED_DEFAULT")
    write(root, "cpf-tools/generator/verify-domain-lifecycle.ps1", " ".join([".cpf-disposable-generator-validation-root", *MODULE.STAGES, "generator-lifecycle-result.sanitized.json", "normalizedSha256", "changedGeneratedFiles", "userOwnedFiles", "externalReferences"]))


def test_accepts_separate_entrypoint_and_implementation(tmp_path: Path) -> None:
    populate(tmp_path)
    MODULE.validate_contract(tmp_path, contract())


def test_rejects_wrapper_that_does_not_delegate_to_canonical_generator(tmp_path: Path) -> None:
    populate(tmp_path)
    write(tmp_path, "cpf-tools/scripts/create-domain.ps1", "Write-Output fake")
    with pytest.raises(MODULE.ContractError, match="required lifecycle tokens"):
        MODULE.validate_contract(tmp_path, contract())
