package com.cpf.data.cache.rediscommon;

import com.cpf.data.cache.api.CpfCacheHealth;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCacheMetricsSnapshot;
import com.cpf.data.cache.api.CpfCachePort;
import com.cpf.data.cache.api.CpfCacheValue;
import com.cpf.data.cache.api.CpfDistributedLockPort;
import com.cpf.data.cache.api.CpfLockToken;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/** Shared Spring Data Redis-protocol CPF L2 cache and fencing-lock runtime for Redis/Valkey providers. */
public class SpringDataRedisCpfCachePort implements CpfCachePort, CpfDistributedLockPort {
    private static final Duration MAX_LOCK_WAIT = Duration.ofMinutes(5);
    private static final Duration MAX_LOCK_LEASE = Duration.ofHours(1);
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;
    private final CpfRedisLikeProviderProperties properties;
    private final String providerName;
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder puts = new LongAdder();
    private final LongAdder evictions = new LongAdder();
    private final LongAdder errors = new LongAdder();
    private final LongAdder lockContentions = new LongAdder();
    private final LongAdder reconnects = new LongAdder();
    private final AtomicReference<Boolean> lastHealthReady = new AtomicReference<>();
    private volatile long lastSuccessEpochMillis;
    private final boolean durableInvalidationConfigured;

    public SpringDataRedisCpfCachePort(String providerName, StringRedisTemplate redis, CpfRedisLikeProviderProperties properties) {
        this(providerName, redis, properties, false);
    }

    public SpringDataRedisCpfCachePort(
            String providerName,
            StringRedisTemplate redis,
            CpfRedisLikeProviderProperties properties,
            boolean durableInvalidationConfigured) {
        this.providerName = Objects.requireNonNull(providerName, "providerName").trim().toUpperCase(java.util.Locale.ROOT);
        this.redis = Objects.requireNonNull(redis, "redis");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.durableInvalidationConfigured = durableInvalidationConfigured;
        properties.validate(this.providerName);
    }

