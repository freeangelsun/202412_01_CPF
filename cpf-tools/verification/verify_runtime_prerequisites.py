#!/usr/bin/env python3
"""Full Repository Gradle/Frontend runtime을 실행할 수 있는 환경인지 fail-closed 판정합니다."""
from pathlib import Path
import argparse,json,re,shutil,subprocess,sys
ROOT=Path(__file__).resolve().parents[2]
def version_tuple(s):
 m=re.search(r'(\d+)\.(\d+)\.(\d+)',s);return tuple(map(int,m.groups())) if m else None
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default=str(ROOT));a=ap.parse_args();root=Path(a.root).resolve();fail=[]
 wrapper=(root/'gradlew.bat' if (root/'gradlew.bat').is_file() else root/'gradlew')
 if not wrapper.is_file():fail.append('GRADLE_WRAPPER_MISSING')
 for rel in ['cpf-admin/frontend/package.json','cpf-admin/frontend/package-lock.json','cpf-biz-frontend/package.json','cpf-biz-frontend/package-lock.json']:
  if not (root/rel).is_file():fail.append('FULL_FRONTEND_FILE_MISSING:'+rel)
 node=shutil.which('node');npm=shutil.which('npm')
 if not node:fail.append('NODE_MISSING')
 else:
  nv=subprocess.check_output([node,'--version'],text=True).strip();v=version_tuple(nv)
  if v is None or not ((22,18,0)<=v<(25,0,0)):fail.append('NODE_VERSION_UNSUPPORTED:'+nv+':required=>=22.18.0<25')
 if not npm:fail.append('NPM_MISSING')
 else:
  n=subprocess.check_output([npm,'--version'],text=True).strip()
  if n!='10.9.2':fail.append('NPM_VERSION_MISMATCH:'+n+':required=10.9.2')
 print('CPF_FULL_RUNTIME_PREREQUISITES='+('PASS' if not fail else 'FAIL'))
 print('failures='+str(len(fail)))
 for x in fail:print(x)
 return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
