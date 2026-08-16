#!/usr/bin/env python3
"""Create/compare a CPF upgrade surface snapshot.

The report is intentionally predictive: it does not claim binary compatibility. It exposes
Public Starter/API, config, generator, DB migration and OpenAPI surface drift before upgrade.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path

PUBLIC_DECL = re.compile(r"\bpublic\s+(?:final\s+|sealed\s+|abstract\s+)?(?:class|interface|record|@interface|enum)\s+(\w+)")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+);", re.MULTILINE)
CONFIG_PREFIX = re.compile(r"@ConfigurationProperties\s*\(\s*prefix\s*=\s*[\"']([^\"']+)")


def sha(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def public_api(root: Path, modules: list[dict]) -> list[str]:
    values=set()
    for module in modules:
        if module.get('visibility')!='public': continue
        base=root/module['ownerPath']
        if not base.exists(): continue
        for f in base.rglob('*.java'):
            rel=f.as_posix()
            if '/src/test/' in rel or any(t in rel for t in ('/internal/','/runtime/','/autoconfigure/','/config/')): continue
            text=f.read_text(encoding='utf-8',errors='ignore')
            d=PUBLIC_DECL.search(text); p=PACKAGE.search(text)
            if d and p: values.add(f"{p.group(1)}.{d.group(1)}")
    # cpf-core is a public framework contract and is intentionally outside Starter catalog ownership.
    core=root/'cpf-core/src/main/java'
    if core.exists():
        for f in core.rglob('*.java'):
            text=f.read_text(encoding='utf-8',errors='ignore'); d=PUBLIC_DECL.search(text); p=PACKAGE.search(text)
            if d and p and '.internal.' not in p.group(1): values.add(f"{p.group(1)}.{d.group(1)}")
    return sorted(values)


def config_prefixes(root: Path) -> list[str]:
    values=set()
    for base_name in ('cpf-starters','cpf-admin','cpf-biz-admin','cpf-batch'):
        base=root/base_name
        if not base.exists(): continue
        for f in base.rglob('*.java'):
            if '/src/test/' in f.as_posix(): continue
            values.update(CONFIG_PREFIX.findall(f.read_text(encoding='utf-8',errors='ignore')))
    return sorted(values)


def openapi_operations(root: Path) -> dict[str,list[str]]:
    result={}
    for key,rel in {'ADM':'cpf-admin/frontend/openapi/cpf-openapi.json','BZA':'cpf-biz-admin/frontend/openapi/cpf-openapi.json'}.items():
        path=root/rel; ops=[]
        if path.is_file():
            doc=json.loads(path.read_text(encoding='utf-8'))
            for route,item in (doc.get('paths') or {}).items():
                for method,operation in item.items():
                    if isinstance(operation,dict) and operation.get('operationId'):
                        ops.append(f"{method.upper()} {route}#{operation['operationId']}")
        result[key]=sorted(ops)
    return result


def snapshot(root: Path) -> dict:
    catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
    modules=catalog.get('modules') or []
    public_starters=sorted(f"{m.get('groupId')}:{m.get('artifactId')}|{m.get('usageLevel')}" for m in modules if m.get('visibility')=='public')
    generator_files=['cpf-tools/generator/contracts/cpf-domain.schema.json','cpf-tools/generator/contracts/generator-lifecycle-contract.json','cpf-tools/generator/contracts/capability-profiles.json']
    generator={rel:sha(root/rel) for rel in generator_files if (root/rel).is_file()}
    migrations={}
    vendor_root=root/'cpf-tools/db/vendor'
    if vendor_root.exists():
        for vendor in ('oracle','postgresql','mariadb'):
            base=vendor_root/vendor/'migration'
            migrations[vendor]=sorted(f.relative_to(root).as_posix() for f in base.rglob('V*.sql') if f.is_file()) if base.exists() else []
    return {
        'schemaVersion':1,
        'publicStarters':public_starters,
        'publicApi':public_api(root,modules),
        'configPrefixes':config_prefixes(root),
        'generatorContractSha256':generator,
        'dbMigrations':migrations,
        'openApiOperations':openapi_operations(root),
    }


def compare(old: dict,new: dict)->dict:
    def delta_list(a,b):
        aa=set(a or []); bb=set(b or [])
        return {'added':sorted(bb-aa),'removed':sorted(aa-bb)}
    result={
        'publicStarters':delta_list(old.get('publicStarters'),new.get('publicStarters')),
        'publicApi':delta_list(old.get('publicApi'),new.get('publicApi')),
        'configPrefixes':delta_list(old.get('configPrefixes'),new.get('configPrefixes')),
        'generatorChanged':old.get('generatorContractSha256')!=new.get('generatorContractSha256'),
        'dbMigrations':{},'openApiOperations':{},
    }
    for vendor in sorted(set((old.get('dbMigrations') or {}))|set((new.get('dbMigrations') or {}))):
        result['dbMigrations'][vendor]=delta_list((old.get('dbMigrations') or {}).get(vendor), (new.get('dbMigrations') or {}).get(vendor))
    for app in sorted(set((old.get('openApiOperations') or {}))|set((new.get('openApiOperations') or {}))):
        result['openApiOperations'][app]=delta_list((old.get('openApiOperations') or {}).get(app), (new.get('openApiOperations') or {}).get(app))
    breaking=[]
    for section in ('publicStarters','publicApi','configPrefixes'):
        if result[section]['removed']: breaking.append(section)
    if any(row['removed'] for row in result['openApiOperations'].values()): breaking.append('openApiOperations')
    # Removed DB migration files are dangerous; added migrations are normal upgrade inputs.
    if any(row['removed'] for row in result['dbMigrations'].values()): breaking.append('dbMigrations')
    result['breakingCandidates']=breaking
    result['status']='BREAKING_REVIEW_REQUIRED' if breaking else 'COMPATIBLE_CANDIDATE'
    return result


def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--output'); ap.add_argument('--baseline'); ap.add_argument('--fail-on-breaking',action='store_true'); args=ap.parse_args()
    root=Path(args.root).resolve(); current=snapshot(root); payload={'current':current}
    rc=0
    if args.baseline:
        baseline=json.loads(Path(args.baseline).read_text(encoding='utf-8'))
        if 'current' in baseline: baseline=baseline['current']
        payload['comparison']=compare(baseline,current)
        if args.fail_on_breaking and payload['comparison']['breakingCandidates']: rc=1
    text=json.dumps(payload,ensure_ascii=False,indent=2)+'\n'
    if args.output:
        target=Path(args.output); target.parent.mkdir(parents=True,exist_ok=True); target.write_text(text,encoding='utf-8')
    else: print(text,end='')
    if args.baseline: print(f"CPF_UPGRADE_IMPACT={payload['comparison']['status']}")
    else: print(f"CPF_UPGRADE_SNAPSHOT=PASS publicApi={len(current['publicApi'])} starters={len(current['publicStarters'])}")
    return rc

if __name__=='__main__': raise SystemExit(main())
