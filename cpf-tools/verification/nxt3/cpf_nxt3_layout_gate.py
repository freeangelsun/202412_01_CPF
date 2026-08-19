#!/usr/bin/env python3
"""CPF NXT3 Tools/Deploy/Batch/Hygiene physical-layout Gate.

삭제 전 상태도 검증할 수 있도록 legacy 디렉터리 자체의 존재가 아니라, 그 안의 모든 파일이
Root-relative file-only Delete Manifest에 등록됐는지와 active stale reference 0을 판정한다.
"""
from __future__ import annotations
import argparse,csv,json,re
from pathlib import Path

TOOLS_ALLOWED={'build','contracts','db','environment','generator','governance','release','runtime','security','supply-chain','testing','verification'}
TOOLS_LEGACY={'config','performance','product-governance','promotion','runtime-alternatives','scripts','analysis'}
BATCH_TARGET={'api','runtime','scheduler','worker','control-plane','center-cut','agent','runtime-support','testkit'}
BATCH_OLD={'contract','execution-runtime','control-server','center-cut-runner','host-agent','runtime-common'}
PROTECTED=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
OLD_STRINGS=['cpf-batch-control-server','cpf-batch-host-agent','cpf-batch-runtime-common','cpf-batch-center-cut-runner',':runtime:batch:control-server',':runtime:batch:host-agent',':runtime:batch:runtime-common',':runtime:batch:center-cut-runner']
TEXT_EXT={'.java','.kt','.gradle','.kts','.json','.yaml','.yml','.xml','.properties','.md','.ps1','.sh','.py','.sql','.csv','.txt','.env'}
LEGACY_ACTIVE_PREFIXES=tuple([f'cpf-tools/{x}/' for x in TOOLS_LEGACY]+[f'cpf-batch/{x}/' for x in BATCH_OLD]+['deploy/env/','deploy/local/','deploy/cells/','deploy/inventory/','deploy/runtimes/batch/','deploy/schemas/','cpf-reference/'])

class Gate:
 def __init__(self): self.checks=[]
 def check(self,name,ok,detail=''): self.checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
 def result(self):
  fail=[x for x in self.checks if x['status']=='FAIL']; return {'status':'PASS' if not fail else 'FAIL','pass':len(self.checks)-len(fail),'fail':len(fail),'checks':self.checks}

def read_manifest(path:Path):
 if not path.exists(): return set()
 if path.suffix.lower()=='.csv':
  with path.open(encoding='utf-8-sig',newline='') as f:
   return {(row.get('path') or '').replace('\\','/').strip('/') for row in csv.DictReader(f) if (row.get('path') or '').strip()}
 return {line.strip().replace('\\','/').strip('/') for line in path.read_text(encoding='utf-8-sig').splitlines() if line.strip() and not line.lstrip().startswith('#')}

def active_text_files(root:Path):
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in TEXT_EXT: continue
  rel=p.relative_to(root).as_posix()
  if rel.startswith('.git/') or '/build/' in '/'+rel or rel.startswith(('cpf-docs/','cpf-tools/verification/nxt3/')): continue
  if rel == 'cpf-tools/runtime/metadata/bat-runtime-role-contract.json': continue
  if any(rel.startswith(x) for x in LEGACY_ACTIVE_PREFIXES): continue
  yield p

def files_under(root:Path,relroot:str):
 p=root/relroot
 return [x.relative_to(root).as_posix() for x in p.rglob('*') if x.is_file()] if p.exists() else []

