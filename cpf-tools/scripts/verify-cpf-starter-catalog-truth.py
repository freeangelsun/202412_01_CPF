#!/usr/bin/env python3
"""Fail-closed validator for the canonical CPF starter catalog and derivatives."""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path


class GateError(RuntimeError):
    pass


def load_json(path: Path) -> dict:
    if not path.is_file():
        raise GateError(f"required JSON missing: {path}")
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except json.JSONDecodeError as exc:
        raise GateError(f"invalid JSON {path}: {exc}") from exc


def unique(values: list[str], label: str) -> None:
    blanks = [index for index, value in enumerate(values) if not value]
    if blanks:
        raise GateError(f"blank {label} at indexes={blanks[:20]}")
    duplicates = [value for value, count in Counter(values).items() if count > 1]
    if duplicates:
        raise GateError(f"duplicate {label}={duplicates[:20]}")


def require_equal(label: str, expected, actual) -> None:
    if expected != actual:
        raise GateError(f"{label} drift: canonical={expected!r} derivative={actual!r}")


def validate(root: Path, metadata_only: bool) -> dict:
    contract_root = root / "cpf-tools/generator/contracts"
    catalog_path = contract_root / "cpf-starter-catalog.json"
    profiles_path = contract_root / "capability-profiles.json"
    settings_path = root / "settings.gradle"
    public_bom_path = root / "cpf-tools/build/platform-bom/public-bom/build.gradle"
    catalog = load_json(catalog_path)
    profiles = load_json(profiles_path)
    if not settings_path.is_file():
        raise GateError(f"settings.gradle missing: {settings_path}")
    if not public_bom_path.is_file():
        raise GateError(f"public BOM missing: {public_bom_path}")
    settings = settings_path.read_text(encoding="utf-8-sig")
    public_bom = public_bom_path.read_text(encoding="utf-8-sig")

    modules = catalog.get("modules") or []
    public_profiles = catalog.get("publicProfiles") or []
    groups = catalog.get("capabilityGroups") or []
    target = catalog.get("targetPhysicalLayout") or {}
    if not modules:
        raise GateError("canonical starter catalog has no modules")
    if not catalog.get("starterAdmissionPolicy", {}).get("failClosed"):
        raise GateError("starter admission policy must be fail-closed")
    if catalog.get("profileMode") != "ACTUAL_PROFILE_ARTIFACT":
        raise GateError(f"invalid profileMode={catalog.get('profileMode')!r}")

    group_ids = [str(item.get("id", "")) for item in groups]
    group_paths = [str(item.get("path", "")) for item in groups]
    unique(group_ids, "capability group id")
    unique(group_paths, "capability group path")
    require_equal("target capabilityGroupCount", int(target.get("capabilityGroupCount", -1)), len(group_ids))
    require_equal("target profileCount", int(target.get("profileCount", -1)), len(public_profiles))

    project_paths = [str(item.get("projectPath", "")) for item in modules]
    owner_paths = [str(item.get("ownerPath", "")).replace("\\", "/") for item in modules]
    artifacts = [str(item.get("artifactId", "")) for item in modules]
    packages = [str(item.get("packageBase", "")) for item in modules]
    config_prefixes = [str(item.get("configPrefix", "")) for item in modules]
    for values, label in [
        (project_paths, "projectPath"), (owner_paths, "ownerPath"),
        (artifacts, "artifactId"), (packages, "packageBase"),
        (config_prefixes, "configPrefix"),
    ]:
        unique(values, label)

    public_modules = [item for item in modules if item.get("visibility") == "public"]
    internal_modules = [item for item in modules if item.get("visibility") == "internal"]
    if len(public_modules) != len(public_profiles):
        raise GateError(f"public module count mismatch profiles={len(public_profiles)} modules={len(public_modules)}")
    public_names_from_modules = sorted(
        item["ownerPath"].replace("\\", "/").split("/")[-1] for item in public_modules
    )
    require_equal("public profile physical names", sorted(public_profiles), public_names_from_modules)
    for module in modules:
        project = str(module.get("projectPath", ""))
        owner = str(module.get("ownerPath", "")).replace("\\", "/")
        if not project.startswith(":cpf-starter-"):
            raise GateError(f"invalid starter projectPath={project}")
        if not owner.startswith("cpf-starters/"):
            raise GateError(f"invalid starter ownerPath={owner}")
        owner_group = str(module.get("ownerGroup", ""))
        internal_role = str(module.get("internalRole", ""))
        if not owner_group or not internal_role:
            raise GateError(f"module lacks exactly-one ownership role: {project}")
        if module.get("visibility") not in {"public", "internal"}:
            raise GateError(f"invalid visibility for {project}: {module.get('visibility')}")
        if module.get("visibility") == "public":
            if module.get("kind") != "starter-profile" or internal_role != "public-profile":
                raise GateError(f"non-profile leaked to public surface: {project}")
        elif module.get("kind") == "starter-profile":
            raise GateError(f"profile marked internal: {project}")
        if not metadata_only:
            directory = root / owner
            if not directory.is_dir():
                raise GateError(f"catalog ownerPath directory missing: {owner}")
            if not (directory / "build.gradle").is_file() and not (directory / "build.gradle.kts").is_file():
                raise GateError(f"catalog module has no Gradle build: {owner}")

    removed = set(catalog.get("removedArtifactIds") or [])
    active = set(artifacts)
    leaked_removed = sorted(removed & active)
    if leaked_removed:
        raise GateError(f"removed artifact remains active={leaked_removed}")

    if "cpf-starter-catalog.json" not in settings:
        raise GateError("settings.gradle does not consume canonical starter catalog")
    for required_snippet in ("include projectPath", "project(projectPath).projectDir = file(ownerPath)"):
        if required_snippet not in settings:
            raise GateError(f"settings.gradle catalog consumer missing: {required_snippet}")

    require_equal("profiles canonicalStarterCatalog", "cpf-starter-catalog.json", profiles.get("canonicalStarterCatalog"))
    require_equal("profiles publicProfiles", public_profiles, profiles.get("publicProfiles"))
    require_equal("profiles capabilityGroups", group_ids, profiles.get("capabilityGroups"))
    for key in ("providerSlots", "standardInheritancePolicy", "approvedExternalExceptionPolicy", "capabilityComposition"):
        require_equal(f"profiles {key}", catalog.get(key), profiles.get(key))
    derivative_public_names = [item.get("publicName") for item in profiles.get("profiles") or []]
    require_equal("profiles definitions", public_profiles, derivative_public_names)
    module_by_artifact = {item["artifactId"]: item for item in modules}
    for profile in profiles.get("profiles") or []:
        aggregate = profile.get("aggregateProject")
        module = module_by_artifact.get(aggregate)
        if not module or module.get("visibility") != "public" or module.get("kind") != "starter-profile":
            raise GateError(f"profile aggregate is not a public profile module: {aggregate}")

    if "findAll { it.visibility?.toString()=='public' }" not in public_bom:
        raise GateError("public BOM does not derive constraints from catalog visibility")
    if "it.kind?.toString()!='starter-profile'" not in public_bom or "it.internalRole?.toString()!='public-profile'" not in public_bom:
        raise GateError("public BOM lacks internal-leaf leak guard")
    internal_artifacts = [item["artifactId"] for item in internal_modules]
    literal_leaks = sorted(
        artifact for artifact in internal_artifacts
        if re.search(rf"['\"](?:[^'\"]*:)?{re.escape(artifact)}(?::[^'\"]*)?['\"]", public_bom)
    )
    if literal_leaks:
        raise GateError(f"internal starter literal leaked into public BOM={literal_leaks[:20]}")

    return {
        "status": "PASS",
        "catalog_id": catalog.get("catalogId"),
        "catalog_baseline_sha": catalog.get("baselineSha"),
        "module_count": len(modules),
        "public_profile_count": len(public_modules),
        "internal_module_count": len(internal_modules),
        "capability_group_count": len(group_ids),
        "provider_slot_count": len(catalog.get("providerSlots") or {}),
        "metadata_only": metadata_only,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--metadata-only", action="store_true")
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()
    try:
        result = validate(args.root.resolve(), args.metadata_only)
        if args.json_output:
            path = args.json_output
            if not path.is_absolute():
                path = args.root.resolve() / path
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except (GateError, OSError, json.JSONDecodeError) as exc:
        print(f"CPF starter catalog truth FAILED: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
