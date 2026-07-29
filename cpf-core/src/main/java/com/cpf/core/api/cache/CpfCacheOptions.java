package com.cpf.core.api.cache;

import java.time.Duration;

/** Cache Aside, Negative Cache와 분산 single-flight의 공통 정책입니다. */
public record CpfCacheOptions(Duration ttl, Duration negativeTtl, Duration lockWait,
                              Duration lockLease, boolean cacheNull, boolean failOpen) {
    public static final CpfCacheOptions DEFAULT = new CpfCacheOptions(
            Duration.ofMinutes(5), Duration.ofSeconds(20),
            Duration.ofSeconds(2), Duration.ofSeconds(10), true, true);
    public CpfCacheOptions {
        ttl = positive(ttl, "ttl");
        negativeTtl = positive(negativeTtl, "negativeTtl");
        lockWait = nonNegative(lockWait, "lockWait");
        lockLease = positive(lockLease, "lockLease");
        if (lockLease.compareTo(lockWait) < 0) throw new IllegalArgumentException("lockLease는 lockWait보다 짧을 수 없습니다.");
    }
    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) throw new IllegalArgumentException(name + "는 0보다 커야 합니다.");
        return value;
    }
    private static Duration nonNegative(Duration value, String name) {
        if (value == null || value.isNegative()) throw new IllegalArgumentException(name + "는 음수일 수 없습니다.");
        return value;
    }
}
