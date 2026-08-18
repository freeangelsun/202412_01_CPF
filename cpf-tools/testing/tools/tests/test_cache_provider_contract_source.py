from pathlib import Path
ROOT = Path(__file__).resolve().parents[4]
def text(rel): return (ROOT / rel).read_text(encoding="utf-8")
def test_shared_runtime_owns_protocol_and_durable_invalidation_semantics():
    cache=text("cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCache.java")
    coordinator=text("cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinator.java")
    assert "implements CpfCache, CpfDistributedLockPort" in cache
    assert "ScanOptions.scanOptions()" in cache
    assert "redis.call('get', KEYS[1]) == ARGV[1]" in cache
    assert "durable.version(" in coordinator and "durable.advanceVersion(" in coordinator and "reconcileNow()" in coordinator
def test_redis_and_valkey_are_thin_explicit_providers_over_shared_runtime():
    redis=text("cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/RedisCpfCache.java")
    valkey=text("cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/ValkeyCpfCache.java")
    assert "extends SpringDataRedisCpfCache" in redis and "extends SpringDataRedisCpfCache" in valkey
    for rel in ("cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/CpfRedisCacheAutoConfiguration.java","cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyAutoConfiguration.java"):
        source=text(rel); assert "CpfCacheInvalidationCoordinator" in source; assert "JdbcCpfCacheInvalidationStore" in source; assert "CpfRedisProtocolProviderSelection.requireExclusive" in source
def test_adm_consumes_shared_invalidation_owner():
    assert "import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;" in text("cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java")
def test_public_cache_is_exactly_one_business_surface():
    api=text("cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCache.java")
    assert "getOrLoad(" in api and "getOrLoadOptional(" in api
    assert not (ROOT/"cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCacheAsideService.java").exists()
