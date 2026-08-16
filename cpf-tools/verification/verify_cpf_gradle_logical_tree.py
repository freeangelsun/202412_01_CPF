#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

PROJECT_DIR = re.compile(
    r"project\(\s*['\"](:[^'\"]+)['\"]\s*\)\.projectDir\s*=\s*file\(\s*['\"]([^'\"]+)['\"]\s*\)"
)
PROJECT_REF = re.compile(r"\bproject\(\s*['\"](:[^'\"]+)['\"]\s*\)")
ARTIFACT_ID = re.compile(r"\bartifactId\s*=\s*['\"]([^'\"]+)['\"]")

NON_STARTER_EXPECTED = {
    ':framework:core': 'cpf-core',
    ':framework:testkit': 'cpf-tools/testing/cpf-testkit',
    ':apps:admin': 'cpf-admin',
    ':apps:biz-admin': 'cpf-biz-admin',
    ':apps:education': 'cpf-education',
    ':runtime:gateway': 'cpf-gateway',
    ':runtime:local': 'cpf-tools/runtime/cpf-local-runtime',
    ':runtime:local-batch': 'cpf-tools/runtime/cpf-local-batch-runtime',
    ':internal:verification:core-only-consumer': 'cpf-tools/verification/core-only-consumer',
    ':runtime:batch': 'cpf-batch',
    ':runtime:batch:api': 'cpf-batch/api',
    ':runtime:batch:runtime-support': 'cpf-batch/runtime-support',
    ':runtime:batch:runtime': 'cpf-batch/runtime',
    ':runtime:batch:control-plane': 'cpf-batch/control-plane',
    ':runtime:batch:scheduler': 'cpf-batch/scheduler',
    ':runtime:batch:worker': 'cpf-batch/worker',
    ':runtime:batch:center-cut': 'cpf-batch/center-cut',
    ':runtime:batch:agent': 'cpf-batch/agent',
    ':runtime:batch:testkit': 'cpf-batch/testkit',
}


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    args = ap.parse_args()
    root = Path(args.root).resolve()
    errors: list[str] = []

    settings = root / 'settings.gradle'
    catalog_path = root / 'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    if not settings.is_file():
        print('FAIL settings.gradle missing')
        return 1
    if not catalog_path.is_file():
        print('FAIL canonical Starter Catalog missing')
        return 1

    settings_text = settings.read_text(encoding='utf-8')
    assignments = {path: owner.replace('\\', '/') for path, owner in PROJECT_DIR.findall(settings_text)}
    for path, owner in NON_STARTER_EXPECTED.items():
        actual = assignments.get(path)
        if actual != owner:
            errors.append(f'logical non-starter mapping mismatch {path}: expected={owner} actual={actual}')
        build_file = root / owner / 'build.gradle'
        if not build_file.is_file():
            errors.append(f'logical non-starter physical owner missing {path}: {owner}')

    catalog = json.loads(catalog_path.read_text(encoding='utf-8'))
    modules = catalog.get('modules') or []
    logical_paths: set[str] = set(NON_STARTER_EXPECTED)
    artifact_ids: set[str] = set()
    for module in modules:
        project_path = str(module.get('projectPath') or '')
        owner = str(module.get('ownerPath') or '').replace('\\', '/').rstrip('/')
        artifact = str(module.get('artifactId') or '')
        visibility = str(module.get('visibility') or '')
        expected_prefix = ':starters:' if visibility == 'public' else ':internal:' if visibility == 'internal' else ''
        if not expected_prefix or not project_path.startswith(expected_prefix):
            errors.append(f'logical starter partition mismatch {artifact}: {project_path} visibility={visibility}')
        if project_path in logical_paths:
            errors.append(f'duplicate logical project path: {project_path}')
        logical_paths.add(project_path)
        if artifact in artifact_ids or not artifact:
            errors.append(f'duplicate/blank artifactId: {artifact}')
        artifact_ids.add(artifact)
        build_file = root / owner / 'build.gradle'
        if not build_file.is_file():
            errors.append(f'catalog physical owner missing {artifact}: {owner}')
            continue
        if module.get('publicationRequired') is not False:
            text = build_file.read_text(encoding='utf-8')
            explicit = set(ARTIFACT_ID.findall(text))
            if artifact not in explicit:
                errors.append(
                    f'publication coordinate not pinned after logical rename {artifact}: '
                    f'{owner}/build.gradle explicitArtifactIds={sorted(explicit)}'
                )

    # All active Gradle project() consumers must use the logical tree, not the retired flat :cpf-* path.
    active_gradle_files = [settings]
    active_gradle_files.extend(
        path for path in root.rglob('*.gradle')
        if '.gradle' not in path.relative_to(root).parts
        and 'node_modules' not in path.relative_to(root).parts
        and not ('build' in path.relative_to(root).parts and not path.relative_to(root).as_posix().startswith('cpf-tools/build/'))
    )
    retired_refs: list[str] = []
    for path in active_gradle_files:
        text = path.read_text(encoding='utf-8', errors='ignore')
        for ref in PROJECT_REF.findall(text):
            if ref.startswith(':cpf-'):
                retired_refs.append(f'{path.relative_to(root).as_posix()} -> {ref}')
    if retired_refs:
        errors.extend(f'retired flat Gradle path: {row}' for row in retired_refs)

    convention = root / 'cpf-tools/build/cpf-root-conventions.gradle'
    if convention.is_file():
        help_text = convention.read_text(encoding='utf-8')
        if 'Gradle Projects의 프로젝트 목록은 확장 특성상 flat' in help_text:
            errors.append('cpfHelp still claims Gradle Projects are flat')
        for label in ('apps / runtime / framework / starters / internal', 'cpfRunLocal', 'cpfBuild', 'cpfVerifyFast'):
            if label not in help_text:
                errors.append(f'cpfHelp logical-tree guidance missing: {label}')

    if errors:
        for error in errors:
            print(f'FAIL {error}')
        print(f'FAIL logicalProjects={len(logical_paths)} errors={len(errors)}')
        return 1
    print(
        f'PASS logicalProjects={len(logical_paths)} starters={len(modules)} '
        f'nonStarters={len(NON_STARTER_EXPECTED)} retiredProjectRefs=0 publicationCoordinatesPinned=true'
    )
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
