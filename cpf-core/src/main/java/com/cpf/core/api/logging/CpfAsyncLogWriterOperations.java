package com.cpf.core.api.logging;

import java.time.Instant;
import java.util.Objects;

/** Runtime visibility for the bounded asynchronous transaction-log writer. */
public interface CpfAsyncLogWriterOperations {
    WriterSnapshot snapshot();

    enum WriterState { RUNNING, DEGRADED, DRAINING, CLOSED }

    record WriterSnapshot(
            boolean asynchronous,
            int workerCount,
            int queueCapacity,
            int queuedCount,
            int activeCount,
            long acceptedCount,
            long completedCount,
            long rejectedCount,
            long fallbackPreservedCount,
            long terminalLossCount,
            Instant lastFailureAt,
            Instant lastTerminalLossAt,
            WriterState state) {
        public WriterSnapshot {
            if (workerCount < 0 || queueCapacity < 1 || queuedCount < 0
                    || queuedCount > queueCapacity || activeCount < 0 || activeCount > workerCount) {
                throw new IllegalArgumentException("invalid asynchronous writer capacity metrics");
            }
            if (asynchronous && workerCount < 1) {
                throw new IllegalArgumentException("asynchronous writer requires at least one worker");
            }
            if (!asynchronous && (queuedCount != 0 || activeCount != 0)) {
                throw new IllegalArgumentException("synchronous writer cannot report queued or active work");
            }
            if (acceptedCount < 0L || completedCount < 0L || rejectedCount < 0L
                    || fallbackPreservedCount < 0L || terminalLossCount < 0L) {
                throw new IllegalArgumentException("writer counters must be non-negative");
            }
            if (completedCount > acceptedCount) {
                throw new IllegalArgumentException("completedCount cannot exceed acceptedCount");
            }
            if (terminalLossCount > 0L && lastTerminalLossAt == null) {
                throw new IllegalArgumentException("terminal loss timestamp is required");
            }
            state = Objects.requireNonNull(state, "state");
        }

        public long inFlightCount() {
            return Math.max(0L, acceptedCount - completedCount);
        }

        public String health() {
            if (terminalLossCount > 0) return "DOWN";
            if (rejectedCount > 0 || fallbackPreservedCount > 0 || state == WriterState.DEGRADED) {
                return "DEGRADED";
            }
            return state == WriterState.CLOSED ? "OUT_OF_SERVICE" : "UP";
        }
    }
}
