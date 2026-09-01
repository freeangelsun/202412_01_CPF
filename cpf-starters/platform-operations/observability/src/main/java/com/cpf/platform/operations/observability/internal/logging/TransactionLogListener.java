package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.security.api.CpfMaskingRuntime;

import com.cpf.platform.operations.observability.api.logging.CpfAsyncLogWriterOperations;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.platform.operations.observability.api.CpfTraceContext;
import com.cpf.platform.operations.observability.internal.logging.fallback.CpfTransactionLogFallbackPort;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Receives transaction-log events and writes them through a bounded asynchronous DB/spool pipeline.
 * Queue saturation and shutdown draining preserve events in the durable fallback journal instead of dropping them.
 */
@Component
public class TransactionLogListener implements CpfAsyncLogWriterOperations, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(TransactionLogListener.class);
    private static final int MAX_WORKERS = 32;
    private static final int MAX_QUEUE_CAPACITY = 100_000;
    private static final long MAX_SHUTDOWN_WAIT_MS = 60_000L;

    private final TransactionLogService logService;
    private final CpfTransactionLogFallbackPort fallbackStore;
    private final ThreadPoolExecutor executor;
    private final int workerCount;
    private final int queueCapacity;
    private final long shutdownWaitMs;
    private final Clock clock;
    private volatile CpfTelemetry telemetry = CpfTelemetry.noop();
    private volatile CpfTraceSamplingPolicy traceSamplingPolicy = new CpfTraceSamplingPolicy();
    private final AtomicBoolean draining = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong acceptedCount = new AtomicLong();
    private final AtomicLong completedCount = new AtomicLong();
    private final AtomicLong rejectedCount = new AtomicLong();
    private final AtomicLong fallbackPreservedCount = new AtomicLong();
    private final AtomicLong terminalLossCount = new AtomicLong();
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>();
    private final AtomicReference<Instant> lastTerminalLossAt = new AtomicReference<>();

    @Autowired
    public TransactionLogListener(
            TransactionLogService logService,
            CpfTransactionLogFallbackPort fallbackStore,
            Environment environment) {
        this(logService, fallbackStore, settings(environment), Clock.systemUTC());
    }

    /** Compatibility constructor used by direct unit consumers; Spring uses the bounded async constructor. */
    public TransactionLogListener(
            TransactionLogService logService,
            CpfTransactionLogFallbackPort fallbackStore) {
        this(logService, fallbackStore, AsyncSettings.synchronous(), Clock.systemUTC());
    }

    TransactionLogListener(
            TransactionLogService logService,
            CpfTransactionLogFallbackPort fallbackStore,
            AsyncSettings settings,
            Clock cpfStarterClock) {
        this.logService = Objects.requireNonNull(logService, "logService");
        this.fallbackStore = Objects.requireNonNull(fallbackStore, "fallbackStore");
        this.clock = Objects.requireNonNull(cpfStarterClock, "clock");
        AsyncSettings safeSettings = Objects.requireNonNull(settings, "settings");
        this.workerCount = safeSettings.workerCount();
        this.queueCapacity = safeSettings.queueCapacity();
        this.shutdownWaitMs = safeSettings.shutdownWait().toMillis();
        this.executor = safeSettings.enabled()
                ? new ThreadPoolExecutor(
                        workerCount,
                        workerCount,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(queueCapacity),
                        namedThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy())
                : null;
    }


    @Autowired
    void configureTelemetry(CpfTelemetry telemetry, CpfTraceSamplingPolicy traceSamplingPolicy) {
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        this.traceSamplingPolicy = Objects.requireNonNull(traceSamplingPolicy, "traceSamplingPolicy");
    }

    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        if (event == null || event.getRecord() == null) {
            CpfTransactionContextAnomalyMonitor.recordMissing("TransactionLogListener.invalidEvent");
            terminalLossCount.incrementAndGet();
            Instant now = clock.instant();
            lastFailureAt.set(now);
            lastTerminalLossAt.set(now);
            log.error("CPF transaction-log event or record was null; event was rejected without side effects");
            return;
        }
        final LogWork work;
        try {
            work = snapshot(event);
        } catch (RuntimeException snapshotFailure) {
            preserveFallback(new LogWork(event.getRecord(), copyDetails(event.getDetails()), event.getLogPolicy()),
                    snapshotFailure, "EVENT_SNAPSHOT_FAILED");
            return;
        }
        if (closed.get() || draining.get()) {
            rejectedCount.incrementAndGet();
            preserveFallback(work, new RejectedExecutionException("ASYNC_LOG_WRITER_NOT_ACCEPTING"),
                    "ASYNC_WRITER_NOT_ACCEPTING");
            return;
        }
        if (executor == null) {
            acceptedCount.incrementAndGet();
            persist(work);
            return;
        }
        try {
            executor.execute(new LogTask(work));
            acceptedCount.incrementAndGet();
        } catch (RejectedExecutionException queueFull) {
            rejectedCount.incrementAndGet();
            preserveFallback(work, queueFull, "ASYNC_QUEUE_FULL");
        }
    }

    @Override
    public WriterSnapshot snapshot() {
        WriterState state;
        if (closed.get()) state = WriterState.CLOSED;
        else if (draining.get()) state = WriterState.DRAINING;
        else if (terminalLossCount.get() > 0 || rejectedCount.get() > 0 || fallbackPreservedCount.get() > 0) {
            state = WriterState.DEGRADED;
        } else state = WriterState.RUNNING;
        return new WriterSnapshot(
                executor != null,
                workerCount,
                queueCapacity,
                executor == null ? 0 : executor.getQueue().size(),
                executor == null ? 0 : executor.getActiveCount(),
                acceptedCount.get(),
                completedCount.get(),
                rejectedCount.get(),
                fallbackPreservedCount.get(),
                terminalLossCount.get(),
                lastFailureAt.get(),
                lastTerminalLossAt.get(),
                state);
    }

    @Override
    @PreDestroy
    public void close() {
        if (executor == null || !draining.compareAndSet(false, true)) {
            closed.set(true);
            return;
        }
        executor.shutdown();
        boolean terminated = false;
        try {
            terminated = executor.awaitTermination(shutdownWaitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            lastFailureAt.set(clock.instant());
        }
        if (!terminated) {
            List<Runnable> abandoned = executor.shutdownNow();
            for (Runnable runnable : abandoned) {
                if (runnable instanceof LogTask task) {
                    rejectedCount.incrementAndGet();
                    preserveFallback(task.work,
                            new RejectedExecutionException("ASYNC_LOG_WRITER_SHUTDOWN_DRAIN"),
                            "ASYNC_SHUTDOWN_DRAIN");
                }
            }
        }
        closed.set(true);
        draining.set(false);
    }

    private LogWork snapshot(TransactionLogEvent event) {
        TransactionLogRecord copy = new TransactionLogRecord();
        BeanUtils.copyProperties(event.getRecord(), copy);
        CpfTransactionTraceEnricher.enrich(copy);
        CpfTransactionLogIdentity.ensure(copy);
        return new LogWork(copy, copyDetails(event.getDetails()), event.getLogPolicy());
    }

    private static Map<String, String> copyDetails(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    private void persist(LogWork work) {
        CpfTelemetry.CpfTelemetrySpan span = startPersistenceSpan(work.record());
        try {
            logService.saveTransactionLog(work.record(), work.details(), work.logPolicy());
        } catch (Exception databaseFailure) {
            markSpanError(span, databaseFailure);
            preserveFallback(work, databaseFailure, "DB_PERSISTENCE_FAILED");
        } finally {
            closeSpan(span);
            completedCount.incrementAndGet();
        }
    }

    private static void markSpanError(CpfTelemetry.CpfTelemetrySpan span, Throwable failure) {
        try {
            span.error(failure);
        } catch (RuntimeException telemetryFailure) {
            log.warn("CPF transaction-log telemetry error marking failed; logging continues. failureType={}",
                    telemetryFailure.getClass().getName());
        }
    }

    private static void closeSpan(CpfTelemetry.CpfTelemetrySpan span) {
        try {
            span.close();
        } catch (RuntimeException telemetryFailure) {
            log.warn("CPF transaction-log telemetry close failed; logging continues. failureType={}",
                    telemetryFailure.getClass().getName());
        }
    }

    private CpfTelemetry.CpfTelemetrySpan startPersistenceSpan(TransactionLogRecord record) {
        try {
            boolean success = transactionSucceeded(record);
            if (!traceSamplingPolicy.shouldSample(
                    record.getTransactionId(), record.getStandardExecutionId(), record.getModuleId(), success)) {
                return CpfTelemetry.noop().startSpan("transaction-log.persist", "LOCAL", Map.of());
            }
            CpfTraceContext context = CpfTransactionTraceEnricher.enrich(record);
            return telemetry.startSpan(context.child(
                    CpfTraceContext.SpanKind.LOCAL,
                    "transaction-log.persist",
                    "DB_LOG",
                    Math.max(1, context.attempt() + 1),
                    Map.of()));
        } catch (RuntimeException telemetryFailure) {
            log.warn("CPF transaction-log telemetry start failed; logging continues. failureType={}",
                    telemetryFailure.getClass().getName());
            return CpfTelemetry.noop().startSpan("transaction-log.persist", "LOCAL", Map.of());
        }
    }

    private static boolean transactionSucceeded(TransactionLogRecord record) {
        if (record == null) return false;
        Integer status = record.getHttpStatus();
        return record.getErrorCode() == null && record.getErrorMessage() == null
                && (status == null || status < 400);
    }

    private void preserveFallback(LogWork work, Throwable failure, String boundary) {
        Instant failedAt = clock.instant();
        lastFailureAt.set(failedAt);
        String transactionId = safeTransactionId(work.record());
        try {
            boolean created = fallbackStore.enqueue(work.record(), work.details(), work.logPolicy(), failure);
            fallbackPreservedCount.incrementAndGet();
            log.warn("CPF transaction-log write was preserved in the durable journal. "
                            + "transactionId={}, boundary={}, created={}, failureType={}, failureMessageMasked={}",
                    transactionId,
                    boundary,
                    created,
                    failure == null ? "UNKNOWN" : failure.getClass().getName(),
                    maskedMessage(failure));
        } catch (RuntimeException fallbackFailure) {
            terminalLossCount.incrementAndGet();
            lastTerminalLossAt.set(failedAt);
            long anomalyCount = CpfTransactionContextAnomalyMonitor.recordMissing(
                    "TransactionLogListener.dbAndFallbackFailure");
            log.error("CPF transaction-log DB/queue and durable journal both failed. "
                            + "transactionId={}, boundary={}, primaryFailureType={}, fallbackFailureType={}, "
                            + "fallbackMessageMasked={}, anomalyCount={}",
                    transactionId,
                    boundary,
                    failure == null ? "UNKNOWN" : failure.getClass().getName(),
                    fallbackFailure.getClass().getName(),
                    maskedMessage(fallbackFailure),
                    anomalyCount);
        }
    }

    private static String safeTransactionId(TransactionLogRecord record) {
        return CpfTransactionLogIdentity.opaque(record == null ? null : record.getTransactionId());
    }

    private static String maskedMessage(Throwable failure) {
        return CpfMaskingRuntime.mask(failure == null ? "" : failure.getMessage(), 500);
    }

    private static AsyncSettings settings(Environment environment) {
        Objects.requireNonNull(environment, "environment");
        boolean enabled = environment.getProperty("cpf.logging.async.enabled", Boolean.class, true);
        int defaultWorkers = Math.max(1, Math.min(2, Runtime.getRuntime().availableProcessors()));
        int workers = bounded(environment.getProperty(
                "cpf.logging.async.worker-count", Integer.class, defaultWorkers), 1, MAX_WORKERS,
                "cpf.logging.async.worker-count");
        int capacity = bounded(environment.getProperty(
                "cpf.logging.async.queue-capacity", Integer.class, 4096), 1, MAX_QUEUE_CAPACITY,
                "cpf.logging.async.queue-capacity");
        long shutdownWait = bounded(environment.getProperty(
                "cpf.logging.async.shutdown-wait-ms", Long.class, 10_000L), 1L, MAX_SHUTDOWN_WAIT_MS,
                "cpf.logging.async.shutdown-wait-ms");
        return new AsyncSettings(enabled, workers, capacity, Duration.ofMillis(shutdownWait));
    }

    private static int bounded(int value, int minimum, int maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static long bounded(long value, long minimum, long maximum, String property) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(property + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static ThreadFactory namedThreadFactory() {
        AtomicInteger sequence = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "cpf-transaction-log-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    record AsyncSettings(boolean enabled, int workerCount, int queueCapacity, Duration shutdownWait) {
        AsyncSettings {
            Objects.requireNonNull(shutdownWait, "shutdownWait");
            if (workerCount < 1 || queueCapacity < 1 || shutdownWait.isNegative() || shutdownWait.isZero()) {
                throw new IllegalArgumentException("valid async writer settings are required");
            }
        }

        static AsyncSettings synchronous() {
            return new AsyncSettings(false, 1, 1, Duration.ofSeconds(1));
        }
    }

    private record LogWork(
            TransactionLogRecord record,
            Map<String, String> details,
            LogPolicyDecision logPolicy) {
    }

    private final class LogTask implements Runnable {
        private final LogWork work;

        private LogTask(LogWork work) {
            this.work = work;
        }

        @Override
        public void run() {
            persist(work);
        }
    }
}
