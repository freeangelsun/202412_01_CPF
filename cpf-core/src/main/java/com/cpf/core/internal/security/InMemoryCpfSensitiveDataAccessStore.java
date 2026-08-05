package com.cpf.core.internal.security;

import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessGrant;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessStatus;
import com.cpf.core.api.security.CpfSensitiveDataAccessRuntimeStatus;
import com.cpf.core.spi.security.CpfSensitiveDataAccessStore;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/** 단일 JVM bounded Provider. 다중 인스턴스 제품은 CAS를 보장하는 영속 Provider로 대체합니다. */
public final class InMemoryCpfSensitiveDataAccessStore
        implements CpfSensitiveDataAccessStore, CpfSensitiveDataAccessRuntimeStatus {
    public static final int DEFAULT_MAXIMUM_GRANTS = 10_000;
    public static final Duration DEFAULT_TERMINAL_RETENTION = Duration.ofHours(24);
    private static final int MAXIMUM_ALLOWED_GRANTS = 1_000_000;
    private static final Duration MAXIMUM_TERMINAL_RETENTION = Duration.ofDays(365);

    private final ConcurrentMap<String, AccessGrant> grants = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> terminalSince = new ConcurrentHashMap<>();
    private final Object mutationMonitor = new Object();
    private final int maximumGrants;
    private final Duration terminalRetention;
    private final Clock clock;
    private final AtomicLong evictionCount = new AtomicLong();
    private final AtomicLong capacityRejectionCount = new AtomicLong();
    private volatile Instant lastCapacityRejectionAt;

    public InMemoryCpfSensitiveDataAccessStore() {
        this(DEFAULT_MAXIMUM_GRANTS, DEFAULT_TERMINAL_RETENTION, Clock.systemUTC());
    }

    public InMemoryCpfSensitiveDataAccessStore(
            int maximumGrants,
            Duration terminalRetention,
            Clock clock) {
        if (maximumGrants < 1 || maximumGrants > MAXIMUM_ALLOWED_GRANTS) {
            throw new IllegalArgumentException("maximumGrants must be between 1 and 1000000");
        }
        this.terminalRetention = Objects.requireNonNull(terminalRetention, "terminalRetention");
        if (terminalRetention.isZero() || terminalRetention.isNegative()
                || terminalRetention.compareTo(MAXIMUM_TERMINAL_RETENTION) > 0) {
            throw new IllegalArgumentException("terminalRetention must be between 1ns and 365 days");
        }
        this.maximumGrants = maximumGrants;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CreateResult createIfAbsent(AccessGrant grant) {
        Objects.requireNonNull(grant, "grant");
        synchronized (mutationMonitor) {
            AccessGrant existing = grants.get(grant.requestId());
            if (existing != null) {
                return new CreateResult(false, existing);
            }
            Instant now = clock.instant();
            evictRetainedTerminalGrants(now);
            if (grants.size() >= maximumGrants) {
                capacityRejectionCount.incrementAndGet();
                lastCapacityRejectionAt = now;
                return CreateResult.exhausted();
            }
            grants.put(grant.requestId(), grant);
            recordTerminalTransition(grant, now);
            return new CreateResult(true, null);
        }
    }

    @Override
    public Optional<AccessGrant> find(String requestId) {
        return Optional.ofNullable(grants.get(Objects.requireNonNull(requestId, "requestId")));
    }

    @Override
    public boolean compareAndSet(String requestId, long expectedVersion, AccessGrant next) {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(next, "next");
        if (!requestId.equals(next.requestId())) {
            throw new IllegalArgumentException("next grant requestId must match store key");
        }
        synchronized (mutationMonitor) {
            AccessGrant current = grants.get(requestId);
            if (current == null || current.version() != expectedVersion) {
                return false;
            }
            grants.put(requestId, next);
            recordTerminalTransition(next, clock.instant());
            return true;
        }
    }

    @Override
    public RuntimeSnapshot snapshot() {
        Instant now = clock.instant();
        int count = grants.size();
        int terminalCount = 0;
        for (AccessGrant grant : grants.values()) {
            if (isTerminal(grant.status())) terminalCount++;
        }
        Health health = count >= maximumGrants ? Health.CAPACITY_EXHAUSTED : Health.HEALTHY;
        return new RuntimeSnapshot(
                health, count, terminalCount, maximumGrants, terminalRetention,
                evictionCount.get(), capacityRejectionCount.get(), lastCapacityRejectionAt, now);
    }

    private void recordTerminalTransition(AccessGrant grant, Instant now) {
        if (isTerminal(grant.status())) {
            terminalSince.putIfAbsent(grant.requestId(), now);
        } else {
            terminalSince.remove(grant.requestId());
        }
    }

    private void evictRetainedTerminalGrants(Instant now) {
        for (var entry : terminalSince.entrySet()) {
            Instant evictAt;
            try {
                evictAt = entry.getValue().plus(terminalRetention);
            } catch (RuntimeException overflow) {
                continue;
            }
            if (now.isBefore(evictAt)) continue;
            AccessGrant current = grants.get(entry.getKey());
            if (current == null) {
                terminalSince.remove(entry.getKey(), entry.getValue());
                continue;
            }
            if (!isTerminal(current.status())) {
                terminalSince.remove(entry.getKey(), entry.getValue());
                continue;
            }
            if (grants.remove(entry.getKey(), current)) {
                terminalSince.remove(entry.getKey(), entry.getValue());
                evictionCount.incrementAndGet();
            }
        }
    }

    private static boolean isTerminal(AccessStatus status) {
        return status == AccessStatus.REJECTED
                || status == AccessStatus.CONSUMED
                || status == AccessStatus.EXPIRED;
    }
}
