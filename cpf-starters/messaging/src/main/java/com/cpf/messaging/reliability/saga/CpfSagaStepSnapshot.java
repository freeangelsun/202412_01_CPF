package com.cpf.messaging.reliability.saga;

/** Saga Step 실행/보상 시도와 결과의 불변 조회 Snapshot입니다. */
public record CpfSagaStepSnapshot(
        int stepNo,
        String stepId,
        CpfSagaStepStatus status,
        String resultCode,
        String resultSnapshot,
        String errorMessage,
        int executeAttempts,
        int compensationAttempts) { }
