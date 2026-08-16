#!/usr/bin/env python3
"""Fail-closed gate for the canonical cpf-education identity and package IA."""
from __future__ import annotations
import argparse,csv,re,sys
from pathlib import Path
CANONICAL_ROOTS={'base','common','web','data','transaction','messaging','integration','file','security','batch','operations','generator','scenarios','verification'}
FORBIDDEN=(re.compile(r'cpf-reference'),re.compile(r'com\.cpf\.reference'),re.compile(r'\bReferenceApplication\b'),re.compile(r'\bEDUERENCE\b'),re.compile(r'\brefAP01\b'),re.compile(r'\bref-local-01\b'))
TEXT_SUFFIX={'.java','.json','.yml','.yaml','.properties','.gradle','.xml','.sql','.ps1','.py','.mjs','.ts'}
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();errors=[]
 if (root/'cpf-reference').exists():errors.append('retired product root exists: cpf-reference')
 edu=root/'cpf-education/src/main/java/com/cpf/education'
 if not edu.is_dir():errors.append('canonical Education package root missing')
 else:
  actual={p.relative_to(edu).parts[0] for p in edu.rglob('*.java') if p.is_file() and len(p.relative_to(edu).parts)>1}
  extra=sorted(actual-CANONICAL_ROOTS);missing=sorted(CANONICAL_ROOTS-actual)
  if extra:errors.append('non-canonical Education top package roots: '+','.join(extra))
  if missing:errors.append('missing canonical Education top package roots: '+','.join(missing))
 scan_roots=[root/'settings.gradle',root/'build.gradle',root/'gradle',root/'deploy',root/'cpf-education',root/'cpf-tools/release',root/'cpf-tools/runtime',root/'cpf-tools/generator/contracts',root/'cpf-tools/db/config',root/'cpf-tools/db/canonical',root/'cpf-tools/db/metadata',root/'cpf-tools/governance',root/'cpf-admin/frontend']
 for sr in scan_roots:
  paths=[sr] if sr.is_file() else sr.rglob('*') if sr.exists() else []
  for p in paths:
   if not p.is_file() or any(x in p.parts for x in ('build','bin','__pycache__','.git')):continue
   if p.suffix.lower() not in TEXT_SUFFIX and p.name not in {'settings.gradle','build.gradle'}:continue
   try:text=p.read_text(encoding='utf-8',errors='replace')
   except OSError:continue
   for pat in FORBIDDEN:
    if pat.search(text):errors.append(f'{p.relative_to(root).as_posix()}: active retired identity {pat.pattern}')
 # database ownership contract
 contract=root/'cpf-tools/generator/contracts/education-reference-fixture-contract.json'
 if not contract.is_file():errors.append('Education reference fixture ownership contract missing')
 for e in errors:print('[FAIL]',e)
 if errors:return 1
 print('CPF_EDUCATION_ACTIVE_SURFACE=PASS roots=14 retired_identity=0')
 return 0
if __name__=='__main__':raise SystemExit(main())
