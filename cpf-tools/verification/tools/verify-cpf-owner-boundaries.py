#!/usr/bin/env python3
"""Repository-wide module dependency and Java internal-package owner boundary gate."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path

class GateError(RuntimeError): pass
PROJECT_DECL=re.compile(r"(?m)^\s*([A-Za-z][A-Za-z0-9_]*)\s*(?:\(\s*)?project\(['\"]:([^'\"]+)['\"]\)")
PROJECT_DIR=re.compile(r"project\(['\"](?P<logical>:[^'\"]+)['\"]\)\.projectDir\s*=\s*file\(['\"](?P<physical>[^'\"]+)['\"]\)")
PACKAGE=re.compile(r'(?m)^\s*package\s+([A-Za-z_][A-Za-z0-9_.]*)\s*;')
INTERNAL_IMPORT=re.compile(r'(?m)^\s*import\s+(?:static\s+)?(com\.cpf\.[A-Za-z0-9_.]+\.internal(?:\.[A-Za-z0-9_.*]+)?)\s*;')
INTERNAL_FQCN=re.compile(r'\b(com\.cpf\.[A-Za-z0-9_.]+\.internal(?:\.[A-Za-z0-9_]+)+)\b')
FORBIDDEN_DEP={
 ('cpf-core','cpf-admin'),('cpf-core','cpf-backoffice/online'),('cpf-core','cpf-batch'),('cpf-core','cpf-gateway'),('cpf-core','cpf-member'),('cpf-core','cpf-education'),('cpf-core','cpf-starters'),
 ('cpf-admin','cpf-backoffice/online'),
}
SKIP={'build','.gradle','node_modules','generated','.git','__pycache__'}

def top(path:Path,root:Path)->str:
 try:return path.relative_to(root).parts[0]
 except:return ''

def java_module(path:Path,root:Path)->str:
 """Nearest Gradle project owning a Java source, represented as root-relative path."""
 current=path.parent
 while current!=root and root in current.parents:
  if (current/'build.gradle').is_file() or (current/'build.gradle.kts').is_file():
   return current.relative_to(root).as_posix()
  current=current.parent
 return top(path,root)

def verify(root:Path)->dict:
 root=root.resolve(); findings=[]; graph={}; builds=[]; java=[]
 settings_file=root/'settings.gradle'
 if not settings_file.is_file() and not (root/'settings.gradle.kts').is_file(): findings.append('root settings missing')
 if not (root/'build.gradle').is_file() and not (root/'build.gradle.kts').is_file(): findings.append('root build missing')
 settings_text=settings_file.read_text(encoding='utf-8-sig',errors='replace') if settings_file.is_file() else ''
 logical_to_physical={m.group('logical'):m.group('physical').replace('\\','/').strip('/') for m in PROJECT_DIR.finditer(settings_text)}
 catalog_file=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
 if catalog_file.is_file():
  try:
   catalog=json.loads(catalog_file.read_text(encoding='utf-8-sig'))
   for row in catalog.get('modules',[]):
    if isinstance(row,dict) and row.get('projectPath') and row.get('ownerPath'):
     logical_to_physical[str(row['projectPath'])]=str(row['ownerPath']).replace('\\','/').strip('/')
  except Exception as exc:
   findings.append(f'canonical starter catalog unreadable: {exc}')
 def project_id(build:Path)->str:
  rel=build.parent.relative_to(root).as_posix()
  return rel if rel!='.' else 'root'
 def resolve_dep(raw:str)->str:
  logical=':'+raw
  return logical_to_physical.get(logical, raw.replace(':','/'))
 def is_test_scope(configuration:str)->bool:
  return 'test' in configuration.lower()
 def allowed_batch_dependency(source_project:str, dest_project:str)->bool:
  if source_project=='cpf-admin': return dest_project=='cpf-batch/api'
  if source_project=='cpf-starters/profiles/batch-service':
   return dest_project in {'cpf-batch/api','cpf-batch/runtime','cpf-batch/runtime-support'}
  if source_project=='cpf-tools/runtime/cpf-local-batch-runtime': return dest_project.startswith('cpf-batch/')
  if source_project.startswith('cpf-batch/'): return dest_project.startswith('cpf-batch/')
  return False
 for build in sorted(list(root.rglob('build.gradle'))+list(root.rglob('build.gradle.kts'))):
  if any(x in SKIP for x in build.parts): continue
  source_project=project_id(build); source=source_project.split('/',1)[0]; builds.append(build.relative_to(root).as_posix()); production_deps=[]
  text=build.read_text(encoding='utf-8-sig',errors='replace')
  for configuration,raw in PROJECT_DECL.findall(text):
   dest_project=resolve_dep(raw); dest=dest_project.split('/',1)[0]
   if not is_test_scope(configuration): production_deps.append(dest_project)
   if (source,dest) in FORBIDDEN_DEP: findings.append(f'{build.relative_to(root)}: forbidden dependency {source}->{dest}')
   if dest=='cpf-batch' and not allowed_batch_dependency(source_project,dest_project):
    findings.append(f'{build.relative_to(root)}: unauthorized Batch runtime dependency {source_project}->{dest_project}')
  graph[source_project]=sorted(set(production_deps))
 # Operational SCC excludes test-scoped edges; forbidden owner access above remains enforced for every scope.
 visiting=set(); visited=set()
 def dfs(node,stack):
  if node in visiting:
   findings.append('dependency cycle: '+' -> '.join(stack+[node])); return
  if node in visited:return
  visiting.add(node)
  for dep in graph.get(node,[]):
   if dep in graph: dfs(dep,stack+[node])
  visiting.remove(node); visited.add(node)
 for n in graph: dfs(n,[])
 package_modules={}
 source_text={}
 for p in sorted(root.rglob('src/main/java/**/*.java')):
  if any(x in SKIP for x in p.parts):continue
  rel=p.relative_to(root).as_posix(); text=p.read_text(encoding='utf-8-sig',errors='replace'); java.append(rel); source_text[p]=text
  m=PACKAGE.search(text)
  if m: package_modules.setdefault(m.group(1),set()).add(java_module(p,root))
 def target_modules(fqcn:str)->set[str]:
  candidate=fqcn.replace('.*','')
  while candidate.startswith('com.cpf.'):
   if candidate in package_modules:return package_modules[candidate]
   if '.' not in candidate:return set()
   candidate=candidate.rsplit('.',1)[0]
  return set()
 internal=[]
 for p,text in source_text.items():
  src=java_module(p,root); rel=p.relative_to(root).as_posix(); checked=set()
  for ref in INTERNAL_IMPORT.findall(text):
   checked.add(ref.replace('.*','')); owners=target_modules(ref)
   if not owners:
    findings.append(f'{rel}: unresolved CPF internal import {ref}'); continue
   if src not in owners:
    findings.append(f'{rel}: cross-module internal import {ref} owner={sorted(owners)} source={src}')
    internal.append({'file':rel,'reference':ref,'owners':sorted(owners),'source':src})
  for ref in INTERNAL_FQCN.findall(text):
   if any(ref.startswith(prefix) for prefix in checked): continue
   owners=target_modules(ref)
   if not owners:
    findings.append(f'{rel}: unresolved CPF internal reference {ref}'); continue
   if src not in owners:
    findings.append(f'{rel}: cross-module internal reference {ref} owner={sorted(owners)} source={src}')
    internal.append({'file':rel,'reference':ref,'owners':sorted(owners),'source':src})
  if top(p,root)=='cpf-core' and re.search(r'\bcom\.cpf\.(?:admin|bizadmin|batch|gateway|member|education|starter|integration|messaging|file|security|web)\.',text):
   findings.append(f'{rel}: cpf-core owns reverse/runtime CPF reference')
 result={'status':'PASS' if not findings else 'FAIL','buildFileCount':len(builds),'mainJavaFileCount':len(java),'internalReferenceCount':len(internal),'findings':findings}
 if findings: raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
 return result

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');a=ap.parse_args();root=Path(a.root).resolve()
 try:r=verify(root);c=0
 except Exception as e:
  try:r=json.loads(str(e))
  except Exception:r={'status':'FAIL','message':str(e)}
  c=1
 if a.json_output:
  o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(r,ensure_ascii=False,indent=2));return c
if __name__=='__main__':raise SystemExit(main())