def main(argv=None):
 ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); ap.add_argument('--delete-manifest',default='cpf-docs/deliverables/DELETE_MANIFEST.csv'); ap.add_argument('--garbage-ledger',default='cpf-docs/work/GARBAGE_SWEEP_DECISIONS.csv'); a=ap.parse_args(argv)
 root=Path(a.root).resolve(); g=Gate(); dm=root/a.delete_manifest; manifest=read_manifest(dm)
 g.check('DELETE_MANIFEST_PRESENT',dm.exists(),a.delete_manifest)
 dir_rows=[p for p in manifest if (root/p).exists() and (root/p).is_dir()]
 g.check('DELETE_MANIFEST_FILE_ONLY',not dir_rows,','.join(sorted(dir_rows)))
 protected=[p for p in manifest if any(p==x.rstrip('/') or p.startswith(x) for x in PROTECTED)]
 g.check('DELETE_MANIFEST_PROTECTED_ZERO',not protected,','.join(sorted(protected)))

 tools=root/'cpf-tools'; dirs={p.name for p in tools.iterdir() if p.is_dir()} if tools.exists() else set()
 g.check('TOOLS_REQUIRED_OWNERS',TOOLS_ALLOWED<=dirs,'missing='+','.join(sorted(TOOLS_ALLOWED-dirs)))
 for old in sorted(TOOLS_LEGACY):
  files=files_under(root,'cpf-tools/'+old); unmanifested=[x for x in files if x not in manifest]
  g.check('TOOLS_LEGACY_FILES_MANIFESTED_'+old.upper().replace('-','_'),not unmanifested,','.join(unmanifested[:20]))
 g.check('TOOLS_RUNTIME_PROFILES',(tools/'runtime/profiles').exists())
 g.check('TOOLS_WRONG_RUNTIME_ALTERNATIVES_TARGET',not (tools/'runtime/alternatives').exists())
 g.check('TOOLS_CANONICAL_STARTER_CATALOG',(tools/'generator/contracts/cpf-starter-catalog.json').exists())
 stale_catalog='cpf-tools/config/cpf-starter-catalog.json'
 if (root/stale_catalog).exists(): g.check('TOOLS_STALE_CATALOG_DELETE_DECISION',stale_catalog in manifest)

 dep=root/'deploy'
 for env in ('local','dev','stg','prod'):
  for part in ('services','inventory','topology'):
   g.check(f'DEPLOY_{env.upper()}_{part.upper()}',(dep/'environments'/env/part).exists())
 g.check('DEPLOY_RUNTIMES_PLATFORM',(dep/'runtimes/platform').exists())
 g.check('DEPLOY_RUNTIMES_BATCH',(dep/'runtimes/batch').exists())
 g.check('DEPLOY_CI_RETAINED',(dep/'ci').exists())
 g.check('DEPLOY_SCHEMAS',(dep/'schemas').exists())
 for old in ('env','local','cells','inventory','batch','schema'):
  files=files_under(root,'deploy/'+old); unmanifested=[x for x in files if x not in manifest]
  g.check('DEPLOY_LEGACY_FILES_MANIFESTED_'+old.upper(),not unmanifested,','.join(unmanifested[:20]))
 # 실제 환경별 파일 라우팅 검증
 for p in (dep/'environments').glob('*') if (dep/'environments').exists() else []:
  if not p.is_dir() or p.name not in {'local','dev','stg','prod'}: continue
  for f in (p/'services').glob('*') if (p/'services').exists() else []:
   if f.is_file() and re.match(r'^(local|dev|stg|prod)-',f.name,re.I): g.check('DEPLOY_SERVICE_ENV_'+f.name.upper(),f.name.lower().startswith(p.name+'-'))
  for f in (p/'inventory').glob('*') if (p/'inventory').exists() else []:
   if f.is_file() and re.match(r'^(local|dev|stg|prod)-',f.name,re.I): g.check('DEPLOY_INVENTORY_ENV_'+f.name.upper(),f.name.lower().startswith(p.name+'-'))

 b=root/'cpf-batch'; bdirs={p.name for p in b.iterdir() if p.is_dir()} if b.exists() else set()
 g.check('BATCH_EXACT_TARGET_DIRS',BATCH_TARGET<=bdirs,'missing='+','.join(sorted(BATCH_TARGET-bdirs)))
 for old in sorted(BATCH_OLD):
  files=files_under(root,'cpf-batch/'+old); unmanifested=[x for x in files if x not in manifest]
  g.check('BATCH_LEGACY_FILES_MANIFESTED_'+old.upper().replace('-','_'),not unmanifested,','.join(unmanifested[:20]))
 corpus=[]
 for p in active_text_files(root):
  try: corpus.append((p.relative_to(root).as_posix(),p.read_text(encoding='utf-8',errors='ignore')))
  except Exception: pass
 text='\n'.join(t for _,t in corpus)
 for old in OLD_STRINGS: g.check('BATCH_STALE_'+re.sub(r'\W+','_',old).strip('_').upper(),old not in text)
 oldcat_hits=[rel for rel,t in corpus if 'cpf-tools/config/cpf-starter-catalog.json' in t]
 g.check('STALE_CATALOG_ACTIVE_REFERENCE_ZERO',not oldcat_hits,','.join(oldcat_hits))
 g.check('OLD_CURRENTIZER_NOT_ACTIVE',not (root/'cpf-tools/verification/nxt3/currentize_nxt3_layout.py').exists())

 g.check('EDUCATION_ROOT_PRESENT',(root/'cpf-education').exists())
 ref_files=files_under(root,'cpf-reference')
 g.check('REFERENCE_ROOT_ABSENT',not ref_files,','.join(ref_files[:20]))
 deploy_reference=[x for x in files_under(root,'deploy') if 'cpf-reference' in x.lower()]
 g.check('DEPLOY_REFERENCE_ZERO',not deploy_reference,','.join(deploy_reference[:20]))
 for domain in ['cpf-member','cpf-external']:
  d=root/domain; logical=domain.removeprefix('cpf-'); definition=root/'cpf-tools/generator/definitions'/logical/'cpf-domain.yaml'; g.check('GEN_ROOT_'+domain.upper(),d.exists() and definition.exists() and not (d/'.cpf').exists())
  if d.exists():
   t='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in d.rglob('build.gradle'))
   g.check('GEN_PUBLIC_ONLY_'+domain.upper(), 'cpf-starter-integration-http' not in t and 'cpf-starter-integration-resilience' not in t)
 ign=(root/'.gitignore').read_text(encoding='utf-8',errors='ignore') if (root/'.gitignore').exists() else ''
 g.check('GENERATED_BUILD_IGNORED','/cpf-*/**/build/' in ign and '/cpf-*/**/.gradle/' in ign)
 gl=root/a.garbage_ledger; g.check('GARBAGE_LEDGER_PRESENT',gl.exists(),a.garbage_ledger)
 if gl.exists():
  with gl.open(encoding='utf-8-sig',newline='') as f: grows=list(csv.DictReader(f))
  decided={x.get('path','').replace('\\','/').strip('/') for x in grows}
  missing=[x for x in manifest if x not in decided]
  g.check('DELETE_DECISIONS_COVER_MANIFEST',not missing,','.join(missing[:30]))
 result=g.result(); print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if result['status']=='PASS' else 2
if __name__=='__main__': raise SystemExit(main())
