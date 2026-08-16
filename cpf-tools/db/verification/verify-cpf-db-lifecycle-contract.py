#!/usr/bin/env python3
"""Validate the canonical Oracle/PostgreSQL/MariaDB lifecycle contract and fail on vendor drift."""
from __future__ import annotations
import argparse,json,sys
from pathlib import Path
class DbContractError(RuntimeError):pass
OFFICIAL=('mariadb','postgresql','oracle')
STAGES=['baseline-install','sequential-upgrade','runtime-query','schema-drift','reverse-rollback','forward-reapply','performance-regression','backup-restore','point-in-time-recovery']
LIFECYCLE_KEYS=['provision','emptyInstall','productSeed','testSeed','verify','migration','rollback']

def read(path:Path)->dict:
 if not path.is_file():raise DbContractError(f'missing JSON: {path}')
 try:return json.loads(path.read_text(encoding='utf-8'))
 except Exception as exc:raise DbContractError(f'invalid JSON {path}: {exc}') from exc

def validate(root:Path,check_paths:bool=True)->tuple[int,int]:
 manifest=read(root/'cpf-tools/db/vendor-pack-manifest.json');contract=read(root/'cpf-tools/db/cpf-db-lifecycle-contract.json')
 if tuple(manifest.get('supportedVendors') or ())!=OFFICIAL or tuple(manifest.get('officialVendors') or ())!=OFFICIAL:raise DbContractError('official/supported vendors must be exactly mariadb, postgresql, oracle')
 if manifest.get('candidateVendors') not in ([],None):raise DbContractError('candidateVendors must be empty')
 if tuple(contract.get('officialVendors') or ())!=OFFICIAL:raise DbContractError('lifecycle officialVendors order/policy drift')
 if contract.get('orderedStages')!=STAGES:raise DbContractError('DB lifecycle stages must use the approved order')
 if contract.get('statusModel',{}).get('development')!='완료' or contract.get('statusModel',{}).get('runtimeVerification')!='미검증':raise DbContractError('development/runtime verification status must remain separated')
 runtime_manifest=contract.get('runtimeEnvironmentManifest') or {}
 if contract.get('environmentManifestVariable')!='CPF_DB_RUNTIME_MANIFEST':raise DbContractError('runtime environment manifest variable drift')
 if runtime_manifest.get('schemaVersion')!=1 or tuple(runtime_manifest.get('vendorOrder') or ())!=OFFICIAL:raise DbContractError('runtime environment manifest schema/vendor order drift')
 if runtime_manifest.get('secretValuePolicy')!='ENVIRONMENT_REFERENCE_ONLY':raise DbContractError('runtime environment manifest must forbid inline secrets')
 runtime_keys=runtime_manifest.get('requiredEnvironmentKeys') or {}
 if tuple(runtime_keys)!=OFFICIAL or any(not runtime_keys.get(vendor) for vendor in OFFICIAL):raise DbContractError('runtime environment manifest required keys are incomplete')
 forbidden=('mysql','mssql','sqlserver','h2')
 if any(token in json.dumps(contract).lower() for token in forbidden):raise DbContractError('unsupported vendor leaked into lifecycle contract')
 for script in contract.get('requiredStaticGates',[]):
  if check_paths and not (root/script).is_file():raise DbContractError(f'required static DB gate missing: {script}')
 for contract_key in ('backupContract','pitrContract','dataRetentionContract','testDataPolicy','performanceContract'):
  contract_path=contract.get(contract_key)
  if not contract_path:raise DbContractError(f'{contract_key} missing')
  if check_paths and not (root/contract_path).is_file():raise DbContractError(f'{contract_key} path missing: {contract_path}')
  if check_paths:
   linked=read(root/contract_path)
   if tuple(linked.get('officialVendors') or ())!=OFFICIAL:raise DbContractError(f'{contract_key} officialVendors drift')
 if check_paths and not (root/contract['runtimeExecutor']).is_file():raise DbContractError(f"runtime executor missing: {contract['runtimeExecutor']}")
 if check_paths:
  runtime_executor=(root/contract['runtimeExecutor']).read_text(encoding='utf-8-sig')
  for token in ('CPF_DB_RUNTIME_MANIFEST','RuntimeManifestPath','runtimeManifestSha256','inline secret is forbidden'):
   if token not in runtime_executor:raise DbContractError(f'runtime executor does not consume the environment manifest contract: token={token}')
 for vendor in OFFICIAL:
  entry=manifest['vendors'].get(vendor);vendor_contract=contract['vendorContracts'].get(vendor)
  if not entry or not vendor_contract:raise DbContractError(f'{vendor}: manifest/contract missing')
  pack_path=root/entry['pack'];pack=read(pack_path)
  if pack.get('vendor')!=vendor or pack.get('status')!='완료':raise DbContractError(f'{vendor}: pack development status incomplete')
  if pack.get('runtimeVerification') not in ('미검증','완료'):raise DbContractError(f'{vendor}: invalid runtimeVerification')
  lifecycle=entry.get('lifecycle',{})
  for key in LIFECYCLE_KEYS:
   if not lifecycle.get(key):raise DbContractError(f'{vendor}: lifecycle key missing={key}')
  pack_migration=str(pack.get('historicalMigrationRoot') or '').rstrip('/')
  pack_rollback=str(pack.get('historicalRollbackRoot') or '').rstrip('/')
  contract_migration=str(vendor_contract['migrationRoot']).replace('/{logicalDatabase}','').rstrip('/')
  contract_rollback=str(vendor_contract['rollbackRoot']).replace('/{logicalDatabase}','').rstrip('/')
  if not contract_migration.startswith(pack_migration+'/'):raise DbContractError(f'{vendor}: migration root escapes canonical historical pack')
  if contract_rollback!=pack_rollback:raise DbContractError(f'{vendor}: rollback root drift')
  if lifecycle['migration']!=vendor_contract['migrationRoot']:raise DbContractError(f'{vendor}: manifest migration and lifecycle contract disagree')
  if lifecycle['rollback']!=vendor_contract['rollbackRoot']:raise DbContractError(f'{vendor}: manifest rollback and lifecycle contract disagree')
  for root_key in ('vendorRoot','runtimeRoot','domainTemplateRoot'):
   value=entry.get(root_key)
   if check_paths and value and not (root/value).exists():raise DbContractError(f'{vendor}: path missing {root_key}={value}')
 return len(OFFICIAL),len(STAGES)

def main()->int:
 parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=Path.cwd());parser.add_argument('--skip-path-check',action='store_true');args=parser.parse_args()
 vendors,stages=validate(args.root.resolve(),not args.skip_path_check);print(f'[PASS] CPF DB lifecycle vendors={vendors} stages={stages} runtimeStatusSeparated=true');return 0
if __name__=='__main__':
 try:raise SystemExit(main())
 except DbContractError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
