#!/usr/bin/env python3
from pathlib import Path
import json,re,sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
errors=[]; files=[]
secret_patterns=[
 ('private-key',re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----')),
 ('aws-access-key',re.compile(r'\bAKIA[0-9A-Z]{16}\b')),
 ('github-token',re.compile(r'\bgh[pousr]_[A-Za-z0-9_]{30,}\b')),
 ('password-literal',re.compile(r'(?i)\b(?:password|passwd|secret|token)\s*[:=]\s*["\'](?!\$\{|\*\*\*|<|REQUIRED|CHANGE_ME|CPF_)[^"\']{8,}["\']')),
]
for p in R.rglob('*'):
    if not p.is_file(): continue
    rel=p.relative_to(R).as_posix(); files.append(rel)
    if '__pycache__' in rel or rel.endswith(('.pyc','.class','.log','.tmp','.bak','~')): errors.append(f'generated/temporary file: {rel}')
    if len(rel)>220: errors.append(f'Windows-risk path length {len(rel)}: {rel}')
    if p.stat().st_size==0: errors.append(f'empty file: {rel}')
    if p.stat().st_size>2_000_000: continue
    try: text=p.read_text(encoding='utf-8')
    except (UnicodeDecodeError,OSError): continue
    for name,pattern in secret_patterns:
        if pattern.search(text): errors.append(f'possible {name}: {rel}')
    for line_no,line in enumerate(text.splitlines(),1):
        if line.endswith((' ', '\t')): errors.append(f'trailing whitespace {rel}:{line_no}')
    if rel.endswith('.json'):
        try: json.loads(text)
        except Exception as e: errors.append(f'invalid JSON {rel}: {e}')
if len(files)!=len(set(files)): errors.append('duplicate paths')
for e in errors: print('FAIL',e)
if errors: raise SystemExit(1)
print(f'PASS overlay hygiene files={len(files)} empty=0 temp=0 longpath=0 secret-pattern=0')
