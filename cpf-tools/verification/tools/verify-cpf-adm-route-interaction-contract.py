#!/usr/bin/env python3
"""ADM route interaction/permission/OpenAPI fail-closed contract.

Physical sidebar routes are intentionally not 1:1 with every ADM capability
requirement.  This gate validates the current route registry and the explicit
capability->coverage route mapping in the canonical ADM requirement ledger.
"""
from __future__ import annotations
import argparse,csv,re,sys
from pathlib import Path
ROUTE=re.compile(r'^\s*"(?P<id>[^"]+)": \{ routeId: "(?P=id)", path: "(?P<path>[^"]+)", menuId: "(?P<menu>[^"]+)", label: "(?P<label>[^"]+)", group: "(?P<group>[^"]+)", icon: "[^"]+", ownerModule: "(?P<owner>[^"]+)", riskLevel: "(?P<risk>[^"]+)", featureFlag: "(?P<flag>[^"]+)", expectedOperationIds: \[(?P<ops>.*?)\], component:',re.M)
OP_ID=re.compile(r'operationId\s*=\s*"([^"]+)"')
REQUIRED_ERRORS=('401','403','404','409','429','500','503')
class ContractError(RuntimeError):pass

def read_routes(path:Path):
 text=path.read_text(encoding='utf-8'); rows={}
 for m in ROUTE.finditer(text):
  d=m.groupdict(); rid=d['id']
  if rid in rows: raise ContractError(f'duplicate route id: {rid}')
  d['operations']=re.findall(r'"([^"]+)"',d['ops']); rows[rid]=d
 if not rows: raise ContractError('ADM route registry empty')
 paths=[r['path'] for r in rows.values()]
 if len(paths)!=len(set(paths)): raise ContractError('duplicate route path')
 return rows

def source_ops(root:Path):
 result=set()
 for p in (root/'cpf-admin/src/main/java').rglob('*.java'):
  try: result.update(OP_ID.findall(p.read_text(encoding='utf-8')))
  except UnicodeDecodeError: pass
 return result

def validate(root:Path):
 routes=read_routes(root/'cpf-admin/frontend/src/app/routes.ts'); known=source_ops(root)
 req=root/'cpf-docs/work/current/CPF_ADM_UI_FUNCTION_REQUIREMENTS.csv'
 if not req.is_file(): raise ContractError('canonical ADM capability ledger missing')
 with req.open(encoding='utf-8-sig',newline='') as h: caps=list(csv.DictReader(h))
 errors=[]
 if len(caps)!=80: errors.append(f'ADM capability ledger must remain exactly 80 axes: {len(caps)}')
 for rid,r in routes.items():
  if not r['operations']: errors.append(f'{rid}: expectedOperationIds empty')
  if len(r['operations'])!=len(set(r['operations'])): errors.append(f'{rid}: duplicate operation id')
  if not r['flag'].startswith('adm.route.') or not r['flag'].endswith('.enabled'): errors.append(f'{rid}: feature flag contract drift')
  for op in r['operations']:
   if op not in known: errors.append(f'{rid}: controller operation missing: {op}')
 for row in caps:
  coverage=(row.get('coverage_route_id') or row.get('route_id') or '').strip()
  if coverage not in routes: errors.append(f"{row.get('exec_id')}: coverage route missing: {coverage}")
  if (row.get('coverage_mode') or '').strip() not in {'DIRECT','INTEGRATED'}: errors.append(f"{row.get('exec_id')}: coverage_mode must be DIRECT/INTEGRATED")
  if not (row.get('coverage_notes') or '').strip(): errors.append(f"{row.get('exec_id')}: coverage_notes empty")
 page=(root/'cpf-admin/frontend/src/components/page-contract/pageContract.ts').read_text(encoding='utf-8')
 for status in REQUIRED_ERRORS:
  if status not in page: errors.append(f'page error-status contract missing {status}')
 return routes,caps,errors

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());a=ap.parse_args()
 try:routes,caps,errors=validate(a.root.resolve())
 except (OSError,ContractError) as e: print('[FAIL]',e,file=sys.stderr);return 1
 for e in errors: print('[FAIL]',e)
 if errors:return 1
 print(f'[PASS] ADM interaction contract physicalRoutes={len(routes)} capabilities={len(caps)} coverage=80 errorStatuses=7')
 return 0
if __name__=='__main__':raise SystemExit(main())
