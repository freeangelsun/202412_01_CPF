package com.cpf.data.cache;

import com.cpf.data.cache.api.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCacheAsideServiceTest {
    @Test
    void cacheFailureCanFailOpenButOriginFailureNeverBecomesMiss() {
        CpfCachePort failingCache = new FailingCache();
        CpfDistributedLockPort failingLock = new FailingLock();
        CpfCacheAsideService service = new CpfCacheAsideService(failingCache, failingLock);
        CpfCacheOptions failOpen = new CpfCacheOptions(Duration.ofMinutes(1), Duration.ofSeconds(5),
                Duration.ZERO, Duration.ofSeconds(1), true, true);
        CpfCacheValue loaded = service.getOrLoad(new CpfCacheKey("code", "A", "TENANT"), failOpen,
                key -> new CpfCacheValue(true, false, new byte[]{1}, "application/octet-stream", 1,
                        Instant.now().plusSeconds(10)));
        assertThat(loaded.found()).isTrue();

        assertThatThrownBy(() -> service.getOrLoad(new CpfCacheKey("code", "B", "TENANT"), failOpen,
                key -> { throw new IllegalStateException("origin-down"); }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("origin-down");
    }

    private static final class FailingCache implements CpfCachePort {
        public CpfCacheValue get(CpfCacheKey key){throw new IllegalStateException("cache-down");}
        public void put(CpfCacheKey key,CpfCacheValue value,Duration ttl){throw new IllegalStateException("cache-down");}
        public boolean evict(CpfCacheKey key){return false;}
        public long evictNamespace(String tenantId,String namespace){return 0;}
        public CpfCacheMetricsSnapshot metrics(){return new CpfCacheMetricsSnapshot("FAIL",0,0,0,0,1,0,0,Instant.now());}
        public CpfCacheHealth health(){return new CpfCacheHealth(false,"FAIL","NONE",false,false,0,java.util.List.of("DOWN"),Instant.now());}
    }
    private static final class FailingLock implements CpfDistributedLockPort {
        public Optional<CpfLockToken> tryAcquire(String name,Duration wait,Duration lease){throw new IllegalStateException("lock-down");}
        public boolean release(CpfLockToken token){return false;}
    }
}
