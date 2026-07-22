package com.cpf.core.api.logging;

/**
 * 운영 모듈이 CPF 내부 worker나 저장소를 직접 참조하지 않고 추적 로그를 복구하는 공개 포트입니다.
 */
public interface CpfTraceRecoveryPort {

    TraceRecoveryStatus status();

    TraceRecoveryRunResult recoverReadyEvents();

    PoisonRetryResult retryPoison(RecoveryTarget target, String recoveryEventId);

    enum RecoveryTarget {
        TRANSACTION,
        SEGMENT
    }

    record SpoolStatus(
            String health,
            int pendingCount,
            int processingCount,
            int poisonCount,
            long spoolBytes,
            long maxSpoolBytes,
            long enqueueFailureCount,
            long staleReclaimedCount,
            long malformedPoisonCount,
            long poisonRetryCount,
            String spoolDirectory) {
    }

    record WorkerStatus(
            boolean running,
            long recoveredCount,
            long failedAttemptCount,
            SpoolStatus spool) {
    }

    record TraceRecoveryStatus(WorkerStatus transaction, WorkerStatus segment) {
    }

    record WorkerRunResult(
            int claimedCount,
            int recoveredCount,
            int failedCount,
            boolean alreadyRunning,
            SpoolStatus spool) {
    }

    record TraceRecoveryRunResult(WorkerRunResult transaction, WorkerRunResult segment) {
    }

    record PoisonRetryResult(RecoveryTarget target, String recoveryEventId, boolean accepted) {
    }
}
