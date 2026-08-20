#!/usr/bin/env python3
"""Application/Generated/Education Source의 임의 DB Vendor branch를 금지하는 Gate."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
VENDORS=r'(?:oracle|postgres(?:ql)?|mariadb)'
BRANCH=re.compile(rf'(?is)(?:\bif\s*\([^)]*{VENDORS}|\belse\s+if\b[^\n]*{VENDORS}|\bswitch\s*\([^)]*(?:vendor|database)|getDatabaseProductName\s*\(|DatabaseMetaData[^\n]*{VENDORS})')
THREE_COPY=re.compile(r'(?i)(oracle|postgres(?:ql)?|mariadb)')
AUTHORIZED_PARTS={'dialect','renderer','generated','vendor','vendors','db','database','migration','migrations'}
SCAN_ROOTS=['cpf-common','cpf-admin','cpf-backoffice/online','cpf-batch','cpf-gateway','cpf-starters','cpf-education','cpf-member','cpf-external']

def authorized(p:Path)->bool:
 # Vendor 차이는 Application/Business가 아니라 명시적인 Data Provider/Dialect 경계가 소유한다.
 parts={x.lower() for x in p.parts}
 if p.stem.lower().endswith('dialect') or p.stem in {'CpfSqlResources','CpfDatabaseVendor'}: return True
 if 'cpf-starters' in parts and 'data' in parts: return True
 return bool(parts&AUTHORIZED_PARTS) and ('src' not in parts or 'main' not in parts or 'java' not in parts)

def main(argv=None):
 ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); a=ap.parse_args(argv); root=Path(a.root).resolve(); findings=[]; scanned=0
 for rr in SCAN_ROOTS:
  base=root/rr
  if not base.exists(): continue
  for p in base.rglob('*'):
   if not p.is_file() or p.suffix.lower() not in {'.java','.kt','.xml','.sql'} or 'build' in p.parts: continue
   scanned+=1
   try: s=p.read_text(encoding='utf-8',errors='ignore')
   except Exception: continue
   rel=p.relative_to(root).as_posix()
   if p.suffix.lower() in {'.java','.kt'} and BRANCH.search(s) and not authorized(p): findings.append({'path':rel,'type':'APPLICATION_VENDOR_BRANCH'})
 # Generated Customer Domain의 업무 Source는 Vendor 이름 자체를 알지 않아야 한다.
 # Education은 공식 DB3 의미동일성/운영 검증을 설명할 수 있으므로 문자열 존재만으로 실패시키지 않는다.
 for rr in ['cpf-member','cpf-external']:
  base=root/rr
  if not base.exists(): continue
  for p in base.rglob('*'):
   if not p.is_file() or p.suffix.lower() not in {'.java','.kt','.xml'} or 'build' in p.parts: continue
   rel=p.relative_to(root).as_posix()
   if '/db/generated/' in '/'+rel: continue
   s=p.read_text(encoding='utf-8',errors='ignore')
   # 주석의 'Oracle/PostgreSQL/MariaDB 동일 Source' 설명은 허용하고 실행 코드/SQL 토큰만 검사한다.
   code=re.sub(r'/\*.*?\*/|<!--.*?-->|//.*$','',s,flags=re.S|re.M)
   if re.search(VENDORS,code,re.I): findings.append({'path':rel,'type':'GENERATED_OR_EDU_VENDOR_TOKEN'})
 result={'status':'PASS' if not findings else 'FAIL','scannedFiles':scanned,'findingCount':len(findings),'findings':findings[:200]}; print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if not findings else 2
if __name__=='__main__': raise SystemExit(main())
