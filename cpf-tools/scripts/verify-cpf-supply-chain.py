#!/usr/bin/env python3
import argparse,csv,json,re,sys
from pathlib import Path
ALLOW={'Apache-2.0','MIT','BSD-2-Clause','BSD-3-Clause','ISC'}
CONDITIONAL={'MPL-2.0','EPL-2.0','LGPL-2.1-only','LGPL-3.0-only'}
DENY={'GPL-2.0-only','GPL-3.0-only','AGPL-3.0-only','SSPL-1.0','BUSL-1.1','BSL-1.1','RSAL','UNKNOWN','NOASSERTION'}
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--sbom');ap.add_argument('--release',action='store_true');a=ap.parse_args();root=Path(a.root);fail=[]
 lock=root/'cpf-tools/supply-chain/approved-primary-oss.csv'; policy=root/'cpf-tools/supply-chain/license-policy.yml'; notice=root/'cpf-docs/legal/THIRD_PARTY_NOTICES_QA32.md'
 for p in (lock,policy,notice):
  if not p.is_file():fail.append(f'missing {p}')
 if lock.is_file():
  rows=list(csv.DictReader(lock.open(encoding='utf-8-sig')))
  for r in rows:
   if not r['version'] or not r['source_url'].startswith('https://'):fail.append(f'incomplete approved component {r.get("component")}')
   if r['license'] in DENY:fail.append(f'denied approved component {r.get("component")}:{r.get("license")}')
 sbom=Path(a.sbom) if a.sbom else root/'build/reports/cyclonedx/bom.json'
 if a.release and not sbom.is_file():fail.append('release SBOM missing')
 if sbom.is_file():
  data=json.loads(sbom.read_text(encoding='utf-8'));components=data.get('components',[])
  for c in components:
   licenses=[]
   for item in c.get('licenses',[]):
    lic=item.get('license',{});licenses.append(lic.get('id') or lic.get('name') or 'UNKNOWN')
   if not licenses:licenses=['UNKNOWN']
   if any(x in DENY for x in licenses):fail.append(f'denied/unknown license {c.get("name")}:{licenses}')
 print(json.dumps({'status':'PASS' if not fail else 'FAIL','failures':fail},ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
