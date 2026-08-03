package com.cpf.common.cache;

import com.cpf.core.api.cache.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Local 개발과 장애 격리용 Provider입니다.
 * 운영 Profile에서는 AutoConfiguration이 명시적으로 차단합니다.
 */
public final class CpfLocalCacheProvider implements CpfCachePort, CpfDistributedLockPort {
    private static final Duration MAX_LOCK_WAIT = Duration.ofMinutes(5);
    private static final Duration MAX_LOCK_LEASE = Duration.ofHours(1);
    private final ConcurrentHashMap<String, Entry> values = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CpfLockToken> locks = new ConcurrentHashMap<>();
    private final AtomicLong fencing = new AtomicLong();
    private final CpfCacheCounters counters = new CpfCacheCounters();
    private volatile long lastSuccess = System.currentTimeMillis();

    @Override
    public CpfCacheValue get(CpfCacheKey key) {
        Objects.requireNonNull(key, "key");
        Entry entry = values.get(key.canonical());
        if (entry == null) { counters.misses.increment(); return CpfCacheValue.miss(); }
        if (!entry.expiresAt().isAfter(Instant.now())) {
            values.remove(key.canonical(), entry);
            counters.misses.increment();
            return CpfCacheValue.miss();
        }
        counters.hits.increment();
        lastSuccess = System.currentTimeMillis();
        return entry.value();
    }

    @Override
    public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        validateTtl(ttl);
        if (!value.found()) throw new IllegalArgumentException("Cache miss 값은 저장할 수 없습니다.");
        values.put(key.canonical(), new Entry(value, Instant.now().plus(ttl)));
        counters.puts.increment();
        lastSuccess = System.currentTimeMillis();
    }

    @Override
    public boolean evict(CpfCacheKey key) {
        Objects.requireNonNull(key, "key");
        boolean removed = values.remove(key.canonical()) != null;
        if (removed) counters.evictions.increment();
        return removed;
    }

    @Override
    public long evictNamespace(String tenantId, String namespace) {
        String sample = new CpfCacheKey(namespace, "_", tenantId).canonical();
        String prefix = sample.substring(0, sample.length() - 1);
        List<String> targets = new ArrayList<>();
        values.keySet().forEach(key -> { if (key.startsWith(prefix)) targets.add(key); });
        long removed = 0;
        for (String target : targets) {
            if (values.remove(target) != null) removed++;
        }
        counters.evictions.add(removed);
        return removed;
    }

    @Override

    public CpfCacheMetricsSnapshot metrics() { return counters.snapshot("LOCAL"); }

    @Override
    public CpfCacheHealth health() {
        return new CpfCacheHealth(true, "LOCAL", "SINGLE_JVM", false, false,
                lastSuccess, List.of("LOCAL_SIMULATOR", "DURABLE_INVALIDATION_EXTERNAL"), Instant.now());
    }

    @Override
    public Optional<CpfLockToken> tryAcquire(String lockName, Duration wait, Duration lease) {
        validateLock(lockName, wait, lease);
        long deadline = System.nanoTime() + wait.toNanos();
        do {
            Instant now = Instant.now();
            AtomicReference<CpfLockToken> acquired = new AtomicReference<>();
            locks.compute(lockName, (key, current) -> {
                if (current == null || !current.expiresAt().isAfter(now)) {
                    CpfLockToken token = new CpfLockToken(lockName, UUID.randomUUID().toString(),
                            fencing.incrementAndGet(), now, now.plus(lease));
                    acquired.set(token);
                    return token;
                }
                return current;
            });
            if (acquired.get() != null) {
                lastSuccess = System.currentTimeMillis();
                return Optional.of(acquired.get());
            }
            counters.lockContentions.increment();
            if (wait.isZero()) break;
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) break;
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
        java.util.concurrent.atomic.AtomicBoolean released = new java.util.concurrent.atomic.AtomicBoolean(false);
        locks.computeIfPresent(token.lockName(), (key, current) -> {
            boolean ownerMatch = current.ownerId().equals(token.ownerId())
                    && current.fencingToken() == token.fencingToken();
            if (ownerMatch) {
                released.set(true);
                return null;
            }
            return current;
        });
        return released.get();
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Cache TTL은 0보다 커야 합니다.");
        }
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

    private record Entry(CpfCacheValue value, Instant expiresAt) { }
}
