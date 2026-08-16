from __future__ import annotations

import importlib.util
import json
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-publication-starter-closure.py"
SPEC = importlib.util.spec_from_file_location("publication_starter", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def fixture(root: Path) -> tuple[Path, Path, Path]:
    canonical = {
        "modules": [
            {"artifactId": "cpf-starter-profile-a", "ownerPath": "cpf-starters/profiles/a"},
            {"artifactId": "cpf-starter-data-b", "ownerPath": "cpf-starters/data/b"},
        ]
    }
    final = {
        "sourceShaPolicy": "runtime-exact-sha-evidence",
        "baselinePolicy": "GIT_HEAD_RUNTIME",
        "baselineSha": "RUNTIME_GIT_HEAD",
        "canonicalStarterCatalog": "cpf-tools/generator/contracts/cpf-starter-catalog.json",
        "removedArtifactIds": [],
        "artifacts": [
            {"artifactId": "cpf-starter-profile-a", "ownerPath": "cpf-starters/profiles/a", "kind": "starter-profile"},
            {"artifactId": "cpf-starter-data-b", "ownerPath": "cpf-starters/data/b", "kind": "internal-starter"},
        ],
    }
    canonical_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    final_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    script_path = root / "cpf-tools/verification/tools/verify-local-artifact-propagation.ps1"
    canonical_path.parent.mkdir(parents=True, exist_ok=True)
    final_path.parent.mkdir(parents=True, exist_ok=True)
    script_path.parent.mkdir(parents=True, exist_ok=True)
    canonical_path.write_text(json.dumps(canonical), encoding="utf-8")
    final_path.write_text(json.dumps(final), encoding="utf-8")
    script_path.write_text("\n".join([
        "$starterKinds = @('starter-profile', 'internal-starter')",
        "$artifactCatalog.canonicalStarterCatalog",
        "$canonicalByArtifact",
        "Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-starters') -Recurse -File",
        "Starter catalog/physical project mismatch",
        "$batchArtifactRows = @($artifactCatalog.artifacts",
        "artifact='cpf-internal-platform-bom'",
        "$publicStarterArtifacts",
        "$internalStarterArtifacts",
        "Internal Starter leaked into public BOM",
        "CPF internal BOM must import the exact public BOM once",
    ]), encoding="utf-8")
    return canonical_path, final_path, script_path


def test_accepts_nested_starter_closure(tmp_path: Path) -> None:
    fixture(tmp_path)
    result = MODULE.verify(tmp_path)
    assert result["starterCount"] == 2
    assert result["publicProfiles"] == 1
    assert result["internalStarters"] == 1


def test_rejects_legacy_flat_kind_and_consumer(tmp_path: Path) -> None:
    _, final_path, script_path = fixture(tmp_path)
    final = json.loads(final_path.read_text())
    final["artifacts"][0]["kind"] = "starter"
    final_path.write_text(json.dumps(final), encoding="utf-8")
    script_path.write_text(script_path.read_text() + "\nWhere-Object { [string]$_.kind -eq 'starter' }", encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="legacy final artifact kind"):
        MODULE.verify(tmp_path)



def test_rejects_stale_static_baseline(tmp_path: Path) -> None:
    _, final_path, _ = fixture(tmp_path)
    final = json.loads(final_path.read_text())
    final["baselineSha"] = "4aea798c913787e86341809e2cef2b9495cbf7ba"
    final_path.write_text(json.dumps(final), encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="runtime Git HEAD baseline"):
        MODULE.verify(tmp_path)

def test_rejects_owner_path_drift(tmp_path: Path) -> None:
    _, final_path, _ = fixture(tmp_path)
    final = json.loads(final_path.read_text())
    final["artifacts"][1]["ownerPath"] = "cpf-starters/data/other"
    final_path.write_text(json.dumps(final), encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="closure mismatch"):
        MODULE.verify(tmp_path)


def test_physical_mode_rejects_missing_module(tmp_path: Path) -> None:
    fixture(tmp_path)
    with pytest.raises(MODULE.GateError, match="physical Starter Gradle modules missing"):
        MODULE.verify(tmp_path, require_physical=True)


def test_rejects_legacy_center_cut_artifact_assumption(tmp_path: Path) -> None:
    _, _, script_path = fixture(tmp_path)
    script_path.write_text(script_path.read_text() + "\n$artifactId -ne 'cpf-center-cut-runner'", encoding="utf-8")
    with pytest.raises(MODULE.GateError, match="flat/legacy Starter assumptions"):
        MODULE.verify(tmp_path)


def test_physical_mode_accepts_exact_nested_module_closure(tmp_path: Path) -> None:
    fixture(tmp_path)
    for relative in ["cpf-starters/profiles/a", "cpf-starters/data/b"]:
        directory = tmp_path / relative
        directory.mkdir(parents=True, exist_ok=True)
        (directory / "build.gradle").write_text("plugins { id 'java-library' }\n", encoding="utf-8")
    result = MODULE.verify(tmp_path, require_physical=True)
    assert result["physicalChecked"] is True
