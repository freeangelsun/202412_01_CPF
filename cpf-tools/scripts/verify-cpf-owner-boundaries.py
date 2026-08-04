#!/usr/bin/env python3
"""Repository-wide module dependency/import owner boundary gate."""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


class GateError(RuntimeError):
    pass


MODULE_OWNER = {
    'cpf-core': 'core',
    'cpf-common': 'common',
    'cpf-admin': 'admin',
    'cpf-biz-admin': 'biz-admin',
    'cpf-batch': 'batch',
    'cpf-gateway': 'gateway',
    'cpf-member': 'generated-domain',
    'cpf-reference': 'generated-domain',
    'cpf-starters': 'starters',
    'cpf-tools': 'tools',
}
PACKAGE_OWNER = {
    'core': 'cpf-core',
    'common': 'cpf-common',
    'admin': 'cpf-admin',
    'bizadmin': 'cpf-biz-admin',
    'batch': 'cpf-batch',
    'gateway': 'cpf-gateway',
    'member': 'cpf-member',
    'reference': 'cpf-reference',
    'starter': 'cpf-starters',
    'tools': 'cpf-tools',
}
PROJECT = re.compile(r"project\(['\"]:([^'\"]+)['\"]\)")
CPF_INTERNAL_REFERENCE = re.compile(r'\bcom\.cpf\.([a-zA-Z0-9_]+)\.internal(?:\.|\b)')
CPF_BATCH_RUNTIME_REFERENCE = re.compile(r'\bcom\.cpf\.batch\..*(?:internal|runtime)(?:\.|\b)')

# The canonical direction is Generated/Business -> common -> core.  These
# reverse/runtime edges are always forbidden regardless of an individual build file.
FORBIDDEN_DEP = {
    ('cpf-core', 'cpf-common'),
    ('cpf-core', 'cpf-admin'),
    ('cpf-core', 'cpf-biz-admin'),
    ('cpf-core', 'cpf-batch'),
    ('cpf-core', 'cpf-gateway'),
    ('cpf-core', 'cpf-member'),
    ('cpf-core', 'cpf-reference'),
    ('cpf-core', 'cpf-starters'),
    ('cpf-common', 'cpf-admin'),
    ('cpf-common', 'cpf-biz-admin'),
    ('cpf-common', 'cpf-batch'),
    ('cpf-common', 'cpf-gateway'),
    ('cpf-common', 'cpf-member'),
    ('cpf-common', 'cpf-reference'),
    ('cpf-admin', 'cpf-biz-admin'),
}


def top(path: Path, root: Path) -> str:
    try:
        return path.relative_to(root).parts[0]
    except (ValueError, IndexError):
        return ''


def verify(root: Path) -> dict:
    findings: list[str] = []
    graph: dict[str, list[str]] = {}
    build_files: list[str] = []
    java_files: list[str] = []
    internal_refs: list[dict[str, str]] = []

    settings = root / 'settings.gradle'
    settings_kts = root / 'settings.gradle.kts'
    if not settings.is_file() and not settings_kts.is_file():
        findings.append('repository settings.gradle/settings.gradle.kts is required; sparse snapshot cannot pass owner gate')
    if not (root / 'build.gradle').is_file() and not (root / 'build.gradle.kts').is_file():
        findings.append('root build.gradle/build.gradle.kts is required; sparse snapshot cannot pass owner gate')

    for build in sorted(root.rglob('build.gradle')) + sorted(root.rglob('build.gradle.kts')):
        if any(part in build.parts for part in ('build', '.gradle', 'node_modules')):
            continue
        source = top(build, root)
        build_files.append(build.relative_to(root).as_posix())
        dependencies: list[str] = []
        text = build.read_text(encoding='utf-8-sig')
        for raw in PROJECT.findall(text):
            destination = raw.split(':', 1)[0]
            dependencies.append(destination)
            if (source, destination) in FORBIDDEN_DEP:
                findings.append(f'{build.relative_to(root)}: forbidden dependency {source}->{destination}')
            if source != 'cpf-batch' and destination == 'cpf-batch':
                findings.append(f'{build.relative_to(root)}: non-BAT module depends on BAT runtime')
        graph[source] = sorted(set(graph.get(source, []) + dependencies))

    visiting: set[str] = set()
    visited: set[str] = set()

    def dfs(node: str, stack: list[str]) -> None:
        if node in visiting:
            findings.append('dependency cycle: ' + ' -> '.join(stack + [node]))
            return
        if node in visited:
            return
        visiting.add(node)
        for dependency in graph.get(node, []):
            dfs(dependency, stack + [node])
        visiting.remove(node)
        visited.add(node)

    for node in graph:
        dfs(node, [])

    for path in sorted(root.rglob('src/main/java/**/*.java')):
        if any(part in path.parts for part in ('build', 'generated')):
            continue
        source = top(path, root)
        relative = path.relative_to(root).as_posix()
        text = path.read_text(encoding='utf-8-sig')
        java_files.append(relative)

        for package_segment in sorted(set(CPF_INTERNAL_REFERENCE.findall(text))):
            expected = PACKAGE_OWNER.get(package_segment)
            reference = f'com.cpf.{package_segment}.internal'
            if expected is None:
                findings.append(f'{relative}: unknown CPF internal owner {reference}')
                internal_refs.append({'file': relative, 'reference': reference, 'expectedOwner': ''})
            elif expected != source:
                findings.append(f'{relative}: cross-owner internal reference {reference} expectedOwner={expected}')
                internal_refs.append({'file': relative, 'reference': reference, 'expectedOwner': expected})

        if source == 'cpf-core' and re.search(r'\bcom\.cpf\.(?:common|admin|bizadmin|batch|gateway|member|reference|starter)\.', text):
            findings.append(f'{relative}: cpf-core owns reverse/runtime CPF reference')
        if source != 'cpf-batch' and CPF_BATCH_RUNTIME_REFERENCE.search(text):
            findings.append(f'{relative}: BAT runtime implementation reference')

    if len(build_files) < 3:
        findings.append(f'owner graph incomplete: expected repository module build files, found {len(build_files)}')
    if len(java_files) < 1:
        findings.append('owner graph incomplete: no main Java source scanned')

    result = {
        'status': 'PASS' if not findings else 'FAIL',
        'moduleOwners': MODULE_OWNER,
        'packageOwners': PACKAGE_OWNER,
        'buildFileCount': len(build_files),
        'mainJavaFileCount': len(java_files),
        'moduleGraph': graph,
        'internalReferenceCount': len(internal_refs),
        'internalReferences': internal_refs,
        'findings': findings,
    }
    if findings:
        raise GateError(json.dumps(result, ensure_ascii=False, indent=2))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', default='.')
    parser.add_argument('--json-output')
    args = parser.parse_args()
    root = Path(args.root).resolve()
    try:
        result = verify(root)
        code = 0
    except Exception as failure:
        try:
            result = json.loads(str(failure))
        except json.JSONDecodeError:
            result = {'status': 'FAIL', 'message': str(failure)}
        code = 1
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else root / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == '__main__':
    raise SystemExit(main())
