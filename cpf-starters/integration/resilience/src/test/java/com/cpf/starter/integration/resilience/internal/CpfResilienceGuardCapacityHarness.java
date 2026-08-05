package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResilienceRuntimePolicy;
import com.cpf.core.api.resilience.CpfResilienceRuntimeStatus;
import com.cpf.core.spi.resilience.CpfResilienceFailureClassifier;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/** Verifies fail-closed cardinality control and deterministic idle eviction. */
public final class CpfResilienceGuardCapacityHarness {
    private CpfResilienceGuardCapacityHarness() {}

    public static void main(String[] args) {
        AtomicLong nanos = new AtomicLong();
        PolicyStore policies = new PolicyStore();
        for (String operation : List.of("guard.one", "guard.two", "guard.three")) {
            policies.add(policy(operation));
        }
        CpfResilienceEngine engine = new CpfResilienceEngine(
                policies,
                failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE,
                (a, b, c, d, e, f) -> {},
                (policy, context) -> CpfResilienceRuntimePolicy.legacyCompatible(policy),
                null,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Executors.newVirtualThreadPerTaskExecutor(),
                () -> 0.5d,
                nanos::get,
                2,
                Duration.ofNanos(10));
        try {
            check(success(engine, "guard.one"), "first guard must execute");
            check(success(engine, "guard.two"), "second guard must execute");
            CpfResilienceOutcome<String> rejected = execute(engine, "guard.three");
            check(rejected.status() == CpfResilienceOutcome.Status.REJECTED
                            && "GUARD_CAPACITY_EXHAUSTED".equals(rejected.reasonCode()),
                    "new cardinality must fail closed at capacity");
            CpfResilienceRuntimeStatus.RuntimeSnapshot full = engine.resilienceRuntimeSnapshot();
            check(full.health() == CpfResilienceRuntimeStatus.Health.DEGRADED
                            && full.guardCount() == 2
                            && full.guardCapacityRejectionCount() == 1L,
                    "capacity rejection must be observable");

            nanos.addAndGet(11L);
            check(success(engine, "guard.three"), "expired idle guards must be evicted");
            CpfResilienceRuntimeStatus.RuntimeSnapshot recovered = engine.resilienceRuntimeSnapshot();
            check(recovered.guardCount() == 1 && recovered.guardEvictionCount() == 2L,
                    "idle eviction must be bounded and counted");
        } finally {
            engine.close();
        }
        CpfResilienceRuntimeStatus.RuntimeSnapshot closed = engine.resilienceRuntimeSnapshot();
        check(closed.closed() && closed.health() == CpfResilienceRuntimeStatus.Health.DOWN
                        && closed.guardCount() == 0,
                "close must release all guard state");
        System.out.println("CPF_RESILIENCE_GUARD_CAPACITY_HARNESS_PASS");
    }

    private static boolean success(CpfResilienceEngine engine, String operation) {
        return execute(engine, operation).status() == CpfResilienceOutcome.Status.SUCCESS;
    }

    private static CpfResilienceOutcome<String> execute(CpfResilienceEngine engine, String operation) {
        return engine.execute(new CpfResilienceCallContext(
                operation, "tx-" + operation, null, Instant.EPOCH, Map.of()), () -> "OK");
    }

    private static CpfResiliencePolicy policy(String operation) {
        return new CpfResiliencePolicy(
                operation, 1L, Duration.ofSeconds(1), 1, Duration.ZERO,
                3, Duration.ofSeconds(5), 1, 100, Duration.ofMinutes(1), true, true);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class PolicyStore implements CpfResiliencePolicyStore {
        private final java.util.Map<String, CpfResiliencePolicy> policies = new java.util.HashMap<>();
        void add(CpfResiliencePolicy policy) { policies.put(policy.operationId(), policy); }
        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) {
            return Optional.ofNullable(policies.get(operationId));
        }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) {
            return policies.values().stream().skip(offset).limit(limit).toList();
        }
        @Override public String request(CpfResiliencePolicy policy, String requester, String reason) {
            throw new UnsupportedOperationException();
        }
        @Override public CpfResiliencePolicy approve(String requestId, String approver, String reason) {
            throw new UnsupportedOperationException();
        }
        @Override public void reject(String requestId, String approver, String reason) {
            throw new UnsupportedOperationException();
        }
    }
}
