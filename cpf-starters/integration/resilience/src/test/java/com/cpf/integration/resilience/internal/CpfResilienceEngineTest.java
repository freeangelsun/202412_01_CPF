package com.cpf.integration.resilience.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CpfResilienceEngineTest {
    private static final Instant NOW = Instant.parse("2026-08-03T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private AutoCloseable contextScope;

    @BeforeEach
    void bindContext() {
        contextScope = CpfResilienceTestSupport.bindContext("tx-1", CLOCK);
    }

    @AfterEach
    void closeContext() throws Exception {
        contextScope.close();
    }

    @Test
    void retriesIdempotentCallAndSucceeds() {
        CpfResiliencePolicy policy = policy("http.customer", 2, 10, true);
        AtomicInteger calls = new AtomicInteger();
        RecordingAudit audit = new RecordingAudit();

        try (CpfResilienceEngine engine = engine(new StubStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE, audit)) {
            CpfResilienceOutcome<String> result = engine.execute(context("http.customer", "idem-1"), () -> {
                if (calls.incrementAndGet() == 1) throw new IllegalStateException("retry");
                return "OK";
            });

            assertThat(result.status()).isEqualTo(CpfResilienceOutcome.Status.SUCCESS);
            assertThat(result.attempts()).isEqualTo(2);
            assertThat(audit.events).hasSize(1);
        }
    }

    @Test
    void rejectsRetryWithoutIdempotencyKeyAndAuditsTheDecision() {
        CpfResiliencePolicy policy = policy("tcp.send", 2, 10, true);
        RecordingAudit audit = new RecordingAudit();

        try (CpfResilienceEngine engine = engine(new StubStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE, audit)) {
            CpfResilienceOutcome<String> result = engine.execute(context("tcp.send", null), () -> "NO");

            assertThat(result.status()).isEqualTo(CpfResilienceOutcome.Status.REJECTED);
            assertThat(result.reasonCode()).isEqualTo("IDEMPOTENCY_KEY_REQUIRED");
            assertThat(audit.events).singleElement().asString().contains("IDEMPOTENCY_KEY_REQUIRED");
        }
    }

    @Test
    void rejectsMissingPolicyInsteadOfExecutingUnprotectedCall() {
        RecordingAudit audit = new RecordingAudit();
        try (CpfResilienceEngine engine = engine(new StubStore(null),
                failure -> CpfResilienceFailureClassifier.Classification.NON_RETRYABLE, audit)) {
            AtomicInteger calls = new AtomicInteger();
            CpfResilienceOutcome<String> result = engine.execute(context("missing.policy", "idem-1"), () -> {
                calls.incrementAndGet();
                return "unsafe";
            });

            assertThat(result.status()).isEqualTo(CpfResilienceOutcome.Status.REJECTED);
            assertThat(result.reasonCode()).isEqualTo("ACTIVE_POLICY_REQUIRED");
            assertThat(calls).hasValue(0);
            assertThat(audit.events).hasSize(1);
        }
    }

    @Test
    void preservesUnknownResultForAmbiguousFailure() {
        CpfResiliencePolicy policy = policy("payment.submit", 1, 10, true);
        try (CpfResilienceEngine engine = engine(new StubStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT,
                new RecordingAudit())) {
            CpfResilienceOutcome<String> result = engine.execute(context("payment.submit", "idem-1"),
                    () -> { throw new IllegalStateException("ack lost"); });

            assertThat(result.status()).isEqualTo(CpfResilienceOutcome.Status.UNKNOWN_RESULT);
            assertThat(result.reasonCode()).isEqualTo("AMBIGUOUS_FAILURE");
        }
    }

    @Test
    void rateLimitIsSharedAcrossCallsForTheSamePolicyRevision() {
        CpfResiliencePolicy policy = policy("rate.limited", 1, 1, true);
        try (CpfResilienceEngine engine = engine(new StubStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.NON_RETRYABLE,
                new RecordingAudit())) {
            CpfResilienceOutcome<String> first = engine.execute(context("rate.limited", "idem-1"), () -> "OK");
            CpfResilienceOutcome<String> second = engine.execute(context("rate.limited", "idem-2"), () -> "NO");

            assertThat(first.status()).isEqualTo(CpfResilienceOutcome.Status.SUCCESS);
            assertThat(second.status()).isEqualTo(CpfResilienceOutcome.Status.REJECTED);
            assertThat(second.reasonCode()).isEqualTo("RATE_LIMIT");
        }
    }

    @Test
    void auditFailureDoesNotReturnAFalseSuccess() {
        CpfResiliencePolicy policy = policy("audit.required", 1, 10, true);
        CpfResilienceAuditSink failingAudit = (eventType, operationId, operatorId, reason, details, occurredAt) -> {
            throw new IllegalStateException("audit unavailable");
        };

        try (CpfResilienceEngine engine = engine(new StubStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.NON_RETRYABLE, failingAudit)) {
            CpfResilienceOutcome<String> result =
                    engine.execute(context("audit.required", "idem-1"), () -> "OK");
            assertThat(result.status()).isEqualTo(CpfResilienceOutcome.Status.UNKNOWN_RESULT);
            assertThat(result.value()).isNull();
            assertThat(result.reasonCode()).isEqualTo("AUDIT_PERSISTENCE_FAILED_AFTER_SUCCESS");
            assertThat(result.attempts()).isEqualTo(1);
            assertThat(result.policyRevision()).isEqualTo(1L);
        }
    }

    private static CpfResilienceEngine engine(CpfResiliencePolicyStore store,
                                               CpfResilienceFailureClassifier classifier,
                                               CpfResilienceAuditSink audit) {
        return CpfResilienceTestSupport.engine(store, classifier, audit, CLOCK,
                Executors.newVirtualThreadPerTaskExecutor());
    }

    private static CpfResilienceCallContext context(String operationId, String idempotencyKey) {
        return CpfResilienceCallContext.recoveredLineage(operationId, "tx-1", idempotencyKey, Map.of(), CLOCK);
    }

    private static CpfResiliencePolicy policy(String operationId, int maxAttempts,
                                               int rateLimitPermits, boolean reconcileEnabled) {
        return new CpfResiliencePolicy(operationId, 1, Duration.ofSeconds(1), maxAttempts,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, rateLimitPermits,
                Duration.ofMinutes(1), true, reconcileEnabled);
    }

    private record StubStore(CpfResiliencePolicy policy) implements CpfResiliencePolicyStore {
        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) {
            return Optional.ofNullable(policy);
        }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) {
            return policy == null ? List.of() : List.of(policy);
        }
        @Override public String request(CpfResiliencePolicy policy, String requesterId, String reason) {
            return "request-1";
        }
        @Override public CpfResiliencePolicy approve(String requestId, String approverId, String reason) {
            return policy;
        }
        @Override public void reject(String requestId, String approverId, String reason) {
        }
    }

    private static final class RecordingAudit implements CpfResilienceAuditSink {
        private final List<String> events = new ArrayList<>();
        @Override public void record(String eventType, String operationId, String operatorId,
                                     String reason, Map<String, String> details, Instant occurredAt) {
            events.add(eventType + ":" + operationId + ":" + reason);
        }
    }
}
