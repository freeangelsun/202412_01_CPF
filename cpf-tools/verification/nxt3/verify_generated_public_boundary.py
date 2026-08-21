#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, re
from pathlib import Path

INTERNAL_REQUIRED={'cpf-starter-integration-http','cpf-starter-integration-resilience'}
PUBLIC_DIRECT_REQUIRED_API={'cpf-starter','cpf-starter-secure-api','cpf-starter-data-mybatis'}
BATCH_CAPABILITY_PUBLIC={'cpf-starter-batch'}

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--evidence',type=Path)
    ns=ap.parse_args(); root=ns.root.resolve(); checks=[]
    def add(name,ok,detail): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
    mods={m['artifactId']:m for m in catalog['modules']}
    for aid in sorted(INTERNAL_REQUIRED):
        m=mods.get(aid); add(f'catalog-{aid}-internal',bool(m and m.get('visibility')=='internal'),m or {})
    for aid in sorted(PUBLIC_DIRECT_REQUIRED_API|BATCH_CAPABILITY_PUBLIC):
        m=mods.get(aid); add(f'catalog-{aid}-public',bool(m and m.get('visibility')=='public'),m or {})
    profile_defs=catalog.get('profileDefinitions',{})
    for profile in ('secure-api','batch'):
        runtime=set(profile_defs.get(profile,{}).get('runtimeProjects',[]))
        needed={mods[x]['projectPath'] for x in INTERNAL_REQUIRED if x in mods}
        add(f'profile-{profile}-owns-integration-leaves',needed.issubset(runtime),sorted(runtime))
    profile_files={
        'secure-api':root/'cpf-starters/profiles/secure-api/build.gradle',
        'batch':root/'cpf-starters/profiles/batch-service/build.gradle',
    }
    for profile,path in profile_files.items():
        text=path.read_text(encoding='utf-8')
        missing=[x for x in INTERNAL_REQUIRED if x not in mods or f"implementation project('{mods[x]['projectPath']}')" not in text]
        add(f'profile-{profile}-build-composes-internal',not missing,{'file':str(path),'missingImplementationComposition':missing})
    # Generated Domain은 online을 필수 생성하고 definition에서 선택한 경우에만 batch를 생성한다. 두 Surface 모두 Public Starter/Profile만 직접 소비한다.
    for root_name in ('cpf-member','cpf-external'):
        path=root/root_name/'online/build.gradle'; text=path.read_text(encoding='utf-8')
        direct=set(re.findall(r'com\.cpf\.starter:([a-z0-9-]+):',text))
        forbidden=sorted(direct & INTERNAL_REQUIRED)
        missing=sorted(PUBLIC_DIRECT_REQUIRED_API-direct)
        add(f'{root_name}-online-public-direct-boundary',not forbidden and not missing,{'file':str(path),'direct':sorted(direct),'forbiddenInternal':forbidden,'missingRequiredPublic':missing})
        definition=root/root_name/'cpf-domain.yaml'
        batch_selected='batch: true' in definition.read_text(encoding='utf-8',errors='replace').lower()
        batch_dir=root/root_name/'batch'
        batch_exists=batch_dir.is_dir() and any(y.is_file() for y in batch_dir.rglob('*'))
        unexpected=[x for x in ('domain','jobpack') if (root/root_name/x).is_dir() and any(y.is_file() for y in (root/root_name/x).rglob('*'))]
        add(f'{root_name}-generated-optional-batch-selection',batch_exists==batch_selected,{'selected':batch_selected,'exists':batch_exists})
        add(f'{root_name}-generated-domain-jobpack-zero',not unexpected,unexpected)
        if batch_exists:
            batch_build=batch_dir/'build.gradle'; batch_text=batch_build.read_text(encoding='utf-8') if batch_build.is_file() else ''
            batch_direct=set(re.findall(r'com\.cpf\.starter:([a-z0-9-]+):',batch_text))
            add(f'{root_name}-batch-public-direct-boundary',not (batch_direct & INTERNAL_REQUIRED) and 'cpf-starter-batch' in batch_direct,{'direct':sorted(batch_direct)})
    batch_profile=root/'cpf-starters/profiles/batch-service/build.gradle'
    batch_text=batch_profile.read_text(encoding='utf-8') if batch_profile.is_file() else ''
    add('batch-capability-separate-public-profile',batch_profile.is_file() and 'cpf-starter-batch' in mods and mods['cpf-starter-batch'].get('visibility')=='public',str(batch_profile))
    failures=[c for c in checks if c['status']=='FAIL']
    result={'gate':'NXT3_GENERATED_PUBLIC_STARTER_BOUNDARY','status':'PASS' if not failures else 'FAIL','failedCount':len(failures),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failures else 0
if __name__=='__main__': raise SystemExit(main())
