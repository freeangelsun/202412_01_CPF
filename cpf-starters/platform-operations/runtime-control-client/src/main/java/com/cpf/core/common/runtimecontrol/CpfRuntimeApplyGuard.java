package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

/**
 * Runtime ChangeApplier 호출에 deadline, bounded retry, bulkhead, circuit stop을 강제합니다.
 *
 * <p>Public SPI는 변경하지 않습니다. 명시적 {@link CpfRuntimeApplyResult#failure(String, String)}는
 * side effect 미발생 계약이므로 transient code에 한해 재시도합니다. 예외, timeout, null 결과는
 * side effect 여부를 알 수 없어 UNKNOWN으로 보존하고 자동 재시도하지 않습니다.</p>
 */
public final class CpfRuntimeApplyGuard implements AutoCloseable {
    private static final Set<String> TRANSIENT_ERROR_CODES = Set.of(
            "BUSY",
            "DEPENDENCY_TIMEOUT",
            "DEPENDENCY_UNAVAILABLE",
            "LOCK_TIMEOUT",
            "RATE_LIMITED",
            "RETRYABLE",
            "SERVICE_UNAVAILABLE",
            "TEMPORARY_FAILURE",
            "TRANSIENT_FAILURE");

    private final Policy policy;
    private final Clock clock;
    private final Sleeper sleeper;
    private final DoubleSupplier jitterSource;
    private final ThreadPoolExecutor executor;
    private final Semaphore bulkhead;
    private final Map<String, Circuit> circuits = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public CpfRuntimeApplyGuard(Policy policy) {
        this(policy, Clock.systemUTC(), Thread::sleep, Math::random);
    }