    @Override
    public CpfCacheValue get(CpfCacheKey key) {
        Objects.requireNonNull(key, "key");
        try {
            String encoded = redis.opsForValue().get(cacheKey(key));
            if (encoded == null) {
                misses.increment();
                return CpfCacheValue.miss();
            }
            CpfCacheValue value = decode(encoded);
            if (value.expiresAt() != null && !value.expiresAt().isAfter(Instant.now())) {
                redis.delete(cacheKey(key));
                misses.increment();
                return CpfCacheValue.miss();
            }
            hits.increment();
            markSuccess();
            return value;
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    @Override
    public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        validateTtl(ttl);
        if (!value.found()) {
            throw new IllegalArgumentException("Cache miss 값은 저장할 수 없습니다.");
        }
        if (value.payload().length > properties.getMaximumPayloadBytes()) {
            throw new IllegalArgumentException("cache payload exceeds configured maximum-payload-bytes");
        }
        try {
            CpfCacheValue stored = new CpfCacheValue(
                    true, value.negative(), value.payload(), value.contentType(), value.version(), Instant.now().plus(ttl));
            redis.opsForValue().set(cacheKey(key), encode(stored), ttl);
            puts.increment();
            markSuccess();
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    @Override
    public boolean evict(CpfCacheKey key) {
        Objects.requireNonNull(key, "key");
        try {
            boolean removed = Boolean.TRUE.equals(redis.delete(cacheKey(key)));
            if (removed) {
                evictions.increment();
                publishInvalidation(key.canonical());
            }
            markSuccess();
            return removed;
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    @Override
    public long evictNamespace(String tenantId, String namespace) {
        CpfCacheKey sample = new CpfCacheKey(namespace, "_", tenantId);
        String canonicalPrefix = sample.canonical().substring(0, sample.canonical().length() - 1);
        String pattern = properties.getKeyPrefix() + canonicalPrefix.substring("cpf:".length()) + "*";
        List<String> keys = scan(pattern);
        if (keys.isEmpty()) {
            markSuccess();
            return 0;
        }
        try {
            Long removed = redis.delete(keys);
            long count = removed == null ? 0 : removed;
            evictions.add(count);
            if (count > 0) {
                publishInvalidation(sample.tenantId() + ":" + sample.namespace() + ":*");
            }
            markSuccess();
            return count;
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    @Override
    public CpfCacheMetricsSnapshot metrics() {
        return new CpfCacheMetricsSnapshot(
                providerName, hits.sum(), misses.sum(), puts.sum(), evictions.sum(), errors.sum(),
                lockContentions.sum(), reconnects.sum(), Instant.now());
    }

    @Override
    public CpfCacheHealth health() {
        List<String> reasons = new ArrayList<>();
        boolean ready = false;
        try (RedisConnection connection = Objects.requireNonNull(redis.getConnectionFactory(), "connectionFactory")
                .getConnection()) {
            ready = "PONG".equalsIgnoreCase(connection.ping());
            if (!ready) {
                reasons.add("PING_NOT_PONG");
            }
            if (!durableInvalidationConfigured) {
                reasons.add("DURABLE_INVALIDATION_LEDGER_NOT_CONFIGURED");
                if (properties.isRequired()) {
                    ready = false;
                }
            }
        } catch (RuntimeException failure) {
            errors.increment();
            reasons.add(providerName + "_UNAVAILABLE");
        }
        Boolean previousReady = lastHealthReady.getAndSet(ready);
        if (Boolean.FALSE.equals(previousReady) && ready) {
            reconnects.increment();
        }
        if (ready) {
            markSuccess();
        }
        return new CpfCacheHealth(
                ready, providerName, "DISTRIBUTED_L2", properties.isTls(), durableInvalidationConfigured,
                lastSuccessEpochMillis, reasons, Instant.now());
    }

    @Override
    public Optional<CpfLockToken> tryAcquire(String lockName, Duration wait, Duration lease) {
        validateLock(lockName, wait, lease);
        long deadline = System.nanoTime() + wait.toNanos();
        do {
            try {
                Long fencingToken = redis.opsForValue().increment(fenceKey(lockName));
                if (fencingToken == null || fencingToken <= 0) {
                    throw new IllegalStateException(providerName + " fencing token allocation failed");
                }
                Instant acquiredAt = Instant.now();
                String ownerId = UUID.randomUUID().toString();
                String storedOwner = ownerId + "|" + fencingToken;
                Boolean acquired = redis.opsForValue().setIfAbsent(lockKey(lockName), storedOwner, lease);
                if (Boolean.TRUE.equals(acquired)) {
                    markSuccess();
                    return Optional.of(new CpfLockToken(
                            lockName, ownerId, fencingToken, acquiredAt, acquiredAt.plus(lease)));
                }
                lockContentions.increment();
            } catch (RuntimeException failure) {
                errors.increment();
                throw failure;
            }
            if (wait.isZero()) {
                break;
            }
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                break;
            }
            try {
                TimeUnit.NANOSECONDS.sleep(Math.min(remainingNanos, TimeUnit.MILLISECONDS.toNanos(25)));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        } while (System.nanoTime() < deadline);
        return Optional.empty();
    }

    @Override
    public boolean release(CpfLockToken token) {
        Objects.requireNonNull(token, "token");
        try {
            String expectedOwner = token.ownerId() + "|" + token.fencingToken();
            Long removed = redis.execute(RELEASE_SCRIPT, List.of(lockKey(token.lockName())), expectedOwner);
            boolean released = removed != null && removed == 1L;
            if (released) {
                markSuccess();
            }
            return released;
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    private List<String> scan(String pattern) {
        List<String> keys = new ArrayList<>();
        try (RedisConnection connection = Objects.requireNonNull(redis.getConnectionFactory(), "connectionFactory")
                .getConnection();
             Cursor<byte[]> cursor = connection.scan(
                     ScanOptions.scanOptions().match(pattern).count(properties.getScanCount()).build())) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
            return keys;
        } catch (RuntimeException failure) {
            errors.increment();
            throw failure;
        }
    }

    private void publishInvalidation(String subject) {
        redis.convertAndSend(properties.getInvalidationChannel(), subject);
    }

    private String cacheKey(CpfCacheKey key) {
        return properties.getKeyPrefix() + key.canonical().substring("cpf:".length());
    }

    private String lockKey(String lockName) {
        return properties.getKeyPrefix() + "lock:" + lockName;
    }

    private String fenceKey(String lockName) {
        return properties.getKeyPrefix() + "fence:" + lockName;
    }

    private static String encode(CpfCacheValue value) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String contentType = encoder.encodeToString(value.contentType().getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(value.payload());
        long expiresAt = value.expiresAt() == null ? 0 : value.expiresAt().toEpochMilli();
        return value.version() + "|" + value.negative() + "|" + expiresAt + "|" + contentType + "|" + payload;
    }

    private static CpfCacheValue decode(String encoded) {
        String[] fields = encoded.split("\\|", -1);
        if (fields.length != 5) {
            throw new IllegalStateException("Invalid CPF cache payload envelope");
        }
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            long version = Long.parseLong(fields[0]);
            boolean negative = Boolean.parseBoolean(fields[1]);
            long expiresAt = Long.parseLong(fields[2]);
            String contentType = new String(decoder.decode(fields[3]), StandardCharsets.UTF_8);
            byte[] payload = decoder.decode(fields[4]);
            return new CpfCacheValue(
                    true, negative, payload, contentType, version,
                    expiresAt <= 0 ? null : Instant.ofEpochMilli(expiresAt));
        } catch (RuntimeException malformed) {
            throw new IllegalStateException("Invalid CPF cache payload envelope", malformed);
        }
    }

    private static void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Cache TTL은 0보다 커야 합니다.");
        }
    }

    private static void validateLock(String lockName, Duration wait, Duration lease) {
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

    private void markSuccess() {
        lastSuccessEpochMillis = System.currentTimeMillis();
    }
}
