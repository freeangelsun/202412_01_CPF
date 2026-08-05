package com.cpf.core.api.logging;

import java.time.Instant;

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
        public String health() {
            if (terminalLossCount > 0) return "DOWN";
            if (rejectedCount > 0 || fallbackPreservedCount > 0) return "DEGRADED";
            return state == WriterState.CLOSED ? "OUT_OF_SERVICE" : "UP";
        }
    }
}
