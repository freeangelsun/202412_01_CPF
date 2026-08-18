package com.cpf.data.cache.api;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Package-private cache-aside implementation shared by the public CpfCache contract. */
final class CpfCacheLoadingSupport {
    private static final ConcurrentHashMap<String, ReentrantLock> LOCAL_SINGLE_FLIGHT = new ConcurrentHashMap<>();

    private CpfCacheLoadingSupport() { }

    static CpfCacheValue getOrLoad(
            CpfCache cache,
            CpfCacheKey key,
            CpfCacheOptions options,
            CpfCacheLoader loader) {
        Objects.requireNonNull(cache, "cache");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(loader, "loader");

        CpfCacheValue cached = cacheGet(cache, key, options);
        if (cached.found()) return cached;

        if (cache instanceof CpfDistributedLockPort distributedLock) {
            return withDistributedSingleFlight(cache, distributedLock, key, options, loader);
        }
        return withLocalSingleFlight(cache, key, options, loader);
    }

    private static CpfCacheValue withDistributedSingleFlight(
            CpfCache cache,
            CpfDistributedLockPort locks,
            CpfCacheKey key,
            CpfCacheOptions options,
            CpfCacheLoader loader) {
        String lockName = "cache-load:" + key.canonical();
        Optional<CpfLockToken> token;
        try {
            token = locks.tryAcquire(lockName, options.lockWait(), options.lockLease());
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (RuntimeException lockFailure) {
            if (!options.failOpen()) throw lockFailure;
            return loadOrigin(key, loader);
        }

        if (token.isEmpty()) {
            CpfCacheValue second = cacheGet(cache, key, options);
            if (second.found()) return second;
            return loadOrigin(key, loader);
        }

        RuntimeException primaryFailure = null;
        try {
            return loadAndPopulate(cache, key, options, loader);
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (RuntimeException failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            try {
                boolean released = locks.release(token.orElseThrow());
                if (!released && !options.failOpen()) {
                    throw new IllegalStateException("CPF cache single-flight lock ownership release failed");
                }
            // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
            } catch (RuntimeException releaseFailure) {
                if (primaryFailure != null) primaryFailure.addSuppressed(releaseFailure);
                else if (!options.failOpen()) throw releaseFailure;
            }
        }
    }

    private static CpfCacheValue withLocalSingleFlight(
            CpfCache cache,
            CpfCacheKey key,
            CpfCacheOptions options,
            CpfCacheLoader loader) {
        String lockKey = Integer.toHexString(System.identityHashCode(cache)) + ':' + key.canonical();
        ReentrantLock lock = LOCAL_SINGLE_FLIGHT.computeIfAbsent(lockKey, ignored -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = tryLock(lock, options);
            if (!acquired) {
                CpfCacheValue second = cacheGet(cache, key, options);
                if (second.found()) return second;
                return loadOrigin(key, loader);
            }
            return loadAndPopulate(cache, key, options, loader);
        } finally {
            if (acquired) lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) LOCAL_SINGLE_FLIGHT.remove(lockKey, lock);
        }
    }

    private static boolean tryLock(ReentrantLock lock, CpfCacheOptions options) {
        try {
            if (options.lockWait().isZero()) return lock.tryLock();
            return lock.tryLock(options.lockWait().toNanos(), TimeUnit.NANOSECONDS);
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static CpfCacheValue loadAndPopulate(
            CpfCache cache,
            CpfCacheKey key,
            CpfCacheOptions options,
            CpfCacheLoader loader) {
        CpfCacheValue second = cacheGet(cache, key, options);
        if (second.found()) return second;

        CpfCacheValue loaded = loadOrigin(key, loader);
        if (!loaded.found()) {
            if (!options.cacheNull()) return CpfCacheValue.miss();
            CpfCacheValue negative = CpfCacheValue.negative(0, Instant.now().plus(options.negativeTtl()));
            cachePut(cache, key, negative, options.negativeTtl(), options);
            return negative;
        }
        cachePut(cache, key, loaded, options.ttl(), options);
        return loaded;
    }

    private static CpfCacheValue cacheGet(CpfCache cache, CpfCacheKey key, CpfCacheOptions options) {
        try {
            CpfCacheValue value = cache.get(key);
            return value == null ? CpfCacheValue.miss() : value;
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (RuntimeException cacheFailure) {
            if (options.failOpen()) return CpfCacheValue.miss();
            throw cacheFailure;
        }
    }

    private static void cachePut(
            CpfCache cache,
            CpfCacheKey key,
            CpfCacheValue value,
            java.time.Duration ttl,
            CpfCacheOptions options) {
        try {
            cache.put(key, value, ttl);
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (RuntimeException cacheFailure) {
            if (!options.failOpen()) throw cacheFailure;
        }
    }

    private static CpfCacheValue loadOrigin(CpfCacheKey key, CpfCacheLoader loader) {
        try {
            CpfCacheValue value = loader.load(key);
            return value == null ? CpfCacheValue.miss() : value;
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 Cache miss 시 single-flight·negative cache·fail-open 정책을 적용하는 내부 Cache-Aside 구현의 정책을 유지합니다.
        } catch (Exception originFailure) {
            throw originFailure instanceof RuntimeException runtime
                    ? runtime
                    : new IllegalStateException("CPF cache origin loader failed", originFailure);
        }
    }
}
