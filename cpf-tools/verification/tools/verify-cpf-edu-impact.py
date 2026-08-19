#!/usr/bin/env python3
"""Require EDU coverage, tests, and an impact declaration with product-surface changes."""
from __future__ import annotations
import argparse,csv,fnmatch,os,subprocess
from pathlib import Path
CATEGORIES={
 'PUBLIC_CONTRACT':['cpf-core/src/main/java/**/api/**/*.java','cpf-core/src/main/java/**/spi/**/*.java','cpf-common/src/main/java/**/api/**/*.java','cpf-common/src/main/java/**/spi/**/*.java','cpf-batch/api/src/main/java/**/*.java'],
 'STARTER':['cpf-starters/**'],
 'GENERATOR':['cpf-tools/generator/**'],
 'ADM_OPERATION':['cpf-admin/src/main/java/**/*Controller.java','cpf-admin/frontend/src/app/routes.ts','cpf-admin/frontend/src/features/**'],
 'OPENAPI_CONTRACT':['cpf-admin/frontend/openapi/**','cpf-biz-admin/openapi/**','cpf-admin/frontend/src/generated/**','cpf-biz-frontend/src/generated/**'],
 'DB_CONTRACT':['cpf-tools/db/**'],
 'RUNTIME_CONTRACT':['cpf-gateway/src/main/java/**','cpf-batch/**/src/main/java/**'],
}
DECL='cpf-docs/work/current/CPF_EDU_IMPACT_DECLARATION.csv'
COVERAGE={'cpf-tools/governance/cpf-edu-executable-catalog.json'}
TEST_MARKERS=('/src/test/','/testing/','/verification/','.test.ts','.spec.ts','Test.java')

def match(path,pattern):return fnmatch.fnmatch(path,pattern) or fnmatch.fnmatch(path,pattern.replace('**/','*'))
def changed_from_git(root:Path,base_ref:str|None):
 if base_ref:cmd=['git','diff','--name-only',f'{base_ref}...HEAD']
 elif os.getenv('GITHUB_BASE_REF'):cmd=['git','diff','--name-only',f'origin/{os.environ["GITHUB_BASE_REF"]}...HEAD']
 else:cmd=['git','diff-tree','--no-commit-id','--name-only','-r','HEAD']
 cp=subprocess.run(cmd,cwd=root,text=True,capture_output=True)
 if cp.returncode:raise RuntimeError(cp.stderr.strip() or 'git changed-file query failed')
 return [x.strip().replace('\\','/') for x in cp.stdout.splitlines() if x.strip()]
def validate(root:Path,changed:list[str],declaration:Path):
 errors=[];changed=set(p.replace('\\','/').lstrip('./') for p in changed if p.strip())
 impacted={cat for cat,patterns in CATEGORIES.items() if any(any(match(p,pat) for pat in patterns) for p in changed)}
 if not impacted:return []
 if DECL not in changed:errors.append(f'impactful product change requires changed declaration in same commit: {DECL}')

 # Coverage authority must exist, but a product change does not rewrite the 135-feature catalog unless the educational surface itself changes.
 if not all((root/path).is_file() for path in COVERAGE):errors.append('current EDU executable coverage catalog is missing')
 if not any(any(marker in '/'+p for marker in TEST_MARKERS) for p in changed):errors.append('impactful product change requires a changed executable test')
 if not declaration.is_file():return errors+[f'EDU impact declaration missing: {declaration}']
 with declaration.open(encoding='utf-8-sig',newline='') as f:rows=list(csv.DictReader(f))
 declared={r.get('surface','') for r in rows if r.get('status')=='완료'}
 for cat in sorted(impacted-declared):errors.append(f'impact category has no completed declaration: {cat}')
 for r in rows:
  iid=r.get('impact_id','<missing>');fids=[x.strip() for x in (r.get('edu_feature_ids') or '').split('|') if x.strip()]
  if not fids or any(not x.startswith('EDU-') for x in fids):errors.append(f'{iid}: concrete EDU feature IDs required')
  patterns=[x.strip() for x in (r.get('changed_path_pattern') or '').split('|') if x.strip()]
  if r.get('surface') in impacted and not any(any(match(p,pat) for pat in patterns) for p in changed):
   # Multiple declaration rows per surface are allowed; unrelated rows do not fail.
   continue
  for field in ['coverage_artifacts','test_artifacts','owner']:
   if not (r.get(field) or '').strip():errors.append(f'{iid}: {field} required')
 return errors

def main():
 p=argparse.ArgumentParser();p.add_argument('--root',type=Path,default=Path.cwd());p.add_argument('--changed-files',type=Path);p.add_argument('--base-ref');p.add_argument('--declaration',type=Path);a=p.parse_args();root=a.root.resolve()
 try:
  changed=[x.strip() for x in a.changed_files.read_text(encoding='utf-8').splitlines() if x.strip()] if a.changed_files else changed_from_git(root,a.base_ref)
 except Exception as e:print('[FAIL]',e);return 1
 declaration=a.declaration or root/DECL;e=validate(root,changed,declaration)
 for x in e:print('[FAIL]',x)
 if e:return 1
 impacted=sorted(cat for cat,pats in CATEGORIES.items() if any(any(match(p,pat) for pat in pats) for p in changed))
 print(f'[PASS] CPF EDU impact changedFiles={len(changed)} impacted={"|".join(impacted) or "none"}')
 return 0
if __name__=='__main__':raise SystemExit(main())
