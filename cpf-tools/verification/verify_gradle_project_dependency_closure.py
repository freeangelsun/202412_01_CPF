#!/usr/bin/env python3
"""Fail closed when a Gradle script references an undeclared project path."""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


QUOTED_VALUE = re.compile(r"['\"]([^'\"]+)['\"]")
PROJECT_REFERENCE = re.compile(
    r"\bproject\s*\(\s*(?:path\s*:\s*)?['\"](:[^'\"]+)['\"]\s*\)"
)
PROJECT_DIR_ASSIGNMENT = re.compile(
    r"project\s*\(\s*['\"](:[^'\"]+)['\"]\s*\)\s*\.projectDir\s*=\s*file\s*\(\s*['\"]([^'\"]+)['\"]\s*\)"
)
GROUP_ASSIGNMENT = re.compile(r"(?m)^\s*group\s*=\s*[\'\"]([^\'\"]+)[\'\"]")


def production_project_targets(text: str) -> list[str]:
    targets: list[str] = []
    for line in text.splitlines():
        match = re.search(r"^\s*([A-Za-z_][\w]*)\s+project\s*\(\s*(?:path\s*:\s*)?[\"'](:[^\"']+)[\"']\s*\)", line)
        if not match:
            continue
        configuration, target = match.groups()
        if configuration.lower().startswith("test") or "test" in configuration.lower():
            continue
        targets.append(target)
    return targets


def normalized_project_path(value: str) -> str:
    value = value.strip()
    return value if value.startswith(":") else f":{value}"


def project_group(text: str) -> str | None:
    match = GROUP_ASSIGNMENT.search(text)
    return match.group(1).strip() if match else None


def same_component_identity_violation(source_project: str, source_group: str | None, target_project: str, target_group: str | None) -> str | None:
    """Detect Gradle project dependencies that collapse to the same group+leaf component identity.

    Gradle/Spring dependency-management can resolve a project dependency as a self component when two
    distinct logical projects share the same Maven group and leaf project name (for example jdbc -> jdbc).
    Published artifactId may be unique, but the project dependency component identity is still ambiguous.
    """
    if not source_group or source_group != target_group or source_project == target_project:
        return None
    source_leaf = source_project.rsplit(":", 1)[-1]
    target_leaf = target_project.rsplit(":", 1)[-1]
    if source_leaf == target_leaf:
        return f"{source_project} -> {target_project} shares Gradle component identity {source_group}:{source_leaf}"
    return None


def include_declarations(text: str) -> list[str]:
    """Return include declarations, including deterministic multiline parenthesized forms."""
    declarations: list[str] = []
    lines = text.splitlines()
    index = 0
    while index < len(lines):
        stripped = lines[index].strip()
        if not re.match(r"^include(?:\s|\()", stripped):
            index += 1
            continue
        declaration = stripped
        balance = declaration.count("(") - declaration.count(")")
        while balance > 0 and index + 1 < len(lines):
            index += 1
            continuation = lines[index].strip()
            declaration += "\n" + continuation
            balance += continuation.count("(") - continuation.count(")")
        declarations.append(declaration)
        index += 1
    return declarations


def declared_projects(settings_path: Path) -> set[str]:
    projects: set[str] = set()
    text = settings_path.read_text(encoding="utf-8")
    for declaration in include_declarations(text):
        for value in QUOTED_VALUE.findall(declaration):
            if value and not value.startswith("cpf-tools/"):
                projects.add(normalized_project_path(value))
    # Gradle creates parent projects implicitly for a nested include.
    for project in tuple(projects):
        segments = project.strip(":").split(":")
        projects.update(":" + ":".join(segments[:index]) for index in range(1, len(segments)))
    return projects


def aggregate_boundary_violation(
    source_project: str,
    source_kind: str,
    target_project: str,
    target_kind: str,
    internal_provider: bool,
) -> str | None:
    if target_project == ":starters:base" and source_kind != "starter-profile":
        return f"{source_project} may not reverse-depend on aggregate :cpf-starter"
    if target_kind == "starter-profile" and internal_provider and source_kind != "starter-profile":
        return f"internal provider {source_project} may not depend on public profile {target_project}"
    return None


def owning_build(script: Path, build_roots: list[Path]) -> Path:
    candidates = [candidate for candidate in build_roots if script.is_relative_to(candidate)]
    return max(candidates, key=lambda candidate: len(candidate.parts))


def strongly_connected_components(graph: dict[str, set[str]]) -> list[list[str]]:
    """Tarjan SCC, kept local so the Gate has no optional Python dependency."""
    index = 0
    indexes: dict[str, int] = {}
    lowlinks: dict[str, int] = {}
    stack: list[str] = []
    on_stack: set[str] = set()
    result: list[list[str]] = []

    def visit(node: str) -> None:
        nonlocal index
        indexes[node] = lowlinks[node] = index
        index += 1
        stack.append(node)
        on_stack.add(node)
        for target in sorted(graph.get(node, set())):
            if target not in indexes:
                visit(target)
                lowlinks[node] = min(lowlinks[node], lowlinks[target])
            elif target in on_stack:
                lowlinks[node] = min(lowlinks[node], indexes[target])
        if lowlinks[node] == indexes[node]:
            component: list[str] = []
            while True:
                current = stack.pop()
                on_stack.remove(current)
                component.append(current)
                if current == node:
                    break
            result.append(sorted(component))

    for node in sorted(graph):
        if node not in indexes:
            visit(node)
    return result


