#!/usr/bin/env python3
"""Fail-closed static dialect lint for CPF official vendor seed bundles."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
OFFICIAL=('mariadb','postgresql','oracle')
SEED_PLAN_KEYS=('productSeedFiles','optionalSampleSeedFiles','testSeedFiles')
SEED_BUNDLES=('00_product_seed.sql','00_optional_sample_seed.sql','00_test_seed.sql')
FORBIDDEN={
 'mariadb': (r'\bON\s+CONFLICT\b',r'\bRETURNING\b',r'\bVARCHAR2\b',r'\bNVARCHAR2\b',r'\bFROM\s+DUAL\b'),
 'postgresql': (r'\bON\s+DUPLICATE\s+KEY\b',r'\bINSERT\s+IGNORE\b',r'\bVARCHAR2\b',r'\bFROM\s+DUAL\b'),
 'oracle': (r'\bON\s+CONFLICT\b',r'\bON\s+DUPLICATE\s+KEY\b',r'\bINSERT\s+IGNORE\b',r'`[^`]+`')
}
def strip_comments_and_check_quotes(vendor:str,text:str)->tuple[str,bool]:
 out=[];i=0;state='normal';balanced=True
 while i<len(text):
  ch=text[i];nxt=text[i+1] if i+1<len(text) else ''
  if state=='normal':
   if ch=='-' and nxt=='-': state='line_comment';out.extend('  ');i+=2;continue
   if ch=='/' and nxt=='*': state='block_comment';out.extend('  ');i+=2;continue
   if ch=="'": state='single';out.append(ch);i+=1;continue
   out.append(ch);i+=1;continue
  if state=='line_comment':
   if ch=='\n': state='normal';out.append('\n')
   else: out.append(' ')
   i+=1;continue
  if state=='block_comment':
   if ch=='*' and nxt=='/': state='normal';out.extend('  ');i+=2;continue
   out.append('\n' if ch=='\n' else ' ');i+=1;continue
  # single-quoted literal
  out.append(ch)
  if vendor=='mariadb' and ch=='\\' and nxt:
   out.append(nxt);i+=2;continue
  if ch=="'" and nxt=="'":
   out.append(nxt);i+=2;continue
  if ch=="'": state='normal'
  i+=1
 if state=='single': balanced=False
 return ''.join(out),balanced

def lint(vendor:str,text:str,*,require_terminal:bool=True)->list[str]:
 failures=[]
 stripped,quotes_balanced=strip_comments_and_check_quotes(vendor,text)
 if not stripped.strip(): failures.append('empty SQL')
 for pattern in FORBIDDEN[vendor]:
  if re.search(pattern,stripped,re.I): failures.append(f'forbidden token: {pattern}')
 if '\x00' in text: failures.append('NUL byte')
 if not quotes_balanced: failures.append('unbalanced single quotes')
 if require_terminal and vendor in ('mariadb','postgresql') and not re.search(r';\s*$',stripped): failures.append('missing terminal semicolon')
 if require_terminal and vendor=='oracle' and not re.search(r'(;|/)\s*$',stripped): failures.append('missing terminal terminator')
 return failures
def seed_paths(root:Path,vendor:str)->list[Path]:
 plan=json.loads((root/'cpf-tools/db/config/database-source-plan.json').read_text(encoding='utf-8'))
 vendor_root=root/f'cpf-tools/db/vendor/{vendor}'
 source_names={str(name) for key in SEED_PLAN_KEYS for name in plan[vendor][key]}
 paths={vendor_root/'source'/name for name in source_names}
 paths.update(vendor_root/'source'/name for name in SEED_BUNDLES)
 paths.update(vendor_root/'seed'/name for name in SEED_BUNDLES)
 return sorted(paths)
def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());ap.add_argument('--json-output',type=Path);a=ap.parse_args()
 findings=[];checked=0
 for v in OFFICIAL:
  vendor_root=a.root/f'cpf-tools/db/vendor/{v}'
  paths=seed_paths(a.root,v)
  for p in paths:
   if not p.is_file(): findings.append(f'{v}:{p.relative_to(vendor_root)}:missing')
  for p in paths:
   if not p.is_file(): continue
   checked+=1
   rel=p.relative_to(vendor_root)
   for f in lint(v,p.read_text(encoding='utf-8-sig')): findings.append(f'{v}:{rel}:{f}')
 result={'schemaVersion':1,'status':'PASS' if not findings else 'FAIL','officialVendors':list(OFFICIAL),'checkedFiles':checked,'findings':findings}
 if a.json_output:
  a.json_output.parent.mkdir(parents=True,exist_ok=True)
  a.json_output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
 print(json.dumps(result,ensure_ascii=False,sort_keys=True));return 0 if not findings else 1
if __name__=='__main__':raise SystemExit(main())
