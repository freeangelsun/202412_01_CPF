#!/usr/bin/env python3
"""Physical starter/catalog/package/publication truth gate with runtime Git HEAD evidence."""
from __future__ import annotations
import argparse,json,re,subprocess,sys
from pathlib import Path
class GateError(RuntimeError):pass
def git(root,*a):
 p=subprocess.run(['git','-C',str(root),*a],capture_output=True,text=True)
 if p.returncode:raise GateError(f"git {' '.join(a)} failed: {p.stderr.strip()}")
 return p.stdout.strip()
def verify(root:Path,expected_sha=None,require_clean=False):
 head=git(root,'rev-parse','HEAD');status=git(root,'status','--porcelain')
 if expected_sha and expected_sha!=head:raise GateError(f'HEAD mismatch expected={expected_sha} actual={head}')
 if require_clean and status:raise GateError('working tree is not clean')
 p=root/'cpf-tools/config/cpf-starter-catalog.json';c=json.loads(p.read_text(encoding='utf-8-sig'))
 findings=[]
 if c.get('baselinePolicy')!='GIT_HEAD_RUNTIME' or c.get('baselineSha')!='RUNTIME_GIT_HEAD':findings.append('catalog must use runtime Git HEAD policy; stale static baseline forbidden')
 modules=c.get('modules') or c.get('starters') or []
 if not isinstance(modules,list) or not modules:findings.append('catalog module list is empty')
 checked=[]
 for i,m in enumerate(modules):
  if not isinstance(m,dict):findings.append(f'module[{i}] invalid');continue
  owner=m.get('ownerPath') or m.get('physicalPath') or m.get('path');package=m.get('packageBase') or m.get('basePackage')
  if not owner:findings.append(f'module[{i}] ownerPath missing');continue
  d=root/owner
  if not d.exists():findings.append(f'{owner}: physical module missing');continue
  if not (d/'build.gradle').is_file() and not (d/'build.gradle.kts').is_file():findings.append(f'{owner}: build file missing')
  if package:
   java=list(d.rglob('src/main/java/**/*.java'))
   declarations=[]
   for f in java:
    mm=re.search(r'(?m)^\s*package\s+([\w.]+)\s*;',f.read_text(encoding='utf-8-sig',errors='replace'))
    if mm:declarations.append(mm.group(1))
   if declarations and not any(x==package or x.startswith(package+'.') for x in declarations):findings.append(f'{owner}: packageBase={package} has no physical package declaration')
  if owner == 'cpf-starters/data/persistence-jdbc':
   imports_file=d/'src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports'
   if not imports_file.is_file(): findings.append(f'{owner}: AutoConfiguration.imports missing')
   else:
    imports_text=imports_file.read_text(encoding='utf-8-sig')
    if 'com.cpf.starter.persistence.jdbc' in imports_text: findings.append(f'{owner}: legacy package remains in AutoConfiguration.imports')
    for fqcn in [x.strip() for x in imports_text.splitlines() if x.strip() and not x.strip().startswith('#')]:
     if fqcn.startswith('com.cpf.starter.data.persistence.jdbc'):
      java=d/'src/main/java'/Path(*fqcn.split('.')).with_suffix('.java')
      # owner root already contains com/cpf/...; strip duplicated package prefix path base.
      java=d/'src/main/java'/Path(*fqcn.split('.')).with_suffix('.java')
      if not java.is_file(): findings.append(f'{owner}: auto-configuration target missing {fqcn}')
   for java in d.rglob('src/main/java/**/*.java'):
    if 'package com.cpf.starter.persistence.jdbc' in java.read_text(encoding='utf-8-sig',errors='replace'):
     findings.append(f'{java.relative_to(root)}: legacy persistence package remains')
  checked.append(owner)
 result={'status':'PASS' if not findings else 'FAIL','verifiedAgainstSha':head,'workingTreeClean':not bool(status),'catalogRevision':c.get('catalogRevision'),'moduleCount':len(modules),'checkedOwnerPaths':checked,'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--expected-sha');p.add_argument('--require-clean',action='store_true');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root,a.expected_sha,a.require_clean);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