    CpfRuntimeApplyGuard(Policy policy, Clock clock, Sleeper sleeper, DoubleSupplier jitterSource) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
        this.jitterSource = Objects.requireNonNull(jitterSource, "jitterSource");
        AtomicInteger sequence = new AtomicInteger();
        ThreadFactory threadFactory = task -> {
            Thread thread = new Thread(task, "cpf-runtime-apply-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        this.bulkhead = new Semaphore(policy.maxConcurrency(), true);
        this.executor = new ThreadPoolExecutor(
                policy.maxConcurrency(),
                policy.maxConcurrency(),
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(policy.maxConcurrency()),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        this.executor.allowCoreThreadTimeOut(true);
    }

    public static CpfRuntimeApplyGuard defaults() {
        return new CpfRuntimeApplyGuard(Policy.defaults());
    }

    public CpfRuntimeApplyResult execute(
            CpfRuntimeChangeApplier applier,
            CpfRuntimeDelivery delivery,
            Runnable beforeAttempt,
            Runnable afterSafeFailure) {
        Objects.requireNonNull(applier, "applier");
        Objects.requireNonNull(delivery, "delivery");
        Objects.requireNonNull(beforeAttempt, "beforeAttempt");
        Objects.requireNonNull(afterSafeFailure, "afterSafeFailure");
        if (closed.get()) {
            return CpfRuntimeApplyResult.failure(
                    "APPLY_GUARD_CLOSED",
                    "Runtime apply guard가 종료되어 새 요청을 수락할 수 없습니다.");
        }

        String circuitKey = normalize(delivery.changeType());
        CircuitPermit permit = acquireCircuitPermit(circuitKey);
        if (!permit.allowed()) {
            return CpfRuntimeApplyResult.failure(
                    "APPLY_CIRCUIT_OPEN",
                    "Runtime apply circuit가 cooldown 중입니다. changeType=" + circuitKey);
        }

        Instant startedAt = clock.instant();
        Instant deadline = effectiveDeadline(delivery, startedAt);
        if (!deadline.isAfter(startedAt)) {
            CpfRuntimeApplyResult expired = CpfRuntimeApplyResult.failure(
                    "DELIVERY_EXPIRED",
                    "Runtime delivery deadline이 이미 만료되었습니다.");
            completeCircuit(circuitKey, permit.halfOpenProbe(), expired);
            return expired;
        }

        CpfRuntimeApplyResult result = null;
        int localAttempt = 0;
        while (localAttempt < policy.maxAttempts()) {
            localAttempt++;
            long remainingMillis = remainingMillis(deadline);
            if (remainingMillis <= 0L) {
                result = CpfRuntimeApplyResult.failure(
                        "APPLY_RETRY_BUDGET_EXHAUSTED",
                        "Runtime apply deadline 내 재시도 budget을 소진했습니다.");
                break;
            }

            CpfRuntimeDelivery attemptDelivery = withAttempt(delivery, localAttempt);
            AttemptOutcome outcome = invokeOnce(applier, attemptDelivery, beforeAttempt, remainingMillis);
            result = outcome.result();
            if (!shouldRetry(result, outcome.started(), localAttempt)) {
                break;
            }
            if (outcome.started()) {
                try {
                    afterSafeFailure.run();
                } catch (RuntimeException cleanupFailure) {
                    result = CpfRuntimeApplyResult.unknown(
                            "APPLY_SAFE_FAILURE_CLEANUP_UNKNOWN",
                            cleanupFailure.getMessage() == null ? "" : cleanupFailure.getMessage());
                    break;
                }
            }

            long backoffMillis = backoffMillis(localAttempt);
            long remainingAfterAttempt = remainingMillis(deadline);
            if (backoffMillis > 0L && remainingAfterAttempt <= backoffMillis) {
                result = CpfRuntimeApplyResult.failure(
                        "TRANSIENT_RETRY_BUDGET_EXHAUSTED",
                        "Runtime apply deadline 내 backoff budget이 부족합니다.");
                break;
            }
            if (backoffMillis == 0L) {
                continue;
            }
            try {
                sleeper.sleep(backoffMillis);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                result = CpfRuntimeApplyResult.failure(
                        "APPLY_RETRY_INTERRUPTED",
                        "Runtime apply 재시도 backoff가 중단되었습니다.");
                break;
            }
        }

        if (result == null) {
            result = CpfRuntimeApplyResult.unknown(
                    "APPLY_RESULT_MISSING",
                    "Runtime apply guard가 최종 결과를 확정하지 못했습니다.");
        }
        completeCircuit(circuitKey, permit.halfOpenProbe(), result);
        return result;
    }

    public CpfRuntimeApplyResult execute(
            CpfRuntimeChangeApplier applier,
            CpfRuntimeDelivery delivery,
            Runnable beforeAttempt) {
        return execute(applier, delivery, beforeAttempt, () -> { });
    }

    public CpfRuntimeApplyResult execute(CpfRuntimeChangeApplier applier, CpfRuntimeDelivery delivery) {
        return execute(applier, delivery, () -> { }, () -> { });
    }

    private AttemptOutcome invokeOnce(
            CpfRuntimeChangeApplier applier,
            CpfRuntimeDelivery delivery,
            Runnable beforeAttempt,
            long remainingMillis) {
        if (!bulkhead.tryAcquire()) {
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.failure(
                            "APPLY_BULKHEAD_FULL",
                            "Runtime apply bulkhead가 가득 차 요청을 거부했습니다."),
                    false);
        }

        try {
            // PREPARED durable journal을 호출 스레드에서 먼저 확정합니다. 이를 worker 내부에서 수행하면
            // timeout 반환이 fsync보다 앞서 UNKNOWN 증거 없이 side effect가 시작될 수 있습니다.
            beforeAttempt.run();
        } catch (RuntimeException prepareFailure) {
            bulkhead.release();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.unknown(
                            "APPLY_PREPARE_UNKNOWN",
                            prepareFailure.getMessage() == null ? "" : prepareFailure.getMessage()),
                    false);
        }

        long budgetMillis = Math.min(remainingMillis, remainingMillis(delivery.expiresAt()));
        if (budgetMillis <= 0L) {
            bulkhead.release();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.failure(
                            "APPLY_RETRY_BUDGET_EXHAUSTED",
                            "PREPARED 이후 Runtime apply deadline budget이 남아 있지 않습니다."),
                    false);
        }

