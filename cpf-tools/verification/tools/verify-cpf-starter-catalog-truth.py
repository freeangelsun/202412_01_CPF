#!/usr/bin/env python3
"""Fail-closed physical starter/catalog/package/publication truth gate."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
from collections import Counter
from pathlib import Path, PurePosixPath


class GateError(RuntimeError):
    pass


def git(root: Path, *args: str) -> str:
    process = subprocess.run(
        ["git", "-C", str(root), *args], capture_output=True, text=True
    )
    if process.returncode:
        raise GateError(
            f"git {' '.join(args)} failed: {process.stderr.strip()}"
        )
    return process.stdout.strip()


def source_identity(root: Path, expected_sha: str | None = None) -> str:
    if expected_sha:
        value = expected_sha.strip().lower()
        if not re.fullmatch(r"[0-9a-f]{40}", value):
            raise GateError("expected source identity must be 40 lowercase hex chars")
        return value
    env = os.environ.get("CPF_SOURCE_SHA", "").strip().lower()
    if re.fullmatch(r"[0-9a-f]{40}", env):
        return env
    h = hashlib.sha1()
    excluded = {".git", ".gradle", "build", "node_modules", ".pytest_cache", "__pycache__", "dist", "coverage"}
    for path in sorted(root.rglob("*")):
        if not path.is_file():
            continue
        rel = path.relative_to(root)
        if any(part in excluded for part in rel.parts):
            continue
        rel_text = rel.as_posix().encode("utf-8")
        h.update(len(rel_text).to_bytes(4, "big")); h.update(rel_text)
        data = path.read_bytes()
        h.update(len(data).to_bytes(8, "big")); h.update(data)
    return h.hexdigest()


def declared_settings_projects(root: Path) -> set[str]:
    settings = root / "settings.gradle"
    if not settings.is_file():
        return set()
    text = settings.read_text(encoding="utf-8", errors="replace")
    projects: set[str] = set()
    for line in text.splitlines():
        if not line.lstrip().startswith("include"):
            continue
        for value in re.findall(r"""['"](:[^'"]+)['"]""", line):
            projects.add(value)
            parts = value.strip(":").split(":")
            projects.update(":" + ":".join(parts[:i]) for i in range(1, len(parts)))
    return projects


def duplicates(values: list[str]) -> list[str]:
    return sorted(value for value, count in Counter(values).items() if count > 1)


def normalized_owner_path(raw: object) -> str | None:
    if not isinstance(raw, str) or not raw.strip():
        return None
    value = raw.strip().replace("\\", "/")
    path = PurePosixPath(value)
    if path.is_absolute() or ".." in path.parts or value != path.as_posix():
        return None
    return value


def verify(root: Path, expected_sha: str | None = None, require_clean: bool = False) -> dict:
    head = source_identity(root, expected_sha)
    status: str | None = None
    if require_clean:
        status = git(root, "status", "--porcelain")
        if status:
            raise GateError("working tree is not clean")

    catalog_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    if not catalog_path.is_file():
        raise GateError(f"catalog missing: {catalog_path.relative_to(root)}")
    catalog = json.loads(catalog_path.read_text(encoding="utf-8-sig"))
    findings: list[str] = []

    required_top_level = (
        "catalogId",
        "publicProfiles",
        "capabilityGroups",
        "modules",
        "providerSlots",
        "removedArtifactIds",
        "starterAdmissionPolicy",
        "profileDefinitions",
    )
    for field in required_top_level:
        if field not in catalog:
            findings.append(f"catalog required field missing: {field}")

    if catalog.get("baselinePolicy") != "GIT_HEAD_RUNTIME" or catalog.get("baselineSha") != "RUNTIME_GIT_HEAD":
        findings.append(
            "catalog must use runtime source identity policy; stale static baseline forbidden"
        )

    modules = catalog.get("modules") or []
    if not isinstance(modules, list) or not modules:
        findings.append("catalog module list is empty")
        modules = []

    artifact_ids: list[str] = []
    owner_paths: list[str] = []
    package_bases: list[str] = []
    checked: list[str] = []
    module_by_artifact: dict[str, dict] = {}

    for index, module in enumerate(modules):
        if not isinstance(module, dict):
            findings.append(f"module[{index}] invalid")
            continue

        artifact = module.get("artifactId") or module.get("id")
        owner = normalized_owner_path(
            module.get("ownerPath") or module.get("physicalPath") or module.get("path")
        )
        package = module.get("packageBase") or module.get("basePackage")
        visibility = module.get("visibility")
        role = module.get("role")
        kind = module.get("kind")
        project_path = module.get("projectPath")

        if not isinstance(artifact, str) or not artifact.strip():
            findings.append(f"module[{index}] artifactId missing")
        else:
            artifact = artifact.strip()
            artifact_ids.append(artifact)
            module_by_artifact[artifact] = module

        if not owner:
            findings.append(f"module[{index}] ownerPath invalid or missing")
            continue
        owner_paths.append(owner)
        if not owner.startswith("cpf-starters/"):
            findings.append(f"{owner}: ownerPath must remain under cpf-starters/")

        if not isinstance(package, str) or not re.fullmatch(r"[a-zA-Z_]\w*(?:\.[a-zA-Z_]\w*)*", package):
            findings.append(f"{owner}: packageBase invalid or missing")
            package = None
        else:
            package_bases.append(package)

        if visibility not in {"public", "internal"}:
            findings.append(f"{owner}: visibility must be public or internal")
        if not isinstance(role, str) or not role.strip():
            findings.append(f"{owner}: role missing")
        if isinstance(kind, str) and kind:
            public_kinds = {"starter-base", "starter-common", "starter-profile", "starter-provider"}
            if visibility == "public" and kind not in public_kinds:
                findings.append(f"{owner}: invalid public starter kind {kind}")
            if visibility == "internal" and kind != "internal-starter":
                findings.append(f"{owner}: internal module must use internal-starter kind")
        else:
            if visibility == "public" and role != "profile":
                findings.append(f"{owner}: legacy public fixture must use profile role")
            if visibility == "internal" and role == "profile":
                findings.append(f"{owner}: profile modules must be public")
        if project_path is not None and (not isinstance(project_path, str) or not re.fullmatch(r":(?:starters|internal|framework)(?::[a-z0-9][a-z0-9-]*)+", project_path)):
            findings.append(f"{owner}: invalid logical projectPath {project_path!r}")

        directory = root / owner
        if not directory.is_dir():
            findings.append(f"{owner}: physical module missing")
            continue
        if not (directory / "build.gradle").is_file() and not (directory / "build.gradle.kts").is_file():
            findings.append(f"{owner}: build file missing")

        java_files = sorted(directory.rglob("src/main/java/**/*.java"))
        declarations: list[str] = []
        for java_file in java_files:
            match = re.search(
                r"(?m)^\s*package\s+([\w.]+)\s*;",
                java_file.read_text(encoding="utf-8-sig", errors="replace"),
            )
            if match:
                declarations.append(match.group(1))
        if package:
            # Public profile modules are dependency-only aggregators by design and may
            # legitimately have no main Java source. Internal leaf/provider modules
            # must own an implementation package so catalog metadata cannot mask an
            # empty physical module. A profile that does contain Java must still match
            # its declared package base.
            if not declarations and role != "profile" and module.get("sourceOwnership") != "assembly-only":
                findings.append(
                    f"{owner}: packageBase={package} has no physical main Java package declaration"
                )
            elif declarations and not any(
                declaration == package or declaration.startswith(package + ".")
                for declaration in declarations
            ):
                findings.append(
                    f"{owner}: packageBase={package} has no matching physical package declaration"
                )

        if owner == "cpf-starters/data/persistence/jdbc":
            imports_file = (
                directory
                / "src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
            )
            if not imports_file.is_file():
                findings.append(f"{owner}: AutoConfiguration.imports missing")
            else:
                imports_text = imports_file.read_text(encoding="utf-8-sig")
                if "com.cpf.starter.persistence.jdbc" in imports_text:
                    findings.append(
                        f"{owner}: legacy package remains in AutoConfiguration.imports"
                    )
                for fqcn in [
                    line.strip()
                    for line in imports_text.splitlines()
                    if line.strip() and not line.strip().startswith("#")
                ]:
                    if fqcn.startswith("com.cpf.starter.data.persistence.jdbc"):
                        target = (
                            directory
                            / "src/main/java"
                            / Path(*fqcn.split(".")).with_suffix(".java")
                        )
                        if not target.is_file():
                            findings.append(
                                f"{owner}: auto-configuration target missing {fqcn}"
                            )
            for java_file in directory.rglob("src/main/java/**/*.java"):
                if "package com.cpf.starter.persistence.jdbc" in java_file.read_text(
                    encoding="utf-8-sig", errors="replace"
                ):
                    findings.append(
                        f"{java_file.relative_to(root)}: legacy persistence package remains"
                    )
        checked.append(owner)

    for duplicate in duplicates(artifact_ids):
        findings.append(f"duplicate artifactId: {duplicate}")
    for duplicate in duplicates(owner_paths):
        findings.append(f"duplicate ownerPath: {duplicate}")
    for duplicate in duplicates(package_bases):
        findings.append(f"duplicate packageBase: {duplicate}")

    removed = catalog.get("removedArtifactIds") or []
    if not isinstance(removed, list):
        findings.append("removedArtifactIds must be a list")
        removed = []
    active_removed_overlap = sorted(set(artifact_ids).intersection(str(x) for x in removed))
    for artifact in active_removed_overlap:
        findings.append(f"active artifact also listed as removed: {artifact}")

    public_profiles = catalog.get("publicProfiles") or []
    profile_definitions = catalog.get("profileDefinitions") or {}
    if not isinstance(public_profiles, list) or not public_profiles:
        findings.append("publicProfiles must be a non-empty list")
        public_profiles = []
    if not isinstance(profile_definitions, dict):
        findings.append("profileDefinitions must be an object")
        profile_definitions = {}

    declared_profile_map = {
        str(module.get("profileId")): artifact
        for artifact, module in module_by_artifact.items()
        if module.get("visibility") == "public" and module.get("role") == "profile" and module.get("profileId")
    }
    if declared_profile_map:
        if set(declared_profile_map) != set(str(x) for x in public_profiles):
            findings.append(
                "public profile module closure mismatch "
                f"profiles={sorted(str(x) for x in public_profiles)} actual={sorted(declared_profile_map)}"
            )
        for profile, definition in profile_definitions.items():
            if isinstance(definition, dict) and profile in declared_profile_map:
                if definition.get("artifactId") != declared_profile_map[profile]:
                    findings.append(
                        f"profileDefinitions.{profile}: artifactId mismatch expected={declared_profile_map[profile]} actual={definition.get('artifactId')}"
                    )
    else:
        declared_public_profile_artifacts = {
            artifact for artifact, module in module_by_artifact.items()
            if module.get("visibility") == "public" and module.get("role") == "profile"
        }
        expected_public_profile_artifacts = {
            f"cpf-starter-profile-{profile}" for profile in public_profiles if isinstance(profile, str)
        }
        if declared_public_profile_artifacts != expected_public_profile_artifacts:
            findings.append(
                "public profile module closure mismatch "
                f"expected={sorted(expected_public_profile_artifacts)} actual={sorted(declared_public_profile_artifacts)}"
            )
    if set(profile_definitions) != set(public_profiles):
        findings.append(
            "profileDefinitions/publicProfiles key mismatch "
            f"definitions={sorted(profile_definitions)} profiles={sorted(str(x) for x in public_profiles)}"
        )

    capability_groups = catalog.get("capabilityGroups") or []
    group_ids: set[str] = set()
    if not isinstance(capability_groups, list) or not capability_groups:
        findings.append("capabilityGroups must be a non-empty list")
        capability_groups = []
    for index, group in enumerate(capability_groups):
        if not isinstance(group, dict):
            findings.append(f"capabilityGroup[{index}] invalid")
            continue
        group_id = group.get("id")
        group_path = normalized_owner_path(group.get("path"))
        if not isinstance(group_id, str) or not group_id.strip():
            findings.append(f"capabilityGroup[{index}] id missing")
        else:
            group_ids.add(group_id)
        if not group_path or not group_path.startswith("cpf-starters/"):
            findings.append(f"capabilityGroup[{index}] path invalid")
        elif not (root / group_path).is_dir():
            findings.append(f"{group_path}: capability group physical path missing")
    for duplicate in duplicates(
        [str(group.get("id")) for group in capability_groups if isinstance(group, dict)]
    ):
        findings.append(f"duplicate capability group id: {duplicate}")

    project_to_module: dict[str, dict] = {}
    active_projects: set[str] = set(declared_settings_projects(root))
    for artifact, module in module_by_artifact.items():
        project = module.get("projectPath")
        if not isinstance(project, str) or not project:
            project = ":" + artifact
        active_projects.add(project)
        project_to_module[project] = module

    for profile, definition in profile_definitions.items():
        if not isinstance(definition, dict):
            findings.append(f"profileDefinitions.{profile} invalid")
            continue
        for project in (definition.get("runtimeProjects") or []) + (definition.get("apiProjects") or []):
            if project not in active_projects:
                findings.append(f"profileDefinitions.{profile}: unknown project {project}")
        for capability in definition.get("requiredCapabilities") or []:
            if capability not in group_ids:
                findings.append(f"profileDefinitions.{profile}: unknown capability {capability}")

    provider_slots = catalog.get("providerSlots") or {}
    if not isinstance(provider_slots, dict) or not provider_slots:
        findings.append("providerSlots must be a non-empty object")
        provider_slots = {}
    for slot, providers in provider_slots.items():
        if not isinstance(providers, dict) or not providers:
            findings.append(f"providerSlots.{slot} must be a non-empty object")
            continue
        for provider, binding in providers.items():
            if not isinstance(binding, dict):
                findings.append(f"providerSlots.{slot}.{provider} invalid")
                continue
            project = binding.get("projectPath")
            coordinate = binding.get("coordinate")
            module = project_to_module.get(project)
            if module is None:
                findings.append(f"providerSlots.{slot}.{provider}: unknown projectPath {project}")
                continue
            expected_coordinate = f"{module.get('groupId','com.cpf.starter')}:{module.get('artifactId')}"
            if coordinate != expected_coordinate:
                findings.append(
                    f"providerSlots.{slot}.{provider}: coordinate mismatch expected={expected_coordinate} actual={coordinate}"
                )

    admission = catalog.get("starterAdmissionPolicy")
    if not isinstance(admission, dict) or admission.get("failClosed") is not True:
        findings.append("starterAdmissionPolicy.failClosed must be true")

    result = {
        "status": "PASS" if not findings else "FAIL",
        "verifiedAgainstSha": head,
        "workingTreeClean": (not bool(status)) if status is not None else None,
        "catalogRevision": catalog.get("catalogRevision"),
        "moduleCount": len(modules),
        "checkedOwnerPaths": checked,
        "findings": findings,
    }
    if findings:
        raise GateError(json.dumps(result, ensure_ascii=False, indent=2))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--expected-sha")
    parser.add_argument("--require-clean", action="store_true")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        result = verify(root, args.expected_sha, args.require_clean)
        code = 0
    except Exception as failure:
        try:
            result = json.loads(str(failure))
        except (json.JSONDecodeError, TypeError):
            result = {"status": "FAIL", "message": str(failure)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(
            json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
