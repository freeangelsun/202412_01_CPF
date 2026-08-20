#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path

STARTER_KINDS = {"starter-base", "starter-common", "starter-provider", "starter-profile", "internal-starter"}
RETAINED_REQUIRED = {
    "path", "artifactId", "replacementArtifactId", "replacementOwnerPath",
    "status", "active", "includeInSettings", "publishable",
    "consumerAllowed", "approvalRequired",
}


def norm(value: object) -> str:
    return str(value or "").replace("\\", "/").rstrip("/")


def duplicates(values: list[str]) -> list[str]:
    counts = Counter(values)
    return sorted(value for value, count in counts.items() if not value or count > 1)


def load_json(path: Path, errors: list[str]) -> dict:
    if not path.is_file():
        errors.append(f"missing file: {path}")
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # fail closed on syntax/encoding drift
        errors.append(f"invalid JSON {path}: {exc}")
        return {}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--overlay-only", action="store_true",
                        help="Validate changed payload only; returns 3 because physical closure was not executed.")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    errors: list[str] = []

    canonical_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    generator_config_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    release_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    canonical = load_json(canonical_path, errors)
    generator_config = load_json(generator_config_path, errors)
    release = load_json(release_path, errors)
    if errors:
        print("\n".join(f"FAIL {error}" for error in errors))
        return 1

    modules = canonical.get("modules") or []
    module_by_artifact = {norm(module.get("artifactId")): module for module in modules}
    layout = canonical.get("targetPhysicalLayout") or {}
    public_profiles = canonical.get("publicProfiles") or []
    public_modules = [module for module in modules if module.get("visibility") == "public"]
    internal_modules = [module for module in modules if module.get("visibility") == "internal"]

    if layout.get("moduleCount") != len(modules):
        errors.append(f"moduleCount={layout.get('moduleCount')} actual={len(modules)}")
    profile_modules = [module for module in modules if module.get("kind") == "starter-profile"]
    if layout.get("profileCount") != len(profile_modules):
        errors.append(f"profileCount={layout.get('profileCount')} actual={len(profile_modules)}")
    if "capabilityGroupCount" in layout and layout.get("capabilityGroupCount") != len(canonical.get("capabilityGroups") or []):
        errors.append("capabilityGroupCount mismatch")
    if set(public_profiles) != {str(module.get("artifactId", "")).removeprefix("cpf-starter-") for module in profile_modules}:
        errors.append("publicProfiles does not exactly match public Starter profile artifacts")

    # The canonical Profile composition, the Generator input, and the physical
    # Gradle aggregate must describe one exact project-dependency set.  This is
    # deliberately catalog-driven so retired aliases cannot be reintroduced in
    # a profile build while still passing physical module closure.
    profile_definitions = canonical.get("profileDefinitions") or {}
    generator_profiles = generator_config.get("profiles") or {}
    if not isinstance(profile_definitions, dict):
        errors.append("profileDefinitions must be an object")
        profile_definitions = {}
    if generator_profiles != profile_definitions:
        errors.append("Generator application profiles differ from canonical profileDefinitions")
    if release.get("profileDefinitions") != profile_definitions:
        errors.append("Release artifact profiles differ from canonical profileDefinitions")
    if set(profile_definitions) != set(public_profiles):
        errors.append(
            "profileDefinitions/publicProfiles mismatch "
            f"definitions={sorted(profile_definitions)} profiles={sorted(public_profiles)}"
        )
    project_dependency = re.compile(
        r"^\s*(api|implementation|runtimeOnly|compileOnly)\s+project\(\s*['\"]([^'\"]+)['\"]\s*\)",
        re.MULTILINE,
    )
    for profile_name, definition in profile_definitions.items():
        if not isinstance(definition, dict):
            errors.append(f"invalid profile definition: {profile_name}")
            continue
        artifact_id = norm(definition.get("artifactId"))
        profile_module = module_by_artifact.get(artifact_id)
        if not profile_module or profile_module.get("kind") != "starter-profile":
            errors.append(f"profileDefinition has no active profile module: {profile_name}={artifact_id}")
            continue
        expected_projects = [norm(value) for value in definition.get("runtimeProjects") or []]
        duplicate_expected = duplicates(expected_projects)
        if duplicate_expected:
            errors.append(f"profileDefinition duplicate/blank runtimeProjects {profile_name}: {duplicate_expected}")
        if "apiProjects" not in definition or not isinstance(definition.get("apiProjects"), list):
            errors.append(f"profileDefinition apiProjects must be an explicit list: {profile_name}")
            api_projects: list[str] = []
        else:
            api_projects = [norm(value) for value in definition.get("apiProjects") or []]
        duplicate_api = duplicates(api_projects)
        if duplicate_api:
            errors.append(f"profileDefinition duplicate/blank apiProjects {profile_name}: {duplicate_api}")
        api_outside_runtime = sorted(set(api_projects) - set(expected_projects))
        if api_outside_runtime:
            errors.append(
                f"profileDefinition apiProjects must be a runtimeProjects subset {profile_name}: "
                f"extra={api_outside_runtime}"
            )
        build_relative = f"{norm(profile_module.get('ownerPath'))}/build.gradle"
        build_path = root / build_relative
        if not build_path.is_file():
            errors.append(f"profile build missing: {build_relative}")
            continue
        dependencies = project_dependency.findall(build_path.read_text(encoding="utf-8"))
        actual_projects = [norm(project) for _, project in dependencies]
        duplicate_actual = duplicates(actual_projects)
        if duplicate_actual:
            errors.append(f"profile build duplicate/blank project dependencies {profile_name}: {duplicate_actual}")
        missing = sorted(set(expected_projects) - set(actual_projects))
        extra = sorted(set(actual_projects) - set(expected_projects))
        if missing or extra:
            errors.append(f"profile build/catalog dependency mismatch {profile_name}: missing={missing} extra={extra}")
        for configuration, project in dependencies:
            normalized_project = norm(project)
            expected_configuration = "api" if normalized_project in api_projects else "implementation"
            if configuration != expected_configuration:
                errors.append(
                    f"profile dependency API contract mismatch {profile_name}: "
                    f"{project} uses {configuration}, expected {expected_configuration}"
                )
    for field in ("projectPath", "ownerPath", "artifactId", "configPrefix"):
        bad = duplicates([norm(module.get(field)) for module in modules])
        if bad:
            errors.append(f"duplicate/blank {field}: {bad}")
    bad_gav = duplicates([f"{module.get('groupId')}:{module.get('artifactId')}" for module in modules])
    if bad_gav:
        errors.append(f"duplicate/blank GAV: {bad_gav}")

    for module in modules:
        artifact_id = module.get("artifactId")
        for field in ("displayNameKo", "selectionGroup", "usageHintKo"):
            if not str(module.get(field) or "").strip():
                errors.append(f"starter UX metadata missing {field}: {artifact_id}")
        if not isinstance(module.get("userSelectable"), bool):
            errors.append(f"starter UX metadata userSelectable must be boolean: {artifact_id}")
        if not isinstance(module.get("runtimeRequired"), bool):
            errors.append(f"starter UX metadata runtimeRequired must be boolean: {artifact_id}")
        usage_level = module.get("usageLevel")
        if usage_level not in {"golden", "capability", "advanced", "internal"}:
            errors.append(f"starter UX metadata invalid usageLevel: {artifact_id}={usage_level}")
        if not isinstance(module.get("recommended"), bool):
            errors.append(f"starter UX metadata recommended must be boolean: {artifact_id}")
        visibility = module.get("visibility")
        if visibility == "internal" and usage_level != "internal":
            errors.append(f"internal Starter must be usageLevel=internal: {artifact_id}")
        if visibility == "public" and usage_level == "internal":
            errors.append(f"public Starter cannot be usageLevel=internal: {artifact_id}")
        if visibility == "public" and module.get("userSelectable") is not True:
            errors.append(f"public Starter must be userSelectable=true: {artifact_id}")
        if visibility == "internal" and module.get("userSelectable") is not False:
            errors.append(f"internal Starter must be userSelectable=false: {artifact_id}")
        if visibility == "public":
            allowed_public = {
                "starter-base": "public-base",
                "starter-common": "public-common",
                "starter-provider": "public-provider",
                "starter-profile": "public-profile",
            }
            expected_role = allowed_public.get(module.get("kind"))
            if expected_role is None or module.get("internalRole") != expected_role:
                errors.append(f"invalid public partition: {module.get('artifactId')}")
            if module.get("kind") == "starter-profile" and module.get("ownerGroup") != "profiles":
                errors.append(f"invalid public profile owner: {module.get('artifactId')}")
        elif visibility == "internal":
            if module.get("kind") != "internal-starter":
                errors.append(f"invalid internal partition: {module.get('artifactId')}")
        else:
            errors.append(f"invalid visibility: {module.get('artifactId')}={visibility}")

    active_ids = {norm(module.get("artifactId")) for module in modules}
    active_paths = {norm(module.get("ownerPath")) for module in modules}
    removed_ids = {norm(value) for value in canonical.get("removedArtifactIds") or []}
    removed_roots = {norm(value) for value in canonical.get("removedRepositoryRoots") or []}
    retained = canonical.get("retainedInactiveRoots") or []
    retained_paths: set[str] = set()
    retained_ids: set[str] = set()
    for row in retained:
        missing = sorted(RETAINED_REQUIRED - set(row))
        if missing:
            errors.append(f"retained fields missing {missing}: {row}")
            continue
        path, artifact_id = norm(row.get("path")), norm(row.get("artifactId"))
        if not path.startswith("cpf-starters/") or not artifact_id.startswith("cpf-starter-"):
            errors.append(f"invalid retained identity: {row}")
        if path in retained_paths or artifact_id in retained_ids:
            errors.append(f"duplicate retained identity: {row}")
        retained_paths.add(path); retained_ids.add(artifact_id)
        expected_flags = {
            "status": "PENDING_USER_DELETE_APPROVAL", "active": False,
            "includeInSettings": False, "publishable": False,
            "consumerAllowed": False, "approvalRequired": True,
        }
        if any(row.get(key) != value for key, value in expected_flags.items()):
            errors.append(f"retained contract is not fail-closed: {row}")
        if norm(row.get("replacementArtifactId")) not in active_ids:
            errors.append(f"retained replacement artifact inactive: {row.get('replacementArtifactId')}")
        if norm(row.get("replacementOwnerPath")) not in active_paths:
            errors.append(f"retained replacement owner inactive: {row.get('replacementOwnerPath')}")
    for label, overlap in (
        ("active/retained artifact", active_ids & retained_ids),
        ("active/retained root", active_paths & retained_paths),
        ("active/removed artifact", active_ids & removed_ids),
        ("active/removed root", active_paths & removed_roots),
        ("retained/removed root", retained_paths & removed_roots),
    ):
        if overlap:
            errors.append(f"{label} collision: {sorted(overlap)}")

    release_rows = release.get("artifacts") or []
    release_by_id = {norm(row.get("artifactId")): row for row in release_rows if row.get("artifactId")}
    publishable = {norm(row.get("artifactId")): row for row in modules if row.get("publicationRequired") is not False}
    missing_release = set(publishable) - set(release_by_id)
    drift_release = sorted(
        artifact_id for artifact_id in set(publishable) & set(release_by_id)
        if any(
            norm(publishable[artifact_id].get(field)) != norm(release_by_id[artifact_id].get(field))
            for field in ("ownerPath", "kind", "visibility")
        )
    )
    if missing_release or drift_release:
        errors.append(f"canonical/release mismatch missing={sorted(missing_release)} drift={drift_release}")
    if release.get("retainedInactiveRoots") != retained:
        errors.append("release retainedInactiveRoots differs from canonical")
    if set(release.get("removedArtifactIds") or []) != set(canonical.get("removedArtifactIds") or []):
        errors.append("release removedArtifactIds differs from canonical")
    if set(release.get("removedRepositoryRoots") or []) != set(canonical.get("removedRepositoryRoots") or []):
        errors.append("release removedRepositoryRoots differs from canonical")

    # BOM scripts must execute exact-set comparison and must not assert fixed numeric counts.
    for relative, label in (
        ("cpf-tools/build/platform-bom/internal-bom/build.gradle", "internal BOM"),
        ("cpf-tools/build/platform-bom/public-bom/build.gradle", "public BOM"),
    ):
        path = root / relative
        if not path.is_file():
            errors.append(f"missing {label}: {relative}")
            continue
        text = path.read_text(encoding="utf-8")
        if re.search(r"(?:internalModules|publicModules)\.size\(\)\s*[!=]=\s*\d+", text):
            errors.append(f"{label} contains a fixed-count assertion")
        for marker in ("missing", "extra", "duplicate"):
            if marker not in text:
                errors.append(f"{label} lacks {marker} exact-set diagnostic")

    settings_text = (root / "settings.gradle").read_text(encoding="utf-8") if (root / "settings.gradle").is_file() else ""
    if "Starter physical closure mismatch" not in settings_text or "retainedInactiveRoots" not in settings_text:
        errors.append("settings.gradle lacks active/retained/removed physical closure gate")
    if "cpf-tools/generator/contracts/cpf-starter-catalog.json" not in settings_text:
        errors.append("settings.gradle does not load the canonical Starter Catalog")
    if "cpf-tools/generator/config/application-starters.yml" in settings_text:
        errors.append("settings.gradle loads Generator application config instead of the canonical Starter Catalog")
    build_text = (root / "build.gradle").read_text(encoding="utf-8") if (root / "build.gradle").is_file() else ""
    convention = root / "cpf-tools/build/cpf-root-conventions.gradle"
    if convention.is_file():
        build_text += "\n" + convention.read_text(encoding="utf-8", errors="ignore")
    if not all(token in build_text for token in ("publicationGate", "starterCatalogGate", "verify_starter_catalog.py")):
        errors.append("root build/convention lacks canonical/release publication closure gate")

    if not args.overlay_only:
        physical = {
            path.parent.relative_to(root).as_posix()
            for path in (root / "cpf-starters").rglob("build.gradle")
            if "build" not in path.parent.relative_to(root / "cpf-starters").parts
        }
        expected_physical = active_paths | retained_paths
        missing_physical = expected_physical - physical
        extra_physical = physical - expected_physical
        if missing_physical or extra_physical:
            errors.append(f"physical closure mismatch missing={sorted(missing_physical)} extra={sorted(extra_physical)} retained={sorted(retained_paths)}")
        present_removed = sorted(path for path in removed_roots if (root / path).exists())
        if present_removed:
            errors.append(f"removed repository roots still exist: {present_removed}")
        missing_retained = sorted(path for path in retained_paths if not (root / path / "build.gradle").is_file())
        if missing_retained:
            errors.append(f"retained roots must exist until deletion approval: {missing_retained}")

    if errors:
        print("\n".join(f"FAIL {error}" for error in errors))
        return 1

    summary = f"modules={len(modules)} public={len(public_modules)} internal={len(internal_modules)} retained={len(retained)}"
    if args.overlay_only:
        print(f"PARTIAL {summary}; physical repository closure NOT_EXECUTED")
        return 3
    print(f"PASS {summary}; physical repository closure executed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
