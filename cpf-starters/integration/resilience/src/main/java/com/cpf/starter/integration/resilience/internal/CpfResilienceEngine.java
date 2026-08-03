package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceExecutor;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.spi.resilience.CpfResilienceAuditSink;
import com.cpf.core.spi.resilience.CpfResilienceFailureClassifier;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Shared, revision-aware guards for timeout/retry/circuit/bulkhead/rate-limit/unknown-result. */
public final class CpfResilienceEngine implements CpfResilienceExecutor, AutoCloseable {
    private final CpfResiliencePolicyStore policies;
    private final CpfResilienceFailureClassifier classifier;
    private final CpfResilienceAuditSink audit;
    private final Clock clock;
    private final ExecutorService executor;
    private final ConcurrentHashMap<GuardKey, Guard> guards = new ConcurrentHashMap<>();

    public CpfResilienceEngine(CpfResiliencePolicyStore policies,
                               CpfResilienceFailureClassifier classifier,
                               CpfResilienceAuditSink audit,
                               Clock clock,
                               ExecutorService executor) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.classifier = Objects.requireNonNull(classifier, "classifier");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext context, Supplier<T> action) {
        return executeInternal(context, action, false);
    }

    @Override
    public <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext context, Supplier<T> probe) {
        return executeInternal(context, probe, true);
    }

    private <T> CpfResilienceOutcome<T> executeInternal(CpfResilienceCallContext context, Supplier<T> action, boolean reconcile) {
        Objects.requireNonNull(context, "context"); Objects.requireNonNull(action, "action");
        var activePolicy = policies.findActive(context.operationId());
        if (activePolicy.isEmpty()) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "ACTIVE_POLICY_REQUIRED", 0, 0));
        }
        CpfResiliencePolicy policy = activePolicy.get();
        if (reconcile && !policy.unknownResultReconcileEnabled()) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "RECONCILE_DISABLED", 0, policy.revision()));
        }
        if (policy.maxAttempts() > 1 && context.idempotencyKey() == null) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "IDEMPOTENCY_KEY_REQUIRED", 0, policy.revision()));
        }
        GuardKey key = new GuardKey(policy.operationId(), policy.revision());
        guards.keySet().removeIf(k -> k.operationId.equals(key.operationId) && k.revision != key.revision);
        Guard guard = guards.computeIfAbsent(key, ignored -> new Guard(policy, clock.instant()));
        Instant now = clock.instant();
        if (!guard.rate.tryAcquire(now, policy.rateLimitPermits(), policy.rateLimitWindow())) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null, "RATE_LIMIT", 0, policy.revision()));
        }
        if (!guard.bulkhead.tryAcquire()) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null, "BULKHEAD_FULL", 0, policy.revision()));
        }
        try {
            if (guard.isCircuitOpen(now)) {
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null, "CIRCUIT_OPEN", 0, policy.revision()));
            }
            Throwable last = null;
            for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
                try {
                    T value = invokeWithTimeout(action, policy.timeoutBudget());
                    guard.onSuccess();
                    return audited(context, outcome(CpfResilienceOutcome.Status.SUCCESS, value, null, attempt, policy.revision()));
                } catch (TimeoutException e) {
                    last = e;
                    var classification = classifier.classify(e);
                    if (classification == CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT) {
                        guard.onFailure(policy, clock.instant());
                        return audited(context, outcome(CpfResilienceOutcome.Status.UNKNOWN_RESULT, null, "TIMEOUT_UNKNOWN_RESULT", attempt, policy.revision()));
                    }
                    if (attempt == policy.maxAttempts() || classification != CpfResilienceFailureClassifier.Classification.RETRYABLE) {
                        guard.onFailure(policy, clock.instant());
                        return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null, "TIMEOUT", attempt, policy.revision()));
                    }
                } catch (Throwable e) {
                    if (e instanceof Error error) throw error;
                    last = e;
                    var classification = classifier.classify(e);
                    if (classification == CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT) {
                        guard.onFailure(policy, clock.instant());
                        return audited(context, outcome(CpfResilienceOutcome.Status.UNKNOWN_RESULT, null, "AMBIGUOUS_FAILURE", attempt, policy.revision()));
                    }
                    if (attempt == policy.maxAttempts() || classification != CpfResilienceFailureClassifier.Classification.RETRYABLE) {
                        guard.onFailure(policy, clock.instant());
                        return audited(context, outcome(CpfResilienceOutcome.Status.FAILED, null, e.getClass().getSimpleName(), attempt, policy.revision()));
                    }
                }
                sleep(policy.retryBackoff());
            }
            throw new IllegalStateException("unreachable", last);
        } finally {
            guard.bulkhead.release();
        }
    }

    private <T> T invokeWithTimeout(Supplier<T> action, Duration timeout) throws Throwable {
        Future<T> future = executor.submit(action::get);
        try { return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS); }
        catch (TimeoutException e) { future.cancel(true); throw e; }
        catch (ExecutionException e) { throw e.getCause(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
    }

    private void sleep(Duration duration) {
        if (duration.isZero()) return;
        try { TimeUnit.NANOSECONDS.sleep(duration.toNanos()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CpfResilienceExecutionException("retry interrupted", e); }
    }

    private <T> CpfResilienceOutcome<T> audited(CpfResilienceCallContext context, CpfResilienceOutcome<T> result) {
        try {
            audit.record("RESILIENCE_EXECUTION", context.operationId(), null, result.reasonCode(),
                    Map.of("status", result.status().name(), "attempts", Integer.toString(result.attempts()),
                           "policyRevision", Long.toString(result.policyRevision()), "transactionId", context.transactionId()),
                    clock.instant());
        } catch (RuntimeException e) {
            throw new CpfResilienceExecutionException("resilience audit persistence failed after outcome " + result.status(), e);
        }
        return result;
    }

    private <T> CpfResilienceOutcome<T> outcome(CpfResilienceOutcome.Status status, T value, String reason, int attempts, long revision) {
        return new CpfResilienceOutcome<>(status, value, reason, attempts, revision, clock.instant());
    }

    @Override public void close() { executor.shutdown(); }

    private record GuardKey(String operationId, long revision) {}
    private static final class Guard {
        private final Semaphore bulkhead;
        private final SlidingWindowRate rate = new SlidingWindowRate();
        private final AtomicInteger failures = new AtomicInteger();
        private final AtomicReference<Instant> openUntil = new AtomicReference<>(Instant.EPOCH);
        Guard(CpfResiliencePolicy policy, Instant ignored) { this.bulkhead = new Semaphore(policy.bulkheadMaxConcurrent()); }
        boolean isCircuitOpen(Instant now) { return openUntil.get().isAfter(now); }
        void onSuccess() { failures.set(0); openUntil.set(Instant.EPOCH); }
        void onFailure(CpfResiliencePolicy policy, Instant now) {
            if (failures.incrementAndGet() >= policy.circuitFailureThreshold()) {
                openUntil.set(now.plus(policy.circuitOpenDuration())); failures.set(0);
            }
        }
    }
    private static final class SlidingWindowRate {
        private Instant windowStart = Instant.EPOCH; private int permits;
        synchronized boolean tryAcquire(Instant now, int max, Duration window) {
            if (!now.isBefore(windowStart.plus(window))) { windowStart=now; permits=0; }
            if (permits >= max) return false; permits++; return true;
        }
    }
}
