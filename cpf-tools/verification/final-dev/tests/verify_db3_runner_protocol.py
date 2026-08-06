#!/usr/bin/env python3
from pathlib import Path
import re, sys
p=Path(__file__).resolve().parents[1]/'run-db3-lifecycle.ps1'; s=p.read_text(encoding='utf-8')
checks={
 'expected-head-parameter': r'\[string\]\$ExpectedHead',
 'git-root': r'rev-parse --show-toplevel',
 'stdin-password': r'StandardInput\.WriteLine\(\$Password\)',
 'password-stdin-flag': r"'--password-stdin'",
 'redaction': r'Protect-Text',
 'vendor-oracle': r'CPF_RUNTIME_ORACLE_PASSWORD',
 'vendor-postgresql': r'CPF_RUNTIME_POSTGRESQL_PASSWORD',
 'vendor-mariadb': r'CPF_RUNTIME_MARIADB_PASSWORD',
 'exit-propagation': r'\$overallExit',
}
errors=[name for name,pat in checks.items() if not re.search(pat,s,re.S)]
for forbidden in [r'--password=',r'Tee-Object',r'\$expectedHead\s*=\s*["\'][0-9a-fA-F]{40}["\']']:
 if re.search(forbidden,s,re.I): errors.append('forbidden:'+forbidden)
if errors:
 print('FAIL',*errors,sep='\n');sys.exit(1)
print('PASS DB3 runner protocol static contract')
