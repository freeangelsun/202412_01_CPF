#!/usr/bin/env python3
"""Fail-closed gate for the canonical CPF Education 20 Online + 15 Batch Golden Path.

The gate validates not only scenario counts but the feature-first/role-package source IA,
public API boundary, canonical catalog linkage and absence of compressed nested business types.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

ONLINE = {
    'basiccrud','querypaging','common','validation','internalservice','domaincall','externalrest','fixedlength',
    'transactionrequired','transactionrequiresnew','externalsideeffect','ondemandbatch','centercut','cache','messaging',
    'file','securityaudit','recovery','concurrency','webhook'
}
BATCH = {
    'tasklet','chunk','flatfile','partition','centercut','scheduler','restart','distributedworker',
    'shellcommand','conditionalflow','chunktransaction','requiresnew','steptransaction','externalcall','ondemand'
}
ROLE_NAMES = {
    'controller','service','repository','dao','client','dto','model','handler','listener','adapter',
    'recovery','security','state','reader','processor','writer','step','job'
}
FORBIDDEN_IMPORT = re.compile(r'^import\s+.*(?:\.internal\.|\.impl\.|\.provider\.internal\.|runtime\.internal\.)', re.M)
FORBIDDEN_ALIAS = re.compile(r'@Cpf(?:RestController|Timed|Tx|Dao)\b')
FORBIDDEN_FILE_NAME = re.compile(r'^(?:Online|Batch)\d+|(?:Example|Sample|Demo)(?:\.java)?$', re.I)
TYPE_DECL = re.compile(r'\b(?:public\s+|protected\s+|private\s+)?(?:static\s+)?(?:final\s+)?(?:class|record|interface|enum)\s+[A-Za-z_$][\w$]*')
CATALOG = 'cpf-education/src/main/resources/education/cpf-education-canonical-35.json'
DELETE_MANIFEST = 'cpf-docs/deliverables/DELETE_MANIFEST.csv'


def deleted(root: Path) -> set[str]:
    p = root / DELETE_MANIFEST
    if not p.exists():
        return set()
    import csv
    rows = []
    with p.open(encoding='utf-8-sig', newline='') as fh:
        for row in csv.DictReader(fh):
            path = (row.get('path') or '').strip().replace('\\', '/')
            approved = (row.get('approved') or '').strip().lower() == 'true'
            precondition = (row.get('precondition') or 'NONE').strip()
            user_approved = (row.get('user_approved') or 'false').strip().lower() == 'true'
            # Source-view verifier excludes only paths that are both internally approved and explicitly user-approved.
            if path and approved and user_approved and precondition in {'NONE','SATISFIED'}:
                rows.append(path)
    return set(rows)


def active_java(root: Path, base: Path, deleted_paths: set[str]) -> list[Path]:
    if not base.is_dir():
        return []
    return [
        p for p in base.rglob('*.java')
        if p.relative_to(root).as_posix() not in deleted_paths
    ]


def feature_key_and_root(p: Path, category_root: Path, category: str) -> tuple[str, Path]:
    rel = p.relative_to(category_root).parts
    return (rel[0] if rel else ''), (category_root / rel[0] if rel else category_root)


def role_names_for_feature(files: list[Path], feature_root: Path) -> set[str]:
    roles: set[str] = set()
    for p in files:
        try:
            rel = p.relative_to(feature_root)
        except ValueError:
            continue
        if len(rel.parts) > 1 and rel.parts[0] in ROLE_NAMES:
            roles.add(rel.parts[0])
    return roles


def nested_type_count(text: str) -> int:
    """Count type declarations below top-level brace depth, ignoring comments/strings approximately."""
    # This is intentionally conservative and is only a source-IA gate, not a Java parser.
    scrub = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    scrub = re.sub(r'//.*', '', scrub)
    scrub = re.sub(r'"(?:\\.|[^"\\])*"', '""', scrub)
    depth = 0
    nested = 0
    for line in scrub.splitlines():
        # A declaration at brace depth > 0 is nested inside the primary top-level type.
        if depth > 0 and TYPE_DECL.search(line):
            nested += 1
        depth += line.count('{') - line.count('}')
        depth = max(depth, 0)
    return nested


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    a = ap.parse_args()
    root = Path(a.root).resolve()
    errors: list[str] = []
    dp = deleted(root)
    java_root = root / 'cpf-education/src/main/java/com/cpf/education'
    test_root = root / 'cpf-education/src/test/java/com/cpf/education'

    online_role_groups = 0
    batch_role_groups = 0
    nested_types = 0

    if not java_root.is_dir():
        errors.append('canonical Education Java root missing')
    else:
        online_root = java_root / 'online'
        batch_root = java_root / 'batch'
        online_files = active_java(root, online_root, dp)
        batch_files = active_java(root, batch_root, dp)

        online_grouped: dict[str, list[Path]] = {}
        batch_grouped: dict[str, list[Path]] = {}
        online_roots: dict[str, Path] = {}
        batch_roots: dict[str, Path] = {}
        for p in online_files:
            key, feature_root = feature_key_and_root(p, online_root, 'online')
            online_grouped.setdefault(key, []).append(p)
            online_roots[key] = feature_root
        for p in batch_files:
            key, feature_root = feature_key_and_root(p, batch_root, 'batch')
            batch_grouped.setdefault(key, []).append(p)
            batch_roots[key] = feature_root

        online_keys = set(online_grouped)
        batch_keys = set(batch_grouped)
        if online_keys != ONLINE:
            errors.append('online feature packages mismatch missing=' + ','.join(sorted(ONLINE-online_keys)) + ' extra=' + ','.join(sorted(online_keys-ONLINE)))
        if batch_keys != BATCH:
            errors.append('batch feature packages mismatch missing=' + ','.join(sorted(BATCH-batch_keys)) + ' extra=' + ','.join(sorted(batch_keys-BATCH)))

        for key in sorted(ONLINE & online_keys):
            roles = role_names_for_feature(online_grouped[key], online_roots[key])
            if roles:
                online_role_groups += 1
            else:
                errors.append(f'online feature has no role package: {key}')

        for key in sorted(BATCH & batch_keys):
            roles = role_names_for_feature(batch_grouped[key], batch_roots[key])
            if 'service' in roles or {'reader','processor','writer','step','job'} & roles:
                batch_role_groups += 1
            else:
                errors.append(f'batch feature has no execution role package: {key}')

        for p in online_files + batch_files:
            if FORBIDDEN_FILE_NAME.search(p.name):
                errors.append('non-functional Java name: ' + p.relative_to(root).as_posix())
            text = p.read_text(encoding='utf-8', errors='replace')
            if FORBIDDEN_IMPORT.search(text):
                errors.append('internal/raw implementation import: ' + p.relative_to(root).as_posix())
            if FORBIDDEN_ALIAS.search(text):
                errors.append('deprecated/non-canonical CPF alias: ' + p.relative_to(root).as_posix())
            count = nested_type_count(text)
            if count:
                nested_types += count
                errors.append(f'nested business type(s)={count}: {p.relative_to(root).as_posix()}')

    cp = root / CATALOG
    if not cp.exists():
        errors.append('canonical 35 catalog missing')
    else:
        try:
            data = json.loads(cp.read_text(encoding='utf-8'))
        except Exception as e:
            errors.append('catalog JSON invalid: ' + str(e))
            data = {}
        examples = data.get('examples', []) if isinstance(data, dict) else []
        if len(examples) != 35:
            errors.append(f'catalog example count must be 35, actual={len(examples)}')
        if sum(1 for x in examples if x.get('category') == 'online') != 20:
            errors.append('catalog online count must be 20')
        if sum(1 for x in examples if x.get('category') == 'batch') != 15:
            errors.append('catalog batch count must be 15')
        ids = [x.get('id') for x in examples]
        if len(ids) != len(set(ids)):
            errors.append('duplicate EDU catalog id')
        for x in examples:
            pkg = x.get('package', '')
            primary = x.get('primaryClass', '')
            test = x.get('testClass', '')
            source_dir = root / 'cpf-education/src/main/java' / Path(pkg.replace('.', '/'))
            matches = list(source_dir.rglob(primary + '.java')) if source_dir.exists() and primary else []
            matches = [m for m in matches if m.relative_to(root).as_posix() not in dp]
            if len(matches) != 1:
                errors.append(f"{x.get('id')}: primary class not exactly one: {pkg}.{primary}")
            test_matches = list(test_root.rglob(test + '.java')) if test_root.exists() and test else []
            test_matches = [m for m in test_matches if m.relative_to(root).as_posix() not in dp]
            if len(test_matches) != 1:
                errors.append(f"{x.get('id')}: test class not exactly one: {test}")

    for e in errors:
        print('[FAIL]', e)
    if errors:
        return 1
    print(
        'CPF_EDUCATION_ACTIVE_SURFACE=PASS '
        f'online=20 batch=15 onlineRoleGroups={online_role_groups} '
        f'batchRoleGroups={batch_role_groups} nestedTypes={nested_types} '
        'numeric=0 internal_import=0 catalog=35'
    )
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