def project_build_scripts(
    build_root: Path, projects: set[str], extra_directories: dict[str, Path] | None = None
) -> dict[str, Path]:
    settings_text = (build_root / "settings.gradle").read_text(encoding="utf-8")
    explicit = {
        normalized_project_path(project): (build_root / relative).resolve()
        for project, relative in PROJECT_DIR_ASSIGNMENT.findall(settings_text)
    }
    explicit.update(extra_directories or {})
    scripts: dict[str, Path] = {}
    for project in projects:
        project_dir = explicit.get(project, build_root.joinpath(*project.strip(":").split(":")))
        script = project_dir / "build.gradle"
        if script.is_file():
            scripts[project] = script
    return scripts


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    settings_files = sorted(
        path for path in root.rglob("settings.gradle")
        if ".gradle" not in path.relative_to(root).parts
    )
    if root / "settings.gradle" not in settings_files:
        print("FAIL root settings.gradle is missing")
        return 1
    build_roots = [path.parent for path in settings_files]
    projects_by_root = {path.parent: declared_projects(path) for path in settings_files}

    catalog_path = root / "cpf-tools/generator/contracts/cpf-starter-catalog.json"
    try:
        catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"FAIL invalid canonical Starter Catalog: {exc}")
        return 1
    catalog_project_directories = {
        normalized_project_path(str(module.get("projectPath", ""))):
            (root / str(module.get("ownerPath", ""))).resolve()
        for module in catalog.get("modules") or []
        if module.get("projectPath") and module.get("ownerPath")
    }
    catalog_modules_by_project = {
        normalized_project_path(str(module.get("projectPath", ""))): module
        for module in catalog.get("modules") or []
        if module.get("projectPath")
    }
    projects_by_root[root].update(
        normalized_project_path(str(module.get("projectPath", "")))
        for module in catalog.get("modules") or []
        if module.get("projectPath")
    )

    findings: list[str] = []
    graph_by_root: dict[Path, dict[str, set[str]]] = {}
    for build_root, projects in projects_by_root.items():
        scripts_by_project = project_build_scripts(
            build_root, projects, catalog_project_directories if build_root == root else None
        )
        graph = {project: set() for project in projects}
        groups_by_project = {
            project: project_group(script.read_text(encoding="utf-8"))
            for project, script in scripts_by_project.items()
        }
        for project, script in scripts_by_project.items():
            text = script.read_text(encoding="utf-8")
            targets = PROJECT_REFERENCE.findall(text)
            production_targets = production_project_targets(text)
            graph[project].update(production_targets)
            for target in production_targets:
                violation = same_component_identity_violation(
                    project, groups_by_project.get(project), target, groups_by_project.get(target)
                )
                if violation:
                    findings.append(f"{script.relative_to(root).as_posix()}: {violation}")
            if build_root == root:
                source_kind = str(catalog_modules_by_project.get(project, {}).get("kind", ""))
                relative = script.relative_to(root).as_posix()
                internal_provider = relative.startswith(("cpf-starters/", "cpf-batch/", "cpf-tools/"))
                for target in targets:
                    target_kind = str(catalog_modules_by_project.get(target, {}).get("kind", ""))
                    violation = aggregate_boundary_violation(
                        project, source_kind, target, target_kind, internal_provider
                    )
                    if violation:
                        findings.append(f"{relative}: {violation}")
        graph_by_root[build_root] = graph

    checked_references = 0
    scripts = sorted(
        path for path in root.rglob("*.gradle")
        if ".gradle" not in path.relative_to(root).parts and path.name != "settings.gradle"
    )
    for script in scripts:
        build_root = owning_build(script, build_roots)
        declared = projects_by_root[build_root]
        text = script.read_text(encoding="utf-8")
        for project in PROJECT_REFERENCE.findall(text):
            checked_references += 1
            if project not in declared:
                relative = script.relative_to(root).as_posix()
                owner = build_root.relative_to(root).as_posix() or "."
                findings.append(f"{relative}: {project} is not declared by build root {owner}")

    cycles: list[str] = []
    for build_root, graph in graph_by_root.items():
        owner = build_root.relative_to(root).as_posix() or "."
        for component in strongly_connected_components(graph):
            if len(component) > 1 or (len(component) == 1 and component[0] in graph[component[0]]):
                cycles.append(f"build={owner} scc={' -> '.join(component)}")
    findings.extend(f"project dependency cycle: {cycle}" for cycle in cycles)

    if findings:
        print("\n".join(f"FAIL {finding}" for finding in findings))
        print(f"FAIL references={checked_references} findings={len(findings)} cycles={len(cycles)} builds={len(build_roots)}")
        return 1
    print(f"PASS references={checked_references} undeclared=0 cycles=0 builds={len(build_roots)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
