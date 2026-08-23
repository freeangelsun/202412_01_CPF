#!/usr/bin/env python3
from __future__ import annotations
import argparse,json
from pathlib import Path

def properties(path:Path):
 values={}
 for raw in path.read_text(encoding='utf-8-sig').splitlines():
  line=raw.strip()
  if line and not line.startswith(('#','!')) and '=' in line:
   key,value=line.split('=',1); values[key.strip()]=value.strip()
 return values

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args(); root=Path(a.root).resolve(); f=[]
 contract_path=root/'cpf-backoffice/gradle.properties'; d=properties(contract_path); settings=(root/'settings.gradle').read_text(encoding='utf-8')
 if d.get('cpf.domain.generationMode')!='prebuilt': f.append('MBW-generation-mode-not-prebuilt')
 if d.get('cpf.domain.systemCode')!='MBW': f.append('MBW-system-code-mismatch')
 forbidden=[name for name in ('cpf-domain.yaml','cpf-generator.lock.json','.cpf') if (root/'cpf-backoffice'/name).exists()]
 if forbidden: f.append('MBW-forbidden-generator-metadata:'+','.join(forbidden))
 if settings.count("include ':apps:backoffice'")!=1: f.append('MBW-root-include-not-exactly-one')
 if "project(':apps:backoffice').projectDir = file('cpf-backoffice/online')" not in settings: f.append('MBW-root-projectDir-missing')
 if "getProperty('cpf.domain.generationMode','generated') != 'prebuilt'" not in settings:
  f.append('Generated-scanner-prebuilt-exclusion-missing')
 if 'cpf-generated-cpf-backoffice' in settings: f.append('MBW-generated-project-hardcoded')
 mut=[]
 if a.mutation_self_test:
  mutated={**d,'cpf.domain.generationMode':'generated'}
  mut.append('PASS' if mutated.get('cpf.domain.generationMode')!='prebuilt' else 'FAIL')
  if mut!=['PASS']: f.append('mutation-self-test-failed')
 p={'status':'PASS' if not f else 'FAIL','findings':f,'mutation':mut}; print(json.dumps(p,ensure_ascii=False,indent=2)); return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
