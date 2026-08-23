#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re
from pathlib import Path

PUBLIC_BASE={'cpf-starter','cpf-starter-secure-api'}
OPTIONAL_PUBLIC={'cpf-starter-integration-http','cpf-starter-integration-resilience'}

def properties(path:Path)->dict[str,str]:
    values={}
    if not path.is_file(): return values
    for raw in path.read_text(encoding='utf-8-sig').splitlines():
        line=raw.strip()
        if line and not line.startswith(('#','!')) and '=' in line:
            key,value=line.split('=',1); values[key.strip()]=value.strip()
    return values

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--evidence',type=Path)
    ns=ap.parse_args(); root=ns.root.resolve(); checks=[]
    def add(name,ok,detail): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
    mods={m['artifactId']:m for m in catalog['modules']}
    project_to_module={m.get('projectPath'):m for m in catalog['modules'] if m.get('projectPath')}
    # HTTP/Resilience are explicit, independently selectable Public providers. They must never be silently hidden in a Profile.
    for aid in sorted(OPTIONAL_PUBLIC|PUBLIC_BASE|{'cpf-starter-batch'}):
        m=mods.get(aid); add(f'catalog-{aid}-public',bool(m and m.get('visibility')=='public'),m or {})
    profile_defs=catalog.get('profileDefinitions',{})
    optional_projects={mods[x]['projectPath'] for x in OPTIONAL_PUBLIC if x in mods}
    for profile in ('secure-api','event','batch'):
        runtime=set(profile_defs.get(profile,{}).get('runtimeProjects',[]))
        hidden=sorted(runtime & optional_projects)
        add(f'profile-{profile}-optional-provider-zero',not hidden,{'runtimeProjects':sorted(runtime),'hiddenOptional':hidden})
    # Physical profile Gradle composition must agree with the catalog and must not reintroduce optional providers.
    for profile,path in {
        'secure-api':root/'cpf-starters/profiles/secure-api/build.gradle',
        'event':root/'cpf-starters/profiles/event-service/build.gradle',
        'batch':root/'cpf-starters/profiles/batch-service/build.gradle',
    }.items():
        text=path.read_text(encoding='utf-8') if path.is_file() else ''
        actual=set(re.findall(r"project\(\s*['\"]([^'\"]+)['\"]\s*\)",text))
        expected=set(profile_defs.get(profile,{}).get('runtimeProjects',[]))
        hidden=sorted(actual & optional_projects)
        add(f'profile-{profile}-physical-composition',path.is_file() and actual==expected and not hidden,
            {'file':str(path),'expected':sorted(expected),'actual':sorted(actual),'hiddenOptional':hidden})
    # Generated Customer Domain may directly consume only Public Starter artifacts; physical :internal:* project names are never exposed.
    for root_name in ('cpf-member','cpf-external'):
        domain_root=root/root_name; definition=domain_root/'gradle.properties'; contract=properties(definition)
        generation=contract.get('cpf.domain.generationMode','')
        add(f'{root_name}-developer-contract',contract.get('cpf.domain.contractVersion')=='1',str(definition))
        forbidden=[name for name in ('cpf-domain.yaml','cpf-generator.lock.json','.cpf') if (domain_root/name).exists()]
        add(f'{root_name}-generator-metadata-zero',not forbidden,forbidden)
        if generation!='generated':
            add(f'{root_name}-generated-mode',False,{'generation':generation}); continue
        for module in ('online','batch'):
            build=root/root_name/module/'build.gradle'
            if not build.is_file():
                if module=='online': add(f'{root_name}-{module}-exists',False,str(build))
                continue
            text=build.read_text(encoding='utf-8')
            direct=set(re.findall(r'com\.cpf\.starter:([a-z0-9-]+):',text))
            internal_project=sorted(set(re.findall(r"project\(\s*['\"]([^'\"]+)['\"]\s*\)",text)))
            unknown=sorted(a for a in direct if a not in mods)
            nonpublic=sorted(a for a in direct if a in mods and mods[a].get('visibility')!='public')
            required={'cpf-starter','cpf-starter-secure-api'} if module=='online' else {'cpf-starter','cpf-starter-batch'}
            missing=sorted(required-direct)
            add(f'{root_name}-{module}-public-direct-boundary',not unknown and not nonpublic and not internal_project and not missing,
                {'direct':sorted(direct),'unknown':unknown,'nonPublic':nonpublic,'projectDependencies':internal_project,'missingRequired':missing})
    failures=[c for c in checks if c['status']=='FAIL']
    result={'gate':'NXT3_GENERATED_PUBLIC_STARTER_BOUNDARY','status':'PASS' if not failures else 'FAIL','failedCount':len(failures),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failures else 0
if __name__=='__main__': raise SystemExit(main())
