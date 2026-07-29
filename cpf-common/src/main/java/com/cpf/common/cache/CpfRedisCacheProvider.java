package com.cpf.common.cache;

import com.cpf.core.api.cache.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Redis 공유 Cache와 Lua fencing lock을 제공하는 실제 Adapter입니다. */
public final class CpfRedisCacheProvider implements CpfCachePort, CpfDistributedLockPort {
    private static final byte FORMAT_VERSION = 1;
    private static final int MAX_CONTENT_TYPE_BYTES = 1024;
    private static final int MAX_PAYLOAD_BYTES = 16 * 1024 * 1024;
    private static final Duration MAX_LOCK_WAIT = Duration.ofMinutes(5);
    private static final Duration MAX_LOCK_LEASE = Duration.ofHours(1);
    private final RedisTemplate<String, byte[]> redis;
    private final CpfRedisProperties properties;
    private final CpfCacheCounters counters = new CpfCacheCounters();
    private volatile long lastSuccess;

    public CpfRedisCacheProvider(RedisTemplate<String, byte[]> redis, CpfRedisProperties properties) {
        this.redis = java.util.Objects.requireNonNull(redis, "redis");
        this.properties = java.util.Objects.requireNonNull(properties, "properties");
        properties.validate();
    }

    @Override
    public CpfCacheValue get(CpfCacheKey key) {
        try {
            byte[] encoded = redis.opsForValue().get(key.canonical());
            if (encoded == null) { counters.misses.increment(); return CpfCacheValue.miss(); }
            CpfCacheValue value = decode(encoded);
            counters.hits.increment(); success();
            return value;
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (properties.isFailOpen()) return CpfCacheValue.miss();
            throw error;
        }
    }

