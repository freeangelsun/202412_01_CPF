#!/usr/bin/env python3
from __future__ import annotations
import argparse, json
from pathlib import Path

ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args()
root = Path(ns.root).resolve()
policy_path = root / 'cpf-tools/governance/cpf-optional-surface-policy.json'
fail: list[str] = []
def require(cond: bool, msg: str) -> None:
    if not cond: fail.append(msg)
def text(rel: str) -> str:
    p = root / rel
    return p.read_text(encoding='utf-8-sig') if p.is_file() else ''

require(policy_path.is_file(), 'optional policy missing')
if not policy_path.is_file():
    print('OPTIONAL_SURFACE_CONTRACT=FAIL\n - optional policy missing')
    raise SystemExit(1)
policy = json.loads(policy_path.read_text(encoding='utf-8-sig'))
settings = text('settings.gradle')
local_runtime = text('cpf-tools/runtime/cpf-local-runtime/build.gradle')
root_conventions = text('cpf-tools/build/cpf-root-conventions.gradle')
profile = json.loads(text('cpf-tools/db/config/database-install.default.json'))

for app in policy.get('sourceRemovableApplications', []):
    owner = str(app.get('ownerPath') or '')
    gp = app.get('gradleProject')
    db_key = app.get('databaseModuleKey')
    if gp == ':apps:backoffice':
        require("def cpfBackofficePresent = file('cpf-backoffice/online/build.gradle').isFile()" in settings,
                'Backoffice settings include is not physical-presence guarded')
        require("if (cpfBackofficePresent)" in settings, 'Backoffice projectDir is not guarded')
        require("rootProject.findProject(':apps:backoffice') != null" in local_runtime,
                'local runtime hard-depends on Backoffice')
        require("cpfMountGeneratedDomains" not in local_runtime
                and "dependencies.add('implementation', \"${packageName}:online:1.0.0-SNAPSHOT\")" not in local_runtime,
                'local runtime must not merge independent Generated Domain source builds into its classpath')
        require("rootProject.findProject(':apps:backoffice') != null" in root_conventions,
                'root Backoffice run alias is not absence-safe')
        db = profile.get('modules', {}).get(db_key or '', {})
        require(db.get('required') is False, 'Backoffice DB must be optional')
        require(db.get('sourceOptional') is True and db.get('ownerPath') == owner,
                'Backoffice DB does not follow source optional owner')

# Hard Gradle dependency leaks outside the two approved guarded consumers.
for p in root.rglob('*.gradle'):
    rel = p.relative_to(root).as_posix()
    if not p.is_file() or '.gradle' in p.parts:
        continue
    if 'build' in p.parts and not rel.startswith('cpf-tools/build/'):
        continue
    if rel in {'settings.gradle', 'cpf-tools/runtime/cpf-local-runtime/build.gradle', 'cpf-tools/build/cpf-root-conventions.gradle'}:
        continue
    s = p.read_text(encoding='utf-8-sig', errors='ignore')
    if "project(':apps:backoffice')" in s:
        fail.append(f'unapproved hard Backoffice project dependency: {rel}')

# Deploy inventories must classify Backoffice as optional if present.
for p in (root / 'deploy/environments').glob('*/inventory/*.json'):
    data = json.loads(p.read_text(encoding='utf-8-sig'))
    for row in data.get('services', []):
        if row.get('module') == 'MBW' or row.get('serviceName') == 'cpf-backoffice':
            require(row.get('optional') is True and row.get('sourceOwnerPath') == 'cpf-backoffice',
                    f'Backoffice deploy inventory is not optional: {p.relative_to(root)}')

catalog = json.loads(text('cpf-tools/generator/contracts/cpf-starter-catalog.json'))
selectable = [m for m in catalog.get('modules', []) if m.get('userSelectable') is True]
require(bool(selectable), 'starter catalog has no user-selectable modules')
for m in selectable:
    require(bool(m.get('configPrefix')), f"selectable starter missing configPrefix: {m.get('artifactId')}")

if fail:
    print('OPTIONAL_SURFACE_CONTRACT=FAIL')
    for x in fail: print(' - ' + x)
    raise SystemExit(1)
print(f"OPTIONAL_SURFACE_CONTRACT=PASS sourceRemovable={len(policy.get('sourceRemovableApplications', []))} selectableStarters={len(selectable)}")
