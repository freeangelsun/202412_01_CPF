#!/usr/bin/env python3
"""Verify QA37 root-overlay manifests without treating the overlay as a full repository."""
from __future__ import annotations
import argparse,csv,hashlib,json,sys
from pathlib import Path,PurePosixPath
HASH='cpf-docs/work/manifest/CPF_20260801_QA37_FILES.sha256'
CHANGE='cpf-docs/work/manifest/CPF_20260801_QA37_CHANGE_MANIFEST.csv'
DELETE='cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt'
PACKAGE='cpf-docs/work/manifest/CPF_20260801_QA37_PACKAGE_MANIFEST.json'
ROOT_MANIFEST='cpf-docs/work/manifest/CPF_20260801_QA37_ROOT_OVERLAY_MANIFEST.json'

def fail(msg:str)->None:
 print('[CPF][QA37][PACKAGE][FAIL] '+msg,file=sys.stderr);raise SystemExit(1)
def digest(path:Path)->str:
 h=hashlib.sha256()
 with path.open('rb') as f:
  for block in iter(lambda:f.read(1024*1024),b''):h.update(block)
 return h.hexdigest()
def safe(rel:str)->bool:
 p=PurePosixPath(rel)
 return bool(rel) and not p.is_absolute() and '..' not in p.parts and '\\' not in rel

def main()->None:
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());a=ap.parse_args();root=a.root.resolve()
 files={p.relative_to(root).as_posix():p for p in root.rglob('*') if p.is_file()}
 for rel in files:
  if not safe(rel):fail('unsafe overlay path '+rel)
 package=json.loads((root/PACKAGE).read_text(encoding='utf-8-sig'))
 overlay=json.loads((root/ROOT_MANIFEST).read_text(encoding='utf-8-sig'))
 if package.get('fileCount')!=len(files) or overlay.get('fileCount')!=len(files):fail('fileCount drift')
 expected_hash=set(files)-{HASH};actual_hash={}
 for lineno,line in enumerate((root/HASH).read_text(encoding='utf-8').splitlines(),1):
  if not line.strip():continue
  try:
   digest_value,value=line.split('  ',1)
  except ValueError:
   fail(f'invalid hash line {lineno}')
  if value in actual_hash:fail('duplicate hash path '+value)
  if not safe(value):fail('unsafe hash path '+value)
  actual_hash[value]=digest_value
 if set(actual_hash)!=expected_hash:
  fail(f'hash coverage drift missing={sorted(expected_hash-set(actual_hash))[:5]} extra={sorted(set(actual_hash)-expected_hash)[:5]}')
 for rel,expected in actual_hash.items():
  actual=digest(files[rel])
  if actual!=expected:fail(f'hash mismatch {rel} expected={expected} actual={actual}')
 delete=[x.strip() for x in (root/DELETE).read_text(encoding='utf-8-sig').splitlines() if x.strip()]
 if len(delete)!=len(set(delete)):fail('duplicate delete target')
 if any(not safe(x) for x in delete):fail('unsafe delete target')
 rows=list(csv.DictReader((root/CHANGE).open(encoding='utf-8-sig',newline='')))
 if not rows:fail('change manifest empty')
 overlay_rows=[r for r in rows if r.get('change_type')=='ADD_OR_REPLACE']
 delete_rows=[r for r in rows if r.get('change_type')=='DELETE_REVIEW_REQUIRED']
 expected_change=set(files)-{HASH,CHANGE}
 actual_change={r.get('path') for r in overlay_rows}
 if actual_change!=expected_change:fail('change manifest overlay coverage drift')
 if {r.get('path') for r in delete_rows}!=set(delete):fail('change manifest delete coverage drift')
 print(f'[CPF][QA37][PACKAGE][PASS] files={len(files)} hashes={len(actual_hash)} changes={len(rows)} deletes={len(delete)}')
if __name__=='__main__':main()
