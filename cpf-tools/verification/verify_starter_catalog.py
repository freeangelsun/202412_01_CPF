#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path

STARTER_KINDS = {"starter-profile", "internal-starter"}
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
    mirror_path = root / "cpf-tools/config/cpf-starter-catalog.json"
    release_path = root / "cpf-tools/release/cpf-final-artifact-catalog.json"
    canonical = load_json(canonical_path, errors)
    mirror = load_json(mirror_path, errors)
    release = load_json(release_path, errors)
    if errors:
        print("\n".join(f"FAIL {error}" for error in errors))
        return 1

    if canonical_path.read_bytes() != mirror_path.read_bytes():
        errors.append("generator/config Starter catalogs are not byte-identical")

    modules = canonical.get("modules") or []
    layout = canonical.get("targetPhysicalLayout") or {}
    public_profiles = canonical.get("publicProfiles") or []
    public_modules = [module for module in modules if module.get("visibility") == "public"]
    internal_modules = [module for module in modules if module.get("visibility") == "internal"]

    if layout.get("moduleCount") != len(modules):
        errors.append(f"moduleCount={layout.get('moduleCount')} actual={len(modules)}")
    if layout.get("profileCount") != len(public_modules):
        errors.append(f"profileCount={layout.get('profileCount')} actual={len(public_modules)}")
    if layout.get("capabilityGroupCount") != len(canonical.get("capabilityGroups") or []):
        errors.append("capabilityGroupCount mismatch")
    if set(public_profiles) != {str(module.get("artifactId", "")).removeprefix("cpf-starter-profile-") for module in public_modules}:
        errors.append("publicProfiles does not exactly match public Starter profile artifacts")
    for field in ("projectPath", "ownerPath", "artifactId", "configPrefix"):
        bad = duplicates([norm(module.get(field)) for module in modules])
        if bad:
            errors.append(f"duplicate/blank {field}: {bad}")
    bad_gav = duplicates([f"{module.get('groupId')}:{module.get('artifactId')}" for module in modules])
    if bad_gav:
        errors.append(f"duplicate/blank GAV: {bad_gav}")

    for module in modules:
        visibility = module.get("visibility")
        if visibility == "public":
            if module.get("kind") != "starter-profile" or module.get("ownerGroup") != "profiles" or module.get("internalRole") != "public-profile":
                errors.append(f"invalid public partition: {module.get('artifactId')}")
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

    release_starters = [row for row in release.get("artifacts") or [] if row.get("kind") in STARTER_KINDS]
    release_ids = [norm(row.get("artifactId")) for row in release_starters]
    bad_release_ids = duplicates(release_ids)
    if bad_release_ids:
        errors.append(f"duplicate/blank release Starter artifactId: {bad_release_ids}")
    release_by_id = {norm(row.get("artifactId")): row for row in release_starters}
    active_by_id = {norm(row.get("artifactId")): row for row in modules}
    missing_release = active_ids - set(release_by_id)
    extra_release = set(release_by_id) - active_ids
    drift_release = sorted(
        artifact_id for artifact_id in active_ids & set(release_by_id)
        if any(
            norm(active_by_id[artifact_id].get(field)) != norm(release_by_id[artifact_id].get(field))
            for field in ("ownerPath", "kind", "visibility")
        )
    )
    if missing_release or extra_release or drift_release:
        errors.append(f"canonical/release mismatch missing={sorted(missing_release)} extra={sorted(extra_release)} drift={drift_release}")
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
    build_text = (root / "build.gradle").read_text(encoding="utf-8") if (root / "build.gradle").is_file() else ""
    if "Canonical/release Starter mismatch" not in build_text or "retainedInactiveRoots" not in build_text:
        errors.append("root build lacks canonical/release/retained publication gate")

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
