package cpf.pfw.common.logging.fallback;

import cpf.pfw.common.logging.file.CpfFileLogWriter;
import cpf.pfw.service.common.logging.TransactionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * durable journal의 PFW 거래 로그를 DB 복구 후 다시 적재합니다.
 *
 * <p>한 인스턴스 안에서 worker가 중복 실행되지 않도록 실행 잠금을 사용합니다.
 * 지수 backoff 횟수를 초과한 레코드는 poison 디렉터리로 격리하며, 재적재가
 * 성공하거나 이미 적재된 복구 ID임이 확인되면 processing 파일을 삭제합니다.</p>
 */
@Component
public class TransactionLogRecoveryWorker {
    private static final Logger log = LoggerFactory.getLogger(TransactionLogRecoveryWorker.class);

    private final TransactionLogFallbackStore store;
    private final TransactionLogService logService;
    private final CpfFileLogWriter fileLogWriter;
    private final Clock clock;
    private final int batchSize;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicLong recoveredCount = new AtomicLong();
    private final AtomicLong failedAttemptCount = new AtomicLong();

    @Autowired
    public TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        this(store, logService, fileLogWriter, environment, Clock.systemUTC());
    }

    TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock clock) {
        this.store = store;
        this.logService = logService;
        this.fileLogWriter = fileLogWriter;
        this.clock = clock;
        this.batchSize = bounded(environment.getProperty(
                "cpf.logging.db-fallback.recovery-batch-size", Integer.class, 50), 1, 500);
        this.maxAttempts = bounded(environment.getProperty(
                "cpf.logging.db-fallback.max-attempts", Integer.class, 10), 1, 100);
        this.initialBackoffMs = positive(environment.getProperty(
                "cpf.logging.db-fallback.initial-backoff-ms", Long.class, 1_000L), 1_000L);
        this.maxBackoffMs = positive(environment.getProperty(
                "cpf.logging.db-fallback.max-backoff-ms", Long.class, 300_000L), 300_000L);
    }

    @Scheduled(fixedDelayString = "${cpf.logging.db-fallback.recovery-interval-ms:30000}")
    public void recoverScheduled() {
        recoverPending();
    }

    /**
     * 현재 재시도 시각이 지난 pending 레코드를 최대 batch 크기만큼 복구합니다.
     */
    public RecoveryResult recoverPending() {
        if (!running.compareAndSet(false, true)) {
            return new RecoveryResult(0, 0, 0, true, store.snapshot());
        }
        int claimed = 0;
        int recovered = 0;
        int failed = 0;
        try {
            for (Path pending : store.pendingFiles()) {
                if (claimed >= batchSize) {
                    break;
                }
                claimed++;
                TransactionLogFallbackEnvelope envelope = null;
                try {
                    envelope = store.claim(pending);
                    Instant now = clock.instant();
                    if (envelope.nextAttemptAt() != null && envelope.nextAttemptAt().isAfter(now)) {
                        store.retry(envelope);
                        continue;
                    }
                    logService.saveTransactionLog(
                            envelope.record(),
                            new LinkedHashMap<>(envelope.details()),
                            envelope.logPolicy());
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
        return new WorkerSnapshot(
                running.get(),
                recoveredCount.get(),
                failedAttemptCount.get(),
                store.snapshot());
    }

    private void handleFailure(TransactionLogFallbackEnvelope envelope, Exception failure) {
        if (envelope == null) {
            log.error("DB 거래 로그 복구 journal을 읽거나 claim하는 데 실패했습니다.", failure);
            return;
        }
        int attempt = envelope.attemptCount() + 1;
        long delay = Math.min(maxBackoffMs, initialBackoffMs * (1L << Math.min(attempt - 1, 20)));
        TransactionLogFallbackEnvelope failedEnvelope = envelope.nextAttempt(
                attempt,
                clock.instant().plus(Duration.ofMillis(delay)),
                failure.getClass().getSimpleName());
        try {
            if (attempt >= maxAttempts) {
                store.poison(failedEnvelope);
                log.error("DB 거래 로그 복구 레코드를 poison으로 격리했습니다. recoveryEventId={}, attempts={}",
                        envelope.recoveryEventId(), attempt);
            } else {
                store.retry(failedEnvelope);
                log.warn("DB 거래 로그 복구 재시도를 예약했습니다. recoveryEventId={}, attempts={}, delayMs={}",
                        envelope.recoveryEventId(), attempt, delay);
            }
        } catch (IOException ioException) {
            log.error("DB 거래 로그 복구 journal 상태 변경에 실패했습니다. recoveryEventId={}",
                    envelope.recoveryEventId(), ioException);
        }
    }

    private void writeRecoveryEvent(int claimed, int recovered, int failed) {
        Map<String, Object> event = fileLogWriter.newBaseEvent("PFW", "recovery");
        event.put("eventType", "TRANSACTION_DB_RECOVERY_RUN");
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
            TransactionLogFallbackStore.FallbackSnapshot fallback) {
    }

    public record WorkerSnapshot(
            boolean running,
            long recoveredCount,
            long failedAttemptCount,
            TransactionLogFallbackStore.FallbackSnapshot fallback) {
    }
}
