#!/usr/bin/env python3
from pathlib import Path
import argparse

def main():
 p=argparse.ArgumentParser(); p.add_argument('--root',default='.'); a=p.parse_args(); r=Path(a.root)
 f=r/'cpf-starters/security/src/main/java/com/cpf/starter/security/CpfServerSessionSecurityAutoConfiguration.java'
 s=f.read_text(encoding='utf-8-sig')
 if '.anyRequest().permitAll()' in s: raise SystemExit('BFF anyRequest permitAll is forbidden')
 for token in ['.anyRequest().authenticated()', 'HttpStatus.UNAUTHORIZED', 'HttpStatus.FORBIDDEN', '"/api/bza/auth/login"']:
  if token not in s: raise SystemExit(f'missing BFF security contract: {token}')
 test=r/'cpf-starters/security/src/test/java/com/cpf/starter/security/CpfServerSessionSecurityFilterChainTest.java'
 t=test.read_text(encoding='utf-8-sig')
 for token in ['isUnauthorized()', 'isForbidden()', 'with(csrf())']:
  if token not in t: raise SystemExit(f'missing BFF negative test: {token}')
 print('CPF BFF authorization ownership: PASS')
if __name__=='__main__': main()
