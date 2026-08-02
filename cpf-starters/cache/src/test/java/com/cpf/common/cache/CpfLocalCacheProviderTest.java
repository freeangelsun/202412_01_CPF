package com.cpf.common.cache;

import com.cpf.core.api.cache.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;

class CpfLocalCacheProviderTest {
    @Test void ttlNegativeCacheNamespaceAndFencing() {
        CpfLocalCacheProvider provider=new CpfLocalCacheProvider();
        CpfCacheKey key=new CpfCacheKey("code","A","TENANT");
        provider.put(key,new CpfCacheValue(true,false,"1".getBytes(StandardCharsets.UTF_8),
                "text/plain",1,Instant.now().plusSeconds(1)),Duration.ofSeconds(1));
        assertThat(provider.get(key).found()).isTrue();
        assertThat(provider.evictNamespace("TENANT","code")).isEqualTo(1);
        assertThat(provider.get(key).found()).isFalse();
        CpfLockToken first=provider.tryAcquire("x",Duration.ZERO,Duration.ofSeconds(1)).orElseThrow();
        assertThat(provider.tryAcquire("x",Duration.ZERO,Duration.ofSeconds(1))).isEmpty();
        CpfLockToken foreign = new CpfLockToken("x", "foreign", first.fencingToken(),
                first.acquiredAt(), first.expiresAt());
        assertThat(provider.release(foreign)).isFalse();
        assertThat(provider.release(first)).isTrue();
        assertThat(provider.release(first)).isFalse();
        CpfLockToken second=provider.tryAcquire("x",Duration.ZERO,Duration.ofSeconds(1)).orElseThrow();
        assertThat(second.fencingToken()).isGreaterThan(first.fencingToken());
    }
    @Test void lockWaitAcquiresAfterOwnerRelease() throws Exception {
        CpfLocalCacheProvider provider = new CpfLocalCacheProvider();
        CpfLockToken first = provider.tryAcquire("wait-lock", Duration.ZERO, Duration.ofSeconds(2)).orElseThrow();
        Thread releaser = Thread.startVirtualThread(() -> {
            try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            provider.release(first);
        });
        long started = System.nanoTime();
        CpfLockToken second = provider.tryAcquire("wait-lock", Duration.ofSeconds(1), Duration.ofSeconds(1)).orElseThrow();
        long waitedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        releaser.join();
        assertThat(waitedMillis).isGreaterThanOrEqualTo(50);
        assertThat(second.fencingToken()).isGreaterThan(first.fencingToken());
    }

}
