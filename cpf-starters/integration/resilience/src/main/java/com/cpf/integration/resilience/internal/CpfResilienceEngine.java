package com.cpf.integration.resilience.internal;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;


import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.platform.operations.observability.api.CpfTraceContext;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceDeadline;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimeStatus;
import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyResolver;
import com.cpf.integration.resilience.spi.CpfResilienceRuntimePolicyResolver;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** Revision-aware timeout/retry/circuit/bulkhead/rate-limit/UNKNOWN execution engine. */
public final class CpfResilienceEngine implements CpfResilienceExecutor, CpfResilienceRuntimeStatus, AutoCloseable {
    private static final Duration MAX_REQUEST_CLOCK_SKEW = Duration.ofSeconds(5);
    private static final Duration MIN_DISTRIBUTED_PROBE_LEASE = Duration.ofMillis(100);
    private static final int DEFAULT_MAXIMUM_GUARD_ENTRIES = 10_000;
    private static final Duration DEFAULT_GUARD_IDLE_TTL = Duration.ofMinutes(30);

    private final CpfResiliencePolicyResolver policies;
    private final CpfResilienceFailureClassifier classifier;
    private final CpfResilienceAuditSink audit;
    private final CpfResilienceRuntimePolicyResolver runtimePolicies;
    private final CpfLockManager lockManager;
    private final CpfTelemetry telemetry;
    private final Clock clock;
    private final ExecutorService executor;
    private final DoubleSupplier random;
    private final LongSupplier nanoTime;
    private final Sleeper sleeper;
    private final int maximumGuardEntries;
    private final Duration guardIdleTtl;
    private final long guardIdleTtlNanos;
    private final Object guardAllocationMonitor = new Object();
    private final ConcurrentHashMap<GuardKey, Guard> guards = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Future<?>, Boolean> activeAttempts = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong guardEvictionCount = new AtomicLong();
    private final AtomicLong guardCapacityRejectionCount = new AtomicLong();
    private final CpfContextExecutionFactory contextFactory;

    public CpfResilienceEngine(
            CpfResiliencePolicyResolver policies,
            CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit,
            CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager,
            CpfTelemetry telemetry,
            CpfContextExecutionFactory contextFactory,
            Clock clock,
            ExecutorService executor,
            DoubleSupplier random,
            LongSupplier nanoTime,
            int maximumGuardEntries,
            Duration guardIdleTtl) {
        this(policies, classifier, audit, runtimePolicies, lockManager, telemetry, contextFactory,
                clock, executor, random, nanoTime, maximumGuardEntries, guardIdleTtl,
                CpfResilienceEngine::sleepThread);
    }

    CpfResilienceEngine(
            CpfResiliencePolicyResolver policies,
            CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit,
            CpfResilienceRuntimePolicyResolver runtimePolicies,
            CpfLockManager lockManager,
            CpfTelemetry telemetry,
            CpfContextExecutionFactory contextFactory,
            Clock clock,
            ExecutorService executor,
            DoubleSupplier random,
            LongSupplier nanoTime,
            int maximumGuardEntries,
            Duration guardIdleTtl,
            Sleeper sleeper) {
        this.policies = Objects.requireNonNull(policies, "policies");
        this.classifier = Objects.requireNonNull(classifier, "classifier");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.runtimePolicies = Objects.requireNonNull(runtimePolicies, "runtimePolicies");
        this.lockManager = lockManager;
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.random = Objects.requireNonNull(random, "random");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
        if (maximumGuardEntries < 1 || maximumGuardEntries > 1_000_000) {
            throw new IllegalArgumentException("maximumGuardEntries must be between 1 and 1000000");
        }
        this.maximumGuardEntries = maximumGuardEntries;
        this.guardIdleTtl = positiveBounded(Objects.requireNonNull(guardIdleTtl, "guardIdleTtl"),
                Duration.ofDays(365), "guardIdleTtl");
        this.guardIdleTtlNanos = safeToNanos(this.guardIdleTtl);
    }

