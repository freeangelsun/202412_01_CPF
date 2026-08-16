package com.cpf.messaging.reliability.saga;

import java.util.List;

/** Saga durable 상태와 Step 이력의 불변 조회 Snapshot입니다. */
public record CpfSagaSnapshot(
        String sagaId,
        String sagaType,
        String businessKey,
        String transactionId,
        CpfSagaStatus status,
        int version,
        String errorMessage,
        List<CpfSagaStepSnapshot> steps) {
    public CpfSagaSnapshot {
        steps = List.copyOf(steps == null ? List.of() : steps);
    }
}
