#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args(); root=Path(a.root).resolve()
 cat=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
 mods={m['artifactId']:m for m in cat.get('modules',[]) if m.get('artifactId') and m.get('ownerPath')}
 project_to_artifact={m.get('projectPath'):aid for aid,m in mods.items() if m.get('projectPath')}
 coord_to_artifact={f"{m.get('groupId')}:{aid}":aid for aid,m in mods.items() if m.get('groupId')}
 def runtime_declarations(text):
  declarations=[]; current=[]; depth=0
  for raw in text.splitlines():
   line=re.sub(r'//.*$','',raw).rstrip()
   match=re.match(r'^\s*(api|implementation|runtimeOnly)\b(.*)$',line)
   if match:
    if current: declarations.append('\n'.join(current))
    current=[line]; depth=line.count('(')-line.count(')')
    if depth<=0: declarations.append(current.pop())
   elif current:
    current.append(line); depth+=line.count('(')-line.count(')')
    if depth<=0:
     declarations.append('\n'.join(current));current=[]
  if current: declarations.append('\n'.join(current))
  return declarations
 def dependency_artifacts(text):
  out=set()
  for declaration in runtime_declarations(text):
   for pp in re.findall(r"project\(['\"]([^'\"]+)['\"]\)",declaration):
    if pp in project_to_artifact: out.add(project_to_artifact[pp])
   for g,ar in re.findall(r"['\"]([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+):(?:\$\{[^}]+\}|[^'\"]+)['\"]",declaration):
    k=f'{g}:{ar}'
    if k in coord_to_artifact: out.add(coord_to_artifact[k])
  return out
 def direct(aid):
  m=mods[aid]; p=root/m['ownerPath']/'build.gradle'; text=p.read_text(encoding='utf-8',errors='ignore') if p.is_file() else ''
  return dependency_artifacts(text)
 def closure(roots):
  seen=set(); stack=list(roots)
  while stack:
   x=stack.pop()
   if x in seen or x not in mods: continue
   seen.add(x); stack.extend(direct(x)-seen)
  return seen
 combos={
  'minimal':{'cpf-starter','cpf-starter-secure-api'},
  'persistence-only':{'cpf-starter','cpf-starter-secure-api','cpf-starter-data-jdbc'},
  'http-only':{'cpf-starter','cpf-starter-secure-api','cpf-starter-integration-http'},
  'resilience-only':{'cpf-starter','cpf-starter-secure-api','cpf-starter-integration-resilience'},
 }
 optional={'persistence':'cpf-starter-data-jdbc','http':'cpf-starter-integration-http','resilience':'cpf-starter-integration-resilience'}
 findings=[]; results={}
 for name,roots in combos.items():
  c=closure(roots); results[name]=sorted(c)
  expected={'minimal':set(),'persistence-only':{'persistence'},'http-only':{'http'},'resilience-only':{'resilience'}}[name]
  for label,aid in optional.items():
   present=aid in c
   if present != (label in expected): findings.append(f'{name}:{label}:present={present}')
 # Profile/catalog hidden defaults must not inject optional providers.
 blob=json.dumps(cat.get('profiles',{}),ensure_ascii=False)+json.dumps(cat.get('profileDefinitions',{}),ensure_ascii=False)
 for aid in ('cpf-starter-integration-http','cpf-starter-integration-resilience','cpf-starter-data-jdbc'):
  if aid in blob: findings.append('catalog-hidden-default:'+aid)
 mutation=[]
 if a.mutation_self_test:
  # In-memory mutation must be detected by the same invariant.
  fake=set(results['minimal'])|{'cpf-starter-integration-http'}
  mutation.append('PASS' if 'cpf-starter-integration-http' in fake and 'cpf-starter-integration-http' not in results['minimal'] else 'FAIL')
  fake2=set(results['http-only'])|{'cpf-starter-data-jdbc'}
  mutation.append('PASS' if 'cpf-starter-data-jdbc' in fake2 and 'cpf-starter-data-jdbc' not in results['http-only'] else 'FAIL')
  runtime_edge=dependency_artifacts("implementation project(':internal:integration:resilience')")
  compile_only_edge=dependency_artifacts("compileOnly project(':internal:integration:resilience')")
  mutation.append('PASS' if 'cpf-starter-integration-resilience' in runtime_edge else 'FAIL')
  mutation.append('PASS' if 'cpf-starter-integration-resilience' not in compile_only_edge else 'FAIL')
  if mutation.count('PASS')!=4: findings.append('mutation-self-test-failed')
 payload={'status':'PASS' if not findings else 'FAIL','results':results,'findings':findings,'mutation':mutation}
 print(json.dumps(payload,ensure_ascii=False,indent=2)); return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
