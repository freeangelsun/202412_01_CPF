#!/usr/bin/env python3
from __future__ import annotations
import json, re, sys
from pathlib import Path

root = Path(sys.argv[1] if len(sys.argv) > 1 else '.').resolve()
policy_path = root / 'cpf-tools/governance/cpf-optional-surface-policy.json'
fail=[]
def require(cond,msg):
    if not cond: fail.append(msg)
def text(rel):
    p=root/rel
    return p.read_text(encoding='utf-8-sig') if p.is_file() else ''

require(policy_path.is_file(),'optional policy missing')
policy=json.loads(policy_path.read_text(encoding='utf-8-sig'))
settings=text('settings.gradle')
local_runtime=text('cpf-tools/runtime/cpf-local-runtime/build.gradle')
root_conventions=text('cpf-tools/build/cpf-root-conventions.gradle')
profile=json.loads(text('cpf-tools/db/config/database-install.default.json'))

for app in policy.get('sourceRemovableApplications',[]):
    owner=app['ownerPath']; gp=app.get('gradleProject')
    if gp == ':apps:biz-admin':
        require("def cpfBizAdminPresent = file('cpf-biz-admin/build.gradle').isFile()" in settings,
                'BZA settings include is not physical-presence guarded')
        require("if (cpfBizAdminPresent)" in settings, 'BZA projectDir is not guarded')
        require("rootProject.findProject(':apps:biz-admin') != null" in local_runtime,
                'local runtime hard-depends on BZA')
        require("rootProject.findProject(':apps:biz-admin') != null" in root_conventions,
                'root BZA run alias is not absence-safe')
        db=profile.get('modules',{}).get('bizAdmin',{})
        require(db.get('required') is False, 'BZA DB must be optional')
        require(db.get('sourceOptional') is True and db.get('ownerPath')=='cpf-biz-admin',
                'BZA DB does not follow source optional owner')

# Hard Gradle dependency leaks outside guarded owner/local-runtime block.
for p in root.rglob('*.gradle'):
    if any(x in p.parts for x in ('build','.gradle')) and 'cpf-tools' not in p.parts: continue
    if p == root / 'settings.gradle': continue
    if p.as_posix().endswith('cpf-tools/runtime/cpf-local-runtime/build.gradle'): continue
    s=p.read_text(encoding='utf-8-sig',errors='ignore')
    if "project(':apps:biz-admin')" in s:
        fail.append(f'unapproved hard BZA project dependency: {p.relative_to(root)}')

# Deploy inventories must explicitly classify BZA as optional.
for p in (root/'deploy/environments').glob('*/inventory/*.json'):
    data=json.loads(p.read_text(encoding='utf-8-sig'))
    for row in data.get('services',[]):
        if row.get('module')=='BZA' or row.get('serviceName')=='cpf-biz-admin':
            require(row.get('optional') is True and row.get('sourceOwnerPath')=='cpf-biz-admin',
                    f'BZA deploy inventory is not optional: {p.relative_to(root)}')

# All user-selectable starters need an explicit enabled/capability switch in metadata.
catalog=json.loads(text('cpf-tools/generator/contracts/cpf-starter-catalog.json'))
selectable=[m for m in catalog.get('modules',[]) if m.get('userSelectable') is True]
require(bool(selectable),'starter catalog has no user-selectable modules')
for m in selectable:
    require(bool(m.get('configPrefix')), f"selectable starter missing configPrefix: {m.get('artifactId')}")

if fail:
    print('OPTIONAL_SURFACE_CONTRACT=FAIL')
    for x in fail: print(' - '+x)
    raise SystemExit(1)
print(f"OPTIONAL_SURFACE_CONTRACT=PASS sourceRemovable={len(policy.get('sourceRemovableApplications',[]))} selectableStarters={len(selectable)}")
