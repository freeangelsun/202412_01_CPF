#!/usr/bin/env python3
from __future__ import annotations
import argparse
from pathlib import Path

RETIRED_ROOTS = ('cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend')
GARBAGE_DIRS = {'__pycache__','.pytest_cache','node_modules','.gradle'}
GARBAGE_SUFFIXES = {'.pyc','.class'}
GENERATED_DOMAINS = ('cpf-member','cpf-external')
LAYER_FIRST = ('online/controller','online/service','online/repository','online/dto','online/domaincall','domain/audit','domain/mapper','domain/policy','domain/repository')

def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.')
    root=Path(ap.parse_args().root).resolve(); fail=[]
    for name in RETIRED_ROOTS:
        if (root/name).exists(): fail.append(f'retired product root exists: {name}')
    for domain in GENERATED_DOMAINS:
        p=root/domain
        if not p.exists(): continue
        if (p/'db').exists(): fail.append(f'generated domain owns forbidden DB vendor tree: {domain}/db')
        for rel in LAYER_FIRST:
            if (p/rel).exists(): fail.append(f'generated domain layer-first path exists: {domain}/{rel}')
    for p in root.rglob('*'):
        rel=p.relative_to(root)
        if p.is_dir():
            if p.name in GARBAGE_DIRS: fail.append(f'garbage directory: {rel.as_posix()}')
            try:
                if not any(p.iterdir()): fail.append(f'empty directory: {rel.as_posix()}')
            except OSError: pass
        elif p.is_file() and p.suffix.lower() in GARBAGE_SUFFIXES:
            fail.append(f'compiled/cache file: {rel.as_posix()}')
    # cpf-tools/build is product source. Only compiled Gradle-plugin output is forbidden.
    compiled=root/'cpf-tools/build/gradle-plugin/bin'
    if compiled.exists(): fail.append('compiled Gradle plugin bin directory exists')
    if fail:
        print('CPF_CLEAN_SOURCE_TREE=FAIL')
        for x in fail[:200]: print(' - '+x)
        print(f'failures={len(fail)}')
        return 1
    files=sum(1 for p in root.rglob('*') if p.is_file())
    print(f'CPF_CLEAN_SOURCE_TREE=PASS files={files} retiredRoots=0 generatedDbTrees=0 layerFirst=0 garbage=0 emptyDirs=0')
    return 0
if __name__=='__main__': raise SystemExit(main())
