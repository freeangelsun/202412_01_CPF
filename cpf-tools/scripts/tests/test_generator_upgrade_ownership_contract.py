from __future__ import annotations

import importlib.util
import json
import shutil
from pathlib import Path

import pytest


ROOT = Path(__file__).resolve().parents[3]
CONTRACT = ROOT / "cpf-tools/generator/contracts/generator-lifecycle-contract.json"
UPGRADE = ROOT / "cpf-tools/generator/upgrade-domain.ps1"
LIFECYCLE = ROOT / "cpf-tools/generator/verify-domain-lifecycle.ps1"
VERIFIER = ROOT / "cpf-tools/scripts/verify-cpf-generator-lifecycle.py"


def load_verifier():
    spec = importlib.util.spec_from_file_location("cpf_generator_lifecycle_gate", VERIFIER)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_upgrade_source_keeps_generated_java_managed_and_fails_closed() -> None:
    text = UPGRADE.read_text(encoding="utf-8-sig")

    assert "$userOwnedPrefixes = @('src/main/java/', 'src/test/java/', 'ui/')" not in text
    assert "$userOwnedFiles = @('README.md', 'config/cpf-approved-exceptions.csv')" in text
    assert "managed file drift: $relative" in text
    assert "unmanaged target collision: $relative" in text
    assert "obsolete managed file drift: $ownedPath" in text
    assert "retainedObsoleteOwnership" in text
    assert "$newOwnedFiles += [ordered]@{ path=$obsoleteRelative" in text
    assert "function Get-SafeOwnedRelativePath" in text
    assert "function Get-OwnedTargetPath" in text
    assert "function Assert-Sha256" in text
    assert "Generator ownership path escapes the generated domain root" in text
    assert "Generator ownership manifest contains an invalid SHA-256" in text
    assert text.index("$oldOwned = @{}") < text.index("$tempRoot = Join-Path")


def test_lifecycle_executes_upgrade_ownership_before_database_and_build() -> None:
    contract = json.loads(CONTRACT.read_text(encoding="utf-8"))
    lifecycle = LIFECYCLE.read_text(encoding="utf-8-sig")

    assert contract["stages"] == [
        "fresh-clone", "create", "upgrade-ownership", "database-bootstrap",
        "build-test", "runtime-smoke", "adm-registration", "user-change-protection",
        "safe-remove", "regenerate", "parity",
    ]
    assert contract["requiredScripts"]["createEntryPoint"] == "cpf-tools/scripts/create-domain.ps1"
    assert contract["requiredScripts"]["create"] == "cpf-tools/generator/create-domain.ps1"
    assert contract["requiredScripts"]["upgrade"] == "cpf-tools/generator/upgrade-domain.ps1"
    assert "upgradeOwnershipVerified = $UpgradeOwnershipVerified" in lifecycle
    assert lifecycle.index('Add-StageResult "upgrade-ownership"') < lifecycle.index('Add-StageResult "database-bootstrap"')
    assert "upgrade accepted a modified generator-owned Java file" in lifecycle
    assert "customer-owned extension was incorrectly claimed by generator ownership" in lifecycle
    assert "obsolete generated file ownership was dropped before approved deletion" in lifecycle
    assert "upgrade accepted an ownership path traversal entry" in lifecycle
    assert "upgrade accepted an invalid ownership SHA-256" in lifecycle


def test_gate_rejects_wrapper_misclassified_as_canonical_generator(tmp_path: Path) -> None:
    shutil.copytree(ROOT / "cpf-tools", tmp_path / "cpf-tools")
    contract_path = tmp_path / "cpf-tools/generator/contracts/generator-lifecycle-contract.json"
    contract = json.loads(contract_path.read_text(encoding="utf-8"))
    contract["requiredScripts"]["create"] = "cpf-tools/scripts/create-domain.ps1"
    contract_path.write_text(json.dumps(contract), encoding="utf-8")

    gate = load_verifier()
    with pytest.raises(gate.ContractError, match="required lifecycle tokens missing"):
        gate.validate_contract(tmp_path, gate.load_json(contract_path))


def test_release_gate_rejects_missing_upgrade_ownership_evidence(tmp_path: Path) -> None:
    gate = load_verifier()
    contract = gate.load_json(CONTRACT)
    artifact = tmp_path / "artifact.txt"
    artifact.write_text("fixture", encoding="utf-8")
    import hashlib
    digest = hashlib.sha256(artifact.read_bytes()).hexdigest()
    evidence_root = tmp_path / "evidence"
    stages = [
        {"id": stage, "status": "PASS", "exitCode": 0, "assertions": ["fixture"]}
        for stage in gate.STAGES
    ]
    for vendor in gate.VENDORS:
        vendor_dir = evidence_root / vendor
        vendor_dir.mkdir(parents=True)
        evidence = {
            "status": "PASS",
            "vendor": vendor,
            "sourceSha": "a" * 40,
            "resultSha": "a" * 40,
            "sanitized": True,
            "cleanBefore": True,
            "cleanAfter": True,
            # upgradeOwnershipVerified intentionally omitted
            "userProtectionVerified": True,
            "parityVerified": True,
            "stages": stages,
            "artifacts": [{"path": str(artifact.relative_to(tmp_path)), "sha256": digest}],
        }
        (vendor_dir / "generator-lifecycle-result.sanitized.json").write_text(
            json.dumps(evidence), encoding="utf-8"
        )

    with pytest.raises(gate.ContractError, match="upgrade ownership protection not verified"):
        gate.validate_release_evidence(tmp_path, contract, "a" * 40, evidence_root)
