package com.cpf.platform.operations.observability.internal.logging.file;

import com.cpf.platform.operations.observability.api.logging.CpfAsyncFileLogWriterOperations;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 파일 거래 로그를 bounded queue에서 비동기로 기록하고 overflow·손실·종료 상태를 계측합니다.
 * 큐가 포화되면 기본 정책은 caller-runs로 로그 손실을 방지하며, 명시적으로 reject 정책을 선택한 경우에만
 * 손실 신호와 카운터를 남기고 업무 처리는 차단하지 않습니다.
 */
@Component
public final class CpfAsyncFileLogWriter implements AutoCloseable, CpfAsyncFileLogWriterOperations {
    private static final int DEFAULT_CAPACITY = 4_096;
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);

    private final PreparedLogAdapter adapter;
    private final ArrayBlockingQueue<CpfFileLogWriter.PreparedTransactionLog> queue;
    private final OverflowPolicy overflowPolicy;
    private final Duration shutdownTimeout;
    private final Clock clock;
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong acceptedCount = new AtomicLong();
    private final AtomicLong writtenCount = new AtomicLong();
    private final AtomicLong callerRunsCount = new AtomicLong();
    private final AtomicLong rejectedCount = new AtomicLong();
    private final AtomicLong failedCount = new AtomicLong();
    private final AtomicLong terminalLossCount = new AtomicLong();
    private final AtomicReference<String> lastFailureType = new AtomicReference<>();
    private final ReentrantReadWriteLock admissionLock = new ReentrantReadWriteLock();
    private final Thread worker;

    @Autowired
    public CpfAsyncFileLogWriter(CpfFileLogWriter delegate, Environment environment) {
        this(
                new CpfFileLogAdapter(delegate),
                bounded(environment.getProperty(
                        "cpf.logging.file.async-queue-capacity", Integer.class, DEFAULT_CAPACITY), 1, 100_000),
                OverflowPolicy.parse(environment.getProperty(
                        "cpf.logging.file.async-overflow-policy", "CALLER_RUNS")),
                boundedDuration(environment.getProperty(
                        "cpf.logging.file.async-shutdown-timeout-ms", Long.class,
                        DEFAULT_SHUTDOWN_TIMEOUT.toMillis()), Duration.ofMillis(1), Duration.ofMinutes(1)),
                Clock.systemUTC());
    }

    CpfAsyncFileLogWriter(
            CpfFileLogWriter delegate,
            int capacity,
            OverflowPolicy overflowPolicy,
            Duration shutdownTimeout) {
        this(new CpfFileLogAdapter(delegate), capacity, overflowPolicy, shutdownTimeout, Clock.systemUTC());
    }

    CpfAsyncFileLogWriter(
            PreparedLogAdapter adapter,
            int capacity,
            OverflowPolicy overflowPolicy,
            Duration shutdownTimeout) {
        this(adapter, capacity, overflowPolicy, shutdownTimeout, Clock.systemUTC());
    }

    CpfAsyncFileLogWriter(
            PreparedLogAdapter adapter,
            int capacity,
            OverflowPolicy overflowPolicy,
            Duration shutdownTimeout,
            Clock cpfStarterClock) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.overflowPolicy = Objects.requireNonNull(overflowPolicy, "overflowPolicy");
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        this.clock = Objects.requireNonNull(cpfStarterClock, "clock");
        this.worker = new Thread(this::drainLoop, "cpf-file-log-writer");
        this.worker.setDaemon(true);
        this.worker.setUncaughtExceptionHandler((thread, failure) -> {
            failedCount.incrementAndGet();
            terminalLossCount.incrementAndGet();
            lastFailureType.set(failure.getClass().getSimpleName());
        });
        this.worker.start();
    }

    public PublishResult publish(
            TransactionLogRecord record,
            Map<String, String> details,
            LogPolicyDecision policy) {
        if (!accepting.get()) {
            return rejectClosedAdmission();
        }
        try {
            CpfFileLogWriter.PreparedTransactionLog prepared = adapter.prepare(record, details, policy);
            if (prepared.emptyValue()) {
                return PublishResult.IGNORED;
            }
            var admission = admissionLock.readLock();
            admission.lock();
            try {
                // close() and queue admission are serialized. A payload prepared before close
                // but admitted afterwards must never be stranded in a stopped worker queue.
                if (!accepting.get()) {
                    return rejectClosedAdmission();
                }
                if (queue.offer(prepared)) {
                    acceptedCount.incrementAndGet();
                    return PublishResult.QUEUED;
                }
                if (overflowPolicy == OverflowPolicy.CALLER_RUNS) {
                    acceptedCount.incrementAndGet();
                    callerRunsCount.incrementAndGet();
                    return write(prepared) ? PublishResult.CALLER_RAN : PublishResult.FAILED;
                }
                rejectedCount.incrementAndGet();
                terminalLossCount.incrementAndGet();
                return PublishResult.REJECTED;
            } finally {
                admission.unlock();
            }
        } catch (RuntimeException failure) {
            recordFailure(failure);
            return PublishResult.FAILED;
        }
    }

    public AsyncWriterSnapshot snapshot() {
        return new AsyncWriterSnapshot(
                accepting.get(),
                closed.get(),
                queue.size(),
                queue.remainingCapacity() + queue.size(),
                acceptedCount.get(),
                writtenCount.get(),
                callerRunsCount.get(),
                rejectedCount.get(),
                failedCount.get(),
                lastFailureType.get(),
                clock.instant());
    }

    @Override
    public FileWriterSnapshot fileWriterSnapshot() {
        boolean isAccepting = accepting.get();
        boolean isClosed = closed.get();
        int depth = queue.size();
        FileWriterState state;
        if (isClosed) {
            state = depth == 0 ? FileWriterState.CLOSED : FileWriterState.DRAINING;
        } else if (terminalLossCount.get() > 0L || failedCount.get() > 0L || rejectedCount.get() > 0L) {
            state = FileWriterState.DEGRADED;
        } else {
            state = FileWriterState.RUNNING;
        }
        return new FileWriterSnapshot(
                isAccepting,
                worker.isAlive(),
                depth,
                queue.remainingCapacity() + depth,
                acceptedCount.get(),
                writtenCount.get(),
                callerRunsCount.get(),
                rejectedCount.get(),
                failedCount.get(),
                terminalLossCount.get(),
                lastFailureType.get(),
                clock.instant(),
                state);
    }

    private void drainLoop() {
        while (accepting.get() || !queue.isEmpty()) {
            try {
                CpfFileLogWriter.PreparedTransactionLog prepared = queue.poll(250, TimeUnit.MILLISECONDS);
                if (prepared != null) {
                    write(prepared);
                }
            } catch (InterruptedException interrupted) {
                if (accepting.get()) {
                    Thread.currentThread().interrupt();
                    failedCount.incrementAndGet();
                    terminalLossCount.addAndGet(queue.size());
                    lastFailureType.set("InterruptedException");
                    return;
                }
            }
        }
    }

    private boolean write(CpfFileLogWriter.PreparedTransactionLog prepared) {
        try {
            adapter.write(prepared);
            writtenCount.incrementAndGet();
            return true;
        } catch (RuntimeException failure) {
            recordFailure(failure);
            return false;
        }
    }

    private void recordFailure(RuntimeException failure) {
        failedCount.incrementAndGet();
        terminalLossCount.incrementAndGet();
        lastFailureType.set(failure.getClass().getSimpleName());
    }

    private PublishResult rejectClosedAdmission() {
        rejectedCount.incrementAndGet();
        terminalLossCount.incrementAndGet();
        lastFailureType.compareAndSet(null, "WRITER_CLOSED");
        return PublishResult.CLOSED;
    }

    @PreDestroy
    @Override
    public void close() {
        var closeAdmission = admissionLock.writeLock();
        closeAdmission.lock();
        try {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            accepting.set(false);
        } finally {
            closeAdmission.unlock();
        }
        long deadline;
        try {
            deadline = Math.addExact(System.nanoTime(), shutdownTimeout.toNanos());
        } catch (ArithmeticException overflow) {
            deadline = Long.MAX_VALUE;
        }
        while (!queue.isEmpty() && System.nanoTime() < deadline) {
            try {
                worker.join(Math.min(250L, Math.max(1L, shutdownTimeout.toMillis())));
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        worker.interrupt();
        int abandoned = queue.size();
        if (abandoned > 0) {
            rejectedCount.addAndGet(abandoned);
            terminalLossCount.addAndGet(abandoned);
            lastFailureType.set("SHUTDOWN_DRAIN_TIMEOUT");
            queue.clear();
        }
    }

    private static int bounded(int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException("async queue capacity is outside safety bounds");
        }
        return value;
    }

    private static Duration boundedDuration(long millis, Duration min, Duration max) {
        Duration value = Duration.ofMillis(millis);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException("async shutdown timeout is outside safety bounds");
        }
        return value;
    }

    interface PreparedLogAdapter {
        CpfFileLogWriter.PreparedTransactionLog prepare(
                TransactionLogRecord record,
                Map<String, String> details,
                LogPolicyDecision policy);

        void write(CpfFileLogWriter.PreparedTransactionLog prepared);
    }

    private record CpfFileLogAdapter(CpfFileLogWriter writer) implements PreparedLogAdapter {
        private CpfFileLogAdapter {
            Objects.requireNonNull(writer, "writer");
        }

        @Override
        public CpfFileLogWriter.PreparedTransactionLog prepare(
                TransactionLogRecord record,
                Map<String, String> details,
                LogPolicyDecision policy) {
            return writer.prepareTransaction(record, details, policy);
        }

        @Override
        public void write(CpfFileLogWriter.PreparedTransactionLog prepared) {
            if (!writer.writePreparedTransactionWithOutcome(prepared)) {
                throw new FileLogWriteException();
            }
        }
    }

    private static final class FileLogWriteException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private FileLogWriteException() {
            super("file log writer reported a terminal write failure");
        }
    }

    public enum OverflowPolicy {
        CALLER_RUNS,
        REJECT_WITH_LOSS_SIGNAL;

        static OverflowPolicy parse(String value) {
            try {
                return valueOf(value == null ? "CALLER_RUNS" : value.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException invalid) {
                throw new IllegalArgumentException("unsupported async overflow policy", invalid);
            }
        }
    }

    public enum PublishResult {
        QUEUED,
        CALLER_RAN,
        REJECTED,
        CLOSED,
        IGNORED,
        FAILED
    }

    public record AsyncWriterSnapshot(
            boolean accepting,
            boolean closed,
            int queueDepth,
            int queueCapacity,
            long acceptedCount,
            long writtenCount,
            long callerRunsCount,
            long rejectedCount,
            long failedCount,
            String lastFailureType,
            Instant capturedAt) {
        public long inFlightCount() {
            long accounted = writtenCount + failedCount;
            return Math.max(0L, acceptedCount - accounted);
        }

        public boolean lossDetected() {
            return rejectedCount > 0L || failedCount > 0L;
        }
    }
}
