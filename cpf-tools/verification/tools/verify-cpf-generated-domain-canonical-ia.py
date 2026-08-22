#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path

def parse_yaml_subset(path:Path):
    # 충분한 정적 판정용 key reader. Generator engine의 정본 입력과 충돌하지 않는다.
    text=path.read_text(encoding='utf-8-sig')
    def scalar(key):
        m=re.search(rf'(?m)^\s*{re.escape(key)}:\s*([^#\n]+)',text); return m.group(1).strip().strip('"\'') if m else ''
    name=''; m=re.search(r'(?ms)^domain:\s*\n(?:\s+.*\n)*?\s+name:\s*([A-Za-z0-9_-]+)',text); name=m.group(1) if m else ''
    package=''; m=re.search(r'(?m)^\s+packageName:\s*([^#\n]+)',text); package=m.group(1).strip() if m else name.replace('-','')
    mode=''; m=re.search(r'(?ms)^generation:\s*\n(?:\s+.*\n)*?\s+mode:\s*([A-Za-z0-9_-]+)',text); mode=m.group(1) if m else ''
    feats=[]; fm=re.search(r'(?ms)^businessFeatures:\s*\n((?:\s+-\s*[^\n]+\n?)+)',text)
    if fm: feats=[x.strip().split('#',1)[0].strip() for x in re.findall(r'(?m)^\s+-\s*([^\n]+)',fm.group(1))]
    return name,package,mode,feats

def scan(root:Path):
    findings=[]; details=[]
    for definition in sorted(root.glob('cpf-*/cpf-domain.yaml')):
        name,pkg,mode,features=parse_yaml_subset(definition)
        if mode.lower()=='prebuilt': continue
        if not name: findings.append(f'{definition}:domain-name-missing'); continue
        domain_root=definition.parent
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
