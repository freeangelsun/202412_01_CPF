#!/usr/bin/env python3
"""Canonical starter package-owner/catalog verifier.

Validates physical starter source packages against cpf-starter-catalog.json and
kills stale implementation namespaces/consumer references.  Full checkout is
strict by default; --module may be used for a deliberately sparse verification
root without turning missing modules into a false PASS.
"""
from __future__ import annotations

import argparse
import json
import re
import tempfile
from pathlib import Path

CATALOG = Path('cpf-tools/generator/contracts/cpf-starter-catalog.json')
LEGACY_PATTERNS = (
    re.compile(r'com\.cpf\.starter\.persistence\.mybatis(?:\.|\b)'),
    re.compile(r'com\.cpf\.core\.mapper(?:\.|\b)'),
    re.compile(r'com\.cpf\.core\.config\.CpfMyBatisConfig\b'),
    re.compile(r'com\.cpf\.starter\.messaging\.reliability\.(?!jdbc(?:\.|\b))'),
    re.compile(r'com\.cpf\.core\.common\.broker\.(?:CpfBrokerBridgeAdapter|CpfBrokerConsumerRuntimePolicy|CpfBrokerConsumerWorker|CpfBrokerOutboxIdentity|CpfBrokerPublisherWorker|JdbcCpfBrokerReliabilityRepository)\b'),
    re.compile(r'com\.cpf\.starter\.security\.(?:CpfBff|CpfCredentialCipher\b|CpfCsrfCookieExposureFilter\b|CpfServerSession|CpfSessionReadinessVerifier\b|CpfTrustedOriginFilter\b|JdbcCpfBffCredentialVault\b)'),
    re.compile(r'com\.cpf\.starter\.security\.runtimecontrol(?:\.|\b)'),
)
TEXT_SUFFIXES = {'.java', '.kt', '.groovy', '.xml', '.properties', '.yml', '.yaml', '.imports', '.gradle'}
SKIP_PARTS = {'.git', 'build', '.gradle', 'node_modules', 'dist', 'target'}


def package_of(text: str) -> str | None:
    m = re.search(r'^\s*package\s+([\w.]+)\s*;', text, re.M)
    return m.group(1) if m else None


def load_modules(root: Path) -> list[dict]:
    cp = root / CATALOG
    if not cp.is_file():
        raise ValueError(f'catalog missing: {CATALOG.as_posix()}')
    data = json.loads(cp.read_text(encoding='utf-8'))
    mods = data.get('modules') if isinstance(data, dict) else None
    if not isinstance(mods, list):
        raise ValueError('catalog modules missing')
    out = []
    for m in mods:
        if not isinstance(m, dict):
            continue
        owner = m.get('ownerPath')
        base = m.get('packageBase')
        project = m.get('projectPath')
        if isinstance(owner, str) and owner.startswith('cpf-starters/') and isinstance(base, str) and base:
            out.append({'ownerPath': owner, 'packageBase': base, 'projectPath': project or ''})
    if not out:
        raise ValueError('catalog has no canonical starter modules')
    return out


def selected_modules(mods: list[dict], names: list[str]) -> list[dict]:
    if not names:
        return mods
    wanted = set(names)
    out = []
    for m in mods:
        keys = {m['ownerPath'], m['projectPath'], Path(m['ownerPath']).name}
        if wanted & keys:
            out.append(m)
    missing = wanted - {k for m in out for k in (m['ownerPath'], m['projectPath'], Path(m['ownerPath']).name)}
    if missing:
        raise ValueError('unknown --module: ' + ','.join(sorted(missing)))
    return out


def iter_text(root: Path):
    for p in root.rglob('*'):
        if not p.is_file() or p.suffix.lower() not in TEXT_SUFFIXES:
            continue
        try:
            rel = p.relative_to(root)
        except ValueError:
            continue
        if any(part in SKIP_PARTS for part in rel.parts):
            continue
        yield p, rel


