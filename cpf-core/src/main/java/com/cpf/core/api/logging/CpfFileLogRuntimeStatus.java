package com.cpf.core.api.logging;

import java.time.Instant;
import java.util.Objects;

/** Runtime visibility for file retention, compression and process-lock safety. */
public interface CpfFileLogRuntimeStatus {
    FileLogRuntimeSnapshot fileLogRuntimeSnapshot();

    /** Direct write failures are separated from retention failures. */
    default FileWriteDiagnostics fileWriteDiagnostics() {
        return new FileWriteDiagnostics(0L, null, Instant.EPOCH);
    }

    record FileWriteDiagnostics(long writeFailureCount, String lastFailureType, Instant capturedAt) {
        public FileWriteDiagnostics {
            if (writeFailureCount < 0L) throw new IllegalArgumentException("writeFailureCount must be non-negative");
            lastFailureType = lastFailureType == null ? null : lastFailureType.trim();
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
        }
    }

    enum RetentionState { NEVER_RUN, HEALTHY, DEGRADED, DOWN }

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
