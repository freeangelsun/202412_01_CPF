#!/usr/bin/env python3
import argparse,json,re
from pathlib import Path
FORBIDDEN=[re.compile(r'import\s+com\.cpf\.core\.common\.'),re.compile(r'cpf-gateway-webflux'),re.compile(r'location\.hash'),re.compile(r'localStorage[^\n]*(token|refresh)',re.I)]
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root);targets=[root/'cpf-tools/generator',root/'cpf-tools/templates'];fail=[];files=0
 for d in targets:
  if not d.exists():continue
  for p in d.rglob('*'):
   if p.is_file():
    files+=1;text=p.read_text(encoding='utf-8',errors='ignore')
    for rx in FORBIDDEN:
     if rx.search(text):fail.append(f'{p.relative_to(root)}:{rx.pattern}')
 if files==0:fail.append('generator/template source missing')
 print(json.dumps({'files':files,'status':'PASS' if not fail else 'FAIL','failures':fail},ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
