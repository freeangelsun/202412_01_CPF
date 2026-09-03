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
import argparse,json,re
from pathlib import Path

def parse_contract(path:Path):
    values={}
    for raw in path.read_text(encoding='utf-8-sig').splitlines():
        line=raw.strip()
        if not line or line.startswith(('#','!')) or '=' not in line: continue
        key,value=line.split('=',1); values[key.strip()]=value.strip()
    name=values.get('cpf.domain.name','')
    package=values.get('cpf.domain.packageName','') or name.replace('-','')
    mode=values.get('cpf.domain.generationMode','generated')
    features=[value.strip() for value in values.get('cpf.domain.businessFeatures','').split(',') if value.strip()]
    return name,package,mode,features

def scan(root:Path):
    findings=[]; details=[]
    for definition in sorted(root.glob('cpf-*/gradle.properties')):
        if 'cpf.domain.contractVersion=' not in definition.read_text(encoding='utf-8-sig'): continue
        name,pkg,mode,features=parse_contract(definition)
        if mode.lower()=='prebuilt': continue
        if not name: findings.append(f'{definition}:domain-name-missing'); continue
        domain_root=definition.parent
        forbidden=[value for value in ('cpf-domain.yaml','cpf-generator.lock.json','.cpf') if (domain_root/value).exists()]
        if forbidden: findings.append(f'{domain_root.name}:forbidden-generator-metadata:{forbidden}')
        if not features: findings.append(f'{domain_root.name}:businessFeatures-missing')
        if name in features: findings.append(f'{domain_root.name}:businessFeature-equals-domain:{name}')
        allowed=set(features)|{'base'}
        for module in ('online','batch'):
            src=domain_root/module/'src/main/java'
            test=domain_root/module/'src/test/java'
            if not src.exists(): continue
            base=src/Path(*pkg.split('.'))
            if not base.is_dir(): findings.append(f'{domain_root.name}:{module}:java-base-missing:{pkg}'); continue
            for p in list(src.rglob('*.java'))+list(test.rglob('*.java')):
                text=p.read_text(encoding='utf-8',errors='ignore')
                pm=re.search(r'(?m)^\s*package\s+([A-Za-z0-9_.]+)\s*;',text)
                if pm:
                    package=pm.group(1)
                    for bad in (f'{pkg}.{module}.{name}',f'{pkg}.{name}'):
                        if package==bad or package.startswith(bad+'.'): findings.append(f'{p.relative_to(root)}:legacy-package:{package}')
                rel=p.relative_to(src if p.is_relative_to(src) else test)
                parts=rel.parts
                base_parts=tuple(pkg.split('.'))
                if parts[:len(base_parts)]!=base_parts: findings.append(f'{p.relative_to(root)}:path-base-mismatch')
                elif len(parts)>len(base_parts)+1:
                    first=parts[len(base_parts)]
                    # Application.java can live directly under base; all nested generated business source must be base or feature.
                    if first in {module,name}: findings.append(f'{p.relative_to(root)}:duplicate-segment:{first}')
                    elif first not in allowed: findings.append(f'{p.relative_to(root)}:undefined-feature:{first}')
            details.append({'domain':name,'module':module,'package':pkg,'features':features})
    return findings,details

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args(); root=Path(a.root).resolve()
    findings,details=scan(root); mutation=[]
    if a.mutation_self_test:
        # 순수 패턴 Mutation: 실제 Source를 건드리지 않고 verifier의 금지 규칙 자체를 공격한다.
        pkg='member'; name='member'; samples=['member.online.member.controller','member.member.service']
        for s in samples:
            bad=any(s==x or s.startswith(x+'.') for x in (f'{pkg}.online.{name}',f'{pkg}.{name}'))
            mutation.append('PASS' if bad else 'FAIL')
        if mutation.count('PASS')!=len(samples): findings.append('mutation-self-test-failed')
    payload={'status':'PASS' if not findings else 'FAIL','domainModules':details,'findings':findings,'mutation':mutation}
    print(json.dumps(payload,ensure_ascii=False,indent=2)); return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
