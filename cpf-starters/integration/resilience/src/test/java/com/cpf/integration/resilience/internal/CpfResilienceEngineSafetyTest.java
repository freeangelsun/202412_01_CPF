package com.cpf.integration.resilience.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimePolicy;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfResilienceEngineSafetyTest {
    private static final String CANONICAL_TRANSACTION_ID = "20260822000000000CPFabc12340000001";

    @Test
    void zeroJitterBoundaryAndRetryBudgetAreDeterministic() throws Exception {
        CpfResiliencePolicy base = new CpfResiliencePolicy("test", 1, Duration.ofSeconds(1), 2,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, 100, Duration.ofMinutes(1), true, true);
        CpfResilienceRuntimePolicy runtime = new CpfResilienceRuntimePolicy(base,
                Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100),
                Duration.ofMillis(100), Duration.ofSeconds(1), Duration.ofMillis(1),
                Duration.ofMillis(10), 0.0d, 1, Duration.ofMinutes(1), 1, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();
        try (AutoCloseable ignored = CpfResilienceTestSupport.bindContext("tx", Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
             CpfResilienceEngine engine = CpfResilienceTestSupport.engine(
                new OnePolicy(base), failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE,
                (a, b, c, d, e, f) -> {}, (policy, context) -> runtime, null,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), Executors.newVirtualThreadPerTaskExecutor(),
                () -> 0.5d, System::nanoTime)) {
            CpfResilienceOutcome<String> outcome = engine.execute(
                    CpfResilienceCallContext.recoveredLineage("test", "tx", "idem", Map.of(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)), () -> {
                        if (calls.incrementAndGet() == 1) throw new IllegalStateException("retry");
                        return "OK";
                    });
            assertEquals(CpfResilienceOutcome.Status.SUCCESS, outcome.status());
            assertEquals(2, outcome.attempts());
        }
    }

    @Test
    void normalExecutionWithoutBoundContextRemainsFailClosed() {
        CpfResiliencePolicy base = new CpfResiliencePolicy("test", 1, Duration.ofSeconds(1), 1,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, 100, Duration.ofMinutes(1), true, true);
        try (CpfResilienceEngine engine = CpfResilienceTestSupport.engine(
                new OnePolicy(base), failure -> CpfResilienceFailureClassifier.Classification.NON_RETRYABLE,
                (a, b, c, d, e, f) -> {}, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Executors.newVirtualThreadPerTaskExecutor())) {
            assertThrows(IllegalStateException.class, () -> engine.execute(
                    CpfResilienceCallContext.recoveredLineage(
                            "test", CANONICAL_TRANSACTION_ID, "idem", Map.of(),
                            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
                    () -> "MUST_NOT_RUN"));
        }
    }

    @Test
    void reconciliationRestoresPersistedCanonicalLineageWithoutIngressContext() {
        CpfResiliencePolicy base = new CpfResiliencePolicy("test", 1, Duration.ofSeconds(1), 1,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, 100, Duration.ofMinutes(1), true, true);
        try (CpfResilienceEngine engine = CpfResilienceTestSupport.engine(
                new OnePolicy(base), failure -> CpfResilienceFailureClassifier.Classification.NON_RETRYABLE,
                (a, b, c, d, e, f) -> {}, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                Executors.newVirtualThreadPerTaskExecutor())) {
            CpfResilienceOutcome<String> outcome = engine.reconcile(
                    CpfResilienceCallContext.recoveredLineage(
                            "test", CANONICAL_TRANSACTION_ID, "idem", Map.of(),
                            Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
                    () -> CpfContexts.transactionId());
            assertEquals(CpfResilienceOutcome.Status.SUCCESS, outcome.status());
            assertEquals(CANONICAL_TRANSACTION_ID, outcome.value());
        }
    }

    private record OnePolicy(CpfResiliencePolicy policy) implements CpfResiliencePolicyStore {
        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) { return Optional.of(policy); }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) { return List.of(policy); }
        @Override public String request(CpfResiliencePolicy p, String requester, String reason) { return "r"; }
        @Override public CpfResiliencePolicy approve(String request, String approver, String reason) { return policy; }
        @Override public void reject(String request, String approver, String reason) {}
    }
}
