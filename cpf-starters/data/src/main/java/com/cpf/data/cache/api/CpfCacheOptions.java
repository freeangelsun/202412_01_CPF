package com.cpf.data.cache.api;

import java.time.Duration;
import java.util.Objects;

/** Cache-aside의 만료시간, single-flight 대기/lease, null 저장과 장애 처리 정책을 정의합니다. */
public record CpfCacheOptions(
        Duration ttl,
        Duration negativeTtl,
        Duration lockWait,
        Duration lockLease,
        boolean cacheNull,
        boolean failOpen) {
    public CpfCacheOptions {
        ttl = requirePositive(ttl, "ttl");
        negativeTtl = requirePositive(negativeTtl, "negativeTtl");
        lockWait = requireNonNegative(lockWait, "lockWait");
        lockLease = requirePositive(lockLease, "lockLease");
    }

    private static Duration requirePositive(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static Duration requireNonNegative(Duration value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isNegative()) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }
}
