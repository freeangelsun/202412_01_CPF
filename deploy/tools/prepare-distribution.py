#!/usr/bin/env python3
"""CPF 배포 JAR을 한 artifacts 디렉터리에 모으고 topology-aware manifest를 생성합니다.

빌드 자체는 각 Gradle/Jenkins 단계가 담당하고 이 도구는 배포 산출물 수집·해시·배치 계획만 소유합니다.
Generated Domain은 cpf-tools/generator/definitions와 실제 cpf-<domain> Root를 함께 확인해 자동 편입합니다.
"""
from __future__ import annotations
import argparse, hashlib, json, re, shutil
from pathlib import Path


def sha256(path: Path) -> str:
    h=hashlib.sha256()
    with path.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''): h.update(chunk)
    return h.hexdigest()


def load_json(path: Path): return json.loads(path.read_text(encoding='utf-8-sig'))


def yaml_scalar(text: str, key: str) -> str | None:
    m=re.search(rf'(?m)^\s*{re.escape(key)}:\s*([^#\r\n]+?)\s*$',text)
    return m.group(1).strip() if m else None


def choose_jar(patterns: list[Path]) -> Path | None:
    found=[]
    for pattern in patterns:
        found.extend(p for p in pattern.parent.glob(pattern.name) if p.is_file() and '-plain.jar' not in p.name)
    return sorted(found,key=lambda p:(p.stat().st_mtime_ns,p.name))[-1] if found else None


def target_group(topology: str, entry: dict) -> str:
    if topology=='single-node': return 'node-1'
    if topology=='custom': return entry.get('targetGroup') or entry.get('hostAlias') or entry['serviceName']
    is_batch=entry.get('kind')=='batch' or entry.get('runtimeRole') not in (None,'')
    is_generated=entry.get('generatedDomain') is True
    if topology=='split-batch': return 'batch' if is_batch else 'app'
    if topology=='split-online':
        if is_batch: return 'batch'
        if is_generated: return 'domain-online'
        return 'platform-online'
    return entry['serviceName']


def platform_candidates(root: Path, service: dict) -> list[Path]:
    name=service.get('artifactName') or service.get('serviceName')
    module=service.get('module')
    direct={
        'ADM':root/'cpf-admin/build/libs', 'BZA':root/'cpf-biz-admin/build/libs',
        'GWY':root/'cpf-gateway/build/libs', 'EDU':root/'cpf-education/build/libs'
    }.get(module)
    dirs=[direct] if direct else []
    if service.get('projectPath'):
        fragment=service['projectPath'].strip(':').replace(':','/')
        dirs.append(root/fragment/'build/libs')
    return [d/f'{name}*.jar' for d in dirs if d]


def generated_entries(root: Path, env: str) -> list[dict]:
    rows=[]; defs=root/'cpf-tools/generator/definitions'
    if not defs.is_dir(): return rows
    for definition in sorted(defs.glob('*/cpf-domain.yaml')):
        text=definition.read_text(encoding='utf-8-sig')
        name=yaml_scalar(text,'name'); system=yaml_scalar(text,'systemCode')
        project=root/f'cpf-{name}'
        if not name or not project.is_dir(): continue
        online=bool(re.search(r'(?m)^\s*online:\s*true\s*$',text))
        batch=bool(re.search(r'(?m)^\s*batch:\s*true\s*$',text))
        if online:
            rows.append({'module':system,'serviceName':f'cpf-{name}-online','generatedDomain':True,'kind':'online',
                         'profile':env,'artifactPatterns':[project/'online/build/libs'/f'cpf-{name}-online*.jar']})
        if batch:
            rows.append({'module':system,'serviceName':f'cpf-{name}-batch','generatedDomain':True,'kind':'batch',
                         'profile':env,'artifactPatterns':[project/'batch/build/libs'/f'cpf-{name}-batch*.jar']})
    return rows


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--env',choices=['local','dev','stg','prod'],required=True)
    ap.add_argument('--topology',choices=['single-node','split-online','split-batch','full-distributed','custom'],default='single-node')
    ap.add_argument('--output')
    ap.add_argument('--plan-only',action='store_true',help='JAR가 아직 없어도 배치 계획만 생성합니다.')
    ns=ap.parse_args(); root=Path(ns.root).resolve()
    inventory=root/f'deploy/environments/{ns.env}/inventory'
    candidates=sorted(inventory.glob('*.json'))
    if not candidates: raise SystemExit(f'inventory가 없습니다: {inventory}')
    source=load_json(candidates[0])
    rows=[]
    for service in source.get('services',[]):
        entry=dict(service); entry['generatedDomain']=False; entry['kind']='batch' if service.get('runtimeRole') else 'online'
        entry['artifactPatterns']=platform_candidates(root,service); rows.append(entry)
    rows.extend(generated_entries(root,ns.env))

    output=Path(ns.output).resolve() if ns.output else root/f'build/cpf-distribution/{ns.env}/{ns.topology}'
    artifacts=output/'artifacts'; artifacts.mkdir(parents=True,exist_ok=True)
    runtime_dir=output/'runtime'; runtime_dir.mkdir(parents=True,exist_ok=True)
    # Target 서버는 Source checkout 없이 distribution만 받아도 instance install/start/stop/status를 수행합니다.
    for tool in ('cpf-instance.py','cpf-instance.sh','cpf-instance.ps1'):
        source_tool=root/'deploy'/'tools'/tool
        if source_tool.is_file(): shutil.copy2(source_tool,runtime_dir/tool)
    manifest=[]; missing=[]; used_names=set()
    for row in rows:
        patterns=[Path(x) for x in row.pop('artifactPatterns')]
        jar=choose_jar(patterns)
        item={k:v for k,v in row.items() if k not in {'sshHostEnvKey','sshUserEnvKey'}}
        item['targetGroup']=target_group(ns.topology,item)
        item['sourcePatterns']=[str(p.relative_to(root)).replace('\\','/') for p in patterns]
        if jar is None:
            item['artifactStatus']='MISSING'; missing.append(item['serviceName'])
        else:
            name=jar.name
            if name in used_names: name=f"{item['serviceName']}--{name}"
            used_names.add(name); dst=artifacts/name; shutil.copy2(jar,dst)
            item.update({'artifactStatus':'READY','artifact':f'artifacts/{name}','sha256':sha256(dst),'size':dst.stat().st_size})
        manifest.append(item)
    result={'schemaVersion':1,'environment':ns.env,'topology':ns.topology,
            'artifactDirectory':'artifacts','runtimeDirectory':'runtime','services':manifest,'missingArtifacts':missing}
    output.mkdir(parents=True,exist_ok=True)
    (output/'deployment-manifest.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if missing and not ns.plan_only: raise SystemExit('배포 JAR 누락: '+', '.join(missing))
    print(output/'deployment-manifest.json')
    return 0

if __name__=='__main__': raise SystemExit(main())
