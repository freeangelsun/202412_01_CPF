from __future__ import annotations

import importlib.util
import json
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-qa34-build-contract.py"
SPEC = importlib.util.spec_from_file_location("qa34_build", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def fixture(root: Path) -> None:
    modules = []
    for i in range(6):
        modules.append({"artifactId": f"public-{i}", "projectPath": f":public-{i}", "ownerPath": f"cpf-starters/profiles/p{i}", "visibility": "public", "kind": "starter-profile", "internalRole": "public-profile"})
    for i in range(32):
        modules.append({"artifactId": f"internal-{i}", "projectPath": f":internal-{i}", "ownerPath": f"cpf-starters/data/i{i}", "visibility": "internal", "kind": "internal-starter", "internalRole": "provider"})
    catalog = {"baselinePolicy": "GIT_HEAD_RUNTIME", "baselineSha": "RUNTIME_GIT_HEAD", "modules": modules}
    payload = json.dumps(catalog)
    write(root, "cpf-tools/generator/contracts/cpf-starter-catalog.json", payload)
    write(root, "cpf-tools/config/cpf-starter-catalog.json", payload)
    write(root, "settings.gradle", "pluginManagement {}\nincludeBuild('cpf-tools/build/gradle-plugin')\ndef c=file('cpf-tools/generator/contracts/cpf-starter-catalog.json')\nincludeBuild('cpf-tools/build/platform-bom')\n")
    write(root, "cpf-tools/build/gradle-plugin/build.gradle", "id = 'com.cpf.platform-conventions'\ngroup = 'com.cpf.gradle'\n")
    write(root, "cpf-tools/build/platform-bom/settings.gradle", "include 'public-bom', 'internal-bom'\n")
    write(root, "cpf-tools/build/platform-bom/build.gradle", "dependsOn ':public-bom:publishAllPublicationsToCpfLocalRepository', ':internal-bom:publishAllPublicationsToCpfLocalRepository'\ncheck.dependsOn ':public-bom:check', ':internal-bom:check'\n")
    write(root, "cpf-tools/build/platform-bom/public-bom/build.gradle", "def c=file('../../../generator/contracts/cpf-starter-catalog.json')\ngroup='com.cpf'\ndef publicModules=x.findAll { it.visibility?.toString()=='public' }\nif(publicModules.size()!=6){}\nthrow new Exception('Internal Starter leaked into public BOM')\nartifactId='cpf-platform-bom'\n")
    write(root, "cpf-tools/build/platform-bom/internal-bom/build.gradle", "def c=file('../../../generator/contracts/cpf-starter-catalog.json')\ngroup='com.cpf.internal'\ndef internalModules=x.findAll { it.visibility?.toString()=='internal' }\nif(internalModules.size()!=32){}\napi platform('com.cpf:cpf-platform-bom:1')\nthrow new Exception('Public profile leaked into internal BOM')\nartifactId='cpf-internal-platform-bom'\n")
    write(root, "cpf-member/build.gradle", "com.cpf.platform-conventions")
    generated = "com.cpf.platform-conventions com.cpf:cpf-platform-bom"
    write(root, "cpf-tools/generator/create-domain.ps1", generated)
    write(root, "cpf-tools/generator/export-domain-repository.ps1", generated)
    write(root, "cpf-tools/generator/create-domain-jobpack.ps1", generated)
    write(root, "cpf-tools/scripts/verify-local-artifact-propagation.ps1", "com.cpf.platform-conventions com.cpf.gradle cpf-platform-bom")


def test_accepts_split_public_internal_bom_and_catalog_parity(tmp_path: Path) -> None:
    fixture(tmp_path)
    result = MODULE.verify(tmp_path)
    assert result["publicCount"] == 6
    assert result["internalCount"] == 32


def test_rejects_catalog_mirror_drift(tmp_path: Path) -> None:
    fixture(tmp_path)
    mirror = tmp_path / "cpf-tools/config/cpf-starter-catalog.json"
    mirror.write_text(mirror.read_text() + "\n")
    with pytest.raises(MODULE.ContractError, match="mirror drift"):
        MODULE.verify(tmp_path)


def test_rejects_internal_bom_coordinate_in_generated_domain(tmp_path: Path) -> None:
    fixture(tmp_path)
    generator = tmp_path / "cpf-tools/generator/create-domain.ps1"
    generator.write_text(generator.read_text() + " com.cpf.internal:cpf-internal-platform-bom")
    with pytest.raises(MODULE.ContractError, match="forbidden token"):
        MODULE.verify(tmp_path)
