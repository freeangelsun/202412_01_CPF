package com.cpf.platform.operations.observability.internal.logging.fallback;

import com.cpf.platform.operations.observability.api.logging.CpfLogRecoveryOperations;
import com.cpf.platform.operations.observability.api.logging.CpfLogRecoveryRuntimeStatus;
import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateOperations;
import com.cpf.platform.operations.api.state.CpfStateQueryResult;
import com.cpf.platform.operations.api.state.CpfStateTransitionRequest;
import com.cpf.platform.operations.api.state.CpfStateTransitionResult;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.platform.operations.observability.internal.logging.TransactionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.ObjectProvider;
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
 * durable journal의 CPF 거래 로그를 DB 복구 후 다시 적재합니다.
 *
 * <p>한 인스턴스 안에서 worker가 중복 실행되지 않도록 실행 잠금을 사용합니다.
 * 지수 backoff 횟수를 초과한 레코드는 poison 디렉터리로 격리하며, 재적재가
 * 성공하거나 이미 적재된 복구 ID임이 확인되면 processing 파일을 삭제합니다.</p>
 */
@Component
public final class TransactionLogRecoveryWorker implements CpfLogRecoveryOperations, CpfLogRecoveryRuntimeStatus {
    private static final Logger log = LoggerFactory.getLogger(TransactionLogRecoveryWorker.class);

    private final TransactionLogFallbackStore store;
    private final TransactionLogService logService;
    private final RecoveryEventSink recoveryEventSink;
    private final Clock clock;
    private final CpfStateOperations stateOperations;
    private final int batchSize;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;
    private final Duration processingLeaseTimeout;
    private final boolean scheduledRecoveryEnabled;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicLong recoveredCount = new AtomicLong();
    private final AtomicLong failedAttemptCount = new AtomicLong();
    private final AtomicLong operationalEventFailureCount = new AtomicLong();
    private final AtomicLong unknownResultCount = new AtomicLong();

