#!/usr/bin/env python3
"""Active Starter build files must not consume retired/inactive Starter coordinates."""
from __future__ import annotations
from pathlib import Path
import argparse, json, sys
ROOT=Path(__file__).resolve().parents[2]
ap=argparse.ArgumentParser();ap.add_argument('--root',default=str(ROOT));a=ap.parse_args();root=Path(a.root).resolve()
catp=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json';fail=[]
if not catp.is_file():
    print('CPF_RETIRED_STARTER_DEPENDENCIES=FAIL\nfailures=1\nCATALOG_MISSING');raise SystemExit(1)
cat=json.loads(catp.read_text(encoding='utf-8'))
mods=cat.get('modules',[])
retired={'cpf-starter-foundation-base','cpf-starter-profile-minimal-domain','cpf-starter-openapi-webmvc'}
for item in cat.get('retainedInactiveRoots',[]):
    if item.get('artifactId'): retired.add(str(item['artifactId']))
for item in cat.get('removedArtifacts',[]):
    if item.get('artifactId'): retired.add(str(item['artifactId']))
scanned=0
for module in mods:
    owner=str(module.get('ownerPath','')).strip('/');aid=str(module.get('artifactId',''))
    if not owner: continue
    build=root/owner/'build.gradle'
    if not build.is_file(): continue
    scanned+=1;text=build.read_text(encoding='utf-8',errors='ignore')
    for rid in sorted(retired):
        if f"project(':{rid}')" in text or f'project(\":{rid}\")' in text or f':{rid}:' in text:
            fail.append(f'ACTIVE_RETIRED_DEP:{aid}->{rid}:{owner}/build.gradle')
fail=sorted(set(fail))
print('CPF_RETIRED_STARTER_DEPENDENCIES='+('PASS' if not fail else 'FAIL'))
print(f'activeModules={len(mods)} scannedBuilds={scanned} retiredIds={len(retired)} failures={len(fail)}')
for x in fail: print(x)
raise SystemExit(0 if not fail else 1)
