package com.cpf.starter.integration.resilience.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.api.resilience.CpfResilienceRuntimePolicy;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfResilienceEngineSafetyTest {
    @Test
    void zeroJitterBoundaryAndRetryBudgetAreDeterministic() {
        CpfResiliencePolicy base = new CpfResiliencePolicy("test", 1, Duration.ofSeconds(1), 2,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, 100, Duration.ofMinutes(1), true, true);
        CpfResilienceRuntimePolicy runtime = new CpfResilienceRuntimePolicy(base,
                Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100),
                Duration.ofMillis(100), Duration.ofSeconds(1), Duration.ofMillis(1),
                Duration.ofMillis(10), 0.0d, 1, Duration.ofMinutes(1), 1, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();
        try (CpfResilienceEngine engine = new CpfResilienceEngine(
                new OnePolicy(base), failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE,
                (a, b, c, d, e, f) -> {}, (policy, context) -> runtime, null,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), Executors.newVirtualThreadPerTaskExecutor(),
                () -> 0.5d, System::nanoTime)) {
            CpfResilienceOutcome<String> outcome = engine.execute(
                    new CpfResilienceCallContext("test", "tx", "idem", Instant.EPOCH, Map.of()), () -> {
                        if (calls.incrementAndGet() == 1) throw new IllegalStateException("retry");
                        return "OK";
                    });
            assertEquals(CpfResilienceOutcome.Status.SUCCESS, outcome.status());
            assertEquals(2, outcome.attempts());
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
