#!/usr/bin/env python3
from pathlib import Path
import argparse, json, re, sys

CANONICAL_BASE='com.cpf.starter.data.persistence.mybatis'
STALE=('com.cpf.core.mapper.','com.cpf.starter.persistence.mybatis.','package com.cpf.common.config;','package com.cpf.core.config;')

def package_of(text:str):
    m=re.search(r'^package\s+([\w.]+);',text,re.M)
    return m.group(1) if m else None

def java_valid(text:str, base:str=CANONICAL_BASE)->bool:
    pkg=package_of(text)
    return bool(pkg and (pkg==base or pkg.startswith(base+'.')) and not any(s in text for s in STALE[:2]))

def validate(root:Path):
    module=root/'cpf-starters/data/persistence-mybatis'
    catalog_path=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    if not catalog_path.is_file(): return [f'catalog missing: {catalog_path.relative_to(root)}']
    catalog=json.loads(catalog_path.read_text(encoding='utf-8'))
    modules=catalog.get('modules',[]) if isinstance(catalog,dict) else catalog
    entry=next((x for x in modules if isinstance(x,dict) and (x.get('gradlePath')==':cpf-starter-data-persistence-mybatis' or x.get('projectPath')==':cpf-starter-data-persistence-mybatis')),None)
    if not entry: return ['catalog entry missing']
    base=entry.get('packageBase')
    if base!=CANONICAL_BASE: return [f'catalog packageBase={base!r}']
    viol=[]
    for p in module.rglob('*.java'):
        t=p.read_text(encoding='utf-8',errors='replace'); pkg=package_of(t)
        if not pkg or not (pkg==base or pkg.startswith(base+'.')):
            viol.append(f'{p.relative_to(root)} package={pkg or "<missing>"}')
        if 'com.cpf.core.mapper.' in t or 'com.cpf.starter.persistence.mybatis.' in t:
            viol.append(f'{p.relative_to(root)} stale implementation namespace reference')
    for p in module.rglob('*'):
        if p.is_file() and p.suffix in {'.xml','.properties','.yml','.yaml','.imports'}:
            t=p.read_text(encoding='utf-8',errors='replace')
            if 'com.cpf.core.mapper.' in t or 'com.cpf.starter.persistence.mybatis.' in t or 'com.cpf.core.config.CpfMyBatisConfig' in t:
                viol.append(f'{p.relative_to(root)} stale namespace')
    return viol

def mutation_self_test(root:Path):
    candidates=list((root/'cpf-starters/data/persistence-mybatis').rglob('*.java'))
    if not candidates: raise SystemExit('FAIL mutation self-test: no Java source')
    original=candidates[0].read_text(encoding='utf-8',errors='replace')
    if not java_valid(original): raise SystemExit('FAIL mutation self-test baseline invalid')
    mutated=re.sub(r'^package\s+[\w.]+;', 'package com.cpf.core.bad;', original, count=1, flags=re.M)
    if java_valid(mutated): raise SystemExit('FAIL mutation survived: wrong package owner accepted')
    stale=original.replace('import ', 'import com.cpf.core.mapper.Bad;\nimport ',1) if 'import ' in original else original+'\n// com.cpf.core.mapper.Bad\n'
    if java_valid(stale): raise SystemExit('FAIL mutation survived: stale core mapper namespace accepted')
    print('PASS persistence package owner mutation killed')

ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path('.')); ap.add_argument('--self-test',action='store_true'); args=ap.parse_args(); root=args.root.resolve()
viol=validate(root)
if viol:
    print('FAIL persistence package owner'); print('\n'.join(viol)); raise SystemExit(1)
if args.self_test: mutation_self_test(root)
else: print('PASS persistence package owner')
