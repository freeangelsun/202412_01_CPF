package com.cpf.data.cache;

import com.cpf.data.cache.api.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Consumer가 Provider 구현을 알지 않고 Cache Aside/Negative Cache/Single-flight를 사용하는 표준 Facade입니다.
 * Cache 장애의 fail-open과 원본 Loader 실패를 구분하여 조회 실패를 Cache miss로 위장하지 않습니다.
 */
public final class CpfCacheAsideService {
    private final CpfCache cache;
    private final CpfDistributedLockPort locks;

    public CpfCacheAsideService(CpfCache cache, CpfDistributedLockPort locks) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.locks = Objects.requireNonNull(locks, "locks");
    }

    public CpfCacheValue getOrLoad(CpfCacheKey key, CpfCacheOptions options, CpfCacheLoader loader) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(loader, "loader");

        CpfCacheValue cached = cacheGet(key, options);
        if (cached.found()) return cached;

        String lockName = "cache-load:" + key.canonical();
        java.util.Optional<CpfLockToken> token;
        try {
            token = locks.tryAcquire(lockName, options.lockWait(), options.lockLease());
        } catch (RuntimeException lockFailure) {
            if (!options.failOpen()) throw lockFailure;
            return loadOrigin(key, loader);
        }

        if (token.isEmpty()) {
            CpfCacheValue second = cacheGet(key, options);
            if (second.found()) return second;
            return loadOrigin(key, loader);
        }

        RuntimeException primaryFailure = null;
        try {
            CpfCacheValue second = cacheGet(key, options);
            if (second.found()) return second;

            CpfCacheValue loaded = loadOrigin(key, loader);
            if (!loaded.found()) {
                if (!options.cacheNull()) return CpfCacheValue.miss();
                CpfCacheValue negative = CpfCacheValue.negative(0, Instant.now().plus(options.negativeTtl()));
                cachePut(key, negative, options.negativeTtl(), options);
                return negative;
            }
            cachePut(key, loaded, options.ttl(), options);
            return loaded;
        } catch (RuntimeException failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            try {
                boolean released = locks.release(token.orElseThrow());
                if (!released && !options.failOpen()) {
                    throw new IllegalStateException("CPF cache single-flight lock 소유권 해제에 실패했습니다.");
                }
            } catch (RuntimeException releaseFailure) {
                if (primaryFailure != null) {
                    primaryFailure.addSuppressed(releaseFailure);
                } else if (!options.failOpen()) {
                    throw releaseFailure;
                }
            }
        }
    }

    private CpfCacheValue cacheGet(CpfCacheKey key, CpfCacheOptions options) {
        try {
            CpfCacheValue value = cache.get(key);
            return value == null ? CpfCacheValue.miss() : value;
        } catch (RuntimeException cacheFailure) {
            if (options.failOpen()) return CpfCacheValue.miss();
            throw cacheFailure;
        }
    }

    private void cachePut(CpfCacheKey key, CpfCacheValue value, java.time.Duration ttl, CpfCacheOptions options) {
        try {
            cache.put(key, value, ttl);
        } catch (RuntimeException cacheFailure) {
            if (!options.failOpen()) throw cacheFailure;
        }
    }

    private CpfCacheValue loadOrigin(CpfCacheKey key, CpfCacheLoader loader) {
        try {
            CpfCacheValue value = loader.load(key);
            return value == null ? CpfCacheValue.miss() : value;
        } catch (Exception originFailure) {
            throw originFailure instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("CPF cache 원본 Loader 실행에 실패했습니다.", originFailure);
        }
    }
}
