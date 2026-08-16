package com.cpf.platform.operations.observability.api.logging;

import java.time.Instant;
import java.util.Objects;

/**
 * 제한된 비동기 거래 로그 Writer의 Runtime 상태를 조회하는 공개 운영 계약입니다.
 *
 * <p>구현체는 thread-safe해야 하며 snapshot 조회는 상태를 변경하지 않습니다.
 * terminal loss가 발생한 경우 health를 성공으로 축약하지 않고 DOWN으로 노출해야 합니다.</p>
 */
public interface CpfAsyncLogWriterOperations {
    /**
     * snapshot 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
     * @return Owner 경계에서 조회한 결과입니다. 조회 불가/부분 실패는 구현 계약에 따라 명시적으로 표현해야 합니다.
     */
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
/**
 * inFlightCount 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
 * @return 접수되었으나 완료되지 않은 추정 작업 수입니다.
 */

        public long inFlightCount() {
            return Math.max(0L, acceptedCount - completedCount);
        }
/**
 * health 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
 * @return UP/DEGRADED/DOWN/OUT_OF_SERVICE 중 현재 health 상태입니다.
 */

        public String health() {
            if (terminalLossCount > 0) return "DOWN";
            if (rejectedCount > 0 || fallbackPreservedCount > 0 || state == WriterState.DEGRADED) {
                return "DEGRADED";
            }
            return state == WriterState.CLOSED ? "OUT_OF_SERVICE" : "UP";
        }
    }
}
