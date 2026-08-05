package com.cpf.core.api.logging;

import java.time.Instant;
import java.util.Objects;

/** Safe operational status for transaction-log fallback and recovery processing. */
public interface CpfLogRecoveryRuntimeStatus {
    RecoveryRuntimeSnapshot recoveryRuntimeSnapshot();

    /** Operational failures that must not be confused with journal persistence failures. */
    default RecoveryOperationalDiagnostics recoveryOperationalDiagnostics() {
        return new RecoveryOperationalDiagnostics(0L, 0L, Instant.EPOCH);
    }

    record RecoveryOperationalDiagnostics(
            long operationalEventFailureCount,
            long unknownResultCount,
            Instant capturedAt) {
        public RecoveryOperationalDiagnostics {
            if (operationalEventFailureCount < 0L || unknownResultCount < 0L) {
                throw new IllegalArgumentException("operational recovery counters must be non-negative");
            }
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
        }
    }

    enum RecoveryState { IDLE, RUNNING, DEGRADED, DOWN }

    record RecoveryRuntimeSnapshot(
            boolean scheduledRecoveryEnabled,
            boolean running,
            int batchSize,
            int maxAttempts,
            long recoveredCount,
            long failedAttemptCount,
            int pendingCount,
            int processingCount,
            int poisonCount,
            long spoolBytes,
            long maxSpoolBytes,
            long enqueueFailureCount,
            long staleReclaimedCount,
            long malformedPoisonCount,
            long poisonRetryCount,
            long staleClaimConflictCount,
            String spoolDirectory,
            Instant capturedAt,
            RecoveryState state) {
        public RecoveryRuntimeSnapshot {
            if (batchSize < 1 || maxAttempts < 1) {
                throw new IllegalArgumentException("invalid recovery policy bounds");
            }
            if (recoveredCount < 0 || failedAttemptCount < 0 || pendingCount < 0
                    || processingCount < 0 || poisonCount < 0 || spoolBytes < 0 || maxSpoolBytes < 1
                    || enqueueFailureCount < 0 || staleReclaimedCount < 0 || malformedPoisonCount < 0
                    || poisonRetryCount < 0 || staleClaimConflictCount < 0) {
                throw new IllegalArgumentException("recovery counters must be non-negative");
            }
            spoolDirectory = requireRelativePath(spoolDirectory);
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
            state = Objects.requireNonNull(state, "state");
        }

        public String health() {
            return switch (state) {
                case DOWN -> "DOWN";
                case DEGRADED -> "DEGRADED";
                case IDLE, RUNNING -> "UP";
            };
        }

        private static String requireRelativePath(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("spoolDirectory is required");
            }
            String normalized = value.trim().replace('\\', '/');
            if (normalized.startsWith("/") || normalized.contains("../") || normalized.equals("..")) {
                throw new IllegalArgumentException("spoolDirectory must be repository/runtime relative");
            }
            return normalized;
        }
    }
}
