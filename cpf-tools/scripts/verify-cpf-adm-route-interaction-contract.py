#!/usr/bin/env python3
"""Fail-closed ADM 59-route interaction, permission and OpenAPI operation contract gate."""
from __future__ import annotations
import argparse,csv,re,sys
from pathlib import Path

ROUTE=re.compile(
 r'^\s*"(?P<id>[^"]+)": \{ routeId: "(?P=id)", path: "(?P<path>[^"]+)", menuId: "(?P<menu>[^"]+)", label: "(?P<label>[^"]+)", group: "(?P<group>[^"]+)", icon: "[^"]+", ownerModule: "(?P<owner>[^"]+)", riskLevel: "(?P<risk>[^"]+)", featureFlag: "(?P<flag>[^"]+)", expectedOperationIds: \[(?P<ops>.*?)\], component:',
 re.M,
)
OP_ID=re.compile(r'operationId\s*=\s*"([^"]+)"')
REQUIRED_ERRORS={'401','403','404','409','429','500','503'}
REQUIRED_CONTEXT={'transactionId','executionId','correlationId','from','to','filters','tenant','environment','instance','service'}
BOOL={'true','false'}

class ContractError(RuntimeError): pass

def split(value:str)->list[str]:
 return [part.strip() for part in (value or '').split(';') if part.strip()]

def read_routes(path:Path)->dict[str,dict[str,object]]:
 text=path.read_text(encoding='utf-8')
 rows={}
 for match in ROUTE.finditer(text):
  data=match.groupdict(); rid=data['id']
  if rid in rows: raise ContractError(f'duplicate route id: {rid}')
  operations=re.findall(r'"([^"]+)"',data['ops'])
  rows[rid]={**data,'operations':operations}
 if len(rows)!=59: raise ContractError(f'route registry cardinality must be 59: {len(rows)}')
 paths=[str(row['path']) for row in rows.values()]
 if len(paths)!=len(set(paths)): raise ContractError('duplicate route path')
 return rows

def read_matrix(path:Path)->dict[str,dict[str,str]]:
 with path.open(encoding='utf-8-sig',newline='') as handle:
  values=list(csv.DictReader(handle))
 if len(values)!=59: raise ContractError(f'interaction matrix cardinality must be 59: {len(values)}')
 rows={}
 for row in values:
  rid=(row.get('route_id') or '').strip()
  if not rid or rid in rows: raise ContractError(f'invalid or duplicate matrix route: {rid!r}')
  rows[rid]={key:(value or '').strip() for key,value in row.items()}
 return rows

def source_operations(root:Path)->set[str]:
 result=set()
 for module in ('cpf-admin','cpf-biz-admin'):
  source=root/module/'src/main/java'
  if not source.exists(): continue
  for file in source.rglob('*.java'):
   try:text=file.read_text(encoding='utf-8')
   except UnicodeDecodeError:continue
   result.update(OP_ID.findall(text))
 return result

def catalog_operations(path:Path|None)->set[str]:
 if not path:return set()
 if not path.is_file(): raise ContractError(f'operation catalog missing: {path}')
 with path.open(encoding='utf-8-sig',newline='') as handle:
  return {(row.get('operation_id') or '').strip() for row in csv.DictReader(handle) if (row.get('operation_id') or '').strip()}

def validate(root:Path,matrix_path:Path,catalog_path:Path|None)->list[str]:
 routes=read_routes(root/'cpf-admin/frontend/src/app/routes.ts')
 matrix=read_matrix(matrix_path)
 errors=[]
 if set(routes)!=set(matrix):
  errors.append(f'route/matrix id drift missingMatrix={sorted(set(routes)-set(matrix))} extraMatrix={sorted(set(matrix)-set(routes))}')
 known=source_operations(root)|catalog_operations(catalog_path)
 for rid,route in routes.items():
  row=matrix.get(rid)
  if not row:continue
  route_ops=list(route['operations'])
  matrix_ops=split(row.get('query_operation_ids',''))+split(row.get('mutation_operation_ids',''))
  if not route_ops:errors.append(f'{rid}: expectedOperationIds empty')
  if len(route_ops)!=len(set(route_ops)):errors.append(f'{rid}: duplicate expected operation id')
  if set(route_ops)!=set(matrix_ops):errors.append(f'{rid}: route/matrix operation drift')
  for operation in route_ops:
   if operation not in known:errors.append(f'{rid}: operation not found in controller source/catalog: {operation}')
  if route['path']!=row.get('path'):errors.append(f'{rid}: path drift')
  if route['menu']!=row.get('menu_id'):errors.append(f'{rid}: menu drift')
  if route['owner']!=row.get('owner_module'):errors.append(f'{rid}: owner drift')
  if route['risk']!=row.get('risk_level'):errors.append(f'{rid}: risk drift')
  if route['flag']!=f'adm.route.{rid}.enabled':errors.append(f'{rid}: feature flag drift')
  if not split(row.get('button_permissions','')):errors.append(f'{rid}: button permissions empty')
  for key in ('requires_server_paging','requires_detail','requires_empty_state','requires_reason','requires_approval','requires_cas','requires_audit'):
   if row.get(key) not in BOOL:errors.append(f'{rid}: {key} must be true/false')
  if set(split(row.get('required_error_statuses','')))!=REQUIRED_ERRORS:errors.append(f'{rid}: required error statuses drift')
  if set(split(row.get('preserved_context','')))!=REQUIRED_CONTEXT:errors.append(f'{rid}: causal context contract drift')
  mutations=split(row.get('mutation_operation_ids',''))
  if mutations:
   if row.get('requires_reason')!='true' or row.get('requires_audit')!='true':errors.append(f'{rid}: mutation route must require reason and audit')
   if row.get('risk_level') in {'HIGH','CRITICAL'} and (row.get('requires_approval')!='true' or row.get('requires_cas')!='true'):
    errors.append(f'{rid}: high-risk mutation route must require approval and CAS')
  if row.get('development_status') not in {'완료','부분 구현','미구현','재확인 필요'}:errors.append(f'{rid}: invalid development status')
  if row.get('verification_status') not in {'완료','미검증','실패','재확인 필요'}:errors.append(f'{rid}: invalid verification status')
  if any(token in ';'.join(row.values()) for token in ('미수집','미확정')):errors.append(f'{rid}: unresolved placeholder remains')
 return errors

def main()->int:
 parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=Path.cwd());parser.add_argument('--matrix',type=Path);parser.add_argument('--operation-catalog',type=Path)
 args=parser.parse_args();root=args.root.resolve();matrix=(args.matrix or root/'cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv').resolve();catalog=args.operation_catalog.resolve() if args.operation_catalog else None
 try:errors=validate(root,matrix,catalog)
 except (OSError,ContractError) as error:print(f'[FAIL] {error}',file=sys.stderr);return 1
 for error in errors:print('[FAIL]',error)
 if errors:print(f'[FAIL] ADM route interaction contract errors={len(errors)}');return 1
 print('[PASS] ADM route interaction contract routes=59 errorStatuses=7 contextKeys=10 operationCoverage=complete')
 return 0
if __name__=='__main__':raise SystemExit(main())
