package com.cpf.batch.spi;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionReservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** CPF 승인·감사 원장과 Spring Batch Metadata를 연결하는 Control Plane SPI입니다. */
public interface BatchExecutionLedgerPort {
    /** 동일 scope/key/hash는 기존 실행을 반환하고, 불변 값이 다르면 충돌로 실패합니다. */
    String reserve(BatchApprovedLaunchRequest request);

    /** 기대 상태 중 하나일 때만 상태를 전이합니다. */
    default void transition(
            String cpfExecutionId,
            Set<BatchControlState> expected,
            BatchControlState target,
            String reasonCode,
            String detail,
            Instant reconcileAfter) {
        throw new UnsupportedOperationException(
                "BatchExecutionLedgerPort implementation must support durable state transitions");
    }

    void bind(BatchExecutionLink link);

    default void recordUnknown(String cpfExecutionId, String reasonCode, String detail) {
        transition(cpfExecutionId,
                Set.of(BatchControlState.RESERVED, BatchControlState.STARTING, BatchControlState.STARTED,
                        BatchControlState.STOPPING, BatchControlState.UNKNOWN_RESULT),
                BatchControlState.UNKNOWN_RESULT, reasonCode, detail, Instant.now());
    }

    default Optional<BatchExecutionReservation> findReservation(String cpfExecutionId) {
        return Optional.empty();
    }
    List<BatchExecutionLink> findByCpfExecutionId(String cpfExecutionId);
}