    @Override
    public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) throw new IllegalArgumentException("Cache TTL은 0보다 커야 합니다.");
        try {
            redis.opsForValue().set(key.canonical(), encode(value), ttl);
            counters.puts.increment(); success();
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (!properties.isFailOpen()) throw error;
        }
    }

    @Override
    public boolean evict(CpfCacheKey key) {
        try {
            boolean removed = Boolean.TRUE.equals(redis.delete(key.canonical()));
            if (removed) counters.evictions.increment();
            success();
            return removed;
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (!properties.isFailOpen()) throw error;
            return false;
        }
    }

    @Override
    public long evictNamespace(String tenantId, String namespace) {
        String prefix = new CpfCacheKey(namespace, "_", tenantId).canonical();
        prefix = prefix.substring(0, prefix.length() - 1);
        final String pattern = prefix + "*";
        try {
            Long removed = redis.execute((RedisCallback<Long>) connection -> scanDelete(connection, pattern));
            long count = removed == null ? 0 : removed;
            counters.evictions.add(count);
            success();
            return count;
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (!properties.isFailOpen()) throw error;
            return 0;
        }
    }

    private long scanDelete(RedisConnection connection, String pattern) {
        long removed = 0;
        try (Cursor<byte[]> cursor = connection.keyCommands().scan(
                ScanOptions.scanOptions().match(pattern).count(500).build())) {
            java.util.ArrayList<byte[]> batch = new java.util.ArrayList<>(500);
            while (cursor.hasNext()) {
                batch.add(cursor.next());
                if (batch.size() == 500) {
                    removed += connection.keyCommands().del(batch.toArray(byte[][]::new));
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) removed += connection.keyCommands().del(batch.toArray(byte[][]::new));
        }
        return removed;
    }

    @Override

    public CpfCacheMetricsSnapshot metrics() { return counters.snapshot("REDIS"); }

    @Override
    public CpfCacheHealth health() {
        try (RedisConnection connection = redis.getConnectionFactory().getConnection()) {
            String pong = connection.ping();
            boolean ready = pong != null && pong.equalsIgnoreCase("PONG");
            if (ready) success();
            return new CpfCacheHealth(ready, "REDIS", properties.getTopology().name(),
                    properties.isTls(), true, lastSuccess,
                    ready ? List.of() : List.of("PING_FAILED"), Instant.now());
        } catch (RuntimeException error) {
            counters.errors.increment();
            return new CpfCacheHealth(false, "REDIS", properties.getTopology().name(),
                    properties.isTls(), false, lastSuccess,
                    List.of("REDIS_UNAVAILABLE"), Instant.now());
        }
    }

    @Override
    public Optional<CpfLockToken> tryAcquire(String lockName, Duration wait, Duration lease) {
        validateLock(lockName, wait, lease);
        String lockKey = "cpf:lock:" + lockName;
        String fenceKey = "cpf:lock:fence:" + lockName;
        String owner = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + wait.toNanos();
        DefaultRedisScript<Long> acquireScript = new DefaultRedisScript<>("""
                if redis.call('set', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then
                  return redis.call('incr', KEYS[2])
                end
                return 0
                """, Long.class);
        try {
            do {
                Long fence = redis.execute(acquireScript, List.of(lockKey, fenceKey),
                        owner.getBytes(StandardCharsets.UTF_8),
                        Long.toString(lease.toMillis()).getBytes(StandardCharsets.UTF_8));
                if (fence != null && fence > 0) {
                    success();
                    return Optional.of(new CpfLockToken(lockName, owner, fence,
                            Instant.now(), Instant.now().plus(lease)));
                }
                counters.lockContentions.increment();
                if (wait.isZero()) break;
                long remainingMillis = Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
                try { TimeUnit.MILLISECONDS.sleep(Math.min(25, remainingMillis)); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } while (System.nanoTime() < deadline);
            return Optional.empty();
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (!properties.isFailOpen()) throw error;
            return Optional.empty();
        }
    }

    @Override
    public boolean release(CpfLockToken token) {
        java.util.Objects.requireNonNull(token, "token");
        String key = "cpf:lock:" + token.lockName();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end",
                Long.class);
        try {
            Long result = redis.execute(script, List.of(key), token.ownerId().getBytes(StandardCharsets.UTF_8));
            if (result != null && result == 1L) success();
            return result != null && result == 1L;
        } catch (RuntimeException error) {
            counters.errors.increment();
            if (!properties.isFailOpen()) throw error;
            return false;
        }
    }

    private byte[] encode(CpfCacheValue value) {
        byte[] contentType = value.contentType().getBytes(StandardCharsets.UTF_8);
        byte[] payload = value.payload();
        long expires = value.expiresAt() == null ? 0L : value.expiresAt().toEpochMilli();
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 8 + 8 + 4 + contentType.length + 4 + payload.length);
        buffer.put(FORMAT_VERSION).put((byte) (value.negative() ? 1 : 0))
                .putLong(value.version()).putLong(expires)
                .putInt(contentType.length).put(contentType)
                .putInt(payload.length).put(payload);
        return buffer.array();
    }

    private CpfCacheValue decode(byte[] encoded) {
        if (encoded == null || encoded.length < 26 || encoded.length > MAX_PAYLOAD_BYTES + MAX_CONTENT_TYPE_BYTES + 64) {
            throw new IllegalStateException("CPF Cache envelope 길이가 유효하지 않습니다.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        if (buffer.get() != FORMAT_VERSION) throw new IllegalStateException("지원하지 않는 CPF Cache envelope입니다.");
        boolean negative = buffer.get() == 1;
        long version = buffer.getLong();
        long expires = buffer.getLong();
        int contentTypeLength = bounded(buffer.getInt(), Math.min(MAX_CONTENT_TYPE_BYTES, buffer.remaining() - 4));
        byte[] contentType = new byte[contentTypeLength]; buffer.get(contentType);
        int payloadLength = bounded(buffer.getInt(), Math.min(MAX_PAYLOAD_BYTES, buffer.remaining()));
        if (payloadLength != buffer.remaining()) throw new IllegalStateException("CPF Cache envelope trailing/short payload 오류");
        byte[] payload = new byte[payloadLength]; buffer.get(payload);
        return new CpfCacheValue(true, negative, payload,
                new String(contentType, StandardCharsets.UTF_8), version,
                expires == 0 ? null : Instant.ofEpochMilli(expires));
    }

    private void validateLock(String lockName, Duration wait, Duration lease) {
        if (lockName == null || !lockName.matches("[A-Za-z0-9._:-]{1,180}")) {
            throw new IllegalArgumentException("lockName 형식이 올바르지 않습니다.");
        }
        if (wait == null || wait.isNegative() || wait.compareTo(MAX_LOCK_WAIT) > 0) {
            throw new IllegalArgumentException("Lock wait는 0~5분 범위여야 합니다.");
        }
        if (lease == null || lease.isZero() || lease.isNegative() || lease.compareTo(MAX_LOCK_LEASE) > 0) {
            throw new IllegalArgumentException("Lock lease는 0초 초과 1시간 이하여야 합니다.");
        }
    }

    private int bounded(int value, int max) {
        if (value < 0 || value > max) throw new IllegalStateException("손상된 CPF Cache envelope입니다.");
        return value;
    }
    private void success() { lastSuccess = System.currentTimeMillis(); }
}
