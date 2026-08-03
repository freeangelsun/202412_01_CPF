#!/usr/bin/env python3
"""Repository-wide module dependency/import owner boundary gate."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
class GateError(RuntimeError):pass
MODULE_OWNER={'cpf-core':'core','cpf-common':'common','cpf-admin':'admin','cpf-biz-admin':'biz-admin','cpf-batch':'batch','cpf-gateway':'gateway','cpf-member':'generated-domain','cpf-reference':'generated-domain','cpf-starters':'starters','cpf-tools':'tools'}
PROJECT=re.compile(r"project\(['\"]:([^'\"]+)['\"]\)");IMPORT=re.compile(r'(?m)^\s*import\s+([\w.]+)')
FORBIDDEN_DEP={('cpf-core','cpf-admin'),('cpf-core','cpf-biz-admin'),('cpf-core','cpf-batch'),('cpf-common','cpf-admin'),('cpf-common','cpf-biz-admin'),('cpf-common','cpf-batch'),('cpf-admin','cpf-biz-admin')}
def top(path,root):
 try:return path.relative_to(root).parts[0]
 except:return ''
def verify(root:Path):
 findings=[];graph={};build_files=[];java_files=[];internal_refs=[]
 settings=root/'settings.gradle'
 settings_kts=root/'settings.gradle.kts'
 if not settings.is_file() and not settings_kts.is_file():
  findings.append('repository settings.gradle/settings.gradle.kts is required; sparse snapshot cannot pass owner gate')
 if not (root/'build.gradle').is_file() and not (root/'build.gradle.kts').is_file():
  findings.append('root build.gradle/build.gradle.kts is required; sparse snapshot cannot pass owner gate')
 for b in sorted(root.rglob('build.gradle'))+sorted(root.rglob('build.gradle.kts')):
  if any(x in b.parts for x in ('build','.gradle','node_modules')):continue
  src=top(b,root);build_files.append(b.relative_to(root).as_posix());deps=[]
  text=b.read_text(encoding='utf-8-sig')
  for raw in PROJECT.findall(text):
   dest=raw.split(':',1)[0];deps.append(dest)
   if (src,dest) in FORBIDDEN_DEP:findings.append(f'{b.relative_to(root)}: forbidden dependency {src}->{dest}')
   if src!='cpf-batch' and dest=='cpf-batch':findings.append(f'{b.relative_to(root)}: non-BAT module depends on BAT runtime')
  graph[src]=sorted(set(graph.get(src,[])+deps))
 # cycle detection over top modules
 visiting=set();visited=set()
 def dfs(n,stack):
  if n in visiting:findings.append('dependency cycle: '+' -> '.join(stack+[n]));return
  if n in visited:return
  visiting.add(n)
  for d in graph.get(n,[]):dfs(d,stack+[n])
  visiting.remove(n);visited.add(n)
 for n in graph:dfs(n,[])
 for p in sorted(root.rglob('src/main/java/**/*.java')):
  if any(x in p.parts for x in ('build','generated')):continue
  src=top(p,root);text=p.read_text(encoding='utf-8-sig');java_files.append(p.relative_to(root).as_posix())
  for imp in IMPORT.findall(text):
   if '.internal.' in imp:
    owner=('cpf-'+imp.split('.')[2]) if imp.startswith('com.cpf.') and len(imp.split('.'))>2 else ''
    # any internal import from a different top owner is forbidden
    expected={'core':'cpf-core','common':'cpf-common','admin':'cpf-admin','batch':'cpf-batch','biz':'cpf-biz-admin'}.get(imp.split('.')[2] if imp.startswith('com.cpf.') else '',owner)
    if expected and expected!=src:
     internal_refs.append({'file':p.relative_to(root).as_posix(),'import':imp});findings.append(f'{p.relative_to(root)}: cross-owner internal import {imp}')
   if src=='cpf-core' and ('.admin.' in imp or '.batch.' in imp):findings.append(f'{p.relative_to(root)}: cpf-core owns admin/batch runtime import {imp}')
   if src!='cpf-batch' and re.search(r'com\.cpf\.batch\..*(internal|runtime)',imp):findings.append(f'{p.relative_to(root)}: BAT runtime implementation import {imp}')
 if len(build_files) < 3:
  findings.append(f'owner graph incomplete: expected repository module build files, found {len(build_files)}')
 if len(java_files) < 1:
  findings.append('owner graph incomplete: no main Java source scanned')
 result={'status':'PASS' if not findings else 'FAIL','moduleOwners':MODULE_OWNER,'buildFileCount':len(build_files),'mainJavaFileCount':len(java_files),'moduleGraph':graph,'internalReferenceCount':len(internal_refs),'findings':findings}
 if findings:raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
