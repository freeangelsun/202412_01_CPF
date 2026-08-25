#!/usr/bin/env python3
import argparse,hashlib,json,re
from pathlib import Path
SECRET=re.compile(rb'(?i)(password|secret|api[_-]?key|private[_-]?key|token)\s*[:=]\s*[^\s,;]{6,}')
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--bundle',required=True);ap.add_argument('--manifest',required=True);a=ap.parse_args();root=Path(a.bundle);manifest=json.loads(Path(a.manifest).read_text(encoding="utf-8"));fail=[]
 expected={x['path']:x['sha256'] for x in manifest.get('files',[])}
 actual={}
 for p in root.rglob('*'):
  if p.is_file():
   rel=p.relative_to(root).as_posix();data=p.read_bytes();actual[rel]=hashlib.sha256(data).hexdigest()
   if SECRET.search(data[:2_000_000]):fail.append(f'possible secret: {rel}')
 for rel,h in expected.items():
  if actual.get(rel)!=h:fail.append(f'hash/missing: {rel}')
 for rel in actual.keys()-expected.keys():fail.append(f'unmanifested file: {rel}')
 print(json.dumps({'status':'PASS' if not fail else 'FAIL','failures':fail},ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