    @Override
    public <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext context, Supplier<T> action) {
        return executeInternal(context, action, false);
    }

    @Override
    public <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext context, Supplier<T> probe) {
        return executeInternal(context, probe, true);
    }

    private <T> CpfResilienceOutcome<T> executeInternal(
            CpfResilienceCallContext context, Supplier<T> action, boolean reconcile) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(action, "action");
        CpfTraceContext traceContext = resilienceTraceContext(context, reconcile);
        CpfTelemetry.CpfTelemetrySpan span = startSpanSafely(traceContext);
        try {
            CpfResilienceOutcome<T> result = executeUntraced(context, action, reconcile, traceContext);
            if (result.status() != CpfResilienceOutcome.Status.SUCCESS) {
                markSpanErrorSafely(span, new TraceOutcomeException(result.status().name()));
            }
            return result;
        } catch (RuntimeException | Error failure) {
            markSpanErrorSafely(span, failure);
            throw failure;
        } finally {
            closeSpanSafely(span);
        }
    }

    private <T> CpfResilienceOutcome<T> executeUntraced(
            CpfResilienceCallContext context, Supplier<T> action, boolean reconcile,
            CpfTraceContext traceContext) {
        if (closed.get()) {
            return outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "ENGINE_CLOSED", 0, 0);
        }
        var active = policies.findActive(context.operationId());
        if (active.isEmpty()) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "ACTIVE_POLICY_REQUIRED", 0, 0));
        }

        CpfResiliencePolicy base = active.get();
        CpfResilienceRuntimePolicy runtime = Objects.requireNonNull(
                runtimePolicies.resolve(base, context), "runtime policy");
        if (!runtime.basePolicy().equals(base)) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "RUNTIME_POLICY_BASE_MISMATCH", 0, base.revision()));
        }
        if (reconcile && !base.unknownResultReconcileEnabled()) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "RECONCILE_DISABLED", 0, base.revision()));
        }
        if (base.maxAttempts() > 1 && context.idempotencyKey() == null) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "IDEMPOTENCY_KEY_REQUIRED", 0, base.revision()));
        }

        Instant now = clock.instant();
        if (context.requestedAt().isAfter(safePlus(now, MAX_REQUEST_CLOCK_SKEW))) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "REQUESTED_AT_IN_FUTURE", 0, base.revision()));
        }
        Duration elapsed = elapsed(context.requestedAt(), now);
        if (elapsed.compareTo(runtime.overallTimeout()) >= 0) {
            return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null,
                    "DEADLINE_EXCEEDED_BEFORE_EXECUTION", 0, base.revision()));
        }
        CpfResilienceDeadline deadline = new CpfResilienceDeadline(
                runtime, nanoTime, runtime.overallTimeout().minus(elapsed));

        GuardKey key = new GuardKey(base.operationId(), base.revision(), runtime);
        Guard guard = acquireGuard(key, runtime);
        if (guard == null) {
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "GUARD_CAPACITY_EXHAUSTED", 0, base.revision()));
        }
        try {
            if (!guard.rate.tryAcquire(now, base.rateLimitPermits(), base.rateLimitWindow())) {
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "RATE_LIMIT", 0, base.revision()));
            }

            Duration queueWait = deadline.cap(runtime.bulkheadQueueWait());
        boolean bulkheadAcquired;
        try {
            bulkheadAcquired = guard.bulkhead.acquire(runtime.bulkheadQueueLimit(), queueWait);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "CANCELLED", 0, base.revision()));
        }
        if (!bulkheadAcquired) {
            String reason = deadline.expired()
                    ? "DEADLINE_EXCEEDED_IN_BULKHEAD_QUEUE"
                    : runtime.bulkheadQueueLimit() == 0 ? "BULKHEAD_FULL" : "BULKHEAD_QUEUE_FULL";
            return audited(context, outcome(deadline.expired()
                            ? CpfResilienceOutcome.Status.TIMEOUT : CpfResilienceOutcome.Status.REJECTED,
                    null, reason, 0, base.revision()));
        }

        CpfLockManager.LockToken distributedProbe = null;
        CircuitPermit permit = guard.circuit.tryAcquire(now);
        if (permit == CircuitPermit.DENIED) {
            guard.bulkhead.release();
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "CIRCUIT_OPEN", 0, base.revision()));
        }
        if (permit == CircuitPermit.HALF_OPEN && lockManager == null) {
            guard.circuit.releaseUnused(permit);
            guard.bulkhead.release();
            return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                    "DISTRIBUTED_LOCK_REQUIRED_FOR_HALF_OPEN", 0, base.revision()));
        }
        if (permit == CircuitPermit.HALF_OPEN) {
            Duration probeLease = distributedProbeLease(base, runtime, deadline);
            if (probeLease.compareTo(MIN_DISTRIBUTED_PROBE_LEASE) < 0) {
                guard.circuit.releaseUnused(permit);
                guard.bulkhead.release();
                return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null,
                        "DEADLINE_EXCEEDED_BEFORE_HALF_OPEN_PROBE", 0, base.revision()));
            }
            CpfLockManager.AcquireResult acquired = lockManager.acquire(
                    "cpf:resilience:half-open:" + base.operationId() + ":" + base.revision(),
                    "resilience-engine",
                    context.transactionId() + ":"
                            + (context.idempotencyKey() == null ? "probe" : context.idempotencyKey()),
                    probeLease);
            if (acquired.status() != CpfLockManager.AcquireStatus.ACQUIRED) {
                guard.circuit.releaseUnused(permit);
                guard.bulkhead.release();
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "HALF_OPEN_PROBE_IN_FLIGHT", 0, base.revision()));
            }
            distributedProbe = acquired.token();
        }

        try {
            return executeAttempts(context, action, base, runtime, guard, permit, deadline, traceContext);
            } finally {
                if (distributedProbe != null) {
                    lockManager.release(distributedProbe, "HALF_OPEN_PROBE_COMPLETED");
                }
                guard.bulkhead.release();
            }
        } finally {
            guard.releaseUse(nanoTime.getAsLong());
        }
    }

    private <T> CpfResilienceOutcome<T> executeAttempts(
            CpfResilienceCallContext context,
            Supplier<T> action,
            CpfResiliencePolicy base,
            CpfResilienceRuntimePolicy runtime,
            Guard guard,
            CircuitPermit permit,
            CpfResilienceDeadline deadline,
            CpfTraceContext traceContext) {
        CpfContextSnapshot caller = CpfContexts.requireSnapshot();
        if (!caller.context().transactionId().equals(context.transactionId())) {
            throw new SecurityException("RESILIENCE_TRANSACTION_CONTEXT_MISMATCH");
        }
        Throwable last = null;
        for (int attempt = 1; attempt <= base.maxAttempts(); attempt++) {
            if (closed.get()) {
                guard.circuit.releaseUnused(permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "ENGINE_CLOSED_DURING_EXECUTION", attempt - 1, base.revision()));
            }
            if (deadline.expired()) {
                guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                        base.circuitOpenDuration(), permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null,
                        "OVERALL_TIMEOUT", attempt - 1, base.revision()));
            }
            if (attempt > 1 && !guard.retryBudget.tryAcquire(
                    clock.instant(), runtime.retryBudgetCapacity(), runtime.retryBudgetWindow())) {
                guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                        base.circuitOpenDuration(), permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "RETRY_BUDGET_EXHAUSTED", attempt - 1, base.revision()));
            }
            CpfTelemetry.CpfTelemetrySpan attemptSpan = startSpanSafely(traceContext.child(
                    traceContext.kind(), context.operationId() + ".attempt", traceSegment(context),
                    attempt, traceBaggage(context)));
            try {
                Duration attemptTimeout = deadline.remainingFor(CpfResilienceRuntimePolicy.Stage.ATTEMPT);
                if (attemptTimeout.isZero()) throw new TimeoutException("overall deadline exhausted");
                CpfContext.CpfOperationContext parentOperation = caller.context().operation();
                CpfContext.CpfOperationContext attemptOperation = parentOperation == null ? null
                        : new CpfContext.CpfOperationContext(
                                context.operationId() + ":attempt:" + attempt,
                                parentOperation.operationName(), parentOperation.commandId(),
                                parentOperation.idempotencyKey(), parentOperation.idempotencyScope(),
                                parentOperation.idempotencyMode(), parentOperation.payloadFingerprint(),
                                parentOperation.operationId(), parentOperation.transactionSequence());
                CpfContextSnapshot attemptSnapshot = contextFactory.childSnapshot(caller,
                        new CpfContextExecutionFactory.ChildSpec(
                                context.operationId(), CpfContext.CpfExecutionType.INTEGRATION, attempt,
                                caller.context().execution().deadline(), attemptOperation));
                T value = invokeWithTimeout(action, attemptTimeout, attemptSnapshot);
                if (value == null) {
                    markSpanErrorSafely(attemptSpan, new TraceOutcomeException("NULL_RESULT"));
                    guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                            base.circuitOpenDuration(), permit);
                    return audited(context, outcome(CpfResilienceOutcome.Status.FAILED, null,
                            "NULL_RESULT", attempt, base.revision()));
                }
                guard.circuit.onSuccess();
                return audited(context, outcome(CpfResilienceOutcome.Status.SUCCESS, value, null,
                        attempt, base.revision()));
            } catch (EngineClosedException engineClosed) {
                markSpanErrorSafely(attemptSpan, engineClosed);
                guard.circuit.releaseUnused(permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "ENGINE_CLOSED_DURING_EXECUTION", attempt, base.revision()));
            } catch (InterruptedException interrupted) {
                markSpanErrorSafely(attemptSpan, interrupted);
                Thread.currentThread().interrupt();
                guard.circuit.releaseUnused(permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "CANCELLED", attempt, base.revision()));
            } catch (TimeoutException timeout) {
                markSpanErrorSafely(attemptSpan, timeout);
                last = timeout;
                CpfResilienceFailureClassifier.Classification classification = classify(timeout);
                if (context.sideEffecting() && !context.timeoutRetryAllowed()) {
                    classification = CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT;
                }
                if (classification == CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT) {
                    guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                            base.circuitOpenDuration(), permit);
                    return audited(context, outcome(CpfResilienceOutcome.Status.UNKNOWN_RESULT, null,
                            "TIMEOUT_UNKNOWN_RESULT", attempt, base.revision()));
                }
                if (attempt == base.maxAttempts()
                        || classification != CpfResilienceFailureClassifier.Classification.RETRYABLE) {
                    guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                            base.circuitOpenDuration(), permit);
                    return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null,
                            deadline.expired() ? "OVERALL_TIMEOUT" : "ATTEMPT_TIMEOUT",
                            attempt, base.revision()));
                }
            } catch (Throwable failure) {
                markSpanErrorSafely(attemptSpan, failure);
                if (failure instanceof Error error) throw error;
                last = failure;
                CpfResilienceFailureClassifier.Classification classification = classify(failure);
                if (classification == CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT) {
                    guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                            base.circuitOpenDuration(), permit);
                    return audited(context, outcome(CpfResilienceOutcome.Status.UNKNOWN_RESULT, null,
                            "AMBIGUOUS_FAILURE", attempt, base.revision()));
                }
                if (attempt == base.maxAttempts()
                        || classification != CpfResilienceFailureClassifier.Classification.RETRYABLE) {
                    guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                            base.circuitOpenDuration(), permit);
                    return audited(context, outcome(CpfResilienceOutcome.Status.FAILED, null,
                            failure.getClass().getSimpleName(), attempt, base.revision()));
                }
            } finally {
                closeSpanSafely(attemptSpan);
            }

            Duration delay = retryDelay(runtime, attempt);
            if (deadline.remaining().compareTo(delay) <= 0) {
                guard.circuit.onFailure(clock.instant(), base.circuitFailureThreshold(),
                        base.circuitOpenDuration(), permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.TIMEOUT, null,
                        "OVERALL_TIMEOUT", attempt, base.revision()));
            }
            try {
                sleeper.sleep(delay);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                guard.circuit.releaseUnused(permit);
                return audited(context, outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "CANCELLED", attempt, base.revision()));
            }
        }
        throw new IllegalStateException("unreachable", last);
    }

    private CpfTraceContext resilienceTraceContext(CpfResilienceCallContext context, boolean reconcile) {
        CpfTraceContext.SpanKind kind = traceKind(context);
        String operation = (reconcile ? "reconcile." : "execute.") + context.operationId();
        try {
            return CpfTraceContext.root(
                    traceFallbackIdentifier(context.transactionId()), kind, operation, traceBaggage(context));
        } catch (RuntimeException invalidTraceInput) {
            return CpfTraceContext.root(traceFallbackIdentifier(context.transactionId()),
                    kind, operation, Map.of("cpf.execution", safeTraceValue(context.operationId())));
        }
    }

    private static String traceFallbackIdentifier(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static CpfTraceContext.SpanKind traceKind(CpfResilienceCallContext context) {
        String value = context.attributes().get(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE);
        if (value == null || value.isBlank()) return CpfTraceContext.SpanKind.REMOTE;
        try {
            CpfTraceContext.SpanKind parsed = CpfTraceContext.SpanKind.valueOf(
                    value.trim().toUpperCase(Locale.ROOT));
            return parsed == CpfTraceContext.SpanKind.LOCAL
                    ? parsed : CpfTraceContext.SpanKind.REMOTE;
        } catch (IllegalArgumentException ignored) {
            return CpfTraceContext.SpanKind.REMOTE;
        }
    }

    private static String traceSegment(CpfResilienceCallContext context) {
        String segment = context.attributes().get(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE);
        return segment == null || segment.isBlank() ? "resilience" : safeTraceValue(segment);
    }

    private static Map<String, String> traceBaggage(CpfResilienceCallContext context) {
        LinkedHashMap<String, String> baggage = new LinkedHashMap<>();
        copyTraceAttribute(context.attributes(), baggage, "cpf.module");
        copyTraceAttribute(context.attributes(), baggage, "cpf.channel");
        copyTraceAttribute(context.attributes(), baggage, "cpf.tenant");
        copyTraceAttribute(context.attributes(), baggage, "cpf.correlation");
        baggage.put("cpf.execution", safeTraceValue(context.operationId()));
        return Map.copyOf(baggage);
    }

    private static void copyTraceAttribute(
            Map<String, String> source, Map<String, String> target, String key) {
        String value = source.get(key);
        if (value != null && !value.isBlank()) target.put(key, safeTraceValue(value));
    }

    private static String safeTraceValue(String value) {
        String normalized = value == null ? "unknown" : value.trim();
        if (normalized.isEmpty()) normalized = "unknown";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest, 0, 12);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private CpfTelemetry.CpfTelemetrySpan startSpanSafely(CpfTraceContext context) {
        try {
            return telemetry.startSpan(context);
        } catch (RuntimeException telemetryFailure) {
            return CpfTelemetry.noop().startSpan(context);
        }
    }

    private static void markSpanErrorSafely(CpfTelemetry.CpfTelemetrySpan span, Throwable failure) {
        try {
            span.error(failure);
        } catch (RuntimeException ignored) {
            // Telemetry must never change the business outcome.
        }
    }

    private static void closeSpanSafely(CpfTelemetry.CpfTelemetrySpan span) {
        try {
            span.close();
        } catch (RuntimeException ignored) {
            // Telemetry must never change the business outcome.
        }
    }

    private static final class TraceOutcomeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        TraceOutcomeException(String outcome) { super(outcome, null, false, false); }
    }

    private CpfResilienceFailureClassifier.Classification classify(Throwable failure) {
        try {
            CpfResilienceFailureClassifier.Classification result = classifier.classify(failure);
            return result == null ? CpfResilienceFailureClassifier.Classification.NON_RETRYABLE : result;
        } catch (RuntimeException classifierFailure) {
            return CpfResilienceFailureClassifier.Classification.UNKNOWN_RESULT;
        }
    }

    private Duration retryDelay(CpfResilienceRuntimePolicy runtime, int failedAttempt) {
        long multiplier = failedAttempt >= 62 ? Long.MAX_VALUE : 1L << Math.max(0, failedAttempt - 1);
        Duration exponential;
        try {
            exponential = runtime.initialRetryBackoff().multipliedBy(multiplier);
        } catch (ArithmeticException overflow) {
            exponential = runtime.maxRetryBackoff();
        }
        Duration capped = exponential.compareTo(runtime.maxRetryBackoff()) > 0
                ? runtime.maxRetryBackoff() : exponential;
        if (capped.isZero() || runtime.jitterRatio() == 0.0d) return capped;
        double sample = random.getAsDouble();
        if (!Double.isFinite(sample)) sample = 0.5d;
        double boundedRandom = Math.max(0.0d, Math.min(Math.nextDown(1.0d), sample));
        double factor = (1.0d - runtime.jitterRatio())
                + (2.0d * runtime.jitterRatio() * boundedRandom);
        long nanos = Math.max(0L, Math.round(safeToNanos(capped) * factor));
        return Duration.ofNanos(nanos);
    }

    private <T> T invokeWithTimeout(Supplier<T> action, Duration timeout, CpfContextSnapshot snapshot) throws Throwable {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("caller interrupted before attempt dispatch");
        }
        if (closed.get()) throw new EngineClosedException();
        Future<T> future;
        try {
            future = executor.submit(() -> {
                try (AutoCloseable ignoredContext = CpfContexts.bind(snapshot)) {
                    return action.get();
                }
            });
        } catch (RejectedExecutionException rejectedExecution) {
            if (closed.get()) throw new EngineClosedException();
            throw rejectedExecution;
        }
        activeAttempts.put(future, Boolean.TRUE);
        if (closed.get()) {
            future.cancel(true);
            activeAttempts.remove(future);
            throw new EngineClosedException();
        }
        try {
            T value = future.get(safeToNanos(timeout), TimeUnit.NANOSECONDS);
            if (Thread.currentThread().isInterrupted()) {
                future.cancel(true);
                throw new InterruptedException("caller interrupted while attempt completed");
            }
            return value;
        } catch (TimeoutException timeoutFailure) {
            future.cancel(true);
            throw timeoutFailure;
        } catch (CancellationException cancelled) {
            if (closed.get()) throw new EngineClosedException();
            throw cancelled;
        } catch (ExecutionException executionFailure) {
            throw executionFailure.getCause();
        } catch (InterruptedException interrupted) {
            future.cancel(true);
            throw interrupted;
        } finally {
            activeAttempts.remove(future);
        }
    }

    private <T> CpfResilienceOutcome<T> audited(
            CpfResilienceCallContext context, CpfResilienceOutcome<T> result) {
        try {
            audit.record("RESILIENCE_EXECUTION", context.operationId(), null, result.reasonCode(),
                    Map.of(
                            "status", result.status().name(),
                            "attempts", Integer.toString(result.attempts()),
                            "policyRevision", Long.toString(result.policyRevision()),
                            "transactionId", safeTraceValue(context.transactionId())),
                    clock.instant());
            return result;
        } catch (RuntimeException auditFailure) {
            if (result.status() == CpfResilienceOutcome.Status.REJECTED && result.attempts() == 0) {
                return outcome(CpfResilienceOutcome.Status.REJECTED, null,
                        "AUDIT_UNAVAILABLE", 0, result.policyRevision());
            }
            return outcome(CpfResilienceOutcome.Status.UNKNOWN_RESULT, null,
                    "AUDIT_PERSISTENCE_FAILED_AFTER_" + result.status().name(),
                    result.attempts(), result.policyRevision());
        }
    }

    private <T> CpfResilienceOutcome<T> outcome(
            CpfResilienceOutcome.Status status, T value, String reason, int attempts, long revision) {
        return new CpfResilienceOutcome<>(status, value, reason, attempts, revision, clock.instant());
    }

    private static Duration elapsed(Instant requestedAt, Instant now) {
        if (!requestedAt.isBefore(now)) return Duration.ZERO;
        try {
            return Duration.between(requestedAt, now);
        } catch (ArithmeticException overflow) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }
    }

    private static Duration distributedProbeLease(
            CpfResiliencePolicy base,
            CpfResilienceRuntimePolicy runtime,
            CpfResilienceDeadline deadline) {
        Duration desired = max(base.circuitOpenDuration(), runtime.attemptTimeout());
        return deadline.cap(desired);
    }

    private static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MAX;
        }
    }

    private static Duration max(Duration first, Duration second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    private static long safeToNanos(Duration value) {
        try {
            return Math.max(1L, value.toNanos());
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static void sleepThread(Duration duration) throws InterruptedException {
        if (!duration.isZero()) TimeUnit.NANOSECONDS.sleep(safeToNanos(duration));
    }

    private Guard acquireGuard(GuardKey key, CpfResilienceRuntimePolicy runtime) {
        long nowNanos = nanoTime.getAsLong();
        synchronized (guardAllocationMonitor) {
            guards.entrySet().removeIf(entry -> {
                boolean superseded = entry.getKey().operationId().equals(key.operationId())
                        && !entry.getKey().equals(key)
                        && entry.getValue().unused();
                boolean expired = entry.getValue().expiredAndUnused(nowNanos, guardIdleTtlNanos);
                if (superseded || expired) guardEvictionCount.incrementAndGet();
                return superseded || expired;
            });
            Guard existing = guards.get(key);
            if (existing != null) {
                existing.retain(nowNanos);
                return existing;
            }
            if (guards.size() >= maximumGuardEntries) {
                guardCapacityRejectionCount.incrementAndGet();
                return null;
            }
            Guard created = new Guard(runtime, nowNanos);
            created.retain(nowNanos);
            guards.put(key, created);
            return created;
        }
    }

    @Override
    public RuntimeSnapshot resilienceRuntimeSnapshot() {
        boolean isClosed = closed.get();
        long rejected = guardCapacityRejectionCount.get();
        Health health = isClosed ? Health.DOWN
                : rejected > 0L || guards.size() >= maximumGuardEntries ? Health.DEGRADED : Health.UP;
        return new RuntimeSnapshot(
                health,
                guards.size(),
                maximumGuardEntries,
                guardIdleTtl,
                activeAttempts.size(),
                guardEvictionCount.get(),
                rejected,
                isClosed,
                clock.instant());
    }

    private static Duration positiveBounded(Duration value, Duration maximum, String field) {
        if (value.isZero() || value.isNegative() || value.compareTo(maximum) > 0) {
            throw new IllegalArgumentException(field + " must be positive and <= " + maximum);
        }
        return value;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            activeAttempts.keySet().forEach(future -> future.cancel(true));
            activeAttempts.clear();
            guards.clear();
            executor.shutdownNow();
        }
    }

    private static final class EngineClosedException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private record GuardKey(
            String operationId, long revision, CpfResilienceRuntimePolicy runtimePolicy) {}

    private static final class Guard {
        private final QueuedBulkhead bulkhead;
        private final SlidingWindowRate rate = new SlidingWindowRate();
        private final SlidingWindowRate retryBudget = new SlidingWindowRate();
        private final Circuit circuit = new Circuit();
        private final AtomicInteger users = new AtomicInteger();
        private volatile long lastAccessNanos;

        private Guard(CpfResilienceRuntimePolicy policy, long createdNanos) {
            this.bulkhead = new QueuedBulkhead(policy.basePolicy().bulkheadMaxConcurrent());
            this.lastAccessNanos = createdNanos;
        }

        void retain(long nowNanos) {
            users.incrementAndGet();
            lastAccessNanos = nowNanos;
        }

        void releaseUse(long nowNanos) {
            lastAccessNanos = nowNanos;
            int remaining = users.decrementAndGet();
            if (remaining < 0) {
                users.incrementAndGet();
                throw new IllegalStateException("resilience guard use count underflow");
            }
        }

        boolean unused() {
            return users.get() == 0;
        }

        boolean expiredAndUnused(long nowNanos, long ttlNanos) {
            if (!unused()) return false;
            long elapsed = nowNanos - lastAccessNanos;
            return elapsed < 0L || elapsed >= ttlNanos;
        }
    }

    private enum CircuitPermit { CLOSED, HALF_OPEN, DENIED }

    private static final class Circuit {
        private int failures;
        private Instant openUntil = Instant.EPOCH;
        private boolean halfOpenInFlight;

        synchronized CircuitPermit tryAcquire(Instant now) {
            if (openUntil.isAfter(now)) return CircuitPermit.DENIED;
            if (!openUntil.equals(Instant.EPOCH)) {
                if (halfOpenInFlight) return CircuitPermit.DENIED;
                halfOpenInFlight = true;
                return CircuitPermit.HALF_OPEN;
            }
            return CircuitPermit.CLOSED;
        }

        synchronized void onSuccess() {
            failures = 0;
            openUntil = Instant.EPOCH;
            halfOpenInFlight = false;
        }

        synchronized void onFailure(
                Instant now, int threshold, Duration openDuration, CircuitPermit permit) {
            failures++;
            if (permit == CircuitPermit.HALF_OPEN || failures >= threshold) {
                openUntil = safePlus(now, openDuration);
                failures = 0;
            }
            halfOpenInFlight = false;
        }

        synchronized void releaseUnused(CircuitPermit permit) {
            if (permit == CircuitPermit.HALF_OPEN) halfOpenInFlight = false;
        }
    }

    private static final class QueuedBulkhead {
        private final Semaphore permits;
        private final AtomicInteger queued = new AtomicInteger();

        private QueuedBulkhead(int maxConcurrent) {
            this.permits = new Semaphore(maxConcurrent, true);
        }

        boolean acquire(int queueLimit, Duration wait) throws InterruptedException {
            if (permits.tryAcquire()) return true;
            if (queueLimit == 0 || queued.incrementAndGet() > queueLimit) {
                if (queueLimit > 0) queued.decrementAndGet();
                return false;
            }
            try {
                return permits.tryAcquire(safeToNanos(wait), TimeUnit.NANOSECONDS);
            } finally {
                queued.decrementAndGet();
            }
        }

        void release() {
            permits.release();
        }
    }

    private static final class SlidingWindowRate {
        private Instant windowStart = Instant.EPOCH;
        private int permits;

        synchronized boolean tryAcquire(Instant now, int max, Duration window) {
            if (max <= 0) return false;
            if (!now.isBefore(safePlus(windowStart, window))) {
                windowStart = now;
                permits = 0;
            }
            if (permits >= max) return false;
            permits++;
            return true;
        }
    }
}
