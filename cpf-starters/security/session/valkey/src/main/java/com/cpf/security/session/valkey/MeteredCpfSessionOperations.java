package com.cpf.security.session.valkey;

import com.cpf.security.api.CpfSessionMetrics;
import com.cpf.security.api.CpfSessionMetricsSnapshot;
import com.cpf.security.api.CpfSessionOperations;
import com.cpf.security.api.CpfSessionSnapshot;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

/** Adds provider-neutral operational metrics without duplicating Valkey storage semantics. */
public final class MeteredCpfSessionOperations implements CpfSessionOperations, CpfSessionMetrics {
    private final CpfSessionOperations delegate;
    private final LongAdder creates = new LongAdder();
    private final LongAdder reads = new LongAdder();
    private final LongAdder updates = new LongAdder();
    private final LongAdder revocations = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder providerFailures = new LongAdder();
    private final LongAdder forcedLogouts = new LongAdder();

    public MeteredCpfSessionOperations(CpfSessionOperations delegate) {
        this.delegate = java.util.Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public CpfSessionSnapshot create(String tenantId, String principalId, Duration ttl, Map<String, String> attributes) {
        try {
            CpfSessionSnapshot result = delegate.create(tenantId, principalId, ttl, attributes);
            creates.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public Optional<CpfSessionSnapshot> find(String sessionId) {
        try {
            Optional<CpfSessionSnapshot> result = delegate.find(sessionId);
            reads.increment();
            if (result.isEmpty()) misses.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public CpfSessionSnapshot renew(String sessionId, Duration ttl) {
        try {
            CpfSessionSnapshot result = delegate.renew(sessionId, ttl);
            updates.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public CpfSessionSnapshot rotate(String sessionId, Duration ttl) {
        try {
            CpfSessionSnapshot result = delegate.rotate(sessionId, ttl);
            updates.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public boolean revoke(String sessionId, String reason) {
        try {
            boolean result = delegate.revoke(sessionId, reason);
            if (result) revocations.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public int revokePrincipal(String tenantId, String principalId, String reason) {
        try {
            int result = delegate.revokePrincipal(tenantId, principalId, reason);
            if (result > 0) {
                revocations.add(result);
                forcedLogouts.add(result);
            }
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public List<CpfSessionSnapshot> findByPrincipal(String tenantId, String principalId) {
        try {
            List<CpfSessionSnapshot> result = delegate.findByPrincipal(tenantId, principalId);
            reads.increment();
            return result;
        } catch (RuntimeException failure) {
            providerFailures.increment();
            throw failure;
        }
    }

    @Override
    public CpfSessionMetricsSnapshot snapshot() {
        return new CpfSessionMetricsSnapshot(
                creates.sum(), reads.sum(), updates.sum(), revocations.sum(), misses.sum(),
                providerFailures.sum(), forcedLogouts.sum());
    }
}
