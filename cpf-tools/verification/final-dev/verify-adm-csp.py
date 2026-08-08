#!/usr/bin/env python3
from pathlib import Path
import argparse,sys
BAD=['https://unpkg.com','unpkg.com',"'unsafe-eval'",'http://','https://cdn.','fonts.googleapis.com','fonts.gstatic.com']
def check(root):
 f=root/'cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java'
 if not f.exists(): return ['missing-filter']
 s=f.read_text();e=[]
 if 'Content-Security-Policy' not in s or "script-src 'self'" not in s:e.append('self-csp-missing')
 for b in BAD:
  if b in s:e.append('external-or-unsafe:'+b)
 index=root/'cpf-admin/frontend/index.html'
 if index.exists():
  x=index.read_text()
  for b in ['http://','https://','unpkg.com','cdn.']:
   if b in x:e.append('index-external:'+b)
 return e
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();r=Path(a.root)
 e=check(r)
 if e:print('[CPF][ADM-CSP][FAIL]',*e,sep='\n');return 1
 if a.self_test:
  p=r/'cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java';o=p.read_text();p.write_text(o.replace("script-src 'self'", "script-src 'self' https://unpkg.com 'unsafe-eval'",1));d=bool(check(r));p.write_text(o)
  if not d:print('[CPF][ADM-CSP][FAIL] mutation-not-detected');return 1
 print('[CPF][ADM-CSP][PASS] externalRuntime=0 unsafeEval=0 selfTest='+str(a.self_test).lower());return 0
if __name__=='__main__':sys.exit(main())
