#!/usr/bin/env python3
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse
from pathlib import Path

RETIRED_ROOTS = ('cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend')
GARBAGE_DIRS = {'__pycache__','.pytest_cache','node_modules','.gradle'}
GARBAGE_SUFFIXES = {'.pyc','.class'}
GENERATED_DOMAINS = ('cpf-member','cpf-external')
# cpf-source-state.py 의 GENERATED_PATH_MARKERS 와 같은 경계다.
GENERATED_EVIDENCE_PREFIX = 'cpf-docs/governance/development-harness/evidence/platform/current/generated/'
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
        # cpf-release/ 는 릴리즈 생성 산출물이며 canonical Source Identity(cpf-source-state.py 의
        # GENERATED_PARTS)도 제품 Source 로 계산하지 않는다. 릴리즈를 한 번 생성했는지에 따라
        # 같은 Source 가 PASS/FAIL 로 갈리면 Gate 가 비결정적이 된다.
        if rel.parts and rel.parts[0]=='cpf-release': continue
        # build/ 는 Gradle 출력이지 source 가 아니다. source-empty project 의 canonical compile
        # output(build/classes/java/main)은 IDE classpath 계약이 요구하는 정본 출력 위치이므로,
        # 여기까지 검사하면 Gradle 을 한 번 실행하는 것만으로 이 Gate 가 항상 실패한다.
        # 단 cpf-tools/build 는 Gradle plugin/BOM 제품 Source 라 계속 검사 대상이다.
        # cpf-tools/build/gradle-plugin/build/** 처럼 build 가 두 번 나오므로 모든 위치를 본다.
        if any(part=='build' and rel.parts[:i+1] != ('cpf-tools','build')
               for i,part in enumerate(rel.parts)): continue
        # canonical Source Identity 가 generated 로 분류하는 Harness platform 산출물(Gradle
        # project-cache 등)도 제품 Source 가 아니다.
        if rel.as_posix().startswith(GENERATED_EVIDENCE_PREFIX): continue
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
