#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, subprocess, os, tempfile, shutil
from pathlib import Path

def source_identity(root: Path) -> str:
    env = os.environ.get("CPF_SOURCE_SHA", "").strip()
    if len(env) == 40 and all(c in "0123456789abcdefABCDEF" for c in env):
        return env.lower()
    if (root / '.git').exists():
        cp = subprocess.run(['git','rev-parse','HEAD'], cwd=root, text=True, capture_output=True)
        value = (cp.stdout or '').strip()
        if cp.returncode == 0 and len(value) == 40:
            return value
    base = root / 'cpf-docs/work/BASE_SHA.txt'
    if base.is_file():
        value = base.read_text(encoding='utf-8', errors='ignore').strip()
        if len(value) == 40:
            return value
    return 'UNKNOWN'

REQ = {
 'shared': 'cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon',
 'redis': 'cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis',
 'valkey': 'cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey',
}

def read(p: Path) -> str:
    return p.read_text(encoding='utf-8', errors='ignore') if p.is_file() else ''

def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--evidence')
    ns=ap.parse_args(); root=Path(ns.root).resolve(); checks=[]
    def ck(name, ok, detail=''):
        checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})

    catp=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    cat=json.loads(read(catp) or '{}')
    modules={m.get('ownerPath'):m for m in cat.get('modules',[]) if isinstance(m,dict)}
    expected={
      'cpf-starters/data/cache/spring-data-redis':(':internal:data:cache:spring-data-redis','cpf-cache-spring-data-redis','internal'),
      'cpf-starters/data/cache/redis':(':starters:cache:redis','cpf-starter-cache-redis','public'),
      'cpf-starters/data/cache/valkey':(':starters:cache:valkey','cpf-starter-cache-valkey','public'),
    }
    for owner,(project,artifact,visibility) in expected.items():
        m=modules.get(owner,{})
        ck('catalog:'+owner, m.get('projectPath')==project and m.get('artifactId')==artifact and m.get('visibility')==visibility,
           f"actual={m.get('projectPath')}/{m.get('artifactId')}/{m.get('visibility')}")
    slots=cat.get('providerSlots',{}).get('cache',{})
    ck('provider-slot:redis', slots.get('redis',{}).get('projectPath')==':starters:cache:redis')
    ck('provider-slot:valkey', slots.get('valkey',{}).get('projectPath')==':starters:cache:valkey')

    redis=read(root/REQ['redis']/'CpfRedisCacheAutoConfiguration.java')
    valkey=read(root/REQ['valkey']/'CpfValkeyAutoConfiguration.java')
    shared_sel=read(root/REQ['shared']/'CpfRedisProtocolProviderSelection.java')
    collision=read(root/REQ['shared']/'CpfRedisProtocolProviderCollisionAutoConfiguration.java')
    startup=read(root/REQ['shared']/'CpfRedisLikeStartupValidator.java')
    port=read(root/REQ['shared']/'SpringDataRedisCpfCache.java')
    coord=read(root/REQ['shared']/'CpfCacheInvalidationCoordinator.java')
    operation_test=read(root/'cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCacheOperationTest.java')
    selection_test=read(root/'cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/CpfRedisProtocolProviderSelectionTest.java')
    collision_test=read(root/'cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/CpfRedisProtocolProviderCollisionAutoConfigurationTest.java')
    startup_test=read(root/'cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/CpfRedisLikeStartupValidatorTest.java')

    ck('redis-explicit-enable','cpf.data.cache.redis' in redis and 'cpf.data.cache.valkey.enabled' in redis and 'requireExclusive' in redis)
    ck('valkey-explicit-enable','cpf.data.cache.valkey' in valkey and 'cpf.data.cache.redis.enabled' in valkey and 'requireExclusive' in valkey)
    ck('collision-fail-fast','cannot coexist' in collision and 'IllegalStateException' in collision)
    ck('selection-fail-fast','Redis and Valkey cache providers cannot be enabled at the same time' in shared_sel)
    ck('startup-required-fail-fast','required cache provider unavailable at startup' in startup and 'SmartInitializingSingleton' in startup)
    ck('shared-runtime', all(t in port for t in ['opsForValue().get(', 'opsForValue().set(', 'delete(', 'validateTtl', 'health()', 'UNAVAILABLE']))
    ck('serialization-version', all(t in port for t in ['value.version()', 'Base64', 'contentType', 'expiresAt']))
    ck('durable-reconcile', all(t in coord for t in ['reconcileNow()', 'checkpoint(', 'event.version() <= current', 'durable.version(']))
    ck('operation-tests', all(t in operation_test for t in ['putGetEvictAndMetricsUseTheSharedRuntime','providerFailureIsNotSilentlyConvertedToCacheMiss','missAndInvalidPayloadLimitsAreFailClosed']))
    ck('selection-tests','requireExclusive' in selection_test and 'assertThrows' in selection_test)
    ck('collision-tests','CpfRedisProtocolProviderClasspathGuard' in collision_test and 'assertThrows' in collision_test)
    ck('startup-tests','afterSingletonsInstantiated' in startup_test and 'assertThrows' in startup_test)

    # Execute the actual provider-selection production class as a low-cost Java behavior smoke.
    javac=shutil.which('javac'); java=shutil.which('java')
    if javac and java and (root/REQ['shared']/'CpfRedisProtocolProviderSelection.java').is_file():
        with tempfile.TemporaryDirectory(prefix='cpf-redis-selection-') as td:
            t=Path(td); harness=t/'SelectionHarness.java'
            harness.write_text('''import com.cpf.data.cache.rediscommon.CpfRedisProtocolProviderSelection;\npublic class SelectionHarness { public static void main(String[] a) { CpfRedisProtocolProviderSelection.requireExclusive(true,false); CpfRedisProtocolProviderSelection.requireExclusive(false,true); CpfRedisProtocolProviderSelection.requireExclusive(false,false); try { CpfRedisProtocolProviderSelection.requireExclusive(true,true); throw new AssertionError("collision accepted"); } catch (IllegalStateException expected) { System.out.println("CPF_REDIS_SELECTION_SMOKE=PASS"); } } }\n''',encoding='utf-8')
            cp=subprocess.run([javac,'-encoding','UTF-8','-d',td,str(root/REQ['shared']/'CpfRedisProtocolProviderSelection.java'),str(harness)],capture_output=True,text=True)
            rp=subprocess.run([java,'-cp',td,'SelectionHarness'],capture_output=True,text=True) if cp.returncode==0 else None
            ck('selection-java-smoke', cp.returncode==0 and rp is not None and rp.returncode==0 and 'PASS' in rp.stdout,
               (cp.stderr if cp.returncode else ((rp.stdout+rp.stderr) if rp else ''))[-500:])
    else:
        checks.append({'name':'selection-java-smoke','status':'UNVERIFIED','detail':'javac/java unavailable'})

    failed=[c for c in checks if c['status']=='FAIL']; unverified=[c for c in checks if c['status']=='UNVERIFIED']
    data={'requirementId':'NXT2-REDIS-001','executionSourceSha':source_identity(root),
          'status':'FAIL' if failed else ('UNVERIFIED' if unverified else 'PASS'),'failedCount':len(failed),'unverifiedCount':len(unverified),
          'checks':checks,'liveRuntime':'UNVERIFIED: external Redis/Valkey server/container not available in this execution environment'}
    if ns.evidence:
        ep=Path(ns.evidence); ep.parent.mkdir(parents=True,exist_ok=True); ep.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('CPF_REDIS_VALKEY_CURRENT='+data['status']+f" failed={len(failed)} unverified={len(unverified)} checks={len(checks)}")
    for c in failed+unverified: print(c['status'],c['name'],c['detail'])
    return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())
