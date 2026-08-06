#!/usr/bin/env python3
"""Fail-closed CPF convention plugin, split BOM, and generator build contract gate."""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

CANONICAL_PLUGIN = "com.cpf.platform-conventions"
LEGACY_PLUGIN = "com.cpf.domain-conventions"
CANONICAL_BOM = "com.cpf:cpf-platform-bom"
INTERNAL_BOM = "com.cpf.internal:cpf-internal-platform-bom"
LEGACY_BOM = "com.cpf:cpf-bom"
CANONICAL_CATALOG = Path("cpf-tools/generator/contracts/cpf-starter-catalog.json")
CATALOG_MIRROR = Path("cpf-tools/config/cpf-starter-catalog.json")


class ContractError(RuntimeError):
    pass


def text(path: Path) -> str:
    if not path.is_file():
        raise ContractError(f"missing required file: {path}")
    return path.read_text(encoding="utf-8-sig")


def json_object(path: Path) -> dict:
    try:
        value = json.loads(text(path))
    except json.JSONDecodeError as exc:
        raise ContractError(f"invalid JSON {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ContractError(f"JSON root must be object: {path}")
    return value


def require(material: str, token: str, label: str) -> None:
    if token not in material:
        raise ContractError(f"{label} missing required token: {token}")


def forbid(material: str, token: str, label: str) -> None:
    if token in material:
        raise ContractError(f"{label} still contains forbidden token: {token}")


def verify_generator_bom_contract(generator_materials: dict[str, str]) -> None:
    """Require every generated Gradle path to consume only the public CPF BOM."""
    for name, material in generator_materials.items():
        require(material, CANONICAL_BOM, name)
        forbid(material, LEGACY_BOM, name)
        forbid(material, INTERNAL_BOM, name)


def verify_catalog(root: Path) -> dict:
    canonical_path = root / CANONICAL_CATALOG
    mirror_path = root / CATALOG_MIRROR
    canonical_bytes = canonical_path.read_bytes() if canonical_path.is_file() else b""
    mirror_bytes = mirror_path.read_bytes() if mirror_path.is_file() else b""
    if not canonical_bytes:
        raise ContractError(f"canonical starter catalog missing: {CANONICAL_CATALOG}")
    if not mirror_bytes:
        raise ContractError(f"starter catalog mirror missing: {CATALOG_MIRROR}")
    if canonical_bytes != mirror_bytes:
        raise ContractError(
            f"starter catalog mirror drift: {CATALOG_MIRROR} must be byte-identical to {CANONICAL_CATALOG}"
        )
    catalog = json_object(canonical_path)
    if catalog.get("baselinePolicy") != "GIT_HEAD_RUNTIME" or catalog.get("baselineSha") != "RUNTIME_GIT_HEAD":
        raise ContractError("starter catalog must use runtime Git HEAD baseline policy")
    modules = catalog.get("modules")
    if not isinstance(modules, list) or not modules:
        raise ContractError("starter catalog modules must be non-empty")
    public = [m for m in modules if isinstance(m, dict) and m.get("visibility") == "public"]
    internal = [m for m in modules if isinstance(m, dict) and m.get("visibility") == "internal"]
    official_profiles = catalog.get("publicProfiles") or []
    public_ids = {str(m.get("artifactId", "")).removeprefix("cpf-starter-profile-") for m in public}
    if public_ids != set(official_profiles):
        raise ContractError(f"public starter profile exact set mismatch expected={sorted(official_profiles)} actual={sorted(public_ids)}")
    layout = catalog.get("targetPhysicalLayout") or {}
    if layout.get("moduleCount") != len(modules) or layout.get("profileCount") != len(public):
        raise ContractError("starter catalog derived layout counts mismatch")
    for key in ("artifactId", "projectPath", "ownerPath"):
        values = [m.get(key) for m in modules if isinstance(m, dict)]
        if any(not isinstance(value, str) or not value.strip() for value in values):
            raise ContractError(f"starter catalog contains missing {key}")
        if len(values) != len(set(values)):
            raise ContractError(f"starter catalog contains duplicate {key}")
    if any(m.get("kind") != "starter-profile" or m.get("internalRole") != "public-profile" for m in public):
        raise ContractError("public starter catalog surface contains an internal leaf")
    if any(m.get("kind") != "internal-starter" for m in internal):
        raise ContractError("internal starter catalog surface contains a public profile")
    return {"moduleCount": len(modules), "publicCount": len(public), "internalCount": len(internal)}


def verify(root: Path) -> dict:
    settings = text(root / "settings.gradle")
    plugin_build = text(root / "cpf-tools/build/gradle-plugin/build.gradle")
    aggregate_bom = text(root / "cpf-tools/build/platform-bom/build.gradle")
    bom_settings = text(root / "cpf-tools/build/platform-bom/settings.gradle")
    public_bom = text(root / "cpf-tools/build/platform-bom/public-bom/build.gradle")
    internal_bom = text(root / "cpf-tools/build/platform-bom/internal-bom/build.gradle")
    member = text(root / "cpf-member/build.gradle")
    generator = text(root / "cpf-tools/generator/create-domain.ps1")
    exporter = text(root / "cpf-tools/generator/export-domain-repository.ps1")
    jobpack = text(root / "cpf-tools/generator/create-domain-jobpack.ps1")
    verifier = text(root / "cpf-tools/scripts/verify-local-artifact-propagation.ps1")

    require(settings, "pluginManagement", "root settings")
    require(settings, "includeBuild('cpf-tools/build/gradle-plugin')", "root settings")
    require(settings, str(CANONICAL_CATALOG).replace("\\", "/"), "root settings")
    require(settings, "includeBuild('cpf-tools/build/platform-bom')", "root settings")

    if f"id = '{CANONICAL_PLUGIN}'" not in plugin_build and f"id='{CANONICAL_PLUGIN}'" not in plugin_build:
        raise ContractError("canonical plugin ID is not published")
    require(plugin_build, "group = 'com.cpf.gradle'", "convention plugin")

    require(bom_settings, "include 'public-bom', 'internal-bom'", "platform BOM settings")
    for token in (
        ":public-bom:publishAllPublicationsToCpfLocalRepository",
        ":internal-bom:publishAllPublicationsToCpfLocalRepository",
        "check.dependsOn ':public-bom:check', ':internal-bom:check'",
    ):
        require(aggregate_bom, token, "platform BOM aggregate")

    for material, label, group, artifact, visibility, verify_task in (
        (public_bom, "public BOM", "group='com.cpf'", "artifactId='cpf-platform-bom'", "visibility?.toString()=='public'", "verifyPublicBom"),
        (internal_bom, "internal BOM", "group='com.cpf.internal'", "artifactId='cpf-internal-platform-bom'", "visibility?.toString()=='internal'", "verifyInternalBom"),
    ):
        require(material, group, label)
        require(material, artifact, label)
        require(material, str(CANONICAL_CATALOG.name), label)
        require(material, visibility, label)
        require(material, verify_task, label)
        for equality_marker in ("missing", "extra", "duplicate"):
            require(material, equality_marker, label)
        if re.search(r"(?:publicModules|internalModules)\.size\(\)\s*[!=]=\s*\d+", material):
            raise ContractError(f"{label} contains fixed-count validation")
    require(public_bom, "Internal Starter leaked into public BOM", "public BOM")
    require(internal_bom, CANONICAL_BOM, "internal BOM")
    require(internal_bom, "Public profile leaked into internal BOM", "internal BOM")

    for name, material in {"cpf-member": member, "generator": generator}.items():
        require(material, CANONICAL_PLUGIN, name)
        forbid(material, LEGACY_PLUGIN, name)
    verify_generator_bom_contract(
        {
            "create-domain generator": generator,
            "domain repository exporter": exporter,
            "domain jobpack generator": jobpack,
        }
    )
    for required in (CANONICAL_PLUGIN, "com.cpf.gradle", "cpf-platform-bom"):
        require(verifier, required, "artifact verifier")
    for forbidden in (LEGACY_PLUGIN, "com.cpf.build", "cpf-bom/$version"):
        forbid(verifier, forbidden, "artifact verifier")

    catalog = verify_catalog(root)
    return {"status": "PASS", **catalog, "publicBom": CANONICAL_BOM, "internalBom": INTERNAL_BOM}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root).resolve())
    except ContractError as exc:
        print(f"CPF canonical build contract: FAIL\n{exc}")
        return 1
    print(
        "CPF canonical build contract: PASS "
        f"modules={result['moduleCount']} public={result['publicCount']} internal={result['internalCount']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
