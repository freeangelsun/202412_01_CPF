#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, subprocess, tempfile
from pathlib import Path

CACHE_FILES=[
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCachePort.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheKey.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheValue.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheMetricsSnapshot.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheHealth.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfDistributedLockPort.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfLockToken.java",
"cpf-starters/data/src/main/java/com/cpf/data/cache/CpfCacheCounters.java",
"cpf-starters/data/cache/caffeine/src/main/java/com/cpf/data/cache/CpfLocalCacheProvider.java"]
FEATURE_FILES=[
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/featureflag/CpfFeatureFlagContext.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/featureflag/CpfFeatureFlagOperations.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/featureflag/CpfFeatureFlagResult.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/featureflag/CpfFeatureFlagValue.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/api/featureflag/CpfFeatureFlags.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/spi/featureflag/CpfFeatureFlagProvider.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/spi/featureflag/CpfFeatureFlagStateStore.java",
"cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/spi/featureflag/CpfFeatureFlagAuditSink.java",
"cpf-starters/platform-operations/feature-flag/openfeature/src/main/java/com/cpf/platform/operations/featureflag/openfeature/CpfFeatureFlagRuntime.java",
"cpf-starters/platform-operations/feature-flag/openfeature/src/main/java/com/cpf/platform/operations/featureflag/openfeature/CpfFeatureFlagTransactionRunner.java"]
VALKEY_FILES=CACHE_FILES[:-1]+[
"cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfRedisLikeProviderProperties.java",
"cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCachePort.java",
"cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyProperties.java",
"cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/ValkeyCpfCachePort.java"]
STATIC_PATHS=[
"cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/ValkeyCpfCachePort.java",
"cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java",
"cpf-starters/platform-operations/feature-flag/openfeature/src/main/java/com/cpf/platform/operations/featureflag/openfeature/CpfFeatureFlagAutoConfiguration.java",
"cpf-admin/frontend/src/features/feature-flags/FeatureFlagsPage.vue",
"cpf-tools/db/vendor/oracle/source/15_qa39_resilience_feature_flag.sql",
"cpf-tools/db/vendor/postgresql/source/15_qa39_resilience_feature_flag.sql",
"cpf-tools/db/vendor/mariadb/source/15_qa39_resilience_feature_flag.sql",
"cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinator.java",
"cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/JdbcCpfCacheInvalidationStore.java",
"cpf-tools/db/vendor/oracle/source/16_cache_invalidation_ledger.sql",
"cpf-tools/db/vendor/postgresql/source/16_cache_invalidation_ledger.sql",
"cpf-tools/db/vendor/mariadb/source/16_cache_invalidation_ledger.sql"]
FIXTURE_DIR="cpf-tools/runtime/tools/tests/runtime-fixtures/cache-feature-flag"

def compile_run(repo,files,fixture,main,fixture_tree=False):
 with tempfile.TemporaryDirectory(prefix='cpf-cache-feature-runtime-') as td:
  tmp=Path(td); src=tmp/'src'; classes=tmp/'classes'; classes.mkdir()
  for rel in files:
   p=repo/rel; text=p.read_text(encoding='utf-8'); pkg=next(x.split()[1].rstrip(';') for x in text.splitlines() if x.startswith('package ')); dst=src/Path(pkg.replace('.','/'))/p.name; dst.parent.mkdir(parents=True,exist_ok=True); dst.write_text(text,encoding='utf-8')
  fixtures=([repo/FIXTURE_DIR/fixture]+list((repo/FIXTURE_DIR/'org').rglob('*.java'))) if fixture_tree else [repo/FIXTURE_DIR/fixture]
  for h in fixtures:
   text=h.read_text(encoding='utf-8'); pkg=next(x.split()[1].rstrip(';') for x in text.splitlines() if x.startswith('package ')); dst=src/Path(pkg.replace('.','/'))/h.name; dst.parent.mkdir(parents=True,exist_ok=True); dst.write_text(text,encoding='utf-8')
  cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(classes),*[str(p) for p in sorted(src.rglob('*.java'))]],text=True,capture_output=True)
  if cp.returncode: raise RuntimeError(cp.stdout+cp.stderr)
  run=subprocess.run(['java','-cp',str(classes),main],text=True,capture_output=True)
  if run.returncode: raise RuntimeError(run.stdout+run.stderr)
  return {'javacExitCode':cp.returncode,'javaExitCode':run.returncode,'stdout':run.stdout.strip(),'sourceCount':len(files)}

