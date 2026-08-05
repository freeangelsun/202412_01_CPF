package com.cpf.core.internal.locking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.core.api.locking.CpfLockManager;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class DefaultCpfLockManagerTest {
    @Test
    void expiresLeaseAndRejectsStaleFence() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfLockManager manager = new DefaultCpfLockManager(new InMemoryCpfLockStore(), null, clock);
        CpfLockManager.AcquireResult first = manager.acquire("key", "owner-a", "request-a", Duration.ofSeconds(1));
        assertEquals(CpfLockManager.AcquireStatus.ACQUIRED, first.status());
        assertTrue(manager.validateFence("key", first.token().fencingToken()));
        clock.advance(Duration.ofSeconds(2));
        CpfLockManager.AcquireResult second = manager.acquire("key", "owner-b", "request-b", Duration.ofSeconds(1));
        assertEquals(CpfLockManager.AcquireStatus.ACQUIRED, second.status());
        assertTrue(second.token().fencingToken() > first.token().fencingToken());
        assertFalse(manager.validateFence("key", first.token().fencingToken()));
    }

    @Test
    void renewUsesOptimisticVersionAndRejectsARepeatedStaleToken() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfLockManager manager = new DefaultCpfLockManager(new InMemoryCpfLockStore(), null, clock);
        CpfLockManager.LockToken acquired = manager.acquire(
                "key", "owner", "request", Duration.ofSeconds(5)).token();

        CpfLockManager.RenewResult firstRenew = manager.renew(acquired, Duration.ofSeconds(5));

        assertEquals(CpfLockManager.RenewStatus.RENEWED, firstRenew.status());
        assertEquals(acquired.version() + 1L, firstRenew.token().version());
        assertEquals(CpfLockManager.RenewStatus.STALE_TOKEN,
                manager.renew(acquired, Duration.ofSeconds(5)).status());
        assertFalse(manager.validateToken(acquired));
        assertTrue(manager.validateToken(firstRenew.token()));
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis;
        private MutableClock(Instant initial) { millis = new AtomicLong(initial.toEpochMilli()); }
        private void advance(Duration duration) { millis.addAndGet(duration.toMillis()); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
    }
}
