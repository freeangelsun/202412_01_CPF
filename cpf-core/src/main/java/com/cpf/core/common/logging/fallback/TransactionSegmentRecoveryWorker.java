package cpf.pfw.common.logging.fallback;

import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.common.logging.segment.TransactionSegmentPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 거래 구간 START·END durable journal을 순서대로 DB에 재적재합니다.
 */
@Component
public class TransactionSegmentRecoveryWorker {
    private static final Logger log = LoggerFactory.getLogger(TransactionSegmentRecoveryWorker.class);

    private final TransactionSegmentFallbackStore store;
    private final TransactionSegmentPersistenceService persistenceService;
    private final CpfFileLogWriter fileLogWriter;
    private final Clock clock;
    private final int batchSize;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;
    private final Duration processingLeaseTimeout;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicLong recoveredCount = new AtomicLong();
    private final AtomicLong failedAttemptCount = new AtomicLong();

    @Autowired
    public TransactionSegmentRecoveryWorker(
            TransactionSegmentFallbackStore store,
            TransactionSegmentPersistenceService persistenceService,
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        this(store, persistenceService, fileLogWriter, environment, Clock.systemUTC());
    }

    TransactionSegmentRecoveryWorker(
            TransactionSegmentFallbackStore store,
            TransactionSegmentPersistenceService persistenceService,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock clock) {
        this.store = store;
        this.persistenceService = persistenceService;
        this.fileLogWriter = fileLogWriter;
        this.clock = clock;
        this.batchSize = bounded(environment.getProperty(
                "cpf.logging.segment-fallback.recovery-batch-size", Integer.class, 100), 1, 1000);
        this.maxAttempts = bounded(environment.getProperty(
                "cpf.logging.segment-fallback.max-attempts", Integer.class, 10), 1, 100);
        this.initialBackoffMs = positive(environment.getProperty(
                "cpf.logging.segment-fallback.initial-backoff-ms", Long.class, 1_000L), 1_000L);
        this.maxBackoffMs = positive(environment.getProperty(
                "cpf.logging.segment-fallback.max-backoff-ms", Long.class, 300_000L), 300_000L);
        this.processingLeaseTimeout = Duration.ofMillis(positive(environment.getProperty(
                "cpf.logging.segment-fallback.processing-lease-ms", Long.class, 120_000L), 120_000L));
    }

    @Scheduled(fixedDelayString = "${cpf.logging.segment-fallback.recovery-interval-ms:30000}")
    public void recoverScheduled() {
        recoverPending();
    }

    public RecoveryResult recoverPending() {
        if (!running.compareAndSet(false, true)) {
            return new RecoveryResult(0, 0, 0, true, store.snapshot());
        }
        int claimed = 0;
        int recovered = 0;
        int failed = 0;
        try {
            store.reclaimStaleProcessing(clock.instant(), processingLeaseTimeout);
            for (Path pending : store.eligiblePendingFiles(clock.instant(), batchSize)) {
                TransactionSegmentRecoveryEnvelope envelope = null;
                try {
                    envelope = store.claim(pending);
                    claimed++;
                    persist(envelope);
                    store.complete(envelope.recoveryEventId());
                    recovered++;
                    recoveredCount.incrementAndGet();
                } catch (Exception ex) {
                    failed++;
                    failedAttemptCount.incrementAndGet();
                    handleFailure(envelope, ex);
                }
            }
            if (recovered > 0 || failed > 0) {
                writeRecoveryEvent(claimed, recovered, failed);
            }
            return new RecoveryResult(claimed, recovered, failed, false, store.snapshot());
        } finally {
            running.set(false);
        }
    }

    public WorkerSnapshot snapshot() {
        return new WorkerSnapshot(running.get(), recoveredCount.get(), failedAttemptCount.get(), store.snapshot());
    }

    public boolean retryPoison(String recoveryEventId) {
        try {
            return store.retryPoison(recoveryEventId);
        } catch (IOException ex) {
            throw new IllegalStateException("poison 거래 구간을 재시도 대기열로 이동할 수 없습니다.", ex);
        }
    }

    private void persist(TransactionSegmentRecoveryEnvelope envelope) {
        if ("START".equals(envelope.eventType())) {
            persistenceService.insertRecovered(envelope.record());
            return;
        }
        if ("END".equals(envelope.eventType())) {
            persistenceService.updateEndRecovered(envelope.record());
            return;
        }
        throw new IllegalArgumentException("지원하지 않는 거래 구간 복구 이벤트입니다: " + envelope.eventType());
    }

    private void handleFailure(TransactionSegmentRecoveryEnvelope envelope, Exception failure) {
        if (envelope == null) {
            log.error("거래 구간 복구 journal claim 또는 역직렬화에 실패했습니다.", failure);
            return;
        }
        int attempts = envelope.attemptCount() + 1;
        long delay = Math.min(maxBackoffMs, initialBackoffMs * (1L << Math.min(attempts - 1, 20)));
        TransactionSegmentRecoveryEnvelope failed = envelope.retry(
                attempts,
                clock.instant().plusMillis(delay),
                failure.getClass().getSimpleName());
        try {
            if (attempts >= maxAttempts) {
                store.poison(failed);
                log.error("거래 구간 복구 이벤트를 poison으로 격리했습니다. eventId={}, attempts={}",
                        envelope.recoveryEventId(), attempts);
            } else {
                store.retry(failed);
                log.warn("거래 구간 복구 재시도를 예약했습니다. eventId={}, attempts={}, delayMs={}",
                        envelope.recoveryEventId(), attempts, delay);
            }
        } catch (IOException ioException) {
            log.error("거래 구간 복구 journal 상태 변경에 실패했습니다. eventId={}",
                    envelope.recoveryEventId(), ioException);
        }
    }

    private void writeRecoveryEvent(int claimed, int recovered, int failed) {
        Map<String, Object> event = fileLogWriter.newBaseEvent("PFW", "recovery");
        event.put("eventType", "TRANSACTION_SEGMENT_DB_RECOVERY_RUN");
        event.put("claimedCount", claimed);
        event.put("recoveredCount", recovered);
        event.put("failedCount", failed);
        event.put("fallbackHealth", store.snapshot().health());
        fileLogWriter.writeEvent("PFW", "recovery", event);
    }

    private int bounded(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private long positive(long value, long fallback) {
        return value > 0 ? value : fallback;
    }

    public record RecoveryResult(
            int claimedCount,
            int recoveredCount,
            int failedCount,
            boolean alreadyRunning,
            TransactionSegmentFallbackStore.SegmentFallbackSnapshot fallback) {
    }

    public record WorkerSnapshot(
            boolean running,
            long recoveredCount,
            long failedAttemptCount,
            TransactionSegmentFallbackStore.SegmentFallbackSnapshot fallback) {
    }
}
