from __future__ import annotations

import importlib.util
import json
import subprocess
from pathlib import Path

import pytest


SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-starter-catalog-truth.py"
SPEC = importlib.util.spec_from_file_location("starter_truth", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def initialize(root: Path, catalog: dict) -> None:
    payload = json.dumps(catalog)
    for relative in (
        "cpf-tools/generator/contracts/cpf-starter-catalog.json",
    ):
        path = root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(payload, encoding="utf-8")
    subprocess.run(["git", "init", "-q", str(root)], check=True)
    subprocess.run(["git", "-C", str(root), "config", "user.email", "test@example.invalid"], check=True)
    subprocess.run(["git", "-C", str(root), "config", "user.name", "test"], check=True)
    subprocess.run(["git", "-C", str(root), "add", "."], check=True)
    subprocess.run(["git", "-C", str(root), "commit", "-qm", "fixture"], check=True)


def base_catalog() -> dict:
    return {
        "catalogId": "fixture",
        "baselinePolicy": "GIT_HEAD_RUNTIME",
        "baselineSha": "RUNTIME_GIT_HEAD",
        "publicProfiles": ["minimal-domain"],
        "capabilityGroups": [{"id": "data", "path": "cpf-starters/data"}],
        "modules": [
            {
                "artifactId": "cpf-starter-profile-minimal-domain",
                "projectPath": ":starters:profiles:minimal-domain",
                "profileId": "minimal-domain",
                "ownerPath": "cpf-starters/profiles/minimal-domain",
                "packageBase": "com.cpf.starter.profile.minimaldomain",
                "visibility": "public",
                "role": "profile",
            },
            {
                "artifactId": "cpf-starter-data-jdbc",
                "projectPath": ":starters:data:jdbc",
                "ownerPath": "cpf-starters/data/jdbc",
                "packageBase": "com.cpf.starter.data.jdbc",
                "visibility": "internal",
                "role": "provider",
            },
        ],
        "providerSlots": {
            "data": {
                "jdbc": {
                    "projectPath": ":starters:data:jdbc",
                    "coordinate": "com.cpf.starter:cpf-starter-data-jdbc",
                }
            }
        },
        "removedArtifactIds": [],
        "starterAdmissionPolicy": {"failClosed": True},
        "profileDefinitions": {
            "minimal-domain": {
                "artifactId": "cpf-starter-profile-minimal-domain",
                "runtimeProjects": [":starters:profiles:minimal-domain"],
                "requiredCapabilities": ["data"],
                "providerSlots": ["data"],
            }
        },
    }


def write_module(root: Path, owner: str, package: str | None) -> None:
    directory = root / owner
    directory.mkdir(parents=True)
    (directory / "build.gradle").write_text("plugins { id 'java-library' }\n", encoding="utf-8")
    if package:
        java = directory / "src/main/java" / Path(*package.split(".")) / "Marker.java"
        java.parent.mkdir(parents=True)
        java.write_text(f"package {package};\npublic final class Marker {{}}\n", encoding="utf-8")


def test_rejects_module_without_physical_package(tmp_path: Path) -> None:
    catalog = base_catalog()
    for module in catalog["modules"]:
        write_module(tmp_path, module["ownerPath"], None)
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    with pytest.raises(MODULE.GateError, match="no physical main Java package declaration"):
        MODULE.verify(tmp_path)


def test_rejects_duplicate_and_active_removed_artifact(tmp_path: Path) -> None:
    catalog = base_catalog()
    duplicate = dict(catalog["modules"][1])
    duplicate["ownerPath"] = "cpf-starters/data/other"
    duplicate["packageBase"] = "com.cpf.starter.data.other"
    catalog["modules"].append(duplicate)
    catalog["removedArtifactIds"] = ["cpf-starter-data-jdbc"]
    for module in catalog["modules"]:
        write_module(tmp_path, module["ownerPath"], module["packageBase"])
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    with pytest.raises(MODULE.GateError) as failure:
        MODULE.verify(tmp_path)
    message = str(failure.value)
    assert "duplicate artifactId" in message
    assert "active artifact also listed as removed" in message


def test_accepts_closed_consistent_catalog(tmp_path: Path) -> None:
    catalog = base_catalog()
    for module in catalog["modules"]:
        write_module(tmp_path, module["ownerPath"], module["packageBase"])
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    result = MODULE.verify(tmp_path)
    assert result["status"] == "PASS"
    assert result["moduleCount"] == 2



def test_accepts_dependency_only_public_profile(tmp_path: Path) -> None:
    catalog = base_catalog()
    profile, internal = catalog["modules"]
    write_module(tmp_path, profile["ownerPath"], None)
    write_module(tmp_path, internal["ownerPath"], internal["packageBase"])
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    result = MODULE.verify(tmp_path)
    assert result["status"] == "PASS"


def test_dependency_only_internal_module_remains_fail_closed(tmp_path: Path) -> None:
    catalog = base_catalog()
    profile, internal = catalog["modules"]
    write_module(tmp_path, profile["ownerPath"], None)
    write_module(tmp_path, internal["ownerPath"], None)
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    with pytest.raises(MODULE.GateError, match="no physical main Java package declaration"):
        MODULE.verify(tmp_path)

def test_rejects_canonical_catalog_missing(tmp_path: Path) -> None:
    catalog = base_catalog()
    for module in catalog["modules"]:
        write_module(tmp_path, module["ownerPath"], module["packageBase"])
    (tmp_path / "cpf-starters/data").mkdir(parents=True, exist_ok=True)
    initialize(tmp_path, catalog)
    canonical = tmp_path / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    canonical.unlink()
    with pytest.raises(MODULE.GateError, match="catalog missing"):
        MODULE.verify(tmp_path)
