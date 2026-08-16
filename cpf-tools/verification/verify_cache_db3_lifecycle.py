#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

VENDORS=('oracle','postgresql','mariadb')
FILES=(
 'source/22_cache_invalidation_version_fence.sql',
 'install/17_cache_invalidation_version_fence.sql',
 'migration/V113__cache_invalidation_version_fence.sql',
 'rollback/R113__cache_invalidation_version_fence.sql',
 'verify/113_verify_cache_invalidation_version_fence.sql',
 'runtime/cache/cache_invalidation_version_queries.sql',
)

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();errors=[]
 store=root/'cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/JdbcCpfCacheInvalidationStore.java'
 if not store.is_file(): errors.append('CACHE_JDBC_STORE_MISSING')
 else:
  t=store.read_text(encoding='utf-8',errors='ignore')
  for token in ('ORACLE','POSTGRESQL','MARIADB','CPF_CACHE_INVALIDATION_VERSION','LAST_VERSION < ?'):
   if token not in t: errors.append('CACHE_STORE_TOKEN_MISSING:'+token)
  for forbidden in ('MYSQL','MSSQL','SQLSERVER','H2'):
   if re.search(r'\b'+forbidden+r'\b',t,re.I): errors.append('UNOFFICIAL_DB_IN_CACHE_STORE:'+forbidden)
 provider_local=root/'cpf-starters/data/cache/spring-data-redis/src/main/resources/db/cache-invalidation'
 if provider_local.exists(): errors.append('PROVIDER_LOCAL_DB_LIFECYCLE_PRESENT')
 for v in VENDORS:
  base=root/f'cpf-tools/db/vendor/{v}'
  paths=[base/x for x in FILES]
  for p in paths:
   if not p.is_file(): errors.append(f'{v}:MISSING:'+p.relative_to(root).as_posix())
  if any(not p.is_file() for p in paths): continue
  source,install,migration,rollback,verify,runtime=paths
  ddl=source.read_text(encoding='utf-8')
  if install.read_text(encoding='utf-8')!=ddl: errors.append(v+':INSTALL_SOURCE_DRIFT')
  if migration.read_text(encoding='utf-8')!=ddl: errors.append(v+':MIGRATION_SOURCE_DRIFT')
  for token in ('CPF_CACHE_INVALIDATION_VERSION','CONSUMER_ID','TENANT_ID','NAMESPACE_NAME','CACHE_KEY_VALUE','LAST_VERSION','PRIMARY KEY'):
   if token not in ddl: errors.append(v+':DDL_TOKEN_MISSING:'+token)
  if 'LAST_VERSION >= 0' not in ddl: errors.append(v+':VERSION_CHECK_MISSING')
  if "DEFAULT ''" in ddl or 'DEFAULT ""' in ddl: errors.append(v+':EMPTY_DEFAULT_FORBIDDEN')
  if rollback.read_text(encoding='utf-8').strip().upper()!='DROP TABLE CPF_CACHE_INVALIDATION_VERSION;': errors.append(v+':ROLLBACK_WRONG')
  if 'CPF_CACHE_INVALIDATION_VERSION' not in verify.read_text(encoding='utf-8').upper(): errors.append(v+':VERIFY_WRONG')
  if 'CPF_CACHE_INVALIDATION_VERSION' not in runtime.read_text(encoding='utf-8').upper(): errors.append(v+':RUNTIME_QUERY_WRONG')
  if v=='oracle' and 'VARCHAR2(512 CHAR)' not in ddl: errors.append('oracle:CACHE_KEY_WIDTH_WRONG')
  if v=='postgresql' and 'TIMESTAMP(6) WITH TIME ZONE' not in ddl: errors.append('postgresql:TIMESTAMP_SEMANTICS_WRONG')
  if v=='mariadb' and 'ENGINE=InnoDB' not in ddl: errors.append('mariadb:ENGINE_MISSING')
  pack=base/'pack.json'
  if not pack.is_file(): errors.append(v+':PACK_MISSING'); continue
  data=json.loads(pack.read_text(encoding='utf-8'))
  # Vendor pack is now a canonical root locator. Capability-specific V113 metadata is
  # verified from the actual lifecycle files rather than stale nested pack keys.
  if data.get('owner')!='cpf-tools/db': errors.append(v+':PACK_OWNER_DRIFT')
  if data.get('runtimeVerification')!='미검증': errors.append(v+':FALSE_RUNTIME_PASS')
  if data.get('historicalMigrationRoot')!=f'cpf-tools/db/vendor/{v}/migration': errors.append(v+':PACK_MIGRATION_ROOT_DRIFT')
  if data.get('historicalRollbackRoot')!=f'cpf-tools/db/vendor/{v}/rollback': errors.append(v+':PACK_ROLLBACK_ROOT_DRIFT')
  if data.get('runtimeRoot')!=f'cpf-tools/db/vendor/{v}/runtime': errors.append(v+':PACK_RUNTIME_ROOT_DRIFT')
 if errors:
  print('CPF_CACHE_DB3_LIFECYCLE=FAIL errors='+str(len(errors)))
  for e in errors: print(e)
  return 1
 print('CPF_CACHE_DB3_LIFECYCLE=PASS vendors=oracle,postgresql,mariadb install=true upgrade=V113 rollback=R113 runtimeQuery=true providerLocalDdl=0')
 return 0
if __name__=='__main__': raise SystemExit(main())
