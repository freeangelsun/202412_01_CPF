#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, subprocess, os
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

REQ='NXT2-REDIS-001'

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    ap.add_argument('--evidence')
    ns=ap.parse_args(); root=Path(ns.root).resolve()
    checks=[]
    def check(name, cond, detail=''):
        checks.append({'name':name,'status':'PASS' if cond else 'FAIL','detail':detail})
    def text(rel):
        p=root/rel
        return p.read_text(encoding='utf-8') if p.is_file() else ''
    cat_path=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    try: cat=json.loads(cat_path.read_text(encoding='utf-8'))
    except Exception as e:
        cat={}; check('catalog-json',False,str(e))
    else: check('catalog-json',True)
    mods={m.get('artifactId'):m for m in cat.get('modules',[]) if isinstance(m,dict)}
    for aid in ['cpf-starter-cache-redis','cpf-starter-cache-valkey']:
        check(f'{aid}-public',mods.get(aid,{}).get('visibility')=='public')
    check('shared-runtime-internal',mods.get('cpf-cache-spring-data-redis',{}).get('visibility')=='internal')
    cache=cat.get('providerSlots',{}).get('cache',{})
    for provider in ['redis','valkey']:
        expected=f'cpf-starter-cache-{provider}'
        slot=cache.get(provider,{})
        expected_project=mods.get(expected,{}).get('projectPath')
        check(f'{provider}-provider-slot-project',bool(expected_project) and slot.get('projectPath')==expected_project,repr(slot))
        check(f'{provider}-provider-slot-coordinate',slot.get('coordinate')==f'com.cpf.starter:{expected}',repr(slot))
    ext=cat.get('steeringExtensions',{}).get('redisCacheProvider',{})
    check('shared-runtime-current-path',ext.get('sharedRuntimeProjectPath')==':internal:data:cache:spring-data-redis',repr(ext))
    # Redis/Valkey public provider artifacts are canonical active coordinates.
    # Only the retired shared-provider artifact is forbidden outside explicit legacy/removed fields.
    old={'cpf-starter-data-cache-spring-data-redis'}
    violations=[]
    def walk(v,path='$'):
        if isinstance(v,dict):
            for k,x in v.items(): walk(x,f'{path}.{k}')
        elif isinstance(v,list):
            for i,x in enumerate(v): walk(x,f'{path}[{i}]')
        elif isinstance(v,str) and any(o in v for o in old):
            if not (path.endswith('.legacyArtifactId') or '.removedArtifactIds[' in path): violations.append((path,v))
    walk(cat)
    check('legacy-coordinate-no-active-consumer',not violations,repr(violations[:10]))
    redis_build=text('cpf-starters/data/cache/redis/build.gradle')
    valkey_build=text('cpf-starters/data/cache/valkey/build.gradle')
    check('redis-build-current-shared-runtime',"api project(':internal:data:cache:spring-data-redis')" in redis_build)
    check('redis-public-artifact',"artifactId = 'cpf-starter-cache-redis'" in redis_build)
    check('valkey-build-current-shared-runtime',"api project(':internal:data:cache:spring-data-redis')" in valkey_build)
    check('valkey-public-artifact',"artifactId = 'cpf-starter-cache-valkey'" in valkey_build)
    redis_auto=text('cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/CpfRedisCacheAutoConfiguration.java')
    valkey_auto=text('cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyAutoConfiguration.java')
    for name,src in [('redis',redis_auto),('valkey',valkey_auto)]:
        check(f'{name}-exclusive-selection','requireExclusive' in src)
        check(f'{name}-startup-validator','CpfRedisLikeStartupValidator' in src)
    collision=text('cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfRedisProtocolProviderCollisionAutoConfiguration.java')
    imports=text('cpf-starters/data/cache/spring-data-redis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports')
    check('collision-fail-fast-source','redis && valkey' in collision and 'IllegalStateException' in collision)
    check('collision-auto-config-registered','CpfRedisProtocolProviderCollisionAutoConfiguration' in imports)
    startup=text('cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfRedisLikeStartupValidator.java')
    check('required-startup-fail-closed','isRequired' in startup and 'IllegalStateException' in startup and 'health' in startup.lower())
    runtime=text('cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCache.java')
    for name,token in {
        'runtime-get':'public CpfCacheValue get(', 'runtime-put':'public void put(', 'runtime-evict':'public boolean evict(',
        'runtime-ttl':'validateTtl', 'runtime-serialization':'Base64', 'runtime-health':'PING_NOT_PONG',
        'runtime-reconnect-metric':'reconnects.increment()', 'runtime-fencing-lock':'fencingToken',
        'runtime-error-propagation':'errors.increment()'}.items(): check(name,token in runtime)
    op_test=text('cpf-starters/data/cache/spring-data-redis/src/test/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCacheOperationTest.java')
    check('operation-test-put-get-evict',all(x in op_test for x in ['port.put(', 'port.get(', 'port.evict(']))
    check('operation-test-infra-failure-not-miss','redis unavailable' in op_test and 'assertThrows' in op_test)
    fail=[c for c in checks if c['status']=='FAIL']
    result={'requirementId':REQ,'executionSourceSha':source_identity(root),'status':'PASS' if not fail else 'FAIL','failedCount':len(fail),'checks':checks,'runtimeVerification':'UNVERIFIED: live Redis/Valkey server disconnect/reconnect runtime unavailable in current environment'}
    out=Path(ns.evidence) if ns.evidence else root/'cpf-docs/work/evidence/current/REDIS_VALKEY_PROVIDER.json'
    out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({'status':result['status'],'failedCount':len(fail),'checkCount':len(checks)},ensure_ascii=False))
    raise SystemExit(1 if fail else 0)
if __name__=='__main__': main()
