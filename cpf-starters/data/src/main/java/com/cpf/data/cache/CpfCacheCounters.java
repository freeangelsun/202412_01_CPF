package com.cpf.data.cache;

import com.cpf.data.cache.api.CpfCacheMetricsSnapshot;
import java.time.Instant;
import java.util.concurrent.atomic.LongAdder;

/** Provider 구현이 공유하는 lock-free 누적 지표입니다. */
final class CpfCacheCounters {
    final LongAdder hits = new LongAdder();
    final LongAdder misses = new LongAdder();
    final LongAdder puts = new LongAdder();
    final LongAdder evictions = new LongAdder();
    final LongAdder errors = new LongAdder();
    final LongAdder lockContentions = new LongAdder();
    volatile long invalidationLag;

    CpfCacheMetricsSnapshot snapshot(String provider) {
        return new CpfCacheMetricsSnapshot(provider, hits.sum(), misses.sum(), puts.sum(),
                evictions.sum(), errors.sum(), lockContentions.sum(), Math.max(0, invalidationLag), Instant.now());
    }
}