        AtomicBoolean taskStarted = new AtomicBoolean();
        AtomicBoolean permitReleased = new AtomicBoolean();
        Runnable releasePermit = () -> {
            if (permitReleased.compareAndSet(false, true)) {
                bulkhead.release();
            }
        };
        Future<CpfRuntimeApplyResult> future;
        try {
            future = executor.submit(() -> {
                taskStarted.set(true);
                try {
                    return applier.apply(delivery);
                } finally {
                    releasePermit.run();
                }
            });
        } catch (RejectedExecutionException rejected) {
            releasePermit.run();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.failure(
                            "APPLY_EXECUTOR_UNAVAILABLE",
                            "Runtime apply executor가 요청을 수락하지 않았습니다."),
                    false);
        }

        try {
            CpfRuntimeApplyResult result = future.get(budgetMillis, TimeUnit.MILLISECONDS);
            if (result == null) {
                return new AttemptOutcome(
                        CpfRuntimeApplyResult.unknown(
                                "NULL_APPLY_RESULT",
                                "Runtime applier가 결과를 반환하지 않아 side effect를 확정할 수 없습니다."),
                        true);
            }
            return new AttemptOutcome(result, true);
        } catch (TimeoutException timeout) {
            future.cancel(true);
            if (!taskStarted.get()) releasePermit.run();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.unknown(
                            "APPLY_TIMEOUT_UNKNOWN",
                            "Runtime applier가 deadline 내 종료되지 않아 side effect 결과가 불명입니다."),
                    true);
        } catch (InterruptedException interrupted) {
            future.cancel(true);
            if (!taskStarted.get()) releasePermit.run();
            Thread.currentThread().interrupt();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.unknown(
                            "APPLY_INTERRUPTED_UNKNOWN",
                            "Runtime applier 대기가 중단되어 side effect 결과가 불명입니다."),
                    true);
        } catch (ExecutionException execution) {
            Throwable cause = execution.getCause() == null ? execution : execution.getCause();
            return new AttemptOutcome(
                    CpfRuntimeApplyResult.unknown(
                            cause.getClass().getSimpleName(),
                            cause.getMessage() == null ? "" : cause.getMessage()),
                    true);
        }
    }

    private long remainingMillis(Instant deadline) {
        if (deadline == null) return Long.MAX_VALUE;
        long remaining = deadline.toEpochMilli() - clock.instant().toEpochMilli();
        return Math.max(0L, remaining);
    }

    private boolean shouldRetry(CpfRuntimeApplyResult result, boolean started, int localAttempt) {
        if (localAttempt >= policy.maxAttempts()) return false;
        if (result.applied() || result.restartRequired() || result.unknownResult()) return false;
        String code = normalize(result.errorCode());
        if (!started && "APPLY_BULKHEAD_FULL".equals(code)) return true;
        return isTransient(code);
    }

    private boolean isTransient(String code) {
        if (code.isBlank()) return false;
        return TRANSIENT_ERROR_CODES.contains(code)
                || code.startsWith("RETRYABLE_")
                || code.startsWith("TEMPORARY_")
                || code.startsWith("TRANSIENT_");
    }

    private boolean isCircuitFailure(CpfRuntimeApplyResult result) {
        if (result.unknownResult()) return true;
        String code = normalize(result.errorCode());
        return "APPLY_BULKHEAD_FULL".equals(code) || isTransient(code);
    }

    private Instant effectiveDeadline(CpfRuntimeDelivery delivery, Instant now) {
        Instant localDeadline = now.plusMillis(policy.maxApplyMillis());
        Instant deliveryDeadline = delivery.expiresAt();
        if (deliveryDeadline == null || deliveryDeadline.isAfter(localDeadline)) {
            return localDeadline;
        }
        return deliveryDeadline;
    }

    private long backoffMillis(int completedAttempt) {
        long base = policy.initialBackoffMillis();
        for (int i = 1; i < completedAttempt; i++) {
            if (base >= policy.maxBackoffMillis() / 2L) {
                base = policy.maxBackoffMillis();
                break;
            }
            base *= 2L;
        }
        base = Math.min(base, policy.maxBackoffMillis());
        if (base == 0L || policy.jitterPercent() == 0) return base;
        double centered = Math.max(0.0d, Math.min(1.0d, jitterSource.getAsDouble())) * 2.0d - 1.0d;
        long delta = Math.round(base * (policy.jitterPercent() / 100.0d) * centered);
        return Math.max(0L, base + delta);
    }

    private CpfRuntimeDelivery withAttempt(CpfRuntimeDelivery source, int localAttempt) {
        int attempt = Math.max(0, source.attempt()) + localAttempt - 1;
        return new CpfRuntimeDelivery(
                source.deliveryId(),
                source.changeId(),
                source.changeType(),
                source.instanceId(),
                source.desiredVersion(),
                source.fencingToken(),
                source.requestHash(),
                source.payloadHash(),
                source.payloadSchemaVersion(),
                source.payload(),
                attempt,
                source.expiresAt());
    }

    private CircuitPermit acquireCircuitPermit(String key) {
        Circuit circuit = circuits.computeIfAbsent(key, ignored -> new Circuit());
        synchronized (circuit) {
            Instant now = clock.instant();
            if (circuit.openUntil != null && now.isBefore(circuit.openUntil)) {
                return new CircuitPermit(false, false);
            }
            if (circuit.openUntil != null) {
                if (circuit.halfOpenProbeInFlight) {
                    return new CircuitPermit(false, false);
                }
                circuit.halfOpenProbeInFlight = true;
                return new CircuitPermit(true, true);
            }
            return new CircuitPermit(true, false);
        }
    }

    private void completeCircuit(String key, boolean halfOpenProbe, CpfRuntimeApplyResult result) {
        Circuit circuit = circuits.computeIfAbsent(key, ignored -> new Circuit());
        synchronized (circuit) {
            if (!isCircuitFailure(result)) {
                circuit.consecutiveFailures = 0;
                circuit.openUntil = null;
                circuit.halfOpenProbeInFlight = false;
                return;
            }
            circuit.consecutiveFailures++;
            circuit.halfOpenProbeInFlight = false;
            if (halfOpenProbe || circuit.consecutiveFailures >= policy.circuitFailureThreshold()) {
                circuit.openUntil = clock.instant().plusMillis(policy.circuitOpenMillis());
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            circuits.clear();
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private record AttemptOutcome(CpfRuntimeApplyResult result, boolean started) { }

    private record CircuitPermit(boolean allowed, boolean halfOpenProbe) { }

    private static final class Circuit {
        private int consecutiveFailures;
        private Instant openUntil;
        private boolean halfOpenProbeInFlight;
    }

    /** Runtime apply execution guard 설정입니다. */
    public record Policy(
            long maxApplyMillis,
            int maxAttempts,
            long initialBackoffMillis,
            long maxBackoffMillis,
            int jitterPercent,
            int maxConcurrency,
            int circuitFailureThreshold,
            long circuitOpenMillis) {
        public Policy {
            if (maxApplyMillis < 10L) throw new IllegalArgumentException("maxApplyMillis must be >= 10");
            if (maxAttempts < 1 || maxAttempts > 20) throw new IllegalArgumentException("maxAttempts must be 1..20");
            if (initialBackoffMillis < 0L) throw new IllegalArgumentException("initialBackoffMillis must be >= 0");
            if (maxBackoffMillis < initialBackoffMillis) {
                throw new IllegalArgumentException("maxBackoffMillis must be >= initialBackoffMillis");
            }
            if (jitterPercent < 0 || jitterPercent > 100) throw new IllegalArgumentException("jitterPercent must be 0..100");
            if (maxConcurrency < 1 || maxConcurrency > 32) throw new IllegalArgumentException("maxConcurrency must be 1..32");
            if (circuitFailureThreshold < 1) throw new IllegalArgumentException("circuitFailureThreshold must be >= 1");
            if (circuitOpenMillis < 10L) throw new IllegalArgumentException("circuitOpenMillis must be >= 10");
        }

        public static Policy defaults() {
            return new Policy(30_000L, 3, 200L, 2_000L, 20, 1, 3, 30_000L);
        }
    }
}
