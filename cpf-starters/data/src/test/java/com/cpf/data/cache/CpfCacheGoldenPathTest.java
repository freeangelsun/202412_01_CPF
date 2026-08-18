package com.cpf.data.cache;

import com.cpf.data.cache.api.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCacheGoldenPathTest {
    @Test
    void cacheFailureCanFailOpenButOriginFailureNeverBecomesMiss() {
        CpfCache failingCache = new FailingCache();
        CpfCacheOptions failOpen = new CpfCacheOptions(Duration.ofMinutes(1), Duration.ofSeconds(5),
                Duration.ZERO, Duration.ofSeconds(1), true, true);
        CpfCacheValue loaded = failingCache.getOrLoad(new CpfCacheKey("code", "A", "TENANT"), failOpen,
                key -> value((byte) 1));
        assertThat(loaded.found()).isTrue();

        assertThatThrownBy(() -> failingCache.getOrLoad(new CpfCacheKey("code", "B", "TENANT"), failOpen,
                key -> { throw new IllegalStateException("origin-down"); }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("origin-down");
    }

    @Test
    void localSingleFlightLoadsOnceForSequentialMissThenHit() {
        InMemoryCache cache = new InMemoryCache();
        AtomicInteger loads = new AtomicInteger();
        CpfCacheOptions options = new CpfCacheOptions(Duration.ofMinutes(1), Duration.ofSeconds(5),
                Duration.ofMillis(100), Duration.ofSeconds(1), true, false);
        CpfCacheKey key = new CpfCacheKey("member", "42", "TENANT");

        CpfCacheValue first = cache.getOrLoad(key, options, ignored -> {
            loads.incrementAndGet();
            return value((byte) 7);
        });
        CpfCacheValue second = cache.getOrLoad(key, options, ignored -> {
            loads.incrementAndGet();
            return value((byte) 9);
        });

        assertThat(first.payload()).containsExactly((byte) 7);
        assertThat(second.payload()).containsExactly((byte) 7);
        assertThat(loads).hasValue(1);
    }

    private static CpfCacheValue value(byte payload) {
        return new CpfCacheValue(true, false, new byte[]{payload}, "application/octet-stream", 1,
                Instant.now().plusSeconds(60));
    }

    private static final class FailingCache implements CpfCache {
        public CpfCacheValue get(CpfCacheKey key){throw new IllegalStateException("cache-down");}
        public void put(CpfCacheKey key,CpfCacheValue value,Duration ttl){throw new IllegalStateException("cache-down");}
        public boolean evict(CpfCacheKey key){return false;}
        public long evictNamespace(String tenantId,String namespace){return 0;}
        public CpfCacheMetricsSnapshot metrics(){return new CpfCacheMetricsSnapshot("FAIL",0,0,0,0,1,0,0,Instant.now());}
        public CpfCacheHealth health(){return new CpfCacheHealth(false,"FAIL","NONE",false,false,0,java.util.List.of("DOWN"),Instant.now());}
    }

    private static final class InMemoryCache implements CpfCache {
        private CpfCacheValue value = CpfCacheValue.miss();
        public CpfCacheValue get(CpfCacheKey key){return value;}
        public void put(CpfCacheKey key,CpfCacheValue value,Duration ttl){this.value=value;}
        public boolean evict(CpfCacheKey key){value=CpfCacheValue.miss(); return true;}
        public long evictNamespace(String tenantId,String namespace){value=CpfCacheValue.miss(); return 1;}
        public CpfCacheMetricsSnapshot metrics(){return new CpfCacheMetricsSnapshot("MEM",0,0,0,0,0,0,0,Instant.now());}
        public CpfCacheHealth health(){return new CpfCacheHealth(true,"MEM","JVM",false,false,System.currentTimeMillis(),java.util.List.of(),Instant.now());}
    }
}
