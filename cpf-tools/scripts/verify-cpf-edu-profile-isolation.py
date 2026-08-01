#!/usr/bin/env python3
"""Keep EDU/REF identifiers out of the product ADM default state."""
from __future__ import annotations
import argparse,re
from pathlib import Path
TOKENS=re.compile(r'CPF_(?:EDU|REF)_(?!PROFILE\b)[A-Z0-9_]+')
ALLOWED={'cpf-admin/frontend/src/state/createAdmEducationFixture.ts'}
REQUIRED_PRODUCT='cpf-admin/frontend/src/state/createAdmState.ts'
REQUIRED_FIXTURE='cpf-admin/frontend/src/state/createAdmEducationFixture.ts'
def validate(root:Path):
 errors=[]
 product=root/REQUIRED_PRODUCT;fixture=root/REQUIRED_FIXTURE
 if not product.is_file():errors.append(f'missing product state: {REQUIRED_PRODUCT}')
 else:
  text=product.read_text(encoding='utf-8')
  if TOKENS.search(text):errors.append('product ADM default state contains EDU/REF identifier')
  for token in ['VITE_CPF_EDU_PROFILE === "true"','createAdmEducationFixture()']:
   if token not in text:errors.append(f'product state missing explicit EDU profile guard: {token}')
 if not fixture.is_file():errors.append(f'missing explicit EDU fixture: {REQUIRED_FIXTURE}')
 else:
  text=fixture.read_text(encoding='utf-8')
  for token in ['CPF_EDU_TASKLET_JOB','CPF_EDU_TASKLET_DAILY','CPF_REF_CENTER_CUT_SAMPLE_JOB']:
   if token not in text:errors.append(f'EDU fixture missing protected identifier: {token}')
 for base in [root/'cpf-admin/frontend/src',root/'cpf-admin/src/main']:
  if not base.exists():continue
  for p in base.rglob('*'):
   if not p.is_file() or p.suffix not in {'.ts','.vue','.java','.json','.yml','.yaml','.properties'}:continue
   rel=p.relative_to(root).as_posix()
   if rel in ALLOWED:continue
   text=p.read_text(encoding='utf-8',errors='replace')
   if TOKENS.search(text):errors.append(f'EDU/REF product identifier outside explicit fixture: {rel}')
 return errors
def main():
 p=argparse.ArgumentParser();p.add_argument('--root',type=Path,default=Path.cwd());a=p.parse_args();e=validate(a.root.resolve())
 for x in e:print('[FAIL]',x)
 if e:return 1
 print('[PASS] CPF EDU profile isolation productDefaults=0 explicitFixture=1')
 return 0
if __name__=='__main__':raise SystemExit(main())
