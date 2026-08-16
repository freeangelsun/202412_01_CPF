from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]

def text(rel): return (ROOT / rel).read_text(encoding="utf-8")


def test_shared_runtime_owns_protocol_and_durable_invalidation_semantics():
    port = text('cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/SpringDataRedisCpfCachePort.java')
    coordinator = text('cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinator.java')
    assert 'implements CpfCachePort, CpfDistributedLockPort' in port
    assert 'ScanOptions.scanOptions()' in port
    assert "redis.call('get', KEYS[1]) == ARGV[1]" in port
    assert 'durable.version(' in coordinator
    assert 'durable.advanceVersion(' in coordinator
    assert 'reconcileNow()' in coordinator


def test_redis_and_valkey_are_thin_explicit_providers_over_shared_runtime():
    redis = text('cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/RedisCpfCachePort.java')
    valkey = text('cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/ValkeyCpfCachePort.java')
    redis_auto = text('cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/CpfRedisCacheAutoConfiguration.java')
    valkey_auto = text('cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyAutoConfiguration.java')
    assert 'extends SpringDataRedisCpfCachePort' in redis
    assert 'extends SpringDataRedisCpfCachePort' in valkey
    for source in (redis_auto, valkey_auto):
        assert 'CpfCacheInvalidationCoordinator' in source
        assert 'JdbcCpfCacheInvalidationStore' in source
        assert 'CpfRedisProtocolProviderSelection.requireExclusive' in source


def test_adm_consumes_shared_invalidation_owner():
    adm = text('cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java')
    assert 'import com.cpf.data.cache.rediscommon.CpfCacheInvalidationCoordinator;' in adm
    assert 'import com.cpf.data.cache.CpfCacheInvalidationCoordinator;' not in adm


def test_valkey_legacy_duplicate_sources_are_tombstones_only():
    root = ROOT / 'cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache'
    for path in root.glob('Cpf*.java'):
        source = path.read_text(encoding='utf-8')
        assert 'public class Cpf' not in source
        assert 'public final class Cpf' not in source
        assert 'implements CpfCache' not in source
