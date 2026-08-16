package com.cpf.platform.operations.observability.api.logging;

import java.time.Instant;
import java.util.Objects;

/** Runtime visibility for the bounded asynchronous file-log writer. */
/** CpfAsyncFileLogWriterOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfAsyncFileLogWriterOperations {
    FileWriterSnapshot fileWriterSnapshot();

    enum FileWriterState { RUNNING, DEGRADED, DRAINING, CLOSED }

    record FileWriterSnapshot(
            boolean accepting,
            boolean workerAlive,
            int queueDepth,
            int queueCapacity,
            long acceptedCount,
            long writtenCount,
            long callerRunsCount,
            long rejectedCount,
            long failedCount,
            long terminalLossCount,
            String lastFailureType,
            Instant capturedAt,
            FileWriterState state) {
        public FileWriterSnapshot {
            if (queueDepth < 0 || queueCapacity < 1 || queueDepth > queueCapacity) {
                throw new IllegalArgumentException("invalid file writer queue metrics");
            }
            if (acceptedCount < 0 || writtenCount < 0 || callerRunsCount < 0
                    || rejectedCount < 0 || failedCount < 0 || terminalLossCount < 0) {
                throw new IllegalArgumentException("file writer counters must be non-negative");
            }
            capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
            state = Objects.requireNonNull(state, "state");
        }

        /** inFlightCount 작업을 CPF 표준 계약에 따라 수행한다. */
        public long inFlightCount() {
            return Math.max(0L, acceptedCount - writtenCount - failedCount);
        }

        public String health() {
            if (terminalLossCount > 0L || !workerAlive && state == FileWriterState.RUNNING) {
                return "DOWN";
            }
            if (rejectedCount > 0L || failedCount > 0L || state == FileWriterState.DEGRADED) {
                return "DEGRADED";
            }
            return state == FileWriterState.CLOSED ? "OUT_OF_SERVICE" : "UP";
        }
    }
}