    public TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment) {
        this(store, logService, fileLogWriter, environment, Clock.systemUTC(), null);
    }

    @Autowired
    public TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            ObjectProvider<CpfStateOperations> stateOperationsProvider) {
        this(store, logService, fileLogWriter, environment, Clock.systemUTC(),
                stateOperationsProvider == null ? null : stateOperationsProvider.getIfAvailable());
    }

    TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock cpfStarterClock) {
        this(store, logService, fileLogWriter, environment, cpfStarterClock, null);
    }

    TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            CpfFileLogWriter fileLogWriter,
            Environment environment,
            Clock cpfStarterClock,
            CpfStateOperations stateOperations) {
        this(store, logService, environment, cpfStarterClock, fileEventSink(fileLogWriter), stateOperations);
    }

    TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            Environment environment,
            Clock cpfStarterClock,
            RecoveryEventSink recoveryEventSink) {
        this(store, logService, environment, cpfStarterClock, recoveryEventSink, null);
    }

    TransactionLogRecoveryWorker(
            TransactionLogFallbackStore store,
            TransactionLogService logService,
            Environment environment,
            Clock cpfStarterClock,
            RecoveryEventSink recoveryEventSink,
            CpfStateOperations stateOperations) {
        this.store = store;
        this.logService = logService;
        this.recoveryEventSink = recoveryEventSink;
        this.clock = cpfStarterClock;
        this.stateOperations = stateOperations;
        this.scheduledRecoveryEnabled = environment.getProperty(
                "cpf.logging.db-fallback.enabled", Boolean.class, true);
        this.batchSize = bounded(environment.getProperty(
                "cpf.logging.db-fallback.recovery-batch-size", Integer.class, 50), 1, 500);
        this.maxAttempts = bounded(environment.getProperty(
                "cpf.logging.db-fallback.max-attempts", Integer.class, 10), 1, 100);
        this.initialBackoffMs = boundedPositive(environment.getProperty(
                "cpf.logging.db-fallback.initial-backoff-ms", Long.class, 1_000L),
                1_000L, Duration.ofHours(1).toMillis());
        this.maxBackoffMs = boundedPositive(environment.getProperty(
                "cpf.logging.db-fallback.max-backoff-ms", Long.class, 300_000L),
                300_000L, Duration.ofHours(24).toMillis());
        if (maxBackoffMs < initialBackoffMs) {
            throw new IllegalArgumentException("max backoff must be >= initial backoff");
        }
        this.processingLeaseTimeout = Duration.ofMillis(boundedPositive(environment.getProperty(
                "cpf.logging.db-fallback.processing-lease-ms", Long.class, 120_000L),
                120_000L, Duration.ofHours(24).toMillis()));
    }

    @Scheduled(fixedDelayString = "${cpf.logging.db-fallback.recovery-interval-ms:30000}")
    public void recoverScheduled() {
        if (!scheduledRecoveryEnabled) {
            return;
        }
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
            store.reclaimStaleProcessing(clock.instant(), processingLeaseTimeout);
            for (Path pending : store.eligiblePendingFiles(clock.instant(), batchSize)) {
                TransactionLogFallbackEnvelope envelope = null;
                try {
                    envelope = store.claim(pending);
                    claimed++;
                    ensureRecoveryStateStarted(envelope);
                    logService.saveTransactionLog(
                            envelope.record(),
                            new LinkedHashMap<>(envelope.details()),
                            envelope.logPolicy());
                    ensureRecoveryStateTerminal(envelope, CpfOperationState.SUCCEEDED,
                            "transaction log recovery persisted");
                    if (!store.complete(envelope)) {
                        throw new IllegalStateException("recovery claim was lost after persistence");
                    }
                    recovered++;
                    recoveredCount.incrementAndGet();
                } catch (Exception ex) {
                    failed++;
                    failedAttemptCount.incrementAndGet();
                    handleFailure(envelope, ex);
                }
            }
            if (recovered > 0 || failed > 0) {
                writeRecoveryEventSafely(claimed, recovered, failed);
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
                operationalEventFailureCount.get(),
                unknownResultCount.get(),
                store.snapshot());
    }

    @Override
    public RecoveryRuntimeSnapshot recoveryRuntimeSnapshot() {
        TransactionLogFallbackStore.FallbackSnapshot fallback = store.snapshot();
        RecoveryState state;
        if ("DOWN".equals(fallback.health())) {
            state = RecoveryState.DOWN;
        } else if ("DEGRADED".equals(fallback.health()) || failedAttemptCount.get() > 0L
                || operationalEventFailureCount.get() > 0L || unknownResultCount.get() > 0L) {
            state = RecoveryState.DEGRADED;
        } else {
            state = running.get() ? RecoveryState.RUNNING : RecoveryState.IDLE;
        }
        return new RecoveryRuntimeSnapshot(
                scheduledRecoveryEnabled,
                running.get(),
                batchSize,
                maxAttempts,
                recoveredCount.get(),
                failedAttemptCount.get(),
                fallback.pendingCount(),
                fallback.processingCount(),
                fallback.poisonCount(),
                fallback.spoolBytes(),
                fallback.maxSpoolBytes(),
                fallback.enqueueFailureCount(),
                fallback.staleReclaimedCount(),
                fallback.malformedPoisonCount(),
                fallback.poisonRetryCount(),
                fallback.staleClaimConflictCount(),
                fallback.spoolDirectory(),
                clock.instant(),
                state);
    }

    @Override
    public RecoveryOperationalDiagnostics recoveryOperationalDiagnostics() {
        return new RecoveryOperationalDiagnostics(
                operationalEventFailureCount.get(), unknownResultCount.get(), clock.instant());
    }

    @Override
    public PoisonRetryResult retryPoison(PoisonRetryCommand command, PoisonRetryApproval approval) {
        Instant decidedAt = clock.instant();
        if (command == null || approval == null) {
            String recoveryId = command == null
                    ? "0000000000000000000000000000000000000000000000000000000000000000"
                    : command.recoveryEventId();
            return result(PoisonRetryStatus.APPROVAL_REQUIRED, recoveryId, approval, decidedAt,
                    "CPF_LOG_RECOVERY_APPROVAL_REQUIRED");
        }
        if (command.requesterId().equals(approval.approverId())) {
            return result(PoisonRetryStatus.SEPARATION_OF_DUTIES, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_SOD_VIOLATION");
        }
        if (!approval.activeAt(decidedAt)) {
            return result(PoisonRetryStatus.APPROVAL_EXPIRED, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_APPROVAL_EXPIRED");
        }
        if (!constantTimeEquals(command.commandHash(), approval.commandHash())) {
            return result(PoisonRetryStatus.APPROVAL_SCOPE_MISMATCH, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_APPROVAL_SCOPE_MISMATCH");
        }

        if (!writePoisonRetryAuditSafely("AUTHORIZED", command, approval, null, decidedAt)) {
            return result(PoisonRetryStatus.FAILED, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_AUDIT_UNAVAILABLE");
        }

        TransactionLogFallbackStore.PoisonRetryStoreResult storeResult;
        try {
            storeResult = store.retryPoison(command.recoveryEventId(), command.expectedAttemptCount());
        } catch (IOException | RuntimeException failure) {
            PoisonRetryResult result = result(PoisonRetryStatus.FAILED, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_RETRY_FAILED");
            writePoisonRetryAuditSafely("FAILED", command, approval, result.errorCode(), decidedAt);
            log.error("승인된 poison 거래 로그 재시도에 실패했습니다. recoveryEventId={}",
                    command.recoveryEventId());
            return result;
        }

        PoisonRetryStatus status = switch (storeResult) {
            case RETRIED -> PoisonRetryStatus.RETRIED;
            case NOT_FOUND -> PoisonRetryStatus.NOT_FOUND;
            case STALE_ATTEMPT -> PoisonRetryStatus.STALE_ATTEMPT;
        };
        PoisonRetryResult result = result(status, command.recoveryEventId(), approval, decidedAt,
                status == PoisonRetryStatus.RETRIED ? null : "CPF_LOG_RECOVERY_" + status.name());
        if (writePoisonRetryAuditSafely(status.name(), command, approval, result.errorCode(), decidedAt)) {
            return result;
        }
        if (status == PoisonRetryStatus.RETRIED) {
            unknownResultCount.incrementAndGet();
            return result(PoisonRetryStatus.UNKNOWN_RESULT, command.recoveryEventId(), approval, decidedAt,
                    "CPF_LOG_RECOVERY_AUDIT_UNKNOWN_RESULT");
        }
        return result(PoisonRetryStatus.FAILED, command.recoveryEventId(), approval, decidedAt,
                "CPF_LOG_RECOVERY_AUDIT_UNAVAILABLE");
    }

    /**
     * 승인 없는 기존 호출은 fail-closed 처리합니다.
     *
     * 호환 안내: 승인 객체와 명령 범위를 포함한 {@link #retryPoison(PoisonRetryCommand, PoisonRetryApproval)}를 사용하십시오.
     */
    public boolean retryPoison(String recoveryEventId) {
        return false;
    }

    private PoisonRetryResult result(
            PoisonRetryStatus status,
            String recoveryEventId,
            PoisonRetryApproval approval,
            Instant decidedAt,
            String errorCode) {
        return new PoisonRetryResult(
                status,
                recoveryEventId,
                approval == null ? null : approval.approvalId(),
                decidedAt,
                errorCode);
    }

    private boolean writePoisonRetryAuditSafely(
            String result,
            PoisonRetryCommand command,
            PoisonRetryApproval approval,
            String errorCode,
            Instant decidedAt) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", "TRANSACTION_DB_POISON_RETRY");
        event.put("result", result);
        event.put("recoveryEventId", command.recoveryEventId());
        event.put("expectedAttemptCount", command.expectedAttemptCount());
        event.put("requesterRef", hashIdentifier(command.requesterId()));
        event.put("approverRef", hashIdentifier(approval.approverId()));
        event.put("approvalRef", hashIdentifier(approval.approvalId()));
        event.put("reason", com.cpf.security.api.CpfSensitiveData.sanitizeAuditReason(command.reason()));
        event.put("decidedAt", decidedAt.toString());
        if (errorCode != null) event.put("errorCode", errorCode);
        return writeOperationalEventSafely("audit", event);
    }

    private static String hashIdentifier(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "sha256:" + java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                right.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private void handleFailure(TransactionLogFallbackEnvelope envelope, Exception failure) {
        if (envelope == null) {
            log.error("DB 거래 로그 복구 journal을 읽거나 claim하는 데 실패했습니다.", failure);
            return;
        }
        int attempt = envelope.attemptCount() + 1;
        long delay = retryDelayMs(initialBackoffMs, maxBackoffMs, attempt);
        TransactionLogFallbackEnvelope failedEnvelope = envelope.nextAttempt(
                attempt,
                safePlus(clock.instant(), Duration.ofMillis(delay)),
                failure.getClass().getSimpleName());
        try {
            if (attempt >= maxAttempts) {
                store.poison(failedEnvelope);
                updateRecoveryStateAfterFailure(envelope, CpfOperationState.FAILED,
                        "transaction log recovery moved to poison");
                log.error("DB 거래 로그 복구 레코드를 poison으로 격리했습니다. recoveryEventId={}, attempts={}",
                        envelope.recoveryEventId(), attempt);
            } else {
                store.retry(failedEnvelope);
                updateRecoveryStateAfterFailure(envelope, CpfOperationState.UNKNOWN,
                        "transaction log recovery scheduled for reconcile");
                log.warn("DB 거래 로그 복구 재시도를 예약했습니다. recoveryEventId={}, attempts={}, delayMs={}",
                        envelope.recoveryEventId(), attempt, delay);
            }
        } catch (IOException ioException) {
            log.error("DB 거래 로그 복구 journal 상태 변경에 실패했습니다. recoveryEventId={}",
                    envelope.recoveryEventId(), ioException);
        }
    }

    private void ensureRecoveryStateStarted(TransactionLogFallbackEnvelope envelope) {
        if (stateOperations == null) {
            return;
        }
        String stateKey = recoveryStateKey(envelope);
        String operationId = "recover-start:" + Math.max(0, envelope.attemptCount());
        CpfStateTransitionResult result = stateOperations.start(
                stateKey, operationId, "cpf-log-recovery-worker",
                "transaction log recovery started");
        if (result.applied()) {
            return;
        }
        CpfStateQueryResult current = stateOperations.query(stateKey);
        if (current.status() == CpfStateQueryResult.Status.FOUND
                && (current.snapshot().state() == CpfOperationState.RUNNING
                    || current.snapshot().state() == CpfOperationState.UNKNOWN
                    || current.snapshot().state() == CpfOperationState.SUCCEEDED)) {
            return;
        }
        throw new IllegalStateException("recovery state start unavailable: " + result.status());
    }

    private void ensureRecoveryStateTerminal(
            TransactionLogFallbackEnvelope envelope,
            CpfOperationState targetState,
            String reason) {
        if (stateOperations == null) {
            return;
        }
        String stateKey = recoveryStateKey(envelope);
        CpfStateQueryResult current = stateOperations.query(stateKey);
        if (current.status() != CpfStateQueryResult.Status.FOUND) {
            unknownResultCount.incrementAndGet();
            throw new IllegalStateException("recovery state query unavailable");
        }
        if (current.snapshot().state() == targetState) {
            return;
        }
        CpfStateTransitionResult result = stateOperations.transition(new CpfStateTransitionRequest(
                stateKey,
                current.snapshot().version(),
                targetState,
                "recover-state:" + targetState.name().toLowerCase(java.util.Locale.ROOT)
                        + ":" + Math.max(0, envelope.attemptCount()),
                "cpf-log-recovery-worker",
                reason));
        if (!result.applied()) {
            unknownResultCount.incrementAndGet();
            throw new IllegalStateException("recovery state transition unavailable: " + result.status());
        }
    }

    private void updateRecoveryStateAfterFailure(
            TransactionLogFallbackEnvelope envelope,
            CpfOperationState targetState,
            String reason) {
        if (stateOperations == null) {
            return;
        }
        try {
            ensureRecoveryStateTerminal(envelope, targetState, reason);
        } catch (RuntimeException stateFailure) {
            operationalEventFailureCount.incrementAndGet();
            log.error("거래 로그 복구 상태 기록에 실패했습니다. recoveryEventId={}, targetState={}, failureType={}",
                    envelope.recoveryEventId(), targetState, stateFailure.getClass().getSimpleName());
        }
    }

    private static String recoveryStateKey(TransactionLogFallbackEnvelope envelope) {
        return "log-recovery:" + envelope.recoveryEventId();
    }

    private void writeRecoveryEventSafely(int claimed, int recovered, int failed) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", "TRANSACTION_DB_RECOVERY_RUN");
        event.put("claimedCount", claimed);
        event.put("recoveredCount", recovered);
        event.put("failedCount", failed);
        try {
            event.put("fallbackHealth", store.snapshot().health());
        } catch (RuntimeException unavailable) {
            event.put("fallbackHealth", "UNKNOWN");
        }
        writeOperationalEventSafely("recovery", event);
    }

    private boolean writeOperationalEventSafely(String logType, Map<String, Object> event) {
        try {
            recoveryEventSink.write(logType, Map.copyOf(event));
            return true;
        } catch (RuntimeException failure) {
            operationalEventFailureCount.incrementAndGet();
            log.error("거래 로그 복구 운영 이벤트 기록에 실패했습니다. logType={}, failureType={}",
                    logType, failure.getClass().getSimpleName());
            return false;
        }
    }

    private static RecoveryEventSink fileEventSink(CpfFileLogWriter fileLogWriter) {
        if (fileLogWriter == null) throw new IllegalArgumentException("fileLogWriter is required");
        return (logType, attributes) -> {
            Map<String, Object> event = fileLogWriter.newBaseEvent("CPF", logType);
            event.putAll(attributes);
            if (!fileLogWriter.writeEventWithOutcome("CPF", logType, event)) {
                throw new IllegalStateException("file recovery event write failed");
            }
        };
    }

    private int bounded(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    static long retryDelayMs(long initialBackoffMs, long maxBackoffMs, int attempt) {
        if (initialBackoffMs <= 0L || maxBackoffMs < initialBackoffMs || attempt < 1) {
            throw new IllegalArgumentException("valid bounded retry arguments are required");
        }
        long multiplier = 1L << Math.min(attempt - 1, 30);
        long exponential;
        try {
            exponential = Math.multiplyExact(initialBackoffMs, multiplier);
        } catch (ArithmeticException overflow) {
            exponential = Long.MAX_VALUE;
        }
        return Math.min(maxBackoffMs, exponential);
    }

    private static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MAX;
        }
    }

    private long boundedPositive(long value, long fallback, long maximum) {
        long effective = value > 0L ? value : fallback;
        if (effective > maximum) {
            throw new IllegalArgumentException("configured duration exceeds safety bound");
        }
        return effective;
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
            long operationalEventFailureCount,
            long unknownResultCount,
            TransactionLogFallbackStore.FallbackSnapshot fallback) {
    }

    @FunctionalInterface
    interface RecoveryEventSink {
        void write(String logType, Map<String, Object> event);
    }
}
