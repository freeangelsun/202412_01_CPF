package com.cpf.core.internal.locking;

import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.api.locking.CpfLockRuntimeStatus;
import com.cpf.core.spi.locking.CpfLockAuditSink;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

public final class CpfLockRecoveryMonitoringHarness {
    public static void main(String[] args) {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        DefaultCpfLockManager manager = new DefaultCpfLockManager(
                new InMemoryCpfLockStore(), CpfLockAuditSink.unavailable(), clock);
        CpfLockManager.AcquireResult acquired = manager.acquire("job:1", "node-a", "req-1", Duration.ofSeconds(1));
        check(acquired.status() == CpfLockManager.AcquireStatus.ACQUIRED, "acquire");
        CpfLockRuntimeStatus.LockRuntimeSnapshot healthy = manager.lockRuntimeSnapshot(100);
        check(healthy.active() == 1 && healthy.health() == CpfLockRuntimeStatus.Health.UP, "healthy monitoring");
        clock.advance(Duration.ofSeconds(2));
        CpfLockRuntimeStatus.LockRuntimeSnapshot degraded = manager.lockRuntimeSnapshot(100);
        check(degraded.expiredButUnreconciled() == 1 && degraded.health() == CpfLockRuntimeStatus.Health.DEGRADED,
                "expired monitoring");
        CpfLockManager.RecoveryResult recovery = manager.reconcileExpired(100);
        check(recovery.status() == CpfLockManager.RecoveryStatus.SUCCESS && recovery.recovered() == 1,
                "expired reconciliation");
        check(manager.find("job:1").orElseThrow().state() == CpfLockManager.State.EXPIRED, "durable expired state");
        check(!manager.validateToken(acquired.token()), "stale token rejected after recovery");
        CpfLockManager.AcquireResult takeover = manager.acquire("job:1", "node-b", "req-2", Duration.ofSeconds(5));
        check(takeover.status() == CpfLockManager.AcquireStatus.ACQUIRED, "takeover");
        check(takeover.token().fencingToken() > acquired.token().fencingToken(), "fencing monotonic");
        check(takeover.token().ownerEpoch() > acquired.token().ownerEpoch(), "owner epoch monotonic");

        InMemoryCpfLockStore boundedStore = new InMemoryCpfLockStore(1, clock);
        DefaultCpfLockManager bounded = new DefaultCpfLockManager(
                boundedStore, CpfLockAuditSink.unavailable(), clock);
        CpfLockManager.AcquireResult first = bounded.acquire(
                "bounded:1", "node-a", "req-a", Duration.ofSeconds(5));
        check(first.status() == CpfLockManager.AcquireStatus.ACQUIRED, "bounded first acquire");
        check(bounded.release(first.token(), "capacity test release").status()
                == CpfLockManager.ReleaseStatus.RELEASED, "bounded release");
        CpfLockManager.AcquireResult exhausted = bounded.acquire(
                "bounded:2", "node-b", "req-b", Duration.ofSeconds(5));
        check(exhausted.status() == CpfLockManager.AcquireStatus.RESOURCE_EXHAUSTED,
                "new key must fail closed at capacity");
        CpfLockManager.AcquireResult sameKey = bounded.acquire(
                "bounded:1", "node-c", "req-c", Duration.ofSeconds(5));
        check(sameKey.status() == CpfLockManager.AcquireStatus.ACQUIRED,
                "existing key remains usable at capacity");
        check(sameKey.token().fencingToken() > first.token().fencingToken(),
                "fencing history must never be reused");
        CpfLockRuntimeStatus.LockRuntimeSnapshot capacity = bounded.lockRuntimeSnapshot(100);
        check(capacity.health() == CpfLockRuntimeStatus.Health.CAPACITY_EXHAUSTED,
                "capacity health");
        check(capacity.trackedKeyCount() == 1 && capacity.maximumTrackedKeys() == 1,
                "capacity counters");
        check(capacity.capacityRejectionCount() == 1 && capacity.lastCapacityRejectionAt() != null,
                "capacity rejection evidence");
        System.out.println("CPF_LOCK_RECOVERY_MONITORING_HARNESS_PASS");
    }

    private static void check(boolean value, String label) { if (!value) throw new AssertionError(label); }
    private static final class MutableClock extends Clock {
        private Instant current;
        private MutableClock(Instant current) { this.current = current; }
        void advance(Duration duration) { current = current.plus(duration); }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }
}