def validate(root: Path, module_names: list[str], strict_all: bool) -> tuple[list[str], int, int]:
    violations: list[str] = []
    try:
        mods = selected_modules(load_modules(root), module_names)
    except (ValueError, json.JSONDecodeError) as e:
        return [str(e)], 0, 0

    checked_files = 0
    for m in mods:
        module = root / m['ownerPath']
        if not module.is_dir():
            if strict_all and not module_names:
                violations.append(f"catalog ownerPath missing: {m['ownerPath']}")
            elif module_names:
                violations.append(f"selected ownerPath missing: {m['ownerPath']}")
            continue
        main_java = module / 'src/main/java'
        if not main_java.is_dir():
            violations.append(f"main java missing: {m['ownerPath']}/src/main/java")
            continue
        java_files = list(main_java.rglob('*.java'))
        if not java_files:
            violations.append(f"no Java implementation source: {m['ownerPath']}")
            continue
        for p in java_files:
            checked_files += 1
            text = p.read_text(encoding='utf-8', errors='replace')
            pkg = package_of(text)
            base = m['packageBase']
            if not pkg or not (pkg == base or pkg.startswith(base + '.')):
                violations.append(f"{p.relative_to(root)} package={pkg or '<missing>'} expectedPrefix={base}")

    # Stale implementation namespaces must not survive in source/resources or consumers.
    # In sparse --module mode scan the whole provided root, so a supplied consumer can still fail.
    for p, rel in iter_text(root):
        text = p.read_text(encoding='utf-8', errors='replace')
        for pat in LEGACY_PATTERNS:
            if pat.search(text):
                violations.append(f'{rel} stale implementation namespace: {pat.pattern}')
                break

    # One physical package owner per canonical packageBase.
    bases: dict[str, str] = {}
    for m in mods:
        base = m['packageBase']
        prev = bases.get(base)
        if prev and prev != m['ownerPath']:
            violations.append(f'duplicate canonical packageBase {base}: {prev}, {m["ownerPath"]}')
        bases[base] = m['ownerPath']

    return violations, len(mods), checked_files


def mutation_self_test() -> None:
    with tempfile.TemporaryDirectory(prefix='cpf-starter-package-owner-') as td:
        root = Path(td)
        (root / CATALOG.parent).mkdir(parents=True)
        mods = [
            {'projectPath': ':cpf-starter-a', 'ownerPath': 'cpf-starters/a', 'packageBase': 'com.cpf.starter.a'},
            {'projectPath': ':cpf-starter-b', 'ownerPath': 'cpf-starters/b', 'packageBase': 'com.cpf.starter.b'},
        ]
        (root / CATALOG).write_text(json.dumps({'modules': mods}), encoding='utf-8')
        for owner, pkg in [('cpf-starters/a', 'com.cpf.starter.a'), ('cpf-starters/b', 'com.cpf.starter.b.internal')]:
            p = root / owner / 'src/main/java/X.java'
            p.parent.mkdir(parents=True)
            p.write_text(f'package {pkg};\npublic class X {{}}\n', encoding='utf-8')
        v, mc, fc = validate(root, [], True)
        if v or mc != 2 or fc != 2:
            raise SystemExit('FAIL starter package-owner mutation baseline: ' + '; '.join(v))

        p = root / 'cpf-starters/a/src/main/java/X.java'
        p.write_text('package com.cpf.core.bad;\npublic class X {}\n', encoding='utf-8')
        v, _, _ = validate(root, [], True)
        if not any('expectedPrefix=com.cpf.starter.a' in x for x in v):
            raise SystemExit('FAIL mutation survived: wrong package owner accepted')
        p.write_text('package com.cpf.starter.a;\npublic class X {}\n', encoding='utf-8')

        c = root / 'cpf-reference/src/main/java/Consumer.java'
        c.parent.mkdir(parents=True)
        c.write_text('import com.cpf.core.common.broker.CpfBrokerPublisherWorker;\nclass Consumer {}\n', encoding='utf-8')
        v, _, _ = validate(root, [], True)
        if not any('stale implementation namespace' in x for x in v):
            raise SystemExit('FAIL mutation survived: stale consumer import accepted')
    print('PASS starter package-owner mutation killed')


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument('--root', type=Path, default=Path('.'))
    ap.add_argument('--module', action='append', default=[], help='projectPath, ownerPath, or module directory name; repeatable')
    ap.add_argument('--allow-partial', action='store_true', help='only meaningful with explicit --module; never use for release full-checkout gate')
    ap.add_argument('--self-test', action='store_true')
    args = ap.parse_args()
    root = args.root.resolve()
    if args.allow_partial and not args.module:
        print('FAIL --allow-partial requires explicit --module')
        return 2
    violations, modules, files = validate(root, args.module, strict_all=not args.allow_partial)
    if violations:
        print(f'FAIL starter package owner modules={modules} checkedJava={files}')
        for v in sorted(set(violations)):
            print(v)
        return 1
    if args.self_test:
        mutation_self_test()
    print(f'PASS starter package owner modules={modules} checkedJava={files} consumerStaleRefs=0')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
