#!/usr/bin/env python3
"""Fail closed on locale-sensitive case normalization in product Java source."""
from pathlib import Path
import argparse, json, re

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');args=ap.parse_args();root=Path(args.root).resolve()
 findings=[]; checked=0
 for p in root.rglob('src/main/java/**/*.java'):
  if '/build/' in p.as_posix(): continue
  checked+=1
  try:text=p.read_text(encoding='utf-8')
  except UnicodeDecodeError as e: findings.append(f'{p.relative_to(root).as_posix()}:utf8:{e}');continue
  for line_no,line in enumerate(text.splitlines(),1):
   if re.search(r'\.to(?:Upper|Lower)Case\(\)',line):
    findings.append(f'{p.relative_to(root).as_posix()}:{line_no}:{line.strip()}')
 result={'status':'PASS' if not findings else 'FAIL','checkedJavaFiles':checked,'localeSensitiveNoArgCaseConversions':findings}
 print(json.dumps(result,ensure_ascii=False))
 return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
