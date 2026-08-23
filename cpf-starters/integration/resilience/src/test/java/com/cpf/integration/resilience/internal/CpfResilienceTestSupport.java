package com.cpf.integration.resilience.internal;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyResolver;
import com.cpf.integration.resilience.spi.CpfResilienceRuntimePolicyResolver;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/** Test-only current-contract factory for the internal resilience engine. */
final class CpfResilienceTestSupport {
    private static final int DEFAULT_MAXIMUM_GUARD_ENTRIES = 10_000;
    private static final Duration DEFAULT_GUARD_IDLE_TTL = Duration.ofMinutes(30);

    private CpfResilienceTestSupport() { }

    static AutoCloseable bindContext(String transactionId, Clock clock) {
        Instant now = clock.instant();
        CpfContext context = contextFactory(clock).fromTrustedPropagation(
                transactionId,
                transactionId,
                transactionId,
                LocalDate.ofInstant(now, ZoneOffset.UTC),
                now,
                CpfContext.CpfTransactionOriginKind.INTERNAL,
                null,
                null,
                "cpf.resilience.test",
                null,
                null,
                null,
                CpfContext.CpfExecutionType.INTEGRATION,
                1,
                0,
                null,
                null,
                null,
                null);
        return CpfContexts.bind(CpfContextSnapshot.capture(context, now));
    }

    static CpfResilienceEngine engine(
            CpfResiliencePolicyResolver policies, CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit, Clock clock, ExecutorService executor) {
        return engine(policies, classifier, audit, CpfResilienceRuntimePolicyResolver.legacyCompatible(),
                null, CpfTelemetry.noop(), clock, executor, Math::random, System::nanoTime);
    }

    static CpfResilienceEngine engine(
            CpfResiliencePolicyResolver policies, CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit, CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager, Clock clock, ExecutorService executor,
            DoubleSupplier random, LongSupplier nanoTime) {
        return engine(policies, classifier, audit, runtimePolicies, lockManager, CpfTelemetry.noop(),
                clock, executor, random, nanoTime);
    }

    static CpfResilienceEngine engine(
            CpfResiliencePolicyResolver policies, CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit, CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager, Clock clock, ExecutorService executor,
            DoubleSupplier random, LongSupplier nanoTime, int maximumGuardEntries, Duration guardIdleTtl) {
        return engine(policies, classifier, audit, runtimePolicies, lockManager, CpfTelemetry.noop(),
                clock, executor, random, nanoTime, maximumGuardEntries, guardIdleTtl);
    }

    static CpfResilienceEngine engine(
            CpfResiliencePolicyResolver policies, CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit, CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager, CpfTelemetry telemetry, Clock clock, ExecutorService executor,
            DoubleSupplier random, LongSupplier nanoTime) {
        return engine(policies, classifier, audit, runtimePolicies, lockManager, telemetry, clock, executor,
                random, nanoTime, DEFAULT_MAXIMUM_GUARD_ENTRIES, DEFAULT_GUARD_IDLE_TTL);
    }

    static CpfResilienceEngine engine(
            CpfResiliencePolicyResolver policies, CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit, CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager, CpfTelemetry telemetry, Clock clock, ExecutorService executor,
            DoubleSupplier random, LongSupplier nanoTime, int maximumGuardEntries, Duration guardIdleTtl) {
        return new CpfResilienceEngine(policies, classifier, audit, runtimePolicies, lockManager, telemetry,
                contextFactory(clock), clock, executor, random, nanoTime, maximumGuardEntries, guardIdleTtl);
    }

    private static CpfContextExecutionFactory contextFactory(Clock clock) {
        AtomicLong sequence = new AtomicLong();
        CpfExecutionIdGenerator ids = new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "EX-TEST-" + sequence.incrementAndGet(); }
            @Override public String newSegmentId() { return "SG-TEST-" + sequence.incrementAndGet(); }
        };
        return new CpfContextExecutionFactory(ids, clock);
    }
}
