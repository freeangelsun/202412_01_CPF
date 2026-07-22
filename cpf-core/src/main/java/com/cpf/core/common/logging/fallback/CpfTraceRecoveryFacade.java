package cpf.pfw.common.logging.fallback;

import cpf.pfw.api.logging.CpfTraceRecoveryPort;
import org.springframework.stereotype.Component;

/**
 * 거래 요약과 거래 구간 복구 worker를 하나의 안정적인 PFW 공개 포트로 묶습니다.
 */
@Component
public class CpfTraceRecoveryFacade implements CpfTraceRecoveryPort {
    private final TransactionLogRecoveryWorker transactionWorker;
    private final TransactionSegmentRecoveryWorker segmentWorker;

    public CpfTraceRecoveryFacade(
            TransactionLogRecoveryWorker transactionWorker,
            TransactionSegmentRecoveryWorker segmentWorker) {
        this.transactionWorker = transactionWorker;
        this.segmentWorker = segmentWorker;
    }

    @Override
    public TraceRecoveryStatus status() {
        return new TraceRecoveryStatus(
                transactionStatus(transactionWorker.snapshot()),
                segmentStatus(segmentWorker.snapshot()));
    }

    @Override
    public TraceRecoveryRunResult recoverReadyEvents() {
        return new TraceRecoveryRunResult(
                transactionResult(transactionWorker.recoverPending()),
                segmentResult(segmentWorker.recoverPending()));
    }

    @Override
    public PoisonRetryResult retryPoison(RecoveryTarget target, String recoveryEventId) {
        if (recoveryEventId == null || recoveryEventId.isBlank()) {
            throw new IllegalArgumentException("복구 event ID는 필수입니다.");
        }
        boolean accepted = switch (target) {
            case TRANSACTION -> transactionWorker.retryPoison(recoveryEventId.trim());
            case SEGMENT -> segmentWorker.retryPoison(recoveryEventId.trim());
        };
        return new PoisonRetryResult(target, recoveryEventId.trim(), accepted);
    }

    private WorkerStatus transactionStatus(TransactionLogRecoveryWorker.WorkerSnapshot source) {
        return new WorkerStatus(
                source.running(),
                source.recoveredCount(),
                source.failedAttemptCount(),
                transactionSpool(source.fallback()));
    }

    private WorkerStatus segmentStatus(TransactionSegmentRecoveryWorker.WorkerSnapshot source) {
        return new WorkerStatus(
                source.running(),
                source.recoveredCount(),
                source.failedAttemptCount(),
                segmentSpool(source.fallback()));
    }

    private WorkerRunResult transactionResult(TransactionLogRecoveryWorker.RecoveryResult source) {
        return new WorkerRunResult(
                source.claimedCount(),
                source.recoveredCount(),
                source.failedCount(),
                source.alreadyRunning(),
                transactionSpool(source.fallback()));
    }

    private WorkerRunResult segmentResult(TransactionSegmentRecoveryWorker.RecoveryResult source) {
        return new WorkerRunResult(
                source.claimedCount(),
                source.recoveredCount(),
                source.failedCount(),
                source.alreadyRunning(),
                segmentSpool(source.fallback()));
    }

    private SpoolStatus transactionSpool(TransactionLogFallbackStore.FallbackSnapshot source) {
        return new SpoolStatus(
                source.health(),
                source.pendingCount(),
                source.processingCount(),
                source.poisonCount(),
                source.spoolBytes(),
                source.maxSpoolBytes(),
                source.enqueueFailureCount(),
                source.staleReclaimedCount(),
                source.malformedPoisonCount(),
                source.poisonRetryCount(),
                source.spoolDirectory());
    }

    private SpoolStatus segmentSpool(TransactionSegmentFallbackStore.SegmentFallbackSnapshot source) {
        return new SpoolStatus(
                source.health(),
                source.pendingCount(),
                source.processingCount(),
                source.poisonCount(),
                source.spoolBytes(),
                source.maxSpoolBytes(),
                source.enqueueFailureCount(),
                source.staleReclaimedCount(),
                source.malformedPoisonCount(),
                source.poisonRetryCount(),
                source.spoolDirectory());
    }
}