def verify(repo):
 required=CACHE_FILES+FEATURE_FILES+VALKEY_FILES+STATIC_PATHS+[f'{FIXTURE_DIR}/CpfCacheRuntimeHarness.java',f'{FIXTURE_DIR}/CpfFeatureFlagRuntimeHarness.java',f'{FIXTURE_DIR}/ValkeyCpfCachePortRuntimeHarness.java']; missing=sorted(set(x for x in required if not (repo/x).is_file()))
 if missing: raise FileNotFoundError('missing: '+','.join(missing))
 valkey=(repo/STATIC_PATHS[0]).read_text(encoding='utf-8'); adm=(repo/STATIC_PATHS[1]).read_text(encoding='utf-8'); auto=(repo/STATIC_PATHS[2]).read_text(encoding='utf-8'); front=(repo/STATIC_PATHS[3]).read_text(encoding='utf-8').lower()
 coordinator=(repo/STATIC_PATHS[7]).read_text(encoding='utf-8'); durable=(repo/STATIC_PATHS[8]).read_text(encoding='utf-8')
 checks={
  'canonicalCacheOwner': all(not p.startswith('cpf-core/') and not p.startswith('cpf-common/') for p in CACHE_FILES),
  'canonicalFeatureOwner': all(not p.startswith('cpf-core/') for p in FEATURE_FILES),
  'valkeyImplementsDistributedCache':'extends SpringDataRedisCpfCachePort' in valkey and all(x in (repo/VALKEY_FILES[-3]).read_text(encoding='utf-8') for x in ('implements CpfCachePort, CpfDistributedLockPort','release(CpfLockToken','tryAcquire(')),
  'admCacheConsumer':all(x in adm for x in ('CpfCachePort','evictNamespace','health','metrics')),
  'featureAutoConfiguration':'CpfFeatureFlagRuntime' in auto,
  'frontendConsumer':'feature' in front,
  'threeVendorSchema':all((repo/p).stat().st_size>100 for p in STATIC_PATHS[4:7]),
  'durableCoordinator':all(x in coordinator for x in ('durable.append','reconcileNow','durable.checkpoint')),
  'jdbcDurableStore':all(x in durable for x in ('ORACLE','POSTGRESQL','MARIADB','LAST_EVENT_ID < ?')),
  'threeVendorCacheLedger':all((repo/p).stat().st_size>500 for p in STATIC_PATHS[9:12])}
 if not all(checks.values()): raise AssertionError(checks)
 return {'status':'PASS','checks':checks,
  'cacheRuntime':compile_run(repo,CACHE_FILES,'CpfCacheRuntimeHarness.java','com.cpf.data.cache.CpfCacheRuntimeHarness'),
  'valkeyRuntime':compile_run(repo,VALKEY_FILES,'ValkeyCpfCachePortRuntimeHarness.java','com.cpf.data.cache.valkey.ValkeyCpfCachePortRuntimeHarness',True),
  'featureFlagRuntime':compile_run(repo,FEATURE_FILES,'CpfFeatureFlagRuntimeHarness.java','com.cpf.platform.operations.featureflag.openfeature.CpfFeatureFlagRuntimeHarness')}

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--repo-root',default='.'); ap.add_argument('--report-json',required=True); a=ap.parse_args(); r=verify(Path(a.repo_root).resolve()); Path(a.report_json).write_text(json.dumps(r,ensure_ascii=False,indent=2)+'\n',encoding='utf-8'); print(json.dumps(r,ensure_ascii=False))
if __name__=='__main__': main()
