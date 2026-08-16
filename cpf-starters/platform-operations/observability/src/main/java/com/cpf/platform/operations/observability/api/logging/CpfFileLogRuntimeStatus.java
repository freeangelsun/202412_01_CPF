package com.cpf.platform.operations.observability.api.logging;

import java.time.Instant;
import java.util.Objects;

/** Runtime visibility for file retention, compression and process-lock safety. */
/** CpfFileLogRuntimeStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfFileLogRuntimeStatus {
    FileLogRuntimeSnapshot fileLogRuntimeSnapshot();

    /** Direct write failures are separated from retention failures. */
    default FileWriteDiagnostics fileWriteDiagnostics() {
        return new FileWriteDiagnostics(0L, null, Instant.EPOCH);
    }


    /** Durable recovery spool visibility; terminalLoss > 0 requires operator attention. */
    default FileRecoveryDiagnostics fileRecoveryDiagnostics() {
        return new FileRecoveryDiagnostics(0L,0L,0L,0L,0L,0L,Instant.EPOCH);
    }

    /** FileRecoveryDiagnostics 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record FileRecoveryDiagnostics(long pending, long enqueued, long replayed, long deduplicated,
                                   long quarantined, long terminalLoss, Instant capturedAt) {
        public FileRecoveryDiagnostics {
            if (pending < 0 || enqueued < 0 || replayed < 0 || deduplicated < 0 || quarantined < 0 || terminalLoss < 0)
                throw new IllegalArgumentException("file recovery counters must be non-negative");
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
        }
    }

    /** FileWriteDiagnostics 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record FileWriteDiagnostics(long writeFailureCount, String lastFailureType, Instant capturedAt) {
        public FileWriteDiagnostics {
            if (writeFailureCount < 0L) throw new IllegalArgumentException("writeFailureCount must be non-negative");
            lastFailureType = lastFailureType == null ? null : lastFailureType.trim();
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
        }
    }

    enum RetentionState { NEVER_RUN, HEALTHY, DEGRADED, DOWN }

    /** FileLogRuntimeSnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record FileLogRuntimeSnapshot(
            long retentionRunCount,
            long retentionSkipCount,
            long compressedFileCount,
            long deletedFileCount,
            long retentionFailureCount,
            long processLockTimeoutCount,
            Instant lastRetentionStartedAt,
            Instant lastRetentionCompletedAt,
            String lastFailureType,
            RetentionState state) {
        public FileLogRuntimeSnapshot {
            if (retentionRunCount < 0 || retentionSkipCount < 0 || compressedFileCount < 0
                    || deletedFileCount < 0 || retentionFailureCount < 0 || processLockTimeoutCount < 0) {
                throw new IllegalArgumentException("file log runtime counters must be non-negative");
            }
            state = Objects.requireNonNull(state, "state");
        }

        /** health 작업을 CPF 표준 계약에 따라 수행한다. */
        public String health() {
            return switch (state) {
                case DOWN -> "DOWN";
                case DEGRADED -> "DEGRADED";
                case NEVER_RUN -> "UNKNOWN";
                case HEALTHY -> "UP";
            };
        }
    }
}
