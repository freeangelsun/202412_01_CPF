#!/usr/bin/env python3
from __future__ import annotations
import json, re, sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
FAIL=[]

def load(rel):
    return json.loads((ROOT/rel).read_text(encoding='utf-8-sig'))

def fail(msg): FAIL.append(msg)

starter=load('cpf-tools/generator/contracts/cpf-starter-catalog.json')
cap_rows=starter.get('developerCapabilities',[])
cap_ids=[str(x.get('id','')).strip() for x in cap_rows]
required={'WEB','PERSISTENCE','TRANSACTION','SECURITY','LOGGING_AUDIT','MESSAGING','INTEGRATION','CACHE','BATCH','OBSERVABILITY','CONFIG_COMMON'}
if len(cap_ids)!=len(set(cap_ids)): fail(f'duplicate developer capability ids: {cap_ids}')
if not required.issubset(set(cap_ids)): fail(f'missing developer capabilities: {sorted(required-set(cap_ids))}')
mods=starter.get('modules',[])
projects=[]; artifacts=[]
for m in mods:
    project=str(m.get('projectPath','')); artifact=str(m.get('artifactId','')); cap=str(m.get('developerCapability','')); role=str(m.get('canonicalRole','')); vis=str(m.get('visibility',''))
    projects.append(project); artifacts.append(artifact)
    if cap not in cap_ids: fail(f'{project}: invalid developerCapability={cap}')
    expected='PUBLIC_CAPABILITY_GROUP_OWNER' if vis=='public' else 'INTERNAL_LEAF_FOUNDATION'
    if role!=expected: fail(f'{project}: canonicalRole={role}, expected={expected}')
if len(projects)!=len(set(projects)): fail('duplicate module projectPath')
if len(artifacts)!=len(set(artifacts)): fail('duplicate module artifactId')

cmd=load('cpf-tools/runtime/cli/contracts/cpf-command-catalog.json')
commands={str(x.get('command','')).strip() for x in cmd.get('publicCommands',[])}
required_commands={'bootstrap','domain-new','domain-sync','build','test','run','stop','reset','status','doctor','version','help'}
if not required_commands.issubset(commands): fail(f'missing public commands: {sorted(required_commands-commands)}')

for rel in ['cpf-tools/release/open-git/templates/build.gradle','cpf-tools/release/public/templates/build.gradle']:
    text=(ROOT/rel).read_text(encoding='utf-8')
    for group in ['CPF Build','CPF Test','CPF Domain','CPF Database','CPF Runtime','CPF Verification','CPF Publication','CPF Configuration/Discovery']:
        if group not in text: fail(f'{rel}: missing Gradle group {group}')
    if "tasks.named('build')" not in text or "dependsOn 'cpfBuild'" not in text: fail(f'{rel}: root build parity missing')
    if "tasks.register('test')" not in text or "dependsOn 'cpfTest'" not in text: fail(f'{rel}: root test parity missing')

for rel in ['settings.gradle','cpf-tools/release/open-git/templates/settings.gradle','cpf-tools/release/public/templates/settings.gradle']:
    text=(ROOT/rel).read_text(encoding='utf-8')
    if 'cpf.domain.contractVersion' not in text: fail(f'{rel}: Developer Contract discovery missing')

policy=load('cpf-tools/release/public/cpf-public-surface-policy.json')
if policy.get('mandatoryDomainProjects'): fail('public surface has mandatoryDomainProjects')
for rule in policy.get('sourceRules',[]):
    pattern=str(rule.get('pattern',''))
    if pattern.startswith(('cpf-member/','cpf-external/')): fail(f'fixed Domain source rule: {pattern}')
if int(policy.get('domainDiscovery',{}).get('minimumSelectedDomains',-1))!=0: fail('public domain minimum must be 0')

final=load('cpf-tools/release/cpf-final-artifact-catalog.json')
for a in final.get('artifacts',[]):
    if a.get('kind')=='generated-domain' or a.get('artifactId') in {'cpf-member','cpf-external'}: fail(f'static generated-domain artifact: {a.get("artifactId")}')
    if a.get('artifactId') in {'cpf-backoffice','cpf-backoffice-web'} and a.get('presencePolicy')!='OPTIONAL_IF_OWNER_PRESENT': fail(f'optional backoffice presence policy missing: {a.get("artifactId")}')
projection=final.get('generatedDomainProjection',{})
if projection.get('contractVersion')!='1' or projection.get('minimumSelectedDomains')!=0: fail('final artifact dynamic Domain projection missing')

root_settings=(ROOT/'settings.gradle').read_text(encoding='utf-8')
if "include ':apps:backoffice-web'" in root_settings and 'cpfBackofficeWebPresent' not in root_settings: fail('backoffice-web is unconditional')

# Public BOM must not expose internal starter artifacts.
bom_files=list(ROOT.glob('**/*bom*/**/*.gradle'))+list(ROOT.glob('**/*bom*/**/*.gradle.kts'))
internal_artifacts={str(m.get('artifactId')) for m in mods if m.get('visibility')!='public'}
for p in bom_files:
    try: text=p.read_text(encoding='utf-8')
    except Exception: continue
    for aid in internal_artifacts:
        if aid and re.search(rf"['\"]{re.escape(aid)}['\"]",text): fail(f'internal artifact exposed from BOM candidate {p.relative_to(ROOT)}: {aid}')

if FAIL:
    print(json.dumps({'status':'FAIL','failureCount':len(FAIL),'failures':FAIL},ensure_ascii=False,indent=2)); sys.exit(1)
print(json.dumps({'status':'PASS','capabilityCount':len(cap_ids),'moduleCount':len(mods),'publicCommandCount':len(required_commands),'dynamicDomainMin':0,'duplicateProjectCount':0,'duplicateArtifactCount':0},ensure_ascii=False))
