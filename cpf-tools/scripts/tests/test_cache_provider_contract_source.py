from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]

def text(rel): return (ROOT/rel).read_text(encoding='utf-8')

def test_valkey_implements_public_cache_and_fencing_contract_without_keys_command():
    source=text('cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/ValkeyCpfCachePort.java')
    assert 'implements CpfCachePort, CpfDistributedLockPort' in source
    assert 'ScanOptions.scanOptions()' in source
    assert '.keys(' not in source
    assert "redis.call('get', KEYS[1]) == ARGV[1]" in source
    assert "redis.call('del', KEYS[1])" in source
    assert 'opsForValue().increment(fenceKey(lockName))' in source
    assert 'setIfAbsent(lockKey(lockName), storedOwner, lease)' in source
    assert 'maximum-payload-bytes' in source
    assert 'convertAndSend(properties.getInvalidationChannel()' in source

def test_valkey_auto_configuration_precedes_caffeine_and_exposes_primary_contract():
    source=text('cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/CpfValkeyAutoConfiguration.java')
    assert '@AutoConfigureBefore(name = "com.cpf.starter.data.cache.caffeine.CpfCacheAutoConfiguration")' in source
    assert '@ConditionalOnMissingBean(CpfCachePort.class)' in source
    assert 'ValkeyCpfCachePort cpfValkeyCachePort' in source
    assert 'HealthIndicator health(ValkeyCpfCachePort cache, CpfCacheInvalidationPort durable' in source
    assert 'cpfValkeyInvalidationListenerContainer' in source
    assert 'JdbcCpfCacheInvalidationStore cpfCacheInvalidationStore' in source

def test_caffeine_cache_aside_requires_lock_provider():
    source=text('cpf-starters/data/cache-caffeine/src/main/java/com/cpf/starter/data/cache/caffeine/CpfCacheAutoConfiguration.java')
    assert '@ConditionalOnBean({CpfCachePort.class, CpfDistributedLockPort.class})' in source
    assert '@ConditionalOnMissingBean(CpfCacheAsideService.class)' in source

def test_redis_compatibility_uses_native_valkey_provider_without_missing_common_classes():
    source=text('cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/CpfRedisCompatibilityAutoConfiguration.java')
    assert 'ValkeyCpfCachePort cpfRedisCompatibilityCachePort' in source
    assert 'properties.setEnabled(true)' in source
    for stale in ('CpfRedisCacheProvider','CpfRedisConnectionFactoryBuilder','CpfJdbcCacheInvalidationStore'):
        assert stale not in source
    assert 'CpfRedisProperties' in source
    assert 'CpfCacheInvalidationCoordinator' in source
    assert 'JdbcCpfCacheInvalidationStore' in source

def test_valkey_health_does_not_false_green_durable_invalidation():
    source=text('cpf-starters/data/cache-valkey/src/main/java/com/cpf/starter/data/cache/valkey/ValkeyCpfCachePort.java')
    assert 'DURABLE_INVALIDATION_LEDGER_NOT_CONFIGURED' in source
    assert 'durableInvalidationConfigured' in source
    assert 'properties.isTls(), durableInvalidationConfigured' in source
