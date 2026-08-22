#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args(); root=Path(a.root).resolve(); f=[]
 d=(root/'cpf-backoffice/cpf-domain.yaml').read_text(encoding='utf-8-sig'); settings=(root/'settings.gradle').read_text(encoding='utf-8')
 if not re.search(r'(?m)^\s*mode:\s*prebuilt\s*$',d): f.append('MBW-generation-mode-not-prebuilt')
 if settings.count("include ':apps:backoffice'")!=1: f.append('MBW-root-include-not-exactly-one')
 if "project(':apps:backoffice').projectDir = file('cpf-backoffice/online')" not in settings: f.append('MBW-root-projectDir-missing')
 if 'return !prebuilt' not in settings: f.append('Generated-scanner-prebuilt-exclusion-missing')
 if 'cpf-generated-cpf-backoffice' in settings: f.append('MBW-generated-project-hardcoded')
 mut=[]
 if a.mutation_self_test:
  mutated=d.replace('mode: prebuilt','mode: generated')
  mut.append('PASS' if not re.search(r'(?m)^\s*mode:\s*prebuilt\s*$',mutated) else 'FAIL')
  if mut!=['PASS']: f.append('mutation-self-test-failed')
 p={'status':'PASS' if not f else 'FAIL','findings':f,'mutation':mut}; print(json.dumps(p,ensure_ascii=False,indent=2)); return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
